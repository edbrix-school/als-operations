package com.asg.operations.shipprincipal.controller;

import com.asg.operations.shipprincipal.dto.*;
import com.asg.operations.shipprincipal.service.PrincipalMasterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrincipalController.class)
class PrincipalControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private PrincipalMasterService principalMasterService;
    
    private PrincipalMasterDto mockPrincipalDetail;
    private PrincipalCreateDTO mockCreateDTO;
    
    @BeforeEach
    void setUp() {
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
    
    @Test
    void testGetPrincipal_Success() throws Exception {
        when(principalMasterService.getPrincipal(1L)).thenReturn(mockPrincipalDetail);
        
        mockMvc.perform(get("/principal-master/1")
                .header("X-Document-Id", "1")
                .header("X-Group-Poid", "1")
                .header("X-User-Poid", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testGetPrincipal_NotFound() throws Exception {
        when(principalMasterService.getPrincipal(999L)).thenThrow(new RuntimeException("Principal not found"));
        
        mockMvc.perform(get("/principal-master/999")
                .header("X-Document-Id", "1")
                .header("X-Group-Poid", "1")
                .header("X-User-Poid", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
        
        verify(principalMasterService).getPrincipal(999L);
    }
    
    @Test
    void testCreatePrincipal_Success() throws Exception {
        when(principalMasterService.createPrincipal(any(PrincipalCreateDTO.class), eq(1L), eq(1L)))
                .thenReturn(mockPrincipalDetail);
        
        mockMvc.perform(post("/principal-master")
                .header("X-Document-Id", "1")
                .header("X-Group-Poid", "1")
                .header("X-User-Poid", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockCreateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        verify(principalMasterService).createPrincipal(any(PrincipalCreateDTO.class), eq(1L), eq(1L));
    }
    
    @Test
    void testUpdatePrincipal_Success() throws Exception {
        PrincipalUpdateDTO updateDTO = new PrincipalUpdateDTO();
        updateDTO.setPrincipalName("Updated Principal");
        updateDTO.setCompanyPoid(20L);

        
        when(principalMasterService.updatePrincipal(eq(1L), any(PrincipalUpdateDTO.class), anyLong(), anyLong()))
                .thenReturn(mockPrincipalDetail);
        
        mockMvc.perform(put("/principal-master/1")
                .header("X-Document-Id", "1")
                .header("X-Group-Poid", "1")
                .header("X-User-Poid", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        verify(principalMasterService).updatePrincipal(eq(1L), any(PrincipalUpdateDTO.class), anyLong(), anyLong());
    }
    
    @Test
    void testToggleActive_Success() throws Exception {
        doNothing().when(principalMasterService).toggleActive(1L);
        
        mockMvc.perform(patch("/principal-master/1/activate")
                .header("X-Document-Id", "1")
                .header("X-Group-Poid", "1")
                .header("X-User-Poid", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        verify(principalMasterService).toggleActive(1L);
    }
    
    @Test
    void testDeletePrincipal_Success() throws Exception {
        doNothing().when(principalMasterService).deletePrincipal(1L);
        
        mockMvc.perform(delete("/principal-master/1")
                .header("X-Document-Id", "1")
                .header("X-User-Poid", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        verify(principalMasterService).deletePrincipal(1L);
    }
    
    @Test
    void testCreatePrincipal_MissingHeaders() throws Exception {
        mockMvc.perform(post("/principal-master")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockCreateDTO)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGetPrincipalList_Success() throws Exception {
        Page<PrincipalMasterListDto> mockPage = new PageImpl<>(Arrays.asList(new PrincipalMasterListDto()));
        when(principalMasterService.getPrincipalList(any(), any())).thenReturn(mockPage);
        
        mockMvc.perform(get("/principal-master/list")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk());
        
        verify(principalMasterService).getPrincipalList(any(), any());
    }
    
    @Test
    void testCreateLedger_Success() throws Exception {
        CreateLedgerResponseDto mockResponse = new CreateLedgerResponseDto();
        mockResponse.setSuccess(true);
        when(principalMasterService.createLedger(eq(1L), anyLong(), anyLong(), anyLong())).thenReturn(mockResponse);
        
        mockMvc.perform(post("/principal-master/1/create-ledger")
                .header("X-company-Poid", "1")
                .header("X-User-Poid", "1")
                .header("X-Group-Poid", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

    }
}
