package com.asg.operations.portcalloperation.service;

import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.portcalloperation.dto.PortCallOperationCreateDto;
import com.asg.operations.portcalloperation.dto.PortCallOperationDto;
import com.asg.operations.portcalloperation.dto.PortCallOperationResponseDto;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface PortCallOperationService {

    Map<String, Object> listOperations(String docId, FilterRequestDto request, Pageable pageable, LocalDate startDateValue, LocalDate endDateValue);

    PortCallOperationResponseDto getOperationById(Long id);

    PortCallOperationResponseDto createOperation(PortCallOperationCreateDto dto, Long userPoid, Long groupPoid);

    PortCallOperationResponseDto updateOperation(Long id, PortCallOperationDto dto, Long userPoid, Long groupPoid);

    void deleteOperation(Long id);

    // Stored Procedure Methods
    Map<String, Object> loadPda(String pdaPoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> loadFda(String fdaPoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> loadVoyage(Long voyagePoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> loadEmailList(String transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> getMailTemplate(String transactionPoid, Long templatePoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> getPortReportActivities(String transactionPoid, Long portReportPoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> getEmailRecord(Long emailPoid, String transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    Map<String, Object> getEmailHistory(String transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

}
