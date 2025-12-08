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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PdaPortTariffMasterControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PdaPortTariffHdrService tariffService;

    @InjectMocks
    private PdaPortTariffMasterController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void getTariffList_Success() throws Exception {
        PageResponse<PdaPortTariffMasterResponse> pageResponse = createMockPageResponse();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCompanyPoid).thenReturn(100L);
            
            when(tariffService.getTariffList(any(), any(), any(), any(), eq(100L), any(Pageable.class)))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/v1/pda-port-tariffs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Test
    void getTariffById_Success() throws Exception {
        PdaPortTariffMasterResponse response = createMockResponse();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(100L);
            
            when(tariffService.getTariffById(1L, 100L)).thenReturn(response);

            mockMvc.perform(get("/v1/pda-port-tariffs/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Test
    void createTariff_Success() throws Exception {
        PdaPortTariffMasterRequest request = createMockRequest();
        PdaPortTariffMasterResponse response = createMockResponse();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(100L);
            mockedUserContext.when(UserContext::getCompanyPoid).thenReturn(200L);
            mockedUserContext.when(UserContext::getUserId).thenReturn("user1");
            
            when(tariffService.createTariff(any(), eq(100L), eq(200L), eq("user1"))).thenReturn(response);

            mockMvc.perform(post("/v1/pda-port-tariffs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Test
    void updateTariff_Success() throws Exception {
        PdaPortTariffMasterRequest request = createMockRequest();
        PdaPortTariffMasterResponse response = createMockResponse();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(100L);
            mockedUserContext.when(UserContext::getUserId).thenReturn("user1");
            
            when(tariffService.updateTariff(eq(1L), any(), eq(100L), eq("user1"))).thenReturn(response);

            mockMvc.perform(put("/v1/pda-port-tariffs/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Test
    void deleteTariff_Success() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(100L);
            mockedUserContext.when(UserContext::getUserId).thenReturn("user1");

            mockMvc.perform(delete("/v1/pda-port-tariffs/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Test
    void getChargeDetails_Success() throws Exception {
        ChargeDetailsResponse chargeResponse = createMockChargeDetailsResponse();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(100L);
            
            when(tariffService.getChargeDetails(eq(1L), eq(100L), eq(true))).thenReturn(chargeResponse);

            mockMvc.perform(get("/v1/pda-port-tariffs/1/charges"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Test
    void bulkSaveChargeDetails_Success() throws Exception {
        ChargeDetailsRequest chargeRequest = createMockChargeDetailsRequest();
        ChargeDetailsResponse chargeResponse = createMockChargeDetailsResponse();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(100L);
            
            when(tariffService.bulkSaveChargeDetails(eq(1L), any(), eq(100L), eq("user1"))).thenReturn(chargeResponse);

            mockMvc.perform(post("/v1/pda-port-tariffs/1/charges/bulk")
                    .header("X-User-Id", "user1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chargeRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Test
    void copyTariff_Success() throws Exception {
        CopyTariffRequest copyRequest = createMockCopyRequest();
        PdaPortTariffMasterResponse response = createMockResponse();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(100L);
            mockedUserContext.when(UserContext::getUserId).thenReturn("user1");
            
            when(tariffService.copyTariff(eq(1L), any(), eq(100L), eq("user1"))).thenReturn(response);

            mockMvc.perform(post("/v1/pda-port-tariffs/1/copy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(copyRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    private PdaPortTariffMasterRequest createMockRequest() {
        PdaPortTariffMasterRequest request = new PdaPortTariffMasterRequest();
        request.setPorts(Arrays.asList("1001", "1002"));
        request.setVesselTypes(Arrays.asList("2001"));
        request.setPeriodFrom(LocalDate.now());
        request.setPeriodTo(LocalDate.now().plusMonths(6));
        request.setRemarks("Test tariff");
        return request;
    }

    private PdaPortTariffMasterResponse createMockResponse() {
        PdaPortTariffMasterResponse response = new PdaPortTariffMasterResponse();
        response.setTransactionPoid(1L);
        response.setDocRef("DOC001");
        response.setPorts(Arrays.asList("1001", "1002"));
        response.setVesselTypes(Arrays.asList("2001"));
        response.setPeriodFrom(LocalDate.now());
        response.setPeriodTo(LocalDate.now().plusMonths(6));
        return response;
    }

    private CopyTariffRequest createMockCopyRequest() {
        CopyTariffRequest request = new CopyTariffRequest();
        request.setNewPeriodFrom(LocalDate.now().plusYears(1));
        request.setNewPeriodTo(LocalDate.now().plusYears(1).plusMonths(6));
        return request;
    }

    private ChargeDetailsResponse createMockChargeDetailsResponse() {
        ChargeDetailsResponse response = new ChargeDetailsResponse();
        response.setTransactionPoid(1L);
        response.setChargeDetails(Arrays.asList());
        return response;
    }

    private ChargeDetailsRequest createMockChargeDetailsRequest() {
        ChargeDetailsRequest request = new ChargeDetailsRequest();
        
        PdaPortTariffChargeDetailRequest chargeDetail = new PdaPortTariffChargeDetailRequest();
        chargeDetail.setDetRowId(1L);
        chargeDetail.setChargePoid(BigDecimal.valueOf(1001));
        chargeDetail.setRateTypePoid(BigDecimal.valueOf(2001));
        chargeDetail.setTariffSlab("NONE");
        chargeDetail.setFixRate(BigDecimal.valueOf(100.00));
        chargeDetail.setHarborCallType("N/A");
        chargeDetail.setIsEnabled("Y");
        chargeDetail.setSeqNo(1);
        
        request.setChargeDetails(Arrays.asList(chargeDetail));
        return request;
    }

    private PageResponse<PdaPortTariffMasterResponse> createMockPageResponse() {
        PdaPortTariffMasterResponse response = createMockResponse();
        return new PageResponse<>(
                Arrays.asList(response),
                0,
                10,
                1L,
                1,
                true,
                true,
                1
        );
    }
}