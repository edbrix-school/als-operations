package com.asg.operations.portcalloperation.service.impl;

import com.asg.operations.portcalloperation.client.CommonAttachmentServiceClient;
import com.asg.operations.portcalloperation.dto.PcInfoAttachmentDto;
import com.asg.operations.portcalloperation.dto.PcInfoAttachmentUploadResponseDto;
import com.asg.operations.portcalloperation.entity.PortCallOperationDocsMsgsDtl1;
import com.asg.operations.portcalloperation.repository.PortCallOperationDocsMsgsDtl1Repository;
import com.asg.operations.portcalloperation.service.PortCallOperationDrawerAttachmentService;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Saves and retrieves side drawer attachments via common attachment service (docId=110-163-DRAWER, docKeyPoid=emailPoid)
 * and keeps EMAIL_DOCUMENTS on PortCallOperationDocsMsgsDtl1 in sync (comma-separated names).
 * Isolated from main screen (PC Info) and list area.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortCallOperationDrawerAttachmentServiceImpl implements PortCallOperationDrawerAttachmentService {

    private static final String ATTACHMENT_NAMES_SEPARATOR = ",";

    private final CommonAttachmentServiceClient attachmentClient;
    private final PortCallOperationDocsMsgsDtl1Repository docsMsgsDtl1Repository;

    @Override
    @Transactional
    public PcInfoAttachmentUploadResponseDto uploadDrawerAttachments(Long transactionPoid, Long emailPoid,
                                                                     MultipartFile[] files, String[] remarks, String[] checklistNames) {
        if (!attachmentClient.isConfigured()) {
            throw new IllegalStateException("Common attachment service is not configured. Set common.service.attachment.base-url.");
        }
        PortCallOperationDocsMsgsDtl1 detail = resolveDetail(transactionPoid, emailPoid);

        if (files == null || files.length == 0) {
            log.warn("uploadDrawerAttachments: no files received (files={})", files == null ? "null" : "length=" + files.length);
            return new PcInfoAttachmentUploadResponseDto(List.of(), List.of("No files provided"), true);
        }
        int nonEmpty = (int) java.util.Arrays.stream(files).filter(f -> f != null && !f.isEmpty()).count();
        log.info("uploadDrawerAttachments: transactionPoid={}, emailPoid={}, filesCount={}, nonEmpty={}", transactionPoid, emailPoid, files.length, nonEmpty);

        PcInfoAttachmentUploadResponseDto response = attachmentClient.upload(
                CommonAttachmentServiceClient.DOC_ID_DRAWER, emailPoid, files, remarks, checklistNames);

        log.info("uploadDrawerAttachments: common-services returned uploadedCount={}, errors={}",
                response.getUploadedFiles() != null ? response.getUploadedFiles().size() : 0,
                response.getErrors() != null ? response.getErrors() : List.of());

        if (response.getUploadedFiles() == null || response.getUploadedFiles().isEmpty()) {
            return response;
        }

        List<PcInfoAttachmentDto> uploadedFiles = new ArrayList<>(response.getUploadedFiles());

        try {
            String newNames = uploadedFiles.stream()
                    .map(PcInfoAttachmentDto::getOriginalFileName)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.joining(ATTACHMENT_NAMES_SEPARATOR));
            if (!newNames.isEmpty()) {
                String existing = detail.getEmailDocuments();
                String updated = existing == null || existing.isBlank()
                        ? newNames
                        : existing.trim() + ATTACHMENT_NAMES_SEPARATOR + newNames;
                detail.setEmailDocuments(updated);
                docsMsgsDtl1Repository.save(detail);
                log.info("Updated EMAIL_DOCUMENTS for transactionPoid={}, emailPoid={}, appended {} file(s)", transactionPoid, emailPoid, uploadedFiles.size());
            }
        } catch (Exception e) {
            log.error("Failed to update EMAIL_DOCUMENTS for transactionPoid={}, emailPoid={}, rolling back uploaded files: {}", transactionPoid, emailPoid, e.getMessage(), e);
            try {
                deleteUploadedFiles(emailPoid, uploadedFiles);
                log.info("Successfully rolled back {} uploaded file(s) after EMAIL_DOCUMENTS update failure", uploadedFiles.size());
            } catch (Exception rollbackEx) {
                log.error("Failed to rollback uploaded files for emailPoid={}: {}", emailPoid, rollbackEx.getMessage(), rollbackEx);
            }
            throw new RuntimeException("Failed to update EMAIL_DOCUMENTS after successful file upload: " + e.getMessage(), e);
        }

        return response;
    }

    @Override
    public Map<String, Object> listDrawerAttachments(Long transactionPoid, Long emailPoid, int page, int size) {
        if (!attachmentClient.isConfigured()) {
            throw new IllegalStateException("Common attachment service is not configured. Set common.service.attachment.base-url.");
        }
        resolveDetail(transactionPoid, emailPoid);
        return attachmentClient.listAttachments(CommonAttachmentServiceClient.DOC_ID_DRAWER, emailPoid, page, size);
    }

    @Override
    public String getDrawerAttachmentsSummary(Long transactionPoid, Long emailPoid) {
        String emailDocuments = resolveDetail(transactionPoid, emailPoid).getEmailDocuments();
        return emailDocuments != null ? emailDocuments : "";
    }

    @Override
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadAttachment(Long transactionPoid, Long emailPoid, String storedFileName) {
        if (!attachmentClient.isConfigured()) {
            throw new IllegalStateException("Common attachment service is not configured. Set common.service.attachment.base-url.");
        }
        resolveDetail(transactionPoid, emailPoid);
        return attachmentClient.downloadAttachment(CommonAttachmentServiceClient.DOC_ID_DRAWER, emailPoid, storedFileName);
    }

    @Override
    public boolean isAttachmentServiceAvailable() {
        return attachmentClient.isConfigured();
    }

    /**
     * Resolve Docs Msgs Dtl1 row by emailPoid and ensure it belongs to the given transactionPoid.
     */
    private PortCallOperationDocsMsgsDtl1 resolveDetail(Long transactionPoid, Long emailPoid) {
        Optional<PortCallOperationDocsMsgsDtl1> detailOptional = docsMsgsDtl1Repository.findByEmailPoid(emailPoid);
        if (detailOptional.isEmpty()) {
            throw new ResourceNotFoundException("Docs Msgs Dtl1 (drawer)", "Email Poid", emailPoid);
        }
        PortCallOperationDocsMsgsDtl1 detail = detailOptional.get();
        if (!detail.getTransactionPoid().equals(transactionPoid)) {
            throw new ResourceNotFoundException("Docs Msgs Dtl1 (drawer)", "Email Poid", emailPoid);
        }
        return detail;
    }

    private void deleteUploadedFiles(Long emailPoid, List<PcInfoAttachmentDto> uploadedFiles) {
        for (PcInfoAttachmentDto file : uploadedFiles) {
            if (file.getStoredFileName() != null && !file.getStoredFileName().isBlank()) {
                try {
                    attachmentClient.deleteAttachment(CommonAttachmentServiceClient.DOC_ID_DRAWER, emailPoid, file.getStoredFileName());
                    log.debug("Deleted uploaded file {} (storedFileName: {})", file.getOriginalFileName(), file.getStoredFileName());
                } catch (Exception e) {
                    log.warn("Failed to delete uploaded file {} (storedFileName: {}): {}",
                            file.getOriginalFileName(), file.getStoredFileName(), e.getMessage());
                }
            }
        }
    }
}
