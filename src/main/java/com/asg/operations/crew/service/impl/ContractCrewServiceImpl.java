package com.asg.operations.crew.service.impl;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.enums.LogDetailsEnum;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.crew.dto.*;
import com.asg.operations.crew.entity.ContractCrew;
import com.asg.operations.crew.entity.ContractCrewDtl;
import com.asg.operations.crew.entity.ContractCrewDtlId;
import com.asg.operations.crew.repository.ContractCrewDtlRepository;
import com.asg.operations.crew.repository.ContractCrewRepository;
import com.asg.operations.crew.util.CrewCodeGenerator;
import com.asg.operations.crew.util.EntityMapper;
import com.asg.operations.crew.util.ValidationUtil;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.exceptions.ValidationException;
import com.asg.operations.crew.service.ContractCrewService;
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
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for Contract Crew Master operations
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ContractCrewServiceImpl implements ContractCrewService {

    private final ContractCrewRepository crewRepository;
    private final ContractCrewDtlRepository crewDtlRepository;
    private final EntityMapper entityMapper;
    private final CrewCodeGenerator codeGenerator;
    private final EntityManager entityManager;
    private final LovService lovService;
    private final LoggingService loggingService;
    private final DocumentDeleteService documentDeleteService;

    @Override
    @Transactional(readOnly = true)
    public Page<ContractCrewListResponse> getAllCrewWithFilters(Long groupPoid, Long companyPoid, GetAllCrewFilterRequest filterRequest, int page, int size, String sort) {

        // Build dynamic SQL query
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT c.CREW_POID, c.CREW_NAME, c.CREW_NATION_POID, c.CREW_CDC_NUMBER, ");
        sqlBuilder.append("c.CREW_COMPANY, c.CREW_DESIGNATION, c.CREW_PASSPORT_NUMBER, ");
        sqlBuilder.append("c.CREW_PASSPORT_ISS_DATE, c.CREW_PASSPORT_EXP_DATE, c.CREW_PASSPORT_ISS_PLACE, ");
        sqlBuilder.append("c.REMARKS, c.GROUP_POID, c.COMPANY_POID, c.ACTIVE, c.SEQNO, c.DELETED, ");
        sqlBuilder.append("c.CREATED_BY, c.CREATED_DATE, c.LASTMODIFIED_BY, c.LASTMODIFIED_DATE ");
        sqlBuilder.append("FROM CONTRACT_CREW_MASTER c ");
        sqlBuilder.append("WHERE c.GROUP_POID = :groupPoid AND c.COMPANY_POID = :companyPoid ");

        // Apply isDeleted filter (using ACTIVE field)
        if (filterRequest.getIsDeleted() != null && "N".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND c.ACTIVE = 'Y' ");
        } else if (filterRequest.getIsDeleted() != null && "Y".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND c.ACTIVE = 'N' ");
        }

        // Build filter conditions with sequential parameter indexing
        List<String> filterConditions = new java.util.ArrayList<>();
        List<GetAllCrewFilterRequest.FilterItem> validFilters = new java.util.ArrayList<>();
        if (filterRequest.getFilters() != null && !filterRequest.getFilters().isEmpty()) {
            for (GetAllCrewFilterRequest.FilterItem filter : filterRequest.getFilters()) {
                if (org.springframework.util.StringUtils.hasText(filter.getSearchField()) && org.springframework.util.StringUtils.hasText(filter.getSearchValue())) {
                    validFilters.add(filter);
                    String columnName = mapCrewSearchFieldToColumn(filter.getSearchField());
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
        String orderBy = "ORDER BY c.CREATED_DATE DESC";
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


        // Set filter parameters using sequential indexing
        if (!validFilters.isEmpty()) {
            for (int i = 0; i < validFilters.size(); i++) {
                GetAllCrewFilterRequest.FilterItem filter = validFilters.get(i);
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
        List<ContractCrewListResponse> dtos = results.stream()
                .map(this::mapToCrewListResponseDto)
                .collect(Collectors.toList());
        // Create page
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(dtos, pageable, totalCount);
    }

    private void setLovDetails(List<ContractCrewResponse> dtos) {
        for (ContractCrewResponse dto : dtos) {
            dto.setGroupDet(lovService.getLovItemByPoid(dto.getGroupPoid(), "GROUP", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
            dto.setCompanyDet(lovService.getLovItemByPoid(dto.getCompanyPoid(), "COMPANY", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
            dto.setCrewNationalityDet(lovService.getLovItemByPoid(dto.getCrewNationalityPoid(), "NATIONALITY", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        }
    }

    private String mapCrewSearchFieldToColumn(String searchField) {
        if (searchField == null) {
            return null;
        }
        // Normalize the field name by removing underscores and converting to uppercase
        String normalizedField = searchField.toUpperCase().replace("_", "");

        switch (normalizedField) {
            case "CREWNAME":
            case "NAME":
                return "c.CREW_NAME";
            case "NATIONALITY":
            case "CREWNATIONPOID":
            case "NATIONALITYPOID":
                return "c.CREW_NATION_POID";
            case "CDCNUMBER":
            case "CREW_CDC_NUMBER":
            case "CDC":
                return "c.CREW_CDC_NUMBER";
            case "COMPANY":
            case "CREWCOMPANY":
                return "c.CREW_COMPANY";
            case "DESIGNATION":
            case "CREWDESIGNATION":
                return "c.CREW_DESIGNATION";
            case "CREWPASSPORTNUMBER":
            case "PASSPORTNUMBER":
            case "PASSPORT":
                return "c.CREW_PASSPORT_NUMBER";
            case "CREWPASSPORTISSDATE":
            case "PASSPORTISSUEPLACE":
            case "ISSUEPLACE":
                return "c.CREW_PASSPORT_ISS_PLACE";
            case "REMARKS":
                return "c.REMARKS";
            case "ACTIVE":
            case "STATUS":
                return "c.ACTIVE";
            case "DELETED":
                return "c.DELETED";
            case "CREATEDBY":
                return "c.CREATED_BY";
            case "LASTMODIFIEDBY":
            case "MODIFIEDBY":
                return "c.LASTMODIFIED_BY";
            default:
                // Fallback: assume it's a direct column name from c table
                String columnName = searchField.toUpperCase().replace(" ", "_");
                return "c." + columnName;
        }
    }

    private ContractCrewListResponse mapToCrewListResponseDto(Object[] row) {
        ContractCrewListResponse dto = new ContractCrewListResponse();

        dto.setCrewPoid(row[0] != null ? ((Number) row[0]).longValue() : null);
        dto.setCrewName(convertToString(row[1]));
        dto.setCrewNationalityPoid(row[2] != null ? ((Number) row[2]).longValue() : null);
        dto.setCrewCdcNumber(convertToString(row[3]));
        dto.setCrewCompany(convertToString(row[4]));
        dto.setCrewDesignation(convertToString(row[5]));
        dto.setCrewPassportNumber(convertToString(row[6]));
        dto.setCrewPassportIssueDate(row[7] != null ? ((Timestamp) row[7]).toLocalDateTime().toLocalDate() : null);
        dto.setCrewPassportExpiryDate(row[8] != null ? ((Timestamp) row[8]).toLocalDateTime().toLocalDate() : null);
        dto.setCrewPassportIssuePlace(convertToString(row[9]));
        dto.setRemarks(convertToString(row[10]));
        dto.setGroupPoid(row[11] != null ? ((Number) row[11]).longValue() : null);
        dto.setCompanyPoid(row[12] != null ? ((Number) row[12]).longValue() : null);
        dto.setActive(convertToString(row[13]));
        dto.setCreatedBy(convertToString(row[16]));
        dto.setCreatedDate(row[17] != null ? ((Timestamp) row[17]).toLocalDateTime() : null);
        dto.setLastModifiedBy(convertToString(row[18]));
        dto.setLastModifiedDate(row[19] != null ? ((Timestamp) row[19]).toLocalDateTime() : null);

        return dto;
    }

    private ContractCrewResponse mapToCrewResponseDto(Object[] row) {
        ContractCrewResponse dto = new ContractCrewResponse();

        dto.setCrewPoid(row[0] != null ? ((Number) row[0]).longValue() : null);
        dto.setCrewName(convertToString(row[1]));
        dto.setCrewNationalityPoid(row[2] != null ? ((Number) row[2]).longValue() : null);
        dto.setCrewCdcNumber(convertToString(row[3]));
        dto.setCrewCompany(convertToString(row[4]));
        dto.setCrewDesignation(convertToString(row[5]));
        dto.setCrewPassportNumber(convertToString(row[6]));
        dto.setCrewPassportIssueDate(row[7] != null ? ((Timestamp) row[7]).toLocalDateTime().toLocalDate() : null);
        dto.setCrewPassportExpiryDate(row[8] != null ? ((Timestamp) row[8]).toLocalDateTime().toLocalDate() : null);
        dto.setCrewPassportIssuePlace(convertToString(row[9]));
        dto.setRemarks(convertToString(row[10]));
        dto.setGroupPoid(row[11] != null ? ((Number) row[11]).longValue() : null);
        dto.setCompanyPoid(row[12] != null ? ((Number) row[12]).longValue() : null);
        dto.setActive(convertToString(row[13]));
        dto.setCreatedBy(convertToString(row[16]));
        dto.setCreatedDate(row[17] != null ? ((Timestamp) row[17]).toLocalDateTime() : null);
        dto.setLastModifiedBy(convertToString(row[18]));
        dto.setLastModifiedDate(row[19] != null ? ((Timestamp) row[19]).toLocalDateTime() : null);

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
            return "c.CREATED_DATE";
        }
        String normalizedField = sortField.toUpperCase().replace("_", "");

        switch (normalizedField) {
            case "CREWPOID":
                return "c.CREW_POID";
            case "CREWNAME":
            case "NAME":
                return "c.CREW_NAME";
            case "NATIONALITY":
            case "CREWNATIONPOID":
                return "c.CREW_NATION_POID";
            case "CDCNUMBER":
            case "CDC":
                return "c.CREW_CDC_NUMBER";
            case "COMPANY":
            case "CREWCOMPANY":
                return "c.CREW_COMPANY";
            case "DESIGNATION":
            case "CREWDESIGNATION":
                return "c.CREW_DESIGNATION";
            case "PASSPORTNUMBER":
            case "CREWPASSPORTNUMBER":
                return "c.CREW_PASSPORT_NUMBER";
            case "PASSPORTISSUEDATE":
            case "CREWPASSPORTISSDATE":
                return "c.CREW_PASSPORT_ISS_DATE";
            case "PASSPORTEXPIRYDATE":
            case "CREWPASSPORTEXPDATE":
                return "c.CREW_PASSPORT_EXP_DATE";
            case "PASSPORTISSUEPLACE":
            case "CREWPASSPORTISSPLACE":
                return "c.CREW_PASSPORT_ISS_PLACE";
            case "REMARKS":
                return "c.REMARKS";
            case "ACTIVE":
            case "STATUS":
                return "c.ACTIVE";
            case "CREATEDDATE":
                return "c.CREATED_DATE";
            case "LASTMODIFIEDDATE":
                return "c.LASTMODIFIED_DATE";
            case "CREATEDBY":
                return "c.CREATED_BY";
            case "LASTMODIFIEDBY":
                return "c.LASTMODIFIED_BY";
            default:
                String columnName = sortField.toUpperCase().replace(" ", "_");
                return "c." + columnName;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ContractCrewResponse getCrewById(Long crewPoid) {
        // BigDecimal companyPoid = securityContextUtil.getCurrentCompanyPoid();

        ContractCrew crew = crewRepository.findByCrewPoid(crewPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Crew master not found with id: " + crewPoid));

        // Get crew details
        List<ContractCrewDtl> details = crewDtlRepository.findByIdCrewPoidOrderByIdDetRowId(crewPoid);

        ContractCrewResponse response = entityMapper.toContractCrewRes(crew);
        response.setDetails(details.stream()
                .map(entityMapper::toContractCrewDtlResponse)
                .collect(Collectors.toList()));

        return response;
    }

    @Override
    public ContractCrewResponse createCrew(ContractCrewRequest request, Long companyPoid, Long groupPoid, String userId) {
        // Validate request
        validateCrewRequest(request);

        // Map request to entity
        ContractCrew crew = entityMapper.toContractCrewEntity(companyPoid, groupPoid, userId, request);

        // Generate crew code
        String crewCode = codeGenerator.generateCrewCode(
                companyPoid,
                groupPoid
        );
        //crew.setCrewCode(crewCode);

        // Save entity
        crew = crewRepository.save(crew);

        log.info("Crew created successfully with id: " + crew.getCrewPoid());
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            for (ContractCrewDtlRequest det : request.getDetails()) {
                this.saveCrewDetail(companyPoid, userId, crew.getCrewPoid(), det);
            }
        }

        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), crew.getCrewPoid().toString());
        return getCrewById(crew.getCrewPoid());
    }

    @Override
    public ContractCrewResponse updateCrew(Long companyPoid, String userId, Long crewPoid, ContractCrewRequest request) {
        // Validate request
        validateCrewRequest(request);

        // Check if crew exists

        ContractCrew crew = crewRepository.findByCrewPoidAndCompanyPoid(crewPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Crew master not found with id: " + crewPoid));

        ContractCrew oldCrew = new ContractCrew();
        BeanUtils.copyProperties(crew, oldCrew);

        // Update entity
        entityMapper.updateContractCrewEntity(crew, request);

        // Save updated entity
        crew = crewRepository.save(crew);

        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            for (ContractCrewDtlRequest det : request.getDetails()) {
                //this.saveCrewDetail(companyPoid,"",crew.getCrewPoid(),det);
                String action = StringUtils.isNotBlank(det.getActionType()) ? det.getActionType().toLowerCase() : "";
                log.info("Action: " + action);

                switch (action) {
                    case "iscreated" -> this.saveCrewDetail(companyPoid, userId, crew.getCrewPoid(), det);
                    case "isupdated" -> this.updateCrewDetail(companyPoid, userId, crew.getCrewPoid(), det);
                    case "isdeleted" -> this.deleteCrewDetail(companyPoid, crew.getCrewPoid(), det.getDetRowId());
                }

            }
        }

        loggingService.logChanges(oldCrew, crew, ContractCrew.class, UserContext.getDocumentId(), crewPoid.toString(), LogDetailsEnum.MODIFIED, "CREW_POID");
        return getCrewById(crew.getCrewPoid());
    }

    @Override
    public void deleteCrew(Long companyPoid, Long crewPoid, @Valid DeleteReasonDto deleteReasonDto) {

        ContractCrew crew = crewRepository.findByCrewPoidAndCompanyPoid(crewPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Crew master not found with id: " + crewPoid));

        documentDeleteService.deleteDocument(
                crewPoid,
                "GL_ADVANCE_PETTY_CASH_HDR",
                "CREW_POID",
                deleteReasonDto,
                LocalDate.now()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CrewDetailsResponse getCrewDetails(Long companyPoid, Long crewPoid) {
        // Verify crew exists

        boolean crewExists = crewRepository.findByCrewPoidAndCompanyPoid(crewPoid, companyPoid).isPresent();
        if (!crewExists) {
            throw new ResourceNotFoundException("Crew master not found with id: " + crewPoid);
        }

        // Get detail records
        List<ContractCrewDtl> details = crewDtlRepository.findByIdCrewPoidOrderByIdDetRowId(crewPoid);
        CrewDetailsResponse response = new CrewDetailsResponse();
        response.setCrewPoid(crewPoid);
        response.setDetails(this.toContractCrewDtlResponseList(details));

        return response;
    }

    private List<ContractCrewDtlResponse> toContractCrewDtlResponseList(List<ContractCrewDtl> details) {
        return details.stream()
                .map(entityMapper::toContractCrewDtlResponse)
                .collect(Collectors.toList());
    }

    private void saveCrewDetail(Long companyPoid, String userId, Long crewPoid, ContractCrewDtlRequest detailRequest) {

        ContractCrewDtl newDetail = entityMapper.toContractCrewDtlEntity(userId, detailRequest, crewPoid);
        Long max = crewDtlRepository.findMaxDetRowIdByCrewPoid(crewPoid);
        Long next = (max == null ? 0L : max) + 1L;
        newDetail.getId().setDetRowId(next);
        crewDtlRepository.save(newDetail);
    }

    public void updateCrewDetail(Long companyPoid, String userId, Long crewPoid, ContractCrewDtlRequest detailRequest) {
        ContractCrewDtlId id = new ContractCrewDtlId(crewPoid, detailRequest.getDetRowId());
        ContractCrewDtl detail = crewDtlRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Crew master not found with id: " + crewPoid));

        entityMapper.updateContractCrewDtlEntity(detail, detailRequest);
        crewDtlRepository.save(detail);

    }


    @Override
    public CrewDetailsResponse saveCrewDetails(Long companyPoid, String userId, Long crewPoid, BulkSaveDetailsRequest request) {
        // Verify crew exists

        ContractCrew crew = crewRepository.findByCrewPoidAndCompanyPoid(crewPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Crew master not found with id: " + crewPoid));


        // Validate detail records
        List<ValidationError> validationErrors = new ArrayList<>();

        // Validate dates
        if (request.getDetails() != null) {
            validationErrors.addAll(ValidationUtil.validateAllDetailDates(request.getDetails()));

            // Validate document types and other business rules
            for (int i = 0; i < request.getDetails().size(); i++) {
                ContractCrewDtlRequest detail = request.getDetails().get(i);

                String operation = detail.getActionType();

                // For UPDATE operations, verify record exists
                if ("isupdated".equals(operation) && detail.getDetRowId() != null) {
                    boolean recordExists = crewDtlRepository.existsByIdCrewPoidAndIdDetRowId(crewPoid, detail.getDetRowId());
                    if (!recordExists) {
                        validationErrors.add(new ValidationError(
                                i,
                                "detRowId",
                                "Detail record not found with detRowId: " + detail.getDetRowId()
                        ));
                    }
                }
            }
        }

        // If validation errors exist, throw exception
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(
                    "Validation errors occurred",
                    validationErrors
            );
        }

        // Execute bulk save within transaction
        List<ContractCrewDtl> savedDetails = new ArrayList<>();


        // Step 2: Update existing records
        if (request.getDetails() != null) {
            for (ContractCrewDtlRequest detailRequest : request.getDetails()) {
                String action = detailRequest.getActionType();

                if (StringUtils.isBlank(action)) {
                    log.warn("Detail entry has NULL actionType. Skipping...");
                    continue;
                }
                if ("isdeleted".equalsIgnoreCase(action)) {
                    this.deleteCrewDetail(companyPoid, crewPoid, detailRequest.getDetRowId());
                } else if ("isupdated".equalsIgnoreCase(action) && detailRequest.getDetRowId() != null) {
                    // Update existing record
                    ContractCrewDtlId id = new ContractCrewDtlId(
                            crewPoid,
                            detailRequest.getDetRowId()
                    );
                    Optional<ContractCrewDtl> existingDetail = crewDtlRepository.findById(id);
                    if (existingDetail.isPresent()) {
                        ContractCrewDtl detail = existingDetail.get();
                        entityMapper.updateContractCrewDtlEntity(detail, detailRequest);
                        detail = crewDtlRepository.save(detail);
                        savedDetails.add(detail);
                    }
                } else if ("iscreated".equalsIgnoreCase(action)) {
                    // Insert new record
                    ContractCrewDtl newDetail = entityMapper.toContractCrewDtlEntity(userId, detailRequest, crewPoid);
                    // Ensure embedded id has a detRowId. If missing, assign next available.
                    if (newDetail.getId() == null) {
                        newDetail.setId(new ContractCrewDtlId(crewPoid, null));
                    }
                    if (newDetail.getId().getDetRowId() == null) {
                        Long max = crewDtlRepository.findMaxDetRowIdByCrewPoid(crewPoid);
                        Long next = (max == null ? 0L : max) + 1L;
                        newDetail.getId().setDetRowId(next);
                    }
                    newDetail = crewDtlRepository.save(newDetail);
                    savedDetails.add(newDetail);
                }
            }
        }


        CrewDetailsResponse response = new CrewDetailsResponse();
        response.setCrewPoid(crewPoid);
        response.setDetails(this.toContractCrewDtlResponseList(savedDetails));

        return response;
    }

    @Override
    public void deleteCrewDetail(Long companyPoid, Long crewPoid, Long detRowId) {
        // Verify crew exists

        boolean crewExists = crewRepository.findByCrewPoidAndCompanyPoid(crewPoid, companyPoid).isPresent();
        if (!crewExists) {
            throw new ResourceNotFoundException("Crew master not found with id: " + crewPoid);
        }

        // Verify detail record exists
        ContractCrewDtl contractCrewDtl = crewDtlRepository.findByIdCrewPoidAndIdDetRowId(crewPoid, detRowId);
        if (contractCrewDtl == null || contractCrewDtl.getId().getDetRowId() == null) {
            throw new ResourceNotFoundException(
                    "Detail record not found with crewPoid: " + crewPoid + ", detRowId: " + detRowId
            );
        }

        crewDtlRepository.deleteById(contractCrewDtl.getId());
    }

    private void validateCrewRequest(ContractCrewRequest request) {
        List<ValidationError> errors = new ArrayList<>();

        // Validate passport dates
        ValidationError dateError = ValidationUtil.validatePassportDates(
                request.getCrewPassportIssueDate(),
                request.getCrewPassportExpiryDate()
        );
        if (dateError != null) {
            errors.add(dateError);
        }

        // If validation errors exist, throw exception
        if (!errors.isEmpty()) {
            throw new ValidationException(
                    "Validation errors occurred",
                    errors
            );
        }
    }
}

