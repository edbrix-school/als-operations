package com.asg.operations.projectjob.util;

import com.asg.operations.projectjob.dto.*;
import com.asg.operations.projectjob.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProjectJobMapperTest {

    @Test
    void testMapAirPkgFromDto() {
        ProjectJobAirPkgDto dto = new ProjectJobAirPkgDto();
        dto.setDetRowId(1L);
        dto.setNoOfPacks(10L);
        dto.setPackUnit("PCS");
        dto.setTotalWeight(BigDecimal.valueOf(100.0));
        dto.setTotalVolume(BigDecimal.valueOf(50.0));
        dto.setLength(BigDecimal.valueOf(10.0));
        dto.setWidth(BigDecimal.valueOf(5.0));
        dto.setHeight(2L);
        dto.setImcoClassUnno("1234");
        dto.setProperShippingName("TEST");
        dto.setImcoClassDivision("1.1");
        dto.setPackingGrouping("I");
        dto.setQuantityPackingType("TYPE");
        dto.setPackingInst("INST");
        dto.setAuthorisation("AUTH");
        dto.setDescription("DESC");
        dto.setAppointmentDate(LocalDateTime.now());
        dto.setDeliveryDate(LocalDateTime.now());
        dto.setDetention("D");
        dto.setDocStatus("STATUS");
        dto.setRemarks("REMARKS");
        dto.setChargeableWeight(BigDecimal.valueOf(110.0));

        FFManifestAirPkgDtl entity = new FFManifestAirPkgDtl();
        ProjectJobMapper.mapAirPkgFromDto(dto, entity, 100L);

        assertEquals(100L, entity.getTransactionPoid());
        assertEquals(1L, entity.getDetRowId());
        assertEquals(10L, entity.getNoOfPacks());
        assertEquals("PCS", entity.getPackUnit());
        assertEquals(BigDecimal.valueOf(100.0), entity.getTotalWeight());
        assertEquals(BigDecimal.valueOf(50.0), entity.getTotalVolume());
        assertEquals(BigDecimal.valueOf(10.0), entity.getLength());
        assertEquals(BigDecimal.valueOf(5.0), entity.getWidth());
        assertEquals(2L, entity.getHeight());
        assertEquals("1234", entity.getImcoClassUnno());
        assertEquals("TEST", entity.getProperShippingName());
        assertEquals("1.1", entity.getImcoClassDivision());
        assertEquals("I", entity.getPackingGrouping());
        assertEquals("TYPE", entity.getQuantityPackingType());
        assertEquals("INST", entity.getPackingInst());
        assertEquals("AUTH", entity.getAuthorisation());
        assertEquals("DESC", entity.getDescription());
        assertNotNull(entity.getAppointmentDate());
        assertNotNull(entity.getDeliveryDate());
        assertEquals("D", entity.getDetention());
        assertEquals("STATUS", entity.getDocStatus());
        assertEquals("REMARKS", entity.getRemarks());
        assertEquals(new BigDecimal("110.0"), entity.getChargeableWeight());
    }

    @Test
    void testMapAirPkgFromDto_NullDto() {
        FFManifestAirPkgDtl entity = new FFManifestAirPkgDtl();
        assertDoesNotThrow(() -> ProjectJobMapper.mapAirPkgFromDto(null, entity, 100L));
        assertNull(entity.getTransactionPoid());
    }

    @Test
    void testMapBayanFromDto() {
        ProjectJobBayanDto dto = new ProjectJobBayanDto();
        dto.setDetRowId(1L);
        dto.setBayanNumber("BAYAN123");
        dto.setBayanMode("MODE");
        dto.setDutyAmount(BigDecimal.valueOf(500.0));
        dto.setVatAmount(BigDecimal.valueOf(75.0));
        dto.setTotalPaidAmount(BigDecimal.valueOf(575.0));
        dto.setExpiryDate(LocalDateTime.now());
        dto.setSubmittedDate(LocalDateTime.now());
        dto.setPaymentDate(LocalDateTime.now());

        FFManifestBayanDtl entity = new FFManifestBayanDtl();
        ProjectJobMapper.mapBayanFromDto(dto, entity, 100L);

        assertEquals(100L, entity.getTransactionPoid());
        assertEquals(1L, entity.getDetRowId());
        assertEquals("BAYAN123", entity.getBayanNumber());
        assertEquals("MODE", entity.getBayanMode());
        assertEquals(BigDecimal.valueOf(500.0), entity.getDutyAmount());
        assertEquals(BigDecimal.valueOf(75.0), entity.getVatAmount());
        assertEquals(BigDecimal.valueOf(575.0), entity.getTotalPaidAmount());
        assertNotNull(entity.getExpiryDate());
        assertNotNull(entity.getSubmittedDate());
        assertNotNull(entity.getPaymentDate());
    }

    @Test
    void testMapBayanFromDto_NullChecks() {
        assertDoesNotThrow(() -> {
            ProjectJobMapper.mapBayanFromDto(null, new FFManifestBayanDtl(), 100L);
            ProjectJobMapper.mapBayanFromDto(new ProjectJobBayanDto(), null, 100L);
        });
    }

    @Test
    void testMapChargesFromDto() {
        ProjectJobChargesDto dto = new ProjectJobChargesDto();
        dto.setDetRowId(1L);
        dto.setChargePoid(BigDecimal.valueOf(10L));
        dto.setCurrencyExchange(BigDecimal.valueOf(1.0));
        dto.setQuantity(BigDecimal.valueOf(5.0));
        dto.setBuyingPercharge(BigDecimal.valueOf(50.0));
        dto.setBillingPrecharge(BigDecimal.valueOf(60.0));
        dto.setPaidAtPortPoid(BigDecimal.valueOf(20L));
        dto.setCurrencyCode("USD");
        dto.setPayMode("C"); // payMode is String(1)
        dto.setRcptNoOld("RCPT");
        dto.setChargeCideOld("CHARGE");
        dto.setRcptDaeOld(LocalDate.now());
        dto.setCostInvOld("INV");
        dto.setEquipmentPoid(BigDecimal.valueOf(30L));
        dto.setTotalBuyingCharge(BigDecimal.valueOf(250.0));
        dto.setTotalSellingCharge(BigDecimal.valueOf(300.0));
        dto.setCostInvDtOld(LocalDate.now());
        dto.setRcptIvPoid("40"); // rcptIvPoid is String
        dto.setTotalCostBooked(BigDecimal.valueOf(250.0));
        dto.setDataRowId("50"); // dataRowId is String
        dto.setCostCurrency("USD");
        dto.setCostCurrencyRate(BigDecimal.valueOf(1.0));
        dto.setCostBookRef("REF");
        dto.setPrintGroup("GRP");
        dto.setRemarks("REM");
        dto.setShChargeInv("SH");
        dto.setUnitType("1");
        dto.setTaxPoid(BigDecimal.valueOf(60L));
        dto.setTaxPercentage(BigDecimal.valueOf(5.0));
        dto.setTaxAmount(BigDecimal.valueOf(12.5));
        dto.setTaxInputAmount(BigDecimal.valueOf(12.5));
        dto.setCnRefDocId("CNID");
        dto.setCnRefDocPoid("70"); // cnRefDocPoid is String
        dto.setCnRefDetRowId("80"); // cnRefDetRowId is String
        dto.setCnIssueInvoice("Y");
        dto.setHouseBlPoid(BigDecimal.valueOf(90L));
        dto.setSupplierPoid(BigDecimal.valueOf(100L));
        dto.setChargeBasis("BASIS");
        dto.setEnteryLocation("LOC");

        FFManifestChargesDtl entity = new FFManifestChargesDtl();
        ProjectJobMapper.mapChargesFromDto(dto, entity, 100L);

        assertEquals(100L, entity.getTransactionPoid());
        assertEquals(1L, entity.getDetRowId());
        assertEquals(BigDecimal.valueOf(10L), entity.getChargePoid());
        assertNotNull(entity.getRcptDaeOld());
        assertNotNull(entity.getCostInvDtOld());
        assertNotNull(entity.getUnitType());
    }

    @Test
    void testMapChargesFromDto_NullChecks() {
        assertDoesNotThrow(() -> {
            ProjectJobMapper.mapChargesFromDto(null, new FFManifestChargesDtl(), 100L);
            ProjectJobMapper.mapChargesFromDto(new ProjectJobChargesDto(), null, 100L);
        });
    }

    @Test
    void testMapContainerFromDto() {
        ProjectJobContainerDto dto = new ProjectJobContainerDto();
        dto.setDetRowId(1L);
        dto.setContainerNo("CONT123");
        dto.setEquipmentShipperOwn("Y");
        dto.setCargoDescription("DESC");
        dto.setContainerSealNo("SEAL");
        dto.setContainerIsoCode("ISO");
        dto.setContainerTypePoid(10L); // Correct name in DTO
        dto.setContainerSize("20ft");
        dto.setQuantity(BigDecimal.valueOf(5.0));
        dto.setGrsVolume(BigDecimal.valueOf(50.0));
        dto.setGrsWeight(BigDecimal.valueOf(100.0));
        dto.setNetVolume(BigDecimal.valueOf(45.0));
        dto.setNetWeight(BigDecimal.valueOf(90.0));
        dto.setTareWeight(BigDecimal.valueOf(10.0));
        dto.setNoOfPacks(BigDecimal.valueOf(5.0));
        dto.setPackUnit("PCS");
        dto.setComodityPoid(20L);
        dto.setDestinationPortPoid(30L);
        dto.setImo("IMO");
        dto.setOogL("10"); 
        dto.setOogB(BigDecimal.valueOf(5.0));
        dto.setOogH("2");
        dto.setRefferTemp("TEMP");
        dto.setRefferHum("HUM");
        dto.setRefferVent("VENT");
        dto.setSealNo("SEAL2");
        dto.setUnloadDate(LocalDateTime.now());
        dto.setCfsNote("CFS");
        dto.setDamageNote("DAMAGE");
        dto.setCargoCollectionDate(LocalDateTime.now());
        dto.setTruckDriverDetails("DRIVER");
        dto.setIsImco("Y");
        dto.setImcoClassType("TYPE");
        dto.setImcoClassActual("ACTUAL");
        dto.setDeliveryDate(LocalDateTime.now());
        dto.setDetention("D");
        dto.setDocStatus("STATUS");
        dto.setRemarks("REMARKS");

        FFManifestContainerDtl entity = new FFManifestContainerDtl();
        ProjectJobMapper.mapContainerFromDto(dto, entity, 100L);

        assertEquals(100L, entity.getTransactionPoid());
        assertEquals(1L, entity.getDetRowId());
        assertEquals(BigDecimal.valueOf(10L), entity.getConatinerTypePoid());
        assertEquals(BigDecimal.valueOf(5.0), entity.getQuantity());
        assertEquals("10", entity.getOogL());
        assertEquals(BigDecimal.valueOf(5.0), entity.getOogB());
        assertEquals("2", entity.getOogH());
    }

    @Test
    void testMapContainerFromDto_NullChecks() {
        assertDoesNotThrow(() -> {
            ProjectJobMapper.mapContainerFromDto(null, new FFManifestContainerDtl(), 100L);
            ProjectJobMapper.mapContainerFromDto(new ProjectJobContainerDto(), null, 100L);
        });
    }

    @Test
    void testMapTruckFromDto() {
        ProjectJobTruckDto dto = new ProjectJobTruckDto();
        dto.setDetRowId(1L);
        dto.setBlAwbNumber("BL123");
        dto.setBayanNumber("BAYAN123");
        dto.setBayanCode("CODE");
        dto.setEta(LocalDateTime.now());
        dto.setDutyAmount(new BigDecimal("100"));
        dto.setVatAmount(new BigDecimal("15"));
        dto.setTotalPaidAmount(new BigDecimal("115"));
        dto.setExpiryDate(LocalDateTime.now());
        dto.setSubmittedDate(LocalDateTime.now());
        dto.setPaymentDate(LocalDateTime.now());
        dto.setTruckNumber("TRK123");
        dto.setDocumentStatus("STATUS");

        FFManifestTruckDtl entity = new FFManifestTruckDtl();
        ProjectJobMapper.mapTruckFromDto(dto, entity, 100L);

        assertEquals(100L, entity.getTransactionPoid());
        assertEquals(1L, entity.getDetRowId());
        assertEquals("TRK123", entity.getTruckNumber());
    }

    @Test
    void testMapTruckFromDto_NullChecks() {
        assertDoesNotThrow(() -> {
            ProjectJobMapper.mapTruckFromDto(null, new FFManifestTruckDtl(), 100L);
            ProjectJobMapper.mapTruckFromDto(new ProjectJobTruckDto(), null, 100L);
        });
    }

    @Test
    void testToAirPkgDto() {
        FFManifestAirPkgDtl entity = new FFManifestAirPkgDtl();
        entity.setDetRowId(1L);
        entity.setNoOfPacks(10L);
        entity.setPackUnit("PCS");
        entity.setTotalWeight(BigDecimal.valueOf(100.0));
        entity.setTotalVolume(BigDecimal.valueOf(50.0));
        entity.setLength(BigDecimal.valueOf(10.0));
        entity.setWidth(BigDecimal.valueOf(5.0));
        entity.setHeight(2L);
        entity.setImcoClassUnno("1234");
        entity.setProperShippingName("TEST");
        entity.setImcoClassDivision("1.1");
        entity.setPackingGrouping("I");
        entity.setQuantityPackingType("TYPE");
        entity.setPackingInst("INST");
        entity.setAuthorisation("AUTH");
        entity.setDescription("DESC");
        entity.setAppointmentDate(LocalDateTime.now());
        entity.setDeliveryDate(LocalDateTime.now());
        entity.setDetention("DET");
        entity.setDocStatus("STATUS");
        entity.setRemarks("REMARKS");
        entity.setChargeableWeight(BigDecimal.valueOf(110.0));

        ProjectJobAirPkgDto dto = new ProjectJobAirPkgDto();
        ProjectJobMapper.toAirPkgDto(entity, dto);

        assertEquals(1L, dto.getDetRowId());
        assertEquals(10L, dto.getNoOfPacks());
    }

    @Test
    void testToAirPkgDto_NullEntity() {
        ProjectJobMapper.toAirPkgDto(null, new ProjectJobAirPkgDto());
    }

    @Test
    void testToBayanDto() {
        FFManifestBayanDtl entity = new FFManifestBayanDtl();
        entity.setDetRowId(1L);
        entity.setBlAwbNumber("BL123");
        entity.setBayanNumber("BAYAN123");
        entity.setBayanMode("MODE");
        entity.setDutyAmount(new BigDecimal("100"));
        entity.setVatAmount(new BigDecimal("15"));
        entity.setTotalPaidAmount(new BigDecimal("115"));
        entity.setExpiryDate(LocalDateTime.now());
        entity.setSubmittedDate(LocalDateTime.now());
        entity.setPaymentDate(LocalDateTime.now());

        ProjectJobBayanDto dto = new ProjectJobBayanDto();
        ProjectJobMapper.toBayanDto(entity, dto);

        assertEquals(1L, dto.getDetRowId());
        assertEquals("BL123", dto.getBlAwbNumber());
    }

    @Test
    void testToBayanDto_NullEntity() {
        ProjectJobMapper.toBayanDto(null, new ProjectJobBayanDto());
    }

    @Test
    void testToChargesDto() {
        FFManifestChargesDtl entity = new FFManifestChargesDtl();
        entity.setDetRowId(1L);
        entity.setChargePoid(BigDecimal.valueOf(10L));
        entity.setCurrencyExchange(new BigDecimal("1.0"));
        entity.setQuantity(new BigDecimal("5.0"));
        entity.setBuyingPercharge(new BigDecimal("50.0"));
        entity.setBillingPrecharge(new BigDecimal("60.0"));
        entity.setCurrencyCode("USD");
        entity.setPayMode("CASH");
        entity.setTotalBuyingCharge(new BigDecimal("250.0"));
        entity.setTotalSellingCharge(new BigDecimal("300.0"));
        entity.setRemarks("REM");

        ProjectJobChargesDto dto = new ProjectJobChargesDto();
        ProjectJobMapper.toChargesDto(entity, dto);

        assertEquals(1L, dto.getDetRowId());
        assertEquals(BigDecimal.valueOf(10L), dto.getChargePoid());
    }

    @Test
    void testToChargesDto_NullEntity() {
        ProjectJobMapper.toChargesDto(null, new ProjectJobChargesDto());
    }

    @Test
    void testToContainerDto() {
        FFManifestContainerDtl entity = new FFManifestContainerDtl();
        entity.setDetRowId(1L);
        entity.setContainerNo("CONT123");
        entity.setCargoDescription("DESC");
        entity.setContainerSealNo("SEAL123");
        entity.setContainerIsoCode("ISO");
        entity.setContainerSize("20FT");
        entity.setQuantity(new BigDecimal("1.0"));
        entity.setGrsVolume(BigDecimal.valueOf(100.0));
        entity.setGrsWeight(BigDecimal.valueOf(200.0));
        entity.setRemarks("REM");

        ProjectJobContainerDto dto = new ProjectJobContainerDto();
        ProjectJobMapper.toContainerDto(entity, dto);

        assertEquals(1L, dto.getDetRowId());
        assertEquals("CONT123", dto.getContainerNo());
    }

    @Test
    void testToContainerDto_NullEntity() {
        ProjectJobMapper.toContainerDto(null, new ProjectJobContainerDto());
    }

    @Test
    void testToTruckDto() {
        FFManifestTruckDtl entity = new FFManifestTruckDtl();
        entity.setDetRowId(1L);
        entity.setBlAwbNumber("BL123");
        entity.setBayanNumber("BAYAN123");
        entity.setBayanCode("CODE");
        entity.setEta(LocalDateTime.now());
        entity.setDutyAmount(new BigDecimal("100"));
        entity.setVatAmount(new BigDecimal("15"));
        entity.setTotalPaidAmount(new BigDecimal("115"));
        entity.setExpiryDate(LocalDateTime.now());
        entity.setSubmittedDate(LocalDateTime.now());
        entity.setPaymentDate(LocalDateTime.now());
        entity.setDocumentStatus("STATUS");
        entity.setCreatedBy("USER");
        entity.setCreatedDate(LocalDateTime.now());
        entity.setLastModifiedBy("USER");
        entity.setLastModifiedDate(LocalDateTime.now());
        entity.setTruckNumber("TRUCK123");

        ProjectJobTruckDto dto = new ProjectJobTruckDto();
        ProjectJobMapper.toTruckDto(entity, dto);

        assertEquals(1L, dto.getDetRowId());
        assertEquals("TRUCK123", dto.getTruckNumber());
    }

    @Test
    void testToTruckDto_NullEntity() {
        ProjectJobMapper.toTruckDto(null, new ProjectJobTruckDto());
    }

    @Test
    void testMapHdrFromDto() {
        ProjectJobRequest dto = new ProjectJobRequest();
        dto.setTransactionDate(LocalDate.now());
        dto.setFfJobNo("JOB123");
        dto.setFfJobType("TYPE");
        dto.setLinePoid(BigDecimal.valueOf(10L));
        dto.setQuoatationPoid(BigDecimal.valueOf(20L));
        dto.setPrincipalPoid(BigDecimal.valueOf(30L));
        dto.setMasterBlNo("MBL123");
        dto.setHouseBlNo("HBL123");
        dto.setBlStatus("STATUS");
        dto.setWorkExtensionJobNo("WEX123");
        dto.setBookedBy("USER");
        dto.setFreightFlag("PP");
        dto.setConsignmentType("FCL");
        dto.setSalesmanPoid(BigDecimal.valueOf(40L));
        dto.setAgentPoid(BigDecimal.valueOf(50L));
        dto.setAgentAcctNo("ACCT");
        dto.setAgentIataNo("IATA");
        dto.setShedNo("SHED");
        dto.setJobStatus("OPEN");
        dto.setJobClosedBy("USER2");
        dto.setJobClosedDate(LocalDateTime.now());
        dto.setVoyagePoid(BigDecimal.valueOf(60L));
        dto.setMotherVslVoyageNo("VOY123");
        dto.setMotherVslName("VESSEL");
        dto.setMotherVslSailDate(LocalDateTime.now());
        dto.setMotherVslEta(LocalDateTime.now());
        dto.setMotherVslLoadPortPoid(BigDecimal.valueOf(70L));
        dto.setMotherVslUnloadPortPoid(BigDecimal.valueOf(80L));
        dto.setMotherVslTranshipPortPoid(BigDecimal.valueOf(90L));
        dto.setFeederVoyageNo("FVOY");
        dto.setFeederVslName("FVSL");
        dto.setFeederVslSailDate(LocalDateTime.now());
        dto.setFeederVslEta(LocalDateTime.now());
        dto.setFeederVslArrivalDate(LocalDateTime.now());
        dto.setFeederLoadportPoid(BigDecimal.valueOf(100L));
        dto.setFeederUnloadportPoid(BigDecimal.valueOf(110L));
        dto.setFlightNo("FL123");
        dto.setFlightDate(LocalDateTime.now());
        dto.setAwportOfLoad("LOAD");
        dto.setAwportOfUnload("UNLOAD");
        dto.setShipperPoid(BigDecimal.valueOf(120L));
        dto.setShipperAddressPoid(BigDecimal.valueOf(130L));
        dto.setConsigneePoid(BigDecimal.valueOf(140L));
        dto.setConsigneeAddressPoid(BigDecimal.valueOf(150L));
        dto.setNotifyPoid1(BigDecimal.valueOf(160L));
        dto.setNotifyAddressPoid1(BigDecimal.valueOf(170L));
        dto.setNotifyPoid2(BigDecimal.valueOf(180L));
        dto.setNotifyAddressPoid2(BigDecimal.valueOf(190L));
        dto.setCanRequireToSent("Y");
        dto.setComodityPoid(BigDecimal.valueOf(200L));
        dto.setCargoDescription("DESC");
        dto.setMarkNumbers("MARKS");
        dto.setLpoNo("LPO");
        dto.setLpoDate(LocalDateTime.now());
        dto.setTotalVolume(BigDecimal.valueOf(10.0));
        dto.setTotalNetVolume(BigDecimal.valueOf(9.0));
        dto.setTotalWeight(BigDecimal.valueOf(100.0));
        dto.setTotalNetWeight(BigDecimal.valueOf(90.0));
        dto.setWeightUnit(BigDecimal.valueOf(1L));
        dto.setUnitPack("PCS");
        dto.setTotalNoOfPacks(BigDecimal.valueOf(50.0));
        dto.setChargableWeight(BigDecimal.valueOf(110.0));
        dto.setNoOfPackBooked(BigDecimal.valueOf(50.0));
        dto.setNoOfPackArrived(BigDecimal.valueOf(50.0));
        dto.setHandlingInfo("HANDLING");
        dto.setOtherDetails("OTHER");
        dto.setCustomsDeclarationNo("CUSTOMS");
        dto.setBillingTo("BILLTO");
        dto.setMasterBlWeight(BigDecimal.valueOf(100.0));
        dto.setMasterBlCurrency("USD");
        dto.setTotalCharges(BigDecimal.valueOf(1000.0));
        dto.setCreatedBy("USER");
        dto.setCreatedDate(LocalDateTime.now());
        dto.setLastModifiedBy("USER");
        dto.setLatModifiedDate(LocalDateTime.now());
        dto.setAirArrivalport("ARR");
        dto.setAirDeparturePort("DEP");
        dto.setCarrierCode("CARRIER");
        dto.setAgentDetails("DETAILS");
        dto.setRateChanges(BigDecimal.valueOf(10.0));
        dto.setAgentCharges(BigDecimal.valueOf(20.0));
        dto.setFlightNo2("FL456");
        dto.setFlightDate2(LocalDateTime.now());
        dto.setPriSupCodeOld("SUP");
        dto.setShiprCngCodeOld("CNG");
        dto.setFfBladingNo("BL123");
        dto.setCfInvnoOld("INV123");
        dto.setCurrentDoNo("DO123");
        dto.setDocRef("REF123");
        dto.setDeleted("N");
        dto.setReleasedType("TYPE");
        dto.setRelasedSeqNo(BigDecimal.valueOf(1L));
        dto.setReleasedGrantBy("USERB");
        dto.setReleasedGrantDate("2023-10-10");
        dto.setReleasedGrantReason("REASON");
        dto.setFirstCarrier("FIRST");
        dto.setAccountInfo("ACCT");
        dto.setCanPrinted("N");
        dto.setDoPrinted("N");
        dto.setMablPrinted("N");
        dto.setConsigneManual("CONSIGNEE");
        dto.setNotifyManual("NOTIFY");
        dto.setShipperManual("SHIPPER");
        dto.setOfoqMnfRef("OFOQ");
        dto.setPrincipalAddrPoid(BigDecimal.valueOf(210L));
        dto.setShowNotifyCan("Y");
        dto.setCanSentTo("SENTTO");
        dto.setCanPrintedBy("PRINTEDBY");
        dto.setCanPrintedDt(LocalDateTime.now());
        dto.setFfShJob("SHJOB");
        dto.setBillToCustomerPoid(BigDecimal.valueOf(220L));
        dto.setPrincipalManual("PRINCIPAL");
        dto.setMotherVslFinalDelv("FINAL");
        dto.setProjectRef("PROJREF");
        dto.setRecievedFrom("FROM");
        dto.setDeliveryTo("TO");
        dto.setProjectPoid(BigDecimal.valueOf(230L));
        dto.setBlIssueDate(LocalDateTime.now());
        dto.setAirTransPort("TRANS1");
        dto.setAirTransPort2("TRANS2");
        dto.setSecondCarrier("SECOND");
        dto.setThirdCarrier("THIRD");
        dto.setContainerVolume("VOL");
        dto.setRateClass("CLASS");
        dto.setKgLb("KG");
        dto.setFcrDofCargoRcpt(LocalDateTime.now());
        dto.setFcrCargoRemarks("FCR");
        dto.setFcrSuplierShipperRef("SHIPREF");
        dto.setCoLoaderAgent(BigDecimal.valueOf(240L));
        dto.setIncoTerm("CIF");
        dto.setDocumentStatus("DOCSTAT");
        dto.setSpecialDocumentRemarks("SPECREM");
        dto.setDeliveryDateFrom(LocalDateTime.now());
        dto.setDeliveryDateTo(LocalDateTime.now());
        dto.setCustomsClearanceInvoved("Y");
        dto.setRoadTransport("N");
        dto.setDoFreedays(5);
        dto.setBayanNo("BNO");
        dto.setBayanType("BTYPE");
        dto.setBayanAmount(BigDecimal.valueOf(100.0));
        dto.setBayanExpiry(LocalDateTime.now());
        dto.setBayanStatus("BSTAT");
        dto.setPolicyNo("POL");
        dto.setShipperManualEdi("SHEDI");
        dto.setConsigneManualEdi("CONSEDI");
        dto.setPassengerWithCargo("N");
        dto.setRadioAction("N");
        dto.setPerformaPrintUSD("N");
        dto.setGlobalTracking("N");
        dto.setHouseBlNo2("HBL2");
        dto.setIsMainJob("Y");
        dto.setMainTransactionPoid(BigDecimal.valueOf(250L));
        dto.setHoldDo("N");
        dto.setHoldDoUser("USERH");
        dto.setShipmentMode("SHIPMODE");
        dto.setTransportationMode("TRANSMODE");
        dto.setTruckTransportFrom("FROMT");
        dto.setTruckTransportTo("TOT");

        FFManifestHdr entity = new FFManifestHdr();
        ProjectJobMapper.mapHdrFromDto(dto, entity);

        assertEquals("JOB123", entity.getFfJobNo());
        assertEquals("TYPE", entity.getFfJobType());
    }

    @Test
    void testMapHdrFromDto_NullChecks() {
        assertDoesNotThrow(() -> {
            ProjectJobMapper.mapHdrFromDto(null, new FFManifestHdr());
            ProjectJobMapper.mapHdrFromDto(new ProjectJobRequest(), null);
        });
    }

    @Test
    void testToHdrDto() {
        FFManifestHdr entity = new FFManifestHdr();
        entity.setTransactionPoid(100L);
        entity.setTransactionDate(LocalDate.now());
        entity.setGroupPoid(1L);
        entity.setCompanyPoid(2L);
        entity.setFfJobNo("JOB1");
        entity.setFfJobType("T1");
        entity.setLinePoid(BigDecimal.valueOf(10L));
        entity.setQuoatationPoid(BigDecimal.valueOf(20L));
        entity.setPrincipalPoid(BigDecimal.valueOf(30L));
        entity.setMasterBlNo("MBL");
        entity.setHouseBlNo("HBL");
        entity.setBlStatus("S");
        entity.setWorkExtensionJobNo("EXT");
        entity.setBookedBy("USER");
        entity.setFreightFlag("F");
        entity.setConsignmentType("C");
        entity.setSalesmanPoid(BigDecimal.valueOf(50L));
        entity.setAgentPoid(BigDecimal.valueOf(60L));
        entity.setAgentAcctNo("ACC");
        entity.setAgentIataNo("IATA");
        entity.setShedNo("SHED");
        entity.setJobStatus("OPEN");
        entity.setJobClosedBy("USER");
        entity.setJobClosedDate(LocalDateTime.now());
        entity.setVoyagePoid(BigDecimal.valueOf(70L));
        entity.setMotherVslVoyageNo("MVV");
        entity.setMotherVslName("MVN");
        entity.setMotherVslSailDate(LocalDateTime.now());
        entity.setMotherVslEta(LocalDateTime.now());
        entity.setMotherVslLoadPortPoid(BigDecimal.valueOf(80L));
        entity.setMotherVslUnloadPortPoid(BigDecimal.valueOf(90L));
        entity.setMotherVslTranshipPortPoid(BigDecimal.valueOf(100L));
        entity.setFeederVoyageNo("FVN");
        entity.setFeederVslName("FVN");
        entity.setFeederVslSailDate(LocalDateTime.now());
        entity.setFeederVslEta(LocalDateTime.now());
        entity.setFeederVslArrivalDate(LocalDateTime.now());
        entity.setFeederLoadportPoid(BigDecimal.valueOf(110L));
        entity.setFeederUnloadportPoid(BigDecimal.valueOf(120L));
        entity.setFlightNo("FLN");
        entity.setFlightDate(LocalDateTime.now());
        entity.setAwportOfLoad("130");
        entity.setAwportOfUnload("140");
        entity.setShipperPoid(BigDecimal.valueOf(150L));
        entity.setShipperAddressPoid(BigDecimal.valueOf(160L));
        entity.setConsigneePoid(BigDecimal.valueOf(170L));
        entity.setConsigneeAddressPoid(BigDecimal.valueOf(180L));
        entity.setNotifyPoid1(BigDecimal.valueOf(190L));
        entity.setNotifyAddressPoid1(BigDecimal.valueOf(200L));
        entity.setNotifyPoid2(BigDecimal.valueOf(210L));
        entity.setNotifyAddressPoid2(BigDecimal.valueOf(220L));
        entity.setCanRequireToSent("Y");
        entity.setComodityPoid(BigDecimal.valueOf(230L));
        entity.setCargoDescription("DESC");
        entity.setMarkNumbers("MARKS");
        entity.setLpoNo("LPO");
        entity.setLpoDate(LocalDateTime.now());
        entity.setTotalVolume(BigDecimal.valueOf(100.0));
        entity.setTotalNetVolume(BigDecimal.valueOf(90.0));
        entity.setTotalWeight(BigDecimal.valueOf(200.0));
        entity.setTotalNetWeight(BigDecimal.valueOf(180.0));
        entity.setWeightUnit(BigDecimal.valueOf(1L));
        entity.setUnitPack("P");
        entity.setTotalNoOfPacks(BigDecimal.valueOf(10.0));
        entity.setChargableWeight(BigDecimal.valueOf(210.0));
        entity.setNoOfPackBooked(BigDecimal.valueOf(10.0));
        entity.setNoOfPackArrived(BigDecimal.valueOf(10.0));
        entity.setHandlingInfo("H");
        entity.setOtherDetails("O");
        entity.setCustomsDeclarationNo("CDN");
        entity.setBillingTo("BT");
        entity.setMasterBlWeight(BigDecimal.valueOf(220.0));
        entity.setMasterBlCurrency("USD");
        entity.setTotalCharges(BigDecimal.TEN);
        entity.setAirArrivalport("240");
        entity.setAirDeparturePort("250");
        entity.setCarrierCode("CC");
        entity.setAgentDetails("AD");
        entity.setRateChanges(BigDecimal.TEN);
        entity.setAgentCharges(BigDecimal.ONE);
        entity.setFlightNo2("F2");
        entity.setFlightDate2(LocalDateTime.now());
        entity.setPriSupCodeOld("PSCO");
        entity.setShiprCngCodeOld("SCCO");
        entity.setFfBladingNo("FFBN");
        entity.setCfInvnoOld("CIO");
        entity.setCurrentDoNo("CDN");
        entity.setDocRef("REF");
        entity.setDeleted("N");
        entity.setReleasedType("RT");
        entity.setRelasedSeqNo(BigDecimal.valueOf(1L));
        entity.setReleasedGrantBy("RGB");
        entity.setReleasedGrantDate("2023-10-10");
        entity.setReleasedGrantReason("RGR");
        entity.setFirstCarrier("FC");
        entity.setAccountInfo("AI");
        entity.setCanPrinted("Y");
        entity.setDoPrinted("Y");
        entity.setMablPrinted("Y");
        entity.setConsigneManual("CM");
        entity.setNotifyManual("NM");
        entity.setShipperManual("SM");
        entity.setOfoqMnfRef("OMR");
        entity.setPrincipalAddrPoid(BigDecimal.valueOf(260L));
        entity.setShowNotifyCan("Y");
        entity.setCanSentTo("CST");
        entity.setCanPrintedBy("CPB");
        entity.setCanPrintedDt(LocalDateTime.now());
        entity.setFfShJob("FSJ");
        entity.setBillToCustomerPoid(BigDecimal.valueOf(270L));
        entity.setPrincipalManual("PM");
        entity.setMotherVslFinalDelv("MVFD");
        entity.setProjectRef("PR");
        entity.setRecievedFrom("RF");
        entity.setDeliveryTo("DT");
        entity.setProjectPoid(BigDecimal.valueOf(280L));
        entity.setBlIssueDate(LocalDateTime.now());
        entity.setAirTransPort("AT");
        entity.setAirTransPort2("AT2");
        entity.setSecondCarrier("SC");
        entity.setThirdCarrier("TC");
        entity.setContainerVolume("110.0");
        entity.setRateClass("RC");
        entity.setKgLb("KL");
        entity.setFcrDofCargoRcpt(LocalDateTime.now());
        entity.setFcrCargoRemarks("FCR");
        entity.setFcrSuplierShipperRef("FSSR");
        entity.setCoLoaderAgent(BigDecimal.valueOf(290L));
        entity.setIncoTerm("IT");
        entity.setDocumentStatus("DS");
        entity.setSpecialDocumentRemarks("SDR");
        entity.setDeliveryDateFrom(LocalDateTime.now());
        entity.setDeliveryḌateTo(LocalDateTime.now());
        entity.setCustomsClearanceInvoved("Y");
        entity.setRoadTransport("Y");
        entity.setDoFreedays(5);
        entity.setBayanNo("BN");
        entity.setBayanType("BT");
        entity.setBayanAmount(BigDecimal.TEN);
        entity.setBayanExpiry(LocalDateTime.now());
        entity.setBayanStatus("BS");
        entity.setPolicyNo("PN");
        entity.setShipperManualEdi("SME");
        entity.setConsigneManualEdi("CME");
        entity.setPassengerWithCargo("N");
        entity.setRadioAction("RA");
        entity.setPerformaPrintUSD("Y");
        entity.setGlobalTracking("Y");
        entity.setHouseBlNo2("HBN2");
        entity.setIsMainJob("Y");
        entity.setMainTransactionPoid(BigDecimal.valueOf(290L));
        entity.setHoldDo("N");
        entity.setHoldDoUser("HDU");
        entity.setShipmentMode("SM");
        entity.setTransportationMode("TM");
        entity.setTruckTransportFrom("TTF");
        entity.setTruckTransportTo("TTT");

        FFManifestHdrDtoResponse dto = new FFManifestHdrDtoResponse();
        ProjectJobMapper.toHdrDto(entity, dto);

        assertEquals(100L, dto.getTransactionPoid());
        assertEquals("JOB1", dto.getFfJobNo());
    }

    @Test
    void testToHdrDto_NullEntity() {
        ProjectJobMapper.toHdrDto(null, new FFManifestHdrDtoResponse());
    }

    @Test
    void testMapChargesFromDto_EmptyDto() {
        ProjectJobChargesDto dto = new ProjectJobChargesDto();
        FFManifestChargesDtl entity = new FFManifestChargesDtl();
        ProjectJobMapper.mapChargesFromDto(dto, entity, 100L);
        assertNull(entity.getRcptDaeOld());
        assertNull(entity.getCostInvDtOld());
        assertNull(entity.getUnitType());
    }

    @Test
    void testMapContainerFromDto_EmptyDto() {
        ProjectJobContainerDto dto = new ProjectJobContainerDto();
        FFManifestContainerDtl entity = new FFManifestContainerDtl();
        ProjectJobMapper.mapContainerFromDto(dto, entity, 100L);
        assertNull(entity.getConatinerTypePoid());
        assertNull(entity.getComodityPoid());
        assertNull(entity.getDestinationPortPoid());
    }

    @Test
    void testConstructorIsPrivate() throws Exception {
        java.lang.reflect.Constructor<ProjectJobMapper> constructor = ProjectJobMapper.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}