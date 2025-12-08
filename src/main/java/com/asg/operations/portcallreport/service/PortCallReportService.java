package com.asg.operations.portcallreport.service;

import com.asg.operations.portcallreport.dto.PortActivityResponseDto;
import com.asg.operations.portcallreport.dto.PortCallReportDto;
import com.asg.operations.portcallreport.dto.PortCallReportResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PortCallReportService {

    Page<PortCallReportResponseDto> getReportList(String search, Pageable pageable);

    PortCallReportResponseDto getReportById(Long id);

    PortCallReportResponseDto createReport(PortCallReportDto dto, Long userPoid, Long groupPoid);

    PortCallReportResponseDto updateReport(Long id, PortCallReportDto dto, Long userPoid, Long groupPoid);

    void deleteReport(Long id);

    List<PortActivityResponseDto> getPortActivities(Long userPoid);

    List<Map<String, Object>> getVesselTypes();

}
