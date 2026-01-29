package com.asg.operations.salesquotationprojects.controller;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.salesquotationprojects.dto.SalesQuoteProjectsRequest;
import com.asg.operations.salesquotationprojects.dto.SalesQuoteProjectsResponse;
import com.asg.operations.salesquotationprojects.service.SalesQuoteProjectsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SalesQuoteProjectsControllerTest {

    private MockMvc mockMvc;
    private MockedStatic<com.asg.common.lib.security.util.UserContext> mockedUserContext;

    @Mock
    private SalesQuoteProjectsService salesQuoteProjectsService;

    @Mock
    private com.asg.common.lib.service.LoggingService loggingService;

    @InjectMocks
    private SalesQuoteProjectsController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockedUserContext = mockStatic(com.asg.common.lib.security.util.UserContext.class);
        mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getDocumentId).thenReturn("100");

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (mockedUserContext != null) {
            mockedUserContext.close();
        }
    }

    @Test
    void getSalesQuoteProjectsList_ok() throws Exception {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("content", List.of());
        mockResponse.put("totalElements", 0);

        when(salesQuoteProjectsService.listSalesQuoteProjectsWithFilters(
                eq("100"), any(), any(Pageable.class), any(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/v1/sales-quotation-projects/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sales Quote Projects list fetched successfully"));

        then(salesQuoteProjectsService).should()
                .listSalesQuoteProjectsWithFilters(eq("100"), any(), any(Pageable.class), any(), any());
    }

    @Test
    void getSalesQuoteProjectsList_withFilters() throws Exception {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("content", List.of());

        when(salesQuoteProjectsService.listSalesQuoteProjectsWithFilters(
                eq("100"), any(FilterRequestDto.class), any(Pageable.class),
                eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 12, 31))))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/v1/sales-quotation-projects/list")
                        .param("periodFrom", "2024-01-01")
                        .param("periodTo", "2024-12-31")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sales Quote Projects list fetched successfully"));
    }

    @Test
    void getSalesQuoteProjectById_ok() throws Exception {
        Long transactionPoid = 42L;
        SalesQuoteProjectsResponse response = new SalesQuoteProjectsResponse();
        response.setTransactionPoid(transactionPoid);
        response.setCustomerName("Test Customer");

        when(salesQuoteProjectsService.getSalesQuoteProjectById(transactionPoid))
                .thenReturn(response);

        mockMvc.perform(get("/v1/sales-quotation-projects/{transactionPoid}", transactionPoid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sales Quote Project retrieved successfully"))
                .andExpect(jsonPath("$.result.data.transactionPoid").value(transactionPoid.intValue()))
                .andExpect(jsonPath("$.result.data.customerName").value("Test Customer"));

        then(salesQuoteProjectsService).should().getSalesQuoteProjectById(transactionPoid);
        then(loggingService).should().createLogSummaryEntry(eq(com.asg.common.lib.enums.LogDetailsEnum.VIEWED), eq("100"), eq(transactionPoid.toString()));
    }

    @Test
    void createSalesQuoteProject_ok() throws Exception {
        Long createdPoid = 55L;
        SalesQuoteProjectsResponse response = new SalesQuoteProjectsResponse();
        response.setTransactionPoid(createdPoid);
        response.setCustomerName("New Customer");

        when(salesQuoteProjectsService.createSalesQuoteProject(any(SalesQuoteProjectsRequest.class)))
                .thenReturn(response);

        String requestJson = """
                {
                    "customerType": "CORPORATE",
                    "customerPoid": 1,
                    "customerName": "New Customer",
                    "principalPoid": 2,
                    "shipmentMode": "SEA",
                    "transportationMode": "CONTAINER",
                    "otherMode": "FCL",
                    "salesmanPoid": 3,
                    "commodity": ["Electronics"]
                }
                """;

        mockMvc.perform(post("/v1/sales-quotation-projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sales Quote Project created successfully"))
                .andExpect(jsonPath("$.result.data.transactionPoid").value(createdPoid.intValue()));

        then(salesQuoteProjectsService).should().createSalesQuoteProject(any(SalesQuoteProjectsRequest.class));
    }

    @Test
    void updateSalesQuoteProject_ok() throws Exception {
        Long transactionPoid = 77L;
        SalesQuoteProjectsResponse response = new SalesQuoteProjectsResponse();
        response.setTransactionPoid(transactionPoid);
        response.setCustomerName("Updated Customer");

        when(salesQuoteProjectsService.updateSalesQuoteProject(eq(transactionPoid), any(SalesQuoteProjectsRequest.class)))
                .thenReturn(response);

        String requestJson = """
                {
                    "customerType": "CORPORATE",
                    "customerPoid": 1,
                    "customerName": "Updated Customer",
                    "principalPoid": 2,
                    "shipmentMode": "SEA",
                    "transportationMode": "CONTAINER",
                    "otherMode": "FCL",
                    "salesmanPoid": 3,
                    "commodity": ["Electronics"]
                }
                """;

        mockMvc.perform(put("/v1/sales-quotation-projects/{transactionPoid}", transactionPoid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sales Quote Project updated successfully"))
                .andExpect(jsonPath("$.result.data.transactionPoid").value(transactionPoid.intValue()));

        then(salesQuoteProjectsService).should().updateSalesQuoteProject(eq(transactionPoid), any(SalesQuoteProjectsRequest.class));
    }

    @Test
    void deleteSalesQuoteProject_ok() throws Exception {
        Long transactionPoid = 10L;
        DeleteReasonDto deleteReasonDto = new DeleteReasonDto();
        deleteReasonDto.setDeleteReason("Test deletion");

        doNothing().when(salesQuoteProjectsService).deleteSalesQuoteProject(eq(transactionPoid), any(DeleteReasonDto.class));

        String requestJson = objectMapper.writeValueAsString(deleteReasonDto);

        mockMvc.perform(delete("/v1/sales-quotation-projects/{transactionPoid}", transactionPoid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sales Quote Project deleted successfully"));

        then(salesQuoteProjectsService).should().deleteSalesQuoteProject(eq(transactionPoid), any(DeleteReasonDto.class));
    }

    @Test
    void getCustomerAddress_ok() throws Exception {
        Long customerPoid = 123L;
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("address", "Test Address");
        mockResponse.put("city", "Test City");

        when(salesQuoteProjectsService.getCustomerAddress(customerPoid))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/v1/sales-quotation-projects/customer-address/{customerPoid}", customerPoid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer address retrieved successfully"))
                .andExpect(jsonPath("$.result.data.address").value("Test Address"));

        then(salesQuoteProjectsService).should().getCustomerAddress(customerPoid);
    }

    @Test
    void getTermsAndConditions_ok() throws Exception {
        Long termsPoid = 456L;
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("terms", "Test Terms");
        mockResponse.put("conditions", "Test Conditions");

        when(salesQuoteProjectsService.getTermsAndConditions(termsPoid))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/v1/sales-quotation-projects/terms-conditions/{termsPoid}", termsPoid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Terms and conditions retrieved successfully"))
                .andExpect(jsonPath("$.result.data.terms").value("Test Terms"));

        then(salesQuoteProjectsService).should().getTermsAndConditions(termsPoid);
    }

    @Test
    void getChargeTaxDetails_ok() throws Exception {
        Long chargePoid = 789L;
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("taxRate", "15.0");
        mockResponse.put("taxAmount", "150.00");

        when(salesQuoteProjectsService.getChargeTaxDetails(chargePoid))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/v1/sales-quotation-projects/charge-tax-details/{chargePoid}", chargePoid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Charge tax details retrieved successfully"))
                .andExpect(jsonPath("$.result.data.taxRate").value("15.0"));

        then(salesQuoteProjectsService).should().getChargeTaxDetails(chargePoid);
    }
}