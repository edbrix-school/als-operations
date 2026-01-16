package com.asg.operations.pdaRoRoVehicle.controller;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.operations.pdaRoRoVehicle.dto.*;
import com.asg.operations.pdaRoRoVehicle.service.PdaRoRoEntryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PdaRoRoEntryControllerTest {

    @Mock
    private PdaRoRoEntryService pdaRoroEntryService;

    @Mock
    private com.asg.common.lib.service.LoggingService loggingService;

    @InjectMocks
    private PdaRoRoEntryController pdaRoRoEntryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(pdaRoRoEntryController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void testUploadVehicleDetails_Success() throws Exception {
        PdaRoRoVehicleUploadRequest request = PdaRoRoVehicleUploadRequest.builder()
                .transactionPoid(1000L)
                .voyagePoid(500L)
                .docDate(LocalDate.now())
                .build();

        PdaRoroVehicleUploadResponse response = PdaRoroVehicleUploadResponse.builder()
                .status("Success")
                .vehicleDetails(new ArrayList<>())
                .build();

        when(pdaRoroEntryService.uploadVehicleDetails(any(PdaRoRoVehicleUploadRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/v1/pda-roro-entries/upload-vehicle-details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.status").value("Success"));

        verify(pdaRoroEntryService, times(1)).uploadVehicleDetails(any(PdaRoRoVehicleUploadRequest.class));
    }

    @Test
    void testUploadVehicleDetails_ValidationError() throws Exception {
        PdaRoRoVehicleUploadRequest request = PdaRoRoVehicleUploadRequest.builder()
                .voyagePoid(500L)
                .docDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/v1/pda-roro-entries/upload-vehicle-details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(pdaRoroEntryService, never()).uploadVehicleDetails(any(PdaRoRoVehicleUploadRequest.class));
    }

    @Test
    void testCreateRoRoEntry_Success() throws Exception {
        PdaRoroEntryHdrRequestDto request = PdaRoroEntryHdrRequestDto.builder()
                .vesselVoyagePoid(100L)
                .remarks("Test remarks")
                .build();

        PdaRoRoEntryHdrResponseDto response = PdaRoRoEntryHdrResponseDto.builder()
                .transactionPoid(1L)
                .vesselVoyagePoid(100L)
                .build();

        when(pdaRoroEntryService.createRoRoEntry(any(PdaRoroEntryHdrRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/v1/pda-roro-entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("PDA Ro-Ro Entry created successfully"));

        verify(pdaRoroEntryService, times(1)).createRoRoEntry(any(PdaRoroEntryHdrRequestDto.class));
    }

    @Test
    void testUpdateRoRoEntry_Success() throws Exception {
        Long transactionPoid = 1L;
        PdaRoroEntryHdrRequestDto request = PdaRoroEntryHdrRequestDto.builder()
                .vesselVoyagePoid(100L)
                .remarks("Updated remarks")
                .build();

        PdaRoRoEntryHdrResponseDto response = PdaRoRoEntryHdrResponseDto.builder()
                .transactionPoid(transactionPoid)
                .vesselVoyagePoid(100L)
                .build();

        when(pdaRoroEntryService.updateRoRoEntry(eq(transactionPoid), any(PdaRoroEntryHdrRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/v1/pda-roro-entries/{transactionPoid}", transactionPoid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("PDA Ro-Ro Entry updated successfully"));

        verify(pdaRoroEntryService, times(1)).updateRoRoEntry(eq(transactionPoid), any(PdaRoroEntryHdrRequestDto.class));
    }

    @Test
    void testGetRoRoEntryById_Success() throws Exception {
        Long transactionPoid = 1L;
        PdaRoRoEntryHdrResponseDto response = PdaRoRoEntryHdrResponseDto.builder()
                .transactionPoid(transactionPoid)
                .vesselVoyagePoid(100L)
                .build();

        when(pdaRoroEntryService.getRoRoEntry(transactionPoid))
                .thenReturn(response);

        mockMvc.perform(get("/v1/pda-roro-entries/{transactionPoid}", transactionPoid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("PDA Ro-Ro Entry retrieved successfully"));

        verify(pdaRoroEntryService, times(1)).getRoRoEntry(transactionPoid);
    }

    @Test
    void testDeleteRoRoEntry_Success() throws Exception {
        Long transactionPoid = 1L;

        doNothing().when(pdaRoroEntryService).deleteRoRoEntry(eq(transactionPoid), any());

        mockMvc.perform(delete("/v1/pda-roro-entries/{transactionPoid}", transactionPoid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("PDA Ro-Ro Entry deleted successfully"));

        verify(pdaRoroEntryService, times(1)).deleteRoRoEntry(eq(transactionPoid), any());
    }

    @Test
    void testUploadExcel_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test data".getBytes()
        );

        when(pdaRoroEntryService.uploadExcel(any()))
                .thenReturn("Excel uploaded successfully");

        mockMvc.perform(multipart("/v1/pda-roro-entries/upload-excel")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Excel uploaded successfully"));

        verify(pdaRoroEntryService, times(1)).uploadExcel(any());
    }

    @Test
    void testClearVehicleDetails_Success() throws Exception {
        Long transactionPoid = 1L;

        when(pdaRoroEntryService.clearRoRoVehicleDetails(transactionPoid))
                .thenReturn("Vehicle details cleared successfully");

        mockMvc.perform(post("/v1/pda-roro-entries/{transactionPoid}/clear-vehicle-details", transactionPoid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vehicle details cleared successfully"));

        verify(pdaRoroEntryService, times(1)).clearRoRoVehicleDetails(transactionPoid);
    }
}
