package com.asg.operations.salesquotationprojects.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.finaldisbursementaccount.repository.SalesSalesmanMasterRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesQuoteProjectsServiceImpl implements SalesQuoteProjectsService {

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
                "PDA_PORT_TARIFF_HDR",
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
        response.setCommodity(StringUtils.isBlank(entity.getCommodity()) ? null : Arrays.asList(entity.getCommodity().split(", ")));
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

        // Fetch and set child entities
        response.setChargeDetails(chargeDtlRepository.findByIdTransactionPoid(entity.getTransactionPoid()).stream().map(this::mapToObject).toList());
        response.setNotesDetails(notesDtlRepository.findByIdTransactionPoid(entity.getTransactionPoid()).stream().map(this::mapToObject).toList());
        response.setTcDetails(tcDtlRepository.findByIdTransactionPoid(entity.getTransactionPoid()).stream().map(this::mapToObject).toList());

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

    private Object mapToObject(Object entity) {
        return entity;
    }

    private void updateChargeDetails(Long transactionPoid, List<SalesQuoteProjectsChargeDetailRequest> chargeDetails) {
        for (SalesQuoteProjectsChargeDetailRequest request : chargeDetails) {
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

    private void saveChargeDetails(SalesQuoteProjectsHdr savedEntity, List<SalesQuoteProjectsChargeDetailRequest> chargeDetails) {
        for (SalesQuoteProjectsChargeDetailRequest request : chargeDetails) {
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