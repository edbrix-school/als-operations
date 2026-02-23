package com.asg.operations.portcalloperation.service.impl;

import com.asg.operations.portcalloperation.client.CommonAttachmentServiceClient;
import com.asg.operations.portcalloperation.dto.PcInfoAttachmentDto;
import com.asg.operations.portcalloperation.dto.PcInfoAttachmentUploadResponseDto;
import com.asg.operations.portcalloperation.entity.PortCallOperationHdr;
import com.asg.operations.portcalloperation.repository.PortCallOperationHdrRepository;
import com.asg.operations.portcalloperation.service.PortCallOperationPcInfoAttachmentService;
import com.asg.operations.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Saves and retrieves PC Info attachments via common attachment service and
 * keeps PC_INFO_ATTACHMENTS on PortCallOperationHdr in sync (comma-separated names).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortCallOperationPcInfoAttachmentServiceImpl implements PortCallOperationPcInfoAttachmentService {

    private static final String ATTACHMENT_NAMES_SEPARATOR = ",";

    private final CommonAttachmentServiceClient attachmentClient;
    private final PortCallOperationHdrRepository hdrRepository;

    @Override
    @Transactional
    public PcInfoAttachmentUploadResponseDto uploadPcInfoAttachments(Long transactionPoid, MultipartFile[] files, String[] remarks, String[] checklistNames) {
        if (!attachmentClient.isConfigured()) {
            throw new IllegalStateException("Common attachment service is not configured. Set common.service.attachment.base-url.");
        }
        PortCallOperationHdr hdr = hdrRepository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid));

        if (files == null || files.length == 0) {
            log.warn("uploadPcInfoAttachments: no files received (files={})", files == null ? "null" : "length=" + files.length);
            return new PcInfoAttachmentUploadResponseDto(Collections.emptyList(), List.of("No files provided"), true);
        }
        int nonEmpty = (int) java.util.Arrays.stream(files).filter(f -> f != null && !f.isEmpty()).count();
        log.info("uploadPcInfoAttachments: transactionPoid={}, filesCount={}, nonEmpty={}", transactionPoid, files.length, nonEmpty);

        PcInfoAttachmentUploadResponseDto response = attachmentClient.upload(transactionPoid, files, remarks, checklistNames);

        log.info("uploadPcInfoAttachments: common-services returned uploadedCount={}, errors={}",
                response.getUploadedFiles() != null ? response.getUploadedFiles().size() : 0,
                response.getErrors() != null ? response.getErrors() : List.of());

        // If upload had errors or no files uploaded, return early
        if (response.getUploadedFiles() == null || response.getUploadedFiles().isEmpty()) {
            return response;
        }

        // Track uploaded files for potential rollback if PC_INFO_ATTACHMENTS update fails
        List<PcInfoAttachmentDto> uploadedFiles = new ArrayList<>(response.getUploadedFiles());

        try {
            String newNames = uploadedFiles.stream()
                    .map(PcInfoAttachmentDto::getOriginalFileName)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.joining(ATTACHMENT_NAMES_SEPARATOR));
            if (!newNames.isEmpty()) {
                String existing = hdr.getPcInfoAttachments();
                String updated = existing == null || existing.isBlank()
                        ? newNames
                        : existing.trim() + ATTACHMENT_NAMES_SEPARATOR + newNames;
                hdr.setPcInfoAttachments(updated);
                hdrRepository.save(hdr);
                log.info("Updated PC_INFO_ATTACHMENTS for transactionPoid={}, appended {} file(s)", transactionPoid, uploadedFiles.size());
            }
        } catch (Exception e) {
            // Compensating transaction: If updating PC_INFO_ATTACHMENTS fails, delete the uploaded files
            log.error("Failed to update PC_INFO_ATTACHMENTS for transactionPoid={}, rolling back uploaded files: {}", transactionPoid, e.getMessage(), e);
            try {
                deleteUploadedFiles(transactionPoid, uploadedFiles);
                log.info("Successfully rolled back {} uploaded file(s) after PC_INFO_ATTACHMENTS update failure", uploadedFiles.size());
            } catch (Exception rollbackEx) {
                log.error("Failed to rollback uploaded files for transactionPoid={}: {}", transactionPoid, rollbackEx.getMessage(), rollbackEx);
                // Note: Files remain in common-services but not tracked in operations - manual cleanup may be needed
            }
            // Re-throw the original exception so transaction rolls back
            throw new RuntimeException("Failed to update PC_INFO_ATTACHMENTS after successful file upload: " + e.getMessage(), e);
        }

        return response;
    }

    @Override
    public Map<String, Object> listPcInfoAttachments(Long transactionPoid, int page, int size) {
        if (!attachmentClient.isConfigured()) {
            throw new IllegalStateException("Common attachment service is not configured. Set common.service.attachment.base-url.");
        }
        hdrRepository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid));

        return attachmentClient.listAttachments(transactionPoid, page, size);
    }

    @Override
    public String getPcInfoAttachmentsSummary(Long transactionPoid) {
        return hdrRepository.findById(transactionPoid)
                .map(PortCallOperationHdr::getPcInfoAttachments)
                .orElse("");
    }

    @Override
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadAttachment(Long transactionPoid, String storedFileName) {
        if (!attachmentClient.isConfigured()) {
            throw new IllegalStateException("Common attachment service is not configured. Set common.service.attachment.base-url.");
        }
        hdrRepository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid));

        // Forward the ResponseEntity from common-services (includes headers like Content-Type, Content-Disposition)
        return attachmentClient.downloadAttachment(transactionPoid, storedFileName);
    }

    @Override
    @Transactional
    public void deletePcInfoAttachment(Long transactionPoid, String storedFileName) {
        if (!attachmentClient.isConfigured()) {
            throw new IllegalStateException("Common attachment service is not configured. Set common.service.attachment.base-url.");
        }
        PortCallOperationHdr entity = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid));
        
        // List attachments to find the originalFileName for the storedFileName
        Map<String, Object> attachmentsList = attachmentClient.listAttachments(transactionPoid, 0, 1000);
        String originalFileName = null;
        
        // Extract attachments from response
        List<Map<String, Object>> attachments = extractAttachmentsFromResponse(attachmentsList);
        for (Map<String, Object> attachment : attachments) {
            Object storedName = attachment.get("storedFileName");
            if (storedFileName.equals(storedName)) {
                Object origName = attachment.get("originalFileName");
                if (origName != null) {
                    originalFileName = origName.toString();
                }
                break;
            }
        }
        
        if (originalFileName == null) {
            throw new ResourceNotFoundException("Attachment", "storedFileName", storedFileName);
        }
        
        // Delete from attachment service
        attachmentClient.deleteAttachment(transactionPoid, storedFileName);
        
        // Remove originalFileName from pcInfoAttachments field
        String currentAttachments = entity.getPcInfoAttachments();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(currentAttachments)) {
            final String finalOriginalFileName = originalFileName;
            List<String> fileNames = new ArrayList<>(Arrays.asList(currentAttachments.split(ATTACHMENT_NAMES_SEPARATOR)));
            fileNames.removeIf(name -> name.trim().equals(finalOriginalFileName));
            String updatedAttachments = String.join(ATTACHMENT_NAMES_SEPARATOR, fileNames);
            entity.setPcInfoAttachments(updatedAttachments);
            hdrRepository.save(entity);
        }
    }

    @Override
    public boolean isAttachmentServiceAvailable() {
        return attachmentClient.isConfigured();
    }

    /**
     * Extract attachments list from the common attachment service response.
     * Handles different response structures (content, result.data.content, data.content).
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractAttachmentsFromResponse(Map<String, Object> response) {
        if (response == null) {
            return new ArrayList<>();
        }
        
        Object content = null;
        if (response.containsKey("content")) {
            content = response.get("content");
        } else if (response.containsKey("result")) {
            Object result = response.get("result");
            if (result instanceof Map) {
                Map<String, Object> resultMap = (Map<String, Object>) result;
                Object data = resultMap.get("data");
                if (data instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) data;
                    content = dataMap.get("content");
                }
            }
        } else if (response.containsKey("data")) {
            Object data = response.get("data");
            if (data instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) data;
                content = dataMap.get("content");
            }
        }
        
        if (content instanceof List) {
            List<Map<String, Object>> attachments = new ArrayList<>();
            for (Object item : (List<?>) content) {
                if (item instanceof Map) {
                    attachments.add((Map<String, Object>) item);
                }
            }
            return attachments;
        }
        
        return new ArrayList<>();
    }

    /**
     * Compensating transaction: Delete uploaded files from common-services if PC_INFO_ATTACHMENTS update fails.
     */
    private void deleteUploadedFiles(Long transactionPoid, List<PcInfoAttachmentDto> uploadedFiles) {
        for (PcInfoAttachmentDto file : uploadedFiles) {
            if (file.getStoredFileName() != null && !file.getStoredFileName().isBlank()) {
                try {
                    attachmentClient.deleteAttachment(transactionPoid, file.getStoredFileName());
                    log.debug("Deleted uploaded file {} (storedFileName: {})", file.getOriginalFileName(), file.getStoredFileName());
                } catch (Exception e) {
                    log.warn("Failed to delete uploaded file {} (storedFileName: {}): {}",
                            file.getOriginalFileName(), file.getStoredFileName(), e.getMessage());
                    // Continue deleting other files even if one fails
                }
            }
        }
    }
}
