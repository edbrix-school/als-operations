package com.asg.operations.finaldisbursementaccount.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.LoggingService;
import com.asg.operations.common.PageResponse;
import com.asg.operations.commonlov.dto.LovItem;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.exceptions.CustomException;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.finaldisbursementaccount.dto.FdaHeaderDto;
import com.asg.operations.finaldisbursementaccount.dto.FdaReOpenDto;
import com.asg.operations.finaldisbursementaccount.dto.FdaSupplementaryInfoDto;
import com.asg.operations.finaldisbursementaccount.dto.PartyGlResponse;
import com.asg.operations.finaldisbursementaccount.entity.PdaFdaDtl;
import com.asg.operations.finaldisbursementaccount.entity.PdaFdaHdr;
import com.asg.operations.finaldisbursementaccount.key.PdaFdaDtlId;
import com.asg.operations.finaldisbursementaccount.repository.FdaCustomRepository;
import com.asg.operations.finaldisbursementaccount.repository.PdaFdaDtlRepository;
import com.asg.operations.finaldisbursementaccount.repository.PdaFdaHdrRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FdaServiceImplTest {

    @Mock
    private PdaFdaHdrRepository pdaFdaHdrRepository;

    @Mock
    private PdaFdaDtlRepository pdaFdaDtlRepository;

    @Mock
    private FdaCustomRepository fdaCustomRepository;

    @Mock
    private LovService lovService;

    @Mock
    private DocumentDeleteService documentDeleteService;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private FdaServiceImpl fdaService;

    @BeforeEach
    void setUp() {
        // Mock LovService with lenient stubbing to avoid UnnecessaryStubbingException
        LovItem mockLovItem = new LovItem();
        mockLovItem.setPoid(1L);
        mockLovItem.setCode("TEST");
        mockLovItem.setDescription("Test Item");
        mockLovItem.setLabel("Test Item");
        mockLovItem.setValue(1L);
        mockLovItem.setSeqNo(1);
        lenient().when(lovService.getLovItemByPoid(any(), any(), any(), any(), any())).thenReturn(mockLovItem);
    }

    @Test
    void getFdaHeader_ShouldReturnFdaHeaderDto() {
        PdaFdaHdr entity = createMockFdaHdr();
        when(pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(1L, 1L, 1L))
                .thenReturn(Optional.of(entity));
        when(pdaFdaDtlRepository.findByIdTransactionPoid(1L)).thenReturn(Arrays.asList());

        FdaHeaderDto result = fdaService.getFdaHeader(1L, 1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getTransactionPoid());
    }

    @Test
    void getFdaHeader_WhenNotFound_ShouldThrowException() {
        when(pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(1L, 1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> fdaService.getFdaHeader(1L, 1L, 1L));
    }

    @Test
    void softDeleteFda_ShouldMarkAsDeleted() {
        PdaFdaHdr entity = createMockFdaHdr();

        when(pdaFdaHdrRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(documentDeleteService.deleteDocument(eq(1L), anyString(), anyString(), any(), any())).thenReturn("Success");

        fdaService.softDeleteFda(1L, "user1", new DeleteReasonDto());

        verify(pdaFdaHdrRepository).findById(1L);
        verify(documentDeleteService).deleteDocument(eq(1L), anyString(), anyString(), any(), any());
    }

    @Test
    void getFdaList_ShouldReturnPageResponse() {
        List<PdaFdaHdr> entities = Arrays.asList(createMockFdaHdr());
        Pageable pageable = PageRequest.of(0, 10);
        Page<PdaFdaHdr> page = new PageImpl<>(entities, pageable, 1);

        when(pdaFdaHdrRepository.searchFdaHeaders(anyLong(), anyLong(), any(), any(), any(), any()))
                .thenReturn(page);

        PageResponse<FdaHeaderDto> result = fdaService.getFdaList(1L, 1L, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void deleteCharge_ShouldDeleteCharge() {
        PdaFdaDtl entity = createMockFdaDtl();
        entity.setManual("Y");
        PdaFdaDtlId id = new PdaFdaDtlId(1L, 1L);

        when(pdaFdaDtlRepository.findById(id)).thenReturn(Optional.of(entity));

        fdaService.deleteCharge(1L, 1L, "user1");

        verify(pdaFdaDtlRepository).delete(entity);
    }

    @Test
    void deleteCharge_SystemGenerated_ShouldThrowException() {
        PdaFdaDtl entity = createMockFdaDtl();
        entity.setManual("N");
        PdaFdaDtlId id = new PdaFdaDtlId(1L, 1L);

        when(pdaFdaDtlRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThrows(CustomException.class,
                () -> fdaService.deleteCharge(1L, 1L, "user1"));
    }

    @Test
    void closeFda_WithValidData_ShouldCloseFda() {
        PdaFdaHdr entity = createMockFdaHdr();
        entity.setStatus("O");
        entity.setVesselSailDate(LocalDate.now());
        entity.setAccountsVerified("Y");
        entity.setTotalAmount(BigDecimal.valueOf(1000));

        when(pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(1L, 1L, 1L))
                .thenReturn(Optional.of(entity));
        when(fdaCustomRepository.closeFda(anyLong(), anyLong(), anyLong(), anyLong()))
                .thenReturn("FDA closed successfully");

        String result = fdaService.closeFda(1L, 1L, 1L, 1L);

        assertEquals("FDA closed successfully", result);
    }

    @Test
    void closeFda_AlreadyClosed_ShouldThrowException() {
        PdaFdaHdr entity = createMockFdaHdr();
        entity.setStatus("C");

        when(pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(1L, 1L, 1L))
                .thenReturn(Optional.of(entity));

        assertThrows(CustomException.class,
                () -> fdaService.closeFda(1L, 1L, 1L, 1L));
    }

    @Test
    void reopenFda_ShouldReopenFda() {
        PdaFdaHdr entity = createMockFdaHdr();
        entity.setStatus("CLOSED");
        FdaReOpenDto reopenDto = new FdaReOpenDto();
        reopenDto.setComment("Reopening for corrections");

        when(pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(1L, 1L, 1L))
                .thenReturn(Optional.of(entity));
        when(fdaCustomRepository.reopenFda(anyLong(), anyLong(), anyLong(), anyLong(), anyString()))
                .thenReturn("FDA reopened successfully");

        String result = fdaService.reopenFda(1L, 1L, 1L, 1L, reopenDto);

        assertEquals("FDA reopened successfully", result);
    }

    @Test
    void submitFda_ShouldSubmitFda() {
        PdaFdaHdr entity = createMockFdaHdr();
        entity.setStatus("O");

        when(pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(1L, 1L, 1L))
                .thenReturn(Optional.of(entity));
        when(fdaCustomRepository.submitFda(anyLong(), anyLong(), anyLong(), anyLong()))
                .thenReturn("FDA submitted successfully");

        String result = fdaService.submitFda(1L, 1L, 1L, 1L);

        assertEquals("FDA submitted successfully", result);
    }

    @Test
    void verifyFda_ShouldVerifyFda() {
        PdaFdaHdr entity = createMockFdaHdr();
        entity.setStatus("O");

        when(pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(1L, 1L, 1L))
                .thenReturn(Optional.of(entity));
        when(fdaCustomRepository.verifyFda(anyLong(), anyLong(), anyLong(), anyLong()))
                .thenReturn("FDA verified successfully");

        String result = fdaService.verifyFda(1L, 1L, 1L, 1L);

        assertEquals("FDA verified successfully", result);
        assertEquals("Y", entity.getAccountsVerified());
    }

    @Test
    void returnFda_ShouldReturnFda() {
        PdaFdaHdr entity = createMockFdaHdr();

        when(pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(1L, 1L, 1L))
                .thenReturn(Optional.of(entity));
        when(fdaCustomRepository.returnFda(anyLong(), anyLong(), anyLong(), anyLong(), anyString()))
                .thenReturn("FDA returned successfully");

        String result = fdaService.returnFda(1L, 1L, 1L, 1L, "Correction needed");

        assertEquals("FDA returned successfully", result);
        assertEquals("Correction needed", entity.getOpsCorrectionRemarks());
        assertEquals("N", entity.getAccountsVerified());
    }

    @Test
    void supplementaryFda_ShouldCreateSupplementary() {
        when(fdaCustomRepository.supplementaryFda(anyLong(), anyLong(), anyLong(), anyLong()))
                .thenReturn("Supplementary FDA created");

        String result = fdaService.supplementaryFda(1L, 1L, 1L, 1L);

        assertEquals("Supplementary FDA created", result);
    }

    @Test
    void getSupplementaryInfo_ShouldReturnInfo() {
        List<FdaSupplementaryInfoDto> mockList = Arrays.asList(
                new FdaSupplementaryInfoDto("SUP-001")
        );

        when(fdaCustomRepository.getSupplementaryInfo(anyLong(), anyLong(), anyLong(), anyLong()))
                .thenReturn(mockList);

        List<FdaSupplementaryInfoDto> result = fdaService.getSupplementaryInfo(1L, 1L, 1L, 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void closeFdaWithoutAmount_ShouldCloseFda() {
        PdaFdaHdr entity = createMockFdaHdr();
        entity.setStatus("O");
        entity.setVesselSailDate(LocalDate.now());
        entity.setAccountsVerified("Y");
        entity.setTotalAmount(BigDecimal.ZERO);

        when(pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(1L, 1L, 1L))
                .thenReturn(Optional.of(entity));
        when(fdaCustomRepository.closeFdaWithoutAmount(anyLong(), anyLong(), anyLong(), anyLong(), anyString()))
                .thenReturn("FDA closed without amount");

        String result = fdaService.closeFdaWithoutAmount(1L, 1L, 1L, 1L, "No charges");

        assertEquals("FDA closed without amount", result);
    }

    @Test
    void getPartyGl_ShouldReturnPartyGl() {
        PartyGlResponse mockResponse = new PartyGlResponse();

        when(fdaCustomRepository.getPartyGl(anyLong(), anyLong(), anyLong(), anyLong(), anyString()))
                .thenReturn(mockResponse);

        PartyGlResponse result = fdaService.getPartyGl(1L, 1L, 1L, 1L, "SUPPLIER");

        assertNotNull(result);
    }

    private PdaFdaHdr createMockFdaHdr() {
        PdaFdaHdr entity = new PdaFdaHdr();
        entity.setTransactionPoid(1L);
        entity.setGroupPoid(1L);
        entity.setCompanyPoid(1L);
        entity.setPrincipalPoid(1L);
        entity.setSalesmanPoid(1L);
        entity.setPortPoid(1L);
        entity.setVoyagePoid(1L);
        entity.setVesselPoid(1L);
        entity.setCommodityPoid("1");
        entity.setAddressPoid(1L);
        entity.setTermsPoid(1L);
        entity.setVesselTypePoid("1");
        entity.setLinePoid(1L);
        entity.setPrintBankPoid(1L);
        entity.setVesselHandledBy(1L);
        entity.setGrt(BigDecimal.valueOf(1000));
        entity.setStatus("O");
        entity.setDeleted("N");
        entity.setCreatedDate(LocalDateTime.now());
        return entity;
    }

    private PdaFdaDtl createMockFdaDtl() {
        PdaFdaDtl entity = new PdaFdaDtl();
        PdaFdaDtlId id = new PdaFdaDtlId(1L, 1L);
        entity.setId(id);
        entity.setChargePoid(1L);
        entity.setQty(BigDecimal.valueOf(10));
        entity.setPdaRate(BigDecimal.valueOf(100));
        entity.setAmount(BigDecimal.valueOf(1000));
        entity.setFdaAmount(BigDecimal.valueOf(1000));
        entity.setCostAmount(BigDecimal.valueOf(800));
        entity.setManual("Y");
        return entity;
    }
}