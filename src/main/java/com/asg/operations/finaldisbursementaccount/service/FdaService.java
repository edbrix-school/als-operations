package com.asg.operations.finaldisbursementaccount.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.common.PageResponse;
import com.asg.operations.finaldisbursementaccount.dto.CreateFdaHeaderRequest;
import com.asg.operations.finaldisbursementaccount.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FdaService {

    FdaHeaderDto getFdaHeader(Long fdaPoid, Long groupPoid, Long companyPoid);

    FdaHeaderDto createFdaHeader(CreateFdaHeaderRequest dto, Long groupPoid, Long companyPoid, String userId);

    FdaHeaderDto updateFdaHeader(Long fdaPoid, UpdateFdaHeaderRequest dto, Long groupPoid, Long companyPoid, String userId);

    void softDeleteFda(Long fdaPoid, String userId, @Valid DeleteReasonDto deleteReasonDto);

    PageResponse<FdaHeaderDto> getFdaList(Long groupPoid, Long companyPoid, Long transactionPoid, String vesselName, LocalDate etaFrom, LocalDate etaTo, Pageable pageable);

    PageResponse<FdaChargeDto> getCharges(Long transactionPoid, Long groupPoid, Long companyPoid, Pageable pageable);

    void saveCharges(Long transactionPoid, List<FdaChargeDto> chargeDtos, String userId, Long groupPoid, Long companyPoid);

    void deleteCharge(Long transactionPoid, Long detRowId, String userId);

    String closeFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid);

    String reopenFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid, FdaReOpenDto fdaReOpenDto);

    String submitFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid);

    String verifyFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid);

    String returnFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid, String correctionRemarks);

    String supplementaryFda(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid);

    List<FdaSupplementaryInfoDto> getSupplementaryInfo(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    String closeFdaWithoutAmount(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, String closedRemark);

    PartyGlResponse getPartyGl(Long groupPoid, Long companyPoid, Long userPoid, Long partyPoid, String partyType);

    String createFdaFromPda(Long groupPoid, Long companyPoid, Long userPoid, Long pdaTransactionPoid);

    List<PdaLogResponse> getPdaLogs(Long transactionPoid, Long groupPoid, Long companyPoid);

    Map<String, Object> getAllFdaWithFilters(String documentId, FilterRequestDto filters, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);

    byte[] printFda(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, String currency) throws Exception;

    String customApproval(Long groupPoid, Long companyPoid, Long userPoid, Long fdaPoid, String action);

}