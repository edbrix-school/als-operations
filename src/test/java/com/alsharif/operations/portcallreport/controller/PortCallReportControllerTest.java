package com.alsharif.operations.portcallreport.controller;

import com.alsharif.operations.portcallreport.dto.PortCallReportDto;
import com.alsharif.operations.portcallreport.dto.PortCallReportResponseDto;
import com.alsharif.operations.portcallreport.service.PortCallReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortCallReportController.class)
class PortCallReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PortCallReportService portCallReportService;

    @Test
    void getReportList_ShouldReturnPagedReports() throws Exception {
        PortCallReportResponseDto dto = PortCallReportResponseDto.builder()
                .portCallReportPoid(1L)
                .portCallReportId("PCR00001")
                .portCallReportName("Test Report")
                .build();
        Page<PortCallReportResponseDto> page = new PageImpl<>(List.of(dto));

        when(portCallReportService.getReportList(any(), any())).thenReturn(page);

        mockMvc.perform(get("/port-call-reports")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.content[0].portCallReportId").value("PCR00001"));

        verify(portCallReportService).getReportList(any(), any());
    }

    @Test
    void getReportById_ShouldReturnReport() throws Exception {
        PortCallReportResponseDto dto = PortCallReportResponseDto.builder()
                .portCallReportPoid(1L)
                .portCallReportId("PCR00001")
                .portCallReportName("Test Report")
                .build();

        when(portCallReportService.getReportById(1L)).thenReturn(dto);

        mockMvc.perform(get("/port-call-reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.portCallReportId").value("PCR00001"));

        verify(portCallReportService).getReportById(1L);
    }

    @Test
    void createReport_ShouldReturnCreatedReport() throws Exception {
        PortCallReportDto dto = PortCallReportDto.builder()
                .portCallReportName("New Report")
                .portCallApplVesselType(List.of("TANKER"))
                .active("Y")
                .build();

        PortCallReportResponseDto created = PortCallReportResponseDto.builder()
                .portCallReportPoid(1L)
                .portCallReportId("PCR00001")
                .portCallReportName("New Report")
                .active("Y")
                .build();

        when(portCallReportService.createReport(any(), eq(1L), eq(100L))).thenReturn(created);

        mockMvc.perform(post("/port-call-reports")
                        .header("X-User-Poid", "1")
                        .header("X-Group-Poid", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.portCallReportId").value("PCR00001"));

        verify(portCallReportService).createReport(any(), eq(1L), eq(100L));
    }

    @Test
    void updateReport_ShouldReturnUpdatedReport() throws Exception {
        PortCallReportDto dto = PortCallReportDto.builder()
                .portCallReportName("Updated Report")
                .portCallApplVesselType(List.of("TANKER"))
                .active("Y")
                .build();

        PortCallReportResponseDto updated = PortCallReportResponseDto.builder()
                .portCallReportPoid(1L)
                .portCallReportId("PCR00001")
                .portCallReportName("Updated Report")
                .active("Y")
                .build();

        when(portCallReportService.updateReport(eq(1L), any(), eq(1L), eq(100L))).thenReturn(updated);

        mockMvc.perform(put("/port-call-reports/1")
                        .header("X-User-Poid", "1")
                        .header("X-Group-Poid", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.portCallReportName").value("Updated Report"));

        verify(portCallReportService).updateReport(eq(1L), any(), eq(1L), eq(100L));
    }

    @Test
    void deleteReport_ShouldReturnSuccess() throws Exception {
        doNothing().when(portCallReportService).deleteReport(1L);

        mockMvc.perform(delete("/port-call-reports/1"))
                .andExpect(status().isOk());

        verify(portCallReportService).deleteReport(1L);
    }

    @Test
    void getPortActivities_ShouldReturnActivities() throws Exception {
        when(portCallReportService.getPortActivities(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/port-call-reports/port-activities")
                        .header("X-User-Poid", "1"))
                .andExpect(status().isOk());

        verify(portCallReportService).getPortActivities(1L);
    }

    @Test
    void getVesselTypes_ShouldReturnVesselTypes() throws Exception {
        when(portCallReportService.getVesselTypes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/port-call-reports/vessel-types"))
                .andExpect(status().isOk());

        verify(portCallReportService).getVesselTypes();
    }
}
