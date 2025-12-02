package com.alsharif.operations.finaldisbursementaccount.controller;

import com.alsharif.operations.common.PageResponse;
import com.alsharif.operations.finaldisbursementaccount.dto.*;
import com.alsharif.operations.finaldisbursementaccount.service.FdaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FdaController.class)
class FdaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FdaService fdaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getFdaList_ShouldReturnPageResponse() throws Exception {
        PageResponse<FdaHeaderDto> mockResponse = new PageResponse<>();
        when(fdaService.getFdaList(anyLong(), anyLong(), any(), any(), any(), any(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/fdas")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("FDA list fetched successfully"));
    }

    @Test
    void getFda_ShouldReturnFdaHeader() throws Exception {
        FdaHeaderDto mockDto = new FdaHeaderDto();
        mockDto.setTransactionPoid(1L);
        when(fdaService.getFdaHeader(1L, 1L, 1L)).thenReturn(mockDto);

        mockMvc.perform(get("/api/v1/fdas/1")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("FDA fetched successfully"));
    }

    @Test
    void createFda_ShouldCreateAndReturnFda() throws Exception {
        FdaHeaderDto requestDto = new FdaHeaderDto();
        requestDto.setPrincipalPoid(1L);
        requestDto.setSalesmanPoid(1L);
        requestDto.setPortPoid(1L);
        requestDto.setGrt(BigDecimal.valueOf(1000));

        FdaHeaderDto responseDto = new FdaHeaderDto();
        responseDto.setTransactionPoid(1L);
        when(fdaService.createFdaHeader(any(FdaHeaderDto.class), anyLong(), anyLong(), anyString()))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/fdas")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L)
                        .header("X-User-Id", "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("FDA created successfully"));
    }

    @Test
    void updateFda_ShouldUpdateAndReturnFda() throws Exception {
        UpdateFdaHeaderRequest requestDto = new UpdateFdaHeaderRequest();
        requestDto.setPrincipalPoid(1L);
        requestDto.setSalesmanPoid(1L);
        requestDto.setPortPoid(1L);
        requestDto.setGrt(BigDecimal.valueOf(1000));

        FdaHeaderDto responseDto = new FdaHeaderDto();
        responseDto.setTransactionPoid(1L);
        when(fdaService.updateFdaHeader(anyLong(), any(UpdateFdaHeaderRequest.class), anyLong(), anyLong(), anyString()))
                .thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/fdas/1")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L)
                        .header("X-User-Id", "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("FDA updated successfully"));
    }

    @Test
    void deleteFda_ShouldSoftDeleteFda() throws Exception {
        mockMvc.perform(delete("/api/v1/fdas/1")
                        .header("X-User-Id", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("FDA soft deleted successfully"));
    }

    @Test
    void getCharges_ShouldReturnCharges() throws Exception {
        PageResponse<FdaChargeDto> mockResponse = new PageResponse<>();
        when(fdaService.getCharges(anyLong(), anyLong(), anyLong(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/fdas/1/details")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Charges fetched successfully"));
    }

    @Test
    void saveCharges_ShouldSaveCharges() throws Exception {
        List<FdaChargeDto> charges = Arrays.asList(new FdaChargeDto());

        mockMvc.perform(post("/api/v1/fdas/1/details/bulk-save")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L)
                        .header("X-User-Id", "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(charges)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Charges saved successfully"));
    }

    @Test
    void deleteFdaDetail_ShouldDeleteCharge() throws Exception {
        mockMvc.perform(delete("/api/v1/fdas/1/details/1")
                        .header("X-User-Id", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("FDA detail deleted successfully"));
    }

    @Test
    void closeFda_WithSuccessMessage_ShouldReturnSuccess() throws Exception {
        when(fdaService.closeFda(anyLong(), anyLong(), anyLong(), anyLong()))
                .thenReturn("SUCCESS: FDA closed");

        mockMvc.perform(post("/api/v1/fdas/1/close")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L)
                        .header("X-User-Poid", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("FDA closed successfully"));
    }

    @Test
    void closeFda_WithErrorMessage_ShouldReturnError() throws Exception {
        when(fdaService.closeFda(anyLong(), anyLong(), anyLong(), anyLong()))
                .thenReturn("Error: Cannot close FDA");

        mockMvc.perform(post("/api/v1/fdas/1/close")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L)
                        .header("X-User-Poid", 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void reopenFda_WithSuccessMessage_ShouldReturnSuccess() throws Exception {
        FdaReOpenDto reopenDto = new FdaReOpenDto();
        when(fdaService.reopenFda(anyLong(), anyLong(), anyLong(), anyLong(), any(FdaReOpenDto.class)))
                .thenReturn("SUCESS: FDA reopened");

        mockMvc.perform(post("/api/v1/fdas/1/reopen")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L)
                        .header("X-User-Poid", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reopenDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void submitFda_ShouldSubmitFda() throws Exception {
        when(fdaService.submitFda(anyLong(), anyLong(), anyLong(), anyLong()))
                .thenReturn("FDA submitted successfully");

        mockMvc.perform(post("/api/v1/fdas/1/submit")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L)
                        .header("X-User-Poid", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void verifyFda_ShouldVerifyFda() throws Exception {
        when(fdaService.verifyFda(anyLong(), anyLong(), anyLong(), anyLong()))
                .thenReturn("FDA verified successfully");

        mockMvc.perform(post("/api/v1/fdas/1/verify")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L)
                        .header("X-User-Poid", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void returnFda_ShouldReturnFda() throws Exception {
        FdaReturnDto returnDto = new FdaReturnDto();
        when(fdaService.returnFda(anyLong(), anyLong(), anyLong(), anyLong(), anyString()))
                .thenReturn("FDA returned successfully");

        mockMvc.perform(post("/api/v1/fdas/1/return")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L)
                        .header("X-User-Poid", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correctionRemarks\":\"Need corrections\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void supplementaryFda_WithSuccessMessage_ShouldReturnSuccess() throws Exception {
        when(fdaService.supplementaryFda(anyLong(), anyLong(), anyLong(), anyLong()))
                .thenReturn("SUCCESS: Supplementary FDA created");

        mockMvc.perform(post("/api/v1/fdas/1/supplementary")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L)
                        .header("X-User-Poid", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Created FDA as supplementary successfully"));
    }

    @Test
    void getSupplementaryInfo_ShouldReturnSupplementaryInfo() throws Exception {
        List<FdaSupplementaryInfoDto> mockList = Arrays.asList();
        when(fdaService.getSupplementaryInfo(anyLong(), anyLong(), anyLong(), anyLong()))
                .thenReturn(mockList);

        mockMvc.perform(get("/api/v1/fdas/1/supplementary-info")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L)
                        .header("X-User-Poid", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Supplementary info fetched successfully"));
    }

    @Test
    void closeWithoutAmount_WithSuccessMessage_ShouldReturnSuccess() throws Exception {
        when(fdaService.closeFdaWithoutAmount(anyLong(), anyLong(), anyLong(), anyLong(), anyString()))
                .thenReturn("SUCESS: FDA closed without amount");

        mockMvc.perform(post("/api/v1/fdas/1/close-without-amount")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L)
                        .header("X-User-Poid", 1L)
                        .param("closedRemark", "Closed without amount"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Closed FDA without amount successfully"));
    }

    @Test
    void getPartyGl_ShouldReturnPartyGl() throws Exception {
        PartyGlResponse mockResponse = new PartyGlResponse();
        when(fdaService.getPartyGl(anyLong(), anyLong(), anyLong(), anyLong(), anyString()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/fdas/1/party-gl")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L)
                        .header("X-User-Poid", 1L)
                        .param("partyPoid", "1")
                        .param("partyType", "OWNER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Party General Ledger fetched successfully"));
    }

    @Test
    void createFromPda_WithSuccessMessage_ShouldReturnSuccess() throws Exception {
        when(fdaService.createFdaFromPda(anyLong(), anyLong(), anyLong(), anyLong()))
                .thenReturn("SUCCESS: FDA created from PDA");

        mockMvc.perform(post("/api/v1/fdas/from-pda/1")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L)
                        .header("X-User-Poid", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getPdaLogs_ShouldReturnLogs() throws Exception {
        List<PdaLogResponse> mockLogs = Arrays.asList();
        when(fdaService.getPdaLogs(anyLong(), anyLong(), anyLong()))
                .thenReturn(mockLogs);

        mockMvc.perform(get("/api/v1/fdas/1/logs/pda")
                        .header("X-Group-Poid", 1L)
                        .header("X-Company-Poid", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logs fetched successfully"));
    }
}