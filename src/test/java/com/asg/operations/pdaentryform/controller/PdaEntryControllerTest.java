package com.asg.operations.pdaentryform.controller;

import com.asg.operations.pdaentryform.dto.*;
import com.asg.operations.pdaentryform.service.PdaEntryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PdaEntryControllerTest {

    @Mock
    private PdaEntryService pdaEntryService;

    @InjectMocks
    private PdaEntryController pdaEntryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Long groupPoid;
    private Long companyPoid;
    private String userId;
    private Long transactionPoid;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pdaEntryController).build();
        objectMapper = new ObjectMapper();
        groupPoid = 1L;
        companyPoid = 100L;
        userId = "USER123";
        transactionPoid = 1000L;
    }

    @Test
    void testBulkSaveChargeDetails_Success() throws Exception {
        BulkSaveChargeDetailsRequest request = new BulkSaveChargeDetailsRequest();
        PdaEntryChargeDetailRequest chargeDetail = new PdaEntryChargeDetailRequest();
        chargeDetail.setChargePoid(new BigDecimal(100));
        chargeDetail.setQty(new BigDecimal(5));
        chargeDetail.setDays(new BigDecimal(10));
        chargeDetail.setPdaRate(new BigDecimal(100));
        request.setChargeDetails(List.of(chargeDetail));
        request.setDeleteDetRowIds(new ArrayList<>());

        PdaEntryChargeDetailResponse response = new PdaEntryChargeDetailResponse();
        response.setDetRowId(1L);
        response.setChargePoid(new BigDecimal(100));

        when(pdaEntryService.bulkSaveChargeDetails(
                eq(transactionPoid), any(BulkSaveChargeDetailsRequest.class),
                eq(groupPoid), eq(companyPoid), eq(userId)))
                .thenReturn(List.of(response));

        mockMvc.perform(post("/api/v1/pda-entries/{transactionPoid}/charge-details/bulk-save", transactionPoid)
                .header("X-Group-Poid", groupPoid)
                .header("X-Company-Poid", companyPoid)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].detRowId").value(1));

        verify(pdaEntryService, times(1)).bulkSaveChargeDetails(
                eq(transactionPoid), any(BulkSaveChargeDetailsRequest.class),
                eq(groupPoid), eq(companyPoid), eq(userId));
    }

    @Test
    void testBulkSaveVehicleDetails_Success() throws Exception {
        BulkSaveVehicleDetailsRequest request = new BulkSaveVehicleDetailsRequest();
        PdaEntryVehicleDetailRequest vehicleDetail = new PdaEntryVehicleDetailRequest();
        vehicleDetail.setVesselName("MAERSK");
        vehicleDetail.setVehicleModel("BMW X5");
        request.setVehicleDetails(List.of(vehicleDetail));
        request.setDeleteDetRowIds(new ArrayList<>());

        PdaEntryVehicleDetailResponse response = new PdaEntryVehicleDetailResponse();
        response.setDetRowId(1L);
        response.setVesselName("MAERSK");

        when(pdaEntryService.bulkSaveVehicleDetails(
                eq(transactionPoid), any(BulkSaveVehicleDetailsRequest.class),
                eq(groupPoid), eq(companyPoid), eq(userId)))
                .thenReturn(List.of(response));

        mockMvc.perform(post("/api/v1/pda-entries/{transactionPoid}/vehicle-details/bulk-save", transactionPoid)
                .header("X-Group-Poid", groupPoid)
                .header("X-Company-Poid", companyPoid)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vesselName").value("MAERSK"));

        verify(pdaEntryService, times(1)).bulkSaveVehicleDetails(
                eq(transactionPoid), any(BulkSaveVehicleDetailsRequest.class),
                eq(groupPoid), eq(companyPoid), eq(userId));
    }

    @Test
    void testGetChargeDetails_Success() throws Exception {
        PdaEntryChargeDetailResponse response = new PdaEntryChargeDetailResponse();
        response.setDetRowId(1L);
        response.setChargePoid(new BigDecimal(100));

        when(pdaEntryService.getChargeDetails(transactionPoid, groupPoid, companyPoid))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/pda-entries/{transactionPoid}/charge-details", transactionPoid)
                .header("X-Group-Poid", groupPoid)
                .header("X-Company-Poid", companyPoid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chargePoid").value(100));

        verify(pdaEntryService, times(1)).getChargeDetails(transactionPoid, groupPoid, companyPoid);
    }

    @Test
    void testGetVehicleDetails_Success() throws Exception {
        PdaEntryVehicleDetailResponse response = new PdaEntryVehicleDetailResponse();
        response.setDetRowId(1L);
        response.setVesselName("MAERSK");

        when(pdaEntryService.getVehicleDetails(transactionPoid, groupPoid, companyPoid))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/pda-entries/{transactionPoid}/vehicle-details", transactionPoid)
                .header("X-Group-Poid", groupPoid)
                .header("X-Company-Poid", companyPoid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vesselName").value("MAERSK"));

        verify(pdaEntryService, times(1)).getVehicleDetails(transactionPoid, groupPoid, companyPoid);
    }

    @Test
    void testPublishVehicleDetailsForImport_Success() throws Exception {
        doNothing().when(pdaEntryService).publishVehicleDetailsForImport(
                transactionPoid, groupPoid, companyPoid, Long.valueOf(userId));

        mockMvc.perform(post("/api/v1/pda-entries/{transactionPoid}/vehicle-details/publish", transactionPoid)
                .header("X-Group-Poid", groupPoid)
                .header("X-Company-Poid", companyPoid)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vehicle details published for import successfully"));

        verify(pdaEntryService, times(1)).publishVehicleDetailsForImport(
                transactionPoid, groupPoid, companyPoid, Long.valueOf(userId));
    }

    @Test
    void testCreateFda_Success() throws Exception {
        when(pdaEntryService.createFda(transactionPoid, groupPoid, companyPoid, Long.valueOf(userId)))
                .thenReturn("FDA_POID_12345");

        mockMvc.perform(post("/api/v1/pda-entries/{transactionPoid}/create-fda", transactionPoid)
                .header("X-Group-Poid", groupPoid)
                .header("X-Company-Poid", companyPoid)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("FDA created successfully"))
                .andExpect(jsonPath("$.result").value("FDA_POID_12345"));

        verify(pdaEntryService, times(1)).createFda(transactionPoid, groupPoid, companyPoid, Long.valueOf(userId));
    }

    @Test
    void testClearChargeDetails_Success() throws Exception {
        doNothing().when(pdaEntryService).clearChargeDetails(transactionPoid, groupPoid, companyPoid, Long.valueOf(userId));

        mockMvc.perform(post("/api/v1/pda-entries/{transactionPoid}/charge-details/clear", transactionPoid)
                .header("X-Group-Poid", groupPoid)
                .header("X-Company-Poid", companyPoid)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Charge details cleared successfully"));

        verify(pdaEntryService, times(1)).clearChargeDetails(transactionPoid, groupPoid, companyPoid, Long.valueOf(userId));
    }

    @Test
    void testClearVehicleDetails_Success() throws Exception {
        doNothing().when(pdaEntryService).clearVehicleDetails(transactionPoid, groupPoid, companyPoid, Long.valueOf(userId));

        mockMvc.perform(post("/api/v1/pda-entries/{transactionPoid}/vehicle-details/clear", transactionPoid)
                .header("X-Group-Poid", groupPoid)
                .header("X-Company-Poid", companyPoid)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vehicle details cleared successfully"));

        verify(pdaEntryService, times(1)).clearVehicleDetails(transactionPoid, groupPoid, companyPoid, Long.valueOf(userId));
    }

    @Test
    void testImportVehicleDetails_Success() throws Exception {
        doNothing().when(pdaEntryService).importVehicleDetails(transactionPoid, groupPoid, companyPoid, Long.valueOf(userId));

        mockMvc.perform(post("/api/v1/pda-entries/{transactionPoid}/vehicle-details/import", transactionPoid)
                .header("X-Group-Poid", groupPoid)
                .header("X-Company-Poid", companyPoid)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vehicle details imported successfully"));

        verify(pdaEntryService, times(1)).importVehicleDetails(transactionPoid, groupPoid, companyPoid, Long.valueOf(userId));
    }

    @Test
    void testRecalculateChargeDetails_Success() throws Exception {
        PdaEntryChargeDetailResponse response = new PdaEntryChargeDetailResponse();
        response.setDetRowId(1L);
        response.setChargePoid(new BigDecimal(100));

        when(pdaEntryService.recalculateChargeDetails(transactionPoid, groupPoid, companyPoid, Long.valueOf(userId)))
                .thenReturn(List.of(response));

        mockMvc.perform(post("/api/v1/pda-entries/{transactionPoid}/recalculate", transactionPoid)
                .header("X-Group-Poid", groupPoid)
                .header("X-Company-Poid", companyPoid)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chargePoid").value(100));

        verify(pdaEntryService, times(1)).recalculateChargeDetails(transactionPoid, groupPoid, companyPoid, Long.valueOf(userId));
    }

    @Test
    void testLoadDefaultCharges_Success() throws Exception {
        PdaEntryChargeDetailResponse response = new PdaEntryChargeDetailResponse();
        response.setDetRowId(1L);
        response.setChargePoid(new BigDecimal(100));

        when(pdaEntryService.loadDefaultCharges(transactionPoid, groupPoid, companyPoid, Long.valueOf(userId)))
                .thenReturn(List.of(response));

        mockMvc.perform(post("/api/v1/pda-entries/{transactionPoid}/load-default-charges", transactionPoid)
                .header("X-Group-Poid", groupPoid)
                .header("X-Company-Poid", companyPoid)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chargePoid").value(100));

        verify(pdaEntryService, times(1)).loadDefaultCharges(transactionPoid, groupPoid, companyPoid, Long.valueOf(userId));
    }

    @Test
    void testGetTdrDetails_Success() throws Exception {
        PdaEntryTdrDetailResponse response = new PdaEntryTdrDetailResponse();
        response.setDetRowId(1L);
        response.setMlo("MLO123");

        when(pdaEntryService.getTdrDetails(transactionPoid, groupPoid, companyPoid))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/pda-entries/{transactionPoid}/tdr-details", transactionPoid)
                .header("X-Group-Poid", groupPoid)
                .header("X-Company-Poid", companyPoid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mlo").value("MLO123"));

        verify(pdaEntryService, times(1)).getTdrDetails(transactionPoid, groupPoid, companyPoid);
    }

    @Test
    void testGetAcknowledgmentDetails_Success() throws Exception {
        PdaEntryAcknowledgmentDetailResponse response = new PdaEntryAcknowledgmentDetailResponse();
        response.setDetRowId(1L);
        response.setParticulars("TEST");

        when(pdaEntryService.getAcknowledgmentDetails(transactionPoid, groupPoid, companyPoid))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/pda-entries/{transactionPoid}/acknowledgment-details", transactionPoid)
                .header("X-Group-Poid", groupPoid)
                .header("X-Company-Poid", companyPoid)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].particulars").value("TEST"));

        verify(pdaEntryService, times(1)).getAcknowledgmentDetails(transactionPoid, groupPoid, companyPoid);
    }

    @Test
    void testDeleteChargeDetail_Success() throws Exception {
        doNothing().when(pdaEntryService).deleteChargeDetail(transactionPoid, 1L, groupPoid, companyPoid, userId);

        mockMvc.perform(delete("/api/v1/pda-entries/{transactionPoid}/charge-details/{detRowId}", transactionPoid, 1L)
                .header("X-Group-Poid", groupPoid)
                .header("X-Company-Poid", companyPoid)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Charge detail deleted successfully"));

        verify(pdaEntryService, times(1)).deleteChargeDetail(transactionPoid, 1L, groupPoid, companyPoid, userId);
    }

    @Test
    void testBulkSaveTdrDetails_Success() throws Exception {
        BulkSaveTdrDetailsRequest request = new BulkSaveTdrDetailsRequest();
        PdaEntryTdrDetailRequest tdrDetail = new PdaEntryTdrDetailRequest();
        tdrDetail.setMlo("MLO123");
        request.setTdrDetails(List.of(tdrDetail));
        request.setDeleteDetRowIds(new ArrayList<>());

        PdaEntryTdrDetailResponse response = new PdaEntryTdrDetailResponse();
        response.setDetRowId(1L);
        response.setMlo("MLO123");

        when(pdaEntryService.bulkSaveTdrDetails(
                eq(transactionPoid), any(BulkSaveTdrDetailsRequest.class),
                eq(groupPoid), eq(companyPoid), eq(userId)))
                .thenReturn(List.of(response));

        mockMvc.perform(post("/api/v1/pda-entries/{transactionPoid}/tdr-details/bulk-save", transactionPoid)
                .header("X-Group-Poid", groupPoid)
                .header("X-Company-Poid", companyPoid)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mlo").value("MLO123"));

        verify(pdaEntryService, times(1)).bulkSaveTdrDetails(
                eq(transactionPoid), any(BulkSaveTdrDetailsRequest.class),
                eq(groupPoid), eq(companyPoid), eq(userId));
    }

    @Test
    void testBulkSaveAcknowledgmentDetails_Success() throws Exception {
        BulkSaveAcknowledgmentDetailsRequest request = new BulkSaveAcknowledgmentDetailsRequest();
        PdaEntryAcknowledgmentDetailRequest ackDetail = new PdaEntryAcknowledgmentDetailRequest();
        ackDetail.setParticulars("TEST");
        request.setAcknowledgmentDetails(List.of(ackDetail));
        request.setDeleteDetRowIds(new ArrayList<>());

        PdaEntryAcknowledgmentDetailResponse response = new PdaEntryAcknowledgmentDetailResponse();
        response.setDetRowId(1L);
        response.setParticulars("TEST");

        when(pdaEntryService.bulkSaveAcknowledgmentDetails(
                eq(transactionPoid), any(BulkSaveAcknowledgmentDetailsRequest.class),
                eq(groupPoid), eq(companyPoid), eq(userId)))
                .thenReturn(List.of(response));

        mockMvc.perform(post("/api/v1/pda-entries/{transactionPoid}/acknowledgment-details/bulk-save", transactionPoid)
                .header("X-Group-Poid", groupPoid)
                .header("X-Company-Poid", companyPoid)
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].particulars").value("TEST"));

        verify(pdaEntryService, times(1)).bulkSaveAcknowledgmentDetails(
                eq(transactionPoid), any(BulkSaveAcknowledgmentDetailsRequest.class),
                eq(groupPoid), eq(companyPoid), eq(userId));
    }
}
