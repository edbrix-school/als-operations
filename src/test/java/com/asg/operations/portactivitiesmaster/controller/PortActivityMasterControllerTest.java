package com.asg.operations.portactivitiesmaster.controller;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.portactivitiesmaster.dto.GetAllPortActivityFilterRequest;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterRequest;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterResponse;
import com.asg.operations.portactivitiesmaster.dto.PortActivityListResponse;
import com.asg.operations.portactivitiesmaster.service.PortActivityMasterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PortActivityMasterControllerTest {

    private MockMvc mockMvc;
    private MockedStatic<UserContext> mockedUserContext;
    private ObjectMapper objectMapper;

    @Mock
    private PortActivityMasterService portActivityService;

    @Mock
    private com.asg.common.lib.service.LoggingService loggingService;

    @InjectMocks
    private PortActivityMasterController portActivityMasterController;

    private PortActivityMasterRequest request;
    private PortActivityMasterResponse response;

    @BeforeEach
    void setUp() {
        mockedUserContext = mockStatic(UserContext.class);
        mockedUserContext.when(UserContext::getGroupPoid).thenReturn(1L);
        mockedUserContext.when(UserContext::getUserId).thenReturn("testUser");

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(portActivityMasterController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

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
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (mockedUserContext != null) {
            mockedUserContext.close();
        }
    }

    @Test
    void getPortActivityList_ShouldReturnSuccess() throws Exception {
        GetAllPortActivityFilterRequest filterRequest = new GetAllPortActivityFilterRequest();
        filterRequest.setIsDeleted("N");
        filterRequest.setOperator("AND");
        filterRequest.setFilters(Collections.emptyList());

        Page<PortActivityListResponse> page = new PageImpl<>(List.of(createListResponse()));
        when(portActivityService.getAllPortActivitiesWithFilters(eq(1L), any(GetAllPortActivityFilterRequest.class), eq(0), eq(20), any()))
                .thenReturn(page);

        mockMvc.perform(post("/v1/port-activities/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterRequest))
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Port activity list retrieved successfully"));

        verify(portActivityService).getAllPortActivitiesWithFilters(eq(1L), any(GetAllPortActivityFilterRequest.class), eq(0), eq(20), any());
    }

    @Test
    void getPortActivityById_ShouldReturnSuccess() throws Exception {
        when(portActivityService.getPortActivityById(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/v1/port-activities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Port activity retrieved successfully"));

        verify(portActivityService).getPortActivityById(1L, 1L);
    }

    @Test
    void createPortActivity_ShouldReturnSuccess() throws Exception {
        when(portActivityService.createPortActivity(any(PortActivityMasterRequest.class), eq(1L), eq("testUser")))
                .thenReturn(response);

        mockMvc.perform(post("/v1/port-activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Port activity created successfully"));

        verify(portActivityService).createPortActivity(any(PortActivityMasterRequest.class), eq(1L), eq("testUser"));
    }

    @Test
    void updatePortActivity_ShouldReturnSuccess() throws Exception {
        when(portActivityService.updatePortActivity(eq(1L), any(PortActivityMasterRequest.class), eq(1L), eq("testUser")))
                .thenReturn(response);

        mockMvc.perform(put("/v1/port-activities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Port activity updated successfully"));

        verify(portActivityService).updatePortActivity(eq(1L), any(PortActivityMasterRequest.class), eq(1L), eq("testUser"));
    }

    @Test
    void deletePortActivity_SoftDelete_ShouldReturnSuccess() throws Exception {
        doNothing().when(portActivityService).deletePortActivity(eq(1L), eq(1L), eq("testUser"), eq(false), any());

        mockMvc.perform(delete("/v1/port-activities/1")
                .param("hardDelete", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Port activity deleted successfully"));

        verify(portActivityService).deletePortActivity(eq(1L), eq(1L), eq("testUser"), eq(false), any());
    }

    @Test
    void deletePortActivity_HardDelete_ShouldReturnSuccess() throws Exception {
        doNothing().when(portActivityService).deletePortActivity(eq(1L), eq(1L), eq("testUser"), eq(true), any());

        mockMvc.perform(delete("/v1/port-activities/1")
                .param("hardDelete", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Port activity deleted successfully"));

        verify(portActivityService).deletePortActivity(eq(1L), eq(1L), eq("testUser"), eq(true), any());
    }

    private PortActivityListResponse createListResponse() {
        PortActivityListResponse listResponse = new PortActivityListResponse();
        listResponse.setPortActivityTypePoid(1L);
        listResponse.setPortActivityTypeCode("PA1");
        listResponse.setPortActivityTypeName("Test Activity");
        listResponse.setActive("Y");
        return listResponse;
    }
}