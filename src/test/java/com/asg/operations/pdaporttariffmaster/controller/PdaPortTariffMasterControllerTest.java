package com.asg.operations.pdaporttariffmaster.controller;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.operations.pdaporttariffmaster.dto.*;
import com.asg.operations.pdaporttariffmaster.service.PdaPortTariffHdrService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private PdaPortTariffMasterController controller;

    @BeforeEach
    void setUp() {
        mockedUserContext = mockStatic(UserContext.class);
        mockedUserContext.when(UserContext::getDocumentId).thenReturn("DOC_ID");

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void tearDown() {
        if (mockedUserContext != null) mockedUserContext.close();
    }

    @Test
    void getTariffById_Success() throws Exception {
        when(tariffService.getTariffById(1L)).thenReturn(createMockResponse());

        mockMvc.perform(get("/v1/pda-port-tariffs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tariff retrieved successfully"));

        verify(tariffService).getTariffById(1L);
    }

    @Test
    void createTariff_Success() throws Exception {
        when(tariffService.createTariff(any(PdaPortTariffMasterRequest.class))).thenReturn(createMockResponse());

        mockMvc.perform(post("/v1/pda-port-tariffs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createMockRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tariff created successfully"));

        verify(tariffService).createTariff(any(PdaPortTariffMasterRequest.class));
    }

    @Test
    void updateTariff_Success() throws Exception {
        when(tariffService.updateTariff(eq(1L), any(PdaPortTariffMasterRequest.class))).thenReturn(createMockResponse());

        mockMvc.perform(put("/v1/pda-port-tariffs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createMockRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tariff updated successfully"));

        verify(tariffService).updateTariff(eq(1L), any(PdaPortTariffMasterRequest.class));
    }

    @Test
    void deleteTariff_Success() throws Exception {
        doNothing().when(tariffService).deleteTariff(eq(1L), any());

        mockMvc.perform(delete("/v1/pda-port-tariffs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tariff deleted successfully"));

        verify(tariffService).deleteTariff(eq(1L), any());
    }

    @Test
    void getChargeDetails_Success() throws Exception {
        when(tariffService.getChargeDetails(eq(1L), eq(true))).thenReturn(new ChargeDetailsResponse());

        mockMvc.perform(get("/v1/pda-port-tariffs/1/charges")
                .param("includeSlabs", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Charge details retrieved successfully"));

        verify(tariffService).getChargeDetails(eq(1L), eq(true));
    }

    @Test
    void bulkSaveChargeDetails_Success() throws Exception {
        PdaPortTariffChargeDetailRequest chargeDetail = new PdaPortTariffChargeDetailRequest();
        chargeDetail.setChargePoid(new BigDecimal("1"));
        chargeDetail.setRateTypePoid(new BigDecimal("1"));
        ChargeDetailsRequest chargeRequest = new ChargeDetailsRequest();
        chargeRequest.setChargeDetails(List.of(chargeDetail));

        when(tariffService.bulkSaveChargeDetails(eq(1L), any(ChargeDetailsRequest.class))).thenReturn(new ChargeDetailsResponse());

        mockMvc.perform(post("/v1/pda-port-tariffs/1/charges/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chargeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Charge details saved successfully"));

        verify(tariffService).bulkSaveChargeDetails(eq(1L), any(ChargeDetailsRequest.class));
    }

    @Test
    void copyTariff_Success() throws Exception {
        CopyTariffRequest copyRequest = new CopyTariffRequest();
        copyRequest.setNewPeriodFrom(LocalDate.of(2024, 1, 1));
        copyRequest.setNewPeriodTo(LocalDate.of(2024, 12, 31));

        when(tariffService.copyTariff(eq(1L), any(CopyTariffRequest.class))).thenReturn(createMockResponse());

        mockMvc.perform(post("/v1/pda-port-tariffs/1/copy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(copyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tariff copied successfully"));

        verify(tariffService).copyTariff(eq(1L), any(CopyTariffRequest.class));
    }

    private PdaPortTariffMasterRequest createMockRequest() {
        PdaPortTariffMasterRequest request = new PdaPortTariffMasterRequest();
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
}
