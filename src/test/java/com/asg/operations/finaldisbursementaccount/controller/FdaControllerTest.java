package com.asg.operations.finaldisbursementaccount.controller;

import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.common.PageResponse;
import com.asg.operations.finaldisbursementaccount.dto.*;
import com.asg.operations.finaldisbursementaccount.service.FdaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FdaControllerTest {

    @Mock
    private FdaService fdaService;

    @InjectMocks
    private FdaController fdaController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Long groupPoid = 1L;
    private Long companyPoid = 100L;
    private String userId = "USER123";
    private Long userPoid = 123L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fdaController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    private void mockUserContext(MockedStatic<UserContext> mockedUserContext) {
        mockedUserContext.when(UserContext::getGroupPoid).thenReturn(groupPoid);
        mockedUserContext.when(UserContext::getCompanyPoid).thenReturn(companyPoid);
        mockedUserContext.when(UserContext::getUserId).thenReturn(userId);
        mockedUserContext.when(UserContext::getUserPoid).thenReturn(userPoid);
    }

    @Test
    void getFdaList_ShouldReturnPageResponse() throws Exception {
        PageResponse<FdaHeaderDto> mockResponse = new PageResponse<>();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.getFdaList(anyLong(), anyLong(), any(), any(), any(), any(), any()))
                    .thenReturn(mockResponse);

            mockMvc.perform(get("/v1/fdas")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("FDA list fetched successfully"));
        }
    }

    @Test
    void getFda_ShouldReturnFdaHeader() throws Exception {
        FdaHeaderDto mockDto = new FdaHeaderDto();
        mockDto.setTransactionPoid(1L);
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.getFdaHeader(1L, groupPoid, companyPoid)).thenReturn(mockDto);

            mockMvc.perform(get("/v1/fdas/1")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("FDA fetched successfully"));
        }
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
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.createFdaHeader(any(FdaHeaderDto.class), eq(groupPoid), eq(companyPoid), eq(userId)))
                    .thenReturn(responseDto);

            mockMvc.perform(post("/v1/fdas")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L)
                            .header("X-User-Id", "user1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("FDA created successfully"));
        }
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
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.updateFdaHeader(eq(1L), any(UpdateFdaHeaderRequest.class), eq(groupPoid), eq(companyPoid), eq(userId)))
                    .thenReturn(responseDto);

            mockMvc.perform(put("/v1/fdas/1")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L)
                            .header("X-User-Id", "user1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("FDA updated successfully"));
        }
    }

    @Test
    void deleteFda_ShouldSoftDeleteFda() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            mockMvc.perform(delete("/v1/fdas/1")
                            .header("X-User-Id", "user1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("FDA soft deleted successfully"));
        }
    }

    @Test
    void getCharges_ShouldReturnCharges() throws Exception {
        PageResponse<FdaChargeDto> mockResponse = new PageResponse<>();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.getCharges(eq(1L), eq(groupPoid), eq(companyPoid), any()))
                    .thenReturn(mockResponse);

            mockMvc.perform(get("/v1/fdas/1/details")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Charges fetched successfully"));
        }
    }

    @Test
    void saveCharges_ShouldSaveCharges() throws Exception {
        List<FdaChargeDto> charges = Arrays.asList(new FdaChargeDto());

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            mockMvc.perform(post("/v1/fdas/1/details/bulk-save")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L)
                            .header("X-User-Id", "user1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(charges)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Charges saved successfully"));
        }
    }

    @Test
    void deleteFdaDetail_ShouldDeleteCharge() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            mockMvc.perform(delete("/v1/fdas/1/details/1")
                            .header("X-User-Id", "user1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("FDA detail deleted successfully"));
        }
    }

    @Test
    void closeFda_WithSuccessMessage_ShouldReturnSuccess() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.closeFda(eq(groupPoid), eq(companyPoid), eq(userPoid), eq(1L)))
                    .thenReturn("SUCCESS: FDA closed");

            mockMvc.perform(post("/v1/fdas/1/close")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L)
                            .header("X-User-Poid", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("FDA closed successfully"));
        }
    }

    @Test
    void closeFda_WithErrorMessage_ShouldReturnError() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.closeFda(eq(groupPoid), eq(companyPoid), eq(userPoid), eq(1L)))
                    .thenReturn("Error: Cannot close FDA");

            mockMvc.perform(post("/v1/fdas/1/close")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L)
                            .header("X-User-Poid", 1L))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Test
    void reopenFda_WithSuccessMessage_ShouldReturnSuccess() throws Exception {
        FdaReOpenDto reopenDto = new FdaReOpenDto();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.reopenFda(eq(groupPoid), eq(companyPoid), eq(userPoid), eq(1L), any(FdaReOpenDto.class)))
                    .thenReturn("SUCCESS: FDA reopened");

            mockMvc.perform(post("/v1/fdas/1/reopen")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L)
                            .header("X-User-Poid", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reopenDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("FDA re-opened successfully"));
        }
    }

    @Test
    void submitFda_ShouldSubmitFda() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.submitFda(eq(groupPoid), eq(companyPoid), eq(userPoid), eq(1L)))
                    .thenReturn("SUCCESS: FDA submitted successfully");

            mockMvc.perform(post("/v1/fdas/1/submit")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L)
                            .header("X-User-Poid", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Test
    void verifyFda_ShouldVerifyFda() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.verifyFda(eq(groupPoid), eq(companyPoid), eq(userPoid), eq(1L)))
                    .thenReturn("SUCCESS: FDA verified successfully");

            mockMvc.perform(post("/v1/fdas/1/verify")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L)
                            .header("X-User-Poid", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Test
    void returnFda_ShouldReturnFda() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.returnFda(eq(groupPoid), eq(companyPoid), eq(userPoid), eq(1L), anyString()))
                    .thenReturn("SUCCESS: FDA returned successfully");

            mockMvc.perform(post("/v1/fdas/1/return")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L)
                            .header("X-User-Poid", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"correctionRemarks\":\"Need corrections\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Test
    void supplementaryFda_WithSuccessMessage_ShouldReturnSuccess() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.supplementaryFda(eq(groupPoid), eq(companyPoid), eq(userPoid), eq(1L)))
                    .thenReturn("SUCCESS: Supplementary FDA created");

            mockMvc.perform(post("/v1/fdas/1/supplementary")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L)
                            .header("X-User-Poid", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Created FDA as supplementary successfully"));
        }
    }

    @Test
    void getSupplementaryInfo_ShouldReturnSupplementaryInfo() throws Exception {
        List<FdaSupplementaryInfoDto> mockList = Arrays.asList();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.getSupplementaryInfo(eq(1L), eq(groupPoid), eq(companyPoid), eq(userPoid)))
                    .thenReturn(mockList);

            mockMvc.perform(get("/v1/fdas/1/supplementary-info")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L)
                            .header("X-User-Poid", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Supplementary info fetched successfully"));
        }
    }

    @Test
    void closeWithoutAmount_WithSuccessMessage_ShouldReturnSuccess() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.closeFdaWithoutAmount(eq(1L), eq(groupPoid), eq(companyPoid), eq(userPoid), anyString()))
                    .thenReturn("SUCESS: FDA closed without amount");

            mockMvc.perform(post("/v1/fdas/1/close-without-amount")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L)
                            .header("X-User-Poid", 1L)
                            .param("closedRemark", "Closed without amount"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Closed FDA without amount successfully"));
        }
    }

    @Test
    void getPartyGl_ShouldReturnPartyGl() throws Exception {
        PartyGlResponse mockResponse = new PartyGlResponse();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.getPartyGl(eq(groupPoid), eq(companyPoid), eq(userPoid), eq(1L), eq("OWNER")))
                    .thenReturn(mockResponse);

            mockMvc.perform(get("/v1/fdas/1/party-gl")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L)
                            .header("X-User-Poid", 1L)
                            .param("partyPoid", "1")
                            .param("partyType", "OWNER"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Party General Ledger fetched successfully"));
        }
    }

    @Test
    void createFromPda_WithSuccessMessage_ShouldReturnSuccess() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.createFdaFromPda(eq(groupPoid), eq(companyPoid), eq(userPoid), eq(1L)))
                    .thenReturn("SUCCESS: FDA created from PDA");

            mockMvc.perform(post("/v1/fdas/from-pda/1")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L)
                            .header("X-User-Poid", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Test
    void getPdaLogs_ShouldReturnLogs() throws Exception {
        List<PdaLogResponse> mockLogs = Arrays.asList();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockUserContext(mockedUserContext);
            
            when(fdaService.getPdaLogs(eq(1L), eq(groupPoid), eq(companyPoid)))
                    .thenReturn(mockLogs);

            mockMvc.perform(get("/v1/fdas/1/logs/pda")
                            .header("X-Group-Poid", 1L)
                            .header("X-Company-Poid", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Logs fetched successfully"));
        }
    }
}