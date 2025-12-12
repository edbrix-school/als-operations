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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fdaController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getFdaList_ShouldReturnPageResponse() throws Exception {
        org.springframework.data.domain.Page<FdaHeaderDto> page = 
            new org.springframework.data.domain.PageImpl<>(java.util.List.of(new FdaHeaderDto()));
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(1L);
            mockedUserContext.when(UserContext::getCompanyPoid).thenReturn(100L);
            
            when(fdaService.getAllFdaWithFilters(eq(1L), eq(100L), any(GetAllFdaFilterRequest.class), eq(0), eq(20), isNull()))
                    .thenReturn(page);

            String filterJson = "{\"isDeleted\":\"N\",\"operator\":\"AND\",\"filters\":[]}";

            mockMvc.perform(post("/v1/fdas/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(filterJson)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.data.totalElements").value(1));
        }
    }

    @Test
    void getFda_ShouldReturnFdaHeader() throws Exception {
        FdaHeaderDto mockDto = new FdaHeaderDto();
        mockDto.setTransactionPoid(1L);
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(1L);
            mockedUserContext.when(UserContext::getCompanyPoid).thenReturn(100L);
            
            when(fdaService.getFdaHeader(1L, 1L, 100L)).thenReturn(mockDto);

            mockMvc.perform(get("/v1/fdas/1"))
                    .andExpect(status().isOk())
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
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(1L);
            mockedUserContext.when(UserContext::getCompanyPoid).thenReturn(100L);
            mockedUserContext.when(UserContext::getUserId).thenReturn("user1");
            
            when(fdaService.createFdaHeader(any(FdaHeaderDto.class), eq(1L), eq(100L), eq("user1")))
                    .thenReturn(responseDto);

            mockMvc.perform(post("/v1/fdas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
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
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(1L);
            mockedUserContext.when(UserContext::getCompanyPoid).thenReturn(100L);
            mockedUserContext.when(UserContext::getUserId).thenReturn("user1");
            
            when(fdaService.updateFdaHeader(eq(1L), any(UpdateFdaHeaderRequest.class), eq(1L), eq(100L), eq("user1")))
                    .thenReturn(responseDto);

            mockMvc.perform(put("/v1/fdas/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("FDA updated successfully"));
        }
    }

    @Test
    void deleteFda_ShouldSoftDeleteFda() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("user1");
            
            mockMvc.perform(delete("/v1/fdas/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("FDA soft deleted successfully"));
        }
    }

    @Test
    void getCharges_ShouldReturnCharges() throws Exception {
        PageResponse<FdaChargeDto> mockResponse = new PageResponse<>();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(1L);
            mockedUserContext.when(UserContext::getCompanyPoid).thenReturn(100L);
            
            when(fdaService.getCharges(eq(1L), eq(1L), eq(100L), any()))
                    .thenReturn(mockResponse);

            mockMvc.perform(get("/v1/fdas/1/details")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Charges fetched successfully"));
        }
    }

    @Test
    void saveCharges_ShouldSaveCharges() throws Exception {
        List<FdaChargeDto> charges = Arrays.asList(new FdaChargeDto());

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserId).thenReturn("user1");
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(1L);
            mockedUserContext.when(UserContext::getCompanyPoid).thenReturn(100L);
            
            mockMvc.perform(post("/v1/fdas/1/details/bulk-save")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(charges)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Charges saved successfully"));
        }
    }
}