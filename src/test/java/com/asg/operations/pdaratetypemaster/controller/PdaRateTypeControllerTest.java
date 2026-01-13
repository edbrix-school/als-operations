package com.asg.operations.pdaratetypemaster.controller;

import com.asg.operations.pdaratetypemaster.dto.GetAllRateTypeFilterRequest;
import com.asg.operations.pdaratetypemaster.dto.PdaRateTypeRequestDTO;
import com.asg.operations.pdaratetypemaster.dto.PdaRateTypeResponseDTO;
import com.asg.operations.pdaratetypemaster.dto.PdaRateTypeListResponse;
import com.asg.operations.pdaratetypemaster.service.PdaRateTypeService;
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class PdaRateTypeControllerTest {

    private MockMvc mockMvc;
    private MockedStatic<com.asg.common.lib.security.util.UserContext> mockedUserContext;
    private ObjectMapper objectMapper;

    @Mock
    private PdaRateTypeService service;

    @InjectMocks
    private PdaRateTypeController controller;

    private PdaRateTypeRequestDTO requestDTO;
    private PdaRateTypeResponseDTO responseDTO;

    @BeforeEach
    void setup() {
        mockedUserContext = mockStatic(com.asg.common.lib.security.util.UserContext.class);
        mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getGroupPoid).thenReturn(1L);
        mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getUserId).thenReturn("testUser");

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();

        requestDTO = new PdaRateTypeRequestDTO();
        requestDTO.setRateTypeCode("RT01");
        requestDTO.setRateTypeName("Loading Rate");
        requestDTO.setRateTypeName2("تحميل");
        requestDTO.setRateTypeFormula("(UNIT * DAYS)");
        requestDTO.setDefQty("UNIT");
        requestDTO.setDefDays(new BigDecimal("5"));
        requestDTO.setSeqNo(BigInteger.valueOf(1));
        requestDTO.setActive("Y");

        responseDTO = new PdaRateTypeResponseDTO();
        responseDTO.setRateTypeCode(requestDTO.getRateTypeCode());
        responseDTO.setRateTypeName(requestDTO.getRateTypeName());
        responseDTO.setRateTypeName2(requestDTO.getRateTypeName2());
        responseDTO.setRateTypeFormula(requestDTO.getRateTypeFormula());
        responseDTO.setDefQty(requestDTO.getDefQty());
        responseDTO.setDefDays(requestDTO.getDefDays());
        responseDTO.setSeqNo(requestDTO.getSeqNo());
        responseDTO.setActive(requestDTO.getActive());
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (mockedUserContext != null) {
            mockedUserContext.close();
        }
    }

    @Test
    void testCreatePdaRateType() throws Exception {
        when(service.createRateType(any(PdaRateTypeRequestDTO.class), eq(1L), eq("testUser"))).thenReturn(responseDTO);

        mockMvc.perform(post("/v1/pda-rate-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Rate type created successfully"));

        verify(service).createRateType(any(PdaRateTypeRequestDTO.class), eq(1L), eq("testUser"));
    }

    @Test
    void testUpdatePdaRateType() throws Exception {
        when(service.updateRateType(eq(1L), any(PdaRateTypeRequestDTO.class), eq(1L), eq("testUser"))).thenReturn(responseDTO);

        mockMvc.perform(put("/v1/pda-rate-types/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Rate type updated successfully"));

        verify(service).updateRateType(eq(1L), any(PdaRateTypeRequestDTO.class), eq(1L), eq("testUser"));
    }

    @Test
    void testListPdaRateType() throws Exception {
        GetAllRateTypeFilterRequest filterRequest = new GetAllRateTypeFilterRequest();
        filterRequest.setIsDeleted("N");
        filterRequest.setOperator("AND");
        filterRequest.setFilters(Collections.emptyList());

        Page<PdaRateTypeListResponse> page = new PageImpl<>(List.of(createListResponse()));
        when(service.getAllRateTypesWithFilters(eq(1L), any(GetAllRateTypeFilterRequest.class), eq(0), eq(20), any()))
                .thenReturn(page);

        mockMvc.perform(post("/v1/pda-rate-types/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterRequest))
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Rate type list retrieved successfully"));

        verify(service).getAllRateTypesWithFilters(eq(1L), any(GetAllRateTypeFilterRequest.class), eq(0), eq(20), any());
    }

    @Test
    void testGetById() throws Exception {
        when(service.getRateTypeById(1L, 1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/v1/pda-rate-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Rate type retrieved successfully"));

        verify(service).getRateTypeById(1L, 1L);
    }

    @Test
    void testSoftDelete() throws Exception {
        doNothing().when(service).deleteRateType(1L, 1L, "testUser", false, deleteReasonDto);

        mockMvc.perform(delete("/v1/pda-rate-types/1")
                .param("hardDelete", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Rate type deleted successfully"));

        verify(service).deleteRateType(1L, 1L, "testUser", false, deleteReasonDto);
    }

    @Test
    void testHardDelete() throws Exception {
        doNothing().when(service).deleteRateType(1L, 1L, "testUser", true, deleteReasonDto);

        mockMvc.perform(delete("/v1/pda-rate-types/1")
                .param("hardDelete", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Rate type deleted successfully"));

        verify(service).deleteRateType(1L, 1L, "testUser", true, deleteReasonDto);
    }

    private PdaRateTypeListResponse createListResponse() {
        PdaRateTypeListResponse listResponse = new PdaRateTypeListResponse();
        listResponse.setRateTypeId(1L);
        listResponse.setRateTypeCode("RT01");
        listResponse.setRateTypeName("Loading Rate");
        listResponse.setActive("Y");
        return listResponse;
    }
}