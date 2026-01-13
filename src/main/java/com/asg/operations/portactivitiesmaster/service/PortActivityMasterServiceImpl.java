package com.asg.operations.portactivitiesmaster.service;

import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.enums.LogDetailsEnum;
import org.springframework.beans.BeanUtils;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.portactivitiesmaster.dto.*;
import com.asg.operations.portactivitiesmaster.entity.PortActivityMaster;
import com.asg.operations.portactivitiesmaster.repository.PortActivityMasterRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PortActivityMasterServiceImpl implements PortActivityMasterService {

    private final PortActivityMasterRepository repository;
    private final LovService lovService;
    private final EntityManager entityManager;
    private final LoggingService loggingService;

    @Override
    @Transactional(readOnly = true)
    public Page<PortActivityListResponse> getAllPortActivitiesWithFilters(
            Long groupPoid,
            GetAllPortActivityFilterRequest filterRequest,
            int page, int size, String sort) {

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT p.PORT_ACTIVITY_TYPE_POID, p.GROUP_POID, p.PORT_ACTIVITY_TYPE_CODE, ");
        sqlBuilder.append("p.PORT_ACTIVITY_TYPE_NAME, p.PORT_ACTIVITY_TYPE_NAME2, p.ACTIVE, ");
        sqlBuilder.append("p.SEQNO, p.CREATED_BY, p.CREATED_DATE, p.LASTMODIFIED_BY, ");
        sqlBuilder.append("p.LASTMODIFIED_DATE, p.DELETED, p.REMARKS ");
        sqlBuilder.append("FROM OPS_PORT_ACTIVITY_MASTER p ");
        sqlBuilder.append("WHERE p.GROUP_POID = :groupPoid ");

        if (filterRequest.getIsDeleted() != null && "N".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND (p.DELETED IS NULL OR p.DELETED != 'Y') ");
        } else if (filterRequest.getIsDeleted() != null && "Y".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND p.DELETED = 'Y' ");
        }

        List<String> filterConditions = new java.util.ArrayList<>();
        List<GetAllPortActivityFilterRequest.FilterItem> validFilters = new java.util.ArrayList<>();
        if (filterRequest.getFilters() != null && !filterRequest.getFilters().isEmpty()) {
            for (GetAllPortActivityFilterRequest.FilterItem filter : filterRequest.getFilters()) {
                if (org.springframework.util.StringUtils.hasText(filter.getSearchField()) && org.springframework.util.StringUtils.hasText(filter.getSearchValue())) {
                    validFilters.add(filter);
                    String columnName = mapPortActivitySearchFieldToColumn(filter.getSearchField());
                    int paramIndex = validFilters.size() - 1;
                    filterConditions.add("LOWER(" + columnName + ") LIKE LOWER(:filterValue" + paramIndex + ")");
                }
            }
        }

        if (!filterConditions.isEmpty()) {
            String operator = "AND".equalsIgnoreCase(filterRequest.getOperator()) ? " AND " : " OR ";
            sqlBuilder.append("AND (").append(String.join(operator, filterConditions)).append(") ");
        }

        String orderBy = "ORDER BY p.PORT_ACTIVITY_TYPE_CODE ASC";
        if (org.springframework.util.StringUtils.hasText(sort)) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String sortField = mapPortActivitySortFieldToColumn(sortParts[0].trim());
                String sortDirection = sortParts[1].trim().toUpperCase();
                if ("ASC".equals(sortDirection) || "DESC".equals(sortDirection)) {
                    orderBy = "ORDER BY " + sortField + " " + sortDirection + " NULLS LAST";
                }
            }
        }
        sqlBuilder.append(orderBy);

        String countSql = "SELECT COUNT(*) FROM (" + sqlBuilder.toString() + ")";
        jakarta.persistence.Query query = entityManager.createNativeQuery(sqlBuilder.toString());
        jakarta.persistence.Query countQuery = entityManager.createNativeQuery(countSql);

        query.setParameter("groupPoid", groupPoid);
        countQuery.setParameter("groupPoid", groupPoid);

        if (!validFilters.isEmpty()) {
            for (int i = 0; i < validFilters.size(); i++) {
                GetAllPortActivityFilterRequest.FilterItem filter = validFilters.get(i);
                String paramValue = "%" + filter.getSearchValue() + "%";
                query.setParameter("filterValue" + i, paramValue);
                countQuery.setParameter("filterValue" + i, paramValue);
            }
        }

        Long totalCount = ((Number) countQuery.getSingleResult()).longValue();
        int offset = page * size;
        query.setFirstResult(offset);
        query.setMaxResults(size);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<PortActivityListResponse> dtos = results.stream()
                .map(this::mapToPortActivityListResponseDto)
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(dtos, pageable, totalCount);
    }

    private String mapPortActivitySearchFieldToColumn(String searchField) {
        if (searchField == null) return null;
        String normalizedField = searchField.toUpperCase().replace("_", "");
        switch (normalizedField) {
            case "PORTACTIVITYTYPEPOID":
                return "p.PORT_ACTIVITY_TYPE_POID";
            case "GROUPPOID":
                return "p.GROUP_POID";
            case "PORTACTIVITYTYPECODE":
                return "p.PORT_ACTIVITY_TYPE_CODE";
            case "PORTACTIVITYTYPENAME":
                return "p.PORT_ACTIVITY_TYPE_NAME";
            case "PORTACTIVITYTYPENAME2":
                return "p.PORT_ACTIVITY_TYPE_NAME2";
            case "ACTIVE":
                return "p.ACTIVE";
            case "SEQNO":
                return "p.SEQNO";
            case "CREATEDBY":
                return "p.CREATED_BY";
            case "LASTMODIFIEDBY":
                return "p.LASTMODIFIED_BY";
            case "DELETED":
                return "p.DELETED";
            case "REMARKS":
                return "p.REMARKS";
            default:
                return "p." + searchField.toUpperCase().replace(" ", "_");
        }
    }

    private String mapPortActivitySortFieldToColumn(String sortField) {
        if (sortField == null) return "p.PORT_ACTIVITY_TYPE_CODE";
        String normalizedField = sortField.toUpperCase().replace("_", "");
        switch (normalizedField) {
            case "PORTACTIVITYTYPEPOID":
                return "p.PORT_ACTIVITY_TYPE_POID";
            case "GROUPPOID":
                return "p.GROUP_POID";
            case "PORTACTIVITYTYPECODE":
                return "p.PORT_ACTIVITY_TYPE_CODE";
            case "PORTACTIVITYTYPENAME":
                return "p.PORT_ACTIVITY_TYPE_NAME";
            case "PORTACTIVITYTYPENAME2":
                return "p.PORT_ACTIVITY_TYPE_NAME2";
            case "ACTIVE":
                return "p.ACTIVE";
            case "SEQNO":
                return "p.SEQNO";
            case "CREATEDBY":
                return "p.CREATED_BY";
            case "CREATEDDATE":
                return "p.CREATED_DATE";
            case "LASTMODIFIEDBY":
                return "p.LASTMODIFIED_BY";
            case "LASTMODIFIEDDATE":
                return "p.LASTMODIFIED_DATE";
            case "DELETED":
                return "p.DELETED";
            case "REMARKS":
                return "p.REMARKS";
            default:
                return "p." + sortField.toUpperCase().replace(" ", "_");
        }
    }

    private PortActivityListResponse mapToPortActivityListResponseDto(Object[] row) {
        PortActivityListResponse dto = new PortActivityListResponse();
        dto.setPortActivityTypePoid(row[0] != null ? ((Number) row[0]).longValue() : null);
        dto.setGroupPoid(row[1] != null ? ((Number) row[1]).longValue() : null);
        dto.setPortActivityTypeCode(convertToString(row[2]));
        dto.setPortActivityTypeName(convertToString(row[3]));
        dto.setPortActivityTypeName2(convertToString(row[4]));
        dto.setActive(convertToString(row[5]));
        dto.setSeqno(row[6] != null ? ((Number) row[6]).longValue() : null);
        dto.setCreatedBy(convertToString(row[7]));
        dto.setCreatedDate(row[8] != null ? ((java.sql.Timestamp) row[8]).toLocalDateTime() : null);
        dto.setLastModifiedBy(convertToString(row[9]));
        dto.setLastModifiedDate(row[10] != null ? ((java.sql.Timestamp) row[10]).toLocalDateTime() : null);
        dto.setDeleted(convertToString(row[11]));
        dto.setRemarks(convertToString(row[12]));
        return dto;
    }

    private String convertToString(Object value) {
        return value != null ? value.toString() : null;
    }

    @Override
    @Transactional(readOnly = true)
    public PortActivityMasterResponse getPortActivityById(Long portActivityTypePoid, Long groupPoid) {
        PortActivityMaster entity = repository.findByPortActivityTypePoidAndGroupPoid(portActivityTypePoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Port activity not found"));

        return mapToResponse(entity);
    }

    @Override
    public PortActivityMasterResponse createPortActivity(PortActivityMasterRequest request, Long groupPoid, String userId) {
        PortActivityMaster entity = PortActivityMaster.builder()
                .groupPoid(groupPoid)
                .portActivityTypeCode(generatePortActivityTypeCode(groupPoid))
                .portActivityTypeName(request.getPortActivityTypeName())
                .portActivityTypeName2(request.getPortActivityTypeName2())
                .active(StringUtils.isNotBlank(request.getActive()) ? request.getActive() : "Y")
                .seqno(request.getSeqno())
                .remarks(request.getRemarks())
                .createdBy(userId)
                .createdDate(LocalDateTime.now())
                .lastModifiedBy(userId)
                .lastModifiedDate(LocalDateTime.now())
                .deleted("N")
                .build();

        entity = repository.save(entity);
        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), entity.getPortActivityTypePoid().toString());
        return mapToResponse(entity);
    }

    @Override
    public PortActivityMasterResponse updatePortActivity(Long portActivityTypePoid, PortActivityMasterRequest request, Long groupPoid, String userId) {
        PortActivityMaster entity = repository.findByPortActivityTypePoidAndGroupPoid(portActivityTypePoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Port activity not found"));

        PortActivityMaster oldEntity = new PortActivityMaster();
        BeanUtils.copyProperties(entity, oldEntity);

        entity.setPortActivityTypeName(request.getPortActivityTypeName());
        entity.setPortActivityTypeName2(request.getPortActivityTypeName2());
        entity.setActive(StringUtils.isNotBlank(request.getActive()) ? request.getActive() : entity.getActive());
        entity.setSeqno(request.getSeqno());
        entity.setRemarks(request.getRemarks());
        entity.setLastModifiedBy(userId);
        entity.setLastModifiedDate(LocalDateTime.now());

        entity = repository.save(entity);
        loggingService.logChanges(oldEntity, entity, PortActivityMaster.class, UserContext.getDocumentId(), entity.getPortActivityTypePoid().toString(), LogDetailsEnum.MODIFIED, "PORT_ACTIVITY_TYPE_POID");
        return mapToResponse(entity);
    }

    @Override
    public void deletePortActivity(Long portActivityTypePoid, Long groupPoid, String userId, boolean hardDelete) {
        PortActivityMaster entity = repository.findByPortActivityTypePoidAndGroupPoid(portActivityTypePoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Port activity not found"));

        if (hardDelete) {
            repository.delete(entity);
        } else {
            entity.setDeleted("Y");
            entity.setActive("N");
            entity.setLastModifiedBy(userId);
            entity.setLastModifiedDate(LocalDateTime.now());
            repository.save(entity);
        }
    }

    private PortActivityMasterResponse mapToResponse(PortActivityMaster entity) {
        return PortActivityMasterResponse.builder()
                .portActivityTypePoid(entity.getPortActivityTypePoid())
                .groupPoid(entity.getGroupPoid())
                .groupDet(lovService.getLovItemByPoid(entity.getGroupPoid(), "GROUP", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()))
                .portActivityTypeCode(entity.getPortActivityTypeCode())
                .portActivityTypeName(entity.getPortActivityTypeName())
                .portActivityTypeName2(entity.getPortActivityTypeName2())
                .active(entity.getActive())
                .seqno(entity.getSeqno())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .lastModifiedBy(entity.getLastModifiedBy())
                .lastModifiedDate(entity.getLastModifiedDate())
                .deleted(entity.getDeleted())
                .remarks(entity.getRemarks())
                .build();
    }

    private String generatePortActivityTypeCode(Long groupPoid) {
        String prefix = "PA";
        Integer maxSeq = repository.findMaxCodeSequence(prefix, groupPoid);
        int nextSeq = (maxSeq == null ? 1 : maxSeq + 1);

        return prefix + nextSeq;
    }


}