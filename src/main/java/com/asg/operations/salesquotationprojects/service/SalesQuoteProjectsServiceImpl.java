package com.asg.operations.salesquotationprojects.service;

import com.asg.common.lib.dto.*;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LovDataService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.exceptions.CustomException;
import com.asg.operations.finaldisbursementaccount.repository.GLBankMasterRepository;
import com.asg.operations.finaldisbursementaccount.repository.SalesSalesmanMasterRepository;
import com.asg.operations.finaldisbursementaccount.repository.ShipLineMasterRepository;
import com.asg.operations.finaldisbursementaccount.repository.TermsTemplateRepository;
import com.asg.operations.pdaporttariffmaster.repository.ShipChargeMasterRepository;
import com.asg.operations.salesquotationprojects.dto.*;
import com.asg.operations.salesquotationprojects.entity.SalesQuoteProjectsHdr;
import com.asg.operations.salesquotationprojects.key.ShipCommodityMasterId;
import com.asg.operations.salesquotationprojects.repository.*;
import com.asg.operations.portcallreport.enums.ActionType;
import com.asg.operations.salesquotationprojects.entity.SalesQuoteProjectsChargeDtl;
import com.asg.operations.salesquotationprojects.entity.SalesQuoteProjectsNotesDtl;
import com.asg.operations.salesquotationprojects.entity.SalesQuoteProjectsTcDtl;
import com.asg.operations.salesquotationprojects.key.SalesQuoteProjectsChargeDtlId;
import com.asg.operations.salesquotationprojects.key.SalesQuoteProjectsNotesDtlId;
import com.asg.operations.salesquotationprojects.key.SalesQuoteProjectsTcDtlId;
import com.asg.operations.shipprincipal.repository.AddressMasterRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import oracle.jdbc.OracleTypes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesQuoteProjectsServiceImpl implements SalesQuoteProjectsService {

    private final JdbcTemplate jdbcTemplate;
    private final SalesQuoteProjectsHdrRepository repository;
    private final SalesQuoteProjectsChargeDtlRepository chargeDtlRepository;
    private final SalesQuoteProjectsNotesDtlRepository notesDtlRepository;
    private final SalesQuoteProjectsTcDtlRepository tcDtlRepository;
    private final DocumentSearchService documentSearchService;
    private final DocumentDeleteService documentDeleteService;
    private final AddressMasterRepository addressMasterRepository;
    private final ApSupplierMasterRepository apSupplierMasterRepository;
    private final SalesSalesmanMasterRepository salesSalesmanMasterRepository;
    private final ShipCommodityMasterRepository shipCommodityMasterRepository;
    private final ShipLineMasterRepository shipLineMasterRepository;
    private final AirLineMasterRepository airLineMasterRepository;
    private final TermsTemplateRepository termsTemplateRepository;
    private final GLBankMasterRepository glBankMasterRepository;
    private final ProjectsHdrRepository projectsHdrRepository;
    private final GlobalCurrencyMasterRepository globalCurrencyMasterRepository;
    private final ShipChargeMasterRepository shipChargeMasterRepository;
    private final GlobalTaxMasterRepository globalTaxMasterRepository;
    private final LovDataService lovDataService;


    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> listSalesQuoteProjectsWithFilters(String documentId, FilterRequestDto filterRequestDto, Pageable pageable, LocalDate periodFrom, LocalDate periodTo) {

        String operator = documentSearchService.resolveOperator(filterRequestDto);
        String isDeleted = documentSearchService.resolveIsDeleted(filterRequestDto);
        List<FilterDto> filters = documentSearchService.resolveDateFilters(filterRequestDto, "TRANSACTION_DATE", periodFrom, periodTo);

        RawSearchResult raw = documentSearchService.search(documentId, filters, operator, pageable, isDeleted, "DOC_REF", "TRANSACTION_POID");

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Override
    @Transactional(readOnly = true)
    public SalesQuoteProjectsResponse getSalesQuoteProjectById(Long transactionPoid) {
        SalesQuoteProjectsHdr entity = repository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Quote Project not found with ID: " + transactionPoid));

        return mapToResponse(entity);
    }

    @Override
    public SalesQuoteProjectsResponse createSalesQuoteProject(SalesQuoteProjectsRequest request) {
        if (request.getCustomerPoid() != null) {
            if (!addressMasterRepository.existsByAddressMasterPoid(request.getCustomerPoid())) {
                throw new ResourceNotFoundException("Customer", "Customer Poid", request.getCustomerPoid());
            }
        }
        if (request.getPrincipalPoid() != null) {
            if (!apSupplierMasterRepository.existsBySupplierPoid(request.getPrincipalPoid())) {
                throw new ResourceNotFoundException("Principal", "Principal Poid", request.getPrincipalPoid());
            }
        }
        if (request.getSalesmanPoid() != null) {
            if (!salesSalesmanMasterRepository.existsBySalesmanPoid(request.getSalesmanPoid())) {
                throw new ResourceNotFoundException("Salesman", "Salesman Poid", request.getSalesmanPoid());
            }
        }
        if (request.getLinePoid() != null) {
            if (!shipLineMasterRepository.existsByLinePoid(request.getLinePoid())) {
                throw new ResourceNotFoundException("Line", "Line Poid", request.getLinePoid());
            }
        }
        if (request.getCarrierPoid() != null) {
            if (!airLineMasterRepository.existsByAirlinePoid(request.getCarrierPoid())) {
                throw new ResourceNotFoundException("Carrier", "Carrier Poid", request.getCarrierPoid());
            }
        }
        if (request.getTermsPoid() != null) {
            if (!termsTemplateRepository.existsByTermsPoid(request.getTermsPoid())) {
                throw new ResourceNotFoundException("Terms & Template", "Terms Poid", request.getTermsPoid());
            }
        }
        if (request.getBankAccountPoid() != null) {
            if (!glBankMasterRepository.existsByBankPoid(request.getBankAccountPoid())) {
                throw new ResourceNotFoundException("Bank", "Bank Poid", request.getBankAccountPoid());
            }
        }
//        if (StringUtils.isNotBlank(request.getProjectReferenceNumber())) {
//            if (!projectsHdrRepository.existsByProjectReferenceIgnoreCase(request.getProjectReferenceNumber())) {
//                throw new ResourceNotFoundException("Project", "Project Reference Number", request.getProjectReferenceNumber());
//            }
//        }
        if (StringUtils.isNotBlank(request.getBillingCurrencyCode())) {
            if (!globalCurrencyMasterRepository.existsByCurrencyCodeIgnoreCase(request.getBillingCurrencyCode())) {
                throw new ResourceNotFoundException("Currency", "Currency Code", request.getBillingCurrencyCode());
            }
        }
        if (request.getCommodity() != null && !request.getCommodity().isEmpty()) {
            for (String commodity : request.getCommodity()) {
                long commodityPoid;
                try {
                    commodityPoid = Long.parseLong(commodity);
                } catch (Exception e) {
                    throw new ResourceNotFoundException("Commodity", "Commodity", request.getCommodity());
                }
                if (!shipCommodityMasterRepository.existsById(new ShipCommodityMasterId(commodityPoid, UserContext.getGroupPoid()))) {
                    throw new ResourceNotFoundException("Customer", "Customer Poid", request.getCommodity());
                }
            }
        }

        SalesQuoteProjectsHdr entity = mapToEntity(request);
        entity.setCompanyPoid(UserContext.getCompanyPoid());
        entity.setDeleted("N");
        entity.setCreatedBy(UserContext.getUserId());
        entity.setCreatedDate(LocalDateTime.now());
        entity.setLastModifiedBy(UserContext.getUserId());
        entity.setLastModifiedDate(LocalDateTime.now());
        entity.setTransactionDate(LocalDate.now());

        SalesQuoteProjectsHdr savedEntity = repository.save(entity);

        // Save child details
        if (request.getChargeDetails() != null && !request.getChargeDetails().isEmpty()) {
            saveChargeDetails(savedEntity, request.getChargeDetails());
        }
        if (request.getNotesDetails() != null && !request.getNotesDetails().isEmpty()) {
            saveNotesDetails(savedEntity, request.getNotesDetails());
        }
        if (request.getTcDetails() != null && !request.getTcDetails().isEmpty()) {
            saveTcDetails(savedEntity, request.getTcDetails());
        }

        return mapToResponse(savedEntity);
    }

    @Override
    public SalesQuoteProjectsResponse updateSalesQuoteProject(Long transactionPoid, SalesQuoteProjectsRequest request) {
        SalesQuoteProjectsHdr existingEntity = repository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Quote Project not found with ID: " + transactionPoid));

        if (request.getCustomerPoid() != null) {
            if (!addressMasterRepository.existsByAddressMasterPoid(request.getCustomerPoid())) {
                throw new ResourceNotFoundException("Customer", "Customer Poid", request.getCustomerPoid());
            }
        }
        if (request.getPrincipalPoid() != null) {
            if (!apSupplierMasterRepository.existsBySupplierPoid(request.getPrincipalPoid())) {
                throw new ResourceNotFoundException("Principal", "Principal Poid", request.getPrincipalPoid());
            }
        }
        if (request.getSalesmanPoid() != null) {
            if (!salesSalesmanMasterRepository.existsBySalesmanPoid(request.getSalesmanPoid())) {
                throw new ResourceNotFoundException("Salesman", "Salesman Poid", request.getSalesmanPoid());
            }
        }
        if (request.getLinePoid() != null) {
            if (!shipLineMasterRepository.existsByLinePoid(request.getLinePoid())) {
                throw new ResourceNotFoundException("Line", "Line Poid", request.getLinePoid());
            }
        }
        if (request.getCarrierPoid() != null) {
            if (!airLineMasterRepository.existsByAirlinePoid(request.getCarrierPoid())) {
                throw new ResourceNotFoundException("Carrier", "Carrier Poid", request.getCarrierPoid());
            }
        }
        if (request.getTermsPoid() != null) {
            if (!termsTemplateRepository.existsByTermsPoid(request.getTermsPoid())) {
                throw new ResourceNotFoundException("Terms & Template", "Terms Poid", request.getTermsPoid());
            }
        }
        if (request.getBankAccountPoid() != null) {
            if (!glBankMasterRepository.existsByBankPoid(request.getBankAccountPoid())) {
                throw new ResourceNotFoundException("Bank", "Bank Poid", request.getBankAccountPoid());
            }
        }
//        if (StringUtils.isNotBlank(request.getProjectReferenceNumber())) {
//            if (!projectsHdrRepository.existsByProjectReferenceIgnoreCase(request.getProjectReferenceNumber())) {
//                throw new ResourceNotFoundException("Project", "Project Reference Number", request.getProjectReferenceNumber());
//            }
//        }
        if (StringUtils.isNotBlank(request.getBillingCurrencyCode())) {
            if (!globalCurrencyMasterRepository.existsByCurrencyCodeIgnoreCase(request.getBillingCurrencyCode())) {
                throw new ResourceNotFoundException("Currency", "Currency Code", request.getBillingCurrencyCode());
            }
        }
        if (request.getCommodity() != null && !request.getCommodity().isEmpty()) {
            for (String commodity : request.getCommodity()) {
                long commodityPoid;
                try {
                    commodityPoid = Long.parseLong(commodity);
                } catch (Exception e) {
                    throw new ResourceNotFoundException("Commodity", "Commodity", request.getCommodity());
                }
                if (!shipCommodityMasterRepository.existsById(new ShipCommodityMasterId(commodityPoid, UserContext.getGroupPoid()))) {
                    throw new ResourceNotFoundException("Customer", "Customer Poid", request.getCommodity());
                }
            }
        }

        updateEntityFromRequest(existingEntity, request);
        existingEntity.setLastModifiedBy(UserContext.getUserId());
        existingEntity.setLastModifiedDate(LocalDateTime.now());
        existingEntity.setTransactionDate(LocalDate.now());

        SalesQuoteProjectsHdr savedEntity = repository.save(existingEntity);

        // Update child details with action types
        if (request.getChargeDetails() != null) {
            updateChargeDetails(existingEntity.getTransactionPoid(), request.getChargeDetails());
        }
        if (request.getNotesDetails() != null) {
            updateNotesDetails(existingEntity.getTransactionPoid(), request.getNotesDetails());
        }
        if (request.getTcDetails() != null) {
            updateTcDetails(existingEntity.getTransactionPoid(), request.getTcDetails());
        }

        return mapToResponse(savedEntity);
    }

    @Override
    public void deleteSalesQuoteProject(Long transactionPoid, DeleteReasonDto deleteReasonDto) {
        SalesQuoteProjectsHdr entity = repository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Quote Project not found with ID: " + transactionPoid));

        documentDeleteService.deleteDocument(
                transactionPoid,
                "SALES_QUOTE_PROJECTS_HDR",
                "TRANSACTION_POID",
                deleteReasonDto,
                entity.getTransactionDate()
        );
    }

    private SalesQuoteProjectsResponse mapToResponse(SalesQuoteProjectsHdr entity) {
        SalesQuoteProjectsResponse response = new SalesQuoteProjectsResponse();
        response.setTransactionPoid(entity.getTransactionPoid());
        response.setTransactionDate(entity.getTransactionDate());
        response.setCompanyPoid(entity.getCompanyPoid());
        response.setCompanyDet(lovDataService.getDetailsByPoidAndLovName(entity.getCompanyPoid(), "ADDRESS_MASTER"));
        response.setDocRef(entity.getDocRef());
        response.setCustomerType(entity.getCustomerType());
        response.setCustomerPoid(entity.getCustomerPoid());
        response.setCustomerDet(lovDataService.getDetailsByPoidAndLovName(entity.getCustomerPoid(), "ADDRESS_MASTER"));
        response.setCustomerName(entity.getCustomerName());
        response.setCustomerContact(entity.getCustomerContact());
        response.setCustomerEmail(entity.getCustomerEmail());
        response.setCustomerTelephone(entity.getCustomerTelephone());
        response.setCustomerMobile(entity.getCustomerMobile());
        response.setPrincipalPoid(entity.getPrincipalPoid());
        response.setPrincipalDet(lovDataService.getDetailsByPoidAndLovName(entity.getPrincipalPoid(), "PRINCIPAL_MASTER_FF_QTN"));
        response.setShipmentMode(entity.getShipmentMode());
        response.setTransportationMode(entity.getTransportationMode());
        response.setOtherMode(entity.getOtherMode());
        response.setLinePoid(entity.getLinePoid());
        response.setLineDet(lovDataService.getDetailsByPoidAndLovName(entity.getLinePoid(), "LINE_MASTER"));
        response.setCarrierPoid(entity.getCarrierPoid());
        response.setCarrierDet(lovDataService.getDetailsByPoidAndLovName(entity.getCarrierPoid(), "AIRLINE"));
        response.setQuoteReference(entity.getQuoteReference());
        response.setUnits(entity.getUnits());
        response.setWeight(entity.getWeight());
        response.setCbm(entity.getCbm());
        response.setQuantity(entity.getQuantity());
        response.setFreightTons(entity.getFreightTons());
        response.setAutoRate(entity.getAutoRate());
        response.setBillingCurrencyCode(entity.getBillingCurrencyCode());
        response.setBillingCurrencyDet(lovDataService.getDetailsByCodeAndLovName(entity.getBillingCurrencyCode(), "CURRENCY"));
        response.setAgreedRate(entity.getAgreedRate());
        response.setSalesmanPoid(entity.getSalesmanPoid());
        response.setSalesmanDet(lovDataService.getDetailsByPoidAndLovName(entity.getSalesmanPoid(), "SALESMAN"));
        response.setShippingTerms(entity.getShippingTerms());
        response.setTermsDet(lovDataService.getDetailsByPoidAndLovName(entity.getTermsPoid(), "TERMS_TEMPLATE_MASTER"));
        response.setCommodity(StringUtils.isBlank(entity.getCommodity()) ? null : Arrays.asList(entity.getCommodity().split(", ")));
        response.setQuotationStatus(entity.getQuotationStatus());
        response.setProjectDetails(entity.getProjectDetails());
        response.setIsSupplementaryQuote(entity.getIsSupplementaryQuote());
        response.setProjectReferenceNumber(entity.getProjectReferenceNumber());
        response.setProjectDet(lovDataService.getDetailsByCodeAndLovName(entity.getProjectReferenceNumber(), "PROJECTS_REF_QUOTE"));
        response.setValidityToDate(entity.getValidityToDate());
        response.setTermsPoid(entity.getTermsPoid());
        response.setTotalBuyingAmountLc(entity.getTotalBuyingAmountLc());
        response.setTotalTaxLc(entity.getTotalTaxLc());
        response.setGrantTotalSellAmountLc(entity.getGrantTotalSellAmountLc());
        response.setGrantTotalSellAmountFc(entity.getGrantTotalSellAmountFc());
        response.setRemarks(entity.getRemarks());
        response.setActionStatus(entity.getActionStatus());
        response.setActionDueDate(entity.getActionDueDate());
        response.setBankAccountPoid(entity.getBankAccountPoid());
        response.setBankAccountDet(lovDataService.getDetailsByPoidAndLovName(entity.getBankAccountPoid(), "BANK_MASTER"));
        response.setDeleted(entity.getDeleted());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());

        // Fetch and set child entities
        response.setChargeDetails(chargeDtlRepository.findByIdTransactionPoid(entity.getTransactionPoid()).stream().map(this::mapChargeDetailToResponse).toList());
        response.setNotesDetails(notesDtlRepository.findByIdTransactionPoid(entity.getTransactionPoid()).stream().map(this::mapNotesDetailToResponse).toList());
        response.setTcDetails(tcDtlRepository.findByIdTransactionPoid(entity.getTransactionPoid()).stream().map(this::mapTcDetailToResponse).toList());

        return response;
    }

    private SalesQuoteProjectsHdr mapToEntity(SalesQuoteProjectsRequest request) {
        SalesQuoteProjectsHdr entity = new SalesQuoteProjectsHdr();
        updateEntityFromRequest(entity, request);
        return entity;
    }

    private void updateEntityFromRequest(SalesQuoteProjectsHdr entity, SalesQuoteProjectsRequest request) {
        entity.setCustomerType(request.getCustomerType());
        entity.setCustomerPoid(request.getCustomerPoid());
        entity.setCustomerName(request.getCustomerName());
        entity.setCustomerContact(request.getCustomerContact());
        entity.setCustomerEmail(request.getCustomerEmail());
        entity.setCustomerTelephone(request.getCustomerTelephone());
        entity.setCustomerMobile(request.getCustomerMobile());
        entity.setPrincipalPoid(request.getPrincipalPoid());
        entity.setShipmentMode(request.getShipmentMode());
        entity.setTransportationMode(request.getTransportationMode());
        entity.setOtherMode(request.getOtherMode());
        entity.setLinePoid(request.getLinePoid());
        entity.setCarrierPoid(request.getCarrierPoid());
        entity.setQuoteReference(request.getQuoteReference());
        entity.setUnits(request.getUnits());
        entity.setWeight(request.getWeight());
        entity.setCbm(request.getCbm());
        entity.setQuantity(request.getQuantity());
        entity.setFreightTons(request.getFreightTons());
        entity.setAutoRate(request.getAutoRate());
        entity.setBillingCurrencyCode(request.getBillingCurrencyCode());
        entity.setAgreedRate(request.getAgreedRate());
        entity.setSalesmanPoid(request.getSalesmanPoid());
        entity.setShippingTerms(request.getShippingTerms());
        entity.setCommodity(request.getCommodity() != null && !request.getCommodity().isEmpty() ? String.join(",", request.getCommodity()) : null);
        entity.setQuotationStatus(request.getQuotationStatus());
        entity.setProjectDetails(request.getProjectDetails());
        entity.setIsSupplementaryQuote(request.getIsSupplementaryQuote());
        entity.setProjectReferenceNumber(request.getProjectReferenceNumber());
        entity.setValidityToDate(request.getValidityToDate());
        entity.setTermsPoid(request.getTermsPoid());
        entity.setTotalBuyingAmountLc(request.getTotalBuyingAmountLc());
        entity.setTotalTaxLc(request.getTotalTaxLc());
        entity.setGrantTotalSellAmountLc(request.getGrantTotalSellAmountLc());
        entity.setGrantTotalSellAmountFc(request.getGrantTotalSellAmountFc());
        entity.setRemarks(request.getRemarks());
        entity.setActionStatus(request.getActionStatus());
        entity.setActionDueDate(request.getActionDueDate());
        entity.setBankAccountPoid(request.getBankAccountPoid());
    }

    private SalesQuoteProjectsChargeDetailResponse mapChargeDetailToResponse(SalesQuoteProjectsChargeDtl entity) {
        SalesQuoteProjectsChargeDetailResponse response = new SalesQuoteProjectsChargeDetailResponse();
        response.setTransactionPoid(entity.getId().getTransactionPoid());
        response.setDetRowId(entity.getId().getDetRowId());
        response.setChargePoid(entity.getChargePoid());
        response.setChargeDet(lovDataService.getDetailsByPoidAndLovName(entity.getChargePoid(), "CHARGE_MASTER_FF"));
        response.setPrintableChargeDesc(entity.getPrintableChargeDesc());
        response.setQuantity(entity.getQuantity());
        response.setUnitPoid(entity.getUnitPoid());
        response.setChargeDet(lovDataService.getDetailsByPoidAndLovName(entity.getUnitPoid(), "PROJECTS_CHARGES_UNIT"));
        response.setBuyCurrencyCode(entity.getBuyCurrencyCode());
        response.setBuyCurrencyDet(lovDataService.getDetailsByCodeAndLovName(entity.getBuyCurrencyCode(), "CURRENCY"));
        response.setBuyCurrencyRate(entity.getBuyCurrencyRate());
        response.setBuyUnitRate(entity.getBuyUnitRate());
        response.setBuyTotalLc(entity.getBuyTotalLc());
        response.setSellUnitRateFc(entity.getSellUnitRateFc());
        response.setSellTotalFc(entity.getSellTotalFc());
        response.setTaxPoid(entity.getTaxPoid());
        response.setChargeDet(lovDataService.getDetailsByPoidAndLovName(entity.getTaxPoid(), "TAX_MASTER"));
        response.setTaxPercentage(entity.getTaxPercentage());
        response.setTaxAmountFc(entity.getTaxAmountFc());
        response.setSellGrandTotalFc(entity.getSellGrandTotalFc());
        response.setTaxAmountLc(entity.getTaxAmountLc());
        response.setSellGrandTotalLc(entity.getSellGrandTotalLc());
        response.setRemarks(entity.getRemarks());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());
        return response;
    }

    private SalesQuoteProjectsNotesDetailResponse mapNotesDetailToResponse(SalesQuoteProjectsNotesDtl entity) {
        SalesQuoteProjectsNotesDetailResponse response = new SalesQuoteProjectsNotesDetailResponse();
        response.setTransactionPoid(entity.getId().getTransactionPoid());
        response.setDetRowId(entity.getId().getDetRowId());
        response.setNotes(entity.getNotes());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());
        return response;
    }

    private SalesQuoteProjectsTcDetailResponse mapTcDetailToResponse(SalesQuoteProjectsTcDtl entity) {
        SalesQuoteProjectsTcDetailResponse response = new SalesQuoteProjectsTcDetailResponse();
        response.setTransactionPoid(entity.getId().getTransactionPoid());
        response.setDetRowId(entity.getId().getDetRowId());
        response.setClauseRef(entity.getClauseRef());
        response.setTermsDescription(entity.getTermsDescription());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());
        return response;
    }

    private void updateChargeDetails(Long transactionPoid, List<SalesQuoteProjectsChargeDetailRequest> chargeDetails) {
        for (SalesQuoteProjectsChargeDetailRequest request : chargeDetails) {

            if (request.getChargePoid() != null) {
                if (!shipChargeMasterRepository.existsByChargePoid(BigDecimal.valueOf(request.getChargePoid()))) {
                    throw new ResourceNotFoundException("Charge", "Charge Poid", request.getChargePoid());
                }
            }
            if (request.getTaxPoid() != null) {
                if (!globalTaxMasterRepository.existsByTaxPoid(request.getTaxPoid())) {
                    throw new ResourceNotFoundException("Tax", "Tax Poid", request.getTaxPoid());
                }
            }
            if (StringUtils.isNotBlank(request.getBuyCurrencyCode())) {
                if (!globalCurrencyMasterRepository.existsByCurrencyCodeIgnoreCase(request.getBuyCurrencyCode())) {
                    throw new ResourceNotFoundException("Currency", "Currency Code", request.getBuyCurrencyCode());
                }
            }

            ActionType action = request.getActionType();
            if (action == null) continue;

            if (action == ActionType.isCreated) {
                Long nextDetRowId = chargeDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                SalesQuoteProjectsChargeDtl entity = new SalesQuoteProjectsChargeDtl();
                SalesQuoteProjectsChargeDtlId id = new SalesQuoteProjectsChargeDtlId(transactionPoid, nextDetRowId);
                entity.setId(id);
                mapChargeRequestToEntity(request, entity);
                entity.setCreatedBy(UserContext.getUserId());
                entity.setCreatedDate(java.time.LocalDate.now());
                chargeDtlRepository.save(entity);
            } else if (action == ActionType.isUpdated) {
                SalesQuoteProjectsChargeDtlId id = new SalesQuoteProjectsChargeDtlId(transactionPoid, request.getDetRowId());
                chargeDtlRepository.findById(id).ifPresent(existing -> {
                    mapChargeRequestToEntity(request, existing);
                    existing.setLastModifiedBy(UserContext.getUserId());
                    existing.setLastModifiedDate(java.time.LocalDate.now());
                    chargeDtlRepository.save(existing);
                });
            } else if (action == ActionType.isDeleted) {
                SalesQuoteProjectsChargeDtlId id = new SalesQuoteProjectsChargeDtlId(transactionPoid, request.getDetRowId());
                chargeDtlRepository.deleteById(id);
            }
        }
    }

    private void updateNotesDetails(Long transactionPoid, List<SalesQuoteProjectsNotesDetailRequest> notesDetails) {
        for (SalesQuoteProjectsNotesDetailRequest request : notesDetails) {
            ActionType action = request.getActionType();
            if (action == null) continue;

            if (action == ActionType.isCreated) {
                Long nextDetRowId = notesDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                SalesQuoteProjectsNotesDtl entity = new SalesQuoteProjectsNotesDtl();
                SalesQuoteProjectsNotesDtlId id = new SalesQuoteProjectsNotesDtlId(transactionPoid, nextDetRowId);
                entity.setId(id);
                entity.setNotes(request.getNotes());
                entity.setCreatedBy(UserContext.getUserId());
                entity.setCreatedDate(java.time.LocalDate.now());
                notesDtlRepository.save(entity);
            } else if (action == ActionType.isUpdated) {
                SalesQuoteProjectsNotesDtlId id = new SalesQuoteProjectsNotesDtlId(transactionPoid, request.getDetRowId());
                notesDtlRepository.findById(id).ifPresent(existing -> {
                    existing.setNotes(request.getNotes());
                    existing.setLastModifiedBy(UserContext.getUserId());
                    existing.setLastModifiedDate(java.time.LocalDate.now());
                    notesDtlRepository.save(existing);
                });
            } else if (action == ActionType.isDeleted) {
                SalesQuoteProjectsNotesDtlId id = new SalesQuoteProjectsNotesDtlId(transactionPoid, request.getDetRowId());
                notesDtlRepository.deleteById(id);
            }
        }
    }

    private void updateTcDetails(Long transactionPoid, List<SalesQuoteProjectsTcDetailRequest> tcDetails) {
        for (SalesQuoteProjectsTcDetailRequest request : tcDetails) {
            ActionType action = request.getActionType();
            if (action == null) continue;

            if (action == ActionType.isCreated) {
                Long nextDetRowId = tcDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid) + 1;
                SalesQuoteProjectsTcDtl entity = new SalesQuoteProjectsTcDtl();
                SalesQuoteProjectsTcDtlId id = new SalesQuoteProjectsTcDtlId(transactionPoid, nextDetRowId);
                entity.setId(id);
                entity.setClauseRef(request.getClauseRef());
                entity.setTermsDescription(request.getTermsDescription());
                entity.setCreatedBy(UserContext.getUserId());
                entity.setCreatedDate(java.time.LocalDate.now());
                tcDtlRepository.save(entity);
            } else if (action == ActionType.isUpdated) {
                SalesQuoteProjectsTcDtlId id = new SalesQuoteProjectsTcDtlId(transactionPoid, request.getDetRowId());
                tcDtlRepository.findById(id).ifPresent(existing -> {
                    existing.setClauseRef(request.getClauseRef());
                    existing.setTermsDescription(request.getTermsDescription());
                    existing.setLastModifiedBy(UserContext.getUserId());
                    existing.setLastModifiedDate(java.time.LocalDate.now());
                    tcDtlRepository.save(existing);
                });
            } else if (action == ActionType.isDeleted) {
                SalesQuoteProjectsTcDtlId id = new SalesQuoteProjectsTcDtlId(transactionPoid, request.getDetRowId());
                tcDtlRepository.deleteById(id);
            }
        }
    }

    private void mapChargeRequestToEntity(SalesQuoteProjectsChargeDetailRequest request, SalesQuoteProjectsChargeDtl entity) {
        entity.setChargePoid(request.getChargePoid());
        entity.setPrintableChargeDesc(request.getPrintableChargeDesc());
        entity.setQuantity(request.getQuantity());
        entity.setUnitPoid(request.getUnitPoid());
        entity.setBuyCurrencyCode(request.getBuyCurrencyCode());
        entity.setBuyCurrencyRate(request.getBuyCurrencyRate());
        entity.setBuyUnitRate(request.getBuyUnitRate());
        entity.setBuyTotalLc(request.getBuyTotalLc());
        entity.setSellUnitRateFc(request.getSellUnitRateFc());
        entity.setSellTotalFc(request.getSellTotalFc());
        entity.setTaxPoid(request.getTaxPoid());
        entity.setTaxPercentage(request.getTaxPercentage());
        entity.setTaxAmountFc(request.getTaxAmountFc());
        entity.setSellGrandTotalFc(request.getSellGrandTotalFc());
        entity.setTaxAmountLc(request.getTaxAmountLc());
        entity.setSellGrandTotalLc(request.getSellGrandTotalLc());
        entity.setRemarks(request.getRemarks());
    }

    // Stored Procedure Implementations
    public Map<String, Object> getCustomerAddress(Long customerPoid) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_GET_QTN_CUST_ADDRESS_V2")
                    .declareParameters(
                            new SqlParameter("P_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_ADDRESS_MASTER_POID", Types.NUMERIC),
                            new SqlParameter("P_ADDRESS_TYPE", Types.VARCHAR),
                            new SqlOutParameter("OUTDATA", OracleTypes.CURSOR)
                    );
            Map<String, Object> params = new HashMap<>();
            params.put("P_USER_POID", UserContext.getUserPoid());
            params.put("P_ADDRESS_MASTER_POID", customerPoid);
            params.put("P_ADDRESS_TYPE", "SALES");
            return jdbcCall.execute(params);
        } catch (Exception e) {
            throw new CustomException("Error retrieving customer address: " + e.getMessage(), 500);
        }
    }

    public Map<String, Object> getTermsAndConditions(Long termsPoid) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_GLOB_TERMS_LOADLIST")
                    .declareParameters(
                            new SqlParameter("P_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_DOC_ID", Types.VARCHAR),
                            new SqlParameter("P_DOC_KEY_POID", Types.NUMERIC),
                            new SqlParameter("P_TERMS_POID", Types.NUMERIC),
                            new SqlOutParameter("OUTDATA", OracleTypes.CURSOR),
                            new SqlOutParameter("P_STATUS", Types.VARCHAR)
                    );
            Map<String, Object> params = new HashMap<>();
            params.put("P_GROUP_POID", UserContext.getGroupPoid());
            params.put("P_COMPANY_POID", UserContext.getCompanyPoid());
            params.put("P_DOC_ID", UserContext.getDocumentId());
            params.put("P_DOC_KEY_POID", 0);
            params.put("P_TERMS_POID", termsPoid);
            return jdbcCall.execute(params);
        } catch (Exception e) {
            throw new CustomException("Error retrieving terms and conditions: " + e.getMessage(), 500);
        }
    }

    public Map<String, Object> getChargeTaxDetails(Long chargePoid) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_GET_CHARGE_TAX_PER_V2")
                    .declareParameters(
                            new SqlParameter("P_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_PARTY_TYPE", Types.VARCHAR),
                            new SqlParameter("P_PARTY_POID", Types.NUMERIC),
                            new SqlParameter("P_CHARGE_POID", Types.NUMERIC),
                            new SqlOutParameter("OUTDATA", OracleTypes.CURSOR)
                    );
            Map<String, Object> params = new HashMap<>();
            params.put("P_COMPANY_POID", UserContext.getCompanyPoid());
            params.put("P_PARTY_TYPE", UserContext.getUserRole());
            params.put("P_PARTY_POID", UserContext.getUserPoid());
            params.put("P_CHARGE_POID", chargePoid);
            return jdbcCall.execute(params);
        } catch (Exception e) {
            throw new CustomException("Error retrieving charge tax details: " + e.getMessage(), 500);
        }
    }

    private void saveChargeDetails(SalesQuoteProjectsHdr savedEntity, List<SalesQuoteProjectsChargeDetailRequest> chargeDetails) {
        for (SalesQuoteProjectsChargeDetailRequest request : chargeDetails) {

            if (request.getChargePoid() != null) {
                if (!shipChargeMasterRepository.existsByChargePoid(BigDecimal.valueOf(request.getChargePoid()))) {
                    throw new ResourceNotFoundException("Charge", "Charge Poid", request.getChargePoid());
                }
            }
            if (request.getTaxPoid() != null) {
                if (!globalTaxMasterRepository.existsByTaxPoid(request.getTaxPoid())) {
                    throw new ResourceNotFoundException("Tax", "Tax Poid", request.getTaxPoid());
                }
            }
            if (StringUtils.isNotBlank(request.getBuyCurrencyCode())) {
                if (!globalCurrencyMasterRepository.existsByCurrencyCodeIgnoreCase(request.getBuyCurrencyCode())) {
                    throw new ResourceNotFoundException("Currency", "Currency Code", request.getBuyCurrencyCode());
                }
            }

            Long nextDetRowId = chargeDtlRepository.findMaxDetRowIdByTransactionPoid(savedEntity.getTransactionPoid()) + 1;
            SalesQuoteProjectsChargeDtl entity = new SalesQuoteProjectsChargeDtl();
            SalesQuoteProjectsChargeDtlId id = new SalesQuoteProjectsChargeDtlId(savedEntity.getTransactionPoid(), nextDetRowId);
            entity.setId(id);
            mapChargeRequestToEntity(request, entity);
            entity.setCreatedBy(UserContext.getUserId());
            entity.setCreatedDate(java.time.LocalDate.now());
            entity.setLastModifiedBy(UserContext.getUserId());
            entity.setLastModifiedDate(java.time.LocalDate.now());
            chargeDtlRepository.save(entity);
        }
    }

    private void saveNotesDetails(SalesQuoteProjectsHdr savedEntity, List<SalesQuoteProjectsNotesDetailRequest> notesDetails) {
        for (SalesQuoteProjectsNotesDetailRequest request : notesDetails) {
            Long nextDetRowId = notesDtlRepository.findMaxDetRowIdByTransactionPoid(savedEntity.getTransactionPoid()) + 1;
            SalesQuoteProjectsNotesDtl entity = new SalesQuoteProjectsNotesDtl();
            SalesQuoteProjectsNotesDtlId id = new SalesQuoteProjectsNotesDtlId(savedEntity.getTransactionPoid(), nextDetRowId);
            entity.setId(id);
            entity.setNotes(request.getNotes());
            entity.setCreatedBy(UserContext.getUserId());
            entity.setCreatedDate(java.time.LocalDate.now());
            entity.setLastModifiedBy(UserContext.getUserId());
            entity.setLastModifiedDate(java.time.LocalDate.now());
            notesDtlRepository.save(entity);
        }
    }

    private void saveTcDetails(SalesQuoteProjectsHdr savedEntity, List<SalesQuoteProjectsTcDetailRequest> tcDetails) {
        for (SalesQuoteProjectsTcDetailRequest request : tcDetails) {
            Long nextDetRowId = tcDtlRepository.findMaxDetRowIdByTransactionPoid(savedEntity.getTransactionPoid()) + 1;
            SalesQuoteProjectsTcDtl entity = new SalesQuoteProjectsTcDtl();
            SalesQuoteProjectsTcDtlId id = new SalesQuoteProjectsTcDtlId(savedEntity.getTransactionPoid(), nextDetRowId);
            entity.setId(id);
            entity.setClauseRef(request.getClauseRef());
            entity.setTermsDescription(request.getTermsDescription());
            entity.setCreatedBy(UserContext.getUserId());
            entity.setCreatedDate(java.time.LocalDate.now());
            entity.setLastModifiedBy(UserContext.getUserId());
            entity.setLastModifiedDate(java.time.LocalDate.now());
            tcDtlRepository.save(entity);
        }
    }
}