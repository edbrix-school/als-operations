package com.alsharif.operations.crew.controller;

import com.alsharif.operations.crew.dto.*;
import com.alsharif.operations.crew.service.ContractCrewService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContractCrewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ContractCrewService crewService;

    @InjectMocks
    private ContractCrewController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("DELETE /api/v1/contract-crew-masters/{crewPoid}/details/{detRowId} calls service with 3 args and returns 200")
    void deleteCrewDetail_ok() throws Exception {
        long companyPoid = 100L;
        long crewPoid = 10L;
        long detRowId = 5L;

        mockMvc.perform(delete("/api/v1/contract-crew-masters/{crewPoid}/details/{detRowId}", crewPoid, detRowId)
                        .header("companyPoid", companyPoid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Detail record deleted successfully"))
                .andExpect(jsonPath("$.crewPoid").value((int) crewPoid))
                .andExpect(jsonPath("$.detRowId").value((int) detRowId));

        then(crewService).should().deleteCrewDetail(companyPoid, crewPoid, detRowId);
    }

    @Test
    @DisplayName("GET /api/v1/contract-crew-masters returns paged list")
    void getCrewList_ok() throws Exception {
        PageResponse<ContractCrewResponse> page = new PageResponse<>(List.of(new ContractCrewResponse()), 0, 20, 1);
        when(crewService.getCrewList(any(), any(), any(), any(), any(), anyLong())).thenReturn(page);

        mockMvc.perform(get("/api/v1/contract-crew-masters")
                        .header("companyPoid", 100L)
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "crewName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/contract-crew-masters/{crewPoid}/details saves details and returns message + payload")
    void saveCrewDetails_ok() throws Exception {
        long companyPoid = 100L;
        String userId = "tester";
        long crewPoid = 10L;
        BulkSaveDetailsRequest req = new BulkSaveDetailsRequest();

        ContractCrewDtlResponse dtl = new ContractCrewDtlResponse();
        dtl.setCrewPoid(crewPoid);
        dtl.setDetRowId(1L);
        CrewDetailsResponse serviceResp = new CrewDetailsResponse();
        serviceResp.setCrewPoid(crewPoid);
        serviceResp.setDetails(List.of(dtl));

        when(crewService.saveCrewDetails(eq(companyPoid), eq(userId), eq(crewPoid), any(BulkSaveDetailsRequest.class)))
                .thenReturn(serviceResp);

        mockMvc.perform(post("/api/v1/contract-crew-masters/{crewPoid}/details", crewPoid)
                        .header("companyPoid", companyPoid)
                        .header("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Details saved successfully"))
                .andExpect(jsonPath("$.crewPoid").value((int) crewPoid))
                .andExpect(jsonPath("$.savedDetails[0].detRowId").value(1));
    }

    @Test
    @DisplayName("DELETE /api/v1/contract-crew-masters/{crewPoid} soft-deletes and returns 200")
    void deleteCrew_ok() throws Exception {
        long companyPoid = 100L;
        long crewPoid = 10L;

        mockMvc.perform(delete("/api/v1/contract-crew-masters/{crewPoid}", crewPoid)
                        .header("companyPoid", companyPoid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Crew master deleted successfully"))
                .andExpect(jsonPath("$.crewPoid").value((int) crewPoid));

        then(crewService).should().deleteCrew(companyPoid, crewPoid);
    }
}
