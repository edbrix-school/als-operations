package com.alsharif.operations.pdaporttariffmaster.service;

import com.alsharif.operations.pdaporttariffmaster.dto.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface PdaPortTariffHdrService {

    PageResponse<PdaPortTariffMasterResponse> getTariffList(
            String portPoid,
            LocalDate periodFrom,
            LocalDate periodTo,
            String vesselTypePoid,
            Long groupPoid,
            Pageable pageable
    );

    PdaPortTariffMasterResponse getTariffById(Long transactionPoid, Long groupPoid);

    PdaPortTariffMasterResponse createTariff(PdaPortTariffMasterRequest request, Long groupPoid, Long companyPoid, String userId);

    PdaPortTariffMasterResponse updateTariff(Long transactionPoid, PdaPortTariffMasterRequest request, Long groupPoid, String userId);

    void deleteTariff(Long transactionPoid, Long groupPoid, String userId, boolean hardDelete);

    PdaPortTariffMasterResponse copyTariff(Long sourceTransactionPoid, CopyTariffRequest request, Long groupPoid, String userId);

    ChargeDetailsResponse getChargeDetails(Long transactionPoid, Long groupPoid, boolean includeSlabs);

    ChargeDetailsResponse bulkSaveChargeDetails(Long transactionPoid, ChargeDetailsRequest request, Long groupPoid, String userId);
}