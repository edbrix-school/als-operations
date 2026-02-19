package com.asg.operations.portcalloperation.client;

import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.portcalloperation.dto.PcInfoAttachmentDto;
import com.asg.operations.portcalloperation.dto.PcInfoAttachmentUploadResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.HashSet;
import java.util.Set;

/**
 * HTTP client to call the common attachment service for upload and list operations.
 * Uses docId = PORT_CALL_OPERATION and docKeyPoid = transactionPoid.
 */
@Slf4j
@Component
public class CommonAttachmentServiceClient {

    /**
     * docId used when calling common attachment service for port call operations (main screen PC Info attachments).
     */
    public static final String DOC_ID_PORT_CALL_OPERATION = "110-163";

    /**
     * docId for other details screen (OPS_PC_OPERATION_HDR.PDA_FDA_ATTACHMENTS).
     */
    public static final String DOC_ID_PDA_FDA = "110-163-PDA-FDA";

    /**
     * docId for berthing screen side drawer (OPS_PC_EST_BERT_DTL.BERTHING_ATTACHMENTS).
     */
    public static final String DOC_ID_BERTHING = "110-163-BERTHING";

    /**
     * docId for pre-arrival screen side drawer (OPS_PC_EST_PREARRIVAL_DTL.PRE_ARRIVAL_ATTACHMENTS).
     */
    public static final String DOC_ID_PREARRIVAL = "110-163-PREARRIVAL";

    /**
     * docId for actual timing screen (OPS_PC_ACT_TIMING_DTL.TIMING_ATTACHMENTS).
     */
    public static final String DOC_ID_TIMING = "110-163-TIMING";

    /**
     * docId for husbandry crew screen (OPS_PC_HUSBANDRY_CREW_DTL.CREW_ATTACHMENTS).
     */
    public static final String DOC_ID_HUSBANDRY_CREW = "110-163-HUSB-CREW";

    /**
     * docId for husbandry other details screen (OPS_PC_HUSBANDRY_OTH_DTL.ARRNGMNT_ATTACHMENTS).
     */
    public static final String DOC_ID_HUSBANDRY_OTH = "110-163-HUSB-OTH";

    /**
     * docId for docs copy screen (OPS_PC_DOCS_COPY_DTL.DOCUMENT_ATTACHMENTS).
     */
    public static final String DOC_ID_DOCS_COPY = "110-163-DOCS-COPY";


