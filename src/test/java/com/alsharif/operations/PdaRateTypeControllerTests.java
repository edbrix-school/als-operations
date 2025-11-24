package com.alsharif.operations;

import com.alsharif.operations.commonlov.controller.PdaRateTypeController;
import com.alsharif.operations.commonlov.dto.PdaRateTypeRequestDTO;
import com.alsharif.operations.commonlov.dto.PdaRateTypeResponseDTO;
import com.alsharif.operations.commonlov.service.PdaRateTypeServiceImpl;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class PdaRateTypeControllerTests {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PdaRateTypeServiceImpl service;

    @InjectMocks
    private PdaRateTypeController controller;

    private PdaRateTypeRequestDTO requestDTO;
    private PdaRateTypeResponseDTO responseDTO;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

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

    // CREATE
    @Test
    void testCreatePdaRateType() throws Exception {
        when(service.createRateType(any(PdaRateTypeRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/pda-rate-type")
                        .param("documentId", "PDA-RT-001")
                        .param("actionRequested", "create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("PDA Rate Type created successfully"));
    }

    // UPDATE
    @Test
    void testUpdatePdaRateType() throws Exception {
        when(service.updateRateType(eq(1L), any(PdaRateTypeRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/pda-rate-type/1")
                        .param("documentId", "PDA-RT-001")
                        .param("actionRequested", "update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("PDA Rate Type updated successfully"));
    }

    // LIST
    @Test
    void testListPdaRateType() throws Exception {

        Page<PdaRateTypeResponseDTO> page = new PageImpl<>(
                Arrays.asList(responseDTO),
                PageRequest.of(0, 10),
                1
        );

        when(service.getRateTypeList(any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/pda-rate-type")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "rateTypeName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // GET BY ID
    @Test
    void testGetById() throws Exception {
        when(service.getRateTypeById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/pda-rate-type/1")
                        .param("documentId", "PDA-RT-001")
                        .param("actionRequested", "get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // DELETE (soft delete)
    @Test
    void testSoftDelete() throws Exception {
        doNothing().when(service).deleteRateType(1L, false);

        mockMvc.perform(delete("/api/v1/pda-rate-type/1")
                        .param("documentId", "PDA-RT-001")
                        .param("actionRequested", "delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("PDA Rate Type soft deleted successfully"));
    }


}
