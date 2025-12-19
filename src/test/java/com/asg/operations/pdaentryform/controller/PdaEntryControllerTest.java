package com.asg.operations.pdaentryform.controller;

import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.pdaentryform.dto.*;
import com.asg.operations.pdaentryform.service.PdaEntryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
    private Long userPoid;
    private Long transactionPoid;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pdaEntryController).build();
        objectMapper = new ObjectMapper();
        groupPoid = 1L;
        companyPoid = 100L;
        userId = "USER123";
        userPoid = 123L;
        transactionPoid = 1000L;
    }

    private void mockUserContext(MockedStatic<UserContext> mockedUserContext) {
        mockedUserContext.when(UserContext::getGroupPoid).thenReturn(groupPoid);
        mockedUserContext.when(UserContext::getCompanyPoid).thenReturn(companyPoid);
        mockedUserContext.when(UserContext::getUserId).thenReturn(userId);
        mockedUserContext.when(UserContext::getUserPoid).thenReturn(userPoid);
    }

    @Test
    void testGetPdaEntryList_Success() throws Exception {
        GetAllPdaFilterRequest filterRequest = new GetAllPdaFilterRequest();
        filterRequest.setIsDeleted("N");
        filterRequest.setOperator("AND");
        filterRequest.setFilters(new ArrayList<>());

        PdaEntryListResponse listResponse = new PdaEntryListResponse();
        listResponse.setTransactionPoid(transactionPoid);
        listResponse.setDocRef("DOC123");

        org.springframework.data.domain.Page<PdaEntryListResponse> page = 
            new org.springframework.data.domain.PageImpl<>(List.of(listResponse));

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.getAllPdaWithFilters(
                    eq(groupPoid), eq(companyPoid), any(GetAllPdaFilterRequest.class),
                    eq(0), eq(20), eq(null)))
                    .thenReturn(page);

            mockMvc.perform(post("/v1/pda-entries/search")
                    .param("page", "0")
                    .param("size", "20")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(filterRequest)))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).getAllPdaWithFilters(
                    eq(groupPoid), eq(companyPoid), any(GetAllPdaFilterRequest.class),
                    eq(0), eq(20), eq(null));
        }
    }

    @Test
    void testGetPdaEntryById_Success() throws Exception {
        PdaEntryResponse response = new PdaEntryResponse();
        response.setTransactionPoid(transactionPoid);
        response.setDocRef("DOC123");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.getPdaEntryById(transactionPoid, groupPoid, companyPoid))
                    .thenReturn(response);

            mockMvc.perform(get("/v1/pda-entries/{transactionPoid}", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).getPdaEntryById(transactionPoid, groupPoid, companyPoid);
        }
    }

    @Test
    void testCreatePdaEntry_Success() throws Exception {
        PdaEntryRequest request = new PdaEntryRequest();
        request.setPrincipalPoid(new BigDecimal(100));
        request.setRefType("GENERAL");

        PdaEntryResponse response = new PdaEntryResponse();
        response.setTransactionPoid(transactionPoid);
        response.setDocRef("DOC123");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.createPdaEntry(any(PdaEntryRequest.class), eq(groupPoid), eq(companyPoid), eq(userPoid)))
                    .thenReturn(response);

            mockMvc.perform(post("/v1/pda-entries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).createPdaEntry(any(PdaEntryRequest.class), eq(groupPoid), eq(companyPoid), eq(userPoid));
        }
    }

    @Test
    void testUpdatePdaEntry_Success() throws Exception {
        PdaEntryRequest request = new PdaEntryRequest();
        request.setPrincipalPoid(new BigDecimal(100));
        request.setRefType("GENERAL");

        PdaEntryResponse response = new PdaEntryResponse();
        response.setTransactionPoid(transactionPoid);
        response.setDocRef("DOC123");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.updatePdaEntry(eq(transactionPoid), any(PdaEntryRequest.class), eq(groupPoid), eq(companyPoid), eq(userPoid)))
                    .thenReturn(response);

            mockMvc.perform(put("/v1/pda-entries/{transactionPoid}", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).updatePdaEntry(eq(transactionPoid), any(PdaEntryRequest.class), eq(groupPoid), eq(companyPoid), eq(userPoid));
        }
    }

    @Test
    void testDeletePdaEntry_Success() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            doNothing().when(pdaEntryService).deletePdaEntry(transactionPoid, groupPoid, companyPoid, userPoid);

            mockMvc.perform(delete("/v1/pda-entries/{transactionPoid}", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).deletePdaEntry(transactionPoid, groupPoid, companyPoid, userPoid);
        }
    }

    @Test
    void testGetChargeDetails_Success() throws Exception {
        PdaEntryChargeDetailResponse response = new PdaEntryChargeDetailResponse();
        response.setDetRowId(1L);
        response.setChargePoid(new BigDecimal(100));

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.getChargeDetails(transactionPoid, groupPoid, companyPoid))
                    .thenReturn(List.of(response));

            mockMvc.perform(get("/v1/pda-entries/{transactionPoid}/charge-details", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.data[0].chargePoid").value(100));

            verify(pdaEntryService, times(1)).getChargeDetails(transactionPoid, groupPoid, companyPoid);
        }
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

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.bulkSaveChargeDetails(
                    eq(transactionPoid), any(BulkSaveChargeDetailsRequest.class),
                    eq(groupPoid), eq(companyPoid), eq(userId)))
                    .thenReturn(List.of(response));

            mockMvc.perform(post("/v1/pda-entries/{transactionPoid}/charge-details/bulk-save", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.data[0].detRowId").value(1));

            verify(pdaEntryService, times(1)).bulkSaveChargeDetails(
                    eq(transactionPoid), any(BulkSaveChargeDetailsRequest.class),
                    eq(groupPoid), eq(companyPoid), eq(userId));
        }
    }

    @Test
    void testDeleteChargeDetail_Success() throws Exception {
        Long detRowId = 1L;

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            doNothing().when(pdaEntryService).deleteChargeDetail(transactionPoid, detRowId, groupPoid, companyPoid, userId);

            mockMvc.perform(delete("/v1/pda-entries/{transactionPoid}/charge-details/{detRowId}", transactionPoid, detRowId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).deleteChargeDetail(transactionPoid, detRowId, groupPoid, companyPoid, userId);
        }
    }

    @Test
    void testClearChargeDetails_Success() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            doNothing().when(pdaEntryService).clearChargeDetails(transactionPoid, groupPoid, companyPoid, userPoid);

            mockMvc.perform(post("/v1/pda-entries/{transactionPoid}/charge-details/clear", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).clearChargeDetails(transactionPoid, groupPoid, companyPoid, userPoid);
        }
    }

    @Test
    void testRecalculateChargeDetails_Success() throws Exception {
        PdaEntryChargeDetailResponse response = new PdaEntryChargeDetailResponse();
        response.setDetRowId(1L);
        response.setChargePoid(new BigDecimal(100));

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.recalculateChargeDetails(transactionPoid, groupPoid, companyPoid, userPoid))
                    .thenReturn(List.of(response));

            mockMvc.perform(post("/v1/pda-entries/{transactionPoid}/recalculate", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).recalculateChargeDetails(transactionPoid, groupPoid, companyPoid, userPoid);
        }
    }

    @Test
    void testLoadDefaultCharges_Success() throws Exception {
        PdaEntryChargeDetailResponse response = new PdaEntryChargeDetailResponse();
        response.setDetRowId(1L);
        response.setChargePoid(new BigDecimal(100));

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.loadDefaultCharges(transactionPoid, groupPoid, companyPoid, userPoid))
                    .thenReturn(List.of(response));

            mockMvc.perform(post("/v1/pda-entries/{transactionPoid}/load-default-charges", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).loadDefaultCharges(transactionPoid, groupPoid, companyPoid, userPoid);
        }
    }

    @Test
    void testGetVehicleDetails_Success() throws Exception {
        PdaEntryVehicleDetailResponse response = new PdaEntryVehicleDetailResponse();
        response.setDetRowId(1L);
        response.setVesselName("MAERSK");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.getVehicleDetails(transactionPoid, groupPoid, companyPoid))
                    .thenReturn(List.of(response));

            mockMvc.perform(get("/v1/pda-entries/{transactionPoid}/vehicle-details", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.data[0].vesselName").value("MAERSK"));

            verify(pdaEntryService, times(1)).getVehicleDetails(transactionPoid, groupPoid, companyPoid);
        }
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

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.bulkSaveVehicleDetails(
                    eq(transactionPoid), any(BulkSaveVehicleDetailsRequest.class),
                    eq(groupPoid), eq(companyPoid), eq(userId)))
                    .thenReturn(List.of(response));

            mockMvc.perform(post("/v1/pda-entries/{transactionPoid}/vehicle-details/bulk-save", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.data[0].vesselName").value("MAERSK"));

            verify(pdaEntryService, times(1)).bulkSaveVehicleDetails(
                    eq(transactionPoid), any(BulkSaveVehicleDetailsRequest.class),
                    eq(groupPoid), eq(companyPoid), eq(userId));
        }
    }

    @Test
    void testImportVehicleDetails_Success() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            doNothing().when(pdaEntryService).importVehicleDetails(transactionPoid, groupPoid, companyPoid, userPoid);

            mockMvc.perform(post("/v1/pda-entries/{transactionPoid}/vehicle-details/import", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).importVehicleDetails(transactionPoid, groupPoid, companyPoid, userPoid);
        }
    }

    @Test
    void testClearVehicleDetails_Success() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            doNothing().when(pdaEntryService).clearVehicleDetails(transactionPoid, groupPoid, companyPoid, userPoid);

            mockMvc.perform(post("/v1/pda-entries/{transactionPoid}/vehicle-details/clear", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).clearVehicleDetails(transactionPoid, groupPoid, companyPoid, userPoid);
        }
    }

    @Test
    void testPublishVehicleDetailsForImport_Success() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            doNothing().when(pdaEntryService).publishVehicleDetailsForImport(
                    transactionPoid, groupPoid, companyPoid, userPoid);

            mockMvc.perform(post("/v1/pda-entries/{transactionPoid}/vehicle-details/publish", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Vehicle details published for import successfully"));

            verify(pdaEntryService, times(1)).publishVehicleDetailsForImport(
                    transactionPoid, groupPoid, companyPoid, userPoid);
        }
    }

    @Test
    void testGetTdrDetails_Success() throws Exception {
        PdaEntryTdrDetailResponse response = new PdaEntryTdrDetailResponse();
        response.setDetRowId(1L);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.getTdrDetails(transactionPoid, groupPoid, companyPoid))
                    .thenReturn(List.of(response));

            mockMvc.perform(get("/v1/pda-entries/{transactionPoid}/tdr-details", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).getTdrDetails(transactionPoid, groupPoid, companyPoid);
        }
    }

    @Test
    void testBulkSaveTdrDetails_Success() throws Exception {
        BulkSaveTdrDetailsRequest request = new BulkSaveTdrDetailsRequest();
        request.setTdrDetails(new ArrayList<>());
        request.setDeleteDetRowIds(new ArrayList<>());

        PdaEntryTdrDetailResponse response = new PdaEntryTdrDetailResponse();
        response.setDetRowId(1L);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.bulkSaveTdrDetails(
                    eq(transactionPoid), any(BulkSaveTdrDetailsRequest.class),
                    eq(groupPoid), eq(companyPoid), eq(userId)))
                    .thenReturn(List.of(response));

            mockMvc.perform(post("/v1/pda-entries/{transactionPoid}/tdr-details/bulk-save", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).bulkSaveTdrDetails(
                    eq(transactionPoid), any(BulkSaveTdrDetailsRequest.class),
                    eq(groupPoid), eq(companyPoid), eq(userId));
        }
    }

    @Test
    void testGetAcknowledgmentDetails_Success() throws Exception {
        PdaEntryAcknowledgmentDetailResponse response = new PdaEntryAcknowledgmentDetailResponse();
        response.setDetRowId(1L);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.getAcknowledgmentDetails(transactionPoid, groupPoid, companyPoid))
                    .thenReturn(List.of(response));

            mockMvc.perform(get("/v1/pda-entries/{transactionPoid}/acknowledgment-details", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).getAcknowledgmentDetails(transactionPoid, groupPoid, companyPoid);
        }
    }

    @Test
    void testBulkSaveAcknowledgmentDetails_Success() throws Exception {
        BulkSaveAcknowledgmentDetailsRequest request = new BulkSaveAcknowledgmentDetailsRequest();
        request.setAcknowledgmentDetails(new ArrayList<>());
        request.setDeleteDetRowIds(new ArrayList<>());

        PdaEntryAcknowledgmentDetailResponse response = new PdaEntryAcknowledgmentDetailResponse();
        response.setDetRowId(1L);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.bulkSaveAcknowledgmentDetails(
                    eq(transactionPoid), any(BulkSaveAcknowledgmentDetailsRequest.class),
                    eq(groupPoid), eq(companyPoid), eq(userId)))
                    .thenReturn(List.of(response));

            mockMvc.perform(post("/v1/pda-entries/{transactionPoid}/acknowledgment-details/bulk-save", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).bulkSaveAcknowledgmentDetails(
                    eq(transactionPoid), any(BulkSaveAcknowledgmentDetailsRequest.class),
                    eq(groupPoid), eq(companyPoid), eq(userId));
        }
    }

    @Test
    void testUploadAcknowledgmentDetails_Success() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            doNothing().when(pdaEntryService).uploadAcknowledgmentDetails(transactionPoid, groupPoid, companyPoid, userPoid);

            mockMvc.perform(post("/v1/pda-entries/{transactionPoid}/acknow/upload-details", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).uploadAcknowledgmentDetails(transactionPoid, groupPoid, companyPoid, userPoid);
        }
    }

    @Test
    void testClearAcknowledgmentDetails_Success() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            doNothing().when(pdaEntryService).clearAcknowledgmentDetails(transactionPoid, groupPoid, companyPoid, userPoid);

            mockMvc.perform(post("/v1/pda-entries/{transactionPoid}/acknow/clear-details", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).clearAcknowledgmentDetails(transactionPoid, groupPoid, companyPoid, userPoid);
        }
    }

    @Test
    void testValidateBeforeSave_Success() throws Exception {
        PdaEntryRequest request = new PdaEntryRequest();
        request.setPrincipalPoid(new BigDecimal(100));
        request.setRefType("GENERAL");

        ValidationResponse response = new ValidationResponse();
        response.setValid(true);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.validateBeforeSave(eq(transactionPoid), any(PdaEntryRequest.class), eq(groupPoid), eq(companyPoid), eq(userPoid)))
                    .thenReturn(response);

            mockMvc.perform(post("/v1/pda-entries/validate-before-save")
                    .param("transactionPoid", transactionPoid.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).validateBeforeSave(eq(transactionPoid), any(PdaEntryRequest.class), eq(groupPoid), eq(companyPoid), eq(userPoid));
        }
    }

    @Test
    void testValidateAfterSave_Success() throws Exception {
        ValidationResponse response = new ValidationResponse();
        response.setValid(true);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.validateAfterSave(transactionPoid, groupPoid, companyPoid, userPoid))
                    .thenReturn(response);

            mockMvc.perform(post("/v1/pda-entries/{transactionPoid}/validate-after-save", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).validateAfterSave(transactionPoid, groupPoid, companyPoid, userPoid);
        }
    }

    @Test
    void testGetVesselDetails_Success() throws Exception {
        BigDecimal vesselPoid = new BigDecimal(500);
        VesselDetailsResponse response = new VesselDetailsResponse();
        response.setVesselTypePoid(new BigDecimal(1));
        response.setImoNumber("IMO123456");

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.getVesselDetails(vesselPoid, groupPoid, companyPoid, userPoid))
                    .thenReturn(response);

            mockMvc.perform(get("/v1/pda-entries/vessel-details")
                    .param("vesselPoid", vesselPoid.toString())
                    .param("transactionPoid", transactionPoid.toString())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).getVesselDetails(vesselPoid, groupPoid, companyPoid, userPoid);
        }
    }

    @Test
    void testCreateFda_Success() throws Exception {
        String fdaResult = "FDA_POID:12345";

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);

            when(pdaEntryService.createFda(transactionPoid, groupPoid, companyPoid, userPoid))
                    .thenReturn(fdaResult);

            mockMvc.perform(post("/v1/pda-entries/{transactionPoid}/create-fda", transactionPoid)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(pdaEntryService, times(1)).createFda(transactionPoid, groupPoid, companyPoid, userPoid);
        }
    }
}