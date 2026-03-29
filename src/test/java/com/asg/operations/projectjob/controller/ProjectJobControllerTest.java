package com.asg.operations.projectjob.controller;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.operations.projectjob.dto.ProjectJobRequest;
import com.asg.operations.projectjob.dto.ProjectJobResponse;
import com.asg.operations.projectjob.dto.ProjectLoadInJobsProcResponse;
import com.asg.operations.projectjob.service.ProjectJobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectJobControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectJobService projectJobService;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private ProjectJobController projectJobController;

    private ObjectMapper objectMapper;
    private MockedStatic<UserContext> userContextMockedStatic;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(projectJobController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(converter)
                .build();

        userContextMockedStatic = mockStatic(UserContext.class);
        userContextMockedStatic.when(UserContext::getDocumentId).thenReturn("DOC123");
        userContextMockedStatic.when(UserContext::getGroupPoid).thenReturn(1L);
        userContextMockedStatic.when(UserContext::getCompanyPoid).thenReturn(2L);
        userContextMockedStatic.when(UserContext::getUserPoid).thenReturn(3L);
    }

    @AfterEach
    void tearDown() throws Exception {
        userContextMockedStatic.close();
        closeable.close();
    }

    @Test
    void testGetProjectJobById() throws Exception {
        ProjectJobResponse response = new ProjectJobResponse();
        when(projectJobService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/v1/project-job/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project job retrieved successfully"));

        verify(loggingService).createLogSummaryEntry(eq(LogDetailsEnum.VIEWED), anyString(), anyString());
    }

    @Test
    void testCreateProjectJob() throws Exception {
        ProjectJobRequest request = new ProjectJobRequest();
        ProjectJobResponse response = new ProjectJobResponse();
        when(projectJobService.create(any(ProjectJobRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/project-job")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("project Job created successfully"));
    }

    @Test
    void testUpdateProjectJob() throws Exception {
        ProjectJobRequest request = new ProjectJobRequest();
        ProjectJobResponse response = new ProjectJobResponse();
        when(projectJobService.update(eq(1L), any(ProjectJobRequest.class))).thenReturn(response);

        mockMvc.perform(put("/v1/project-job/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project Job updated successfully"));
    }

    @Test
    void testReopenJobById() throws Exception {
        when(projectJobService.reopenJob(1L)).thenReturn("Success");

        mockMvc.perform(post("/v1/project-job/reopen-job/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void testLoadJobById() throws Exception {
        ProjectLoadInJobsProcResponse response = new ProjectLoadInJobsProcResponse();
        when(projectJobService.loadJobs(1L)).thenReturn(response);

        mockMvc.perform(get("/v1/project-job/load-job/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project loaded successfully"));
    }

    @Test
    void testDeleteProjectJob() throws Exception {
        DeleteReasonDto deleteReasonDto = new DeleteReasonDto();
        doNothing().when(projectJobService).deleteById(anyLong(), anyLong(), anyLong(), anyLong(), any(DeleteReasonDto.class));

        mockMvc.perform(delete("/v1/project-job/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteReasonDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project Job deleted successfully"));
    }

    @Test
    void testGetProjectJobList() throws Exception {
        FilterRequestDto filterRequest = new FilterRequestDto(null, null, new ArrayList<>());
        Map<String, Object> pageResponse = new HashMap<>();
        when(projectJobService.getAllProjectJobsWithFilters(anyString(), any(), any(Pageable.class), any(), any()))
                .thenReturn(pageResponse);

        mockMvc.perform(post("/v1/project-job/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterRequest))
                        .param("periodFrom", "2023-01-01")
                        .param("periodTo", "2023-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project Jobs retrieved successfully"));
    }

    @Test
    void testGetProjectJobList_NoParams() throws Exception {
        Map<String, Object> pageResponse = new HashMap<>();
        when(projectJobService.getAllProjectJobsWithFilters(anyString(), any(), any(Pageable.class), any(), any()))
                .thenReturn(pageResponse);

        mockMvc.perform(post("/v1/project-job/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project Jobs retrieved successfully"));
    }
}