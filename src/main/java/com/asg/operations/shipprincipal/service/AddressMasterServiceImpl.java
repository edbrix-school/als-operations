package com.asg.operations.shipprincipal.service;

import com.asg.operations.shipprincipal.dto.AddressDetailsDTO;
import com.asg.operations.shipprincipal.dto.AddressMasterResponse;
import com.asg.operations.shipprincipal.dto.AddressTypeMapDTO;
import com.asg.operations.shipprincipal.entity.AddressDetails;
import com.asg.operations.shipprincipal.entity.AddressMaster;
import com.asg.operations.shipprincipal.repository.AddressDetailsRepository;
import com.asg.operations.shipprincipal.repository.AddressMasterRepository;
import com.asg.operations.shipprincipal.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressMasterServiceImpl implements AddressMasterService {

    private final CountryRepository countryRepo;
    private final AddressMasterRepository masterRepo;
    private final AddressDetailsRepository detailsRepo;

    /**
     * Get single Address Master with all department details (tabs).
     */
    @Override
    public AddressMasterResponse getMasterWithDetails(Long poid) {
        AddressMaster master = masterRepo.findById(poid)
                .orElseThrow(() -> new NoSuchElementException("Address Master not found"));

        List<AddressDetails> details = detailsRepo.findByAddressMasterPoidOrderByAddressType(poid);

        // Group by type into DTO map
        Map<String, List<AddressDetailsDTO>> grouped = details.stream()
                .map(this::mapToDTO)
                .collect(Collectors.groupingBy(AddressDetailsDTO::getAddressType));

        AddressTypeMapDTO typeMap = new AddressTypeMapDTO();
        typeMap.setMAIN(grouped.getOrDefault("MAIN", List.of()));
        typeMap.setFINANCE(grouped.getOrDefault("FINANCE", List.of()));
        typeMap.setSALES(grouped.getOrDefault("SALES", List.of()));
        typeMap.setOPERATIONS(grouped.getOrDefault("OPERATIONS", List.of()));
        typeMap.setINVOICE(grouped.getOrDefault("INVOICE", List.of()));
        typeMap.setDELIVERY_ORDER(grouped.getOrDefault("DELIVERY_ORDER", List.of()));
        typeMap.setCARGO_ARRIVAL_NOTICE(grouped.getOrDefault("CARGO_ARRIVAL_NOTICE", List.of()));
        typeMap.setSHIP_CHANDLING(grouped.getOrDefault("SHIP_CHANDLING", List.of()));
        typeMap.setCLAIM_UAC(grouped.getOrDefault("CLAIM_UAC", List.of()));
        typeMap.setCAN(grouped.getOrDefault("CAN", List.of()));

        AddressMasterResponse resp = buildMasterResponse(master, details);
        resp.setAddressTypeMap(typeMap);
        return resp;
    }

    private AddressMaster buildOrUpdateMaster(AddressMasterResponse req, String currentUser) {
        AddressMaster master;
        if (req.getAddressMasterPoid() != null) {
            master = masterRepo.findById(req.getAddressMasterPoid())
                    .orElseThrow(() -> new NoSuchElementException("Address Master not found"));
        } else {
            master = new AddressMaster();
            master.setGroupPoid(1L);
            master.setCreatedBy(currentUser);
            master.setCreatedDate(LocalDateTime.now());
            master.setDeleted("N"); // Ensure deleted flag is set
        }

        master.setAddressName(req.getAddressName());
        master.setAddressName2(req.getAddressName2());
        master.setCountryPoid(req.getCountryId());
        master.setPreferredCommunication(req.getPreferredCommunication() != null ? String.join(",", req.getPreferredCommunication()) : null);
        master.setPartyType(req.getPartyType() != null ? String.join(",", req.getPartyType()) : null);
        master.setWhatsappNo(req.getWhatsappNo());
        master.setLinkedIn(req.getLinkedIn());
        master.setInstagram(req.getInstagram());
        master.setFacebook(req.getFacebook());
        master.setRemarks(req.getRemarks());
        master.setCrNumber(req.getCrNumber());
        master.setIsForwarder(Boolean.TRUE.equals(req.getIsForwarder()) ? "Y" : "N");
        master.setActive(req.getActive());
        master.setSeqno(req.getSeqno());
        master.setLastModifiedBy(currentUser);
        master.setLastModifiedDate(LocalDateTime.now());

        return master;
    }

    @Override
    public void saveAllDetails(AddressTypeMapDTO typeMap, AddressMaster master, String currentUser) {
        if (typeMap == null) return;

        //  Fetch existing details from DB
        List<AddressDetails> existingDetails = detailsRepo.findByAddressMasterPoidOrderByAddressType(master.getAddressMasterPoid());
        Map<String, AddressDetails> existingMap = existingDetails.stream()
                .collect(Collectors.toMap(d -> String.valueOf(d.getAddressPoid()), d -> d));

        List<AddressDetails> toSave = new ArrayList<>();

        Map<String, List<AddressDetailsDTO>> typedLists = Map.of(
                "MAIN", Optional.ofNullable(typeMap.getMAIN()).orElse(List.of()),
                "FINANCE", Optional.ofNullable(typeMap.getFINANCE()).orElse(List.of()),
                "SALES", Optional.ofNullable(typeMap.getSALES()).orElse(List.of()),
                "OPERATIONS", Optional.ofNullable(typeMap.getOPERATIONS()).orElse(List.of()),
                "INVOICE", Optional.ofNullable(typeMap.getINVOICE()).orElse(List.of()),
                "DELIVERY_ORDER", Optional.ofNullable(typeMap.getDELIVERY_ORDER()).orElse(List.of()),
                "CARGO_ARRIVAL_NOTICE", Optional.ofNullable(typeMap.getCARGO_ARRIVAL_NOTICE()).orElse(List.of()),
                "SHIP_CHANDLING", Optional.ofNullable(typeMap.getSHIP_CHANDLING()).orElse(List.of()),
                "CLAIM_UAC", Optional.ofNullable(typeMap.getCLAIM_UAC()).orElse(List.of()),
                "CAN", Optional.ofNullable(typeMap.getCAN()).orElse(List.of())
        );

        int counter = existingDetails.size() + 1;

        for (Map.Entry<String, List<AddressDetailsDTO>> entry : typedLists.entrySet()) {
            String type = entry.getKey();
            for (AddressDetailsDTO dto : entry.getValue()) {
                AddressDetails detail;
                if (dto.getAddressPoid() != null && existingMap.containsKey(dto.getAddressPoid())) {
                    detail = existingMap.get(dto.getAddressPoid());
                    updateDetail(detail, dto, currentUser);
                } else {
                    detail = buildDetail(dto, master, type, counter++, currentUser);
                }
                toSave.add(detail);
            }
        }

        //  Save updated and new details
        if (!toSave.isEmpty()) {
            detailsRepo.saveAll(toSave);
        }
    }

    private void updateDetail(AddressDetails entity, AddressDetailsDTO dto, String currentUser) {
        entity.setContactPerson(dto.getContactPerson());
        entity.setDesignation(dto.getDesignation());
        entity.setOffTel1(dto.getOffTel1());
        entity.setOffTel2(dto.getOffTel2());
        entity.setMobile(dto.getMobile());
        entity.setFax(dto.getFax());

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            entity.setEmail1(dto.getEmail().get(0));
            entity.setEmail2(dto.getEmail().size() > 1 ? dto.getEmail().get(1) : null);
        } else {
            entity.setEmail1(null);
            entity.setEmail2(null);
        }

        entity.setWebsite(dto.getWebsite());
        entity.setPoBox(dto.getPoBox());
        entity.setOffNo(dto.getOffNo());
        entity.setBldg(dto.getBldg());
        entity.setRoad(dto.getRoad());

        String area = dto.getArea();
        String city = dto.getCity();
        if ((city == null || city.isBlank()) && area != null && area.contains(",")) {
            String[] parts = area.split(",", 2);
            area = parts[0].trim();
            city = parts.length > 1 ? parts[1].trim() : null;
        }
        entity.setAreaCity(area);
        entity.setCity(city);

        entity.setState(dto.getState() != null && !dto.getState().isEmpty()
                ? String.join(",", dto.getState())
                : null
        );

        entity.setLandMark(dto.getLandMark());
        entity.setVerified(dto.getVerified());
        entity.setVerifiedBy(dto.getVerifiedBy());
        entity.setVerifiedDate(dto.getVerifiedDate());
        entity.setLastModifiedBy(currentUser);
        entity.setLastModifiedDate(LocalDateTime.now());

        entity.setWhatsappNo(dto.getWhatsappNo());
        entity.setLinkedIn(dto.getLinkedIn());
        entity.setInstagram(dto.getInstagram());
        entity.setFacebook(dto.getFacebook());

    }


    private AddressDetails buildDetail(AddressDetailsDTO dto, AddressMaster master, String type, int counter, String currentUser) {
        AddressDetails detail = new AddressDetails();

        BigDecimal addressPoid;
        if (dto.getAddressPoid() != null) {
            addressPoid = new BigDecimal(dto.getAddressPoid());
        } else {
            addressPoid = BigDecimal.valueOf(System.currentTimeMillis() + counter);
        }
        detail.setAddressPoid(addressPoid);

        detail.setAddressMasterPoid(master.getAddressMasterPoid());
        detail.setAddressType(type);
        detail.setContactPerson(dto.getContactPerson());
        detail.setDesignation(dto.getDesignation());
        detail.setOffTel1(dto.getOffTel1());
        detail.setOffTel2(dto.getOffTel2());
        detail.setMobile(dto.getMobile());
        detail.setFax(dto.getFax());

        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            detail.setEmail1(dto.getEmail().get(0));
            if (dto.getEmail().size() > 1) {
                detail.setEmail2(dto.getEmail().get(1));
            }
        }

        detail.setWebsite(dto.getWebsite());
        detail.setPoBox(dto.getPoBox());
        detail.setOffNo(dto.getOffNo());
        detail.setBldg(dto.getBldg());
        detail.setRoad(dto.getRoad());

        String area = dto.getArea();
        String city = dto.getCity();
        if ((city == null || city.isBlank()) && area != null && area.contains(",")) {
            String[] parts = area.split(",", 2);
            area = parts[0].trim();
            city = parts.length > 1 ? parts[1].trim() : null;
        }
        detail.setAreaCity(area);
        detail.setCity(city);

        detail.setState(dto.getState() != null && !dto.getState().isEmpty()
                ? String.join(",", dto.getState())
                : null
        );

        detail.setLandMark(dto.getLandMark());
        detail.setVerified(dto.getVerified());
        detail.setVerifiedBy(dto.getVerifiedBy());
        detail.setVerifiedDate(dto.getVerifiedDate());
        detail.setCreatedBy(currentUser);
        detail.setCreatedDate(LocalDateTime.now());
        detail.setLastModifiedBy(currentUser);
        detail.setLastModifiedDate(LocalDateTime.now());
        detail.setWhatsappNo(dto.getWhatsappNo());
        detail.setLinkedIn(dto.getLinkedIn());
        detail.setInstagram(dto.getInstagram());
        detail.setFacebook(dto.getFacebook());

        return detail;
    }


    /**
     * Convert AddressMaster -> DTO
     */
    private AddressMasterResponse buildMasterResponse(AddressMaster m, List<AddressDetails> details) {
        AddressMasterResponse resp = new AddressMasterResponse();
        resp.setAddressMasterPoid(m.getAddressMasterPoid());
        resp.setAddressName(m.getAddressName());
        resp.setAddressName2(m.getAddressName2());
        resp.setPreferredCommunication(m.getPreferredCommunication() != null
                ? Arrays.asList(m.getPreferredCommunication().split(","))
                : List.of());
        resp.setPartyType(m.getPartyType() != null
                ? Arrays.asList(m.getPartyType().split(","))
                : List.of());
        resp.setWhatsappNo(m.getWhatsappNo());
        resp.setLinkedIn(m.getLinkedIn());
        resp.setInstagram(m.getInstagram());
        resp.setFacebook(m.getFacebook());
        resp.setRemarks(m.getRemarks());
        resp.setCrNumber(m.getCrNumber());
        resp.setIsForwarder("Y".equalsIgnoreCase(m.getIsForwarder()));
        resp.setActive(m.getActive());
        resp.setSeqno(m.getSeqno());

        // Set audit fields
        resp.setCreatedBy(m.getCreatedBy());
        resp.setCreatedDate(m.getCreatedDate() != null ? m.getCreatedDate().atOffset(java.time.ZoneOffset.UTC) : null);
        resp.setLastModifiedBy(m.getLastModifiedBy());
        resp.setLastModifiedDate(m.getLastModifiedDate() != null ? m.getLastModifiedDate().atOffset(java.time.ZoneOffset.UTC) : null);

        if (m.getCountryPoid() != null && m.getCountryPoid() != 0) {
            countryRepo.findById(m.getCountryPoid()).ifPresent(c -> {
                resp.setCountryId(c.getCountryPoid());
                resp.setCountryName(c.getCountryName());
            });
        }


        if (details != null) {
            details.stream()
                    .filter(d -> "MAIN".equalsIgnoreCase(d.getAddressType()))
                    .findFirst()
                    .ifPresentOrElse(main -> {
                        resp.setMobile(main.getMobile() != null ? main.getMobile() : "");
                        List<String> emails = new ArrayList<>();
                        if (main.getEmail1() != null) emails.add(main.getEmail2());
                        if (main.getEmail2() != null) emails.add(main.getEmail2());
                        resp.setEmail(emails);
                    }, () -> {
                        resp.setMobile("");
                        resp.setEmail(List.of());
                    });
        } else {
            resp.setMobile("");
            resp.setEmail(List.of());
        }

        return resp;
    }

    /**
     * Map AddressDetails -> DTO with area+city split handling
     */
    private AddressDetailsDTO mapToDTO(AddressDetails d) {
        List<String> emails = new ArrayList<>();
        if (d.getEmail1() != null && !d.getEmail1().isBlank()) emails.add(d.getEmail1());
        if (d.getEmail2() != null && !d.getEmail2().isBlank()) emails.add(d.getEmail2());

        String area = d.getAreaCity();
        String city = d.getCity();
        if ((city == null || city.isBlank()) && area != null && area.contains(",")) {
            String[] parts = area.split(",", 2);
            area = parts[0].trim();
            city = parts.length > 1 ? parts[1].trim() : null;
        }

        return AddressDetailsDTO.builder()
                .addressPoid(d.getAddressPoid() != null ? String.valueOf(d.getAddressPoid()) : null)
                .addressType(d.getAddressType())
                .contactPerson(d.getContactPerson())
                .designation(d.getDesignation())
                .offTel1(d.getOffTel1())
                .offTel2(d.getOffTel2())
                .mobile(d.getMobile())
                .fax(d.getFax())
                .email(emails)
                .website(d.getWebsite())
                .poBox(d.getPoBox())
                .offNo(d.getOffNo())
                .bldg(d.getBldg())
                .road(d.getRoad())
                .area(area)
                .city(city)
                .state(d.getState() != null
                        ? Arrays.stream(d.getState().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList()
                        : List.of())
                .landMark(d.getLandMark())
                .verified(d.getVerified())
                .verifiedBy(d.getVerifiedBy())
                .verifiedDate(d.getVerifiedDate())
                .createdBy(d.getCreatedBy())
                .createdDate(d.getCreatedDate())
                .lastModifiedBy(d.getLastModifiedBy())
                .lastModifiedDate(d.getLastModifiedDate())
                .whatsappNo(d.getWhatsappNo())
                .linkedIn(d.getLinkedIn())
                .instagram(d.getInstagram())
                .facebook(d.getFacebook())
                .build();
    }
}

