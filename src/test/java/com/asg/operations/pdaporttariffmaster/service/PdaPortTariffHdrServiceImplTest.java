package com.asg.operations.pdaporttariffmaster.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.pdaporttariffmaster.dto.*;
import com.asg.operations.pdaporttariffmaster.entity.PdaPortTariffHdr;
import com.asg.operations.pdaporttariffmaster.repository.*;
import com.asg.operations.pdaporttariffmaster.util.PdaPortTariffMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdaPortTariffHdrServiceImplTest {

    @Mock private PdaPortTariffHdrRepository tariffHdrRepository;
    @Mock private PdaPortTariffChargeDtlRepository chargeDtlRepository;
    @Mock private PdaPortTariffSlabDtlRepository slabDtlRepository;
    @Mock private PdaPortTariffMapper mapper;
    @Mock private EntityManager entityManager;
    @Mock private PdaRateTypeMasterRepository pdaRateTypeMasterRepository;
    @Mock private ShipPortMasterRepository shipPortMasterRepository;
    @Mock private ShipVesselTypeMasterRepository shipVesselTypeMasterRepository;
    @Mock private ShipChargeMasterRepository shipChargeMasterRepository;
    @Mock private LoggingService loggingService;
    @Mock private DocumentDeleteService documentDeleteService;
    @Mock private DocumentSearchService documentSearchService;

    @InjectMocks
    private PdaPortTariffHdrServiceImpl tariffService;

    @Test
    void getTariffById_Success() {
        PdaPortTariffHdr tariff = createMockTariff();
        PdaPortTariffMasterResponse expectedResponse = createMockResponse();

        when(tariffHdrRepository.findByTransactionPoid(1L)).thenReturn(Optional.of(tariff));
        when(chargeDtlRepository.findByTransactionPoid(1L)).thenReturn(Collections.emptyList());
        when(mapper.toResponseWithChargeDetails(eq(tariff), any())).thenReturn(expectedResponse);

        PdaPortTariffMasterResponse result = tariffService.getTariffById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getTransactionPoid());
        verify(tariffHdrRepository).findByTransactionPoid(1L);
    }

    @Test
    void getTariffById_NotFound() {
        when(tariffHdrRepository.findByTransactionPoid(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tariffService.getTariffById(1L));
    }

    @Test
    void deleteTariff_Success() {
        PdaPortTariffHdr tariff = createMockTariff();
        when(tariffHdrRepository.findByTransactionPoid(1L)).thenReturn(Optional.of(tariff));

        tariffService.deleteTariff(1L, new DeleteReasonDto());

        verify(documentDeleteService).deleteDocument(eq(1L), eq("PDA_PORT_TARIFF_HDR"), eq("TRANSACTION_POID"), any(), any());
    }

    @Test
    void deleteTariff_NotFound() {
        when(tariffHdrRepository.findByTransactionPoid(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tariffService.deleteTariff(1L, new DeleteReasonDto()));
    }

    @Test
    void getChargeDetails_Success() {
        PdaPortTariffHdr tariff = createMockTariff();
        ChargeDetailsResponse expectedResponse = new ChargeDetailsResponse();

        when(tariffHdrRepository.findByTransactionPoid(1L)).thenReturn(Optional.of(tariff));
        when(chargeDtlRepository.findByTransactionPoid(1L)).thenReturn(Collections.emptyList());
        when(mapper.toChargeDetailsResponse(any(), eq(1L))).thenReturn(expectedResponse);

        ChargeDetailsResponse result = tariffService.getChargeDetails(1L, true);

        assertNotNull(result);
        verify(mapper).toChargeDetailsResponse(any(), eq(1L));
    }

    @Test
    void getChargeDetails_NotFound() {
        when(tariffHdrRepository.findByTransactionPoid(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tariffService.getChargeDetails(1L, true));
    }

    @Test
    void bulkSaveChargeDetails_Success() {
        PdaPortTariffHdr tariff = createMockTariff();
        ChargeDetailsRequest request = new ChargeDetailsRequest();
        request.setChargeDetails(Collections.emptyList());
        ChargeDetailsResponse expectedResponse = new ChargeDetailsResponse();

        when(tariffHdrRepository.findByTransactionPoid(1L)).thenReturn(Optional.of(tariff));
        // second call from getChargeDetails inside bulkSave
        when(chargeDtlRepository.findByTransactionPoid(1L)).thenReturn(Collections.emptyList());
        when(mapper.toChargeDetailsResponse(any(), eq(1L))).thenReturn(expectedResponse);

        ChargeDetailsResponse result = tariffService.bulkSaveChargeDetails(1L, request);

        assertNotNull(result);
        verify(tariffHdrRepository, times(2)).findByTransactionPoid(1L);
    }

    @Test
    void createTariff_Success() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCompanyPoid).thenReturn(100L);
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(200L);
            mockedUserContext.when(UserContext::getDocumentId).thenReturn("DOC_ID");

            PdaPortTariffMasterRequest request = createMockRequest();
            PdaPortTariffHdr savedTariff = createMockTariff();
            PdaPortTariffMasterResponse expectedResponse = createMockResponse();

            when(shipPortMasterRepository.existsByIdPortPoidAndIdGroupPoid(any(), any())).thenReturn(true);
            when(shipVesselTypeMasterRepository.existsByVesselTypePoidAndGroupPoid(any(), any())).thenReturn(true);
            when(mapper.listToString(any())).thenReturn("1;2");
            when(tariffHdrRepository.existsOverlappingPeriod(any(), any(), any(), any(), any(), any())).thenReturn(false);
            when(mapper.toEntity(request)).thenReturn(savedTariff);
            when(tariffHdrRepository.save(savedTariff)).thenReturn(savedTariff);
            when(tariffHdrRepository.findByTransactionPoid(1L)).thenReturn(Optional.of(savedTariff));
            when(chargeDtlRepository.findByTransactionPoid(1L)).thenReturn(Collections.emptyList());
            when(mapper.toResponseWithChargeDetails(eq(savedTariff), any())).thenReturn(expectedResponse);

            PdaPortTariffMasterResponse result = tariffService.createTariff(request);

            assertNotNull(result);
            assertEquals(1L, result.getTransactionPoid());
            verify(tariffHdrRepository).save(savedTariff);
        }
    }

    @Test
    void updateTariff_Success() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(200L);
            mockedUserContext.when(UserContext::getDocumentId).thenReturn("DOC_ID");

            PdaPortTariffMasterRequest request = createMockRequest();
            PdaPortTariffHdr existingTariff = createMockTariff();
            PdaPortTariffMasterResponse expectedResponse = createMockResponse();

            when(shipPortMasterRepository.existsByIdPortPoidAndIdGroupPoid(any(), any())).thenReturn(true);
            when(shipVesselTypeMasterRepository.existsByVesselTypePoidAndGroupPoid(any(), any())).thenReturn(true);
            when(mapper.listToString(any())).thenReturn("1;2");
            when(tariffHdrRepository.existsOverlappingPeriod(any(), any(), any(), any(), any(), any())).thenReturn(false);
            when(tariffHdrRepository.findByTransactionPoid(1L)).thenReturn(Optional.of(existingTariff));
            when(tariffHdrRepository.save(existingTariff)).thenReturn(existingTariff);
            when(chargeDtlRepository.findByTransactionPoid(1L)).thenReturn(Collections.emptyList());
            when(mapper.toResponseWithChargeDetails(eq(existingTariff), any())).thenReturn(expectedResponse);

            PdaPortTariffMasterResponse result = tariffService.updateTariff(1L, request);

            assertNotNull(result);
            verify(tariffHdrRepository).save(existingTariff);
        }
    }

    @Test
    void copyTariff_Success() {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCompanyPoid).thenReturn(100L);
            mockedUserContext.when(UserContext::getGroupPoid).thenReturn(200L);
            mockedUserContext.when(UserContext::getDocumentId).thenReturn("DOC_ID");

            PdaPortTariffHdr sourceTariff = createMockTariff();
            PdaPortTariffMasterRequest copyRequest = createMockRequest();
            PdaPortTariffHdr savedTariff = createMockTariff();
            PdaPortTariffMasterResponse expectedResponse = createMockResponse();

            CopyTariffRequest request = new CopyTariffRequest();
            request.setNewPeriodFrom(java.time.LocalDate.of(2025, 1, 1));
            request.setNewPeriodTo(java.time.LocalDate.of(2025, 12, 31));

            when(tariffHdrRepository.findByTransactionPoid(1L)).thenReturn(Optional.of(sourceTariff));
            when(mapper.toRequest(sourceTariff)).thenReturn(copyRequest);
            when(shipPortMasterRepository.existsByIdPortPoidAndIdGroupPoid(any(), any())).thenReturn(true);
            when(shipVesselTypeMasterRepository.existsByVesselTypePoidAndGroupPoid(any(), any())).thenReturn(true);
            when(mapper.listToString(any())).thenReturn("1;2");
            when(tariffHdrRepository.existsOverlappingPeriod(any(), any(), any(), any(), any(), any())).thenReturn(false);
            when(mapper.toEntity(any())).thenReturn(savedTariff);
            when(tariffHdrRepository.save(savedTariff)).thenReturn(savedTariff);
            when(chargeDtlRepository.findByTransactionPoid(1L)).thenReturn(Collections.emptyList());
            when(mapper.toResponseWithChargeDetails(any(), any())).thenReturn(expectedResponse);

            PdaPortTariffMasterResponse result = tariffService.copyTariff(1L, request);

            assertNotNull(result);
            verify(mapper).toRequest(sourceTariff);
        }
    }

    private PdaPortTariffHdr createMockTariff() {
        PdaPortTariffHdr tariff = new PdaPortTariffHdr();
        tariff.setTransactionPoid(1L);
        tariff.setDocRef("DOC001");
        tariff.setGroupPoid(200L);
        tariff.setCompanyPoid(100L);
        return tariff;
    }

    private PdaPortTariffMasterResponse createMockResponse() {
        PdaPortTariffMasterResponse response = new PdaPortTariffMasterResponse();
        response.setTransactionPoid(1L);
        response.setDocRef("DOC001");
        return response;
    }

    private PdaPortTariffMasterRequest createMockRequest() {
        PdaPortTariffMasterRequest request = new PdaPortTariffMasterRequest();
        request.setPort("1");
        request.setVesselTypes(List.of("1", "2"));
        request.setPeriodFrom(java.time.LocalDate.of(2024, 1, 1));
        request.setPeriodTo(java.time.LocalDate.of(2024, 12, 31));
        return request;
    }
}
