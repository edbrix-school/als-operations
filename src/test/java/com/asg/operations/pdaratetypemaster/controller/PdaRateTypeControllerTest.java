package com.asg.operations.pdaratetypemaster.controller;

import com.asg.operations.pdaratetypemaster.dto.PageResponse;
import com.asg.operations.pdaratetypemaster.dto.PdaRateTypeRequestDTO;
import com.asg.operations.pdaratetypemaster.dto.PdaRateTypeResponseDTO;
import com.asg.operations.pdaratetypemaster.service.PdaRateTypeService;
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
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;



import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class PdaRateTypeControllerTest {

    private MockedStatic<com.asg.common.lib.security.util.UserContext> mockedUserContext;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

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

    // CREATE
    @Test
    void testCreatePdaRateType() throws Exception {
        when(service.createRateType(any(PdaRateTypeRequestDTO.class), eq(1L), eq("testUser"))).thenReturn(responseDTO);

        mockMvc.perform(post("/v1/pda-rate-types")
                        .header("X-Group-Poid", "1")
                        .header("X-User-Id", "testUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Rate type created successfully"));
    }

    // UPDATE
    @Test
    void testUpdatePdaRateType() throws Exception {
        when(service.updateRateType(eq(1L), any(PdaRateTypeRequestDTO.class), eq(1L), eq("testUser"))).thenReturn(responseDTO);

        mockMvc.perform(put("/v1/pda-rate-types/1")
                        .header("X-Group-Poid", "1")
                        .header("X-User-Id", "testUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Rate type updated successfully"));
    }

    // LIST
    @Test
    void testListPdaRateType() throws Exception {
        PageResponse<PdaRateTypeResponseDTO> pageResponse = new PageResponse<>(
                Arrays.asList(responseDTO), 0, 10, 1, 1, true, true, 1);

        when(service.getRateTypeList(any(), any(), any(), eq(1L), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/v1/pda-rate-types")
                        .header("X-Group-Poid", "1")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "rateTypeName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // GET BY ID
    @Test
    void testGetById() throws Exception {
        when(service.getRateTypeById(1L, 1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/v1/pda-rate-types/1")
                        .header("X-Group-Poid", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // DELETE (soft delete)
    @Test
    void testSoftDelete() throws Exception {
        doNothing().when(service).deleteRateType(1L, 1L, "testUser", false);

        mockMvc.perform(delete("/v1/pda-rate-types/1")
                        .header("X-Group-Poid", "1")
                        .header("X-User-Id", "testUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Rate type deleted successfully"));
    }


}
