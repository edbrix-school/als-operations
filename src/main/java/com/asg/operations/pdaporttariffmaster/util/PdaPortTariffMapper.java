package com.asg.operations.pdaporttariffmaster.util;

import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.commonlov.dto.LovItem;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.pdaporttariffmaster.dto.*;
import com.asg.operations.pdaporttariffmaster.entity.PdaPortTariffChargeDtl;
import com.asg.operations.pdaporttariffmaster.entity.PdaPortTariffHdr;
import com.asg.operations.pdaporttariffmaster.entity.PdaPortTariffSlabDtl;
import com.asg.operations.pdaporttariffmaster.repository.ShipPortMasterRepository;
import com.asg.operations.pdaporttariffmaster.repository.ShipVesselTypeMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PdaPortTariffMapper {

    private final ShipPortMasterRepository shipPortMasterRepository;
    private final ShipVesselTypeMasterRepository shipVesselTypeMasterRepository;
    private final LovService lovService;

    // Convert comma-separated string to list
    public List<String> stringToList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(str.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // Convert list to comma-separated string
    public String listToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(";", list);
    }

    // Entity to Response (Header)
    public PdaPortTariffMasterResponse toResponse(PdaPortTariffHdr entity) {
        PdaPortTariffMasterResponse response = new PdaPortTariffMasterResponse();
        response.setTransactionPoid(entity.getTransactionPoid());
        response.setTransactionDate(entity.getTransactionDate() != null ?
                entity.getTransactionDate() : null);
        response.setDocRef(entity.getDocRef());

        response.setPort(entity.getPorts());
        if (entity.getPorts() != null && !entity.getPorts().trim().isEmpty()) {
            BigDecimal portPoidBD = BigDecimal.valueOf(Long.parseLong(entity.getPorts()));
            response.setPortDet(lovService.getLovItemByPoid(portPoidBD.longValue(), "PDA_PORT_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
            
            List<String> portNames = shipPortMasterRepository.findPortNamesByPortPoidInAndGroupPoid(List.of(portPoidBD), BigDecimal.valueOf(entity.getGroupPoid()));
            if (!portNames.isEmpty()) {
                response.setPortName(portNames.get(0));
            }
        }

        response.setVesselTypes(stringToList(entity.getVesselTypes()));
        if (stringToList(entity.getVesselTypes()) != null && !stringToList(entity.getVesselTypes()).isEmpty()) {

            List<BigDecimal> vesselPoidBDList = stringToList(entity.getVesselTypes()).stream()
                    .map(v -> BigDecimal.valueOf(Long.parseLong(v)))
                    .toList();

            List<LovItem> vesselTypesDetList = vesselPoidBDList.stream().map(p -> lovService.getLovItemByPoid(p.longValue(), "VESSEL_TYPE_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid())).toList();
            response.setVesselTypesDet(vesselTypesDetList);

            List<String> vesselNames = shipVesselTypeMasterRepository.findVesselTypeNamesByVesselTypePoidInAndGroupPoid(vesselPoidBDList, BigDecimal.valueOf(entity.getGroupPoid()));
            response.setVesselTypeNames(vesselNames);
        }

        response.setGroupPoid(entity.getGroupPoid());
        response.setGroupDet(lovService.getLovItemByPoid(entity.getGroupPoid(), "GROUP", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        response.setCompanyPoid(entity.getCompanyPoid());
        response.setCompanyDet(lovService.getLovItemByPoid(entity.getCompanyPoid(), "COMPANY", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        response.setPeriodFrom(entity.getPeriodFrom());
        response.setPeriodTo(entity.getPeriodTo());
        response.setRemarks(entity.getRemarks());
        response.setDeleted(entity.getDeleted());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());

        // Map charge details if loaded
        if (entity.getChargeDetails() != null && !entity.getChargeDetails().isEmpty()) {
            response.setChargeDetails(
                    entity.getChargeDetails().stream()
                            .map(this::toChargeDetailResponse)
                            .collect(Collectors.toList())
            );
        }

        return response;
    }

    public void setLovDetails(PdaPortTariffMasterResponse response) {

        if (response.getPort() != null && !response.getPort().trim().isEmpty()) {
            BigDecimal portPoidBD = BigDecimal.valueOf(Long.parseLong(response.getPort()));
            response.setPortDet(lovService.getLovItemByPoid(portPoidBD.longValue(), "PDA_PORT_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
            
            List<String> portNames = shipPortMasterRepository.findPortNamesByPortPoidInAndGroupPoid(List.of(portPoidBD), BigDecimal.valueOf(UserContext.getGroupPoid()));
            if (!portNames.isEmpty()) {
                response.setPortName(portNames.get(0));
            }
        }

        if (response.getVesselTypes() != null && !response.getVesselTypes().isEmpty()) {

            List<BigDecimal> vesselPoidBDList = response.getVesselTypes().stream()
                    .map(v -> BigDecimal.valueOf(Long.parseLong(v)))
                    .toList();

            List<LovItem> vesselTypesDetList = vesselPoidBDList.stream().map(p -> lovService.getLovItemByPoid(p.longValue(), "VESSEL_TYPE_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid())).toList();
            response.setVesselTypesDet(vesselTypesDetList);

            List<String> vesselNames = shipVesselTypeMasterRepository.findVesselTypeNamesByVesselTypePoidInAndGroupPoid(vesselPoidBDList, BigDecimal.valueOf(UserContext.getGroupPoid()));
            response.setVesselTypeNames(vesselNames);
        }
        response.setGroupDet(lovService.getLovItemByPoid(response.getGroupPoid(), "GROUP", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        response.setCompanyDet(lovService.getLovItemByPoid(response.getCompanyPoid(), "COMPANY", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
    }

    // Entity to Response (Charge Detail)
    public PdaPortTariffChargeDetailResponse toChargeDetailResponse(PdaPortTariffChargeDtl entity) {
        PdaPortTariffChargeDetailResponse response = new PdaPortTariffChargeDetailResponse();
        response.setDetRowId(entity.getId().getDetRowId());
        response.setChargePoid(entity.getChargePoid());
        response.setRateTypePoid(entity.getRateTypePoid());
        response.setTariffSlab(entity.getTariffSlab());
        response.setFixRate(entity.getFixRate());
        response.setHarborCallType(entity.getHarborCallType());
        response.setIsEnabled(entity.getIsEnabled());
        response.setRemarks(entity.getRemarks());
        response.setSeqNo(entity.getSeqNo());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());
        response.setChargeDet(lovService.getLovItemByPoid(entity.getChargePoid(), "CHARGE_MASTER_FOR_PDA", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        response.setRateTypeDet(lovService.getLovItemByPoid(entity.getRateTypePoid(), "PDA_RATE_TYPE_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        response.setTariffSlabDet(lovService.getLovItemByCode(entity.getTariffSlab(), "PDA_TARIFF_TYPES", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        response.setHarborCallTypeDet(lovService.getLovItemByCode(entity.getHarborCallType(), "HARBOR_CALL_TYPE", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        response.setIsEnabledDet(lovService.getLovItemByCode(entity.getIsEnabled(), "YES_NO", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));

        // Map slab details if loaded
        if (entity.getSlabDetails() != null && !entity.getSlabDetails().isEmpty()) {
            response.setSlabDetails(
                    entity.getSlabDetails().stream()
                            .map(this::toSlabDetailResponse)
                            .collect(Collectors.toList())
            );
        }

        return response;
    }

    // Entity to Response (Slab Detail)
    public PdaPortTariffSlabDetailResponse toSlabDetailResponse(PdaPortTariffSlabDtl entity) {
        PdaPortTariffSlabDetailResponse response = new PdaPortTariffSlabDetailResponse();
        response.setDetRowId(entity.getId().getDetRowId());
        response.setQuantityFrom(entity.getQuantityFrom());
        response.setQuantityTo(entity.getQuantityTo());
        response.setDays1(entity.getDays1());
        response.setRate1(entity.getRate1());
        response.setDays2(entity.getDays2());
        response.setRate2(entity.getRate2());
        response.setDays3(entity.getDays3());
        response.setRate3(entity.getRate3());
        response.setDays4(entity.getDays4());
        response.setRate4(entity.getRate4());
        response.setCallByPort(entity.getCallByPort());
        response.setRemarks(entity.getRemarks());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());
        return response;
    }

    // Request to Entity (Header - for create)
    public PdaPortTariffHdr toEntity(PdaPortTariffMasterRequest request, BigDecimal groupPoid, BigDecimal companyPoid, String docRef, String currentUser) {
        PdaPortTariffHdr entity = new PdaPortTariffHdr();
        entity.setGroupPoid(groupPoid.longValue());
        entity.setCompanyPoid(companyPoid.longValue());
        entity.setDocRef(docRef);
        entity.setPorts(request.getPort());
        entity.setVesselTypes(listToString(request.getVesselTypes()));
        entity.setPeriodFrom(request.getPeriodFrom());
        entity.setPeriodTo(request.getPeriodTo());
        entity.setRemarks(request.getRemarks());
        entity.setDeleted("N");
        entity.setTransactionDate(LocalDate.now());
        entity.setCreatedBy(currentUser);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setLastModifiedBy(currentUser);
        entity.setLastModifiedDate(LocalDateTime.now());
        return entity;
    }

    // Update Entity from Request (Header)
    public void updateEntityFromRequest(PdaPortTariffHdr entity, PdaPortTariffMasterRequest request, String currentUser) {
        entity.setPorts(request.getPort());
        entity.setVesselTypes(listToString(request.getVesselTypes()));
        entity.setPeriodFrom(request.getPeriodFrom());
        entity.setPeriodTo(request.getPeriodTo());
        entity.setRemarks(request.getRemarks());
        entity.setLastModifiedBy(currentUser);
        entity.setLastModifiedDate(LocalDateTime.now());
    }

    // Entity to Request (for copy functionality)
    public PdaPortTariffMasterRequest toRequest(PdaPortTariffHdr entity) {
        PdaPortTariffMasterRequest request = new PdaPortTariffMasterRequest();
        request.setPort(entity.getPorts());
        request.setVesselTypes(stringToList(entity.getVesselTypes()));
        request.setPeriodFrom(entity.getPeriodFrom());
        request.setPeriodTo(entity.getPeriodTo());
        request.setRemarks(entity.getRemarks());
        return request;
    }

    // Entity to Response with separate charge details
    public PdaPortTariffMasterResponse toResponseWithChargeDetails(PdaPortTariffHdr entity, List<PdaPortTariffChargeDtl> chargeDetails) {
        PdaPortTariffMasterResponse response = toResponse(entity);
        if (chargeDetails != null && !chargeDetails.isEmpty()) {
            response.setChargeDetails(
                    chargeDetails.stream()
                            .map(this::toChargeDetailResponse)
                            .collect(Collectors.toList())
            );
        }
        return response;
    }

    // Convert charge details to response
    public ChargeDetailsResponse toChargeDetailsResponse(List<PdaPortTariffChargeDtl> chargeDetails, Long transactionPoid) {
        ChargeDetailsResponse response = new ChargeDetailsResponse();
        response.setTransactionPoid(transactionPoid);
        if (chargeDetails != null && !chargeDetails.isEmpty()) {
            response.setChargeDetails(
                    chargeDetails.stream()
                            .map(this::toChargeDetailResponse)
                            .collect(Collectors.toList())
            );
        }
        return response;
    }

    // Entity to Response (Header only - no child loading)
    public PdaPortTariffMasterResponse toHeaderOnlyResponse(PdaPortTariffHdr entity) {
        PdaPortTariffMasterResponse response = new PdaPortTariffMasterResponse();
        response.setTransactionPoid(entity.getTransactionPoid());
        response.setTransactionDate(entity.getTransactionDate());
        response.setDocRef(entity.getDocRef());

        response.setPort(entity.getPorts());
        if (entity.getPorts() != null && !entity.getPorts().trim().isEmpty()) {
            BigDecimal portPoidBD = BigDecimal.valueOf(Long.parseLong(entity.getPorts()));
            response.setPortDet(lovService.getLovItemByPoid(portPoidBD.longValue(), "PDA_PORT_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
            
            List<String> portNames = shipPortMasterRepository.findPortNamesByPortPoidInAndGroupPoid(List.of(portPoidBD), BigDecimal.valueOf(entity.getGroupPoid()));
            if (!portNames.isEmpty()) {
                response.setPortName(portNames.get(0));
            }
        }

        response.setVesselTypes(stringToList(entity.getVesselTypes()));
        if (stringToList(entity.getVesselTypes()) != null && !stringToList(entity.getVesselTypes()).isEmpty()) {

            List<BigDecimal> vesselPoidBDList = stringToList(entity.getVesselTypes()).stream()
                    .map(v -> BigDecimal.valueOf(Long.parseLong(v)))
                    .toList();

            List<LovItem> vesselTypesDetList = vesselPoidBDList.stream().map(p -> lovService.getLovItemByPoid(p.longValue(), "VESSEL_TYPE_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid())).toList();
            response.setVesselTypesDet(vesselTypesDetList);

            List<String> vesselNames = shipVesselTypeMasterRepository.findVesselTypeNamesByVesselTypePoidInAndGroupPoid(vesselPoidBDList, BigDecimal.valueOf(entity.getGroupPoid()));
            response.setVesselTypeNames(vesselNames);
        }

        response.setGroupPoid(entity.getGroupPoid());
        response.setGroupDet(lovService.getLovItemByPoid(entity.getGroupPoid(), "GROUP", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        response.setCompanyPoid(entity.getCompanyPoid());
        response.setCompanyDet(lovService.getLovItemByPoid(entity.getCompanyPoid(), "COMPANY", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        response.setPeriodFrom(entity.getPeriodFrom());
        response.setPeriodTo(entity.getPeriodTo());
        response.setRemarks(entity.getRemarks());
        response.setDeleted(entity.getDeleted());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());
        return response;
    }
}