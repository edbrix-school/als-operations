package com.asg.operations.crew.controller;

import com.asg.operations.crew.dto.ContractCrewRequest;
import com.asg.operations.crew.dto.ContractCrewResponse;
import com.asg.operations.crew.dto.GetAllCrewFilterRequest;
import com.asg.operations.crew.service.ContractCrewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import org.mockito.MockedStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContractCrewControllerTest {

    private MockMvc mockMvc;
    private MockedStatic<com.asg.common.lib.security.util.UserContext> mockedUserContext;

    @Mock
    private ContractCrewService crewService;

    @InjectMocks
    private ContractCrewController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockedUserContext = mockStatic(com.asg.common.lib.security.util.UserContext.class);
        mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getCompanyPoid).thenReturn(100L);
        mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getGroupPoid).thenReturn(200L);
        mockedUserContext.when(com.asg.common.lib.security.util.UserContext::getUserId).thenReturn("tester");
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (mockedUserContext != null) {
            mockedUserContext.close();
        }
    }

    @Test
    @DisplayName("POST /v1/contract-crew-masters/search returns paged list")
    void getCrewList_ok() throws Exception {
        Page<ContractCrewResponse> page = new PageImpl<>(List.of(new ContractCrewResponse()), 
                org.springframework.data.domain.PageRequest.of(0, 20), 1);
        when(crewService.getAllCrewWithFilters(anyLong(), anyLong(), any(GetAllCrewFilterRequest.class), anyInt(), anyInt(), anyString()))
                .thenReturn(page);

        String filterJson = "{\"isDeleted\":\"N\",\"operator\":\"AND\",\"filters\":[]}";

        mockMvc.perform(post("/v1/contract-crew-masters/search")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "crewName,asc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filterJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /v1/contract-crew-masters/{crewPoid} returns ApiResponse with data")
    void getCrewById_ok() throws Exception {
        long crewPoid = 42L;
        ContractCrewResponse res = new ContractCrewResponse();
        res.setCrewPoid(crewPoid);
        res.setCrewName("John Doe");
        when(crewService.getCrewById(crewPoid)).thenReturn(res);

        mockMvc.perform(get("/v1/contract-crew-masters/{crewPoid}", crewPoid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Crew retrieved successfully"))
                .andExpect(jsonPath("$.result.data.crewPoid").value((int) crewPoid))
                .andExpect(jsonPath("$.result.data.crewName").value("John Doe"));
    }

    @Test
    @DisplayName("POST /v1/contract-crew-masters creates crew and returns ApiResponse with data")
    void createCrew_ok() throws Exception {
        long companyPoid = 100L;
        long groupPoid = 200L;
        String userId = "tester";
        long createdPoid = 55L;

        ContractCrewResponse res = new ContractCrewResponse();
        res.setCrewPoid(createdPoid);
        res.setCrewName("New Crew");
        when(crewService.createCrew(any(ContractCrewRequest.class), eq(companyPoid), eq(groupPoid), eq(userId)))
                .thenReturn(res);

        String reqJson = "{\n" +
                "  \"crewName\": \"New Crew\",\n" +
                "  \"crewNationalityPoid\": 1,\n" +
                "  \"crewCompany\": \"ALS\",\n" +
                "  \"crewDesignation\": \"Seaman\",\n" +
                "  \"crewPassportNumber\": \"P123456\",\n" +
                "  \"crewPassportIssueDate\": \"2025-01-01\",\n" +
                "  \"crewPassportExpiryDate\": \"2027-01-01\"\n" +
                "}";

        mockMvc.perform(post("/v1/contract-crew-masters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Crew created successfully"))
                .andExpect(jsonPath("$.result.data.crewPoid").value((int) createdPoid))
                .andExpect(jsonPath("$.result.data.crewName").value("New Crew"));

        then(crewService).should().createCrew(any(ContractCrewRequest.class), eq(companyPoid), eq(groupPoid), eq(userId));
    }

    @Test
    @DisplayName("PUT /v1/contract-crew-masters/{crewPoid} updates and returns ApiResponse with data")
    void updateCrew_ok() throws Exception {
        long companyPoid = 100L;
        String userId = "tester";
        long crewPoid = 77L;

        ContractCrewResponse res = new ContractCrewResponse();
        res.setCrewPoid(crewPoid);
        res.setCrewName("Updated Crew");
        when(crewService.updateCrew(eq(companyPoid), eq(userId), eq(crewPoid), any(ContractCrewRequest.class)))
                .thenReturn(res);

        String reqJson = "{\n" +
                "  \"crewName\": \"Updated Crew\",\n" +
                "  \"crewNationalityPoid\": 1,\n" +
                "  \"crewCompany\": \"ALS\",\n" +
                "  \"crewDesignation\": \"Seaman\",\n" +
                "  \"crewPassportNumber\": \"P123456\",\n" +
                "  \"crewPassportIssueDate\": \"2025-01-01\",\n" +
                "  \"crewPassportExpiryDate\": \"2027-01-01\"\n" +
                "}";

        mockMvc.perform(put("/v1/contract-crew-masters/{crewPoid}", crewPoid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Crew updated successfully"))
                .andExpect(jsonPath("$.result.data.crewPoid").value((int) crewPoid))
                .andExpect(jsonPath("$.result.data.crewName").value("Updated Crew"));

        then(crewService).should().updateCrew(eq(companyPoid), eq(userId), eq(crewPoid), any(ContractCrewRequest.class));
    }

    @Test
    @DisplayName("DELETE /v1/contract-crew-masters/{crewPoid} soft-deletes and returns 200")
    void deleteCrew_ok() throws Exception {
        long companyPoid = 100L;
        long crewPoid = 10L;

        mockMvc.perform(delete("/v1/contract-crew-masters/{crewPoid}", crewPoid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Crew master deleted successfully"));

        then(crewService).should().deleteCrew(companyPoid, crewPoid);
    }
}
