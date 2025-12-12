package com.asg.operations.finaldisbursementaccount.service;

import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.common.PageResponse;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.exceptions.CustomException;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.finaldisbursementaccount.dto.*;
import com.asg.operations.finaldisbursementaccount.entity.PdaFdaDtl;
import com.asg.operations.finaldisbursementaccount.entity.PdaFdaHdr;
import com.asg.operations.finaldisbursementaccount.key.PdaFdaDtlId;
import com.asg.operations.finaldisbursementaccount.repository.FdaCustomRepository;
import com.asg.operations.finaldisbursementaccount.repository.PdaFdaDtlRepository;
import com.asg.operations.finaldisbursementaccount.repository.PdaFdaHdrRepository;
import com.asg.operations.finaldisbursementaccount.util.CalculationUtils;
import com.asg.operations.finaldisbursementaccount.util.ChargesMapper;
import com.asg.operations.finaldisbursementaccount.util.HeaderMapper;
import com.asg.operations.finaldisbursementaccount.util.ValidationUtils;
import com.asg.operations.pdaentryform.entity.PdaEntryHdr;
import com.asg.operations.pdaentryform.repository.PdaEntryHdrRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
//import net.sf.jasperreports.engine.*;
//import net.sf.jasperreports.engine.util.JRLoader;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.core.io.Resource;
//import org.springframework.jdbc.core.JdbcTemplate;
//import java.io.InputStream;
//import java.sql.Connection;
//import java.sql.SQLException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FdaServiceImpl implements FdaService {

    private final PdaFdaHdrRepository pdaFdaHdrRepository;
    private final PdaFdaDtlRepository pdaFdaDtlRepository;
    private final FdaCustomRepository fdaCustomRepository;
    private final PdaEntryHdrRepository pdaEntryHdrRepository;
    private final ValidationUtils validationUtils;
    private final LovService lovService;
    private final EntityManager entityManager;
