package com.asg.operations.portcallreport.controller;

import com.asg.operations.portcallreport.dto.PortCallReportDto;
import com.asg.operations.portcallreport.dto.PortCallReportResponseDto;
import com.asg.operations.portcallreport.service.PortCallReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PortCallReportControllerTest {

    private MockMvc mockMvc;
    private MockedStatic<com.asg.common.lib.security.util.UserContext> mockedUserContext;

    private ObjectMapper objectMapper;

    @Mock
    private PortCallReportService portCallReportService;
    
    @InjectMocks
    private PortCallReportController controller;
    
    @BeforeEach
    void setUp() {
        mockedUserContext = mockStatic(com.asg.common.lib.security.util.UserContext.class);
        mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getCompanyPoid).thenReturn(100L);
        mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getGroupPoid).thenReturn(200L);
        mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getUserPoid).thenReturn(1L);
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }
    
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (mockedUserContext != null) {
            mockedUserContext.close();
        }
    }



    @Test
    void getReportById_ShouldReturnReport() throws Exception {
        PortCallReportResponseDto dto = PortCallReportResponseDto.builder()
                .portCallReportPoid(1L)
                .portCallReportId("PCR00001")
                .portCallReportName("Test Report")
                .build();

        when(portCallReportService.getReportById(1L)).thenReturn(dto);

        mockMvc.perform(get("/v1/port-call-reports/1"))
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

        when(portCallReportService.createReport(any(), eq(1L), eq(200L))).thenReturn(created);

        mockMvc.perform(post("/v1/port-call-reports")
                        .header("X-User-Poid", "1")
                        .header("X-Group-Poid", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.portCallReportId").value("PCR00001"));

        verify(portCallReportService).createReport(any(), eq(1L), eq(200L));
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

        when(portCallReportService.updateReport(eq(1L), any(), eq(1L), eq(200L))).thenReturn(updated);

        mockMvc.perform(put("/v1/port-call-reports/1")
                        .header("X-User-Poid", "1")
                        .header("X-Group-Poid", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.portCallReportName").value("Updated Report"));

        verify(portCallReportService).updateReport(eq(1L), any(), eq(1L), eq(200L));
    }

    @Test
    void deleteReport_ShouldReturnSuccess() throws Exception {
        doNothing().when(portCallReportService).deleteReport(1L);

        mockMvc.perform(delete("/v1/port-call-reports/1"))
                .andExpect(status().isOk());

        verify(portCallReportService).deleteReport(1L);
    }

    @Test
    void getPortActivities_ShouldReturnActivities() throws Exception {
        when(portCallReportService.getPortActivities(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/port-call-reports/port-activities")
                        .header("X-User-Poid", "1"))
                .andExpect(status().isOk());

        verify(portCallReportService).getPortActivities(1L);
    }

    @Test
    void getVesselTypes_ShouldReturnVesselTypes() throws Exception {
        when(portCallReportService.getVesselTypes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/port-call-reports/vessel-types"))
                .andExpect(status().isOk());

        verify(portCallReportService).getVesselTypes();
    }
}
