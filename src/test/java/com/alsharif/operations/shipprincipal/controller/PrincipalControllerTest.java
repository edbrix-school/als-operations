package com.alsharif.operations.shipprincipal.controller;

import com.alsharif.operations.shipprincipal.dto.*;
import com.alsharif.operations.shipprincipal.service.PrincipalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
    private PrincipalService principalService;
    
    private PrincipalDetailDTO mockPrincipalDetail;
    private PrincipalCreateDTO mockCreateDTO;
    
    @BeforeEach
    void setUp() {
        mockPrincipalDetail = new PrincipalDetailDTO();
        mockPrincipalDetail.setPrincipalPoid(1L);
        mockPrincipalDetail.setPrincipalCode("PRIN001");
        mockPrincipalDetail.setPrincipalName("Test Principal");
        mockPrincipalDetail.setActive("Y");
        
        mockCreateDTO = new PrincipalCreateDTO();
        mockCreateDTO.setGroupPoid(100L);
        mockCreateDTO.setPrincipalCode("PRIN001");
        mockCreateDTO.setPrincipalName("Test Principal");
        mockCreateDTO.setCompanyPoid(10L);
        mockCreateDTO.setActive(true);
    }
    
    @Test
    void testGetPrincipal_Success() throws Exception {
        when(principalService.getPrincipal(1L)).thenReturn(mockPrincipalDetail);
        
        mockMvc.perform(get("/principals/1")
                .header("X-Document-Id", "1")
                .header("X-User-Poid", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    void testGetPrincipal_NotFound() throws Exception {
        when(principalService.getPrincipal(999L)).thenThrow(new RuntimeException("Principal not found"));
        
        mockMvc.perform(get("/principals/999")
                .header("X-Document-Id", "1")
                .header("X-User-Poid", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
        
        verify(principalService).getPrincipal(999L);
    }
    
    @Test
    void testCreatePrincipal_Success() throws Exception {
        when(principalService.createPrincipal(any(PrincipalCreateDTO.class), eq(1L)))
                .thenReturn(mockPrincipalDetail);
        
        mockMvc.perform(post("/principals")
                .header("X-Document-Id", "1")
                .header("X-User-Poid", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockCreateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        verify(principalService).createPrincipal(any(PrincipalCreateDTO.class), eq(1L));
    }
    
    @Test
    void testUpdatePrincipal_Success() throws Exception {
        PrincipalUpdateDTO updateDTO = new PrincipalUpdateDTO();
        updateDTO.setPrincipalName("Updated Principal");
        
        when(principalService.updatePrincipal(eq(1L), any(PrincipalUpdateDTO.class)))
                .thenReturn(mockPrincipalDetail);
        
        mockMvc.perform(put("/principals/1")
                .header("X-Document-Id", "1")
                .header("X-User-Poid", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        verify(principalService).updatePrincipal(eq(1L), any(PrincipalUpdateDTO.class));
    }
    
    @Test
    void testToggleActive_Success() throws Exception {
        doNothing().when(principalService).toggleActive(1L);
        
        mockMvc.perform(patch("/principals/1/activate")
                .header("X-Document-Id", "1")
                .header("X-User-Poid", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        verify(principalService).toggleActive(1L);
    }
    
    @Test
    void testDeletePrincipal_Success() throws Exception {
        doNothing().when(principalService).deletePrincipal(1L);
        
        mockMvc.perform(delete("/principals/1")
                .header("X-Document-Id", "1")
                .header("X-User-Poid", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        verify(principalService).deletePrincipal(1L);
    }
    
    @Test
    void testCreatePrincipal_MissingHeaders() throws Exception {
        mockMvc.perform(post("/principals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockCreateDTO)))
                .andExpect(status().isBadRequest());
    }
}
