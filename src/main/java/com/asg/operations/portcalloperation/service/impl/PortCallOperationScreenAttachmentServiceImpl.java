package com.asg.operations.portcalloperation.service.impl;

import com.asg.operations.portcalloperation.client.CommonAttachmentServiceClient;
import com.asg.operations.portcalloperation.dto.PcInfoAttachmentDto;
import com.asg.operations.portcalloperation.dto.PcInfoAttachmentUploadResponseDto;
import com.asg.operations.portcalloperation.entity.*;
import com.asg.operations.portcalloperation.repository.*;
import com.asg.operations.portcalloperation.service.PortCallOperationScreenAttachmentService;
import com.asg.operations.exceptions.ResourceNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Screen-specific side drawer attachments. Each screen uses its own docId and DB column;
 * docKeyPoid for detail tables is encoded as (transactionPoid * 1e9 + detRowId).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortCallOperationScreenAttachmentServiceImpl implements PortCallOperationScreenAttachmentService {

    private static final String SEP = ",";

    private final CommonAttachmentServiceClient attachmentClient;
    private final PortCallOperationEstBertDtlRepository estBertDtlRepository;
    private final PortCallOperationEstPrearrivalDtlRepository estPrearrivalDtlRepository;
    private final PortCallOperationHdrRepository hdrRepository;
    private final PortCallOperationHusbandryCrewDtlRepository husbandryCrewDtlRepository;
    private final PortCallOperationHusbandryOthDtlRepository husbandryOthDtlRepository;
    private final PortCallOperationDocsCopyDtlRepository docsCopyDtlRepository;
    private final PortCallOperationActTimingDtlRepository actTimingDtlRepository;

    private static long docKey(Long transactionPoid, Long detRowId) {
        return CommonAttachmentServiceClient.toDetailDocKeyPoid(transactionPoid, detRowId);
    }

    private static void appendAndSave(PcInfoAttachmentUploadResponseDto response, String existing,
                                      java.util.function.Consumer<String> setter, Runnable save,
                                      CommonAttachmentServiceClient client, String docId, long docKeyPoid,
                                      List<PcInfoAttachmentDto> uploadedFiles) {
        String newNames = uploadedFiles.stream()
                .map(PcInfoAttachmentDto::getOriginalFileName)
                .filter(name -> StringUtils.isNotBlank(name))
                .collect(Collectors.joining(SEP));
        if (newNames.isEmpty()) return;
        String updated = StringUtils.isBlank(existing) ? newNames : existing.trim() + SEP + newNames;
        setter.accept(updated);
        save.run();
    }

    private static void deleteUploaded(CommonAttachmentServiceClient client, String docId, long docKeyPoid, List<PcInfoAttachmentDto> files) {
        for (PcInfoAttachmentDto f : files) {
            if (StringUtils.isNotBlank(f.getStoredFileName())) {
                try {
                    client.deleteAttachment(docId, docKeyPoid, f.getStoredFileName());
                } catch (Exception e) {
                    log.warn("Rollback delete failed for {}: {}", f.getStoredFileName(), e.getMessage());
                }
            }
        }
    }

    private PcInfoAttachmentUploadResponseDto uploadDetail(String docId, long docKeyPoid,
                                                           String existingValue, java.util.function.Consumer<String> setter, Runnable save,
                                                           MultipartFile[] files, String[] remarks, String[] checklistNames) {
        if (!attachmentClient.isConfigured()) {
            throw new IllegalStateException("Common attachment service is not configured. Set common.service.attachment.base-url.");
        }
        if (files == null || files.length == 0) {
            return new PcInfoAttachmentUploadResponseDto(List.of(), List.of("No files provided"), true);
        }
        PcInfoAttachmentUploadResponseDto response = attachmentClient.upload(docId, docKeyPoid, files, remarks, checklistNames);
        if (response.getUploadedFiles() == null || response.getUploadedFiles().isEmpty()) return response;
        List<PcInfoAttachmentDto> uploaded = new ArrayList<>(response.getUploadedFiles());
        try {
            appendAndSave(response, existingValue, setter, save, attachmentClient, docId, docKeyPoid, uploaded);
        } catch (Exception e) {
            log.error("Failed to update attachment column, rolling back uploads: {}", e.getMessage(), e);
            deleteUploaded(attachmentClient, docId, docKeyPoid, uploaded);
            throw new RuntimeException("Failed to update attachment column after upload: " + e.getMessage(), e);
        }
        return response;
    }

    // ----- Berthing -----
    @Override
    @Transactional
    public PcInfoAttachmentUploadResponseDto uploadBerthingAttachments(Long transactionPoid, Long detRowId,
                                                                       MultipartFile[] files, String[] remarks, String[] checklistNames) {
        PortCallOperationEstBertDtl d = estBertDtlRepository.findById(new PortCallOperationEstBertDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Berthing detail", "detRowId", detRowId));
        long key = docKey(transactionPoid, detRowId);
        return uploadDetail(CommonAttachmentServiceClient.DOC_ID_BERTHING, key,
                d.getBerthingAttachments(), d::setBerthingAttachments, () -> estBertDtlRepository.save(d),
                files, remarks, checklistNames);
    }

    @Override
    public Map<String, Object> listBerthingAttachments(Long transactionPoid, Long detRowId, int page, int size) {
        ensureConfigured();
        resolveBerthing(transactionPoid, detRowId);
        return attachmentClient.listAttachments(CommonAttachmentServiceClient.DOC_ID_BERTHING, docKey(transactionPoid, detRowId), page, size);
    }

    @Override
    public String getBerthingAttachmentsSummary(Long transactionPoid, Long detRowId) {
        return nullToEmpty(resolveBerthing(transactionPoid, detRowId).getBerthingAttachments());
    }

    @Override
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadBerthingAttachment(Long transactionPoid, Long detRowId, String storedFileName) {
        ensureConfigured();
        resolveBerthing(transactionPoid, detRowId);
        return attachmentClient.downloadAttachment(CommonAttachmentServiceClient.DOC_ID_BERTHING, docKey(transactionPoid, detRowId), storedFileName);
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadAllBerthingAttachments(Long transactionPoid, Long detRowId) {
        ensureConfigured();
        resolveBerthing(transactionPoid, detRowId);
        return downloadAllForDetail(CommonAttachmentServiceClient.DOC_ID_BERTHING, transactionPoid, detRowId,
                "berthing-" + transactionPoid + "-" + detRowId + ".zip");
    }

    @Override
    @Transactional
    public void deleteBerthingAttachment(Long transactionPoid, Long detRowId, String storedFileName) {
        ensureConfigured();
        PortCallOperationEstBertDtl entity = resolveBerthing(transactionPoid, detRowId);
        long key = docKey(transactionPoid, detRowId);

        // List attachments to find the originalFileName for the storedFileName
        Map<String, Object> attachmentsList = attachmentClient.listAttachments(CommonAttachmentServiceClient.DOC_ID_BERTHING, key, 0, 1000);
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
        attachmentClient.deleteAttachment(CommonAttachmentServiceClient.DOC_ID_BERTHING, key, storedFileName);

        // Remove originalFileName from berthingAttachments field
        String currentAttachments = entity.getBerthingAttachments();
        if (StringUtils.isNotBlank(currentAttachments)) {
            final String finalOriginalFileName = originalFileName;
            List<String> fileNames = new ArrayList<>(Arrays.asList(currentAttachments.split(SEP)));
            fileNames.removeIf(name -> name.trim().equals(finalOriginalFileName));
            String updatedAttachments = String.join(SEP, fileNames);
            entity.setBerthingAttachments(updatedAttachments);
            estBertDtlRepository.save(entity);
        }
    }

    // ----- Pre-arrival -----
    @Override
    @Transactional
    public PcInfoAttachmentUploadResponseDto uploadPreArrivalAttachments(Long transactionPoid, Long detRowId,
                                                                         MultipartFile[] files, String[] remarks, String[] checklistNames) {
        PortCallOperationEstPrearrivalDtl d = estPrearrivalDtlRepository.findById(new PortCallOperationEstPrearrivalDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Pre-arrival detail", "detRowId", detRowId));
        long key = docKey(transactionPoid, detRowId);
        return uploadDetail(CommonAttachmentServiceClient.DOC_ID_PREARRIVAL, key,
                d.getPreArrivalAttachments(), d::setPreArrivalAttachments, () -> estPrearrivalDtlRepository.save(d),
                files, remarks, checklistNames);
    }

    @Override
    public Map<String, Object> listPreArrivalAttachments(Long transactionPoid, Long detRowId, int page, int size) {
        ensureConfigured();
        resolvePreArrival(transactionPoid, detRowId);
        return attachmentClient.listAttachments(CommonAttachmentServiceClient.DOC_ID_PREARRIVAL, docKey(transactionPoid, detRowId), page, size);
    }

    @Override
    public String getPreArrivalAttachmentsSummary(Long transactionPoid, Long detRowId) {
        return nullToEmpty(resolvePreArrival(transactionPoid, detRowId).getPreArrivalAttachments());
    }

    @Override
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadPreArrivalAttachment(Long transactionPoid, Long detRowId, String storedFileName) {
        ensureConfigured();
        resolvePreArrival(transactionPoid, detRowId);
        return attachmentClient.downloadAttachment(CommonAttachmentServiceClient.DOC_ID_PREARRIVAL, docKey(transactionPoid, detRowId), storedFileName);
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadAllPreArrivalAttachments(Long transactionPoid, Long detRowId) {
        ensureConfigured();
        resolvePreArrival(transactionPoid, detRowId);
        return downloadAllForDetail(CommonAttachmentServiceClient.DOC_ID_PREARRIVAL, transactionPoid, detRowId,
                "pre-arrival-" + transactionPoid + "-" + detRowId + ".zip");
    }

    @Override
    @Transactional
    public void deletePreArrivalAttachment(Long transactionPoid, Long detRowId, String storedFileName) {
        ensureConfigured();
        PortCallOperationEstPrearrivalDtl entity = resolvePreArrival(transactionPoid, detRowId);
        long key = docKey(transactionPoid, detRowId);

        // List attachments to find the originalFileName for the storedFileName
        Map<String, Object> attachmentsList = attachmentClient.listAttachments(CommonAttachmentServiceClient.DOC_ID_PREARRIVAL, key, 0, 1000);
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
        attachmentClient.deleteAttachment(CommonAttachmentServiceClient.DOC_ID_PREARRIVAL, key, storedFileName);

        // Remove originalFileName from preArrivalAttachments field
        String currentAttachments = entity.getPreArrivalAttachments();
        if (StringUtils.isNotBlank(currentAttachments)) {
            final String finalOriginalFileName = originalFileName;
            List<String> fileNames = new ArrayList<>(Arrays.asList(currentAttachments.split(SEP)));
            fileNames.removeIf(name -> name.trim().equals(finalOriginalFileName));
            String updatedAttachments = String.join(SEP, fileNames);
            entity.setPreArrivalAttachments(updatedAttachments);
            estPrearrivalDtlRepository.save(entity);
        }
    }

    // ----- PDA-FDA (header) -----
    @Override
    @Transactional
    public PcInfoAttachmentUploadResponseDto uploadPdaFdaAttachments(Long transactionPoid,
                                                                     MultipartFile[] files, String[] remarks, String[] checklistNames) {
        PortCallOperationHdr h = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid));
        return uploadDetail(CommonAttachmentServiceClient.DOC_ID_PDA_FDA, transactionPoid,
                h.getPdaFdaAttachments(), h::setPdaFdaAttachments, () -> hdrRepository.save(h),
                files, remarks, checklistNames);
    }

    @Override
    public Map<String, Object> listPdaFdaAttachments(Long transactionPoid, int page, int size) {
        ensureConfigured();
        hdrRepository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid));
        return attachmentClient.listAttachments(CommonAttachmentServiceClient.DOC_ID_PDA_FDA, transactionPoid, page, size);
    }

    @Override
    public String getPdaFdaAttachmentsSummary(Long transactionPoid) {
        return nullToEmpty(hdrRepository.findById(transactionPoid).map(PortCallOperationHdr::getPdaFdaAttachments).orElse(null));
    }

    @Override
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadPdaFdaAttachment(Long transactionPoid, String storedFileName) {
        ensureConfigured();
        hdrRepository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid));
        return attachmentClient.downloadAttachment(CommonAttachmentServiceClient.DOC_ID_PDA_FDA, transactionPoid, storedFileName);
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadAllPdaFdaAttachments(Long transactionPoid) {
        ensureConfigured();
        hdrRepository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid));
        return downloadAllForHeader(CommonAttachmentServiceClient.DOC_ID_PDA_FDA, transactionPoid,
                "disbursement-other-details-" + transactionPoid + ".zip");
    }

    @Override
    @Transactional
    public void deletePdaFdaAttachment(Long transactionPoid, String storedFileName) {
        ensureConfigured();
        PortCallOperationHdr entity = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Port call operation", "Transaction Poid", transactionPoid));

        // List attachments to find the originalFileName for the storedFileName
        Map<String, Object> attachmentsList = attachmentClient.listAttachments(CommonAttachmentServiceClient.DOC_ID_PDA_FDA, transactionPoid, 0, 1000);
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
        attachmentClient.deleteAttachment(CommonAttachmentServiceClient.DOC_ID_PDA_FDA, transactionPoid, storedFileName);

        // Remove originalFileName from pdaFdaAttachments field
        String currentAttachments = entity.getPdaFdaAttachments();
        if (StringUtils.isNotBlank(currentAttachments)) {
            final String finalOriginalFileName = originalFileName;
            List<String> fileNames = new ArrayList<>(Arrays.asList(currentAttachments.split(SEP)));
            fileNames.removeIf(name -> name.trim().equals(finalOriginalFileName));
            String updatedAttachments = String.join(SEP, fileNames);
            entity.setPdaFdaAttachments(updatedAttachments);
            hdrRepository.save(entity);
        }
    }

    // ----- Husbandry crew -----
    @Override
    @Transactional
    public PcInfoAttachmentUploadResponseDto uploadHusbandryCrewAttachments(Long transactionPoid, Long detRowId,
                                                                            MultipartFile[] files, String[] remarks, String[] checklistNames) {
        PortCallOperationHusbandryCrewDtl d = husbandryCrewDtlRepository.findById(new PortCallOperationHusbandryCrewDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Husbandry crew detail", "detRowId", detRowId));
        long key = docKey(transactionPoid, detRowId);
        return uploadDetail(CommonAttachmentServiceClient.DOC_ID_HUSBANDRY_CREW, key,
                d.getCrewAttachments(), d::setCrewAttachments, () -> husbandryCrewDtlRepository.save(d),
                files, remarks, checklistNames);
    }

    @Override
    public Map<String, Object> listHusbandryCrewAttachments(Long transactionPoid, Long detRowId, int page, int size) {
        ensureConfigured();
        resolveHusbandryCrew(transactionPoid, detRowId);
        return attachmentClient.listAttachments(CommonAttachmentServiceClient.DOC_ID_HUSBANDRY_CREW, docKey(transactionPoid, detRowId), page, size);
    }

    @Override
    public String getHusbandryCrewAttachmentsSummary(Long transactionPoid, Long detRowId) {
        return nullToEmpty(resolveHusbandryCrew(transactionPoid, detRowId).getCrewAttachments());
    }

    @Override
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadHusbandryCrewAttachment(Long transactionPoid, Long detRowId, String storedFileName) {
        ensureConfigured();
        resolveHusbandryCrew(transactionPoid, detRowId);
        return attachmentClient.downloadAttachment(CommonAttachmentServiceClient.DOC_ID_HUSBANDRY_CREW, docKey(transactionPoid, detRowId), storedFileName);
    }

    // ----- Husbandry other -----
    @Override
    @Transactional
    public PcInfoAttachmentUploadResponseDto uploadHusbandryOthAttachments(Long transactionPoid, Long detRowId,
                                                                           MultipartFile[] files, String[] remarks, String[] checklistNames) {
        PortCallOperationHusbandryOthDtl d = husbandryOthDtlRepository.findById(new PortCallOperationHusbandryOthDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Husbandry other detail", "detRowId", detRowId));
        long key = docKey(transactionPoid, detRowId);
        return uploadDetail(CommonAttachmentServiceClient.DOC_ID_HUSBANDRY_OTH, key,
                d.getArrngmntAttachments(), d::setArrngmntAttachments, () -> husbandryOthDtlRepository.save(d),
                files, remarks, checklistNames);
    }

    @Override
    public Map<String, Object> listHusbandryOthAttachments(Long transactionPoid, Long detRowId, int page, int size) {
        ensureConfigured();
        resolveHusbandryOth(transactionPoid, detRowId);
        return attachmentClient.listAttachments(CommonAttachmentServiceClient.DOC_ID_HUSBANDRY_OTH, docKey(transactionPoid, detRowId), page, size);
    }

    @Override
    public String getHusbandryOthAttachmentsSummary(Long transactionPoid, Long detRowId) {
        return nullToEmpty(resolveHusbandryOth(transactionPoid, detRowId).getArrngmntAttachments());
    }

    @Override
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadHusbandryOthAttachment(Long transactionPoid, Long detRowId, String storedFileName) {
        ensureConfigured();
        resolveHusbandryOth(transactionPoid, detRowId);
        return attachmentClient.downloadAttachment(CommonAttachmentServiceClient.DOC_ID_HUSBANDRY_OTH, docKey(transactionPoid, detRowId), storedFileName);
    }

    // ----- Docs copy -----
    @Override
    @Transactional
    public PcInfoAttachmentUploadResponseDto uploadDocsCopyAttachments(Long transactionPoid, Long detRowId,
                                                                       MultipartFile[] files, String[] remarks, String[] checklistNames) {
        PortCallOperationDocsCopyDtl d = docsCopyDtlRepository.findById(new PortCallOperationDocsCopyDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Docs copy detail", "detRowId", detRowId));
        long key = docKey(transactionPoid, detRowId);
        return uploadDetail(CommonAttachmentServiceClient.DOC_ID_DOCS_COPY, key,
                d.getDocumentAttachments(), d::setDocumentAttachments, () -> docsCopyDtlRepository.save(d),
                files, remarks, checklistNames);
    }

    @Override
    public Map<String, Object> listDocsCopyAttachments(Long transactionPoid, Long detRowId, int page, int size) {
        ensureConfigured();
        resolveDocsCopy(transactionPoid, detRowId);
        return attachmentClient.listAttachments(CommonAttachmentServiceClient.DOC_ID_DOCS_COPY, docKey(transactionPoid, detRowId), page, size);
    }

    @Override
    public String getDocsCopyAttachmentsSummary(Long transactionPoid, Long detRowId) {
        return nullToEmpty(resolveDocsCopy(transactionPoid, detRowId).getDocumentAttachments());
    }

    @Override
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadDocsCopyAttachment(Long transactionPoid, Long detRowId, String storedFileName) {
        ensureConfigured();
        resolveDocsCopy(transactionPoid, detRowId);
        return attachmentClient.downloadAttachment(CommonAttachmentServiceClient.DOC_ID_DOCS_COPY, docKey(transactionPoid, detRowId), storedFileName);
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadAllDocsCopyAttachments(Long transactionPoid, Long detRowId) {
        ensureConfigured();
        resolveDocsCopy(transactionPoid, detRowId);
        return downloadAllForDetail(CommonAttachmentServiceClient.DOC_ID_DOCS_COPY, transactionPoid, detRowId,
                "docs-copy-" + transactionPoid + "-" + detRowId + ".zip");
    }

    @Override
    @Transactional
    public void deleteDocsCopyAttachment(Long transactionPoid, Long detRowId, String storedFileName) {
        ensureConfigured();
        PortCallOperationDocsCopyDtl entity = resolveDocsCopy(transactionPoid, detRowId);
        long key = docKey(transactionPoid, detRowId);

        // List attachments to find the originalFileName for the storedFileName
        Map<String, Object> attachmentsList = attachmentClient.listAttachments(CommonAttachmentServiceClient.DOC_ID_DOCS_COPY, key, 0, 1000);
        String originalFileName = null;
        int attachmentCount = 0;

        // Extract attachments from response
        List<Map<String, Object>> attachments = extractAttachmentsFromResponse(attachmentsList);
        for (Map<String, Object> attachment : attachments) {
            attachmentCount++;
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

        // Prevent deletion if it's the last file
        if (attachmentCount <= 1) {
            throw new ValidationException("Cannot delete the last attachment. Document attachments cannot be empty.");
        }

        // Delete from attachment service
        attachmentClient.deleteAttachment(CommonAttachmentServiceClient.DOC_ID_DOCS_COPY, key, storedFileName);

        // Remove originalFileName from documentAttachments field
        String currentAttachments = entity.getDocumentAttachments();
        if (StringUtils.isNotBlank(currentAttachments)) {
            final String finalOriginalFileName = originalFileName;
            List<String> fileNames = new ArrayList<>(Arrays.asList(currentAttachments.split(SEP)));
            fileNames.removeIf(name -> name.trim().equals(finalOriginalFileName));
            String updatedAttachments = String.join(SEP, fileNames);
            entity.setDocumentAttachments(updatedAttachments);
            docsCopyDtlRepository.save(entity);
        }
    }

    // ----- Actual timing (OPS_PC_ACT_TIMING_DTL.TIMING_ATTACHMENTS) -----

    @Override
    @Transactional
    public PcInfoAttachmentUploadResponseDto uploadTimingAttachments(Long transactionPoid, Long detRowId, MultipartFile[] files, String[] remarks, String[] checklistNames) {
        PortCallOperationActTimingDtl d = resolveTiming(transactionPoid, detRowId);
        long key = docKey(transactionPoid, detRowId);
        return uploadDetail(CommonAttachmentServiceClient.DOC_ID_TIMING, key,
                d.getTimingAttachments(), d::setTimingAttachments, () -> actTimingDtlRepository.save(d),
                files, remarks, checklistNames);
    }

    @Override
    public Map<String, Object> listTimingAttachments(Long transactionPoid, Long detRowId, int page, int size) {
        ensureConfigured();
        resolveTiming(transactionPoid, detRowId);
        return attachmentClient.listAttachments(CommonAttachmentServiceClient.DOC_ID_TIMING, docKey(transactionPoid, detRowId), page, size);
    }

    @Override
    public String getTimingAttachmentsSummary(Long transactionPoid, Long detRowId) {
        return nullToEmpty(resolveTiming(transactionPoid, detRowId).getTimingAttachments());
    }

    @Override
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadTimingAttachment(Long transactionPoid, Long detRowId, String storedFileName) {
        ensureConfigured();
        resolveTiming(transactionPoid, detRowId);
        return attachmentClient.downloadAttachment(CommonAttachmentServiceClient.DOC_ID_TIMING, docKey(transactionPoid, detRowId), storedFileName);
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadAllTimingAttachments(Long transactionPoid, Long detRowId) {
        ensureConfigured();
        resolveTiming(transactionPoid, detRowId);
        return downloadAllForDetail(CommonAttachmentServiceClient.DOC_ID_TIMING, transactionPoid, detRowId,
                "actual-timing-" + transactionPoid + "-" + detRowId + ".zip");
    }

    @Override
    @Transactional
    public void deleteTimingAttachment(Long transactionPoid, Long detRowId, String storedFileName) {
        ensureConfigured();
        PortCallOperationActTimingDtl entity = resolveTiming(transactionPoid, detRowId);
        long key = docKey(transactionPoid, detRowId);

        // List attachments to find the originalFileName for the storedFileName
        Map<String, Object> attachmentsList = attachmentClient.listAttachments(CommonAttachmentServiceClient.DOC_ID_TIMING, key, 0, 1000);
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
        attachmentClient.deleteAttachment(CommonAttachmentServiceClient.DOC_ID_TIMING, key, storedFileName);

        // Remove originalFileName from timingAttachments field
        String currentAttachments = entity.getTimingAttachments();
        if (StringUtils.isNotBlank(currentAttachments)) {
            final String finalOriginalFileName = originalFileName;
            List<String> fileNames = new ArrayList<>(Arrays.asList(currentAttachments.split(SEP)));
            fileNames.removeIf(name -> name.trim().equals(finalOriginalFileName));
            String updatedAttachments = String.join(SEP, fileNames);
            entity.setTimingAttachments(updatedAttachments);
            actTimingDtlRepository.save(entity);
        }
    }

    @Override
    public boolean isAttachmentServiceAvailable() {
        return attachmentClient.isConfigured();
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadAllHusbandryCrewAttachments(Long transactionPoid, Long detRowId) {
        return downloadAllForDetail(CommonAttachmentServiceClient.DOC_ID_HUSBANDRY_CREW, transactionPoid, detRowId, "husbandry-crew-" + transactionPoid + "-" + detRowId + ".zip");
    }

    @Override
    public ResponseEntity<org.springframework.core.io.Resource> downloadAllHusbandryOthAttachments(Long transactionPoid, Long detRowId) {
        return downloadAllForDetail(CommonAttachmentServiceClient.DOC_ID_HUSBANDRY_OTH, transactionPoid, detRowId, "husbandry-other-" + transactionPoid + "-" + detRowId + ".zip");
    }

    private ResponseEntity<org.springframework.core.io.Resource> downloadAllForDetail(String docId, Long transactionPoid, Long detRowId, String zipFileName) {
        ensureConfigured();
        long key = docKey(transactionPoid, detRowId);

        Map<String, Object> listResponse = attachmentClient.listAttachments(docId, key, 0, 1000);
        List<Map<String, Object>> attachments = extractAttachmentsFromResponse(listResponse);

        if (attachments.isEmpty()) {
            throw new ResourceNotFoundException("Attachment", "transactionPoid, detRowId", transactionPoid + ", " + detRowId);
        }

        try {
            // Decide "single file vs ZIP" based on how many files can be actually downloaded (not list metadata count).
            int successCount = 0;

            byte[] firstBytes = null;
            HttpHeaders firstHeaders = null;
            org.springframework.http.HttpStatusCode firstStatus = null;
            String firstEntryName = null;

            ByteArrayOutputStream zipBaos = null;
            ZipOutputStream zos = null;
            int addedEntries = 0;

            for (Map<String, Object> attachment : attachments) {
                if (attachment == null || attachment.get("storedFileName") == null) continue;
                String storedFileName = attachment.get("storedFileName").toString();

                String entryName;
                Object origName = attachment.get("originalFileName");
                if (origName != null && !origName.toString().isBlank()) {
                    entryName = origName.toString();
                } else {
                    entryName = storedFileName;
                }

                try {
                    ResponseEntity<org.springframework.core.io.Resource> fileResponse =
                            attachmentClient.downloadAttachment(docId, key, storedFileName);
                    org.springframework.core.io.Resource resource = fileResponse.getBody();
                    if (resource == null) continue;

                    byte[] bytes;
                    try (InputStream is = resource.getInputStream()) {
                        bytes = is.readAllBytes();
                    }

                    successCount++;
                    if (successCount == 1) {
                        firstBytes = bytes;
                        firstHeaders = fileResponse.getHeaders();
                        firstStatus = fileResponse.getStatusCode();
                        firstEntryName = entryName;
                    } else {
                        // Switch to ZIP mode if this is the 2nd successful file.
                        if (zos == null) {
                            zipBaos = new ByteArrayOutputStream();
                            zos = new ZipOutputStream(zipBaos);

                            // Add the first successful file into the ZIP.
                            zos.putNextEntry(new ZipEntry(firstEntryName));
                            zos.write(firstBytes);
                            zos.closeEntry();
                            addedEntries++;
                        }

                        zos.putNextEntry(new ZipEntry(entryName));
                        zos.write(bytes);
                        zos.closeEntry();
                        addedEntries++;
                    }
                } catch (RuntimeException ex) {
                    log.warn("Skipping missing attachment '{}' for docId={}, key={}: {}", storedFileName, docId, key, ex.getMessage());
                }
            }

            if (successCount == 0) {
                throw new ResourceNotFoundException("Attachment", "transactionPoid, detRowId", transactionPoid + ", " + detRowId);
            }

            // Exactly one file was downloadable: return it directly (no ZIP).
            if (successCount == 1) {
                ByteArrayResource singleResource = new ByteArrayResource(firstBytes);
                if (firstHeaders != null) {
                    // keep original Content-Disposition from common-service
                }
                return ResponseEntity.status(firstStatus != null ? firstStatus : org.springframework.http.HttpStatus.OK)
                        .headers(firstHeaders != null ? firstHeaders : new HttpHeaders())
                        .body(singleResource);
            }

            // Multiple downloadable files: return ZIP.
            if (zos != null) {
                try {
                    zos.close();
                } catch (Exception ignored) {
                }
            }
            byte[] zipBytes = zipBaos.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(zipBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(zipBytes.length);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create zip for attachments", e);
        }
    }

    private ResponseEntity<org.springframework.core.io.Resource> downloadAllForHeader(String docId, Long transactionPoid, String zipFileName) {
        ensureConfigured();

        Map<String, Object> listResponse = attachmentClient.listAttachments(docId, transactionPoid, 0, 1000);
        List<Map<String, Object>> attachments = extractAttachmentsFromResponse(listResponse);

        if (attachments.isEmpty()) {
            throw new ResourceNotFoundException("Attachment", "transactionPoid", transactionPoid);
        }

        try {
            // Decide "single file vs ZIP" based on how many files can be actually downloaded.
            int successCount = 0;

            byte[] firstBytes = null;
            HttpHeaders firstHeaders = null;
            org.springframework.http.HttpStatusCode firstStatus = null;
            String firstEntryName = null;

            ByteArrayOutputStream zipBaos = null;
            ZipOutputStream zos = null;
            int addedEntries = 0;

            for (Map<String, Object> attachment : attachments) {
                if (attachment == null || attachment.get("storedFileName") == null) continue;
                String storedFileName = attachment.get("storedFileName").toString();

                String entryName;
                Object origName = attachment.get("originalFileName");
                if (origName != null && !origName.toString().isBlank()) {
                    entryName = origName.toString();
                } else {
                    entryName = storedFileName;
                }

                try {
                    ResponseEntity<org.springframework.core.io.Resource> fileResponse =
                            attachmentClient.downloadAttachment(docId, transactionPoid, storedFileName);
                    org.springframework.core.io.Resource resource = fileResponse.getBody();
                    if (resource == null) continue;

                    byte[] bytes;
                    try (InputStream is = resource.getInputStream()) {
                        bytes = is.readAllBytes();
                    }

                    successCount++;
                    if (successCount == 1) {
                        firstBytes = bytes;
                        firstHeaders = fileResponse.getHeaders();
                        firstStatus = fileResponse.getStatusCode();
                        firstEntryName = entryName;
                    } else {
                        if (zos == null) {
                            zipBaos = new ByteArrayOutputStream();
                            zos = new ZipOutputStream(zipBaos);

                            zos.putNextEntry(new ZipEntry(firstEntryName));
                            zos.write(firstBytes);
                            zos.closeEntry();
                            addedEntries++;
                        }

                        zos.putNextEntry(new ZipEntry(entryName));
                        zos.write(bytes);
                        zos.closeEntry();
                        addedEntries++;
                    }
                } catch (RuntimeException ex) {
                    log.warn("Skipping missing attachment '{}' for docId={}, transactionPoid={}: {}", storedFileName, docId, transactionPoid, ex.getMessage());
                }
            }

            if (successCount == 0) {
                throw new ResourceNotFoundException("Attachment", "transactionPoid", transactionPoid);
            }

            if (successCount == 1) {
                ByteArrayResource singleResource = new ByteArrayResource(firstBytes);
                if (firstHeaders != null) {
                    firstHeaders.setContentLength(firstBytes.length);
                }
                return ResponseEntity.status(firstStatus != null ? firstStatus : org.springframework.http.HttpStatus.OK)
                        .headers(firstHeaders != null ? firstHeaders : new HttpHeaders())
                        .body(singleResource);
            }

            if (zos != null) {
                try {
                    zos.close();
                } catch (Exception ignored) {
                }
            }

            byte[] zipBytes = zipBaos.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(zipBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(zipBytes.length);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create zip for attachments", e);
        }
    }

    private void ensureConfigured() {
        if (!attachmentClient.isConfigured()) {
            throw new IllegalStateException("Common attachment service is not configured. Set common.service.attachment.base-url.");
        }
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private PortCallOperationEstBertDtl resolveBerthing(Long transactionPoid, Long detRowId) {
        return estBertDtlRepository.findById(new PortCallOperationEstBertDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Berthing detail", "detRowId", detRowId));
    }

    private PortCallOperationEstPrearrivalDtl resolvePreArrival(Long transactionPoid, Long detRowId) {
        return estPrearrivalDtlRepository.findById(new PortCallOperationEstPrearrivalDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Pre-arrival detail", "detRowId", detRowId));
    }

    private PortCallOperationHusbandryCrewDtl resolveHusbandryCrew(Long transactionPoid, Long detRowId) {
        return husbandryCrewDtlRepository.findById(new PortCallOperationHusbandryCrewDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Husbandry crew detail", "detRowId", detRowId));
    }

    private PortCallOperationHusbandryOthDtl resolveHusbandryOth(Long transactionPoid, Long detRowId) {
        return husbandryOthDtlRepository.findById(new PortCallOperationHusbandryOthDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Husbandry other detail", "detRowId", detRowId));
    }

    private PortCallOperationDocsCopyDtl resolveDocsCopy(Long transactionPoid, Long detRowId) {
        return docsCopyDtlRepository.findById(new PortCallOperationDocsCopyDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Docs copy detail", "detRowId", detRowId));
    }

    private PortCallOperationActTimingDtl resolveTiming(Long transactionPoid, Long detRowId) {
        List<PortCallOperationActTimingDtl> entities = actTimingDtlRepository.findByTransactionPoidAndDetRowId(transactionPoid, detRowId);
        if (entities.isEmpty()) {
            throw new ResourceNotFoundException("Actual timing detail", "detRowId", detRowId);
        }
        // Return the first one (there could be multiple with different portReportPoid, but attachments are at detRowId level)
        return entities.get(0);
    }

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
}
