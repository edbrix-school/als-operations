package com.asg.operations.pdaRoRoVehicle.service;

import com.asg.operations.pdaRoRoVehicle.dto.*;
import org.springframework.data.domain.Page;

public interface PdaRoRoEntryService {

    PdaRoRoEntryHdrResponseDto createRoRoEntry(PdaRoroEntryHdrRequestDto request);

    PdaRoRoEntryHdrResponseDto getRoRoEntry(Long transactionPoid);

    PdaRoRoEntryHdrResponseDto updateRoRoEntry(Long transactionPoid, PdaRoroEntryHdrRequestDto request);

    void deleteRoRoEntry(Long transactionPoid);

    Page<RoRoVehicleListResponse> getRoRoVehicleList(Long groupPoid, Long companyPoid,
                                                     GetAllRoRoVehicleFilterRequest filterRequest, 
                                                     int page, int size, String sort);

    String uploadExcel(org.springframework.web.multipart.MultipartFile file);

    PdaRoroVehicleUploadResponse uploadVehicleDetails(PdaRoRoVehicleUploadRequest request);

    String clearRoRoVehicleDetails(Long transactionPoid);

    byte[] printTallySheet(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) throws Exception;
}
