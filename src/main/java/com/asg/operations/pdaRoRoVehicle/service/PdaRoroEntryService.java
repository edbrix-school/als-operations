package com.asg.operations.pdaRoRoVehicle.service;

import com.asg.operations.pdaRoRoVehicle.dto.*;
import com.asg.operations.pdaporttariffmaster.dto.GetAllTariffFilterRequest;
import com.asg.operations.pdaporttariffmaster.dto.PdaPortTariffListResponse;
import org.springframework.data.domain.Page;

public interface PdaRoroEntryService {

    PdaRoroEntryHdrResponseDto createRoroEntry(PdaRoroEntryHdrRequestDto request);

    PdaRoroEntryHdrResponseDto updateRoroEntry(Long transactionPoid,
                         PdaRoroEntryHdrRequestDto request);

    PdaRoroEntryHdrResponseDto getRoroEntry(Long transactionPoid);

    PdaRoroVehicleUploadResponse uploadVehicleDetails(
            PdaRoroVehicleUploadRequest request);

    void deleteRoRoEntry(Long transactionPoid);

    String clearRoroVehicleDetails(Long transactionPoid);

    Page<RoRoVehicleListResponse> getRoRoVehicleList(Long groupPoid, Long companyPoid, GetAllRoRoVehicleFilterRequest filterRequest, int page, int size, String sort);
}
