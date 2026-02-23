package com.asg.operations.projects.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.request.LogRequestDto;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.projects.dto.*;
import com.asg.operations.projects.entity.FFProjectsChargesDtl;
import com.asg.operations.projects.entity.FFProjectsCtrlSheetDtl;
import com.asg.operations.projects.entity.FFProjectsHdr;
import com.asg.operations.projects.repository.FFProjectsChargesDtlRepository;
import com.asg.operations.projects.repository.FFProjectsCtrlSheetDtlRepository;
import com.asg.operations.projects.repository.FFProjectsHdrRepository;
import com.asg.operations.projects.repository.FFProjectsStoredProcRepository;
import com.asg.operations.projects.repository.FreightJobProjectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FFProjectsServiceImpl implements FFProjectsService {

    private final FFProjectsHdrRepository projectsHdrRepository;
    private final FFProjectsChargesDtlRepository projectsChargesDtlRepository;
    private final FFProjectsCtrlSheetDtlRepository projectsCtrlSheetDtlRepository;
    private final FFProjectsStoredProcRepository projectsStoredProcRepository;
    private final FreightJobProjectionRepository freightJobProjectionRepository;
    private final LoggingService loggingService;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> listProjectsWithFilters(String documentId, FilterRequestDto filterRequest, Pageable pageable, LocalDate periodFrom, LocalDate periodTo) {
        Specification<FFProjectsHdr> spec = (root, query, cb) -> cb.equal(root.get("deleted"), "N");

        if (periodFrom != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("periodFrom"), periodFrom));
        }
        if (periodTo != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("periodTo"), periodTo));
        }

        Page<FFProjectsHdr> projectsPage = projectsHdrRepository.findAll(spec, pageable);

        List<FFProjectsListResponse> responseList = projectsPage.getContent().stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", responseList);
        response.put("totalElements", projectsPage.getTotalElements());
        response.put("totalPages", projectsPage.getTotalPages());
        response.put("currentPage", projectsPage.getNumber());
        response.put("pageSize", projectsPage.getSize());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public FFProjectsResponse getProjectById(Long transactionPoid) {
        FFProjectsHdr projectsHdr = projectsHdrRepository.findByTransactionPoidAndDeleted(transactionPoid, "N")
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + transactionPoid));

        List<FFProjectsChargesDtl> chargeDetails = projectsChargesDtlRepository.findByTransactionPoid(transactionPoid);
        List<FFProjectsCtrlSheetDtl> ctrlSheetDetails = projectsCtrlSheetDtlRepository.findByTransactionPoid(transactionPoid);

        return mapToResponse(projectsHdr, chargeDetails, ctrlSheetDetails);
    }

    @Override
    @Transactional
    public FFProjectsResponse createProject(FFProjectsRequest request) {
//        String docRef = generateDocRef();

        FFProjectsHdr projectsHdr = FFProjectsHdr.builder()
                .transactionDate(LocalDate.now())
                .companyPoid(UserContext.getCompanyPoid())
                .quotationReferencePoid(request.getQuotationReferencePoid())
                .projectDescription(request.getProjectDescription())
                .billingTo(request.getBillingTo())
                .billingPartyPoid(request.getBillingPartyPoid())
                .projectCustomerPoid(request.getProjectCustomerPoid())
                .principalPoid(request.getPrincipalPoid())
                .shipmentMode(request.getShipmentMode())
//                .mode(request.getMode())
                .projectReference(request.getProjectReference())
                .periodFrom(request.getPeriodFrom())
                .periodTo(request.getPeriodTo())
                .salesmanPoid(request.getSalesmanPoid())
                .linePoid(request.getLinePoid())
                .carrierCodePoid(request.getCarrierCodePoid())
                .commodity(request.getCommodity())
                .cargoDetails(request.getCargoDetails())
                .billingCurrencyCode(request.getBillingCurrencyCode())
                .projectStatus(request.getProjectStatus() != null ? request.getProjectStatus() : "Open")
                .deleted("N")
                .createdBy(UserContext.getUserName())
                .createdDate(LocalDateTime.now())
                .build();

        projectsHdr = projectsHdrRepository.save(projectsHdr);
        Long transactionPoid = projectsHdr.getTransactionPoid();

        if (request.getChargeDetails() != null && !request.getChargeDetails().isEmpty()) {
            for (FFProjectsChargesDetailRequest chargeReq : request.getChargeDetails()) {
                if ("ISCREATED".equalsIgnoreCase(chargeReq.getActionType())) {
                    List<FFProjectsChargesDtl> existingCharges = projectsChargesDtlRepository.findByTransactionPoid(transactionPoid);
                    long nextDetRowId = existingCharges.stream().mapToLong(FFProjectsChargesDtl::getDetRowId).max().orElse(0L) + 1;
                    
                    FFProjectsChargesDtl detail = FFProjectsChargesDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId)
                        .quotationReferencePoid(chargeReq.getQuotationReferencePoid())
                        .chargeDetailsPoid(chargeReq.getChargeDetailsPoid())
                        .printableChargeDescription(chargeReq.getPrintableChargeDescription())
//                        .chargeBasis(chargeReq.getChargeBasis())
                        .quantity(chargeReq.getQuantity())
                        .unit(chargeReq.getUnit())
                        .buyingCurrencyCode(chargeReq.getBuyingCurrencyCode())
                        .currencyRate(chargeReq.getCurrencyRate())
                        .buyingUnitRate(chargeReq.getBuyingUnitRate())
//                        .buyingTotalBhd(chargeReq.getBuyingTotalBhd())
//                        .sellingUnitRate(chargeReq.getSellingUnitRate())
//                        .sellingTotal(chargeReq.getSellingTotal())
                        .taxIdPoid(chargeReq.getTaxIdPoid())
                        .taxPercentage(chargeReq.getTaxPercentage())
//                        .taxAmount(chargeReq.getTaxAmount())
//                        .sellingGrandTotal(chargeReq.getSellingGrandTotal())
//                        .sellingGrandTotalBhd(chargeReq.getSellingGrandTotalBhd())
//                        .marginBhd(chargeReq.getMarginBhd())
                        .remarks(chargeReq.getRemarks())
//                        .deleted("N")
                        .createdBy(UserContext.getUserName())
                        .createdDate(LocalDateTime.now())
                        .build();
                    projectsChargesDtlRepository.save(detail);
                    String logDetails = String.format("Charge Detail Row - Det Row ID: %s", detail.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                            logDetails);
                }
            }
        }

        if (request.getControlSheetDetails() != null && !request.getControlSheetDetails().isEmpty()) {
            for (FFProjectsCtrlSheetDetailRequest ctrlReq : request.getControlSheetDetails()) {
                if ("ISCREATED".equals(ctrlReq.getActionType())) {
                    List<FFProjectsCtrlSheetDtl> existingSheets = projectsCtrlSheetDtlRepository.findByTransactionPoid(transactionPoid);
                    long nextDetRowId = existingSheets.stream().mapToLong(FFProjectsCtrlSheetDtl::getDetRowId).max().orElse(0L) + 1;
                    
                    FFProjectsCtrlSheetDtl detail = FFProjectsCtrlSheetDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(nextDetRowId)
                        .freightType(ctrlReq.getFreightType())
                        .jobNoPoid(ctrlReq.getJobNoPoid())
                        .origin(ctrlReq.getOriginPoid())
                        .destination(ctrlReq.getDestinationPoid())
                        .etd(ctrlReq.getEtd())
                        .etaAta(ctrlReq.getEtaAta())
                        .arrivalDate(ctrlReq.getArrivalDate())
                        .noOfPackages(ctrlReq.getNoOfPackages())
                        .weight(ctrlReq.getWeight())
                        .cbm(ctrlReq.getCbm())
                        .carrierCode(ctrlReq.getCarrierPoid())
                        .line(ctrlReq.getLinePoid())
                        .truckNumber(ctrlReq.getTruckNumber())
                        .description(ctrlReq.getDescription())
                        .sailDate(ctrlReq.getSailDate())
//                        .jobStatus(ctrlReq.getJobStatus())
//                        .deleted("N")
                        .createdBy(UserContext.getUserName())
                        .createdDate(LocalDateTime.now())
                        .build();
                    projectsCtrlSheetDtlRepository.save(detail);
                    String logDetails = String.format("Control Sheet Row - Det Row ID: %s, Freight Type: %s", detail.getDetRowId(), detail.getFreightType());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                            logDetails);
                }
            }
        }

        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), transactionPoid.toString());

        return getProjectById(transactionPoid);
    }

    @Override
    @Transactional
    public FFProjectsResponse updateProject(Long transactionPoid, FFProjectsRequest request) {
        FFProjectsHdr oldProjectsHdr = projectsHdrRepository.findByTransactionPoidAndDeleted(transactionPoid, "N")
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + transactionPoid));

        FFProjectsHdr newProjectsHdr = new FFProjectsHdr();
        newProjectsHdr.setTransactionPoid(oldProjectsHdr.getTransactionPoid());
        newProjectsHdr.setTransactionDate(oldProjectsHdr.getTransactionDate());
        newProjectsHdr.setCompanyPoid(oldProjectsHdr.getCompanyPoid());
        newProjectsHdr.setDocRef(oldProjectsHdr.getDocRef());
        newProjectsHdr.setQuotationReferencePoid(request.getQuotationReferencePoid());
        newProjectsHdr.setProjectDescription(request.getProjectDescription());
        newProjectsHdr.setBillingTo(request.getBillingTo());
        newProjectsHdr.setBillingPartyPoid(request.getBillingPartyPoid());
        newProjectsHdr.setProjectCustomerPoid(request.getProjectCustomerPoid());
        newProjectsHdr.setPrincipalPoid(request.getPrincipalPoid());
        newProjectsHdr.setShipmentMode(request.getShipmentMode());
