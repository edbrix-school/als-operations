package com.asg.operations.portcalloperation.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.portcalloperation.dto.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PortCallOperationService {

    Map<String, Object> listOperations(String docId, FilterRequestDto request, Pageable pageable, LocalDate startDateValue, LocalDate endDateValue);

    PortCallOperationResponseDto getOperationById(Long id);

    PortCallOperationResponseDto createOperation(PortCallOperationCreateDto dto, Long userPoid, Long groupPoid);

    PortCallOperationResponseDto updateOperation(Long id, PortCallOperationDto dto, Long userPoid, Long groupPoid);

    void deleteOperation(Long id, DeleteReasonDto deleteReasonDto);

    // Stored Procedure Methods
    Map<String, Object> loadPda(String pdaPoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> loadFda(String fdaPoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> loadVoyage(Long voyagePoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> loadEmailList(String transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> getMailTemplate(String transactionPoid, Long templatePoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> getPortReportActivities(String transactionPoid, Long portReportPoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> getEmailRecord(Long emailPoid, String transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> getEmailHistory(String transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    // EstBertDtl CRUD operations
    PortCallOperationEstBertDetailResponseDto getEstBertDetail(Long transactionPoid, Long detRowId);

    PortCallOperationResponseDto createEstBertDetail(Long transactionPoid, PortCallOperationEstBertDetailRequestDto dto);

    PortCallOperationResponseDto updateEstBertDetail(Long transactionPoid, Long detRowId, PortCallOperationEstBertDetailRequestDto dto);

    // EstPrearrivalActDtl CRUD operations
    List<PortCallOperationEstPrearrivalActDetailResponseDto> listEstPrearrivalActDetails(Long transactionPoid, Long detRowId);

    PortCallOperationEstPrearrivalActDetailResponseDto createEstPrearrivalActDetail(Long transactionPoid, Long detRowId, PortCallOperationEstPrearrivalActDetailDto dto);

    PortCallOperationEstPrearrivalActDetailResponseDto updateEstPrearrivalActDetail(Long transactionPoid, Long detRowId, Long preActivityDtlPoid, PortCallOperationEstPrearrivalActDetailDto dto);

    // ActTimingsActvtyDtl CRUD operations
    List<PortCallOperationActTimingsActvtyDetailResponseDto> listActTimingsActvtyDetails(Long transactionPoid, Long detRowId);

    PortCallOperationActTimingsActvtyDetailResponseDto createActTimingsActvtyDetail(Long transactionPoid, Long detRowId, PortCallOperationActTimingsActivityDetailDto dto);

    PortCallOperationActTimingsActvtyDetailResponseDto updateActTimingsActvtyDetail(Long transactionPoid, Long detRowId, Long actualsTimingDtlPoid, PortCallOperationActTimingsActivityDetailDto dto);

    // DocsCopyDtl CRUD operations
    PortCallOperationDocsCopyDetailResponseDto getDocsCopyDetail(Long transactionPoid, Long detRowId);

    PortCallOperationResponseDto createDocsCopyDetail(Long transactionPoid, PortCallOperationDocsCopyDetailRequestDto dto);

    PortCallOperationResponseDto updateDocsCopyDetail(Long transactionPoid, Long detRowId, PortCallOperationDocsCopyDetailRequestDto dto);

}
