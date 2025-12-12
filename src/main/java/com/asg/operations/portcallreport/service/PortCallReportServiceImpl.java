package com.asg.operations.portcallreport.service;

import com.asg.operations.commonlov.dto.LovItem;
import com.asg.operations.commonlov.dto.LovResponse;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.exceptions.CustomException;
import com.asg.operations.exceptions.ResourceAlreadyExistsException;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.portactivitiesmaster.repository.PortActivityMasterRepository;
import com.asg.operations.portcallreport.dto.PortActivityResponseDto;
import com.asg.operations.portcallreport.dto.*;
import com.asg.operations.portcallreport.entity.PortCallReportDtl;
import com.asg.operations.portcallreport.entity.PortCallReportDtlId;
import com.asg.operations.portcallreport.entity.PortCallReportHdr;
import com.asg.operations.portcallreport.enums.ActionType;
import com.asg.operations.portcallreport.repository.PortCallReportDtlRepository;
import com.asg.operations.portcallreport.repository.PortCallReportHdrRepository;
import com.asg.operations.user.entity.User;
import com.asg.operations.user.repository.UserRepository;
import com.asg.operations.vesseltype.entity.VesselType;
import com.asg.operations.vesseltype.repository.VesselTypeRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortCallReportServiceImpl implements PortCallReportService {

    private final JdbcTemplate jdbcTemplate;
    private final PortCallReportHdrRepository hdrRepository;
    private final PortCallReportDtlRepository dtlRepository;
    private final UserRepository userRepository;
    private final VesselTypeRepository vesselTypeRepository;
    private final PortActivityMasterRepository portActivityMasterRepository;
    private final LovService lovService;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Page<PortCallReportResponseDto> getAllPortCallReportsWithFilters(
            Long groupPoid,
            GetAllPortCallReportFilterRequest filterRequest,
            int page, int size, String sort) {

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT h.PORT_CALL_REPORT_POID, h.PORT_CALL_REPORT_ID, h.PORT_CALL_REPORT_NAME, ");
        sqlBuilder.append("h.PORT_CALL_APPL_VESSEL_TYPE, h.ACTIVE, h.SEQNO, h.REMARKS, ");
        sqlBuilder.append("h.CREATED_BY, h.CREATED_DATE, h.LASTMODIFIED_BY, h.LASTMODIFIED_DATE, h.DELETED ");
        sqlBuilder.append("FROM OPS_PORT_CALL_REPORT_HDR h ");
        sqlBuilder.append("WHERE h.GROUP_POID = :groupPoid ");

        if (filterRequest.getIsDeleted() != null && "N".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND (h.DELETED IS NULL OR h.DELETED != 'Y') ");
        } else if (filterRequest.getIsDeleted() != null && "Y".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND h.DELETED = 'Y' ");
        }

        List<String> filterConditions = new java.util.ArrayList<>();
        List<GetAllPortCallReportFilterRequest.FilterItem> validFilters = new java.util.ArrayList<>();
        if (filterRequest.getFilters() != null && !filterRequest.getFilters().isEmpty()) {
            for (GetAllPortCallReportFilterRequest.FilterItem filter : filterRequest.getFilters()) {
                if (StringUtils.hasText(filter.getSearchField()) && StringUtils.hasText(filter.getSearchValue())) {
                    validFilters.add(filter);
                    String columnName = mapPortCallReportSearchFieldToColumn(filter.getSearchField());
                    int paramIndex = validFilters.size() - 1;
                    filterConditions.add("LOWER(" + columnName + ") LIKE LOWER(:filterValue" + paramIndex + ")");
                }
            }
        }

        if (!filterConditions.isEmpty()) {
            String operator = "AND".equalsIgnoreCase(filterRequest.getOperator()) ? " AND " : " OR ";
            sqlBuilder.append("AND (").append(String.join(operator, filterConditions)).append(") ");
        }

        String orderBy = "ORDER BY h.PORT_CALL_REPORT_ID ASC";
        if (StringUtils.hasText(sort)) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String sortField = mapPortCallReportSortFieldToColumn(sortParts[0].trim());
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
                GetAllPortCallReportFilterRequest.FilterItem filter = validFilters.get(i);
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
        List<PortCallReportResponseDto> dtos = results.stream()
                .map(this::mapToPortCallReportResponseDto)
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(dtos, pageable, totalCount);
    }

    private String mapPortCallReportSearchFieldToColumn(String searchField) {
        if (searchField == null) return null;
        String normalizedField = searchField.toUpperCase().replace("_", "");
        switch (normalizedField) {
            case "PORTCALLREPORTPOID": return "h.PORT_CALL_REPORT_POID";
            case "PORTCALLREPORTID": return "h.PORT_CALL_REPORT_ID";
            case "PORTCALLREPORTNAME": return "h.PORT_CALL_REPORT_NAME";
            case "PORTCALLAPPLVESSELTYPE": return "h.PORT_CALL_APPL_VESSEL_TYPE";
            case "ACTIVE": return "h.ACTIVE";
            case "SEQNO": return "h.SEQNO";
            case "REMARKS": return "h.REMARKS";
            case "CREATEDBY": return "h.CREATED_BY";
            case "LASTMODIFIEDBY": return "h.LASTMODIFIED_BY";
            case "DELETED": return "h.DELETED";
            default: return "h." + searchField.toUpperCase().replace(" ", "_");
        }
    }

    private String mapPortCallReportSortFieldToColumn(String sortField) {
        if (sortField == null) return "h.PORT_CALL_REPORT_ID";
        String normalizedField = sortField.toUpperCase().replace("_", "");
        switch (normalizedField) {
            case "PORTCALLREPORTPOID": return "h.PORT_CALL_REPORT_POID";
            case "PORTCALLREPORTID": return "h.PORT_CALL_REPORT_ID";
            case "PORTCALLREPORTNAME": return "h.PORT_CALL_REPORT_NAME";
            case "PORTCALLAPPLVESSELTYPE": return "h.PORT_CALL_APPL_VESSEL_TYPE";
            case "ACTIVE": return "h.ACTIVE";
            case "SEQNO": return "h.SEQNO";
            case "REMARKS": return "h.REMARKS";
            case "CREATEDBY": return "h.CREATED_BY";
            case "CREATEDDATE": return "h.CREATED_DATE";
            case "LASTMODIFIEDBY": return "h.LASTMODIFIED_BY";
            case "LASTMODIFIEDDATE": return "h.LASTMODIFIED_DATE";
            case "DELETED": return "h.DELETED";
            default: return "h." + sortField.toUpperCase().replace(" ", "_");
        }
    }

    private PortCallReportResponseDto mapToPortCallReportResponseDto(Object[] row) {
        String vesselTypes = convertToString(row[3]);
        List<String> vesselTypeList = new ArrayList<>();
        if (vesselTypes != null && !vesselTypes.trim().isEmpty()) {
            vesselTypeList = List.of(vesselTypes.split(","));
        }
        
        return PortCallReportResponseDto.builder()
                .portCallReportPoid(row[0] != null ? ((Number) row[0]).longValue() : null)
                .portCallReportId(convertToString(row[1]))
                .portCallReportName(convertToString(row[2]))
                .portCallApplVesselType(vesselTypeList)
                .active(convertToString(row[4]))
                .seqno(row[5] != null ? ((Number) row[5]).longValue() : null)
                .remarks(convertToString(row[6]))
                .build();
    }

    private String convertToString(Object value) {
        return value != null ? value.toString() : null;
    }

    private String generateReportId() {
        Long maxId = jdbcTemplate.queryForObject(
                "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(PORT_CALL_REPORT_ID, '[0-9]+'))), 0) FROM OPS_PORT_CALL_REPORT_HDR",
                Long.class);
        return "PCR" + String.format("%05d", maxId + 1);
    }

    @Override
    public PortCallReportResponseDto getReportById(Long id) {
        log.info("Fetching port call report by id: {}", id);

        PortCallReportHdr hdr = hdrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Port call report", "Port Call Report Poid", id));

        List<PortCallReportDtl> details = dtlRepository.findByPortCallReportPoid(id);

        String vesselTypes = hdr.getPortCallApplVesselType();
        List<LovItem> vesselTypeLovItems = null;
        List<String> vesselTypeList = new ArrayList<>();
        if (vesselTypes != null) {
            vesselTypeList = List.of(vesselTypes.split(","));
            LovResponse lovResponse = lovService.getLovList("VESSEL_TYPE_MASTER", null, null, null, null, null);
            if (lovResponse != null && lovResponse.getItems() != null) {
                Map<String, LovItem> vesselTypeByPoidMap = lovResponse.getItems().stream()
                        .collect(Collectors.toMap(LovItem::getCode, item -> item));
                vesselTypeLovItems = vesselTypeList.stream()
                        .map(vesselTypeByPoidMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        }

        LovResponse portActivityLov = lovService.getLovList("PORT_ACTIVITY", null, null, null, null, null);
        Map<Long, LovItem> portActivityMap = new HashMap<>();
        if (portActivityLov != null && portActivityLov.getItems() != null) {
            portActivityMap = portActivityLov.getItems().stream()
                    .collect(Collectors.toMap(LovItem::getPoid, item -> item));
        }

        Map<Long, LovItem> finalPortActivityMap = portActivityMap;
        List<PortCallReportDetailResponseDto> detailDtos = details.stream()
                .map(dtl -> PortCallReportDetailResponseDto.builder()
                        .detRowId(dtl.getDetRowId())
                        .portCallReportPoid(dtl.getPortCallReportPoid())
                        .portActivityTypePoid(dtl.getPortActivityTypePoid())
                        .portActivityTypeName(dtl.getPortActivityMaster() != null ? dtl.getPortActivityMaster().getPortActivityTypeName() : null)
                        .activityMandatory(dtl.getActivityMandatory())
                        .portActivityDet(finalPortActivityMap.get(dtl.getPortActivityTypePoid()))
                        .build())
                .collect(Collectors.toList());

        return PortCallReportResponseDto.builder()
                .portCallReportPoid(hdr.getPortCallReportPoid())
                .portCallReportId(hdr.getPortCallReportId())
                .portCallReportName(hdr.getPortCallReportName())
                .portCallApplVesselType(vesselTypeList)
                .portCallApplVesselTypeDet(vesselTypeLovItems)
                .active(hdr.getActive())
                .seqno(hdr.getSeqno())
                .remarks(hdr.getRemarks())
                .details(detailDtos)
                .build();
    }

    @Override
    @Transactional
    public PortCallReportResponseDto createReport(PortCallReportDto dto, Long userPoid, Long groupPoid) {
        log.info("Creating port call report: {}", dto.getPortCallReportName());

        if (groupPoid == null) {
            throw new CustomException("Group POID is required", 400);
        }

        if (hdrRepository.existsByPortCallReportNameIgnoreCaseAndNotDeleted(dto.getPortCallReportName(), null)) {
            throw new ResourceAlreadyExistsException("Port call report name", dto.getPortCallReportName());
        }

        if (dto.getPortCallApplVesselType() != null && !dto.getPortCallApplVesselType().isEmpty()) {
            List<Long> validVesselTypePoids = vesselTypeRepository.findAllActive().stream()
                    .map(VesselType::getVesselTypePoid)
                    .toList();
            for (String vesselTypePoid : dto.getPortCallApplVesselType()) {
                if (!validVesselTypePoids.contains(Long.parseLong(vesselTypePoid))) {
                    throw new CustomException("Invalid vessel type POID: " + vesselTypePoid, 400);
                }
            }
        }

        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            for (PortCallReportDetailDto detail : dto.getDetails()) {
                if (detail.getPortActivityTypePoid() == null) {
                    throw new CustomException("Port activity type is required", 400);
                }
                if (!portActivityMasterRepository.existsById(detail.getPortActivityTypePoid())) {
                    throw new CustomException("Invalid activity type ID: " + detail.getPortActivityTypePoid(), 400);
                }
            }
        }

        String reportId = generateReportId();

        User user = userRepository.findByUserPoid(userPoid)
                .orElseThrow(() -> new ResourceNotFoundException("User", "User Poid", userPoid));

        PortCallReportHdr hdr = PortCallReportHdr.builder()
                .portCallReportId(reportId)
                .groupPoid(groupPoid)
                .portCallReportName(dto.getPortCallReportName())
                .portCallApplVesselType(dto.getPortCallApplVesselType() != null ? String.join(",", dto.getPortCallApplVesselType()) : null)
                .active(dto.getActive())
                .seqno(dto.getSeqno())
                .remarks(dto.getRemarks())
                .createdBy(user.getUserId())
                .build();

        hdr = hdrRepository.save(hdr);

        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            Long reportPoid = hdr.getPortCallReportPoid();
            Long nextDetRowId = dtlRepository.findMaxDetRowIdByPortCallReportPoid(reportPoid) + 1;
            List<PortCallReportDtl> details = new ArrayList<>();
            for (PortCallReportDetailDto detailDto : dto.getDetails()) {
                details.add(PortCallReportDtl.builder()
                        .portCallReportPoid(reportPoid)
                        .detRowId(nextDetRowId++)
                        .portActivityTypePoid(detailDto.getPortActivityTypePoid())
                        .activityMandatory(detailDto.getActivityMandatory())
                        .createdBy(user.getUserId())
                        .build());
            }
            dtlRepository.saveAll(details);
        }

        return getReportById(hdr.getPortCallReportPoid());
    }

    @Override
    @Transactional
    public PortCallReportResponseDto updateReport(Long id, PortCallReportDto dto, Long userPoid, Long groupPoid) {
        log.info("Updating port call report id: {}", id);

        if (groupPoid == null) {
            throw new CustomException("Group POID is required", 400);
        }

        if (hdrRepository.existsByPortCallReportNameIgnoreCaseAndNotDeleted(dto.getPortCallReportName(), id)) {
            throw new ResourceAlreadyExistsException("Port call report name", dto.getPortCallReportName());
        }

        if (dto.getPortCallApplVesselType() != null && !dto.getPortCallApplVesselType().isEmpty()) {
            List<Long> validVesselTypePoids = vesselTypeRepository.findAllActive().stream()
                    .map(VesselType::getVesselTypePoid)
                    .toList();
            for (String vesselTypePoid : dto.getPortCallApplVesselType()) {
                if (!validVesselTypePoids.contains(Long.parseLong(vesselTypePoid))) {
                    throw new CustomException("Invalid vessel type POID: " + vesselTypePoid, 400);
                }
            }
        }

        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            for (PortCallReportDetailDto detail : dto.getDetails()) {
                if (detail.getPortActivityTypePoid() == null) {
                    throw new CustomException("Port activity type is required", 400);
                }
                if (!portActivityMasterRepository.existsById(detail.getPortActivityTypePoid())) {
                    throw new CustomException("Invalid activity type ID: " + detail.getPortActivityTypePoid(), 400);
                }
            }
        }

        User user = userRepository.findByUserPoid(userPoid)
                .orElseThrow(() -> new ResourceNotFoundException("User", "User Poid", userPoid));

        PortCallReportHdr hdr = hdrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Port call report", "Port Call Report Poid", id));

        hdr.setGroupPoid(groupPoid);
        hdr.setPortCallReportName(dto.getPortCallReportName());
        hdr.setPortCallApplVesselType(dto.getPortCallApplVesselType() != null ? String.join(",", dto.getPortCallApplVesselType()) : null);
        hdr.setActive(dto.getActive());
        hdr.setSeqno(dto.getSeqno());
        hdr.setRemarks(dto.getRemarks());
        hdr.setLastModifiedBy(user.getUserId());

        hdrRepository.save(hdr);

        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            for (PortCallReportDetailDto detailDto : dto.getDetails()) {
                ActionType action = detailDto.getActionType();

                if (action == null) {
                    continue;
                }

                if (action == ActionType.isCreated) {
                    Long nextDetRowId = dtlRepository.findMaxDetRowIdByPortCallReportPoid(id) + 1;
                    PortCallReportDtl newDetail = PortCallReportDtl.builder()
                            .portCallReportPoid(id)
                            .detRowId(nextDetRowId)
                            .portActivityTypePoid(detailDto.getPortActivityTypePoid())
                            .activityMandatory(detailDto.getActivityMandatory())
                            .createdBy(user.getUserId())
                            .build();
                    dtlRepository.save(newDetail);
                } else if (action == ActionType.isUpdated) {
                    dtlRepository.findById(new PortCallReportDtlId(id, detailDto.getDetRowId()))
                            .ifPresent(existing -> {
                                existing.setPortActivityTypePoid(detailDto.getPortActivityTypePoid());
                                existing.setActivityMandatory(detailDto.getActivityMandatory());
                                existing.setLastModifiedBy(user.getUserId());
                                dtlRepository.save(existing);
                            });
                } else if (action == ActionType.isDeleted) {
                    dtlRepository.deleteById(new PortCallReportDtlId(id, detailDto.getDetRowId()));
                }
            }
        }

        return getReportById(id);
    }

    @Override
    @Transactional
    public void deleteReport(Long id) {
        log.info("Deleting port call report id: {}", id);

        PortCallReportHdr hdr = hdrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Port call report"));

        hdr.setActive("N");
        hdr.setDeleted("Y");
        hdrRepository.save(hdr);
    }

    @Override
    public List<PortActivityResponseDto> getPortActivities(Long userPoid) {
        log.info("Fetching port activities");

        String sql = "{call PROC_PORT_ACTIVITIES_GET_LIST(?, ?)}";

        List<PortActivityResponseDto> activities = jdbcTemplate.execute((Connection conn) -> {
            try (CallableStatement cs = conn.prepareCall(sql)) {
                cs.setLong(1, userPoid);
                cs.registerOutParameter(2, Types.REF_CURSOR);
                cs.execute();

                List<PortActivityResponseDto> result = new ArrayList<>();
                try (ResultSet rs = (ResultSet) cs.getObject(2)) {
                    while (rs.next()) {
                        result.add(PortActivityResponseDto.builder()
                                .portActivityTypePoid(rs.getLong("PORT_ACTIVITY_TYPE_POID"))
                                .portActivityTypeCode(rs.getString("PORT_ACTIVITY_TYPE_CODE"))
                                .portActivityTypeName(rs.getString("PORT_ACTIVITY_TYPE_NAME"))
                                .portActivityTypeName2(rs.getString("PORT_ACTIVITY_TYPE_NAME2"))
                                .build());
                    }
                }
                return result;
            }
        });

        LovResponse portActivityLov = lovService.getLovList("PORT_ACTIVITY", null, null, null, null, null);
        if (portActivityLov != null && portActivityLov.getItems() != null) {
            Map<Long, LovItem> lovMap = portActivityLov.getItems().stream()
                    .collect(Collectors.toMap(LovItem::getPoid, item -> item));
            activities.forEach(activity -> activity.setPortActivityDet(lovMap.get(activity.getPortActivityTypePoid())));
        }

        return activities;
    }

    @Override
    public List<Map<String, Object>> getVesselTypes() {
        log.info("Fetching vessel types");

        List<VesselType> vesselTypes = vesselTypeRepository.findAllActive();

        return vesselTypes.stream()
                .map(vt -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("value", vt.getVesselTypeCode());
                    map.put("label", vt.getVesselTypeName());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
