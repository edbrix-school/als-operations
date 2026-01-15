package com.asg.operations.portcallreport.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.portcallreport.dto.GetAllPortCallReportFilterRequest;
import com.asg.operations.portcallreport.dto.PortActivityResponseDto;
import com.asg.operations.portcallreport.dto.PortCallReportDto;
import com.asg.operations.portcallreport.dto.PortCallReportListResponse;
import com.asg.operations.portcallreport.dto.PortCallReportResponseDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PortCallReportService {

    Map<String, Object> getAllPortCallReportsWithFilters(String documentId, FilterRequestDto filters, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);

    PortCallReportResponseDto getReportById(Long id);

    PortCallReportResponseDto createReport(PortCallReportDto dto, Long userPoid, Long groupPoid);

    PortCallReportResponseDto updateReport(Long id, PortCallReportDto dto, Long userPoid, Long groupPoid);

    void deleteReport(Long id, @Valid DeleteReasonDto deleteReasonDto);

    List<PortActivityResponseDto> getPortActivities(Long userPoid);

    List<Map<String, Object>> getVesselTypes();

}