    /**
     * Encode (transactionPoid, detRowId) as a unique docKeyPoid for detail tables.
     */
    public static long toDetailDocKeyPoid(long transactionPoid, long detRowId) {
        return transactionPoid * 1_000_000_000L + detRowId;
    }

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public CommonAttachmentServiceClient(RestTemplate restTemplate, @Value("${common.service.attachment.base-url:}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
    }

    public boolean isConfigured() {
        return !baseUrl.isEmpty();
    }

    /**
     * Add X-User-* headers from UserContext so common-services (AsgCommonInterceptor) can set user context.
     * Same pattern as GenericRestClient in common-lib and RoutingService in security gateway.
     */
    private void addUserContextHeaders(HttpHeaders headers) {
        headers.add("X-User-Name", UserContext.getUserName());
        headers.add("X-User-Poid", UserContext.getUserPoid() != null ? UserContext.getUserPoid().toString() : null);
        headers.add("X-User-Id", UserContext.getUserId());
        headers.add("X-User-Email", UserContext.getUserEmail());
        headers.add("X-User-Role", UserContext.getUserRole());
        headers.add("X-Group-Poid", UserContext.getGroupPoid() != null ? UserContext.getGroupPoid().toString() : null);
        if (!headers.containsKey("X-Company-Poid") || StringUtils.isBlank(headers.getFirst("X-Company-Poid"))) {
            headers.add("X-Company-Poid", UserContext.getCompanyPoid() != null ? UserContext.getCompanyPoid().toString() : null);
        }
    }

    /**
     * Upload for main screen PC Info (docId=110-163, docKeyPoid=transactionPoid).
     */
    public PcInfoAttachmentUploadResponseDto upload(Long docKeyPoid, MultipartFile[] files, String[] remarks, String[] checklistNames) {
        return upload(DOC_ID_PORT_CALL_OPERATION, docKeyPoid, files, remarks, checklistNames);
    }

    /**
     * Upload files to common attachment service with given docId and docKeyPoid.
     * Use DOC_ID_PORT_CALL_OPERATION + transactionPoid for main screen, DOC_ID_DRAWER + emailPoid for drawer.
     */
    public PcInfoAttachmentUploadResponseDto upload(String docId, Long docKeyPoid, MultipartFile[] files, String[] remarks, String[] checklistNames) {
        if (!isConfigured()) {
            throw new IllegalStateException("Common attachment service base URL is not configured (common.service.attachment.base-url)");
        }
        String url = baseUrl + "/v1/attachments/" + docId + "/" + docKeyPoid + "/upload";

        int filesReceived = files != null ? files.length : 0;
        log.debug("upload: docId={}, docKeyPoid={}, url={}, filesReceived={}", docId, docKeyPoid, url, filesReceived);

        // Fetch existing attachments to check for duplicate filenames
        Set<String> existingFilenames = getExistingFilenames(docId, docKeyPoid);
        log.info("upload: found {} existing filenames for docKeyPoid={}: {}", existingFilenames.size(), docKeyPoid, existingFilenames);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    log.debug("upload: skip null/empty file");
                    continue;
                }
                byte[] bytes;
                try {
                    bytes = file.getBytes();
                } catch (Exception e) {
                    log.warn("Skip file {}: {}", file.getOriginalFilename(), e.getMessage());
                    continue;
                }
                String originalFilename = filenameOnly(file.getOriginalFilename());
                String uniqueFilename = makeUniqueFilename(originalFilename, existingFilenames);
                if (!uniqueFilename.equals(originalFilename)) {
                    log.info("upload: renamed '{}' to '{}' to ensure uniqueness", originalFilename, uniqueFilename);
                }
                existingFilenames.add(uniqueFilename); // Track in this batch too
                body.add("files", new ByteArrayResource(bytes) {
                    @Override
                    public String getFilename() {
                        return uniqueFilename;
                    }
                });
                log.debug("upload: added file part name={} size={}", uniqueFilename, bytes.length);
            }
        }
        int filesParts = body.get("files") != null ? Objects.requireNonNull(body.get("files")).size() : 0;
        log.info("upload: docId={}, docKeyPoid={}, body files parts={}, remarks={}, checklistName={}",
                docId, docKeyPoid, filesParts,
                body.get("remarks") != null ? Objects.requireNonNull(body.get("remarks")).size() : 0,
                body.get("checklistName") != null ? Objects.requireNonNull(body.get("checklistName")).size() : 0);
        if (filesReceived > 0 && filesParts == 0) {
            log.warn("upload: received {} file(s) but added 0 parts (all null/empty or failed getBytes?)", filesReceived);
        }
        if (remarks != null) {
            for (String r : remarks) {
                body.add("remarks", r != null ? r : "");
            }
        }
        if (checklistNames != null) {
            for (String c : checklistNames) {
                body.add("checklistName", c != null ? c : "");
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        addUserContextHeaders(headers);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }
            );
            Map<String, Object> responseBody = response.getBody();
            log.info("upload: common-services response status={}, body={}", response.getStatusCode(), responseBody);
            return parseUploadResponse(responseBody);
        } catch (Exception e) {
            log.error("Common attachment service upload failed for docId={}, docKeyPoid={}: {}", docId, docKeyPoid, e.getMessage());
            throw new RuntimeException("Attachment upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Strip path to only the file name (no directory).
     */
    private static String filenameOnly(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) return "file";
        int last = Math.max(originalFilename.lastIndexOf('/'), originalFilename.lastIndexOf('\\'));
        return last >= 0 ? originalFilename.substring(last + 1) : originalFilename;
    }

    /**
     * Fetch existing attachment filenames for the given (docId, docKeyPoid) to check for duplicates.
     */
    @SuppressWarnings("unchecked")
    private Set<String> getExistingFilenames(String docId, Long docKeyPoid) {
        try {
            Map<String, Object> listResponse = listAttachments(docId, docKeyPoid, 0, 1000); // Get first 1000 attachments
            if (listResponse == null) {
                log.debug("getExistingFilenames: listResponse is null");
                return new HashSet<>();
            }
            log.debug("getExistingFilenames: listResponse keys={}", listResponse.keySet());

            // Try different response structures: {content: [...]} or {result: {data: {content: [...]}}}
            Object content = null;
            if (listResponse.containsKey("content")) {
                content = listResponse.get("content");
            } else if (listResponse.containsKey("result")) {
                Object result = listResponse.get("result");
                if (result instanceof Map) {
                    Map<String, Object> resultMap = (Map<String, Object>) result;
                    Object data = resultMap.get("data");
                    if (data instanceof Map) {
                        Map<String, Object> dataMap = (Map<String, Object>) data;
                        content = dataMap.get("content");
                    }
                }
            } else if (listResponse.containsKey("data")) {
                Object data = listResponse.get("data");
                if (data instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) data;
                    content = dataMap.get("content");
                }
            }

            if (!(content instanceof List)) {
                log.debug("getExistingFilenames: content is not a List, content={}", content);
                return new HashSet<>();
            }

            Set<String> filenames = new HashSet<>();
            for (Object item : (List<?>) content) {
                if (item instanceof Map) {
                    Map<String, Object> itemMap = (Map<String, Object>) item;
                    Object origName = itemMap.get("originalFileName");
                    if (origName != null) {
                        filenames.add(origName.toString());
                    }
                }
            }
            log.debug("getExistingFilenames: extracted {} filenames: {}", filenames.size(), filenames);
            return filenames;
        } catch (Exception e) {
            log.warn("Failed to fetch existing filenames for docId={}, docKeyPoid={}: {}", docId, docKeyPoid, e.getMessage(), e);
            return new HashSet<>(); // Return empty set on error - will try upload and let server reject if duplicate
        }
    }

    /**
     * Make filename unique by appending a timestamp suffix if it already exists.
     * Format: "filename_yyyyMMddHHmmss.ext" or "filename_yyyyMMddHHmmss_1.ext" if still duplicate.
     */
    private String makeUniqueFilename(String filename, Set<String> existingFilenames) {
        log.debug("makeUniqueFilename: checking '{}' against {} existing filenames", filename, existingFilenames.size());
        if (!existingFilenames.contains(filename)) {
            log.debug("makeUniqueFilename: '{}' is unique", filename);
            return filename; // Already unique
        }
        log.info("makeUniqueFilename: '{}' already exists, generating unique name", filename);
        // Extract name and extension
        int lastDot = filename.lastIndexOf('.');
        String baseName;
        String extension;
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            baseName = filename.substring(0, lastDot);
            extension = filename.substring(lastDot);
        } else {
            baseName = filename;
            extension = "";
        }
        // Try timestamp suffix
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String candidate = baseName + "_" + timestamp + extension;
        if (!existingFilenames.contains(candidate)) {
            return candidate;
        }
        // If timestamp still conflicts, add counter
        int counter = 1;
        do {
            candidate = baseName + "_" + timestamp + "_" + counter + extension;
            counter++;
        } while (existingFilenames.contains(candidate) && counter < 1000);
        return candidate;
    }

    /**
     * List attachments for main screen PC Info (docId=110-163, docKeyPoid=transactionPoid).
     */
    public Map<String, Object> listAttachments(Long docKeyPoid, int page, int size) {
        return listAttachments(DOC_ID_PORT_CALL_OPERATION, docKeyPoid, page, size);
    }

    /**
     * List attachments from common service (active only, paginated) for given docId and docKeyPoid.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> listAttachments(String docId, Long docKeyPoid, int page, int size) {
        if (!isConfigured()) {
            throw new IllegalStateException("Common attachment service base URL is not configured (common.service.attachment.base-url)");
        }
        String url = baseUrl + "/v1/attachments/" + docId + "/" + docKeyPoid
                + "/list?includeArchived=false&page=" + page + "&size=" + size;

        HttpHeaders headers = new HttpHeaders();
        addUserContextHeaders(headers);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }
            );
            return response.getBody() != null ? response.getBody() : new HashMap<>();
        } catch (Exception e) {
            log.error("Common attachment service list failed for docId={}, docKeyPoid={}: {}", docId, docKeyPoid, e.getMessage());
            throw new RuntimeException("Attachment list failed: " + e.getMessage(), e);
        }
    }

    /**
     * Download attachment for main screen PC Info (docId=110-163, docKeyPoid=transactionPoid).
     */
    public ResponseEntity<org.springframework.core.io.Resource> downloadAttachment(Long docKeyPoid, String storedFileName) {
        return downloadAttachment(DOC_ID_PORT_CALL_OPERATION, docKeyPoid, storedFileName);
    }

    /**
     * Download an attachment file from common service by docId, docKeyPoid and storedFileName.
     */
    public ResponseEntity<org.springframework.core.io.Resource> downloadAttachment(String docId, Long docKeyPoid, String storedFileName) {
        if (!isConfigured()) {
            throw new IllegalStateException("Common attachment service base URL is not configured (common.service.attachment.base-url)");
        }
        String url = baseUrl + "/v1/attachments/" + docId + "/" + docKeyPoid + "/" + storedFileName + "/download";

        HttpHeaders headers = new HttpHeaders();
        addUserContextHeaders(headers);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<org.springframework.core.io.Resource> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    org.springframework.core.io.Resource.class
            );
            log.debug("Downloaded attachment storedFileName={} for docId={}, docKeyPoid={}", storedFileName, docId, docKeyPoid);
            return response;
        } catch (Exception e) {
            log.error("Common attachment service download failed for docId={}, docKeyPoid={}, storedFileName={}: {}",
                    docId, docKeyPoid, storedFileName, e.getMessage());
            throw new RuntimeException("Attachment download failed: " + e.getMessage(), e);
        }
    }

    /**
     * Delete attachment for main screen PC Info (for compensating transactions).
     */
    public void deleteAttachment(Long docKeyPoid, String storedFileName) {
        deleteAttachment(DOC_ID_PORT_CALL_OPERATION, docKeyPoid, storedFileName);
    }

    /**
     * Delete an attachment from common service by docId, docKeyPoid and storedFileName.
     */
    public void deleteAttachment(String docId, Long docKeyPoid, String storedFileName) {
        if (!isConfigured()) {
            throw new IllegalStateException("Common attachment service base URL is not configured (common.service.attachment.base-url)");
        }
        String url = baseUrl + "/v1/attachments/" + docId + "/" + docKeyPoid + "/" + storedFileName;

        HttpHeaders headers = new HttpHeaders();
        addUserContextHeaders(headers);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            log.debug("Deleted attachment storedFileName={} for docId={}, docKeyPoid={}", storedFileName, docId, docKeyPoid);
        } catch (Exception e) {
            log.error("Common attachment service delete failed for docId={}, docKeyPoid={}, storedFileName={}: {}",
                    docId, docKeyPoid, storedFileName, e.getMessage());
            throw new RuntimeException("Attachment delete failed: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private PcInfoAttachmentUploadResponseDto parseUploadResponse(Map<String, Object> body) {
        if (body == null) {
            return new PcInfoAttachmentUploadResponseDto(Collections.emptyList(), Collections.emptyList(), false);
        }
        // Common-services wraps response as: {success, message, statusCode, result: {data: {...}}}
        Object result = body.get("result");
        if (result == null || !(result instanceof Map)) {
            log.warn("parseUploadResponse: no 'result' in response body, trying 'data' directly");
            // Fallback: try direct 'data' key (for backward compatibility)
            result = body;
        }
        Map<String, Object> resultMap = (Map<String, Object>) result;
        Object data = resultMap.get("data");
        if (data == null || !(data instanceof Map)) {
            log.warn("parseUploadResponse: no 'data' in result, body keys: {}", body.keySet());
            return new PcInfoAttachmentUploadResponseDto(Collections.emptyList(), Collections.emptyList(), false);
        }
        Map<String, Object> dataMap = (Map<String, Object>) data;
        List<PcInfoAttachmentDto> uploaded = mapAttachmentList(dataMap.get("uploadedFiles"));
        List<String> errors = dataMap.get("errors") instanceof List
                ? (List<String>) dataMap.get("errors")
                : Collections.emptyList();
        boolean hasErrors = Boolean.TRUE.equals(dataMap.get("hasErrors")) || (errors != null && !errors.isEmpty());
        log.debug("parseUploadResponse: uploaded={}, errors={}, hasErrors={}", uploaded.size(), errors, hasErrors);
        return new PcInfoAttachmentUploadResponseDto(uploaded, errors != null ? errors : Collections.emptyList(), hasErrors);
    }

    @SuppressWarnings("unchecked")
    private List<PcInfoAttachmentDto> mapAttachmentList(Object list) {
        if (list == null || !(list instanceof List)) {
            return Collections.emptyList();
        }
        List<PcInfoAttachmentDto> result = new ArrayList<>();
        for (Object item : (List<?>) list) {
            if (item instanceof Map) {
                result.add(mapAttachmentDto((Map<String, Object>) item));
            }
        }
        return result;
    }

    private PcInfoAttachmentDto mapAttachmentDto(Map<String, Object> m) {
        PcInfoAttachmentDto dto = new PcInfoAttachmentDto();
        if (m.get("seqNo") != null) dto.setSeqNo(((Number) m.get("seqNo")).longValue());
        if (m.get("originalFileName") != null) dto.setOriginalFileName(m.get("originalFileName").toString());
        if (m.get("storedFileName") != null) dto.setStoredFileName(m.get("storedFileName").toString());
        if (m.get("remarks") != null) dto.setRemarks(m.get("remarks").toString());
        if (m.get("checklistName") != null) dto.setChecklistName(m.get("checklistName").toString());
        if (m.get("uploadedBy") != null) dto.setUploadedBy(m.get("uploadedBy").toString());
        if (m.get("createdDate") != null) dto.setCreatedDate(m.get("createdDate").toString());
        if (m.get("active") != null) dto.setActive(Boolean.TRUE.equals(m.get("active")));
        if (m.get("deleted") != null) dto.setDeleted(Boolean.TRUE.equals(m.get("deleted")));
        return dto;
    }
}
