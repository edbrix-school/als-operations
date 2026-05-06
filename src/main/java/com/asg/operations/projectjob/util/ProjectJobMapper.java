package com.asg.operations.projectjob.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.asg.common.lib.dto.LovGetListDto;
import com.asg.common.lib.service.LovDataService;
import com.asg.common.lib.utility.DateUtil;
import com.asg.operations.projectjob.dto.FFManifestHdrDto;
import com.asg.operations.projectjob.dto.FFManifestHdrDtoResponse;
import com.asg.operations.projectjob.dto.ProjectJobAirPkgDto;
import com.asg.operations.projectjob.dto.ProjectJobBayanDto;
import com.asg.operations.projectjob.dto.ProjectJobChargesDto;
import com.asg.operations.projectjob.dto.ProjectJobContainerDto;
import com.asg.operations.projectjob.dto.ProjectJobTruckDto;
import com.asg.operations.projectjob.entity.FFManifestAirPkgDtl;
import com.asg.operations.projectjob.entity.FFManifestBayanDtl;
import com.asg.operations.projectjob.entity.FFManifestChargesDtl;
import com.asg.operations.projectjob.entity.FFManifestContainerDtl;
import com.asg.operations.projectjob.entity.FFManifestHdr;
import com.asg.operations.projectjob.entity.FFManifestTruckDtl;
import lombok.RequiredArgsConstructor;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectJobMapper {

    private final LovDataService lovDataService;

    public void mapAirPkgFromDto(ProjectJobAirPkgDto dto, FFManifestAirPkgDtl entity, Long transactionPoid) {

        if (dto == null)
            return;

        entity.setTransactionPoid(transactionPoid);
        entity.setDetRowId(dto.getDetRowId());

        entity.setNoOfPacks(Optional.ofNullable(dto.getNoOfPacks()).orElse(0L));
        entity.setPackUnit(Optional.ofNullable(dto.getPackUnit()).orElse(" "));

        entity.setTotalWeight(Optional.ofNullable(dto.getTotalWeight()).orElse(BigDecimal.ZERO));
        entity.setTotalVolume(dto.getTotalVolume());

        entity.setLength(Optional.ofNullable(dto.getLength()).orElse(BigDecimal.ZERO));
        entity.setWidth(Optional.ofNullable(dto.getWidth()).orElse(BigDecimal.ZERO));
        entity.setHeight(Optional.ofNullable(dto.getHeight()).orElse(0L));

        entity.setImcoClassUnno(dto.getImcoClassUnno());
        entity.setProperShippingName(dto.getProperShippingName());
        entity.setImcoClassDivision(dto.getImcoClassDivision());
        entity.setPackingGrouping(dto.getPackingGrouping());
        entity.setQuantityPackingType(dto.getQuantityPackingType());
        entity.setPackingInst(dto.getPackingInst());
        entity.setAuthorisation(dto.getAuthorisation());
        entity.setDescription(dto.getDescription());

        entity.setAppointmentDate(dto.getAppointmentDate());
        entity.setDeliveryDate(dto.getDeliveryDate());

        entity.setDetention(dto.getDetention());
        entity.setDocStatus(dto.getDocStatus());
        entity.setRemarks(dto.getRemarks());

        entity.setChargeableWeight(dto.getChargeableWeight());
    }

    public void mapBayanFromDto(ProjectJobBayanDto dto, FFManifestBayanDtl entity, Long transactionPoid) {

        if (dto == null || entity == null)
            return;

        entity.setTransactionPoid(transactionPoid);
        entity.setDetRowId(dto.getDetRowId());

        entity.setBlAwbNumber(dto.getBlAwbNumber());
        entity.setBayanNumber(dto.getBayanNumber());
        entity.setBayanMode(dto.getBayanMode());

        entity.setDutyAmount(dto.getDutyAmount());
        entity.setVatAmount(dto.getVatAmount());
        entity.setTotalPaidAmount(dto.getTotalPaidAmount());

        entity.setExpiryDate(dto.getExpiryDate());
        entity.setSubmittedDate(dto.getSubmittedDate());
        entity.setPaymentDate(dto.getPaymentDate());
    }

    public void mapChargesFromDto(ProjectJobChargesDto dto, FFManifestChargesDtl entity, Long transactionPoid) {

        if (dto == null || entity == null)
            return;

        entity.setTransactionPoid(transactionPoid);
        entity.setDetRowId(dto.getDetRowId());

        entity.setChargePoid(dto.getChargePoid());
        entity.setCurrencyExchange(dto.getCurrencyExchange());
        entity.setQuantity(dto.getQuantity());
        entity.setBuyingPercharge(dto.getBuyingPercharge());
        entity.setBillingPrecharge(dto.getBillingPrecharge());
        entity.setPaidAtPortPoid(dto.getPaidAtPortPoid());

        entity.setCurrencyCode(dto.getCurrencyCode());
        entity.setPayMode(dto.getPayMode());
        entity.setRcptNoOld(dto.getRcptNoOld());
        entity.setChargeCodeOld(dto.getChargeCodeOld());
        entity.setRcptDaeOld(dto.getRcptDaeOld());
        entity.setCostInvOld(dto.getCostInvOld());

        entity.setEquipmentPoid(dto.getEquipmentPoid());
        entity.setTotalBuyingCharge(dto.getTotalBuyingCharge());
        entity.setTotalSellingCharge(dto.getTotalSellingCharge());

        entity.setCostInvDtOld(dto.getCostInvDtOld());
        entity.setRcptIvPoid(dto.getRcptIvPoid());
        entity.setTotalCostBooked(dto.getTotalCostBooked());

        entity.setDataRowId(dto.getDataRowId());
        entity.setCostCurrency(dto.getCostCurrency());
        entity.setCostCurrencyRate(dto.getCostCurrencyRate());
        entity.setCostBookRef(dto.getCostBookRef());
        entity.setPrintGroup(dto.getPrintGroup());
        entity.setRemarks(dto.getRemarks());
        entity.setShChargeInv(dto.getShChargeInv());

        entity.setUnitType(dto.getUnitType());

        entity.setTaxPoid(dto.getTaxPoid());
        entity.setTaxPercentage(dto.getTaxPercentage());
        entity.setTaxAmount(dto.getTaxAmount());
        entity.setTaxInputAmount(dto.getTaxInputAmount());

        entity.setCnRefDocId(dto.getCnRefDocId());
        entity.setCnRefDocPoid(dto.getCnRefDocPoid());
        entity.setCnRefDetRowId(dto.getCnRefDetRowId());
        entity.setCnIssueInvoice(dto.getCnIssueInvoice());

        entity.setHouseBlPoid(dto.getHouseBlPoid());
        entity.setSupplierPoid(dto.getSupplierPoid());

        entity.setChargeBasis(dto.getChargeBasis());
        entity.setEnteryLocation(dto.getEnteryLocation());
    }

    public void mapContainerFromDto(ProjectJobContainerDto dto, FFManifestContainerDtl entity, Long transactionPoid) {

        if (dto == null || entity == null)
            return;

        entity.setTransactionPoid(transactionPoid);
        entity.setDetRowId(dto.getDetRowId());

        entity.setContainerNo(dto.getContainerNo());
        entity.setEquipmentShipperOwn(dto.getEquipmentShipperOwn());
        entity.setCargoDescription(dto.getCargoDescription());
        entity.setContainerSealNo(dto.getContainerSealNo());
        entity.setContainerIsoCode(dto.getContainerIsoCode());
        entity.setContainerTypePoid(dto.getContainerTypePoid() != null ? dto.getContainerTypePoid(): null);
        entity.setContainerSize(dto.getContainerSize());
        entity.setQuantity(dto.getQuantity());

        entity.setGrsVolume(dto.getGrsVolume());
        entity.setGrsWeight(dto.getGrsWeight());
        entity.setNetVolume(dto.getNetVolume());
        entity.setNetWeight(dto.getNetWeight());
        entity.setTareWeight(dto.getTareWeight());

        entity.setNoOfPacks(dto.getNoOfPacks());
        entity.setPackUnit(dto.getPackUnit());

        entity.setComodityPoid(dto.getComodityPoid() != null ?dto.getComodityPoid() : null);

        entity.setDestinationPortPoid(
                dto.getDestinationPortPoid() != null ? dto.getDestinationPortPoid() : null);

        entity.setImo(dto.getImo());
        entity.setOogL(dto.getOogL());
        entity.setOogB(dto.getOogB());
        entity.setOogH(dto.getOogH());

        entity.setRefferTemp(dto.getRefferTemp());
        entity.setRefferHum(dto.getRefferHum());
        entity.setRefferVent(dto.getRefferVent());

        entity.setSealNo(dto.getSealNo());

        entity.setUnloadDate(dto.getUnloadDate());
        entity.setCargoCollectionDate(dto.getCargoCollectionDate());
        entity.setDeliveryDate(dto.getDeliveryDate());

        entity.setCfsNote(dto.getCfsNote());
        entity.setDamageNote(dto.getDamageNote());
        entity.setTruckDriverDetails(dto.getTruckDriverDetails());

        entity.setIsImco(dto.getIsImco());
        entity.setImcoClassType(dto.getImcoClassType());
        entity.setImcoClassActual(dto.getImcoClassActual());

        entity.setDetention(dto.getDetention());
        entity.setDocStatus(dto.getDocStatus());
        entity.setRemarks(dto.getRemarks());
    }

    public void mapTruckFromDto(ProjectJobTruckDto dto, FFManifestTruckDtl entity, Long transactionPoid) {

        if (dto == null || entity == null)
            return;

        entity.setTransactionPoid(transactionPoid);
        entity.setDetRowId(dto.getDetRowId());

        entity.setBlAwbNumber(dto.getBlAwbNumber());
        entity.setBayanNumber(dto.getBayanNumber());
        entity.setBayanCode(dto.getBayanCode());

        entity.setEta(dto.getEta());

        entity.setDutyAmount(dto.getDutyAmount());
        entity.setVatAmount(dto.getVatAmount());
        entity.setTotalPaidAmount(dto.getTotalPaidAmount());

        entity.setExpiryDate(dto.getExpiryDate());
        entity.setSubmittedDate(dto.getSubmittedDate());
        entity.setPaymentDate(dto.getPaymentDate());

        entity.setDocumentStatus(dto.getDocumentStatus());

        entity.setTruckNumber(dto.getTruckNumber());
    }

    public void toAirPkgDto(FFManifestAirPkgDtl entity, ProjectJobAirPkgDto dto) {

        if (entity == null)
            return;

        dto.setDetRowId(entity.getDetRowId());

        dto.setNoOfPacks(entity.getNoOfPacks());
        dto.setPackUnit(StringUtil.isBlank(entity.getPackUnit())?null:entity.getPackUnit());
        dto.setTotalWeight(entity.getTotalWeight());
        dto.setTotalVolume(entity.getTotalVolume());

        dto.setLength(entity.getLength());
        dto.setWidth(entity.getWidth());
        dto.setHeight(entity.getHeight());

        dto.setImcoClassUnno(entity.getImcoClassUnno());
        dto.setProperShippingName(entity.getProperShippingName());
        dto.setImcoClassDivision(entity.getImcoClassDivision());
        dto.setPackingGrouping(entity.getPackingGrouping());
        dto.setQuantityPackingType(entity.getQuantityPackingType());
        dto.setPackingInst(entity.getPackingInst());
        dto.setAuthorisation(entity.getAuthorisation());
        dto.setDescription(entity.getDescription());

        dto.setAppointmentDate(entity.getAppointmentDate());
        dto.setDeliveryDate(entity.getDeliveryDate());

        dto.setDetention(entity.getDetention());
        dto.setDocStatus(entity.getDocStatus());
        dto.setRemarks(entity.getRemarks());

        dto.setChargeableWeight(entity.getChargeableWeight());

    }

    public void toBayanDto(FFManifestBayanDtl entity, ProjectJobBayanDto dto) {

        if (entity == null)
            return;

        dto.setDetRowId(entity.getDetRowId());

        dto.setBlAwbNumber(entity.getBlAwbNumber());
        dto.setBayanNumber(entity.getBayanNumber());
        dto.setBayanMode(entity.getBayanMode());

        dto.setDutyAmount(entity.getDutyAmount());
        dto.setVatAmount(entity.getVatAmount());
        dto.setTotalPaidAmount(entity.getTotalPaidAmount());

        dto.setExpiryDate(entity.getExpiryDate());
        dto.setSubmittedDate(entity.getSubmittedDate());
        dto.setPaymentDate(entity.getPaymentDate());

    }

    public void toChargesDto(FFManifestChargesDtl entity, ProjectJobChargesDto dto) {

        if (entity == null)
            return;

        dto.setDetRowId(entity.getDetRowId());

        dto.setChargePoid(entity.getChargePoid());
        dto.setChargesLov(getLov(longConvertion(entity.getChargePoid()), "CHARGE_MASTER_FF"));
        dto.setCurrencyExchange(entity.getCurrencyExchange());
        dto.setQuantity(entity.getQuantity());
        dto.setBuyingPercharge(entity.getBuyingPercharge());
        dto.setBillingPrecharge(entity.getBillingPrecharge());
        dto.setPaidAtPortPoid(entity.getPaidAtPortPoid());
        dto.setCurrencyCode(entity.getCurrencyCode());
        dto.setPayMode(entity.getPayMode());
        dto.setRcptNoOld(entity.getRcptNoOld());
        dto.setChargeCodeOld(entity.getChargeCodeOld());
        dto.setRcptDaeOld(entity.getRcptDaeOld());
        dto.setCostInvOld(entity.getCostInvOld());
        dto.setEquipmentPoid(entity.getEquipmentPoid());
        dto.setTotalBuyingCharge(entity.getTotalBuyingCharge());
        dto.setTotalSellingCharge(entity.getTotalSellingCharge());
        dto.setCostInvDtOld(entity.getCostInvDtOld());
        dto.setRcptIvPoid(entity.getRcptIvPoid());
        dto.setTotalCostBooked(entity.getTotalCostBooked());
        dto.setDataRowId(entity.getDataRowId());
        dto.setCostCurrency(entity.getCostCurrency());
        dto.setCostCurrencyRate(entity.getCostCurrencyRate());
        dto.setCostBookRef(entity.getCostBookRef());
        dto.setPrintGroup(entity.getPrintGroup());
        dto.setRemarks(entity.getRemarks());
        dto.setShChargeInv(entity.getShChargeInv());
        dto.setUnitType(entity.getUnitType());
        dto.setTaxPoid(entity.getTaxPoid());
        dto.setTaxPercentage(entity.getTaxPercentage());
        dto.setTaxAmount(entity.getTaxAmount());
        dto.setTaxInputAmount(entity.getTaxInputAmount());
        dto.setCnRefDocId(entity.getCnRefDocId());
        dto.setCnRefDocPoid(entity.getCnRefDocPoid());
        dto.setCnRefDetRowId(entity.getCnRefDetRowId());
        dto.setCnIssueInvoice(entity.getCnIssueInvoice());
        dto.setHouseBlPoid(entity.getHouseBlPoid());
        dto.setSupplierPoid(entity.getSupplierPoid());
        dto.setChargeBasis(entity.getChargeBasis());
        dto.setEnteryLocation(entity.getEnteryLocation());
    }

    public void toContainerDto(FFManifestContainerDtl entity, ProjectJobContainerDto dto) {

        if (entity == null)
            return;

        dto.setDetRowId(entity.getDetRowId());
        dto.setContainerNo(entity.getContainerNo());
        dto.setEquipmentShipperOwn(entity.getEquipmentShipperOwn());
        dto.setCargoDescription(entity.getCargoDescription());
        dto.setContainerSealNo(entity.getContainerSealNo());
        dto.setContainerIsoCode(entity.getContainerIsoCode());
        dto.setContainerTypePoid(entity.getContainerTypePoid());
        dto.setContainerTypeLov(getLov(longConvertion(entity.getContainerTypePoid()),"CONTAINER_TYPE_MASTER_FF"));
        dto.setContainerSize(entity.getContainerSize());
        dto.setQuantity(entity.getQuantity());
        dto.setGrsVolume(entity.getGrsVolume());
        dto.setGrsWeight(entity.getGrsWeight());
        dto.setNetVolume(entity.getNetVolume());
        dto.setNetWeight(entity.getNetWeight());
        dto.setTareWeight(entity.getTareWeight());
        dto.setNoOfPacks(entity.getNoOfPacks());
        dto.setPackUnit(entity.getPackUnit());
        dto.setComodityPoid(entity.getComodityPoid());
        dto.setDestinationPortPoid(entity.getDestinationPortPoid());
        dto.setImo(entity.getImo());
        dto.setOogB(entity.getOogB());
        dto.setOogL(entity.getOogL());
        dto.setOogH(entity.getOogH());
        dto.setRefferHum(entity.getRefferHum());
        dto.setRefferTemp(entity.getRefferTemp());
        dto.setRefferVent(entity.getRefferVent());
        dto.setSealNo(entity.getSealNo());
        dto.setUnloadDate(entity.getUnloadDate());
        dto.setCargoCollectionDate(entity.getCargoCollectionDate());
        dto.setDeliveryDate(entity.getDeliveryDate());
        dto.setCfsNote(entity.getCfsNote());
        dto.setDamageNote(entity.getDamageNote());
        dto.setTruckDriverDetails(entity.getTruckDriverDetails());
        dto.setIsImco(entity.getIsImco());
        dto.setImcoClassType(entity.getImcoClassType());
        dto.setImcoClassActual(entity.getImcoClassActual());
        dto.setDetention(entity.getDetention());
        dto.setDocStatus(entity.getDocStatus());
        dto.setRemarks(entity.getRemarks());
    }

    public void toTruckDto(FFManifestTruckDtl entity, ProjectJobTruckDto dto) {

        if (entity == null)
            return;

        dto.setDetRowId(entity.getDetRowId());

        dto.setBlAwbNumber(entity.getBlAwbNumber());
        dto.setBayanNumber(entity.getBayanNumber());
        dto.setBayanCode(entity.getBayanCode());

        dto.setEta(entity.getEta());

        dto.setDutyAmount(entity.getDutyAmount());
        dto.setVatAmount(entity.getVatAmount());
        dto.setTotalPaidAmount(entity.getTotalPaidAmount());

        dto.setExpiryDate(entity.getExpiryDate());
        dto.setSubmittedDate(entity.getSubmittedDate());
        dto.setPaymentDate(entity.getPaymentDate());

        dto.setDocumentStatus(entity.getDocumentStatus());

        dto.setTruckNumber(entity.getTruckNumber());

    }

    public void mapHdrFromDto(FFManifestHdrDto dto, FFManifestHdr entity) {

        if (dto == null || entity == null)
            return;

        entity.setTransactionDate(Optional.ofNullable(dto.getTransactionDate()).orElse(DateUtil.getCurrentDateInUserTimeZone()));
        entity.setCompanyPoid(dto.getCompanyPoid());

        entity.setFfJobNo(dto.getFfJobNo());
        entity.setFfJobType(dto.getFfJobType());

        entity.setLinePoid(dto.getLinePoid());
        entity.setQuoatationPoid(dto.getQuoatationPoid());
        entity.setPrincipalPoid(dto.getPrincipalPoid());

        entity.setMasterBlNo(dto.getMasterBlNo());
        entity.setHouseBlNo(dto.getHouseBlNo());
        entity.setBlStatus(dto.getBlStatus());
        entity.setWorkExtensionJobNo(dto.getWorkExtensionJobNo());

        entity.setBookedBy(dto.getBookedBy());
        entity.setFreightFlag(dto.getFreightFlag());
        entity.setConsignmentType(dto.getConsignmentType());

        entity.setSalesmanPoid(dto.getSalesmanPoid());
        entity.setAgentPoid(dto.getAgentPoid());
        entity.setAgentAcctNo(dto.getAgentAcctNo());
        entity.setAgentIataNo(dto.getAgentIataNo());

        entity.setShedNo(dto.getShedNo());
        entity.setJobStatus(dto.getJobStatus());
        entity.setJobClosedBy(dto.getJobClosedBy());
        entity.setJobClosedDate(dto.getJobClosedDate());

        entity.setVoyagePoid(dto.getVoyagePoid());
        entity.setMotherVslVoyageNo(dto.getMotherVslVoyageNo());
        entity.setMotherVslName(dto.getMotherVslName());
        entity.setMotherVslSailDate(dto.getMotherVslSailDate());
        entity.setMotherVslEta(dto.getMotherVslEta());

        entity.setMotherVslLoadPortPoid(dto.getMotherVslLoadPortPoid());
        entity.setMotherVslUnloadPortPoid(dto.getMotherVslUnloadPortPoid());
        entity.setMotherVslTranshipPortPoid(dto.getMotherVslTranshipPortPoid());

        entity.setFeederVoyageNo(dto.getFeederVoyageNo());
        entity.setFeederVslName(dto.getFeederVslName());
        entity.setFeederVslSailDate(dto.getFeederVslSailDate());
        entity.setFeederVslEta(dto.getFeederVslEta());
        entity.setFeederVslArrivalDate(dto.getFeederVslArrivalDate());

        entity.setFeederLoadportPoid(dto.getFeederLoadportPoid());
        entity.setFeederUnloadportPoid(dto.getFeederUnloadportPoid());

        entity.setFlightNo(dto.getFlightNo());
        entity.setFlightDate(dto.getFlightDate());

        entity.setAwportOfLoad(dto.getAwportOfLoad());
        entity.setAwportOfUnload(dto.getAwportOfUnload());

        entity.setShipperPoid(dto.getShipperPoid());
        entity.setShipperAddressPoid(dto.getShipperAddressPoid());
        entity.setConsigneePoid(dto.getConsigneePoid());
        entity.setConsigneeAddressPoid(dto.getConsigneeAddressPoid());

        entity.setNotifyPoid1(dto.getNotifyPoid1());
        entity.setNotifyAddressPoid1(dto.getNotifyAddressPoid1());
        entity.setNotifyPoid2(dto.getNotifyPoid2());
        entity.setNotifyAddressPoid2(dto.getNotifyAddressPoid2());

        entity.setCanRequireToSent(dto.getCanRequireToSent());
        entity.setComodityPoid(Optional.ofNullable(dto.getCommodityPoids())
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")))
                .orElse(null));
        entity.setCargoDescription(dto.getCargoDescription());
        entity.setMarkNumbers(dto.getMarkNumbers());

        entity.setLpoNo(dto.getLpoNo());
        entity.setLpoDate(dto.getLpoDate());

        entity.setTotalVolume(dto.getTotalVolume());
        entity.setTotalNetVolume(dto.getTotalNetVolume());
        entity.setTotalWeight(dto.getTotalWeight());
        entity.setTotalNetWeight(dto.getTotalNetWeight());
        entity.setWeightUnit(dto.getWeightUnit());

        entity.setUnitPack(dto.getUnitPack());
        entity.setTotalNoOfPacks(dto.getTotalNoOfPacks());
        entity.setChargableWeight(dto.getChargableWeight());
        entity.setNoOfPackBooked(dto.getNoOfPackBooked());
        entity.setNoOfPackArrived(dto.getNoOfPackArrived());

        entity.setHandlingInfo(dto.getHandlingInfo());
        entity.setOtherDetails(dto.getOtherDetails());
        entity.setCustomsDeclarationNo(dto.getCustomsDeclarationNo());
        entity.setBillingTo(dto.getBillingTo());

        entity.setMasterBlWeight(dto.getMasterBlWeight());
        entity.setMasterBlCurrency(dto.getMasterBlCurrency());
        entity.setTotalCharges(dto.getTotalCharges());

        entity.setAirArrivalport(dto.getAirArrivalport());
        entity.setAirDeparturePort(dto.getAirDeparturePort());
        entity.setCarrierCode(dto.getCarrierCode());
        entity.setAgentDetails(dto.getAgentDetails());

        entity.setRateChanges(dto.getRateChanges());
        entity.setAgentCharges(dto.getAgentCharges());

        entity.setFlightNo2(dto.getFlightNo2());
        entity.setFlightDate2(dto.getFlightDate2());

        entity.setPriSupCodeOld(dto.getPriSupCodeOld());
        entity.setShiprCngCodeOld(dto.getShiprCngCodeOld());
        entity.setFfBladingNo(dto.getFfBladingNo());

        entity.setCfInvnoOld(dto.getCfInvnoOld());
        entity.setCurrentDoNo(dto.getCurrentDoNo());
        entity.setDeleted(dto.getDeleted());

        entity.setReleasedType(dto.getReleasedType());
        entity.setRelasedSeqNo(dto.getRelasedSeqNo());
        entity.setReleasedGrantBy(dto.getReleasedGrantBy());
        entity.setReleasedGrantDate(dto.getReleasedGrantDate());
        entity.setReleasedGrantReason(dto.getReleasedGrantReason());

        entity.setFirstCarrier(dto.getFirstCarrier());
        entity.setAccountInfo(dto.getAccountInfo());

        entity.setCanPrinted(dto.getCanPrinted());
        entity.setDoPrinted(dto.getDoPrinted());
        entity.setMablPrinted(dto.getMablPrinted());

        entity.setConsigneManual(dto.getConsigneManual());
        entity.setNotifyManual(dto.getNotifyManual());
        entity.setShipperManual(dto.getShipperManual());

        entity.setOfoqMnfRef(dto.getOfoqMnfRef());
        entity.setPrincipalAddrPoid(dto.getPrincipalAddrPoid());

        entity.setShowNotifyCan(dto.getShowNotifyCan());
        entity.setCanSentTo(dto.getCanSentTo());
        entity.setCanPrintedBy(dto.getCanPrintedBy());
        entity.setCanPrintedDt(dto.getCanPrintedDt());

        entity.setFfShJob(dto.getFfShJob());
        entity.setBillToCustomerPoid(dto.getBillToCustomerPoid());

        entity.setPrincipalManual(dto.getPrincipalManual());
        entity.setMotherVslFinalDelv(dto.getMotherVslFinalDelv());

        entity.setProjectRef(dto.getOtherReference());
        entity.setRecievedFrom(dto.getRecievedFrom());
        entity.setDeliveryTo(dto.getDeliveryTo());
        entity.setProjectPoid(dto.getProjectPoid());

        entity.setBlIssueDate(dto.getBlIssueDate());
        entity.setAirTransPort(dto.getAirTransPort());
        entity.setAirTransPort2(dto.getAirTransPort2());

        entity.setSecondCarrier(dto.getSecondCarrier());
        entity.setThirdCarrier(dto.getThirdCarrier());

        entity.setContainerVolume(dto.getContainerVolume());
        entity.setRateClass(dto.getRateClass());
        entity.setKgLb(dto.getKgLb());

        entity.setFcrDofCargoRcpt(dto.getFcrDofCargoRcpt());
        entity.setFcrCargoRemarks(dto.getFcrCargoRemarks());
        entity.setFcrSuplierShipperRef(dto.getFcrSuplierShipperRef());

        entity.setCoLoaderAgent(dto.getCoLoaderAgent());
        entity.setIncoTerm(dto.getIncoTerm());
        entity.setDocumentStatus(dto.getDocumentStatus());
        entity.setSpecialDocumentRemarks(dto.getSpecialDocumentRemarks());

        entity.setDeliveryDateFrom(dto.getDeliveryDateFrom());
        entity.setDeliveryDateTo(dto.getDeliveryDateTo());

        entity.setCustomsClearanceInvoved(dto.getCustomsClearanceInvoved());
        entity.setRoadTransport(dto.getRoadTransport());

        entity.setDoFreedays(dto.getDoFreedays());

        entity.setBayanNo(dto.getBayanNo());
        entity.setBayanType(dto.getBayanType());
        entity.setBayanAmount(dto.getBayanAmount());
        entity.setBayanExpiry(dto.getBayanExpiry());
        entity.setBayanStatus(dto.getBayanStatus());

        entity.setPolicyNo(dto.getPolicyNo());
        entity.setShipperManualEdi(dto.getShipperManualEdi());
        entity.setConsigneManualEdi(dto.getConsigneManualEdi());

        entity.setPassengerWithCargo(dto.getPassengerWithCargo());
        entity.setRadioAction(dto.getRadioAction());
        entity.setPerformaPrintUSD(dto.getPerformaPrintUSD());
        entity.setGlobalTracking(dto.getGlobalTracking());

        entity.setHouseBlNo2(dto.getHouseBlNo2());
        entity.setIsMainJob(dto.getIsMainJob());
        entity.setMainTransactionPoid(dto.getMainTransactionPoid());

        entity.setHoldDo(dto.getHoldDo());
        entity.setHoldDoUser(dto.getHoldDoUser());

        entity.setShipmentMode(dto.getShipmentMode());
        entity.setTransportationMode(dto.getTransportationMode());
        entity.setTruckTransportFrom(dto.getTruckTransportFrom());
        entity.setTruckTransportTo(dto.getTruckTransportTo());
    }

    public void toHdrDto(FFManifestHdr entity, FFManifestHdrDtoResponse dto) {

        if (entity == null)
            return;

        dto.setTransactionPoid(entity.getTransactionPoid());
        dto.setTransactionDate(entity.getTransactionDate());
        dto.setGroupPoid(entity.getGroupPoid());
        dto.setCompanyPoid(entity.getCompanyPoid());

        dto.setFfJobNo(entity.getFfJobNo());
        dto.setFfJobType(entity.getFfJobType());

        dto.setLinePoid(entity.getLinePoid());
        dto.setLineLov(getLov(entity.getLinePoid(),"LINE_MASTER"));
        dto.setQuoatationPoid(entity.getQuoatationPoid());
        dto.setPrincipalPoid(entity.getPrincipalPoid());
        dto.setPrincipalLov(getLov(entity.getPrincipalPoid(),"PRINCIPAL_MASTER"));

        dto.setMasterBlNo(entity.getMasterBlNo());
        dto.setHouseBlNo(entity.getHouseBlNo());
        dto.setBlStatus(entity.getBlStatus());
        dto.setWorkExtensionJobNo(entity.getWorkExtensionJobNo());

        dto.setBookedBy(entity.getBookedBy());
        dto.setFreightFlag(entity.getFreightFlag());
        dto.setConsignmentType(entity.getConsignmentType());

        dto.setSalesmanPoid(entity.getSalesmanPoid());
        dto.setSalesmanLov(getLov(entity.getSalesmanPoid(),"SALESMAN"));
        dto.setAgentPoid(entity.getAgentPoid());
        dto.setAgentAcctNo(entity.getAgentAcctNo());
        dto.setAgentIataNo(entity.getAgentIataNo());

        dto.setShedNo(entity.getShedNo());
        dto.setJobStatus(entity.getJobStatus());
        dto.setJobClosedBy(entity.getJobClosedBy());
        dto.setJobClosedDate(entity.getJobClosedDate());

        dto.setVoyagePoid(entity.getVoyagePoid());
        dto.setMotherVslVoyageNo(entity.getMotherVslVoyageNo());
        dto.setMotherVslName(entity.getMotherVslName());
        dto.setMotherVslSailDate(entity.getMotherVslSailDate());
        dto.setMotherVslEta(entity.getMotherVslEta());

        dto.setMotherVslLoadPortPoid(entity.getMotherVslLoadPortPoid());
        dto.setMotherVslUnloadPortPoid(entity.getMotherVslUnloadPortPoid());
        dto.setMotherVslTranshipPortPoid(entity.getMotherVslTranshipPortPoid());

        dto.setFeederVoyageNo(entity.getFeederVoyageNo());
        dto.setFeederVslName(entity.getFeederVslName());
        dto.setFeederVslSailDate(entity.getFeederVslSailDate());
        dto.setFeederVslEta(entity.getFeederVslEta());
        dto.setFeederVslArrivalDate(entity.getFeederVslArrivalDate());

        dto.setFeederLoadportPoid(entity.getFeederLoadportPoid());
        dto.setFeederLoadPortLov(getLov(entity.getFeederLoadportPoid(),"PORT_MASTER"));
        dto.setFeederUnloadportPoid(entity.getFeederUnloadportPoid());
        dto.setFeederUnloadPortLov(getLov(entity.getFeederUnloadportPoid(),"PORT_MASTER"));

        dto.setFlightNo(entity.getFlightNo());
        dto.setFlightDate(entity.getFlightDate());

        dto.setAwportOfLoad(entity.getAwportOfLoad());
        dto.setAwportOfUnload(entity.getAwportOfUnload());

        dto.setShipperPoid(entity.getShipperPoid());
        dto.setShipperAddressPoid(entity.getShipperAddressPoid());
        dto.setConsigneePoid(entity.getConsigneePoid());
        dto.setConsigneeAddressPoid(entity.getConsigneeAddressPoid());

        dto.setNotifyPoid1(entity.getNotifyPoid1());
        dto.setNotifyLov1(getLov(entity.getNotifyPoid1(),"ADDRESS_MASTER_PROJECTS_HX1"));
        dto.setNotifyAddressPoid1(entity.getNotifyAddressPoid1());
        dto.setNotifyPoid2(entity.getNotifyPoid2());
        dto.setNotifyLov2(getLov(entity.getNotifyPoid2(),"ADDRESS_MASTER_PROJECTS_HX1"));
        dto.setNotifyAddressPoid2(entity.getNotifyAddressPoid2());

        dto.setCanRequireToSent(entity.getCanRequireToSent());
        List<Long> commodityPoids=Optional.ofNullable(entity.getComodityPoid())
                .filter(s -> !s.trim().isEmpty())
                .map(s -> Arrays.stream(s.split(","))
                        .map(String::trim)
                        .map(Long::valueOf)
                        .toList())
                .orElse(List.of());
        dto.setCommodityPoids(commodityPoids);
        dto.setCommodityLovs(commodityPoids.stream().
                map(s->getLov(s,"COMODITY")).toList()
                );
        dto.setCargoDescription(entity.getCargoDescription());
        dto.setMarkNumbers(entity.getMarkNumbers());

        dto.setLpoNo(entity.getLpoNo());
        dto.setLpoDate(entity.getLpoDate());

        dto.setTotalVolume(entity.getTotalVolume());
        dto.setTotalNetVolume(entity.getTotalNetVolume());
        dto.setTotalWeight(entity.getTotalWeight());
        dto.setTotalNetWeight(entity.getTotalNetWeight());
        dto.setWeightUnit(entity.getWeightUnit());

        dto.setUnitPack(entity.getUnitPack());
        dto.setTotalNoOfPacks(entity.getTotalNoOfPacks());
        dto.setChargableWeight(entity.getChargableWeight());
        dto.setNoOfPackBooked(entity.getNoOfPackBooked());
        dto.setNoOfPackArrived(entity.getNoOfPackArrived());

        dto.setHandlingInfo(entity.getHandlingInfo());
        dto.setOtherDetails(entity.getOtherDetails());
        dto.setCustomsDeclarationNo(entity.getCustomsDeclarationNo());
        dto.setBillingTo(entity.getBillingTo());
        try {
            dto.setBillingToLov(entity.getBillingTo() != null ? getLov(Long.valueOf(entity.getBillingTo()), "FF_BILLING_TO") : null);
        } catch (NumberFormatException e) {
            dto.setBillingToLov(null);
        }

        dto.setMasterBlWeight(entity.getMasterBlWeight());
        dto.setMasterBlCurrency(entity.getMasterBlCurrency());
        dto.setTotalCharges(entity.getTotalCharges());

        dto.setAirArrivalport(entity.getAirArrivalport());
        dto.setAirDeparturePort(entity.getAirDeparturePort());
        dto.setCarrierCode(entity.getCarrierCode());
        dto.setAgentDetails(entity.getAgentDetails());

        dto.setRateChanges(entity.getRateChanges());
        dto.setAgentCharges(entity.getAgentCharges());

        dto.setFlightNo2(entity.getFlightNo2());
        dto.setFlightDate2(entity.getFlightDate2());

        dto.setPriSupCodeOld(entity.getPriSupCodeOld());
        dto.setShiprCngCodeOld(entity.getShiprCngCodeOld());
        dto.setFfBladingNo(entity.getFfBladingNo());

        dto.setCfInvnoOld(entity.getCfInvnoOld());
        dto.setCurrentDoNo(entity.getCurrentDoNo());
        dto.setDocRef(entity.getDocRef());
        dto.setDeleted(entity.getDeleted());

        dto.setReleasedType(entity.getReleasedType());
        dto.setRelasedSeqNo(entity.getRelasedSeqNo());
        dto.setReleasedGrantBy(entity.getReleasedGrantBy());
        dto.setReleasedGrantDate(entity.getReleasedGrantDate());
        dto.setReleasedGrantReason(entity.getReleasedGrantReason());

        dto.setFirstCarrier(entity.getFirstCarrier());
        dto.setAccountInfo(entity.getAccountInfo());

        dto.setCanPrinted(entity.getCanPrinted());
        dto.setDoPrinted(entity.getDoPrinted());
        dto.setMablPrinted(entity.getMablPrinted());

        dto.setConsigneManual(entity.getConsigneManual());
        dto.setNotifyManual(entity.getNotifyManual());
        dto.setShipperManual(entity.getShipperManual());

        dto.setOfoqMnfRef(entity.getOfoqMnfRef());
        dto.setPrincipalAddrPoid(entity.getPrincipalAddrPoid());

        dto.setShowNotifyCan(entity.getShowNotifyCan());
        dto.setCanSentTo(entity.getCanSentTo());
        dto.setCanPrintedBy(entity.getCanPrintedBy());
        dto.setCanPrintedDt(entity.getCanPrintedDt());

        dto.setFfShJob(entity.getFfShJob());
        dto.setBillToCustomerPoid(entity.getBillToCustomerPoid());
        dto.setBillToCustomerLov(getLov(entity.getBillToCustomerPoid(),"CUSTOMER_SUPPLIER_MASTER"));

        dto.setPrincipalManual(entity.getPrincipalManual());
        dto.setMotherVslFinalDelv(entity.getMotherVslFinalDelv());

        dto.setOtherReference(entity.getProjectRef());
        dto.setRecievedFrom(entity.getRecievedFrom());
        dto.setDeliveryTo(entity.getDeliveryTo());
        dto.setProjectPoid(entity.getProjectPoid());
        dto.setProjectLov(
                getLov(entity.getProjectPoid(),"PROJECTS")
        );

        dto.setBlIssueDate(entity.getBlIssueDate());
        dto.setAirTransPort(entity.getAirTransPort());
        dto.setAirTransPort2(entity.getAirTransPort2());

        dto.setSecondCarrier(entity.getSecondCarrier());
        dto.setThirdCarrier(entity.getThirdCarrier());

        dto.setContainerVolume(entity.getContainerVolume());
        dto.setRateClass(entity.getRateClass());
        dto.setKgLb(entity.getKgLb());

        dto.setFcrDofCargoRcpt(entity.getFcrDofCargoRcpt());
        dto.setFcrCargoRemarks(entity.getFcrCargoRemarks());
        dto.setFcrSuplierShipperRef(entity.getFcrSuplierShipperRef());

        dto.setCoLoaderAgent(entity.getCoLoaderAgent());
        dto.setIncoTerm(entity.getIncoTerm());
        dto.setDocumentStatus(entity.getDocumentStatus());
        dto.setSpecialDocumentRemarks(entity.getSpecialDocumentRemarks());

        dto.setDeliveryDateFrom(entity.getDeliveryDateFrom());
        dto.setDeliveryDateTo(entity.getDeliveryDateTo());

        dto.setCustomsClearanceInvoved(entity.getCustomsClearanceInvoved());
        dto.setRoadTransport(entity.getRoadTransport());

        dto.setDoFreedays(entity.getDoFreedays());

        dto.setBayanNo(entity.getBayanNo());
        dto.setBayanType(entity.getBayanType());
        dto.setBayanAmount(entity.getBayanAmount());
        dto.setBayanExpiry(entity.getBayanExpiry());
        dto.setBayanStatus(entity.getBayanStatus());

        dto.setPolicyNo(entity.getPolicyNo());
        dto.setShipperManualEdi(entity.getShipperManualEdi());
        dto.setConsigneManualEdi(entity.getConsigneManualEdi());

        dto.setPassengerWithCargo(entity.getPassengerWithCargo());
        dto.setRadioAction(entity.getRadioAction());
        dto.setPerformaPrintUSD(entity.getPerformaPrintUSD());
        dto.setGlobalTracking(entity.getGlobalTracking());

        dto.setHouseBlNo2(entity.getHouseBlNo2());
        dto.setIsMainJob(entity.getIsMainJob());
        dto.setMainTransactionPoid(entity.getMainTransactionPoid());

        dto.setHoldDo(entity.getHoldDo());
        dto.setHoldDoUser(entity.getHoldDoUser());

        dto.setShipmentMode(entity.getShipmentMode());
        dto.setTransportationMode(entity.getTransportationMode());
        dto.setTruckTransportFrom(entity.getTruckTransportFrom());
        dto.setTruckTransportTo(entity.getTruckTransportTo());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedDate(entity.getLastModifiedDate());
    }

    private LovGetListDto getLov(Long poid, String lovName) {
        if (poid == null) return null;
        return lovDataService.getDetailsByPoidAndLovNameFast(poid, lovName);
    }

    private Long longConvertion(BigDecimal previousValue){
        if(previousValue!=null){
            return previousValue.longValue();
        }
        return null;
    }

}
