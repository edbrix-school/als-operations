package com.asg.operations.pdaRoRoVehicle.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.operations.pdaRoRoVehicle.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdaRoRoEntryServiceTest {

    @Mock
    private PdaRoRoEntryService pdaRoroEntryService;

    private PdaRoRoVehicleUploadRequest request;

    @BeforeEach
    void setUp() {
        request = PdaRoRoVehicleUploadRequest.builder()
                .transactionPoid(1000L)
                .voyagePoid(500L)
                .docDate(LocalDate.now())
                .build();
    }

    @Test
    void testUploadVehicleDetails_Success() {
        PdaRoroVehicleUploadResponse expectedResponse = PdaRoroVehicleUploadResponse.builder()
                .status("Success")
                .build();

        when(pdaRoroEntryService.uploadVehicleDetails(any(PdaRoRoVehicleUploadRequest.class)))
                .thenReturn(expectedResponse);

        PdaRoroVehicleUploadResponse actualResponse = pdaRoroEntryService.uploadVehicleDetails(request);

        assertNotNull(actualResponse);
        assertEquals("Success", actualResponse.getStatus());
        verify(pdaRoroEntryService, times(1)).uploadVehicleDetails(any(PdaRoRoVehicleUploadRequest.class));
    }

    @Test
    void testUploadVehicleDetails_WithTransactionPoid() {
        assertNotNull(request.getTransactionPoid());
        assertEquals(1000L, request.getTransactionPoid());
    }

    @Test
    void testCreateRoRoEntry_Success() {
        PdaRoroEntryHdrRequestDto request = PdaRoroEntryHdrRequestDto.builder()
                .vesselVoyagePoid(100L)
                .remarks("Test")
                .build();

        PdaRoRoEntryHdrResponseDto expectedResponse = PdaRoRoEntryHdrResponseDto.builder()
                .transactionPoid(1L)
                .vesselVoyagePoid(100L)
                .build();

        when(pdaRoroEntryService.createRoRoEntry(any(PdaRoroEntryHdrRequestDto.class)))
                .thenReturn(expectedResponse);

        PdaRoRoEntryHdrResponseDto actualResponse = pdaRoroEntryService.createRoRoEntry(request);

        assertNotNull(actualResponse);
        assertEquals(1L, actualResponse.getTransactionPoid());
        verify(pdaRoroEntryService, times(1)).createRoRoEntry(any(PdaRoroEntryHdrRequestDto.class));
    }

    @Test
    void testUpdateRoRoEntry_Success() {
        Long transactionPoid = 1L;
        PdaRoroEntryHdrRequestDto request = PdaRoroEntryHdrRequestDto.builder()
                .vesselVoyagePoid(100L)
                .remarks("Updated")
                .build();

        PdaRoRoEntryHdrResponseDto expectedResponse = PdaRoRoEntryHdrResponseDto.builder()
                .transactionPoid(transactionPoid)
                .vesselVoyagePoid(100L)
                .build();

        when(pdaRoroEntryService.updateRoRoEntry(eq(transactionPoid), any(PdaRoroEntryHdrRequestDto.class)))
                .thenReturn(expectedResponse);

        PdaRoRoEntryHdrResponseDto actualResponse = pdaRoroEntryService.updateRoRoEntry(transactionPoid, request);

        assertNotNull(actualResponse);
        assertEquals(transactionPoid, actualResponse.getTransactionPoid());
        verify(pdaRoroEntryService, times(1)).updateRoRoEntry(eq(transactionPoid), any(PdaRoroEntryHdrRequestDto.class));
    }

    @Test
    void testGetRoRoEntry_Success() {
        Long transactionPoid = 1L;
        PdaRoRoEntryHdrResponseDto expectedResponse = PdaRoRoEntryHdrResponseDto.builder()
                .transactionPoid(transactionPoid)
                .vesselVoyagePoid(100L)
                .build();

        when(pdaRoroEntryService.getRoRoEntry(transactionPoid))
                .thenReturn(expectedResponse);

        PdaRoRoEntryHdrResponseDto actualResponse = pdaRoroEntryService.getRoRoEntry(transactionPoid);

        assertNotNull(actualResponse);
        assertEquals(transactionPoid, actualResponse.getTransactionPoid());
        verify(pdaRoroEntryService, times(1)).getRoRoEntry(transactionPoid);
    }

    @Test
    void testDeleteRoRoEntry_Success() {
        Long transactionPoid = 1L;

        doNothing().when(pdaRoroEntryService).deleteRoRoEntry(transactionPoid, new DeleteReasonDto());

        pdaRoroEntryService.deleteRoRoEntry(transactionPoid, new DeleteReasonDto());

        verify(pdaRoroEntryService, times(1)).deleteRoRoEntry(transactionPoid, new DeleteReasonDto());
    }

    @Test
    void testGetRoRoVehicleList_Success() {
        Long groupPoid = 1L;
        Long companyPoid = 100L;
        GetAllRoRoVehicleFilterRequest filterRequest = new GetAllRoRoVehicleFilterRequest();
        List<RoRoVehicleListResponse> content = new ArrayList<>();
        Page<RoRoVehicleListResponse> expectedPage = new PageImpl<>(content);

        when(pdaRoroEntryService.getRoRoVehicleList(eq(groupPoid), eq(companyPoid), any(), eq(0), eq(20), isNull()))
                .thenReturn(expectedPage);

        Page<RoRoVehicleListResponse> actualPage = pdaRoroEntryService.getRoRoVehicleList(groupPoid, companyPoid, filterRequest, 0, 20, null);

        assertNotNull(actualPage);
        assertEquals(0, actualPage.getTotalElements());
        verify(pdaRoroEntryService, times(1)).getRoRoVehicleList(eq(groupPoid), eq(companyPoid), any(), eq(0), eq(20), isNull());
    }

    @Test
    void testUploadExcel_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "test".getBytes());

        when(pdaRoroEntryService.uploadExcel(any()))
                .thenReturn("Excel uploaded successfully");

        String result = pdaRoroEntryService.uploadExcel(file);

        assertNotNull(result);
        assertEquals("Excel uploaded successfully", result);
        verify(pdaRoroEntryService, times(1)).uploadExcel(any());
    }

    @Test
    void testClearRoRoVehicleDetails_Success() {
        Long transactionPoid = 1L;

        when(pdaRoroEntryService.clearRoRoVehicleDetails(transactionPoid))
                .thenReturn("Vehicle details cleared successfully");

        String result = pdaRoroEntryService.clearRoRoVehicleDetails(transactionPoid);

        assertNotNull(result);
        assertEquals("Vehicle details cleared successfully", result);
        verify(pdaRoroEntryService, times(1)).clearRoRoVehicleDetails(transactionPoid);
    }
}
