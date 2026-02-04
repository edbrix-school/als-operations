package com.asg.operations.portcalloperation.controller;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.operations.portcalloperation.dto.*;
import com.asg.operations.portcalloperation.service.PortCallOperationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PortCallOperationControllerTest {

    @Mock
    private PortCallOperationService service;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private PortCallOperationController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getOperationById_Success() throws Exception {
        PortCallOperationResponseDto dto = PortCallOperationResponseDto.builder()
                .transactionPoid(1L)
                .build();
        when(service.getOperationById(1L)).thenReturn(dto);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getDocumentId).thenReturn("DOC123");

            mockMvc.perform(get("/v1/port-call-operations/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Operation retrieved successfully"));
            
            verify(service).getOperationById(1L);
            verify(loggingService).createLogSummaryEntry(eq(LogDetailsEnum.VIEWED), eq("DOC123"), eq("1"));
        }
    }

    @Test
    void getOperationById_NotFound() throws Exception {
        when(service.getOperationById(1L)).thenReturn(null);

        mockMvc.perform(get("/v1/port-call-operations/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Operation not found"));
    }

    @Test
    void createOperation_Success() throws Exception {
        PortCallOperationMailDetailDto mailDetail = PortCallOperationMailDetailDto.builder()
                .detRowId(1L)
                .communicationType("EMAIL")
                .build();
        PortCallOperationCreateDto createDto = PortCallOperationCreateDto.builder()
                .vesselVoyagePoid(1L)
                .callType("CALL")
                .principalPoid(1L)
                .portOfCallPoid(1L)
                .mailDetails(Arrays.asList(mailDetail))
                .build();
        PortCallOperationResponseDto responseDto = PortCallOperationResponseDto.builder()
                .transactionPoid(1L)
                .build();

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserPoid).thenReturn(1L);
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(1L);

            when(service.createOperation(any(), eq(1L), eq(1L))).thenReturn(responseDto);

            mockMvc.perform(post("/v1/port-call-operations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Operation created successfully"));
            
            verify(service).createOperation(any(), eq(1L), eq(1L));
        }
    }

    @Test
    void deleteOperation_Success() throws Exception {
        DeleteReasonDto deleteReasonDto = new DeleteReasonDto();
        
        doNothing().when(service).deleteOperation(eq(1L), any(DeleteReasonDto.class));

        mockMvc.perform(delete("/v1/port-call-operations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteReasonDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Operation deleted successfully"));
        
        verify(service).deleteOperation(eq(1L), any(DeleteReasonDto.class));
    }

    @Test
    void loadPda_Success() throws Exception {
        Map<String, Object> result = new HashMap<>();

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(1L);
            mockedUserContext.when(UserContext::getCompanyPoid).thenReturn(100L);
            mockedUserContext.when(UserContext::getUserPoid).thenReturn(1L);

            when(service.loadPda(eq("123"), eq(1L), eq(100L), eq(1L))).thenReturn(result);

            mockMvc.perform(get("/v1/port-call-operations/load-pda/123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("PDA data loaded successfully"));
        }
    }

    @Test
    void updateOperation_Success() throws Exception {
        PortCallOperationMailDetailDto mailDetail = PortCallOperationMailDetailDto.builder()
                .detRowId(1L)
                .communicationType("EMAIL")
                .build();
        PortCallOperationDto dto = PortCallOperationDto.builder()
                .vesselVoyagePoid(1L)
                .callType("CALL")
                .principalPoid(1L)
                .mailDetails(Arrays.asList(mailDetail))
                .build();
        PortCallOperationResponseDto responseDto = PortCallOperationResponseDto.builder().transactionPoid(1L).build();

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getUserPoid).thenReturn(1L);
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(1L);

            when(service.updateOperation(eq(1L), any(), eq(1L), eq(1L))).thenReturn(responseDto);

            mockMvc.perform(put("/v1/port-call-operations/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Operation updated successfully"));
        }
    }

    @Test
    void loadFda_Success() throws Exception {
        Map<String, Object> result = new HashMap<>();

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(1L);
            mockedUserContext.when(UserContext::getCompanyPoid).thenReturn(100L);
            mockedUserContext.when(UserContext::getUserPoid).thenReturn(1L);

            when(service.loadFda(eq("123"), eq(1L), eq(100L), eq(1L))).thenReturn(result);

            mockMvc.perform(get("/v1/port-call-operations/load-fda/123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("FDA data loaded successfully"));
        }
    }

    @Test
    void loadVoyage_Success() throws Exception {
        Map<String, Object> result = new HashMap<>();

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(1L);
            mockedUserContext.when(UserContext::getCompanyPoid).thenReturn(100L);
            mockedUserContext.when(UserContext::getUserPoid).thenReturn(1L);

            when(service.loadVoyage(eq(123L), eq(1L), eq(100L), eq(1L))).thenReturn(result);

            mockMvc.perform(get("/v1/port-call-operations/load-voyage/123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Voyage data loaded successfully"));
        }
    }

    @Test
    void getEstBertDetail_Success() throws Exception {
        PortCallOperationEstBertDetailResponseDto dto = PortCallOperationEstBertDetailResponseDto.builder().build();
        when(service.getEstBertDetail(1L, 1L)).thenReturn(dto);

        mockMvc.perform(get("/v1/port-call-operations/1/est-bert-details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("EstBertDetail retrieved successfully"));
    }

    @Test
    void createEstBertDetail_Success() throws Exception {
        PortCallOperationEstBertDetailRequestDto dto = PortCallOperationEstBertDetailRequestDto.builder()
                .eta(java.time.LocalDateTime.now())
                .etb(java.time.LocalDateTime.now().plusHours(2))
                .sendEmail(false)
                .build();
        PortCallOperationResponseDto responseDto = PortCallOperationResponseDto.builder().build();
        when(service.createEstBertDetail(eq(1L), any())).thenReturn(responseDto);

        mockMvc.perform(post("/v1/port-call-operations/1/est-bert-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("EstBertDetail created successfully"));
    }

    @Test
    void updateEstBertDetail_Success() throws Exception {
        PortCallOperationEstBertDetailRequestDto dto = PortCallOperationEstBertDetailRequestDto.builder()
                .eta(java.time.LocalDateTime.now())
                .etb(java.time.LocalDateTime.now().plusHours(2))
                .sendEmail(false)
                .build();
        PortCallOperationResponseDto responseDto = PortCallOperationResponseDto.builder().build();
        when(service.updateEstBertDetail(eq(1L), eq(1L), any())).thenReturn(responseDto);

        mockMvc.perform(put("/v1/port-call-operations/1/est-bert-details/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("EstBertDetail updated successfully"));
    }

    @Test
    void createEstPrearrivalActDetail_Success() throws Exception {
        PortCallOperationEstPrearrivalActDetailDto dto = PortCallOperationEstPrearrivalActDetailDto.builder()
                .sendEmail(false)
                .build();
        PortCallOperationEstPrearrivalActDetailResponseDto responseDto = PortCallOperationEstPrearrivalActDetailResponseDto.builder().build();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getDocumentId).thenReturn("DOC123");
            
            when(service.createEstPrearrivalActDetail(eq(1L), eq(1L), any())).thenReturn(responseDto);

            mockMvc.perform(post("/v1/port-call-operations/1/est-prearrival-details/1/activities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("EstPrearrivalActDetail created successfully"));
            
            verify(loggingService).createLogSummaryEntry(eq(LogDetailsEnum.CREATED), eq("DOC123"), eq("1"));
        }
    }

    @Test
    void updateEstPrearrivalActDetail_Success() throws Exception {
        PortCallOperationEstPrearrivalActDetailDto dto = PortCallOperationEstPrearrivalActDetailDto.builder()
                .sendEmail(false)
                .build();
        PortCallOperationEstPrearrivalActDetailResponseDto responseDto = PortCallOperationEstPrearrivalActDetailResponseDto.builder().build();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getDocumentId).thenReturn("DOC123");
            
            when(service.updateEstPrearrivalActDetail(eq(1L), eq(1L), eq(1L), any())).thenReturn(responseDto);

            mockMvc.perform(put("/v1/port-call-operations/1/est-prearrival-details/1/activities/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("EstPrearrivalActDetail updated successfully"));
            
            verify(loggingService).createLogSummaryEntry(eq(LogDetailsEnum.MODIFIED), eq("DOC123"), eq("1"));
        }
    }

    @Test
    void createActTimingsActvtyDetail_Success() throws Exception {
        PortCallOperationActTimingsActivityDetailDto dto = PortCallOperationActTimingsActivityDetailDto.builder()
                .sendEmail(false)
                .build();
        PortCallOperationActTimingsActvtyDetailResponseDto responseDto = PortCallOperationActTimingsActvtyDetailResponseDto.builder().build();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getDocumentId).thenReturn("DOC123");
            
            when(service.createActTimingsActvtyDetail(eq(1L), eq(1L), any())).thenReturn(responseDto);

            mockMvc.perform(post("/v1/port-call-operations/1/act-timing-details/1/activities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("ActTimingsActvtyDetail created successfully"));
            
            verify(loggingService).createLogSummaryEntry(eq(LogDetailsEnum.CREATED), eq("DOC123"), eq("1"));
        }
    }

    @Test
    void updateActTimingsActvtyDetail_Success() throws Exception {
        PortCallOperationActTimingsActivityDetailDto dto = PortCallOperationActTimingsActivityDetailDto.builder()
                .sendEmail(false)
                .build();
        PortCallOperationActTimingsActvtyDetailResponseDto responseDto = PortCallOperationActTimingsActvtyDetailResponseDto.builder().build();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getDocumentId).thenReturn("DOC123");
            
            when(service.updateActTimingsActvtyDetail(eq(1L), eq(1L), eq(1L), any())).thenReturn(responseDto);

            mockMvc.perform(put("/v1/port-call-operations/1/act-timing-details/1/activities/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("ActTimingsActvtyDetail updated successfully"));
            
            verify(loggingService).createLogSummaryEntry(eq(LogDetailsEnum.MODIFIED), eq("DOC123"), eq("1"));
        }
    }

    @Test
    void getDocsCopyDetail_Success() throws Exception {
        PortCallOperationDocsCopyDetailResponseDto dto = PortCallOperationDocsCopyDetailResponseDto.builder().build();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getDocumentId).thenReturn("DOC123");
            
            when(service.getDocsCopyDetail(1L, 1L)).thenReturn(dto);

            mockMvc.perform(get("/v1/port-call-operations/1/docs-copy-details/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("DocsCopyDetail retrieved successfully"));
            
            verify(loggingService).createLogSummaryEntry(eq(LogDetailsEnum.VIEWED), eq("DOC123"), eq("1"));
        }
    }

    @Test
    void createDocsCopyDetail_Success() throws Exception {
        PortCallOperationDocsCopyDetailRequestDto dto = PortCallOperationDocsCopyDetailRequestDto.builder()
                .documentAttachments("test-attachment.pdf")
                .sendEmail(false)
                .build();
        PortCallOperationResponseDto responseDto = PortCallOperationResponseDto.builder().build();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getDocumentId).thenReturn("DOC123");
            
            when(service.createDocsCopyDetail(eq(1L), any())).thenReturn(responseDto);

            mockMvc.perform(post("/v1/port-call-operations/1/docs-copy-details")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("DocsCopyDetail created successfully"));
            
            verify(loggingService).createLogSummaryEntry(eq(LogDetailsEnum.CREATED), eq("DOC123"), eq("1"));
        }
    }

    @Test
    void updateDocsCopyDetail_Success() throws Exception {
        PortCallOperationDocsCopyDetailRequestDto dto = PortCallOperationDocsCopyDetailRequestDto.builder()
                .documentAttachments("updated-attachment.pdf")
                .sendEmail(false)
                .build();
        PortCallOperationResponseDto responseDto = PortCallOperationResponseDto.builder().build();
        
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getDocumentId).thenReturn("DOC123");
            
            when(service.updateDocsCopyDetail(eq(1L), eq(1L), any())).thenReturn(responseDto);

            mockMvc.perform(put("/v1/port-call-operations/1/docs-copy-details/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("DocsCopyDetail updated successfully"));
            
            verify(loggingService).createLogSummaryEntry(eq(LogDetailsEnum.MODIFIED), eq("DOC123"), eq("1"));
        }
    }
}