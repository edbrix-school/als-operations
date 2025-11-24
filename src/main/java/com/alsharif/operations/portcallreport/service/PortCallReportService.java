package com.alsharif.operations.portcallreport.service;

import com.alsharif.operations.portcallreport.dto.PortCallReportDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PortCallReportService {

    Page<PortCallReportDto> getReportList(String search, Pageable pageable);

    PortCallReportDto getReportById(Long id);

    PortCallReportDto createReport(PortCallReportDto dto, Long userPoid);

    PortCallReportDto updateReport(Long id, PortCallReportDto dto, Long userPoid);

    void deleteReport(Long id);

    List<Map<String, Object>> getPortActivities(Long userPoid);

    List<Map<String, Object>> getVesselTypes();

}