//        newProjectsHdr.setMode(request.getMode());
        newProjectsHdr.setProjectReference(request.getProjectReference());
        newProjectsHdr.setPeriodFrom(request.getPeriodFrom());
        newProjectsHdr.setPeriodTo(request.getPeriodTo());
        newProjectsHdr.setSalesmanPoid(request.getSalesmanPoid());
        newProjectsHdr.setLinePoid(request.getLinePoid());
        newProjectsHdr.setCarrierCodePoid(request.getCarrierCodePoid());
        newProjectsHdr.setCommodity(request.getCommodity());
        newProjectsHdr.setCargoDetails(request.getCargoDetails());
        newProjectsHdr.setBillingCurrencyCode(request.getBillingCurrencyCode());
        newProjectsHdr.setProjectStatus(request.getProjectStatus());
        newProjectsHdr.setDeleted(oldProjectsHdr.getDeleted());
        newProjectsHdr.setCreatedBy(oldProjectsHdr.getCreatedBy());
        newProjectsHdr.setCreatedDate(oldProjectsHdr.getCreatedDate());

        loggingService.logChanges(oldProjectsHdr, newProjectsHdr, FFProjectsHdr.class, UserContext.getDocumentId(),
                transactionPoid.toString(), LogDetailsEnum.MODIFIED, "Project POID");

        oldProjectsHdr.setQuotationReferencePoid(request.getQuotationReferencePoid());
        oldProjectsHdr.setProjectDescription(request.getProjectDescription());
        oldProjectsHdr.setBillingTo(request.getBillingTo());
        oldProjectsHdr.setBillingPartyPoid(request.getBillingPartyPoid());
        oldProjectsHdr.setProjectCustomerPoid(request.getProjectCustomerPoid());
        oldProjectsHdr.setPrincipalPoid(request.getPrincipalPoid());
        oldProjectsHdr.setShipmentMode(request.getShipmentMode());
