package com.asg.operations.pdaporttariffmaster.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.pdaporttariffmaster.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface PdaPortTariffHdrService {

    PdaPortTariffMasterResponse getTariffById(Long transactionPoid, Long groupPoid);

    PdaPortTariffMasterResponse createTariff(PdaPortTariffMasterRequest request, Long groupPoid, Long companyPoid, String userId);

    PdaPortTariffMasterResponse updateTariff(Long transactionPoid, PdaPortTariffMasterRequest request, Long groupPoid, String userId);

    void deleteTariff(Long transactionPoid, Long groupPoid, String userId, boolean hardDelete, @Valid DeleteReasonDto deleteReasonDto);

    PdaPortTariffMasterResponse copyTariff(Long sourceTransactionPoid, CopyTariffRequest request, Long groupPoid, String userId);

    ChargeDetailsResponse getChargeDetails(Long transactionPoid, Long groupPoid, boolean includeSlabs);

    ChargeDetailsResponse bulkSaveChargeDetails(Long transactionPoid, ChargeDetailsRequest request, Long groupPoid, String userId);

    Map<String, Object> getAllTariffsWithFilters(String documentId, FilterRequestDto filters, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);
}