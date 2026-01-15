package com.asg.operations.portcallreport.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.operations.portcallreport.dto.GetAllPortCallReportFilterRequest;
import com.asg.operations.portcallreport.dto.PortActivityResponseDto;
import com.asg.operations.portcallreport.dto.PortCallReportDto;
import com.asg.operations.portcallreport.dto.PortCallReportListResponse;
import com.asg.operations.portcallreport.dto.PortCallReportResponseDto;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

public interface PortCallReportService {

    org.springframework.data.domain.Page<PortCallReportListResponse> getAllPortCallReportsWithFilters(Long groupPoid, GetAllPortCallReportFilterRequest filterRequest, int page, int size, String sort);

    PortCallReportResponseDto getReportById(Long id);

    PortCallReportResponseDto createReport(PortCallReportDto dto, Long userPoid, Long groupPoid);

    PortCallReportResponseDto updateReport(Long id, PortCallReportDto dto, Long userPoid, Long groupPoid);

    void deleteReport(Long id, @Valid DeleteReasonDto deleteReasonDto);

    List<PortActivityResponseDto> getPortActivities(Long userPoid);

    List<Map<String, Object>> getVesselTypes();

}