//        oldProjectsHdr.setMode(request.getMode());
        oldProjectsHdr.setProjectReference(request.getProjectReference());
        oldProjectsHdr.setPeriodFrom(request.getPeriodFrom());
        oldProjectsHdr.setPeriodTo(request.getPeriodTo());
        oldProjectsHdr.setSalesmanPoid(request.getSalesmanPoid());
        oldProjectsHdr.setLinePoid(request.getLinePoid());
        oldProjectsHdr.setCarrierCodePoid(request.getCarrierCodePoid());
        oldProjectsHdr.setCommodity(request.getCommodity());
        oldProjectsHdr.setCargoDetails(request.getCargoDetails());
        oldProjectsHdr.setBillingCurrencyCode(request.getBillingCurrencyCode());
        oldProjectsHdr.setProjectStatus(request.getProjectStatus());
        oldProjectsHdr.setLastModifiedBy(UserContext.getUserName());
        oldProjectsHdr.setLastModifiedDate(LocalDateTime.now());
        projectsHdrRepository.save(oldProjectsHdr);

        if (request.getChargeDetails() != null && !request.getChargeDetails().isEmpty()) {
            updateProjectCharges(request.getChargeDetails(), transactionPoid);
        }

        if (request.getControlSheetDetails() != null && !request.getControlSheetDetails().isEmpty()) {
            updateProjectControlSheets(request.getControlSheetDetails(), transactionPoid);
        }

        return getProjectById(transactionPoid);
    }

    @Override
    @Transactional
    public void deleteProject(Long transactionPoid, DeleteReasonDto deleteReasonDto) {
        FFProjectsHdr projectsHdr = projectsHdrRepository.findByTransactionPoidAndDeleted(transactionPoid, "N")
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + transactionPoid));

        projectsHdr.setDeleted("Y");
        projectsHdr.setLastModifiedBy(UserContext.getUserName());
        projectsHdr.setLastModifiedDate(LocalDateTime.now());
        projectsHdrRepository.save(projectsHdr);

        loggingService.createLogSummaryEntry(LogDetailsEnum.DELETED, UserContext.getDocumentId(), transactionPoid.toString());
    }

    @Override
    public Map<String, Object> loadQuotationDetails(Long quotationPoid) {
        return projectsStoredProcRepository.loadProjectsQuotation(quotationPoid);
    }

    @Override
    public Map<String, Object> loadJobDetails(Long transactionPoid) {
        return projectsStoredProcRepository.loadProjectsJobs(transactionPoid);
    }

    @Override
    @Transactional
    public List<FFProjectsCtrlSheetDetailResponse> batchControlSheetOperations(Long transactionPoid, List<FFProjectsCtrlSheetDetailRequest> requests) {
        FFProjectsHdr projectsHdr = projectsHdrRepository.findByTransactionPoidAndDeleted(transactionPoid, "N")
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + transactionPoid));

        updateProjectControlSheets(requests, transactionPoid);
        
        List<FFProjectsCtrlSheetDtl> details = projectsCtrlSheetDtlRepository.findByTransactionPoid(transactionPoid);
        return details.stream()
                .map(this::mapCtrlSheetDetailToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FFProjectsCtrlSheetDetailResponse> getControlSheetsByProject(Long transactionPoid, String freightType) {
        List<FFProjectsCtrlSheetDtl> details;
        
        if (freightType != null && !freightType.isEmpty()) {
            details = projectsCtrlSheetDtlRepository.findByTransactionPoidAndFreightType(transactionPoid, freightType);
        } else {
            details = projectsCtrlSheetDtlRepository.findByTransactionPoid(transactionPoid);
        }

        return details.stream()
                .map(this::mapCtrlSheetDetailToResponse)
                .collect(Collectors.toList());
    }

    private String generateDocRef() {
        String year = String.valueOf(java.time.Year.now().getValue());
        String prefix = "PROJ-" + year + "-";
        Integer maxSeq = projectsHdrRepository.findMaxSequenceByPrefix(prefix);
        int nextSeq = (maxSeq != null ? maxSeq : 0) + 1;
        return prefix + String.format("%05d", nextSeq);
    }

    private FFProjectsResponse mapToResponse(FFProjectsHdr hdr, List<FFProjectsChargesDtl> chargeDetails, List<FFProjectsCtrlSheetDtl> ctrlSheetDetails) {
        List<FFProjectsChargesDetailResponse> chargeResponses = chargeDetails.stream()
                .map(this::mapChargeDetailToResponse)
                .collect(Collectors.toList());

        List<FFProjectsCtrlSheetDetailResponse> ctrlSheetResponses = ctrlSheetDetails.stream()
                .map(this::mapCtrlSheetDetailToResponse)
                .collect(Collectors.toList());

        return FFProjectsResponse.builder()
                .transactionPoid(hdr.getTransactionPoid())
                .transactionDate(hdr.getTransactionDate())
                .companyPoid(hdr.getCompanyPoid())
                .docRef(hdr.getDocRef())
                .quotationReferencePoid(hdr.getQuotationReferencePoid())
                .projectDescription(hdr.getProjectDescription())
                .billingTo(hdr.getBillingTo())
                .billingPartyPoid(hdr.getBillingPartyPoid())
                .projectCustomerPoid(hdr.getProjectCustomerPoid())
                .principalPoid(hdr.getPrincipalPoid())
                .shipmentMode(hdr.getShipmentMode())
//                .mode(hdr.getMode())
                .projectReference(hdr.getProjectReference())
                .periodFrom(hdr.getPeriodFrom())
                .periodTo(hdr.getPeriodTo())
                .salesmanPoid(hdr.getSalesmanPoid())
                .linePoid(hdr.getLinePoid())
                .carrierCodePoid(hdr.getCarrierCodePoid())
                .commodity(hdr.getCommodity())
                .cargoDetails(hdr.getCargoDetails())
                .billingCurrencyCode(hdr.getBillingCurrencyCode())
                .projectStatus(hdr.getProjectStatus())
//                .totalBuyingRateBhd(hdr.getTotalBuyingRateBhd())
//                .totalVatBhd(hdr.getTotalVatBhd())
//                .grandTotalSellRateBhd(hdr.getGrandTotalSellRateBhd())
//                .grandTotalSellRateFc(hdr.getGrandTotalSellRateFc())
                .createdBy(hdr.getCreatedBy())
                .createdDate(hdr.getCreatedDate())
                .lastModifiedBy(hdr.getLastModifiedBy())
                .lastModifiedDate(hdr.getLastModifiedDate())
                .chargeDetails(chargeResponses)
                .controlSheetDetails(ctrlSheetResponses)
                .build();
    }

    private FFProjectsChargesDetailResponse mapChargeDetailToResponse(FFProjectsChargesDtl dtl) {
        return FFProjectsChargesDetailResponse.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .quotationReferencePoid(dtl.getQuotationReferencePoid())
                .chargeDetailsPoid(dtl.getChargeDetailsPoid())
                .printableChargeDescription(dtl.getPrintableChargeDescription())
//                .chargeBasis(dtl.getChargeBasis())
                .quantity(dtl.getQuantity())
                .unit(dtl.getUnit())
                .buyingCurrencyCode(dtl.getBuyingCurrencyCode())
                .currencyRate(dtl.getCurrencyRate())
                .buyingUnitRate(dtl.getBuyingUnitRate())
//                .buyingTotalBhd(dtl.getBuyingTotalBhd())
//                .sellingUnitRate(dtl.getSellingUnitRate())
//                .sellingTotal(dtl.getSellingTotal())
                .taxIdPoid(dtl.getTaxIdPoid())
                .taxPercentage(dtl.getTaxPercentage())
//                .taxAmount(dtl.getTaxAmount())
//                .sellingGrandTotal(dtl.getSellingGrandTotal())
//                .sellingGrandTotalBhd(dtl.getSellingGrandTotalBhd())
//                .marginBhd(dtl.getMarginBhd())
                .remarks(dtl.getRemarks())
                .createdBy(dtl.getCreatedBy())
                .createdDate(dtl.getCreatedDate())
                .lastModifiedBy(dtl.getLastModifiedBy())
                .lastModifiedDate(dtl.getLastModifiedDate())
                .build();
    }

    private FFProjectsCtrlSheetDetailResponse mapCtrlSheetDetailToResponse(FFProjectsCtrlSheetDtl dtl) {
        return FFProjectsCtrlSheetDetailResponse.builder()
                .transactionPoid(dtl.getTransactionPoid())
                .detRowId(dtl.getDetRowId())
                .freightType(dtl.getFreightType())
                .jobNoPoid(dtl.getJobNoPoid())
                .originPoid(dtl.getOrigin())
                .destinationPoid(dtl.getDestination())
                .etd(dtl.getEtd())
                .etaAta(dtl.getEtaAta())
                .arrivalDate(dtl.getArrivalDate())
                .noOfPackages(dtl.getNoOfPackages())
                .weight(dtl.getWeight())
                .cbm(dtl.getCbm())
                .carrierPoid(dtl.getCarrierCode())
                .linePoid(dtl.getLine())
                .truckNumber(dtl.getTruckNumber())
                .description(dtl.getDescription())
                .sailDate(dtl.getSailDate())
//                .jobStatus(dtl.getJobStatus())
                .createdBy(dtl.getCreatedBy())
                .createdDate(dtl.getCreatedDate())
                .lastModifiedBy(dtl.getLastModifiedBy())
                .lastModifiedDate(dtl.getLastModifiedDate())
                .build();
    }

    private FFProjectsListResponse mapToListResponse(FFProjectsHdr hdr) {
        return FFProjectsListResponse.builder()
                .transactionPoid(hdr.getTransactionPoid())
                .transactionDate(hdr.getTransactionDate())
                .docRef(hdr.getDocRef())
                .projectDescription(hdr.getProjectDescription())
                .projectStatus(hdr.getProjectStatus())
                .periodFrom(hdr.getPeriodFrom())
                .periodTo(hdr.getPeriodTo())
//                .grandTotalSellRateBhd(hdr.getGrandTotalSellRateBhd())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getAirFreightJobs(Long transactionPoid) {
        return freightJobProjectionRepository.findAirFreightJobs(transactionPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getSeaFreightJobs(Long transactionPoid) {
        return freightJobProjectionRepository.findSeaFreightJobs(transactionPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getRoadFreightJobs(Long transactionPoid) {
        return freightJobProjectionRepository.findRoadFreightJobs(transactionPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getAllFreightJobs(Long transactionPoid, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null) {
            return freightJobProjectionRepository.findAllFreightJobsByDateRange(transactionPoid, fromDate, toDate);
        }
        return freightJobProjectionRepository.findAllFreightJobs(transactionPoid);
    }

    private void updateProjectCharges(List<FFProjectsChargesDetailRequest> charges, Long transactionPoid) {
        String currentUser = UserContext.getUserName();
        LocalDateTime now = LocalDateTime.now();
        String docId = UserContext.getDocumentId();
        String docKeyPoid = transactionPoid.toString();

        List<FFProjectsChargesDtl> toSave = new ArrayList<>();
        List<FFProjectsChargesDtl> toUpdate = new ArrayList<>();
        List<Long> toDelete = new ArrayList<>();
        List<LogRequestDto<FFProjectsChargesDtl>> logRequests = new ArrayList<>();

        List<FFProjectsChargesDtl> existingCharges = projectsChargesDtlRepository.findByTransactionPoid(transactionPoid);
        long nextChargeDetRowId = existingCharges.stream().mapToLong(FFProjectsChargesDtl::getDetRowId).max().orElse(0L);

        for (FFProjectsChargesDetailRequest charge : charges) {
            switch (charge.getActionType().toUpperCase()) {
                case "ISCREATED":
                    toSave.add(FFProjectsChargesDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(++nextChargeDetRowId)
                            .quotationReferencePoid(charge.getQuotationReferencePoid())
                            .chargeDetailsPoid(charge.getChargeDetailsPoid())
                            .printableChargeDescription(charge.getPrintableChargeDescription())
//                            .chargeBasis(charge.getChargeBasis())
                            .quantity(charge.getQuantity())
                            .unit(charge.getUnit())
                            .buyingCurrencyCode(charge.getBuyingCurrencyCode())
                            .currencyRate(charge.getCurrencyRate())
                            .buyingUnitRate(charge.getBuyingUnitRate())
//                            .buyingTotalBhd(charge.getBuyingTotalBhd())
//                            .sellingUnitRate(charge.getSellingUnitRate())
//                            .sellingTotal(charge.getSellingTotal())
                            .taxIdPoid(charge.getTaxIdPoid())
                            .taxPercentage(charge.getTaxPercentage())
//                            .taxAmount(charge.getTaxAmount())
//                            .sellingGrandTotal(charge.getSellingGrandTotal())
//                            .sellingGrandTotalBhd(charge.getSellingGrandTotalBhd())
//                            .marginBhd(charge.getMarginBhd())
                            .remarks(charge.getRemarks())
//                            .deleted("N")
                            .createdBy(currentUser)
                            .createdDate(now)
                            .lastModifiedBy(currentUser)
                            .lastModifiedDate(now)
                            .build());
                    break;

                case "ISUPDATED":
                    FFProjectsChargesDtl existingCharge = projectsChargesDtlRepository
                            .findByTransactionPoidAndDetRowId(transactionPoid, charge.getDetRowId())
                            .orElseThrow(() -> new ResourceNotFoundException("Charge not found"));

                    FFProjectsChargesDtl oldCharge = new FFProjectsChargesDtl();
                    BeanUtils.copyProperties(existingCharge, oldCharge);

                    existingCharge.setQuotationReferencePoid(charge.getQuotationReferencePoid());
                    existingCharge.setChargeDetailsPoid(charge.getChargeDetailsPoid());
                    existingCharge.setPrintableChargeDescription(charge.getPrintableChargeDescription());
//                    existingCharge.setChargeBasis(charge.getChargeBasis());
                    existingCharge.setQuantity(charge.getQuantity());
                    existingCharge.setUnit(charge.getUnit());
                    existingCharge.setBuyingCurrencyCode(charge.getBuyingCurrencyCode());
                    existingCharge.setCurrencyRate(charge.getCurrencyRate());
                    existingCharge.setBuyingUnitRate(charge.getBuyingUnitRate());
//                    existingCharge.setBuyingTotalBhd(charge.getBuyingTotalBhd());
//                    existingCharge.setSellingUnitRate(charge.getSellingUnitRate());
//                    existingCharge.setSellingTotal(charge.getSellingTotal());
                    existingCharge.setTaxIdPoid(charge.getTaxIdPoid());
                    existingCharge.setTaxPercentage(charge.getTaxPercentage());
//                    existingCharge.setTaxAmount(charge.getTaxAmount());
//                    existingCharge.setSellingGrandTotal(charge.getSellingGrandTotal());
//                    existingCharge.setSellingGrandTotalBhd(charge.getSellingGrandTotalBhd());
//                    existingCharge.setMarginBhd(charge.getMarginBhd());
                    existingCharge.setRemarks(charge.getRemarks());
                    existingCharge.setLastModifiedBy(currentUser);
                    existingCharge.setLastModifiedDate(now);
                    toUpdate.add(existingCharge);

                    String logDetail = String.format("KeyId = TRANSACTION_POID: %s DET_ROW_ID: %s", transactionPoid, charge.getDetRowId());
                    logRequests.add(new LogRequestDto<>(oldCharge, existingCharge, FFProjectsChargesDtl.class, docId, docKeyPoid, logDetail));
                    break;

                case "ISDELETED":
                    toDelete.add(charge.getDetRowId());
                    loggingService.logDelete(charge, docId, docKeyPoid);
                    break;
            }
        }

        if (!toSave.isEmpty()) {
            projectsChargesDtlRepository.saveAll(toSave);
            toSave.forEach(e -> {
                String logDetail = String.format("Row Created on Charge Detail with detRowId: %s", e.getDetRowId());
                loggingService.createLogSummaryEntry(docId, docKeyPoid, logDetail);
            });
        }

        if (!toUpdate.isEmpty()) {
            projectsChargesDtlRepository.saveAll(toUpdate);
            if (!logRequests.isEmpty()) {
                loggingService.createLogBatch(logRequests);
            }
        }

        if (!toDelete.isEmpty()) {
            toDelete.forEach(detRowId -> {
                FFProjectsChargesDtl detail = projectsChargesDtlRepository
                        .findByTransactionPoidAndDetRowId(transactionPoid, detRowId)
                        .orElseThrow(() -> new ResourceNotFoundException("Charge detail not found"));
//                detail.setDeleted("Y");
                detail.setLastModifiedBy(currentUser);
                detail.setLastModifiedDate(now);
                projectsChargesDtlRepository.save(detail);
            });
        }
    }

    private void updateProjectControlSheets(List<FFProjectsCtrlSheetDetailRequest> controlSheets, Long transactionPoid) {
        String currentUser = UserContext.getUserName();
        LocalDateTime now = LocalDateTime.now();
        String docId = UserContext.getDocumentId();
        String docKeyPoid = transactionPoid.toString();

        List<FFProjectsCtrlSheetDtl> toSave = new ArrayList<>();
        List<FFProjectsCtrlSheetDtl> toUpdate = new ArrayList<>();
        List<Long> toDelete = new ArrayList<>();
        List<LogRequestDto<FFProjectsCtrlSheetDtl>> logRequests = new ArrayList<>();

        List<FFProjectsCtrlSheetDtl> existingSheets = projectsCtrlSheetDtlRepository.findByTransactionPoid(transactionPoid);
        long nextSheetDetRowId = existingSheets.stream().mapToLong(FFProjectsCtrlSheetDtl::getDetRowId).max().orElse(0L);

        for (FFProjectsCtrlSheetDetailRequest ctrl : controlSheets) {
            switch (ctrl.getActionType().toUpperCase()) {
                case "ISCREATED":
                    toSave.add(FFProjectsCtrlSheetDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(++nextSheetDetRowId)
                            .freightType(ctrl.getFreightType())
                            .jobNoPoid(ctrl.getJobNoPoid())
                            .origin(ctrl.getOriginPoid())
                            .destination(ctrl.getDestinationPoid())
                            .etd(ctrl.getEtd())
                            .etaAta(ctrl.getEtaAta())
                            .arrivalDate(ctrl.getArrivalDate())
                            .noOfPackages(ctrl.getNoOfPackages())
                            .weight(ctrl.getWeight())
                            .cbm(ctrl.getCbm())
                            .carrierCode(ctrl.getCarrierPoid())
                            .line(ctrl.getLinePoid())
                            .truckNumber(ctrl.getTruckNumber())
                            .description(ctrl.getDescription())
                            .sailDate(ctrl.getSailDate())
//                            .jobStatus(ctrl.getJobStatus())
//                            .deleted("N")
                            .createdBy(currentUser)
                            .createdDate(now)
                            .lastModifiedBy(currentUser)
                            .lastModifiedDate(now)
                            .build());
                    break;

                case "ISUPDATED":
                    FFProjectsCtrlSheetDtl existingCtrl = projectsCtrlSheetDtlRepository
                            .findByTransactionPoidAndDetRowId(transactionPoid, ctrl.getDetRowId())
                            .orElseThrow(() -> new ResourceNotFoundException("Control sheet not found"));

                    FFProjectsCtrlSheetDtl oldCtrl = new FFProjectsCtrlSheetDtl();
                    BeanUtils.copyProperties(existingCtrl, oldCtrl);

                    existingCtrl.setFreightType(ctrl.getFreightType());
                    existingCtrl.setJobNoPoid(ctrl.getJobNoPoid());
                    existingCtrl.setOrigin(ctrl.getOriginPoid());
                    existingCtrl.setDestination(ctrl.getDestinationPoid());
                    existingCtrl.setEtd(ctrl.getEtd());
                    existingCtrl.setEtaAta(ctrl.getEtaAta());
                    existingCtrl.setArrivalDate(ctrl.getArrivalDate());
                    existingCtrl.setNoOfPackages(ctrl.getNoOfPackages());
                    existingCtrl.setWeight(ctrl.getWeight());
                    existingCtrl.setCbm(ctrl.getCbm());
                    existingCtrl.setCarrierCode(ctrl.getCarrierPoid());
                    existingCtrl.setLine(ctrl.getLinePoid());
                    existingCtrl.setTruckNumber(ctrl.getTruckNumber());
                    existingCtrl.setDescription(ctrl.getDescription());
                    existingCtrl.setSailDate(ctrl.getSailDate());
//                    existingCtrl.setJobStatus(ctrl.getJobStatus());
                    existingCtrl.setLastModifiedBy(currentUser);
                    existingCtrl.setLastModifiedDate(now);
                    toUpdate.add(existingCtrl);

                    String logDetail = String.format("KeyId = TRANSACTION_POID: %s DET_ROW_ID: %s", transactionPoid, ctrl.getDetRowId());
                    logRequests.add(new LogRequestDto<>(oldCtrl, existingCtrl, FFProjectsCtrlSheetDtl.class, docId, docKeyPoid, logDetail));
                    break;

                case "ISDELETED":
                    toDelete.add(ctrl.getDetRowId());
                    loggingService.logDelete(ctrl, docId, docKeyPoid);
                    break;
            }
        }

        if (!toSave.isEmpty()) {
            projectsCtrlSheetDtlRepository.saveAll(toSave);
            toSave.forEach(e -> {
                String logDetail = String.format("Row Created on Control Sheet with detRowId: %s", e.getDetRowId());
                loggingService.createLogSummaryEntry(docId, docKeyPoid, logDetail);
            });
        }

        if (!toUpdate.isEmpty()) {
            projectsCtrlSheetDtlRepository.saveAll(toUpdate);
            if (!logRequests.isEmpty()) {
                loggingService.createLogBatch(logRequests);
            }
        }

        if (!toDelete.isEmpty()) {
            toDelete.forEach(detRowId -> {
                FFProjectsCtrlSheetDtl detail = projectsCtrlSheetDtlRepository
                        .findByTransactionPoidAndDetRowId(transactionPoid, detRowId)
                        .orElseThrow(() -> new ResourceNotFoundException("Control sheet detail not found"));
//                detail.setDeleted("Y");
                detail.setLastModifiedBy(currentUser);
                detail.setLastModifiedDate(now);
                projectsCtrlSheetDtlRepository.save(detail);
            });
        }
    }
}
