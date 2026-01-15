package com.asg.operations.pdaRoRoVehicle.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.pdaRoRoVehicle.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface PdaRoRoEntryService {

    PdaRoRoEntryHdrResponseDto createRoRoEntry(PdaRoroEntryHdrRequestDto request);

    PdaRoRoEntryHdrResponseDto getRoRoEntry(Long transactionPoid);

    PdaRoRoEntryHdrResponseDto updateRoRoEntry(Long transactionPoid, PdaRoroEntryHdrRequestDto request);

    void deleteRoRoEntry(Long transactionPoid, @Valid DeleteReasonDto deleteReasonDto);

    Map<String, Object> getRoRoVehicleList(String documentId, FilterRequestDto filters, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);

    String uploadExcel(org.springframework.web.multipart.MultipartFile file);

    PdaRoroVehicleUploadResponse uploadVehicleDetails(PdaRoRoVehicleUploadRequest request);

    String clearRoRoVehicleDetails(Long transactionPoid);
}
