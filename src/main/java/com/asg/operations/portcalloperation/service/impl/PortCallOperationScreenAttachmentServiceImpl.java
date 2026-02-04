package com.asg.operations.portcalloperation.service.impl;

import com.asg.operations.portcalloperation.client.CommonAttachmentServiceClient;
import com.asg.operations.portcalloperation.dto.PcInfoAttachmentDto;
import com.asg.operations.portcalloperation.dto.PcInfoAttachmentUploadResponseDto;
import com.asg.operations.portcalloperation.entity.*;
import com.asg.operations.portcalloperation.repository.*;
import com.asg.operations.portcalloperation.service.PortCallOperationScreenAttachmentService;
import com.asg.operations.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private static long docKey(Long transactionPoid, Long detRowId) {
        return CommonAttachmentServiceClient.toDetailDocKeyPoid(transactionPoid, detRowId);
    }

    private static void appendAndSave(PcInfoAttachmentUploadResponseDto response, String existing,
                                      java.util.function.Consumer<String> setter, Runnable save,
                                      CommonAttachmentServiceClient client, String docId, long docKeyPoid,
                                      List<PcInfoAttachmentDto> uploadedFiles) {
        String newNames = uploadedFiles.stream()
                .map(PcInfoAttachmentDto::getOriginalFileName)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(SEP));
        if (newNames.isEmpty()) return;
        String updated = existing == null || existing.isBlank() ? newNames : existing.trim() + SEP + newNames;
        setter.accept(updated);
        save.run();
    }

    private static void deleteUploaded(CommonAttachmentServiceClient client, String docId, long docKeyPoid, List<PcInfoAttachmentDto> files) {
        for (PcInfoAttachmentDto f : files) {
            if (f.getStoredFileName() != null && !f.getStoredFileName().isBlank()) {
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
    public boolean isAttachmentServiceAvailable() {
        return attachmentClient.isConfigured();
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
}
