package com.asg.operations.pdaporttariffmaster.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.pdaporttariffmaster.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface PdaPortTariffHdrService {

    PdaPortTariffMasterResponse getTariffById(Long transactionPoid);

    PdaPortTariffMasterResponse createTariff(PdaPortTariffMasterRequest request);

    PdaPortTariffMasterResponse updateTariff(Long transactionPoid, PdaPortTariffMasterRequest request);

    void deleteTariff(Long transactionPoid, @Valid DeleteReasonDto deleteReasonDto);

    PdaPortTariffMasterResponse copyTariff(Long sourceTransactionPoid, CopyTariffRequest request);

    ChargeDetailsResponse getChargeDetails(Long transactionPoid, boolean includeSlabs);

    ChargeDetailsResponse bulkSaveChargeDetails(Long transactionPoid, ChargeDetailsRequest request);

    Map<String, Object> getAllTariffsWithFilters(String documentId, FilterRequestDto filters, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);
}