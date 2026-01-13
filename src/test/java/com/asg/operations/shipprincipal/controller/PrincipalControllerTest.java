package com.asg.operations.shipprincipal.controller;

import com.asg.operations.shipprincipal.dto.*;
import com.asg.operations.shipprincipal.service.PrincipalMasterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PrincipalControllerTest {

    private MockMvc mockMvc;
    private MockedStatic<com.asg.common.lib.security.util.UserContext> mockedUserContext;
    private ObjectMapper objectMapper;

    @Mock
    private PrincipalMasterService principalMasterService;

    @InjectMocks
    private PrincipalController controller;

    private PrincipalMasterDto mockPrincipalDetail;
    private PrincipalCreateDTO mockCreateDTO;

    @BeforeEach
    void setUp() {
        mockedUserContext = mockStatic(com.asg.common.lib.security.util.UserContext.class);
        mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getCompanyPoid).thenReturn(100L);
        mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getGroupPoid).thenReturn(200L);
        mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getUserPoid).thenReturn(1L);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();

        mockPrincipalDetail = new PrincipalMasterDto();
        mockPrincipalDetail.setPrincipalPoid(1L);
        mockPrincipalDetail.setPrincipalCode("PRIN001");
        mockPrincipalDetail.setPrincipalName("Test Principal");
        mockPrincipalDetail.setActive("Y");

        mockCreateDTO = new PrincipalCreateDTO();
        mockCreateDTO.setPrincipalCode("PRIN001");
        mockCreateDTO.setPrincipalName("Test Principal");
        mockCreateDTO.setCompanyPoid(10L);
        mockCreateDTO.setActive("Y");
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (mockedUserContext != null) {
            mockedUserContext.close();
        }
    }

    @Test
    void testGetPrincipal_Success() throws Exception {
        when(principalMasterService.getPrincipal(1L)).thenReturn(mockPrincipalDetail);

        mockMvc.perform(get("/v1/principal-master/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetPrincipal_NotFound() throws Exception {
        when(principalMasterService.getPrincipal(999L)).thenThrow(new RuntimeException("Principal not found"));

        try {
            mockMvc.perform(get("/v1/principal-master/999"));
        } catch (Exception e) {
            // Expected ServletException due to unhandled RuntimeException
        }

        verify(principalMasterService).getPrincipal(999L);
    }

    @Test
    void testCreatePrincipal_Success() throws Exception {
        when(principalMasterService.createPrincipal(any(PrincipalCreateDTO.class), eq(200L), eq(1L)))
                .thenReturn(mockPrincipalDetail);

        mockMvc.perform(post("/v1/principal-master")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockCreateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(principalMasterService).createPrincipal(any(PrincipalCreateDTO.class), eq(200L), eq(1L));
    }

    @Test
    void testUpdatePrincipal_Success() throws Exception {
        PrincipalUpdateDTO updateDTO = new PrincipalUpdateDTO();
        updateDTO.setPrincipalName("Updated Principal");
        updateDTO.setCompanyPoid(20L);

        when(principalMasterService.updatePrincipal(eq(1L), any(PrincipalUpdateDTO.class), anyLong(), anyLong()))
                .thenReturn(mockPrincipalDetail);

        mockMvc.perform(put("/v1/principal-master/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(principalMasterService).updatePrincipal(eq(1L), any(PrincipalUpdateDTO.class), eq(200L), eq(1L));
    }

    @Test
    void testToggleActive_Success() throws Exception {
        doNothing().when(principalMasterService).toggleActive(1L);

        mockMvc.perform(patch("/v1/principal-master/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(principalMasterService).toggleActive(1L);
    }

    @Test
    void testDeletePrincipal_Success() throws Exception {
        doNothing().when(principalMasterService).deletePrincipal(1L, deleteReasonDto);

        mockMvc.perform(delete("/v1/principal-master/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(principalMasterService).deletePrincipal(1L, deleteReasonDto);
    }

    @Test
    void testGetPrincipalList_Success() throws Exception {
        GetAllPrincipalFilterRequest filterRequest = new GetAllPrincipalFilterRequest();
        filterRequest.setIsDeleted("N");
        filterRequest.setOperator("AND");
        filterRequest.setFilters(Collections.emptyList());

        when(principalMasterService.getAllPrincipalsWithFilters(eq(200L), any(GetAllPrincipalFilterRequest.class), eq(0), eq(20), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(post("/v1/principal-master/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterRequest))
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(principalMasterService).getAllPrincipalsWithFilters(eq(200L), any(GetAllPrincipalFilterRequest.class), eq(0), eq(20), any());
    }

    @Test
    void testCreateLedger_Success() throws Exception {
        CreateLedgerResponseDto mockResponse = new CreateLedgerResponseDto();
        mockResponse.setSuccess(true);
        when(principalMasterService.createLedger(eq(1L), eq(200L), eq(100L), eq(1L))).thenReturn(mockResponse);

        mockMvc.perform(post("/v1/principal-master/1/create-ledger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}