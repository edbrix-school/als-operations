package com.alsharif.operations.pdaporttariffmaster.service;

import com.alsharif.operations.pdaporttariffmaster.dto.*;
import com.alsharif.operations.pdaporttariffmaster.entity.*;
import com.alsharif.operations.pdaporttariffmaster.repository.*;
import com.alsharif.operations.pdaporttariffmaster.util.*;
import com.alsharif.operations.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdaPortTariffHdrServiceImplTest {

    @Mock
    private PdaPortTariffHdrRepository tariffHdrRepository;

    @Mock
    private PdaPortTariffChargeDtlRepository chargeDtlRepository;

    @Mock
    private PdaPortTariffSlabDtlRepository slabDtlRepository;

    @Mock
    private PdaPortTariffMapper mapper;

    @Mock
    private DateOverlapValidator overlapValidator;

    @Mock
    private PortTariffDocumentRefGenerator docRefGenerator;

    @Mock
    private EntityManager entityManager;

    @Mock
    private PdaRateTypeMasterRepository pdaRateTypeMasterRepository;

    @InjectMocks
    private PdaPortTariffHdrServiceImpl tariffService;

    @Test
    void getTariffList_Success() {
        // Given
        String portPoid = "1001";
        LocalDate periodFrom = LocalDate.now();
        LocalDate periodTo = LocalDate.now().plusMonths(6);
        String vesselTypePoid = "2001";
        Long groupPoid = 100L;
        Pageable pageable = PageRequest.of(0, 10);
        
        PdaPortTariffHdr tariff = createMockTariff();
        Page<PdaPortTariffHdr> tariffPage = new PageImpl<>(Arrays.asList(tariff), pageable, 1);
        PdaPortTariffMasterResponse response = createMockResponse();
        
        when(tariffHdrRepository.searchTariffs(
                BigDecimal.valueOf(groupPoid), portPoid, periodFrom, periodTo, vesselTypePoid, pageable))
                .thenReturn(tariffPage);
        when(mapper.toHeaderOnlyResponse(tariff)).thenReturn(response);
        
        // When
        PageResponse<PdaPortTariffMasterResponse> result = tariffService.getTariffList(
                portPoid, periodFrom, periodTo, vesselTypePoid, groupPoid, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(1L, result.getTotalElements());
        verify(mapper).toHeaderOnlyResponse(tariff);
    }

    @Test
    void getTariffById_Success() {
        // Given
        Long transactionPoid = 1L;
        Long groupPoid = 100L;
        PdaPortTariffHdr tariff = createMockTariff();
        PdaPortTariffMasterResponse expectedResponse = createMockResponse();

        when(tariffHdrRepository.findByTransactionPoidAndGroupPoid(
                transactionPoid, BigDecimal.valueOf(groupPoid)))
                .thenReturn(Optional.of(tariff));
        when(chargeDtlRepository.findByTransactionPoidOrderBySeqNoAscDetRowIdAsc(transactionPoid))
                .thenReturn(Arrays.asList());
        when(mapper.toResponseWithChargeDetails(eq(tariff), any()))
                .thenReturn(expectedResponse);

        // When
        PdaPortTariffMasterResponse result = tariffService.getTariffById(transactionPoid, groupPoid);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse.getTransactionPoid(), result.getTransactionPoid());
        verify(tariffHdrRepository).findByTransactionPoidAndGroupPoid(
                transactionPoid, BigDecimal.valueOf(groupPoid));
    }

    @Test
    void getTariffById_NotFound() {
        // Given
        Long transactionPoid = 1L;
        Long groupPoid = 100L;

        when(tariffHdrRepository.findByTransactionPoidAndGroupPoid(
                transactionPoid, BigDecimal.valueOf(groupPoid)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
                () -> tariffService.getTariffById(transactionPoid, groupPoid));
    }

    @Test
    void deleteTariff_SoftDelete() {
        // Given
        Long transactionPoid = 1L;
        Long groupPoid = 100L;
        PdaPortTariffHdr tariff = createMockTariff();

        when(tariffHdrRepository.findByTransactionPoidAndGroupPoidAndDeleted(
                transactionPoid, BigDecimal.valueOf(groupPoid), "N"))
                .thenReturn(Optional.of(tariff));

        // When
        tariffService.deleteTariff(transactionPoid, groupPoid, "user1", false);

        // Then
        verify(tariffHdrRepository).save(tariff);
        assertEquals("Y", tariff.getDeleted());
    }

    @Test
    void getChargeDetails_Success() {
        // Given
        Long transactionPoid = 1L;
        Long groupPoid = 100L;
        PdaPortTariffHdr tariff = createMockTariff();
        ChargeDetailsResponse expectedResponse = new ChargeDetailsResponse();

        when(tariffHdrRepository.findByTransactionPoidAndGroupPoidAndDeleted(
                transactionPoid, BigDecimal.valueOf(groupPoid), "N"))
                .thenReturn(Optional.of(tariff));
        when(chargeDtlRepository.findByTransactionPoidOrderBySeqNoAscDetRowIdAsc(transactionPoid))
                .thenReturn(Arrays.asList());
        when(mapper.toChargeDetailsResponse(any(), eq(transactionPoid)))
                .thenReturn(expectedResponse);

        // When
        ChargeDetailsResponse result = tariffService.getChargeDetails(transactionPoid, groupPoid, true);

        // Then
        assertNotNull(result);
        verify(mapper).toChargeDetailsResponse(any(), eq(transactionPoid));
    }

    @Test
    void getTariffList_EmptyResult() {
        // Given
        Long groupPoid = 100L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<PdaPortTariffHdr> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(tariffHdrRepository.searchTariffs(any(), any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);
        
        // When
        PageResponse<PdaPortTariffMasterResponse> result = tariffService.getTariffList(
                null, null, null, null, groupPoid, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(0L, result.getTotalElements());
    }

    @Test
    void getTariffList_NullFilters() {
        // Given
        Long groupPoid = 100L;
        Pageable pageable = PageRequest.of(0, 10);
        PdaPortTariffHdr tariff = createMockTariff();
        Page<PdaPortTariffHdr> tariffPage = new PageImpl<>(Arrays.asList(tariff), pageable, 1);
        PdaPortTariffMasterResponse response = createMockResponse();
        
        when(tariffHdrRepository.searchTariffs(
                BigDecimal.valueOf(groupPoid), null, null, null, null, pageable))
                .thenReturn(tariffPage);
        when(mapper.toHeaderOnlyResponse(tariff)).thenReturn(response);
        
        // When
        PageResponse<PdaPortTariffMasterResponse> result = tariffService.getTariffList(
                null, null, null, null, groupPoid, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void deleteTariff_HardDelete() {
        // Given
        Long transactionPoid = 1L;
        Long groupPoid = 100L;
        PdaPortTariffHdr tariff = createMockTariff();

        when(tariffHdrRepository.findByTransactionPoidAndGroupPoidAndDeleted(
                transactionPoid, BigDecimal.valueOf(groupPoid), "N"))
                .thenReturn(Optional.of(tariff));

        // When
        tariffService.deleteTariff(transactionPoid, groupPoid, "user1", true);

        // Then
        verify(slabDtlRepository).deleteByTransactionPoid(transactionPoid);
        verify(chargeDtlRepository).deleteByTransactionPoid(transactionPoid);
        verify(tariffHdrRepository).delete(tariff);
    }

    @Test
    void deleteTariff_NotFound() {
        // Given
        Long transactionPoid = 1L;
        Long groupPoid = 100L;

        when(tariffHdrRepository.findByTransactionPoidAndGroupPoidAndDeleted(
                transactionPoid, BigDecimal.valueOf(groupPoid), "N"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> tariffService.deleteTariff(transactionPoid, groupPoid, "user1", false));
    }

    @Test
    void getChargeDetails_NotFound() {
        // Given
        Long transactionPoid = 1L;
        Long groupPoid = 100L;

        when(tariffHdrRepository.findByTransactionPoidAndGroupPoidAndDeleted(
                transactionPoid, BigDecimal.valueOf(groupPoid), "N"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> tariffService.getChargeDetails(transactionPoid, groupPoid, true));
    }

    private PdaPortTariffMasterRequest createMockRequest() {
        PdaPortTariffMasterRequest request = new PdaPortTariffMasterRequest();
        request.setPorts(Arrays.asList("1001", "1002"));
        request.setVesselTypes(Arrays.asList("2001"));
        request.setPeriodFrom(LocalDate.now());
        request.setPeriodTo(LocalDate.now().plusMonths(6));
        request.setRemarks("Test tariff");
        return request;
    }

    private PdaPortTariffHdr createMockTariff() {
        PdaPortTariffHdr tariff = new PdaPortTariffHdr();
        tariff.setTransactionPoid(1L);
        tariff.setGroupPoid(BigDecimal.valueOf(100L));
        tariff.setDocRef("DOC001");
        tariff.setDeleted("N");
        return tariff;
    }

    private PdaPortTariffMasterResponse createMockResponse() {
        PdaPortTariffMasterResponse response = new PdaPortTariffMasterResponse();
        response.setTransactionPoid(1L);
        response.setDocRef("DOC001");
        return response;
    }

    private CopyTariffRequest createMockCopyRequest() {
        CopyTariffRequest request = new CopyTariffRequest();
        request.setNewPeriodFrom(LocalDate.now().plusYears(1));
        request.setNewPeriodTo(LocalDate.now().plusYears(1).plusMonths(6));
        return request;
    }
}