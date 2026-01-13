package com.asg.operations.pdaporttariffmaster.controller;

import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.pdaporttariffmaster.dto.*;
import com.asg.operations.pdaporttariffmaster.service.PdaPortTariffHdrService;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PdaPortTariffMasterControllerTest {

    private MockMvc mockMvc;
    private MockedStatic<UserContext> mockedUserContext;
    private ObjectMapper objectMapper;

    @Mock
    private PdaPortTariffHdrService tariffService;

    @InjectMocks
    private PdaPortTariffMasterController controller;

    @BeforeEach
    void setUp() {
        mockedUserContext = mockStatic(UserContext.class);
        mockedUserContext.when(UserContext::getCompanyPoid).thenReturn(100L);
        mockedUserContext.when(UserContext::getGroupPoid).thenReturn(200L);
        mockedUserContext.when(UserContext::getUserId).thenReturn("user1");

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (mockedUserContext != null) {
            mockedUserContext.close();
        }
    }

    @Test
    void getTariffList_Success() throws Exception {
        GetAllTariffFilterRequest filterRequest = new GetAllTariffFilterRequest();
        filterRequest.setIsDeleted("N");
        filterRequest.setOperator("AND");
        filterRequest.setFilters(Collections.emptyList());

        Page<PdaPortTariffListResponse> page = new PageImpl<>(List.of(createMockListResponse()));
        when(tariffService.getAllTariffsWithFilters(eq(200L), eq(100L), any(GetAllTariffFilterRequest.class), eq(0), eq(20), any()))
                .thenReturn(page);

        mockMvc.perform(post("/v1/pda-port-tariffs/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterRequest))
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tariff list fetched successfully"));

        verify(tariffService).getAllTariffsWithFilters(eq(200L), eq(100L), any(GetAllTariffFilterRequest.class), eq(0), eq(20), any());
    }

    @Test
    void getTariffById_Success() throws Exception {
        PdaPortTariffMasterResponse response = createMockResponse();
        when(tariffService.getTariffById(1L, 200L)).thenReturn(response);

        mockMvc.perform(get("/v1/pda-port-tariffs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tariff retrieved successfully"));

        verify(tariffService).getTariffById(1L, 200L);
    }

    @Test
    void createTariff_Success() throws Exception {
        PdaPortTariffMasterRequest request = createMockRequest();
        PdaPortTariffMasterResponse response = createMockResponse();

        when(tariffService.createTariff(any(), eq(200L), eq(100L), eq("user1"))).thenReturn(response);

        mockMvc.perform(post("/v1/pda-port-tariffs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tariff created successfully"));

        verify(tariffService).createTariff(any(), eq(200L), eq(100L), eq("user1"));
    }

    @Test
    void updateTariff_Success() throws Exception {
        PdaPortTariffMasterRequest request = createMockRequest();
        PdaPortTariffMasterResponse response = createMockResponse();

        when(tariffService.updateTariff(eq(1L), any(), eq(200L), eq("user1"))).thenReturn(response);

        mockMvc.perform(put("/v1/pda-port-tariffs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tariff updated successfully"));

        verify(tariffService).updateTariff(eq(1L), any(), eq(200L), eq("user1"));
    }

    @Test
    void deleteTariff_Success() throws Exception {
        doNothing().when(tariffService).deleteTariff(1L, 200L, "user1", false, deleteReasonDto);

        mockMvc.perform(delete("/v1/pda-port-tariffs/1")
                .param("hardDelete", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tariff deleted successfully"));

        verify(tariffService).deleteTariff(1L, 200L, "user1", false, deleteReasonDto);
    }

    @Test
    void getChargeDetails_Success() throws Exception {
        ChargeDetailsResponse chargeResponse = new ChargeDetailsResponse();
        when(tariffService.getChargeDetails(eq(1L), eq(200L), eq(true))).thenReturn(chargeResponse);

        mockMvc.perform(get("/v1/pda-port-tariffs/1/charges")
                .param("includeSlabs", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Charge details retrieved successfully"));

        verify(tariffService).getChargeDetails(eq(1L), eq(200L), eq(true));
    }

    @Test
    void bulkSaveChargeDetails_Success() throws Exception {
        ChargeDetailsRequest chargeRequest = new ChargeDetailsRequest();
        chargeRequest.setChargeDetails(List.of(new PdaPortTariffChargeDetailRequest()));
        ChargeDetailsResponse chargeResponse = new ChargeDetailsResponse();

        when(tariffService.bulkSaveChargeDetails(eq(1L), any(), eq(200L), eq("user1"))).thenReturn(chargeResponse);

        mockMvc.perform(post("/v1/pda-port-tariffs/1/charges/bulk")
                .header("X-User-Id", "user1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chargeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Charge details saved successfully"));

        verify(tariffService).bulkSaveChargeDetails(eq(1L), any(), eq(200L), eq("user1"));
    }

    @Test
    void copyTariff_Success() throws Exception {
        CopyTariffRequest copyRequest = new CopyTariffRequest();
        copyRequest.setNewPeriodFrom(LocalDate.of(2024, 1, 1));
        copyRequest.setNewPeriodTo(LocalDate.of(2024, 12, 31));
        PdaPortTariffMasterResponse response = createMockResponse();

        when(tariffService.copyTariff(eq(1L), any(), eq(200L), eq("user1"))).thenReturn(response);

        mockMvc.perform(post("/v1/pda-port-tariffs/1/copy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(copyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tariff copied successfully"));

        verify(tariffService).copyTariff(eq(1L), any(), eq(200L), eq("user1"));
    }

    private PdaPortTariffMasterRequest createMockRequest() {
        PdaPortTariffMasterRequest request = new PdaPortTariffMasterRequest();
        request.setDocRef("DOC001");
        request.setRemarks("Test tariff");
        request.setPort("1");
        request.setVesselTypes(List.of("1", "2"));
        request.setPeriodFrom(LocalDate.of(2024, 1, 1));
        request.setPeriodTo(LocalDate.of(2024, 12, 31));
        return request;
    }

    private PdaPortTariffMasterResponse createMockResponse() {
        PdaPortTariffMasterResponse response = new PdaPortTariffMasterResponse();
        response.setTransactionPoid(1L);
        response.setDocRef("DOC001");
        return response;
    }

    private PdaPortTariffListResponse createMockListResponse() {
        PdaPortTariffListResponse response = new PdaPortTariffListResponse();
        response.setTransactionPoid(1L);
        response.setDocRef("DOC001");
        return response;
    }
}