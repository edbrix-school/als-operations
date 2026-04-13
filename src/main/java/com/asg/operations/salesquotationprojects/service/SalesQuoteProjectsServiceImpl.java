package com.asg.operations.salesquotationprojects.service;

import com.asg.common.lib.dto.*;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.utility.DateUtil;
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
import com.asg.operations.salesquotationprojects.entity.GlobalTermsCustomChanges;
import com.asg.operations.salesquotationprojects.key.SalesQuoteProjectsChargeDtlId;
import com.asg.operations.salesquotationprojects.key.SalesQuoteProjectsNotesDtlId;
import com.asg.operations.salesquotationprojects.key.GlobalTermsCustomChangesId;
import com.asg.operations.shipprincipal.entity.AddressDetails;
import com.asg.operations.shipprincipal.entity.AddressMaster;
import com.asg.operations.shipprincipal.repository.AddressDetailsRepository;
import com.asg.operations.shipprincipal.repository.AddressMasterRepository;
import com.asg.operations.shipprincipal.repository.ShipPrincipalRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SalesQuoteProjectsServiceImpl implements SalesQuoteProjectsService {

    private final JdbcTemplate jdbcTemplate;
    private final SalesQuoteProjectsHdrRepository repository;
    private final SalesQuoteProjectsChargeDtlRepository chargeDtlRepository;
    private final SalesQuoteProjectsNotesDtlRepository notesDtlRepository;
    private final GlobalTermsCustomChangesRepository globalTermsCustomChangesRepository;
    private final DocumentSearchService documentSearchService;
    private final DocumentDeleteService documentDeleteService;
    private final AddressDetailsRepository addressDetailsRepository;
    private final AddressMasterRepository addressMasterRepository;
    private final ShipPrincipalRepository shipPrincipalRepository;
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
    private final LoggingService loggingService;
    private final EntityManager entityManager;

    private static final String LEGACY_DOC_FIELD_NAME_CUSTOMER_POID = "CustomerPoid";


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
        log.info("Fetching sales quote project by id: {}", transactionPoid);
        SalesQuoteProjectsHdr entity = repository.findById(transactionPoid).orElseThrow(() -> new ResourceNotFoundException("Sales Quote Project not found with ID: " + transactionPoid));

        SalesQuoteProjectsResponse response = mapToResponse(entity);

        log.info("getSalesQuoteProjectById completed for transactionPoid={} companyPoid={}", transactionPoid, UserContext.getCompanyPoid());
        return response;
    }

    @Override
    @Transactional
    public SalesQuoteProjectsResponse createSalesQuoteProject(SalesQuoteProjectsRequest request) {
        log.info("Creating sales quote project");

        String customerType = request.getCustomerType().toLowerCase();
        Set<String> allowedTypes = Set.of("existing", "new");
        if (!allowedTypes.contains(customerType)) {
            throw new CustomException("Customer Type should be either Existing or New", 400);
        }

        if ("existing".equalsIgnoreCase(customerType) && request.getCustomerPoid() == null) {
            throw new ValidationException("Customer Poid is required for Existing customer type");
        }

        if (request.getCustomerPoid() != null && StringUtils.isNotBlank(customerType) && !"new".equalsIgnoreCase(customerType)) {
            if (!addressDetailsRepository.existsByAddressPoid(request.getCustomerPoid())) {
                throw new ResourceNotFoundException("Customer", "Customer Poid", request.getCustomerPoid());
            }
        }
        if (request.getPrincipalPoid() != null) {
            if (!shipPrincipalRepository.existsByPrincipalPoid(request.getPrincipalPoid())) {
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
        if (StringUtils.isNotBlank(request.getProjectReferenceNumber())) {
            if (!projectsHdrRepository.existsByDocRefIgnoreCase(request.getProjectReferenceNumber())) {
                throw new ResourceNotFoundException("Project", "Doc Ref", request.getProjectReferenceNumber());
            }
        }
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

        SalesQuoteProjectsHdr savedEntity = repository.saveAndFlush(entity);
        entityManager.refresh(savedEntity);

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

        String key = savedEntity.getTransactionPoid().toString();
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), key, String.format("%s %s", LogDetailsEnum.CREATED, savedEntity.getDocRef()));
        return mapToResponse(savedEntity);
    }

    @Override
    public SalesQuoteProjectsResponse updateSalesQuoteProject(Long transactionPoid, SalesQuoteProjectsRequest request) {
        log.info("Updating sales quote project id: {}", transactionPoid);

        String customerType = request.getCustomerType().toLowerCase();
        Set<String> allowedTypes = Set.of("existing", "new");
        if (!allowedTypes.contains(customerType)) {
            throw new CustomException("Customer Type should be either Existing or New", 400);
        }

        if ("existing".equalsIgnoreCase(customerType) && request.getCustomerPoid() == null) {
            throw new ValidationException("Customer Poid is required for Existing customer type");
        }

        SalesQuoteProjectsHdr existingEntity = repository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Quote Project not found with ID: " + transactionPoid));

        SalesQuoteProjectsHdr oldHeader = new SalesQuoteProjectsHdr();
        BeanUtils.copyProperties(existingEntity, oldHeader);

        if (request.getCustomerPoid() != null && StringUtils.isNotBlank(customerType) && !"new".equalsIgnoreCase(customerType)) {
            if (!addressDetailsRepository.existsByAddressPoid(request.getCustomerPoid())) {
                throw new ResourceNotFoundException("Customer", "Customer Poid", request.getCustomerPoid());
            }
        }
        if (request.getPrincipalPoid() != null) {
            if (!shipPrincipalRepository.existsByPrincipalPoid(request.getPrincipalPoid())) {
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
        if (StringUtils.isNotBlank(request.getProjectReferenceNumber())) {
            if (!projectsHdrRepository.existsByDocRefIgnoreCase(request.getProjectReferenceNumber())) {
                throw new ResourceNotFoundException("Project", "Doc Ref", request.getProjectReferenceNumber());
            }
        }
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

        SalesQuoteProjectsHdr savedEntity = repository.save(existingEntity);

        // Update child details with action types
        if (request.getChargeDetails() != null && !request.getChargeDetails().isEmpty()) {
            updateChargeDetails(existingEntity.getTransactionPoid(), request.getChargeDetails());
        }
        if (request.getNotesDetails() != null && !request.getNotesDetails().isEmpty()) {
            updateNotesDetails(existingEntity.getTransactionPoid(), request.getNotesDetails());
        }
        if (request.getTcDetails() != null && !request.getTcDetails().isEmpty()) {
            saveTcDetails(existingEntity, request.getTcDetails());
        }

        loggingService.logChanges(oldHeader, existingEntity, SalesQuoteProjectsHdr.class, UserContext.getDocumentId(), transactionPoid.toString(), LogDetailsEnum.MODIFIED, "TRANSACTION_POID");
        return mapToResponse(savedEntity);
    }

    @Override
    public void deleteSalesQuoteProject(Long transactionPoid, DeleteReasonDto deleteReasonDto) {
        log.info("Deleting sales quote project id: {}", transactionPoid);
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
        response.setDocRef(entity.getDocRef());
        response.setCustomerType(entity.getCustomerType());
        response.setCustomerPoid(entity.getCustomerPoid());
        response.setCustomerName(entity.getCustomerName());
        response.setCustomerContact(entity.getCustomerContact());
        response.setCustomerEmail(entity.getCustomerEmail());
        response.setCustomerTelephone(entity.getCustomerTelephone());
        response.setCustomerMobile(entity.getCustomerMobile());
        response.setPrincipalPoid(entity.getPrincipalPoid());
        response.setShipmentMode(entity.getShipmentMode());
        response.setTransportationMode(entity.getTransportationMode());
        response.setOtherMode(entity.getOtherMode());
        response.setLinePoid(entity.getLinePoid());
        response.setCarrierPoid(entity.getCarrierPoid());
        response.setQuoteReference(entity.getQuoteReference());
        response.setUnits(entity.getUnits());
        response.setWeight(entity.getWeight());
        response.setCbm(entity.getCbm());
        response.setQuantity(entity.getQuantity());
        response.setFreightTons(entity.getFreightTons());
        response.setAutoRate(entity.getAutoRate());
        response.setBillingCurrencyCode(entity.getBillingCurrencyCode());
        response.setAgreedRate(entity.getAgreedRate());
        response.setSalesmanPoid(entity.getSalesmanPoid());
        response.setShippingTerms(entity.getShippingTerms());
        response.setQuotationStatus(entity.getQuotationStatus());
        response.setProjectDetails(entity.getProjectDetails());
        response.setIsSupplementaryQuote(entity.getIsSupplementaryQuote());
        response.setProjectReferenceNumber(entity.getProjectReferenceNumber());
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
        response.setDeleted(entity.getDeleted());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());

        if (StringUtils.isNotBlank(entity.getCommodity())) {
            List<String> commodities = Arrays.asList(entity.getCommodity().split(","));
            response.setCommodity(commodities);
        }

        // Fetch and set child entities
        response.setChargeDetails(new ArrayList<>(chargeDtlRepository.findByIdTransactionPoid(entity.getTransactionPoid()).stream().map(this::mapChargeDetailToResponse).toList()));
        response.setNotesDetails(new ArrayList<>(notesDtlRepository.findByIdTransactionPoid(entity.getTransactionPoid()).stream().map(this::mapNotesDetailToResponse).toList()));
        String docId = UserContext.getDocumentId();
        Long refTermsPoid = entity.getTermsPoid();
        if (refTermsPoid != null) {
            response.setTcDetails(new ArrayList<>(globalTermsCustomChangesRepository.findByIdDocIdAndIdDocKeyPoidAndIdRefTermsPoid(docId, entity.getTransactionPoid(), refTermsPoid).stream().map(this::mapTcDetailToResponse).toList()));
        } else {
            response.setTcDetails(new ArrayList<>());
        }
        DetRowIdSort.sortAscending(response.getChargeDetails());
        DetRowIdSort.sortAscending(response.getNotesDetails());
        DetRowIdSort.sortAscending(response.getTcDetails());
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
        LocalDate transactionDate = request.getTransactionDate() != null
                ? request.getTransactionDate()
                : DateUtil.getCurrentDateInUserTimeZone();
        entity.setTransactionDate(transactionDate);
    }

    private SalesQuoteProjectsChargeDetailResponse mapChargeDetailToResponse(SalesQuoteProjectsChargeDtl entity) {
        SalesQuoteProjectsChargeDetailResponse response = new SalesQuoteProjectsChargeDetailResponse();
        response.setTransactionPoid(entity.getId().getTransactionPoid());
        response.setDetRowId(entity.getId().getDetRowId());
        response.setChargePoid(entity.getChargePoid());
        response.setPrintableChargeDesc(entity.getPrintableChargeDesc());
        response.setQuantity(entity.getQuantity());
        response.setUnitPoid(entity.getUnitPoid());
        response.setBuyCurrencyCode(entity.getBuyCurrencyCode());
        response.setBuyCurrencyRate(entity.getBuyCurrencyRate());
        response.setBuyUnitRate(entity.getBuyUnitRate());
        response.setBuyTotalLc(entity.getBuyTotalLc());
        response.setSellUnitRateFc(entity.getSellUnitRateFc());
        response.setSellTotalFc(entity.getSellTotalFc());
        response.setTaxPoid(entity.getTaxPoid());
        response.setTaxPercentage(entity.getTaxPercentage());
        response.setTaxAmountFc(entity.getTaxAmountFc());
        response.setSellGrandTotalFc(entity.getSellGrandTotalFc());
        response.setTaxAmountLc(entity.getTaxAmountLc());
        response.setSellGrandTotalLc(entity.getSellGrandTotalLc());
        response.setRemarks(entity.getRemarks());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate() != null ? entity.getCreatedDate() : null);
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate() != null ? entity.getLastModifiedDate() : null);
        return response;
    }

    private SalesQuoteProjectsNotesDetailResponse mapNotesDetailToResponse(SalesQuoteProjectsNotesDtl entity) {
        SalesQuoteProjectsNotesDetailResponse response = new SalesQuoteProjectsNotesDetailResponse();
        response.setTransactionPoid(entity.getId().getTransactionPoid());
        response.setDetRowId(entity.getId().getDetRowId());
        response.setNotes(entity.getNotes());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate() != null ? entity.getCreatedDate() : null);
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate() != null ? entity.getLastModifiedDate() : null);
        return response;
    }

    private SalesQuoteProjectsTcDetailResponse mapTcDetailToResponse(GlobalTermsCustomChanges entity) {
        SalesQuoteProjectsTcDetailResponse response = new SalesQuoteProjectsTcDetailResponse();
        response.setTransactionPoid(entity.getId().getDocKeyPoid());
        response.setDetRowId(entity.getId().getDetRowId());
        response.setClauseRef(entity.getClauseNo());
        response.setTermsDescription(entity.getClauseDetails());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate() != null ? entity.getCreatedDate() : null);
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate() != null ? entity.getLastModifiedDate() : null);
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
                SalesQuoteProjectsChargeDtl saved = chargeDtlRepository.save(entity);
                String logDetail = String.format("Row Created on [Sales Quotation Projects Charge Details] with detRowId: %s", saved.getId().getDetRowId());
                loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
            } else if (action == ActionType.isUpdated) {
                SalesQuoteProjectsChargeDtlId id = new SalesQuoteProjectsChargeDtlId(transactionPoid, request.getDetRowId());
                chargeDtlRepository.findById(id).ifPresent(existing -> {
                    SalesQuoteProjectsChargeDtl oldDetail = new SalesQuoteProjectsChargeDtl();
                    BeanUtils.copyProperties(existing, oldDetail);
                    mapChargeRequestToEntity(request, existing);
                    chargeDtlRepository.save(existing);
                    String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getId().getTransactionPoid(), existing.getId().getDetRowId());
                    loggingService.createLog(oldDetail, existing, SalesQuoteProjectsChargeDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                });
            } else if (action == ActionType.isDeleted) {
                SalesQuoteProjectsChargeDtlId id = new SalesQuoteProjectsChargeDtlId(transactionPoid, request.getDetRowId());
                chargeDtlRepository.deleteById(id);
                loggingService.logDelete(request, UserContext.getDocumentId(), transactionPoid.toString());
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
                SalesQuoteProjectsNotesDtl saved = notesDtlRepository.save(entity);
                String logDetail = String.format("Row Created on [Sales Quotation Projects Notes Details] with detRowId: %s", saved.getId().getDetRowId());
                loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
            } else if (action == ActionType.isUpdated) {
                SalesQuoteProjectsNotesDtlId id = new SalesQuoteProjectsNotesDtlId(transactionPoid, request.getDetRowId());
                notesDtlRepository.findById(id).ifPresent(existing -> {
                    SalesQuoteProjectsNotesDtl oldDetail = new SalesQuoteProjectsNotesDtl();
                    BeanUtils.copyProperties(existing, oldDetail);
                    existing.setNotes(request.getNotes());
                    notesDtlRepository.save(existing);
                    String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getId().getTransactionPoid(), existing.getId().getDetRowId());
                    loggingService.createLog(oldDetail, existing, SalesQuoteProjectsNotesDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                });
            } else if (action == ActionType.isDeleted) {
                SalesQuoteProjectsNotesDtlId id = new SalesQuoteProjectsNotesDtlId(transactionPoid, request.getDetRowId());
                notesDtlRepository.deleteById(id);
                loggingService.logDelete(request, UserContext.getDocumentId(), transactionPoid.toString());
            }
        }
    }

    private void updateTcDetails(SalesQuoteProjectsHdr existingEntity, List<SalesQuoteProjectsTcDetailRequest> tcDetails) {
        String docId = UserContext.getDocumentId();
        Long companyPoid = UserContext.getCompanyPoid();
        Long refTermsPoid = existingEntity.getTermsPoid();

        if (refTermsPoid == null) {
            throw new CustomException("Terms POID is required to update TC details", 400);
        }

        // Check if there are any existing records with different REF_TERMS_POID
        // If termsPoid changed, delete all old records for this DOC_ID and DOC_KEY_POID
        List<GlobalTermsCustomChanges> existingRecords = globalTermsCustomChangesRepository.findByIdDocIdAndIdDocKeyPoid(docId, existingEntity.getTransactionPoid());
        boolean hasDifferentTermsPoid = existingRecords.stream()
                .anyMatch(record -> !refTermsPoid.equals(record.getId().getRefTermsPoid()));

        if (hasDifferentTermsPoid) {
            // Terms POID changed - delete all old records
            globalTermsCustomChangesRepository.deleteAll(existingRecords);
            log.info("Terms POID changed. Deleted {} old Global Terms Custom Changes records for DOC_ID: {}, DOC_KEY_POID: {}, old REF_TERMS_POID(s) replaced with: {}",
                    existingRecords.size(), docId, existingEntity.getTransactionPoid(), refTermsPoid);
        }

        for (SalesQuoteProjectsTcDetailRequest request : tcDetails) {
            ActionType action = request.getActionType();
            if (action == null) continue;

            if (action == ActionType.isCreated) {
                Long nextDetRowId = globalTermsCustomChangesRepository.findMaxDetRowIdByDocIdAndDocKeyPoidAndRefTermsPoid(docId, existingEntity.getTransactionPoid(), refTermsPoid) + 1;
                GlobalTermsCustomChanges entity = new GlobalTermsCustomChanges();
                GlobalTermsCustomChangesId id = new GlobalTermsCustomChangesId(docId, existingEntity.getTransactionPoid(), refTermsPoid, nextDetRowId);
                entity.setId(id);
                entity.setCompanyPoid(companyPoid);
                entity.setClauseNo(request.getClauseRef());
                entity.setClauseDetails(request.getTermsDescription());
                entity.setActive("Y");
                GlobalTermsCustomChanges saved = globalTermsCustomChangesRepository.save(entity);
                String logDetail = String.format("Row Created on [Sales Quotation Projects Terms and Condition Details] with detRowId: %s", saved.getId().getDetRowId());
                loggingService.createLogSummaryEntry(UserContext.getDocumentId(), existingEntity.getTransactionPoid().toString(), logDetail);
            } else if (action == ActionType.isUpdated) {
                GlobalTermsCustomChangesId id = new GlobalTermsCustomChangesId(docId, existingEntity.getTransactionPoid(), refTermsPoid, request.getDetRowId());
                globalTermsCustomChangesRepository.findById(id).ifPresent(existing -> {
                    GlobalTermsCustomChanges oldDetail = new GlobalTermsCustomChanges();
                    BeanUtils.copyProperties(existing, oldDetail);
                    existing.setClauseNo(request.getClauseRef());
                    existing.setClauseDetails(request.getTermsDescription());
                    globalTermsCustomChangesRepository.save(existing);
                    String logDetail = String.format("KeyId = DOC_ID %s: DOC_KEY_POID %s: REF_TERMS_POID %s: DET_ROW_ID %s", existing.getId().getDocId(), existing.getId().getDocKeyPoid(), existing.getId().getRefTermsPoid(), existing.getId().getDetRowId());
                    loggingService.createLog(oldDetail, existing, GlobalTermsCustomChanges.class, UserContext.getDocumentId(), existingEntity.getTransactionPoid().toString(), logDetail);
                });
            } else if (action == ActionType.isDeleted) {
                GlobalTermsCustomChangesId id = new GlobalTermsCustomChangesId(docId, existingEntity.getTransactionPoid(), refTermsPoid, request.getDetRowId());
                globalTermsCustomChangesRepository.deleteById(id);
                loggingService.logDelete(request, UserContext.getDocumentId(), existingEntity.getTransactionPoid().toString());
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

    public Map<String, Object> getTermsAndConditions(Long termsPoid, Long docKeyPoid) {
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
            params.put("P_DOC_KEY_POID", docKeyPoid);
            params.put("P_TERMS_POID", termsPoid);
            return jdbcCall.execute(params);
        } catch (Exception e) {
            throw new CustomException("Error retrieving terms and conditions: " + e.getMessage(), 500);
        }
    }

    public Map<String, Object> getChargeTaxDetails(LocalDateTime transactionDate, Long companyPoid, Long partyPoid, Long chargePoid) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_GET_CHARGE_TAX_PER_V3")
                    .declareParameters(
                            new SqlParameter("P_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_TRANSACTION_DATE", Types.DATE),
                            new SqlParameter("P_PARTY_TYPE", Types.VARCHAR),
                            new SqlParameter("P_PARTY_POID", Types.NUMERIC),
                            new SqlParameter("P_CHARGE_POID", Types.NUMERIC),
                            new SqlOutParameter("OUTDATA", OracleTypes.CURSOR)
                    );
            Map<String, Object> params = new HashMap<>();
            params.put("P_COMPANY_POID", companyPoid);
            params.put("P_TRANSACTION_DATE", transactionDate);
            params.put("P_PARTY_TYPE", "CUSTOMER");
            params.put("P_PARTY_POID", partyPoid);
            params.put("P_CHARGE_POID", chargePoid);
            return jdbcCall.execute(params);
        } catch (Exception e) {
            throw new CustomException("Error retrieving charge tax details: " + e.getMessage(), 500);
        }
    }

    @Override
    public AddressDetailsDto getCustomerDetailsById(BigDecimal addressPoid) {

        AddressDetails addressToUse = addressDetailsRepository.findByAddressPoidAndAddressType(addressPoid, "SALES");

        if (addressToUse == null) {
            addressToUse = addressDetailsRepository.findByAddressPoidAndAddressType(addressPoid, "MAIN");
            if (addressToUse == null) {
                throw new ResourceNotFoundException("Address Details", "addressPoid", addressPoid);
            }
        }

        AddressMaster addressMaster = addressMasterRepository.findByAddressMasterPoid(addressToUse.getAddressMasterPoid());

        if (addressMaster == null) {
            throw new ResourceNotFoundException("Address Master", "addressMasterPoid", addressToUse.getAddressMasterPoid());
        }

        return AddressDetailsDto.builder()
                .addressPoid(addressToUse.getAddressPoid())
                .addressName(addressMaster.getAddressName())
                .contactPerson(addressToUse.getContactPerson())
                .email(addressToUse.getEmail1())
                .telephone(addressToUse.getOffTel1())
                .mobile(addressToUse.getMobile())
                .poBox(addressToUse.getPoBox())
                .whatsAppNumber(addressToUse.getWhatsappNo())
                .build();
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
            chargeDtlRepository.save(entity);
            String logDetail = String.format("Row Created on [Sales Quotation Projects Charge Details] with detRowId: %s", entity.getId().getDetRowId());
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), savedEntity.getTransactionPoid().toString(), logDetail);
        }
    }

    private void saveNotesDetails(SalesQuoteProjectsHdr savedEntity, List<SalesQuoteProjectsNotesDetailRequest> notesDetails) {
        for (SalesQuoteProjectsNotesDetailRequest request : notesDetails) {
            Long nextDetRowId = notesDtlRepository.findMaxDetRowIdByTransactionPoid(savedEntity.getTransactionPoid()) + 1;
            SalesQuoteProjectsNotesDtl entity = new SalesQuoteProjectsNotesDtl();
            SalesQuoteProjectsNotesDtlId id = new SalesQuoteProjectsNotesDtlId(savedEntity.getTransactionPoid(), nextDetRowId);
            entity.setId(id);
            entity.setNotes(request.getNotes());
            notesDtlRepository.save(entity);
            String logDetail = String.format("Row Created on [Sales Quotation Projects Notes Details] with detRowId: %s", entity.getId().getDetRowId());
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), savedEntity.getTransactionPoid().toString(), logDetail);
        }
    }

    private void saveTcDetails(SalesQuoteProjectsHdr savedEntity, List<SalesQuoteProjectsTcDetailRequest> tcDetails) {
        String docId = UserContext.getDocumentId();
        Long companyPoid = UserContext.getCompanyPoid();
        Long refTermsPoid = savedEntity.getTermsPoid();

        if (refTermsPoid == null) {
            throw new CustomException("Terms POID is required to save TC details", 400);
        }

        // Delete any existing records for this DOC_ID and DOC_KEY_POID before creating new ones
        // This handles the case where termsPoid changes - old records are removed
        List<GlobalTermsCustomChanges> existingRecords = globalTermsCustomChangesRepository.findByIdDocIdAndIdDocKeyPoid(docId, savedEntity.getTransactionPoid());
        if (!existingRecords.isEmpty()) {
            globalTermsCustomChangesRepository.deleteAll(existingRecords);
            log.info("Deleted {} old Global Terms Custom Changes records for DOC_ID: {}, DOC_KEY_POID: {}",
                    existingRecords.size(), docId, savedEntity.getTransactionPoid());
        }

        for (SalesQuoteProjectsTcDetailRequest request : tcDetails) {
            Long nextDetRowId = globalTermsCustomChangesRepository.findMaxDetRowIdByDocIdAndDocKeyPoidAndRefTermsPoid(docId, savedEntity.getTransactionPoid(), refTermsPoid) + 1;
            GlobalTermsCustomChanges entity = new GlobalTermsCustomChanges();
            GlobalTermsCustomChangesId id = new GlobalTermsCustomChangesId(docId, savedEntity.getTransactionPoid(), refTermsPoid, nextDetRowId);
            entity.setId(id);
            entity.setCompanyPoid(companyPoid);
            entity.setClauseNo(request.getClauseRef());
            entity.setClauseDetails(request.getTermsDescription());
            entity.setActive("Y");
            globalTermsCustomChangesRepository.save(entity);
            String logDetail = String.format("Row Created on [Sales Quotation Projects Terms and Condition Details] with detRowId: %s", entity.getId().getDetRowId());
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), savedEntity.getTransactionPoid().toString(), logDetail);
        }
    }
}