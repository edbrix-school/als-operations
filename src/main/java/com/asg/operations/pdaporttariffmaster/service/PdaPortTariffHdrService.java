package com.asg.operations.pdaporttariffmaster.service;

import com.asg.operations.pdaporttariffmaster.dto.*;
import org.springframework.data.domain.Page;

public interface PdaPortTariffHdrService {

    PdaPortTariffMasterResponse getTariffById(Long transactionPoid, Long groupPoid);

    PdaPortTariffMasterResponse createTariff(PdaPortTariffMasterRequest request, Long groupPoid, Long companyPoid, String userId);

    PdaPortTariffMasterResponse updateTariff(Long transactionPoid, PdaPortTariffMasterRequest request, Long groupPoid, String userId);

    void deleteTariff(Long transactionPoid, Long groupPoid, String userId, boolean hardDelete);

    PdaPortTariffMasterResponse copyTariff(Long sourceTransactionPoid, CopyTariffRequest request, Long groupPoid, String userId);

    ChargeDetailsResponse getChargeDetails(Long transactionPoid, Long groupPoid, boolean includeSlabs);

    ChargeDetailsResponse bulkSaveChargeDetails(Long transactionPoid, ChargeDetailsRequest request, Long groupPoid, String userId);

    Page<PdaPortTariffMasterResponse> getAllTariffsWithFilters(Long groupPoid, Long companyPoid, GetAllTariffFilterRequest filterRequest, int page, int size, String sort);
}