//    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public FdaHeaderDto getFdaHeader(Long transactionPoid, Long groupPoid, Long companyPoid) {

        PdaFdaHdr entity = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        FdaHeaderDto fdaHeaderDto = HeaderMapper.mapHeaderEntityToDto(entity);
        setDetailsForHeader(fdaHeaderDto);

        List<PdaFdaDtl> dtls = pdaFdaDtlRepository.findByIdTransactionPoid(transactionPoid);

        List<FdaChargeDto> charges = dtls.stream()
                .map(ChargesMapper::mapChargeEntityToDto)
                .collect(Collectors.toList());

        for (FdaChargeDto charge : charges) {
            setDetailsForCharge(charge);
        }

        CalculationUtils.computeProfitLossRuntime(charges, fdaHeaderDto);

        fdaHeaderDto.setCharges(charges);

        return fdaHeaderDto;
    }

    @Override
    @Transactional
    public FdaHeaderDto createFdaHeader(FdaHeaderDto dto, Long groupPoid, Long companyPoid, String userId) {

        validationUtils.validateHeaderBeforeSave(dto);

        if (dto.getArrivalDate() != null && dto.getVesselSailDate() != null &&
                dto.getVesselSailDate().isBefore(dto.getArrivalDate())) {
            throw new CustomException("Vessel sail date cannot be before arrival date", 422);
        }

        PdaFdaHdr entity = new PdaFdaHdr();
        HeaderMapper.mapHeaderDtoToEntity(dto, entity, userId);

        entity.setCreatedBy(userId);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setDocRef(validationUtils.generateDocRef((groupPoid)));
        entity = pdaFdaHdrRepository.save(entity);

        if (dto.getCharges() != null && !dto.getCharges().isEmpty()) {
            saveCharges(entity.getTransactionPoid(), dto.getCharges(), userId, groupPoid, companyPoid);
        }

        return getFdaHeader(entity.getTransactionPoid(), groupPoid, companyPoid);
    }

    @Override
    @Transactional
    public FdaHeaderDto updateFdaHeader(Long transactionPoid, UpdateFdaHeaderRequest dto, Long groupPoid, Long companyPoid, String userId) {

        validationUtils.validateHeaderBeforeUpdate(dto);

        PdaFdaHdr entity = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        HeaderMapper.mapUpdateHeaderDtoToEntity(dto, entity, userId);
        pdaFdaHdrRepository.save(entity);

        if (dto.getCharges() != null && !dto.getCharges().isEmpty()) {
            saveCharges(transactionPoid, dto.getCharges(), userId, groupPoid, companyPoid);
        }

        return getFdaHeader(transactionPoid, groupPoid, companyPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FdaHeaderDto> getFdaList(Long groupPoid, Long companyPoid, Long transactionPoid, String vesselName, LocalDate etaFrom, LocalDate etaTo, Pageable pageable) {

//        can be used in future
//        Page<PdaFdaHdr> page = pdaFdaHdrRepository.searchFdaHeaders(groupPoid, companyPoid, transactionPoid, vesselName, etaFrom, etaTo, pageable);

        Page<PdaFdaHdr> page = pdaFdaHdrRepository.searchFdaHeaders(groupPoid, companyPoid, transactionPoid, etaFrom, etaTo, pageable);

        List<FdaHeaderDto> content = page.getContent().stream()
                .map(HeaderMapper::mapHeaderEntityToDto)
                .collect(Collectors.toList());

        for (FdaHeaderDto fdaHeaderDto : content) {
            setDetailsForHeader(fdaHeaderDto);
        }

        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast(), page.getNumberOfElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FdaListResponse> getAllFdaWithFilters(Long groupPoid, Long companyPoid, GetAllFdaFilterRequest filterRequest, int page, int size, String sort) {

        // Build dynamic SQL query
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT f.TRANSACTION_POID, f.TRANSACTION_DATE, f.GROUP_POID, f.COMPANY_POID, ");
        sqlBuilder.append("f.PRINCIPAL_POID, f.PRINCIPAL_CONTACT, f.DOC_REF, f.VOYAGE_POID, f.VESSEL_POID, ");
        sqlBuilder.append("f.ARRIVAL_DATE, f.SAIL_DATE, f.PORT_POID, f.COMODITY_POID, f.OPERATION_TYPE, ");
        sqlBuilder.append("f.IMPORT_QTY, f.EXPORT_QTY, f.TOTAL_QUANTITY, f.UNIT, f.HARBOUR_CALL_TYPE, ");
        sqlBuilder.append("f.CURRENCY_CODE, f.CURRENCY_RATE, f.COST_CENTRE_POID, f.VESSEL_VERIFIED, ");
        sqlBuilder.append("f.VESSEL_VERIFIED_DATE, f.VESSEL_VERIFIED_BY, f.URGENT_APPROVAL, ");
        sqlBuilder.append("f.PRINCIPAL_APRVL_DAYS, f.PRINCIPAL_APPROVED, f.PRINCIPAL_APPROVED_DATE, ");
        sqlBuilder.append("f.PRINCIPAL_APPROVED_BY, f.REMINDER_MINUTES, f.CARGO_DETAILS, f.STATUS, ");
        sqlBuilder.append("f.FDA_CLOSED_DATE, f.REMARKS, f.TOTAL_AMOUNT, f.CREATED_BY, f.CREATED_DATE, ");
        sqlBuilder.append("f.LASTMODIFIED_BY, f.LASTMODIFIED_DATE, f.DELETED, f.PDA_REF, f.ADDRESS_POID, ");
        sqlBuilder.append("f.SALESMAN_POID, f.TRANSHIPMENT_QTY, f.DWT, f.GRT, f.IMO_NUMBER, f.NRT, ");
        sqlBuilder.append("f.NUMBER_OF_DAYS, f.PORT_DESCRIPTION, f.TERMS_POID, f.VESSEL_TYPE_POID, ");
        sqlBuilder.append("f.LINE_POID, f.PRINT_PRINCIPAL, f.VOYAGE_NO, f.PROFIT_LOSS_AMOUNT, ");
        sqlBuilder.append("f.PROFIT_LOSS_PER, f.FDA_CLOSING_BY, f.GL_CLOSING_DATE, f.REF_TYPE, ");
        sqlBuilder.append("f.CLOSED_REMARK, f.SUPPLEMENTARY, f.SUPPLEMENTARY_FDA_POID, f.BUSINESS_REF_BY, ");
        sqlBuilder.append("f.FDA_WITHOUT_CHARGES, f.PRINT_BANK_POID, f.PORT_CALL_NUMBER, ");
        sqlBuilder.append("f.NOMINATED_PARTY_TYPE, f.NOMINATED_PARTY_POID, f.DOCUMENT_SUBMITTED_DATE, ");
        sqlBuilder.append("f.DOCUMENT_SUBMITTED_BY, f.DOCUMENT_SUBMITTED_STATUS, f.FDA_SUB_TYPE, ");
        sqlBuilder.append("f.SUB_CATEGORY, f.DOCUMENT_RECEIVED_DATE, f.DOCUMENT_RECEIVED_FROM, ");
        sqlBuilder.append("f.DOCUMENT_RECEIVED_STATUS, f.SUBMISSION_ACCEPTED_DATE, ");
        sqlBuilder.append("f.VERIFICATION_ACCEPTED_DATE, f.SUBMISSION_ACCEPTED_BY, ");
        sqlBuilder.append("f.VERIFICATION_ACCEPTED_BY, f.VESSEL_HANDLED_BY, f.VESSEL_SAIL_DATE, ");
        sqlBuilder.append("f.ACCOUNTS_VERIFIED, f.OPS_CORRECTION_REMARKS, f.OPS_RETURNED_DATE ");
        sqlBuilder.append("FROM PDA_FDA_HDR f ");
        sqlBuilder.append("WHERE f.GROUP_POID = :groupPoid AND f.COMPANY_POID = :companyPoid ");

        // Apply isDeleted filter
        if (filterRequest.getIsDeleted() != null && "N".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND (f.DELETED IS NULL OR f.DELETED != 'Y') ");
        } else if (filterRequest.getIsDeleted() != null && "Y".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND f.DELETED = 'Y' ");
        }

        // Apply date range filters
        if (org.springframework.util.StringUtils.hasText(filterRequest.getFrom())) {
            sqlBuilder.append("AND TRUNC(f.TRANSACTION_DATE) >= TO_DATE(:fromDate, 'YYYY-MM-DD') ");
        }
        if (org.springframework.util.StringUtils.hasText(filterRequest.getTo())) {
            sqlBuilder.append("AND TRUNC(f.TRANSACTION_DATE) <= TO_DATE(:toDate, 'YYYY-MM-DD') ");
        }

        // Build filter conditions with sequential parameter indexing
        List<String> filterConditions = new java.util.ArrayList<>();
        List<GetAllFdaFilterRequest.FilterItem> validFilters = new java.util.ArrayList<>();
        if (filterRequest.getFilters() != null && !filterRequest.getFilters().isEmpty()) {
            for (GetAllFdaFilterRequest.FilterItem filter : filterRequest.getFilters()) {
                if (org.springframework.util.StringUtils.hasText(filter.getSearchField()) && org.springframework.util.StringUtils.hasText(filter.getSearchValue())) {
                    validFilters.add(filter);
                    String columnName = mapFdaSearchFieldToColumn(filter.getSearchField());
                    int paramIndex = validFilters.size() - 1;
                    filterConditions.add("LOWER(" + columnName + ") LIKE LOWER(:filterValue" + paramIndex + ")");
                }
            }
        }

        // Add filter conditions with operator
        if (!filterConditions.isEmpty()) {
            String operator = "AND".equalsIgnoreCase(filterRequest.getOperator()) ? " AND " : " OR ";
            sqlBuilder.append("AND (").append(String.join(operator, filterConditions)).append(") ");
        }

        // Apply sorting
        String orderBy = "ORDER BY f.TRANSACTION_DATE DESC";
        if (org.springframework.util.StringUtils.hasText(sort)) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String sortField = mapSortFieldToColumn(sortParts[0].trim());
                String sortDirection = sortParts[1].trim().toUpperCase();
                if ("ASC".equals(sortDirection) || "DESC".equals(sortDirection)) {
                    orderBy = "ORDER BY " + sortField + " " + sortDirection + " NULLS LAST";
                }
            }
        }
        sqlBuilder.append(orderBy);

        // Create count query
        String countSql = "SELECT COUNT(*) FROM (" + sqlBuilder.toString() + ")";

        // Create query
        Query query = entityManager.createNativeQuery(sqlBuilder.toString());
        Query countQuery = entityManager.createNativeQuery(countSql);

        // Set parameters
        query.setParameter("groupPoid", groupPoid);
        query.setParameter("companyPoid", companyPoid);
        countQuery.setParameter("groupPoid", groupPoid);
        countQuery.setParameter("companyPoid", companyPoid);

        if (org.springframework.util.StringUtils.hasText(filterRequest.getFrom())) {
            query.setParameter("fromDate", filterRequest.getFrom());
            countQuery.setParameter("fromDate", filterRequest.getFrom());
        }
        if (org.springframework.util.StringUtils.hasText(filterRequest.getTo())) {
            query.setParameter("toDate", filterRequest.getTo());
            countQuery.setParameter("toDate", filterRequest.getTo());
        }

        // Set filter parameters using sequential indexing
        if (!validFilters.isEmpty()) {
            for (int i = 0; i < validFilters.size(); i++) {
                GetAllFdaFilterRequest.FilterItem filter = validFilters.get(i);
                String paramValue = "%" + filter.getSearchValue() + "%";
                query.setParameter("filterValue" + i, paramValue);
                countQuery.setParameter("filterValue" + i, paramValue);
            }
        }

        // Get total count
        Long totalCount = ((Number) countQuery.getSingleResult()).longValue();

        // Apply pagination
        int offset = page * size;
        query.setFirstResult(offset);
        query.setMaxResults(size);

        // Execute query and map results
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<FdaListResponse> dtos = results.stream()
                .map(this::mapToFdaListResponseDto)
                .collect(Collectors.toList());

        // Create page
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(dtos, pageable, totalCount);
    }

    private String mapFdaSearchFieldToColumn(String searchField) {
        if (searchField == null) {
            return null;
        }
        // Normalize the field name by removing underscores and converting to uppercase
        String normalizedField = searchField.toUpperCase().replace("_", "");

        switch (normalizedField) {
            case "DOCREF":
                return "f.DOC_REF";
            case "STATUS":
                return "f.STATUS";
            case "REMARKS":
                return "f.REMARKS";
            case "PRINCIPALCONTACT":
                return "f.PRINCIPAL_CONTACT";
            case "OPERATIONTYPE":
                return "f.OPERATION_TYPE";
            case "UNIT":
                return "f.UNIT";
            case "HARBOURCALLTYPE":
                return "f.HARBOUR_CALL_TYPE";
            case "CURRENCYCODE":
                return "f.CURRENCY_CODE";
            case "VESSELVERIFIED":
                return "f.VESSEL_VERIFIED";
            case "VESSELVERIFIEDBY":
                return "f.VESSEL_VERIFIED_BY";
            case "URGENTAPPROVAL":
                return "f.URGENT_APPROVAL";
            case "PRINCIPALAPPROVED":
                return "f.PRINCIPAL_APPROVED";
            case "PRINCIPALAPPROVEDBY":
                return "f.PRINCIPAL_APPROVED_BY";
            case "CARGODETAILS":
                return "f.CARGO_DETAILS";
            case "CREATEDBY":
                return "f.CREATED_BY";
            case "LASTMODIFIEDBY":
                return "f.LASTMODIFIED_BY";
            case "DELETED":
                return "f.DELETED";
            case "PDAREF":
                return "f.PDA_REF";
            case "IMONUMBER":
                return "f.IMO_NUMBER";
            case "PORTDESCRIPTION":
                return "f.PORT_DESCRIPTION";
            case "VESSELTYPEPOID":
                return "f.VESSEL_TYPE_POID";
            case "VOYAGENO":
                return "f.VOYAGE_NO";
            case "PROFITLOSSPER":
                return "f.PROFIT_LOSS_PER";
            case "FDACLOSINGBY":
                return "f.FDA_CLOSING_BY";
            case "REFTYPE":
                return "f.REF_TYPE";
            case "CLOSEDREMARK":
                return "f.CLOSED_REMARK";
            case "SUPPLEMENTARY":
                return "f.SUPPLEMENTARY";
            case "BUSINESSREFBY":
                return "f.BUSINESS_REF_BY";
            case "FDAWITHOUTCHARGES":
                return "f.FDA_WITHOUT_CHARGES";
            case "PORTCALLNUMBER":
                return "f.PORT_CALL_NUMBER";
            case "NOMINATEDPARTYTYPE":
                return "f.NOMINATED_PARTY_TYPE";
            case "DOCUMENTSUBMITTEDBY":
                return "f.DOCUMENT_SUBMITTED_BY";
            case "DOCUMENTSUBMITTEDSTATUS":
                return "f.DOCUMENT_SUBMITTED_STATUS";
            case "FDASUBTYPE":
                return "f.FDA_SUB_TYPE";
            case "SUBCATEGORY":
                return "f.SUB_CATEGORY";
            case "DOCUMENTRECEIVEDFROM":
                return "f.DOCUMENT_RECEIVED_FROM";
            case "DOCUMENTRECEIVEDSTATUS":
                return "f.DOCUMENT_RECEIVED_STATUS";
            case "SUBMISSIONACCEPTEDBY":
                return "f.SUBMISSION_ACCEPTED_BY";
            case "VERIFICATIONACCEPTEDBY":
                return "f.VERIFICATION_ACCEPTED_BY";
            case "ACCOUNTSVERIFIED":
                return "f.ACCOUNTS_VERIFIED";
            case "OPSCORRECTIONREMARKS":
                return "f.OPS_CORRECTION_REMARKS";
            default:
                // Fallback: assume it's a direct column name from f table
                String columnName = searchField.toUpperCase().replace(" ", "_");
                return "f." + columnName;
        }
    }

    private FdaListResponse mapToFdaListResponseDto(Object[] row) {
        FdaListResponse dto = new FdaListResponse();

        dto.setTransactionPoid(row[0] != null ? ((Number) row[0]).longValue() : null);
        dto.setTransactionDate(row[1] != null ? ((Timestamp) row[1]).toLocalDateTime().toLocalDate() : null);
        dto.setGroupPoid(row[2] != null ? ((Number) row[2]).longValue() : null);
        dto.setCompanyPoid(row[3] != null ? ((Number) row[3]).longValue() : null);
        dto.setPrincipalPoid(row[4] != null ? ((Number) row[4]).longValue() : null);
        dto.setPrincipalContact(convertToString(row[5]));
        dto.setDocRef(convertToString(row[6]));
        dto.setVoyagePoid(row[7] != null ? ((Number) row[7]).longValue() : null);
        dto.setVesselPoid(row[8] != null ? ((Number) row[8]).longValue() : null);
        dto.setArrivalDate(row[9] != null ? ((Timestamp) row[9]).toLocalDateTime().toLocalDate() : null);
        dto.setSailDate(row[10] != null ? ((Timestamp) row[10]).toLocalDateTime().toLocalDate() : null);
        dto.setPortPoid(row[11] != null ? ((Number) row[11]).longValue() : null);
        dto.setCommodityPoid(convertToString(row[12]));
        dto.setOperationType(convertToString(row[13]));
        dto.setTotalQuantity(row[16] != null ? (BigDecimal) row[16] : null);
        dto.setUnit(convertToString(row[17]));
        dto.setHarbourCallType(convertToString(row[18]));
        dto.setCurrencyCode(convertToString(row[19]));
        dto.setStatus(convertToString(row[32]));
        dto.setTotalAmount(row[35] != null ? (BigDecimal) row[35] : null);
        dto.setPdaRef(convertToString(row[41]));
        dto.setSalesmanPoid(row[43] != null ? ((Number) row[43]).longValue() : null);
        dto.setVoyageNo(convertToString(row[55]));
        dto.setRefType(convertToString(row[60]));
        dto.setDeleted(convertToString(row[40]));
        dto.setCreatedBy(convertToString(row[36]));
        dto.setCreatedDate(row[37] != null ? ((Timestamp) row[37]).toLocalDateTime() : null);
        dto.setLastModifiedBy(convertToString(row[38]));
        dto.setLastModifiedDate(row[39] != null ? ((Timestamp) row[39]).toLocalDateTime() : null);

        return dto;
    }

    private FdaHeaderDto mapToFdaResponseDto(Object[] row) {
        FdaHeaderDto dto = new FdaHeaderDto();

        dto.setTransactionPoid(row[0] != null ? ((Number) row[0]).longValue() : null);
        dto.setTransactionDate(row[1] != null ? ((Timestamp) row[1]).toLocalDateTime().toLocalDate() : null);
        dto.setGroupPoid(row[2] != null ? ((Number) row[2]).longValue() : null);
        dto.setCompanyPoid(row[3] != null ? ((Number) row[3]).longValue() : null);
        dto.setPrincipalPoid(row[4] != null ? ((Number) row[4]).longValue() : null);
        dto.setPrincipalContact(convertToString(row[5]));
        dto.setDocRef(convertToString(row[6]));
        dto.setVoyagePoid(row[7] != null ? ((Number) row[7]).longValue() : null);
        dto.setVesselPoid(row[8] != null ? ((Number) row[8]).longValue() : null);
        dto.setArrivalDate(row[9] != null ? ((Timestamp) row[9]).toLocalDateTime().toLocalDate() : null);
        dto.setSailDate(row[10] != null ? ((Timestamp) row[10]).toLocalDateTime().toLocalDate() : null);
        dto.setPortPoid(row[11] != null ? ((Number) row[11]).longValue() : null);
        dto.setCommodityPoid(convertToString(row[12]));
        dto.setOperationType(convertToString(row[13]));
        dto.setImportQty(row[14] != null ? (BigDecimal) row[14] : null);
        dto.setExportQty(row[15] != null ? (BigDecimal) row[15] : null);
        dto.setTotalQuantity(row[16] != null ? (BigDecimal) row[16] : null);
        dto.setUnit(convertToString(row[17]));
        dto.setHarbourCallType(convertToString(row[18]));
        dto.setCurrencyCode(convertToString(row[19]));
        dto.setCurrencyRate(row[20] != null ? (BigDecimal) row[20] : null);
        dto.setCostCentrePoid(row[21] != null ? ((Number) row[21]).longValue() : null);
        dto.setVesselVerified(convertToString(row[22]));
        dto.setVesselVerifiedDate(row[23] != null ? ((Timestamp) row[23]).toLocalDateTime().toLocalDate() : null);
        dto.setVesselVerifiedBy(convertToString(row[24]));
        dto.setUrgentApproval(convertToString(row[25]));
        dto.setPrincipalAprvlDays(row[26] != null ? ((Number) row[26]).longValue() : null);
        dto.setPrincipalApproved(convertToString(row[27]));
        dto.setPrincipalApprovedDate(row[28] != null ? ((Timestamp) row[28]).toLocalDateTime().toLocalDate() : null);
        dto.setPrincipalApprovedBy(convertToString(row[29]));
        dto.setReminderMinutes(row[30] != null ? ((Number) row[30]).longValue() : null);
        dto.setCargoDetails(convertToString(row[31]));
        dto.setStatus(convertToString(row[32]));
        dto.setFdaClosedDate(row[33] != null ? ((Timestamp) row[33]).toLocalDateTime().toLocalDate() : null);
        dto.setRemarks(convertToString(row[34]));
        dto.setTotalAmount(row[35] != null ? (BigDecimal) row[35] : null);
        dto.setCreatedBy(convertToString(row[36]));
        dto.setCreatedDate(row[37] != null ? ((Timestamp) row[37]).toLocalDateTime() : null);
        dto.setLastModifiedBy(convertToString(row[38]));
        dto.setLastModifiedDate(row[39] != null ? ((Timestamp) row[39]).toLocalDateTime() : null);
        dto.setDeleted(convertToString(row[40]));
        dto.setPdaRef(convertToString(row[41]));
        dto.setAddressPoid(row[42] != null ? ((Number) row[42]).longValue() : null);
        dto.setSalesmanPoid(row[43] != null ? ((Number) row[43]).longValue() : null);
        dto.setTranshipmentQty(row[44] != null ? (BigDecimal) row[44] : null);
        dto.setDwt(row[45] != null ? (BigDecimal) row[45] : null);
        dto.setGrt(row[46] != null ? (BigDecimal) row[46] : null);
        dto.setImoNumber(convertToString(row[47]));
        dto.setNrt(row[48] != null ? (BigDecimal) row[48] : null);
        dto.setNumberOfDays(row[49] != null ? (BigDecimal) row[49] : null);
        dto.setPortDescription(convertToString(row[50]));
        dto.setTermsPoid(row[51] != null ? ((Number) row[51]).longValue() : null);
        dto.setVesselTypePoid(convertToString(row[52]));
        dto.setLinePoid(row[53] != null ? ((Number) row[53]).longValue() : null);
        dto.setPrintPrincipal(row[54] != null ? ((Number) row[54]).longValue() : null);
        dto.setVoyageNo(convertToString(row[55]));
        dto.setProfitLossAmount(row[56] != null ? (BigDecimal) row[56] : null);
        dto.setProfitLossPer(convertToString(row[57]));
        dto.setFdaClosingBy(convertToString(row[58]));
        dto.setGlClosingDate(row[59] != null ? ((Timestamp) row[59]).toLocalDateTime().toLocalDate() : null);
        dto.setRefType(convertToString(row[60]));
        dto.setClosedRemark(convertToString(row[61]));
        dto.setSupplementary(convertToString(row[62]));
        dto.setSupplementaryFdaPoid(row[63] != null ? ((Number) row[63]).longValue() : null);
        dto.setBusinessRefBy(convertToString(row[64]));
        dto.setFdaWithoutCharges(convertToString(row[65]));
        dto.setPrintBankPoid(row[66] != null ? ((Number) row[66]).longValue() : null);
        dto.setPortCallNumber(convertToString(row[67]));
        dto.setNominatedPartyType(convertToString(row[68]));
        dto.setNominatedPartyPoid(row[69] != null ? ((Number) row[69]).longValue() : null);
        dto.setDocumentSubmittedDate(row[70] != null ? ((Timestamp) row[70]).toLocalDateTime().toLocalDate() : null);
        dto.setDocumentSubmittedBy(convertToString(row[71]));
        dto.setDocumentSubmittedStatus(convertToString(row[72]));
        dto.setFdaSubType(convertToString(row[73]));
        dto.setSubCategory(convertToString(row[74]));
        dto.setDocumentReceivedDate(row[75] != null ? ((Timestamp) row[75]).toLocalDateTime().toLocalDate() : null);
        dto.setDocumentReceivedFrom(convertToString(row[76]));
        dto.setDocumentReceivedStatus(convertToString(row[77]));
        dto.setSubmissionAcceptedDate(row[78] != null ? ((Timestamp) row[78]).toLocalDateTime().toLocalDate() : null);
        dto.setVerificationAcceptedDate(row[79] != null ? ((Timestamp) row[79]).toLocalDateTime().toLocalDate() : null);
        dto.setSubmissionAcceptedBy(convertToString(row[80]));
        dto.setVerificationAcceptedBy(convertToString(row[81]));
        dto.setVesselHandledBy(row[82] != null ? ((Number) row[82]).longValue() : null);
        dto.setVesselSailDate(row[83] != null ? ((Timestamp) row[83]).toLocalDateTime().toLocalDate() : null);
        dto.setAccountsVerified(convertToString(row[84]));
        dto.setOpsCorrectionRemarks(convertToString(row[85]));
        dto.setOpsReturnedDate(row[86] != null ? ((Timestamp) row[86]).toLocalDateTime().toLocalDate() : null);

        return dto;
    }

    private String convertToString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Character) {
            return String.valueOf((Character) value);
        }
        return value.toString();
    }

    private String mapSortFieldToColumn(String sortField) {
        if (sortField == null) {
            return "f.TRANSACTION_DATE";
        }
        String normalizedField = sortField.toUpperCase().replace("_", "");

        switch (normalizedField) {
            case "TRANSACTIONPOID":
                return "f.TRANSACTION_POID";
            case "TRANSACTIONDATE":
                return "f.TRANSACTION_DATE";
            case "DOCREF":
                return "f.DOC_REF";
            case "STATUS":
                return "f.STATUS";
            case "REMARKS":
                return "f.REMARKS";
            case "TOTALAMOUNT":
                return "f.TOTAL_AMOUNT";
            case "CREATEDDATE":
                return "f.CREATED_DATE";
            case "LASTMODIFIEDDATE":
                return "f.LASTMODIFIED_DATE";
            case "FDACLOSEDDATE":
                return "f.FDA_CLOSED_DATE";
            case "ARRIVALDATE":
                return "f.ARRIVAL_DATE";
            case "SAILDATE":
                return "f.SAIL_DATE";
            default:
                String columnName = sortField.toUpperCase().replace(" ", "_");
                return "f." + columnName;
        }
    }

    @Override
    @Transactional
    public void softDeleteFda(Long transactionPoid, String userId) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        hdr.setDeleted("Y");
        hdr.setLastModifiedBy(userId);
        hdr.setLastModifiedDate(LocalDateTime.now());
        pdaFdaHdrRepository.save(hdr);

        List<PdaFdaDtl> details = pdaFdaDtlRepository.findByIdTransactionPoid(transactionPoid);
        pdaFdaDtlRepository.deleteAll(details);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FdaChargeDto> getCharges(Long transactionPoid, Long groupPoid, Long companyPoid, Pageable pageable) {

        pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        Page<FdaChargeDto> dtoPage = pdaFdaDtlRepository.findByTransactionPoid(transactionPoid, pageable)
                .map(ChargesMapper::mapChargeEntityToDto);

        List<FdaChargeDto> charges = dtoPage.getContent();

        for (FdaChargeDto chargeDto : charges) {
            setDetailsForCharge(chargeDto);
        }

        CalculationUtils.computeProfitLossRuntime(charges, null);

        return new PageResponse<>(
                charges,
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages(),
                dtoPage.isFirst(),
                dtoPage.isLast(),
                dtoPage.getNumberOfElements()
        );
    }

    @Override
    @Transactional
    public void saveCharges(Long transactionPoid, List<FdaChargeDto> chargeDtos, String userId, Long groupPoid, Long companyPoid) {
        List<PdaFdaDtl> toSave = new ArrayList<>();

        for (FdaChargeDto dto : chargeDtos) {
            String action = StringUtils.isNotBlank(dto.getActionType()) ? dto.getActionType().toLowerCase() : "";

            switch (action) {
                case "isdeleted":
                    if (dto.getDetRowId() != null) {
                        PdaFdaDtlId id = new PdaFdaDtlId(transactionPoid, dto.getDetRowId());
                        pdaFdaDtlRepository.findById(id).ifPresent(entity -> {
                            if ("N".equalsIgnoreCase(entity.getManual())) {
                                throw new CustomException("Cannot delete system-generated charge lines", 403);
                            }
                            pdaFdaDtlRepository.delete(entity);
                        });
                    }
                    break;
                case "iscreated":
                case "isupdated":
                    validationUtils.handleCreateOrUpdate(transactionPoid, dto, toSave, userId);
                    break;
                default:
                    // ignore unknown actions
            }
        }

        if (!toSave.isEmpty()) {
            pdaFdaDtlRepository.saveAll(toSave);
        }

        validationUtils.recalculateHeaderTotals(transactionPoid, userId, groupPoid, companyPoid);
    }

    @Override
    @Transactional
    public void deleteCharge(Long transactionPoid, Long detRowId, String userId) {
        PdaFdaDtlId id = new PdaFdaDtlId(transactionPoid, detRowId);

        PdaFdaDtl entity = pdaFdaDtlRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("FDA Detail", "detRowId", detRowId));

        if ("N".equalsIgnoreCase(entity.getManual())) {
            throw new CustomException("Cannot delete system-generated charge lines", 403);
        }

        pdaFdaDtlRepository.delete(entity);
    }

    @Override
    @Transactional
    public String closeFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        if ("C".equalsIgnoreCase(hdr.getStatus())) {
            throw new CustomException("FDA is already closed.", 400);
        }

        if (hdr.getVesselSailDate() == null) {
            throw new CustomException("Actual Vessel Sail Date is required for closing the FDA.", 400);
        }

        if (!"Y".equalsIgnoreCase(hdr.getAccountsVerified())) {
            throw new CustomException("Accounts verification is required before closing the FDA.", 400);
        }

        if (hdr.getTotalAmount() == null || hdr.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new CustomException("FDA total amount is zero. Please use Close Without Amount option.", 400);
        }

        return fdaCustomRepository.closeFda(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    @Override
    @Transactional
    public String reopenFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid, FdaReOpenDto fdaReOpenDto) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        if (!"CLOSED".equalsIgnoreCase(hdr.getStatus())) {
            throw new CustomException("Only closed FDAs can be reopened.", 400);
        }

        return fdaCustomRepository.reopenFda(groupPoid, companyPoid, userPoid, transactionPoid, fdaReOpenDto.getComment());
    }

    @Override
    @Transactional
    public String submitFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(hdr.getStatus())) {
            throw new CustomException("Closed FDAs cannot be submitted.", 400);
        }

        return fdaCustomRepository.submitFda(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    @Override
    @Transactional
    public String verifyFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(hdr.getStatus())) {
            throw new CustomException("Closed FDAs cannot be verified.", 400);
        }

        hdr.setAccountsVerified("Y");
        hdr.setLastModifiedBy(String.valueOf(userPoid));
        hdr.setLastModifiedDate(LocalDateTime.now());
        pdaFdaHdrRepository.save(hdr);

        return fdaCustomRepository.verifyFda(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String returnFda(Long groupPoid, Long companyPoid, Long userPoid,
                            Long transactionPoid, String correctionRemarks) {
        if (StringUtils.isBlank(correctionRemarks)) {
            throw new CustomException("Correction remarks are required", 400);
        }

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA", "Transaction Poid", transactionPoid));

        hdr.setOpsCorrectionRemarks(correctionRemarks);
        hdr.setOpsReturnedDate(LocalDate.now());
        hdr.setAccountsVerified("N");
        hdr.setLastModifiedBy("SYSTEM");
        hdr.setLastModifiedDate(LocalDateTime.now());

        pdaFdaHdrRepository.save(hdr);

        return fdaCustomRepository.returnFda(groupPoid, companyPoid, userPoid, transactionPoid, correctionRemarks);
    }

    @Override
    @Transactional
    public String supplementaryFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {
        return fdaCustomRepository.supplementaryFda(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FdaSupplementaryInfoDto> getSupplementaryInfo(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        return fdaCustomRepository.getSupplementaryInfo(transactionPoid, groupPoid, companyPoid, userPoid);
    }

    @Override
    @Transactional
    public String closeFdaWithoutAmount(Long transactionPoid, Long groupPoid, Long companyPoid,
                                        Long userPoid, String closedRemark) {

        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(hdr.getStatus())) {
            throw new CustomException("FDA is already closed.", 400);
        }

        if (StringUtils.isBlank(closedRemark)) {
            throw new CustomException("Closed remarks are required for closing FDA without amount.", 400);
        }

        if (hdr.getVesselSailDate() == null) {
            throw new CustomException("Actual Vessel Sail Date is required for closing the FDA.", 400);
        }

        if (!"Y".equalsIgnoreCase(hdr.getAccountsVerified())) {
            throw new CustomException("Accounts verification is required before closing the FDA.", 400);
        }

        if (hdr.getTotalAmount() != null && hdr.getTotalAmount().compareTo(BigDecimal.ZERO) != 0) {
            throw new CustomException("Close Without Amount is allowed only when FDA total amount is zero.", 400);
        }

        return fdaCustomRepository.closeFdaWithoutAmount(transactionPoid, groupPoid, companyPoid, userPoid, closedRemark);
    }

    @Override
    @Transactional(readOnly = true)
    public PartyGlResponse getPartyGl(Long groupPoid, Long companyPoid, Long userPoid, Long partyPoid, String partyType) {
        return fdaCustomRepository.getPartyGl(groupPoid, companyPoid, userPoid, partyPoid, partyType);
    }

    @Override
    @Transactional
    public String createFdaFromPda(Long groupPoid, Long companyPoid, Long userPoid, Long pdaTransactionPoid) {
        return fdaCustomRepository.createFdaFromPda(groupPoid, companyPoid, userPoid, pdaTransactionPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PdaLogResponse> getPdaLogs(Long transactionPoid, Long groupPoid, Long companyPoid) {

        Optional<PdaFdaHdr> fdaHdrOptional = pdaFdaHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoidAndDeleted(transactionPoid, groupPoid, companyPoid, "N");

        if (fdaHdrOptional.isEmpty()) {
            return List.of();
        }
        PdaFdaHdr fdaHeader = fdaHdrOptional.get();

        List<PdaFdaHdr> logs = pdaFdaHdrRepository
                .findByPdaRefAndGroupPoidAndCompanyPoidAndDeleted(fdaHeader.getPdaRef(), groupPoid, companyPoid, "N");

        if (logs.isEmpty()) {
            return List.of();
        }

        List<Long> pdaTransactionIds = logs.stream()
                .map(fda -> {
                    try {
                        return Long.valueOf(fda.getPdaRef());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, PdaEntryHdr> pdaMap = pdaEntryHdrRepository.findAllById(pdaTransactionIds).stream()
                .collect(Collectors.toMap(pda -> String.valueOf(pda.getTransactionPoid()), pda -> pda));

        return logs.stream().map(fda -> {
            PdaLogResponse response = new PdaLogResponse();

            PdaEntryHdr pda = pdaMap.get(fda.getPdaRef());
            if (pda != null) {
                response.setPdaTransactionPoid(pda.getTransactionPoid());
                response.setPdaDocRef(pda.getDocRef());
                response.setPdaTransactionDate(pda.getTransactionDate());
            } else {
                response.setPdaTransactionPoid(null);
                response.setPdaDocRef(fda.getPdaRef());
                response.setPdaTransactionDate(null);
            }

            response.setFdaTransactionPoid(fda.getTransactionPoid());
            response.setFdaDocRef(fda.getDocRef());
            response.setFdaTransactionDate(fda.getTransactionDate());
            response.setFdaStatus(fda.getStatus());

            response.setDocumentSubmittedDate(fda.getDocumentSubmittedDate());
            response.setDocumentSubmittedBy(fda.getDocumentSubmittedBy());
            response.setDocumentSubmittedStatus(fda.getDocumentSubmittedStatus());

            response.setVerificationAcceptedDate(fda.getVerificationAcceptedDate());
            response.setVerificationAcceptedBy(fda.getVerificationAcceptedBy());
            response.setDocumentReceivedStatus(fda.getDocumentReceivedStatus());

            response.setCreatedBy(fda.getCreatedBy());
            response.setCreatedDate(fda.getCreatedDate());
            response.setLastModifiedBy(fda.getLastModifiedBy());
            response.setLastModifiedDate(fda.getLastModifiedDate());

            return response;
        }).collect(Collectors.toList());
    }

//    @Override
//    @Transactional
//    public Resource generateFdaReport(Long transactionPoid, String reportType,
//                                      Long companyId, Long userId, Long groupId) {
//        Connection connection = null;
//        try {
//            String reportFileName = CalculationUtils.getReportFileName(reportType);
//
//            InputStream reportStream = getClass().getClassLoader()
//                    .getResourceAsStream("reports/" + reportFileName);
//
//            if (reportStream == null) {
//                throw new ResourceNotFoundException("Report template", "Report File Name", "reports/" + reportFileName);
//            }
//
//            JasperReport jasperReport;
//            try {
//                InputStream compiledStream = getClass().getClassLoader()
//                        .getResourceAsStream("reports/" + reportFileName.replace(".jrxml", ".jasper"));
//                if (compiledStream != null) {
//                    jasperReport = (JasperReport) JRLoader.loadObject(compiledStream);
//                } else {
//                    jasperReport = JasperCompileManager.compileReport(reportStream);
//                }
//            } finally {
//                reportStream.close();
//            }
//
//            Map<String, Object> parameters = new HashMap<>();
//            parameters.put("DOC_KEY_POID", transactionPoid);
//            parameters.put("LOGIN_COMP_POID", companyId);
//            parameters.put("LOGIN_USER_POID", userId);
//            parameters.put("LOGIN_GROUP_POID", groupId);
//
//            connection = jdbcTemplate.getDataSource().getConnection();
//
//            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
//
//            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
//
//            return new ByteArrayResource(pdfBytes);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Error generating FDA report: " + e.getMessage(), e);
//        } finally {
//            if (connection != null) {
//                try {
//                    connection.close();
//                } catch (SQLException ignored) {
//                }
//            }
//        }
//    }

    private void setDetailsForHeader(FdaHeaderDto fdaHeaderDto) {

        fdaHeaderDto.setGroupDet(lovService.getLovItemByPoid(fdaHeaderDto.getGroupPoid(), "GROUP", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setCompanyDet(lovService.getLovItemByPoid(fdaHeaderDto.getCompanyPoid(), "COMPANY", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setPrincipalDet(lovService.getLovItemByPoid(fdaHeaderDto.getPrincipalPoid(), "PRINCIPAL_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setVoyageDet(lovService.getLovItemByPoid(fdaHeaderDto.getVoyagePoid(), "VESSAL_VOYAGE", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setVesselDet(lovService.getLovItemByPoid(fdaHeaderDto.getVesselPoid(), "VESSEL_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setPortDet(lovService.getLovItemByPoid(fdaHeaderDto.getPortPoid(), "PDA_PORT_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setAddressDet(lovService.getLovItemByPoid(fdaHeaderDto.getAddressPoid(), "ADDRESS_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setSalesmanDet(lovService.getLovItemByPoid(fdaHeaderDto.getSalesmanPoid(), "SALESMAN", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setTermsDet(lovService.getLovItemByPoid(fdaHeaderDto.getTermsPoid(), "TERMS_TEMPLATE_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setVesselTypeDet(lovService.getLovItemByPoid(Long.valueOf(fdaHeaderDto.getVesselTypePoid()), "VESSEL_TYPE_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setLineDet(lovService.getLovItemByPoid(fdaHeaderDto.getLinePoid(), "LINE_MASTER_ALL", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setPrintBankDet(lovService.getLovItemByPoid(fdaHeaderDto.getPrintBankPoid(), "BANK_MASTER_COMPANYWISE", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setVesselHandledByDet(lovService.getLovItemByPoid(fdaHeaderDto.getVesselHandledBy(), "PDA_USER_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setCommodityDet(lovService.getLovItemByCode(fdaHeaderDto.getCommodityPoid(), "COMODITY", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setNominatedPartyTypeDet(lovService.getLovItemByCode(fdaHeaderDto.getNominatedPartyType(), "PDA_NOMINATED_PARTY_TYPE", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setOperationTypeDet(lovService.getLovItemByCode(fdaHeaderDto.getOperationType(), "PDA_OPERATION_TYPES", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setUnitDet(lovService.getLovItemByCode(fdaHeaderDto.getUnit(), "UNIT_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        fdaHeaderDto.setPdaRefDet(lovService.getLovItemByCode(fdaHeaderDto.getPdaRef(), "PROCESS_PDA", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        if (StringUtils.isNotBlank(fdaHeaderDto.getNominatedPartyType()) && "CUSTOMER".equalsIgnoreCase(fdaHeaderDto.getNominatedPartyType())) {
            fdaHeaderDto.setNominatedPartyDet(lovService.getLovItemByPoid(fdaHeaderDto.getNominatedPartyPoid(), "PDA_NOMINATED_PARTY_CUSTOMER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        }
        if (StringUtils.isNotBlank(fdaHeaderDto.getNominatedPartyType()) && "PRINCIPAL".equalsIgnoreCase(fdaHeaderDto.getNominatedPartyType())) {
            fdaHeaderDto.setNominatedPartyDet(lovService.getLovItemByPoid(fdaHeaderDto.getNominatedPartyPoid(), "PDA_NOMINATED_PARTY_PRINCIPAL", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        }
    }

    private void setDetailsForCharge(FdaChargeDto charge) {
        charge.setChargeDet(lovService.getLovItemByPoid(charge.getChargePoid(), "CHARGE_MASTER_FOR_PDA", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        charge.setRateTypeDet(lovService.getLovItemByPoid(charge.getRateTypePoid(), "PDA_RATE_TYPE_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        charge.setPrincipalDet(lovService.getLovItemByPoid(charge.getPrincipalPoid(), "PRINCIPAL_MASTER_FOR_PDA", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        charge.setPdaDet(lovService.getLovItemByPoid(charge.getPdaPoid(), "PROCESS_PDA", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        charge.setDetailsFromDet(lovService.getLovItemByCode(charge.getDetailsFrom(), "FDA_DETAIL", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
    }

}
