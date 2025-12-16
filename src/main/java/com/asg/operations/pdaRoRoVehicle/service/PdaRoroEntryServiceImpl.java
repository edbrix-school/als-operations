package com.asg.operations.pdaRoRoVehicle.service;

import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.commonlov.dto.LovItem;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.pdaRoRoVehicle.dto.*;
import com.asg.operations.pdaRoRoVehicle.entity.PdaRoroEntryHdr;
import com.asg.operations.pdaRoRoVehicle.repository.PdaRoroEntryDtlRepository;
import com.asg.operations.pdaRoRoVehicle.repository.PdaRoroEntryHdrRepository;
import com.asg.operations.pdaporttariffmaster.dto.GetAllTariffFilterRequest;
import com.asg.operations.pdaporttariffmaster.dto.PdaPortTariffListResponse;
import com.asg.operations.pdaporttariffmaster.entity.PdaPortTariffHdr;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import oracle.jdbc.internal.OracleTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class PdaRoroEntryServiceImpl implements PdaRoroEntryService{

    private final PdaRoroEntryHdrRepository hdrRepository;
    private final PdaRoroEntryDtlRepository dtlRepository;
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    @Override
    public PdaRoroEntryHdrResponseDto createRoroEntry(PdaRoroEntryHdrRequestDto request) {

        PdaRoroEntryHdr entity = PdaRoroEntryHdr.builder()
                .vesselVoyagePoid(request.getVesselVoyagePoid())
                //.vesselName(request.getVesselName())
               // .voyageNo(request.getVoyageNo())
                .transactionDate(LocalDate.now())
                .deleted("N")
                .createdBy(getCurrentUser())
                .createdDate(LocalDateTime.now())
                .remarks(request.getRemarks())
                .build();

        hdrRepository.save(entity);
        return mapToResponse(entity);
    }

    @Override
    public PdaRoroEntryHdrResponseDto updateRoroEntry(Long transactionPoid,
                                PdaRoroEntryHdrRequestDto request) {

        PdaRoroEntryHdr entity = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new RuntimeException("RORO Entry not found"));

        entity.setVesselVoyagePoid(request.getVesselVoyagePoid());
        //entity.setVesselName(request.getVesselName());
        //entity.setVoyageNo(request.getVoyageNo());
        entity.setRemarks(request.getRemarks());
        entity.setDeleted("N");
        entity.setLastModifiedBy(getCurrentUser());
        entity.setLastModifiedDate(LocalDateTime.now());
        return  mapToResponse(entity);
    }

    @Override
    @Transactional
    public PdaRoroEntryHdrResponseDto getRoroEntry(Long transactionPoid) {

        PdaRoroEntryHdr hdr = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new RuntimeException("Not found"));

        return mapToResponse(hdr);
    }

    private PdaRoroEntryHdrResponseDto mapToResponse(PdaRoroEntryHdr hdr) {

        List<PdaRoroVehicleDtlResponseDto> dtls =
                dtlRepository.findByIdTransactionPoid(hdr.getTransactionPoid())
                        .stream()
                        .map(d -> PdaRoroVehicleDtlResponseDto.builder()
                                .detRowId(d.getId().getDetRowId())
                                .blNumber(d.getBlNumber())
                                .shipper(d.getShipper())
                                .consignee(d.getConsignee())
                                .vinNumber(d.getVinNumber())
                                .description(d.getDescription())
                                .blGwt(d.getBlGwt())
                                .blCbm(d.getBlCbm())
                                .portOfLoad(d.getPortOfLoad())
                                .agent(d.getAgent())
                                .createBy(d.getCreatedBy())
                                .lastModifyBy(d.getLastModifiedBy())
                                .createDate(d.getCreatedDate())
                                .lastModifyDate(d.getLastModifiedDate())
                                .build())
                        .toList();

        return PdaRoroEntryHdrResponseDto.builder()
                .transactionPoid(hdr.getTransactionPoid())
                .docRef(hdr.getDocRef())
                .transactionDate(hdr.getTransactionDate())
                .vesselVoyagePoid(hdr.getVesselVoyagePoid())
                .vesselName(hdr.getVesselName())
                .voyageNo(hdr.getVoyageNo())
                .remarks(hdr.getRemarks())
                .vehicleDetails(dtls)
                .createBy(hdr.getCreatedBy())
                .lastModifyBy(hdr.getLastModifiedBy())
                .createDate(hdr.getCreatedDate())
                .lastModifyDate(hdr.getLastModifiedDate())
                .build();
    }

    public PdaRoroVehicleUploadResponse uploadVehicleDetails(
            PdaRoroVehicleUploadRequest request) {

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("PROC_PDA_RORO_DTLS_UPLOAD")
                .declareParameters(
                        new SqlParameter("P_GROUP_POID", Types.NUMERIC),
                        new SqlParameter("P_COMPANY_POID", Types.NUMERIC),
                        new SqlParameter("P_TRANSACTION_POID", Types.NUMERIC),
                        new SqlParameter("P_VOYAGE_POID", Types.NUMERIC),
                        new SqlParameter("P_DOC_DATE", Types.DATE),
                        new SqlOutParameter("P_STATUS", Types.VARCHAR),
                        new SqlOutParameter(
                                "OUTDATA",
                                OracleTypes.CURSOR,
                                (rs, rowNum) -> PdaRoroVehicleDtlResponseDto.builder()
                                        .blNumber(rs.getString("BL_NUMBER"))
                                        .shipper(rs.getString("SHIPPER"))
                                        .consignee(rs.getString("CONSIGNEE"))
                                        .vinNumber(rs.getString("VIN_NUMBER"))
                                        .description(rs.getString("DESCRIPTION"))
                                        .blGwt(rs.getDouble("BL_GWT"))
                                        .blCbm(rs.getDouble("BL_CBM"))
                                        .portOfLoad(rs.getString("PORT_OF_LOAD"))
                                        .agent(rs.getString("AGENT"))
                                        .build()
                        )
                );

        Map<String, Object> result = jdbcCall.execute(
                new MapSqlParameterSource()
                        .addValue("P_GROUP_POID", UserContext.getGroupPoid())
                        .addValue("P_COMPANY_POID", UserContext.getCompanyPoid())
                        .addValue("P_TRANSACTION_POID", request.getTransactionPoid())
                        .addValue("P_VOYAGE_POID", request.getVoyagePoid())
                        .addValue("P_DOC_DATE", Date.valueOf(request.getDocDate()))
        );

        return PdaRoroVehicleUploadResponse.builder()
                .status((String) result.get("P_STATUS"))
                .vehicleDetails(
                        (List<PdaRoroVehicleDtlResponseDto>) result.get("OUTDATA")
                )
                .build();
    }

    @Override
    public void deleteRoRoEntry(Long transactionPoid) {

        PdaRoroEntryHdr hdr = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new RuntimeException("Not found"));

        hdr.setDeleted("Y");
        hdr.setLastModifiedBy(getCurrentUser());
        hdr.setLastModifiedDate(LocalDateTime.now());
        hdrRepository.save(hdr);

    }

    public String clearRoroVehicleDetails(Long transactionPoid) {

        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withSchemaName("PRODUCTION")
                .withProcedureName("PROC_PDA_RORO_DTLS_CLEAR")
                .declareParameters(
                        new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                        new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                        new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                        new SqlParameter("P_TRANSACTION_POID", Types.NUMERIC),
                        new SqlOutParameter("P_STATUS", Types.VARCHAR)
                );

        Map<String, Object> result = jdbcCall.execute(
                new MapSqlParameterSource()
                        .addValue("P_LOGIN_GROUP_POID", UserContext.getGroupPoid())
                        .addValue("P_LOGIN_USER_POID", UserContext.getUserPoid())
                        .addValue("P_LOGIN_COMPANY_POID", UserContext.getCompanyPoid())
                        .addValue("P_TRANSACTION_POID", transactionPoid)
        );

        return (String) result.get("P_STATUS");
    }

   /* public void uploadRoroExcel(MultipartFile file) throws Exception {

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        Set<String> vinSet = new HashSet<>();
        List<Object[]> batchArgs = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowIndex = 0;

            for (Row row : sheet) {

                if (rowIndex++ == 0) continue; // header

                String vin = getString(row.getCell(3));
                if (vin == null || vin.isBlank()) {
                    throw new RuntimeException("VIN missing at row " + rowIndex);
                }

                if (!vinSet.add(vin)) {
                    throw new RuntimeException("Duplicate VIN in Excel: " + vin);
                }

                batchArgs.add(new Object[]{
                        getString(row.getCell(0)), // BL_NUMBER
                        getString(row.getCell(1)), // SHIPPER
                        getString(row.getCell(2)), // CONSIGNEE
                        vin,                       // VIN_NUMBER
                        getString(row.getCell(4)), // DESCRIPTION
                        getDouble(row.getCell(5)), // BL_GWT
                        getDouble(row.getCell(6)), // BL_CBM
                        getString(row.getCell(7)), // PORT_OF_LOAD
                        getString(row.getCell(8))  // AGENT
                });
            }
        }

        String sql = """
        INSERT INTO PDA_RORO_ENTRY_LOAD_TEMP
        (BL_NUMBER, SHIPPER, CONSIGNEE, VIN_NUMBER,
         DESCRIPTION, BL_GWT, BL_CBM, PORT_OF_LOAD, AGENT)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }*/

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<RoRoVehicleListResponse> getRoRoVehicleList(
            Long groupPoid, Long companyPoid,
            GetAllRoRoVehicleFilterRequest filterRequest,
            int page, int size, String sort) {

        // Build dynamic SQL query
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT t.TRANSACTION_POID, t.DOC_REF, t.TRANSACTION_DATE, t.VESSEL_NAME, ");
        sqlBuilder.append("t.VOYAGE_NO, t.PERIOD_FROM, t.PERIOD_TO,  t.DELETED, ");
        sqlBuilder.append("t.CREATED_DATE, t.LASTMODIFIED_DATE ");
        sqlBuilder.append("FROM PDA_RORO_ENTRY_HDR t ");
        sqlBuilder.append("WHERE t.GROUP_POID = :groupPoid AND t.COMPANY_POID = :companyPoid ");

        // Apply isDeleted filter
        if (filterRequest.getIsDeleted() != null && "N".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND (t.DELETED IS NULL OR t.DELETED != 'Y') ");
        } else if (filterRequest.getIsDeleted() != null && "Y".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND t.DELETED = 'Y' ");
        }

        // Apply date range filters
        if (StringUtils.hasText(filterRequest.getFrom())) {
            sqlBuilder.append("AND TRUNC(t.TRANSACTION_DATE) >= TO_DATE(:fromDate, 'YYYY-MM-DD') ");
        }
        if (StringUtils.hasText(filterRequest.getTo())) {
            sqlBuilder.append("AND TRUNC(t.TRANSACTION_DATE) <= TO_DATE(:toDate, 'YYYY-MM-DD') ");
        }

        // Build filter conditions with sequential parameter indexing
        List<String> filterConditions = new java.util.ArrayList<>();
        List<GetAllRoRoVehicleFilterRequest.FilterItem> validFilters = new java.util.ArrayList<>();
        if (filterRequest.getFilters() != null && !filterRequest.getFilters().isEmpty()) {
            for (GetAllRoRoVehicleFilterRequest.FilterItem filter : filterRequest.getFilters()) {
                if (StringUtils.hasText(filter.getSearchField()) && StringUtils.hasText(filter.getSearchValue())) {
                    validFilters.add(filter);
                    String columnName = mapTariffSearchFieldToColumn(filter.getSearchField());
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
        String orderBy = "ORDER BY t.TRANSACTION_DATE DESC";
        if (StringUtils.hasText(sort)) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String sortField = mapTariffSortFieldToColumn(sortParts[0].trim());
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

        if (StringUtils.hasText(filterRequest.getFrom())) {
            query.setParameter("fromDate", filterRequest.getFrom());
            countQuery.setParameter("fromDate", filterRequest.getFrom());
        }
        if (StringUtils.hasText(filterRequest.getTo())) {
            query.setParameter("toDate", filterRequest.getTo());
            countQuery.setParameter("toDate", filterRequest.getTo());
        }

        // Set filter parameters using sequential indexing
        if (!validFilters.isEmpty()) {
            for (int i = 0; i < validFilters.size(); i++) {
                GetAllRoRoVehicleFilterRequest.FilterItem filter = validFilters.get(i);
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
        List<RoRoVehicleListResponse> dtos = results.stream()
                .map(this::mapToTariffListResponseDto)
                .collect(Collectors.toList());

        // Create page
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(dtos, pageable, totalCount);
    }

    private String mapTariffSearchFieldToColumn(String searchField) {
        if (searchField == null) {
            return null;
        }
        // Normalize the field name by removing underscores and converting to uppercase
        String normalizedField = searchField.toUpperCase().replace("_", "");

        switch (normalizedField) {
            case "DOCREF":
                return "t.DOC_REF";
            case "VOYAGENOS":
            case "VOYAGENO":
                return "t.VOYAGE_NO";
            case "VESSELNAMES":
            case "VESSELNAME":
            case "VESSEL":
                return "t.VESSEL_NAME";
            case "PERIODFROM":
                return "t.PERIOD_FROM";
            case "PERIODTO":
                return "t.PERIOD_TO";
            default:
                // Fallback: assume it's a direct column name from t table
                String columnName = searchField.toUpperCase().replace(" ", "_");
                return "t." + columnName;
        }
    }

    private String mapTariffSortFieldToColumn(String sortField) {
        if (sortField == null) {
            return "t.TRANSACTION_DATE";
        }
        String normalizedField = sortField.toUpperCase().replace("_", "");

        switch (normalizedField) {
            case "TRANSACTIONPOID":
                return "t.TRANSACTION_POID";
            case "DOCREF":
                return "t.DOC_REF";
            case "TRANSACTIONDATE":
                return "t.TRANSACTION_DATE";
            case "VOYAGENOS":
            case "VOYAGENO":
                return "t.VOYAGE_NO";
            case "VESSELNAMES":
            case "VESSELNAME":
            case "VESSEL":
                return "t.VESSEL_NAME";
            case "PERIODFROM":
                return "t.PERIOD_FROM";
            case "PERIODTO":
                return "t.PERIOD_TO";
            case "DELETED":
                return "t.DELETED";
            case "CREATEDDATE":
                return "t.CREATED_DATE";
            case "LASTMODIFIEDDATE":
                return "t.LASTMODIFIED_DATE";
            default:
                String columnName = sortField.toUpperCase().replace(" ", "_");
                return "t." + columnName;
        }
    }

    private RoRoVehicleListResponse mapToTariffListResponseDto(Object[] row) {
        RoRoVehicleListResponse dto = new RoRoVehicleListResponse();

        dto.setTransactionPoid(row[0] != null ? ((Number) row[0]).longValue() : null);
        dto.setDocRef(convertToString(row[1]));
        dto.setTransactionDate(row[2] != null ? ((Timestamp) row[2]).toLocalDateTime().toLocalDate() : null);
        dto.setLineName(convertToString(row[3]));
        dto.setVoyageNo(convertToString(row[4]));
        dto.setVesselName(convertToString(row[5]));
        dto.setPeriodFrom(row[6] != null ? ((Timestamp) row[6]).toLocalDateTime().toLocalDate() : null);
        dto.setPeriodTo(row[7] != null ? ((Timestamp) row[6]).toLocalDateTime().toLocalDate() : null);
        dto.setDeleted(convertToString(row[8]));
        dto.setCreatedDate(row[9] != null ? ((Timestamp) row[9]).toLocalDateTime() : null);
        dto.setLastModifiedDate(row[10] != null ? ((Timestamp) row[10]).toLocalDateTime() : null);
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


    public static String getCurrentUser() {
        return UserContext.getUserId() != null ? String.valueOf(UserContext.getUserId()) : "SYSTEM";
    }
}
