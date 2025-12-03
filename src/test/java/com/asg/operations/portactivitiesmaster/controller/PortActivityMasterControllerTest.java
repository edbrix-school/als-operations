package com.asg.operations.portactivitiesmaster.controller;

import com.asg.operations.portactivitiesmaster.dto.PageResponse;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterRequest;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterResponse;
import com.asg.operations.portactivitiesmaster.service.PortActivityMasterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PortActivityMasterController.class)
class PortActivityMasterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortActivityMasterService portActivityService;

    @Autowired
    private ObjectMapper objectMapper;

    private PortActivityMasterRequest request;
    private PortActivityMasterResponse response;
    private PageResponse<PortActivityMasterResponse> pageResponse;

    @BeforeEach
    void setUp() {
        request = PortActivityMasterRequest.builder()
                .portActivityTypeName("Test Activity")
                .portActivityTypeName2("Test Activity 2")
                .active("Y")
                .seqno(1L)
                .remarks("Test remarks")
                .build();

        response = PortActivityMasterResponse.builder()
                .portActivityTypePoid(1L)
                .groupPoid(1L)
                .portActivityTypeCode("PA1")
                .portActivityTypeName("Test Activity")
                .portActivityTypeName2("Test Activity 2")
                .active("Y")
                .seqno(1L)
                .createdBy("testUser")
                .createdDate(LocalDateTime.now())
                .deleted("N")
                .remarks("Test remarks")
                .build();

        pageResponse = PageResponse.<PortActivityMasterResponse>builder()
                .content(List.of(response))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();
    }

    @Test
    void getPortActivityList_ShouldReturnSuccess() throws Exception {
        // Given
        when(portActivityService.getPortActivityList(any(), any(), any(), eq(1L), any(Pageable.class)))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/port-activities")
                        .header("X-Group-Poid", "1")
                        .param("code", "PA1")
                        .param("name", "Test")
                        .param("active", "Y"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Port activity list retrieved successfully"))
                .andExpect(jsonPath("$.result.data.content[0].portActivityTypeCode").value("PA1"));

        verify(portActivityService).getPortActivityList(eq("PA1"), eq("Test"), eq("Y"), eq(1L), any(Pageable.class));
    }

    @Test
    void getPortActivityById_ShouldReturnSuccess() throws Exception {
        // Given
        when(portActivityService.getPortActivityById(1L, 1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/port-activities/1")
                        .header("X-Group-Poid", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Port activity retrieved successfully"))
                .andExpect(jsonPath("$.result.data.portActivityTypePoid").value(1))
                .andExpect(jsonPath("$.result.data.portActivityTypeCode").value("PA1"));

        verify(portActivityService).getPortActivityById(1L, 1L);
    }

    @Test
    void createPortActivity_ShouldReturnSuccess() throws Exception {
        // Given
        when(portActivityService.createPortActivity(any(PortActivityMasterRequest.class), eq(1L), eq("testUser")))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/port-activities")
                        .header("X-Group-Poid", "1")
                        .header("X-User-Id", "testUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Port activity created successfully"))
                .andExpect(jsonPath("$.result.data.portActivityTypeCode").value("PA1"));

        verify(portActivityService).createPortActivity(any(PortActivityMasterRequest.class), eq(1L), eq("testUser"));
    }

    @Test
    void createPortActivity_ShouldReturnBadRequest_WhenInvalidInput() throws Exception {
        // Given
        PortActivityMasterRequest invalidRequest = PortActivityMasterRequest.builder().build();

        // When & Then
        mockMvc.perform(post("/api/v1/port-activities")
                        .header("X-Group-Poid", "1")
                        .header("X-User-Id", "testUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(portActivityService, never()).createPortActivity(any(), any(), any());
    }

    @Test
    void updatePortActivity_ShouldReturnSuccess() throws Exception {
        // Given
        when(portActivityService.updatePortActivity(eq(1L), any(PortActivityMasterRequest.class), eq(1L), eq("testUser")))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/port-activities/1")
                        .header("X-Group-Poid", "1")
                        .header("X-User-Id", "testUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Port activity updated successfully"))
                .andExpect(jsonPath("$.result.data.portActivityTypeCode").value("PA1"));

        verify(portActivityService).updatePortActivity(eq(1L), any(PortActivityMasterRequest.class), eq(1L), eq("testUser"));
    }

    @Test
    void deletePortActivity_SoftDelete_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(portActivityService).deletePortActivity(1L, 1L, "testUser", false);

        // When & Then
        mockMvc.perform(delete("/api/v1/port-activities/1")
                        .header("X-Group-Poid", "1")
                        .header("X-User-Id", "testUser")
                        .param("hardDelete", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Port activity deleted successfully"));

        verify(portActivityService).deletePortActivity(1L, 1L, "testUser", false);
    }

    @Test
    void deletePortActivity_HardDelete_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(portActivityService).deletePortActivity(1L, 1L, "testUser", true);

        // When & Then
        mockMvc.perform(delete("/api/v1/port-activities/1")
                        .header("X-Group-Poid", "1")
                        .header("X-User-Id", "testUser")
                        .param("hardDelete", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Port activity deleted successfully"));

        verify(portActivityService).deletePortActivity(1L, 1L, "testUser", true);
    }
}