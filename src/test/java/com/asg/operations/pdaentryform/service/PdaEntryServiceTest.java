package com.asg.operations.pdaentryform.service;

import com.asg.operations.pdaentryform.dto.*;
import com.asg.operations.pdaentryform.entity.PdaEntryDtl;
import com.asg.operations.pdaentryform.entity.PdaEntryDtlId;
import com.asg.operations.pdaentryform.entity.PdaEntryHdr;
import com.asg.operations.pdaentryform.entity.PdaEntryVehicleDtl;
import com.asg.operations.pdaentryform.repository.*;
import com.asg.operations.pdaentryform.service.impl.PdaEntryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdaEntryServiceTest {

    @Mock
    private PdaEntryHdrRepository entryHdrRepository;

    @Mock
    private PdaEntryDtlRepository entryDtlRepository;

    @Mock
    private PdaEntryVehicleDtlRepository vehicleDtlRepository;

    @Mock
    private PdaEntryTdrDetailRepository tdrDetailRepository;

    @Mock
    private PdaEntryAcknowledgmentDtlRepository acknowledgmentDtlRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private jakarta.persistence.EntityManager entityManager;

    @InjectMocks
    private PdaEntryServiceImpl pdaEntryService;

    private Long groupPoid;
    private Long companyPoid;
    private String userId;
    private Long transactionPoid;

    @BeforeEach
    void setUp() {
        groupPoid = 1L;
        companyPoid = 100L;
        userId = "USER123";
        transactionPoid = 1000L;
    }

    @Test
    void testBulkSaveChargeDetails_CreateNew() {
        PdaEntryHdr entry = new PdaEntryHdr();
        entry.setTransactionPoid(transactionPoid);
        entry.setStatus("PROPOSAL");

        when(entryHdrRepository.findByTransactionPoidAndFilters(transactionPoid, groupPoid, companyPoid))
                .thenReturn(Optional.of(entry));
        when(entryHdrRepository.findById(transactionPoid))
                .thenReturn(Optional.of(entry));
        when(entryDtlRepository.calculateTotalAmount(transactionPoid))
                .thenReturn(new BigDecimal("5500.00"));
        when(entryDtlRepository.save(any(PdaEntryDtl.class)))
                .thenReturn(new PdaEntryDtl());
        when(entryHdrRepository.save(any(PdaEntryHdr.class)))
                .thenReturn(entry);

        BulkSaveChargeDetailsRequest request = new BulkSaveChargeDetailsRequest();
        PdaEntryChargeDetailRequest chargeDetail = new PdaEntryChargeDetailRequest();
        chargeDetail.setDetRowId(null);
        chargeDetail.setChargePoid(new BigDecimal(100));
        chargeDetail.setQty(new BigDecimal(5));
        chargeDetail.setDays(new BigDecimal(10));
        chargeDetail.setPdaRate(new BigDecimal(100));
        chargeDetail.setTaxPercentage(new BigDecimal(10));
        request.setChargeDetails(List.of(chargeDetail));
        request.setDeleteDetRowIds(new ArrayList<>());

        List<PdaEntryChargeDetailResponse> result = pdaEntryService.bulkSaveChargeDetails(
                transactionPoid, request, groupPoid, companyPoid, userId);

        assertNotNull(result);
    }

    @Test
    void testBulkSaveVehicleDetails_CreateNew() {
        PdaEntryHdr entry = new PdaEntryHdr();
        entry.setTransactionPoid(transactionPoid);
        entry.setStatus("PROPOSAL");

        when(entryHdrRepository.findByTransactionPoidAndFilters(transactionPoid, groupPoid, companyPoid))
                .thenReturn(Optional.of(entry));
        when(vehicleDtlRepository.save(any(PdaEntryVehicleDtl.class)))
                .thenReturn(new PdaEntryVehicleDtl());

        BulkSaveVehicleDetailsRequest request = new BulkSaveVehicleDetailsRequest();
        PdaEntryVehicleDetailRequest vehicleDetail = new PdaEntryVehicleDetailRequest();
        vehicleDetail.setDetRowId(null);
        vehicleDetail.setVesselName("MAERSK");
        vehicleDetail.setVehicleModel("BMW X5");
        vehicleDetail.setVinNumber("VIN123456");
        request.setVehicleDetails(List.of(vehicleDetail));
        request.setDeleteDetRowIds(new ArrayList<>());

        List<PdaEntryVehicleDetailResponse> result = pdaEntryService.bulkSaveVehicleDetails(
                transactionPoid, request, groupPoid, companyPoid, userId);

        assertNotNull(result);
    }

    @Test
    void testBulkSaveChargeDetails_Delete() {
        PdaEntryHdr entry = new PdaEntryHdr();
        entry.setTransactionPoid(transactionPoid);
        entry.setStatus("PROPOSAL");

        PdaEntryDtl existingDetail = new PdaEntryDtl();
        existingDetail.setTransactionPoid(transactionPoid);
        existingDetail.setDetRowId(100L);

        when(entryHdrRepository.findByTransactionPoidAndFilters(transactionPoid, groupPoid, companyPoid))
                .thenReturn(Optional.of(entry));
        when(entryHdrRepository.findById(transactionPoid))
                .thenReturn(Optional.of(entry));
        when(entryDtlRepository.findById(any(PdaEntryDtlId.class)))
                .thenReturn(Optional.of(existingDetail));
        when(entryDtlRepository.calculateTotalAmount(transactionPoid))
                .thenReturn(BigDecimal.ZERO);
        when(entryHdrRepository.save(any(PdaEntryHdr.class)))
                .thenReturn(entry);

        BulkSaveChargeDetailsRequest request = new BulkSaveChargeDetailsRequest();
        request.setChargeDetails(new ArrayList<>());
        request.setDeleteDetRowIds(List.of(100L));

        pdaEntryService.bulkSaveChargeDetails(transactionPoid, request, groupPoid, companyPoid, userId);

        assertNotNull(request);
    }

    @Test
    void testGetChargeDetails() {
        PdaEntryHdr entry = new PdaEntryHdr();
        entry.setTransactionPoid(transactionPoid);

        PdaEntryDtl detail = new PdaEntryDtl();
        detail.setTransactionPoid(transactionPoid);
        detail.setDetRowId(1L);
        detail.setChargePoid(new BigDecimal(100));
        detail.setQty(new BigDecimal(5));

        when(entryHdrRepository.findByTransactionPoidAndFilters(transactionPoid, groupPoid, companyPoid))
                .thenReturn(Optional.of(entry));
        when(entryDtlRepository.findByTransactionPoidOrderBySeqnoAscDetRowIdAsc(transactionPoid))
                .thenReturn(List.of(detail));

        List<PdaEntryChargeDetailResponse> result = pdaEntryService.getChargeDetails(
                transactionPoid, groupPoid, companyPoid);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetVehicleDetails() {
        PdaEntryHdr entry = new PdaEntryHdr();
        entry.setTransactionPoid(transactionPoid);

        PdaEntryVehicleDtl vehicleDetail = new PdaEntryVehicleDtl();
        vehicleDetail.setTransactionPoid(transactionPoid);
        vehicleDetail.setDetRowId(1L);
        vehicleDetail.setVesselName("MAERSK");

        when(entryHdrRepository.findByTransactionPoidAndFilters(transactionPoid, groupPoid, companyPoid))
                .thenReturn(Optional.of(entry));
        when(vehicleDtlRepository.findByTransactionPoidOrderByDetRowIdAsc(transactionPoid))
                .thenReturn(List.of(vehicleDetail));

        List<PdaEntryVehicleDetailResponse> result = pdaEntryService.getVehicleDetails(
                transactionPoid, groupPoid, companyPoid);

        assertNotNull(result);
        assertEquals(1, result.size());
    }


    @Test
    void testClearChargeDetails() {
        PdaEntryHdr entry = new PdaEntryHdr();
        entry.setTransactionPoid(transactionPoid);
        entry.setStatus("PROPOSAL");
        entry.setRefType("GENERAL");

        when(entryHdrRepository.findByTransactionPoidAndFilters(transactionPoid, groupPoid, companyPoid))
                .thenReturn(Optional.of(entry));

        pdaEntryService.clearChargeDetails(transactionPoid, groupPoid, companyPoid, Long.valueOf(userId));

        assertNotNull(entry);
    }

    @Test
    void testClearVehicleDetails() {
        PdaEntryHdr entry = new PdaEntryHdr();
        entry.setTransactionPoid(transactionPoid);
        entry.setStatus("PROPOSAL");

        when(entryHdrRepository.findByTransactionPoidAndFilters(transactionPoid, groupPoid, companyPoid))
                .thenReturn(Optional.of(entry));

        pdaEntryService.clearVehicleDetails(transactionPoid, groupPoid, companyPoid, Long.valueOf(userId));

        assertNotNull(entry);
    }


    @Test
    void testPublishVehicleDetailsForImport() {
        PdaEntryHdr entry = new PdaEntryHdr();
        entry.setTransactionPoid(transactionPoid);
        entry.setStatus("PROPOSAL");

        when(entryHdrRepository.findByTransactionPoidAndFilters(transactionPoid, groupPoid, companyPoid))
                .thenReturn(Optional.of(entry));

        pdaEntryService.publishVehicleDetailsForImport(transactionPoid, groupPoid, companyPoid, Long.valueOf(userId));

        assertNotNull(entry);
    }

    @Test
    void testGetTdrDetails() {
        PdaEntryHdr entry = new PdaEntryHdr();
        entry.setTransactionPoid(transactionPoid);

        when(entryHdrRepository.findByTransactionPoidAndFilters(transactionPoid, groupPoid, companyPoid))
                .thenReturn(Optional.of(entry));
        when(tdrDetailRepository.findByTransactionPoidOrderByDetRowIdAsc(transactionPoid))
                .thenReturn(new ArrayList<>());

        List<PdaEntryTdrDetailResponse> result = pdaEntryService.getTdrDetails(
                transactionPoid, groupPoid, companyPoid);

        assertNotNull(result);
    }

    @Test
    void testGetAcknowledgmentDetails() {
        PdaEntryHdr entry = new PdaEntryHdr();
        entry.setTransactionPoid(transactionPoid);

        when(entryHdrRepository.findByTransactionPoidAndFilters(transactionPoid, groupPoid, companyPoid))
                .thenReturn(Optional.of(entry));
        when(acknowledgmentDtlRepository.findByTransactionPoidOrderByDetRowIdAsc(transactionPoid))
                .thenReturn(new ArrayList<>());

        List<PdaEntryAcknowledgmentDetailResponse> result = pdaEntryService.getAcknowledgmentDetails(
                transactionPoid, groupPoid, companyPoid);

        assertNotNull(result);
    }

    @Test
    void testRecalculateChargeDetails() {
        PdaEntryHdr entry = new PdaEntryHdr();
        entry.setTransactionPoid(transactionPoid);
        entry.setStatus("PROPOSAL");
        entry.setVesselTypePoid(new BigDecimal(1));
        entry.setGrt(new BigDecimal(1000));
        entry.setNrt(new BigDecimal(500));
        entry.setDwt(new BigDecimal(2000));
        entry.setPortPoid(new BigDecimal(1));
        entry.setSailDate(java.time.LocalDate.now());
        entry.setNumberOfDays(new BigDecimal(5));
        entry.setHarbourCallType("PORT");
        entry.setTotalQuantity(new BigDecimal(100));

        when(entryHdrRepository.findByTransactionPoidAndFilters(transactionPoid, groupPoid, companyPoid))
                .thenReturn(Optional.of(entry));
        when(entryHdrRepository.findById(transactionPoid))
                .thenReturn(Optional.of(entry));
        when(entryDtlRepository.calculateTotalAmount(transactionPoid))
                .thenReturn(new BigDecimal("5000.00"));
        when(entryHdrRepository.save(any(PdaEntryHdr.class)))
                .thenReturn(entry);

        List<PdaEntryChargeDetailResponse> result = pdaEntryService.recalculateChargeDetails(
                transactionPoid, groupPoid, companyPoid, Long.valueOf(userId));

        assertNotNull(result);
    }

    @Test
    void testLoadDefaultCharges() {
        PdaEntryHdr entry = new PdaEntryHdr();
        entry.setTransactionPoid(transactionPoid);
        entry.setStatus("PROPOSAL");
        entry.setVesselTypePoid(new BigDecimal(1));
        entry.setGrt(new BigDecimal(1000));
        entry.setNrt(new BigDecimal(500));
        entry.setDwt(new BigDecimal(2000));
        entry.setPortPoid(new BigDecimal(1));
        entry.setSailDate(java.time.LocalDate.now());
        entry.setNumberOfDays(new BigDecimal(5));
        entry.setHarbourCallType("PORT");
        entry.setTotalQuantity(new BigDecimal(100));

        when(entryHdrRepository.findByTransactionPoidAndFilters(transactionPoid, groupPoid, companyPoid))
                .thenReturn(Optional.of(entry));
        when(entryHdrRepository.findById(transactionPoid))
                .thenReturn(Optional.of(entry));
        when(entryDtlRepository.calculateTotalAmount(transactionPoid))
                .thenReturn(new BigDecimal("5000.00"));
        when(entryHdrRepository.save(any(PdaEntryHdr.class)))
                .thenReturn(entry);

        List<PdaEntryChargeDetailResponse> result = pdaEntryService.loadDefaultCharges(
                transactionPoid, groupPoid, companyPoid, Long.valueOf(userId));

        assertNotNull(result);
    }
}
