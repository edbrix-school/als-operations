package com.asg.operations.portcalloperation.service;

import com.asg.operations.portcalloperation.dto.PcInfoAttachmentUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Service for side drawer attachments (Docs Msgs Dtl1): delegates to common attachment service
 * with docId=110-163-DRAWER and docKeyPoid=emailPoid, and syncs attachment names to
 * PortCallOperationDocsMsgsDtl1.emailDocuments (comma-separated). Isolated from main screen and list area.
 */
public interface PortCallOperationDrawerAttachmentService {

    /**
     * Upload files via common attachment service and append their names to EMAIL_DOCUMENTS for the given email row.
     *
     * @param transactionPoid port call operation id (used for validation only)
     * @param emailPoid       Docs Msgs Dtl1 email row id (used as docKeyPoid)
     * @param files           files to upload
     * @param remarks         optional remarks per file (by index)
     * @param checklistNames  optional checklist names per file (by index)
     * @return upload response with uploaded files and any errors
     */
    PcInfoAttachmentUploadResponseDto uploadDrawerAttachments(Long transactionPoid,
                                                              Long emailPoid,
                                                              MultipartFile[] files,
                                                              String[] remarks,
                                                              String[] checklistNames);

    /**
     * List drawer attachments from common attachment service (paginated) for the given email row.
     *
     * @param transactionPoid port call operation id (for validation)
     * @param emailPoid       Docs Msgs Dtl1 email row id
     * @param page            page number (0-based)
     * @param size            page size
     * @return response map containing data from common service (e.g. content, totalElements)
     */
    Map<String, Object> listDrawerAttachments(Long transactionPoid, Long emailPoid, int page, int size);

    /**
     * Get comma-separated attachment names stored in EMAIL_DOCUMENTS for the given email row.
     */
    String getDrawerAttachmentsSummary(Long transactionPoid, Long emailPoid);

    /**
     * Download an attachment file by storedFileName for the drawer section.
     *
     * @param transactionPoid port call operation id (for validation)
     * @param emailPoid       Docs Msgs Dtl1 email row id
     * @param storedFileName  the stored filename (fileNameMapped) from the upload response
     * @return ResponseEntity with file content as Resource and proper headers
     */
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadAttachment(Long transactionPoid, Long emailPoid, String storedFileName);

    /**
     * Check if common attachment service is configured and available.
     */
    boolean isAttachmentServiceAvailable();
}
