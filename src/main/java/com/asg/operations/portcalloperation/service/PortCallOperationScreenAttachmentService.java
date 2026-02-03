package com.asg.operations.portcalloperation.service;

import com.asg.operations.portcalloperation.dto.PcInfoAttachmentUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Service for screen-specific side drawer attachments. Each screen uses its own docId and DB column
 * so attachments are isolated between screens. Common drawer (EMAIL_DOCUMENTS) is unchanged.
 */
public interface PortCallOperationScreenAttachmentService {

    // ----- Berthing (OPS_PC_EST_BERT_DTL.BERTHING_ATTACHMENTS) -----
    PcInfoAttachmentUploadResponseDto uploadBerthingAttachments(Long transactionPoid, Long detRowId,
                                                                 MultipartFile[] files, String[] remarks, String[] checklistNames);
    Map<String, Object> listBerthingAttachments(Long transactionPoid, Long detRowId, int page, int size);
    String getBerthingAttachmentsSummary(Long transactionPoid, Long detRowId);
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadBerthingAttachment(Long transactionPoid, Long detRowId, String storedFileName);

    // ----- Pre-arrival (OPS_PC_EST_PREARRIVAL_DTL.PRE_ARRIVAL_ATTACHMENTS) -----
    PcInfoAttachmentUploadResponseDto uploadPreArrivalAttachments(Long transactionPoid, Long detRowId,
                                                                   MultipartFile[] files, String[] remarks, String[] checklistNames);
    Map<String, Object> listPreArrivalAttachments(Long transactionPoid, Long detRowId, int page, int size);
    String getPreArrivalAttachmentsSummary(Long transactionPoid, Long detRowId);
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadPreArrivalAttachment(Long transactionPoid, Long detRowId, String storedFileName);

    // ----- Other details / PDA-FDA (OPS_PC_OPERATION_HDR.PDA_FDA_ATTACHMENTS) -----
    PcInfoAttachmentUploadResponseDto uploadPdaFdaAttachments(Long transactionPoid,
                                                                MultipartFile[] files, String[] remarks, String[] checklistNames);
    Map<String, Object> listPdaFdaAttachments(Long transactionPoid, int page, int size);
    String getPdaFdaAttachmentsSummary(Long transactionPoid);
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadPdaFdaAttachment(Long transactionPoid, String storedFileName);

    // ----- Husbandry crew (OPS_PC_HUSBANDRY_CREW_DTL.CREW_ATTACHMENTS) -----
    PcInfoAttachmentUploadResponseDto uploadHusbandryCrewAttachments(Long transactionPoid, Long detRowId,
                                                                       MultipartFile[] files, String[] remarks, String[] checklistNames);
    Map<String, Object> listHusbandryCrewAttachments(Long transactionPoid, Long detRowId, int page, int size);
    String getHusbandryCrewAttachmentsSummary(Long transactionPoid, Long detRowId);
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadHusbandryCrewAttachment(Long transactionPoid, Long detRowId, String storedFileName);

    // ----- Husbandry other (OPS_PC_HUSBANDRY_OTH_DTL.ARRNGMNT_ATTACHMENTS) -----
    PcInfoAttachmentUploadResponseDto uploadHusbandryOthAttachments(Long transactionPoid, Long detRowId,
                                                                      MultipartFile[] files, String[] remarks, String[] checklistNames);
    Map<String, Object> listHusbandryOthAttachments(Long transactionPoid, Long detRowId, int page, int size);
    String getHusbandryOthAttachmentsSummary(Long transactionPoid, Long detRowId);
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadHusbandryOthAttachment(Long transactionPoid, Long detRowId, String storedFileName);

    // ----- Docs copy (OPS_PC_DOCS_COPY_DTL.DOCUMENT_ATTACHMENTS) -----
    PcInfoAttachmentUploadResponseDto uploadDocsCopyAttachments(Long transactionPoid, Long detRowId,
                                                                   MultipartFile[] files, String[] remarks, String[] checklistNames);
    Map<String, Object> listDocsCopyAttachments(Long transactionPoid, Long detRowId, int page, int size);
    String getDocsCopyAttachmentsSummary(Long transactionPoid, Long detRowId);
    org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadDocsCopyAttachment(Long transactionPoid, Long detRowId, String storedFileName);

    boolean isAttachmentServiceAvailable();
}
