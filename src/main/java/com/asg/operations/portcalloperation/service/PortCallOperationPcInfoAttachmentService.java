package com.asg.operations.portcalloperation.service;

import com.asg.operations.portcalloperation.dto.PcInfoAttachmentUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Service for PC Info attachments: delegates to common attachment service and syncs
 * attachment names to PortCallOperationHdr.pcInfoAttachments (comma-separated).
 */
public interface PortCallOperationPcInfoAttachmentService {

    /**
     * Upload files via common attachment service and append their names to PC_INFO_ATTACHMENTS.
     *
     * @param transactionPoid port call operation id (used as docKeyPoid)
     * @param files           files to upload
     * @param remarks         optional remarks per file (by index)
     * @param checklistNames  optional checklist names per file (by index)
     * @return upload response with uploaded files and any errors
     */
    PcInfoAttachmentUploadResponseDto uploadPcInfoAttachments(Long transactionPoid,
                                                               MultipartFile[] files,
                                                               String[] remarks,
                                                               String[] checklistNames);

    /**
     * List PC Info attachments from common attachment service (paginated).
     *
     * @param transactionPoid port call operation id
     * @param page            page number (0-based)
     * @param size            page size
     * @return response map containing data from common service (e.g. content, totalElements)
     */
    Map<String, Object> listPcInfoAttachments(Long transactionPoid, int page, int size);

    /**
     * Get comma-separated attachment names stored in PC_INFO_ATTACHMENTS for the given operation.
     */
    String getPcInfoAttachmentsSummary(Long transactionPoid);

    /**
     * Download an attachment file by storedFileName.
     *
     * @param transactionPoid port call operation id
     * @param storedFileName  the stored filename (fileNameMapped) from the upload response
     * @return ResponseEntity with file content as Resource and proper headers
     */
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadAttachment(Long transactionPoid, String storedFileName);

    /**
     * Check if common attachment service is configured and available.
     */
    boolean isAttachmentServiceAvailable();
}
