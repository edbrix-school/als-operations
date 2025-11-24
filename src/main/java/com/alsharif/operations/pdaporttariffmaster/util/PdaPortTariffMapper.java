package com.alsharif.operations.pdaporttariffmaster.util;

import com.alsharif.operations.pdaporttariffmaster.dto.*;
import com.alsharif.operations.pdaporttariffmaster.entity.PdaPortTariffChargeDtl;
import com.alsharif.operations.pdaporttariffmaster.entity.PdaPortTariffHdr;
import com.alsharif.operations.pdaporttariffmaster.entity.PdaPortTariffSlabDtl;
import com.alsharif.operations.pdaporttariffmaster.repository.ShipPortMasterRepository;
import com.alsharif.operations.pdaporttariffmaster.repository.ShipVesselTypeMasterRepository;
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

        response.setPorts(stringToList(entity.getPorts()));
        if (stringToList(entity.getPorts()) != null && !stringToList(entity.getPorts()).isEmpty()) {

            List<BigDecimal> portPoidBDList = stringToList(entity.getPorts()).stream()
                    .map(p -> BigDecimal.valueOf(Long.parseLong(p)))
                    .toList();

            List<String> portNames = shipPortMasterRepository.findPortNamesByPortPoidInAndGroupPoid(portPoidBDList, entity.getGroupPoid());

            response.setPortNames(portNames);
        }

        response.setVesselTypes(stringToList(entity.getVesselTypes()));
        if (stringToList(entity.getVesselTypes()) != null && !stringToList(entity.getVesselTypes()).isEmpty()) {

            List<BigDecimal> vesselPoidBDList = stringToList(entity.getVesselTypes()).stream()
                    .map(v -> BigDecimal.valueOf(Long.parseLong(v)))
                    .toList();

            List<String> vesselNames = shipVesselTypeMasterRepository.findVesselTypeNamesByVesselTypePoidInAndGroupPoid(vesselPoidBDList, entity.getGroupPoid());

            response.setVesselTypeNames(vesselNames);
        }


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
        entity.setGroupPoid(groupPoid);
        entity.setCompanyPoid(companyPoid);
        entity.setDocRef(docRef);
        entity.setPorts(listToString(request.getPorts()));
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
        entity.setPorts(listToString(request.getPorts()));
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
        request.setPorts(stringToList(entity.getPorts()));
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

        response.setPorts(stringToList(entity.getPorts()));
        if (stringToList(entity.getPorts()) != null && !stringToList(entity.getPorts()).isEmpty()) {

            List<BigDecimal> portPoidBDList = stringToList(entity.getPorts()).stream()
                    .map(p -> BigDecimal.valueOf(Long.parseLong(p)))
                    .toList();

            List<String> portNames = shipPortMasterRepository.findPortNamesByPortPoidInAndGroupPoid(portPoidBDList, entity.getGroupPoid());

            response.setPortNames(portNames);
        }

        response.setVesselTypes(stringToList(entity.getVesselTypes()));
        if (stringToList(entity.getVesselTypes()) != null && !stringToList(entity.getVesselTypes()).isEmpty()) {

            List<BigDecimal> vesselPoidBDList = stringToList(entity.getVesselTypes()).stream()
                    .map(v -> BigDecimal.valueOf(Long.parseLong(v)))
                    .toList();

            List<String> vesselNames = shipVesselTypeMasterRepository.findVesselTypeNamesByVesselTypePoidInAndGroupPoid(vesselPoidBDList, entity.getGroupPoid());

            response.setVesselTypeNames(vesselNames);
        }

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