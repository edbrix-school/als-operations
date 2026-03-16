package com.asg.operations.portcalloperation.service;

import com.asg.operations.portcalloperation.dto.PcInfoAttachmentUploadResponseDto;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Service for screen-specific side drawer attachments. Each screen uses its own docId and DB column
 * so attachments are isolated between screens. Common drawer (EMAIL_DOCUMENTS) is unchanged.
 */
public interface PortCallOperationScreenAttachmentService {

    // ----- Berthing (OPS_PC_EST_BERT_DTL.BERTHING_ATTACHMENTS) -----
    PcInfoAttachmentUploadResponseDto uploadBerthingAttachments(Long transactionPoid, Long detRowId, MultipartFile[] files, String[] remarks, String[] checklistNames);

    Map<String, Object> listBerthingAttachments(Long transactionPoid, Long detRowId, int page, int size);

    String getBerthingAttachmentsSummary(Long transactionPoid, Long detRowId);

    ResponseEntity<org.springframework.core.io.Resource> downloadBerthingAttachment(Long transactionPoid, Long detRowId, String storedFileName);

    void deleteBerthingAttachment(Long transactionPoid, Long detRowId, String storedFileName);


    // ----- Pre-arrival (OPS_PC_EST_PREARRIVAL_DTL.PRE_ARRIVAL_ATTACHMENTS) -----
    PcInfoAttachmentUploadResponseDto uploadPreArrivalAttachments(Long transactionPoid, Long detRowId, MultipartFile[] files, String[] remarks, String[] checklistNames);

    Map<String, Object> listPreArrivalAttachments(Long transactionPoid, Long detRowId, int page, int size);

    String getPreArrivalAttachmentsSummary(Long transactionPoid, Long detRowId);

    ResponseEntity<org.springframework.core.io.Resource> downloadPreArrivalAttachment(Long transactionPoid, Long detRowId, String storedFileName);

    void deletePreArrivalAttachment(Long transactionPoid, Long detRowId, String storedFileName);


    // ----- Other details / PDA-FDA (OPS_PC_OPERATION_HDR.PDA_FDA_ATTACHMENTS) -----
    PcInfoAttachmentUploadResponseDto uploadPdaFdaAttachments(Long transactionPoid, MultipartFile[] files, String[] remarks, String[] checklistNames);

    Map<String, Object> listPdaFdaAttachments(Long transactionPoid, int page, int size);

    String getPdaFdaAttachmentsSummary(Long transactionPoid);

    ResponseEntity<org.springframework.core.io.Resource> downloadPdaFdaAttachment(Long transactionPoid, String storedFileName);

    void deletePdaFdaAttachment(Long transactionPoid, String storedFileName);


    // ----- Husbandry crew (OPS_PC_HUSBANDRY_CREW_DTL.CREW_ATTACHMENTS) -----
    PcInfoAttachmentUploadResponseDto uploadHusbandryCrewAttachments(Long transactionPoid, Long detRowId, MultipartFile[] files, String[] remarks, String[] checklistNames);

    Map<String, Object> listHusbandryCrewAttachments(Long transactionPoid, Long detRowId, int page, int size);

    String getHusbandryCrewAttachmentsSummary(Long transactionPoid, Long detRowId);

    ResponseEntity<org.springframework.core.io.Resource> downloadHusbandryCrewAttachment(Long transactionPoid, Long detRowId, String storedFileName);

    ResponseEntity<org.springframework.core.io.Resource> downloadAllHusbandryCrewAttachments(Long transactionPoid, Long detRowId);


    // ----- Husbandry other (OPS_PC_HUSBANDRY_OTH_DTL.ARRNGMNT_ATTACHMENTS) -----
    PcInfoAttachmentUploadResponseDto uploadHusbandryOthAttachments(Long transactionPoid, Long detRowId, MultipartFile[] files, String[] remarks, String[] checklistNames);

    Map<String, Object> listHusbandryOthAttachments(Long transactionPoid, Long detRowId, int page, int size);

    String getHusbandryOthAttachmentsSummary(Long transactionPoid, Long detRowId);

    ResponseEntity<org.springframework.core.io.Resource> downloadHusbandryOthAttachment(Long transactionPoid, Long detRowId, String storedFileName);

    ResponseEntity<org.springframework.core.io.Resource> downloadAllHusbandryOthAttachments(Long transactionPoid, Long detRowId);


    // ----- Docs copy (OPS_PC_DOCS_COPY_DTL.DOCUMENT_ATTACHMENTS) -----
    PcInfoAttachmentUploadResponseDto uploadDocsCopyAttachments(Long transactionPoid, Long detRowId, MultipartFile[] files, String[] remarks, String[] checklistNames);

    Map<String, Object> listDocsCopyAttachments(Long transactionPoid, Long detRowId, int page, int size);

    String getDocsCopyAttachmentsSummary(Long transactionPoid, Long detRowId);

    ResponseEntity<Resource> downloadDocsCopyAttachment(Long transactionPoid, Long detRowId, String storedFileName);


    void deleteDocsCopyAttachment(Long transactionPoid, Long detRowId, String storedFileName);

    // ----- Actual timing (OPS_PC_ACT_TIMING_DTL.TIMING_ATTACHMENTS) -----
    PcInfoAttachmentUploadResponseDto uploadTimingAttachments(Long transactionPoid, Long detRowId, MultipartFile[] files, String[] remarks, String[] checklistNames);

    Map<String, Object> listTimingAttachments(Long transactionPoid, Long detRowId, int page, int size);

    String getTimingAttachmentsSummary(Long transactionPoid, Long detRowId);

    ResponseEntity<Resource> downloadTimingAttachment(Long transactionPoid, Long detRowId, String storedFileName);

    void deleteTimingAttachment(Long transactionPoid, Long detRowId, String storedFileName);

    boolean isAttachmentServiceAvailable();
}
