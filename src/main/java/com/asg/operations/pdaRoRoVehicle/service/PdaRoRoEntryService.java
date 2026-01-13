package com.asg.operations.pdaRoRoVehicle.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.operations.pdaRoRoVehicle.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

public interface PdaRoRoEntryService {

    PdaRoRoEntryHdrResponseDto createRoRoEntry(PdaRoroEntryHdrRequestDto request);

    PdaRoRoEntryHdrResponseDto getRoRoEntry(Long transactionPoid);

    PdaRoRoEntryHdrResponseDto updateRoRoEntry(Long transactionPoid, PdaRoroEntryHdrRequestDto request);

    void deleteRoRoEntry(Long transactionPoid, @Valid DeleteReasonDto deleteReasonDto);

    Page<RoRoVehicleListResponse> getRoRoVehicleList(Long groupPoid, Long companyPoid,
                                                     GetAllRoRoVehicleFilterRequest filterRequest, 
                                                     int page, int size, String sort);

    String uploadExcel(org.springframework.web.multipart.MultipartFile file);

    PdaRoroVehicleUploadResponse uploadVehicleDetails(PdaRoRoVehicleUploadRequest request);

    String clearRoRoVehicleDetails(Long transactionPoid);
}
