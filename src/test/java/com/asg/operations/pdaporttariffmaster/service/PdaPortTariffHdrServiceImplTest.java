package com.asg.operations.pdaporttariffmaster.service;

import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.pdaporttariffmaster.dto.*;
import com.asg.operations.pdaporttariffmaster.entity.PdaPortTariffHdr;
import com.asg.operations.pdaporttariffmaster.repository.*;
import com.asg.operations.pdaporttariffmaster.util.DateOverlapValidator;
import com.asg.operations.pdaporttariffmaster.util.PdaPortTariffMapper;
import com.asg.operations.pdaporttariffmaster.util.PortTariffDocumentRefGenerator;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
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

    @Mock
    private ShipPortMasterRepository shipPortMasterRepository;

    @Mock
    private ShipVesselTypeMasterRepository shipVesselTypeMasterRepository;

    @Mock
    private ShipChargeMasterRepository shipChargeMasterRepository;

    @InjectMocks
    private PdaPortTariffHdrServiceImpl tariffService;

    @Test
    void getAllTariffsWithFilters_Success() {
        GetAllTariffFilterRequest filterRequest = new GetAllTariffFilterRequest();
        filterRequest.setIsDeleted("N");

        jakarta.persistence.Query mockQuery = mock(jakarta.persistence.Query.class);
        jakarta.persistence.Query mockCountQuery = mock(jakarta.persistence.Query.class);

        when(entityManager.createNativeQuery(anyString())).thenReturn(mockQuery).thenReturn(mockCountQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockCountQuery.setParameter(anyString(), any())).thenReturn(mockCountQuery);
        when(mockQuery.setFirstResult(anyInt())).thenReturn(mockQuery);
        when(mockQuery.setMaxResults(anyInt())).thenReturn(mockQuery);
        when(mockCountQuery.getSingleResult()).thenReturn(1L);
        Object[] mockRow = new Object[11];
        // Fill with correct data types based on mapToTariffResponseDto expectations
        mockRow[0] = 1L; // TRANSACTION_POID (Number)
        mockRow[1] = "DOC001"; // DOC_REF (String)
        mockRow[2] = new java.sql.Timestamp(System.currentTimeMillis()); // TRANSACTION_DATE (Timestamp)
        mockRow[3] = "PORT1,PORT2"; // PORTS (String)
        mockRow[4] = "VESSEL1,VESSEL2"; // VESSEL_TYPES (String)
        mockRow[5] = new java.sql.Timestamp(System.currentTimeMillis()); // PERIOD_FROM (Timestamp)
        mockRow[6] = new java.sql.Timestamp(System.currentTimeMillis()); // PERIOD_TO (Timestamp)
        mockRow[7] = "Test remarks"; // REMARKS (String)
        mockRow[8] = "N"; // DELETED (String)
        mockRow[9] = new java.sql.Timestamp(System.currentTimeMillis()); // CREATED_DATE (Timestamp)
        mockRow[10] = new java.sql.Timestamp(System.currentTimeMillis()); // LASTMODIFIED_DATE (Timestamp)

        java.util.List<Object[]> mockResults = new java.util.ArrayList<>();
        mockResults.add(mockRow);
        when(mockQuery.getResultList()).thenReturn(mockResults);

        Page<PdaPortTariffListResponse> result = tariffService.getAllTariffsWithFilters(
                100L, 200L, filterRequest, 0, 10, "docRef,asc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getTariffById_Success() {
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

        PdaPortTariffMasterResponse result = tariffService.getTariffById(transactionPoid, groupPoid);

        assertNotNull(result);
        assertEquals(expectedResponse.getTransactionPoid(), result.getTransactionPoid());
    }

    @Test
    void getTariffById_NotFound() {
        Long transactionPoid = 1L;
        Long groupPoid = 100L;

        when(tariffHdrRepository.findByTransactionPoidAndGroupPoid(
                transactionPoid, BigDecimal.valueOf(groupPoid)))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tariffService.getTariffById(transactionPoid, groupPoid));
    }

    @Test
    void deleteTariff_SoftDelete() {
        Long transactionPoid = 1L;
        Long groupPoid = 100L;
        PdaPortTariffHdr tariff = createMockTariff();

        when(tariffHdrRepository.findByTransactionPoidAndGroupPoidAndDeleted(
                transactionPoid, BigDecimal.valueOf(groupPoid), "N"))
                .thenReturn(Optional.of(tariff));

        tariffService.deleteTariff(transactionPoid, groupPoid, "user1", false);

        verify(tariffHdrRepository).save(tariff);
        assertEquals("Y", tariff.getDeleted());
    }

    @Test
    void deleteTariff_HardDelete() {
        Long transactionPoid = 1L;
        Long groupPoid = 100L;
        PdaPortTariffHdr tariff = createMockTariff();

        when(tariffHdrRepository.findByTransactionPoidAndGroupPoidAndDeleted(
                transactionPoid, BigDecimal.valueOf(groupPoid), "N"))
                .thenReturn(Optional.of(tariff));

        tariffService.deleteTariff(transactionPoid, groupPoid, "user1", true);

        verify(slabDtlRepository).deleteByTransactionPoid(transactionPoid);
        verify(chargeDtlRepository).deleteByTransactionPoid(transactionPoid);
        verify(tariffHdrRepository).delete(tariff);
    }

    @Test
    void getChargeDetails_Success() {
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

        ChargeDetailsResponse result = tariffService.getChargeDetails(transactionPoid, groupPoid, true);

        assertNotNull(result);
        verify(mapper).toChargeDetailsResponse(any(), eq(transactionPoid));
    }

    private PdaPortTariffHdr createMockTariff() {
        PdaPortTariffHdr tariff = new PdaPortTariffHdr();
        tariff.setTransactionPoid(1L);
        tariff.setDocRef("DOC001");
        return tariff;
    }

    private PdaPortTariffMasterResponse createMockResponse() {
        PdaPortTariffMasterResponse response = new PdaPortTariffMasterResponse();
        response.setTransactionPoid(1L);
        response.setDocRef("DOC001");
        return response;
    }
}