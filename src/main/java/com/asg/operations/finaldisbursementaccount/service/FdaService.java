package com.asg.operations.finaldisbursementaccount.service;

import com.asg.operations.common.PageResponse;
//import org.springframework.core.io.Resource;
import com.asg.operations.finaldisbursementaccount.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface FdaService {

    FdaHeaderDto getFdaHeader(Long fdaPoid, Long groupPoid, Long companyPoid);

    FdaHeaderDto createFdaHeader(FdaHeaderDto dto, Long groupPoid, Long companyPoid, String userId);

    FdaHeaderDto updateFdaHeader(Long fdaPoid, UpdateFdaHeaderRequest dto, Long groupPoid, Long companyPoid, String userId);

    void softDeleteFda(Long fdaPoid, String userId);

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

//    Resource generateFdaReport(Long transactionPoid, String reportType, Long companyId, Long userId, Long groupId);

    Page<FdaListResponse> getAllFdaWithFilters(Long groupPoid, Long companyPoid, GetAllFdaFilterRequest filterRequest, int page, int size, String sort);

    byte[] printFda(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, String currency) throws Exception;

}