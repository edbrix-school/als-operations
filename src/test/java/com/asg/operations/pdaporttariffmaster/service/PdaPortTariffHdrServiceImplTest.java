package com.asg.operations.pdaporttariffmaster.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.operations.commonlov.dto.LovItem;
import com.asg.operations.commonlov.dto.LovResponse;
import com.asg.operations.commonlov.service.LovService;
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
import java.util.List;
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

    @Mock
    private LovService lovService;

    @Mock
    private com.asg.common.lib.service.DocumentDeleteService documentDeleteService;

    @InjectMocks
    private PdaPortTariffHdrServiceImpl tariffService;

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

        tariffService.deleteTariff(transactionPoid, groupPoid, "user1", new DeleteReasonDto());

        verify(documentDeleteService).deleteDocument(eq(transactionPoid), eq("PDA_PORT_TARIFF_HDR"), eq("TRANSACTION_POID"), any(), any());
    }

    @Test
    void deleteTariff_HardDelete() {
        Long transactionPoid = 1L;
        Long groupPoid = 100L;
        PdaPortTariffHdr tariff = createMockTariff();

        when(tariffHdrRepository.findByTransactionPoidAndGroupPoidAndDeleted(
                transactionPoid, BigDecimal.valueOf(groupPoid), "N"))
                .thenReturn(Optional.of(tariff));

        tariffService.deleteTariff(transactionPoid, groupPoid, "user1",  new DeleteReasonDto());

        verify(documentDeleteService).deleteDocument(eq(transactionPoid), eq("PDA_PORT_TARIFF_HDR"), eq("TRANSACTION_POID"), any(), any());
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