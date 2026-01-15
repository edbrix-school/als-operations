package com.asg.operations.pdaRoRoVehicle.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.operations.commonlov.service.LovService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import com.asg.operations.pdaRoRoVehicle.dto.*;
import com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryHdr;
import com.asg.operations.pdaRoRoVehicle.repository.PdaRoroEntryDtlRepository;
import com.asg.operations.pdaRoRoVehicle.repository.PdaRoRoEntryHdrRepository;
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

import java.sql.Date;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class PdaRoRoEntryServiceImpl implements PdaRoRoEntryService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PdaRoRoEntryServiceImpl.class);

    private final PdaRoRoEntryHdrRepository hdrRepository;
    private final PdaRoroEntryDtlRepository dtlRepository;
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;
    private final com.asg.operations.commonlov.service.LovService lovService;
    private final com.asg.common.lib.service.PrintService printService;
    private final javax.sql.DataSource dataSource;
    private final LovService lovService;
    private final LoggingService loggingService;
    private final DocumentDeleteService documentDeleteService;

    @Override
    public PdaRoRoEntryHdrResponseDto createRoRoEntry(PdaRoroEntryHdrRequestDto request) {
        Map<String, Object> voyageDetails = getVoyageDetails(request.getVesselVoyagePoid());
        
        PdaRoRoEntryHdr entity = PdaRoRoEntryHdr.builder()
                .vesselVoyagePoid(request.getVesselVoyagePoid())
                .vesselName((String) voyageDetails.get("VESSEL_NAME"))
                .voyageNo((String) voyageDetails.get("VOYAGE_NO"))
                .transactionDate(LocalDate.now())
                .deleted("N")
                .companyPoid(UserContext.getCompanyPoid())
                .groupPoid(UserContext.getGroupPoid())
                .createdBy(getCurrentUser())
                .createdDate(LocalDateTime.now())
                .lastModifiedBy(getCurrentUser())
                .lastModifiedDate(LocalDateTime.now())
                .remarks(request.getRemarks())
                .build();

        hdrRepository.save(entity);
        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), entity.getTransactionPoid().toString());
        return mapToResponse(entity);
    }

    private Map<String, Object> getVoyageDetails(Long vesselVoyagePoid) {
        String sql = """
            SELECT VOYAGE.TRANSACTION_POID AS POID, VOYAGE.JOB_NO, 
                   VESSEL.VESSEL_NAME, VOYAGE.VOYAGE_NO, MLINE.LINE_CODE
            FROM SHIP_VOYAGE_HDR VOYAGE
            INNER JOIN SHIP_LINE_MASTER MLINE ON VOYAGE.LINE_POID = MLINE.LINE_POID
            INNER JOIN SHIP_VESSEL_MASTER VESSEL ON VESSEL.VESSEL_POID = VOYAGE.VESSEL_POID
            WHERE VOYAGE.TRANSACTION_POID = ?
            """;
        
        try {
            return jdbcTemplate.queryForMap(sql, vesselVoyagePoid);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new com.asg.operations.exceptions.ResourceNotFoundException(
                "Voyage details not found for vessel voyage POID: " + vesselVoyagePoid
            );
        }
    }

    @Override
    public PdaRoRoEntryHdrResponseDto updateRoRoEntry(Long transactionPoid, PdaRoroEntryHdrRequestDto request) {
        PdaRoRoEntryHdr entity = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new com.asg.operations.exceptions.ResourceNotFoundException(
                        "PDA Ro-Ro Entry not found with ID: " + transactionPoid));

        PdaRoRoEntryHdr oldEntity = new PdaRoRoEntryHdr();
        BeanUtils.copyProperties(entity, oldEntity);

        Map<String, Object> voyageDetails = getVoyageDetails(request.getVesselVoyagePoid());
        
        entity.setVesselVoyagePoid(request.getVesselVoyagePoid());
        entity.setVesselName((String) voyageDetails.get("VESSEL_NAME"));
        entity.setVoyageNo((String) voyageDetails.get("VOYAGE_NO"));
        entity.setRemarks(request.getRemarks());
        entity.setDeleted("N");
        entity.setLastModifiedBy(getCurrentUser());
        entity.setLastModifiedDate(LocalDateTime.now());
        loggingService.logChanges(oldEntity, entity, PdaRoRoEntryHdr.class, UserContext.getDocumentId(), entity.getTransactionPoid().toString(), LogDetailsEnum.MODIFIED, "TRANSACTION_POID");
        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public PdaRoRoEntryHdrResponseDto getRoRoEntry(Long transactionPoid) {
        PdaRoRoEntryHdr hdr = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new com.asg.operations.exceptions.ResourceNotFoundException(
                        "PDA Ro-Ro Entry not found with ID: " + transactionPoid));
        return mapToResponse(hdr);
    }

    private PdaRoRoEntryHdrResponseDto mapToResponse(PdaRoRoEntryHdr hdr) {

        List<PdaRoRoVehicleDtlResponseDto> dtls =
                dtlRepository.findByIdTransactionPoid(hdr.getTransactionPoid())
                        .stream()
                        .map(d -> PdaRoRoVehicleDtlResponseDto.builder()
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

        com.asg.operations.commonlov.dto.LovItem vesselVoyageLov = null;
        if (hdr.getVesselVoyagePoid() != null) {
            vesselVoyageLov = lovService.getLovItemByPoid(
                    hdr.getVesselVoyagePoid(),
                    "PDA_RORO_VESSEL_VOYAGE",
                    UserContext.getGroupPoid(),
                    UserContext.getCompanyPoid(),
                    UserContext.getUserPoid()
            );
        }

        return PdaRoRoEntryHdrResponseDto.builder()
                .transactionPoid(hdr.getTransactionPoid())
                .docRef(hdr.getDocRef())
                .transactionDate(hdr.getTransactionDate())
                .vesselVoyagePoid(hdr.getVesselVoyagePoid())
                .vesselVoyagePoidDetail(vesselVoyageLov)
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

    @Override
    public void deleteRoRoEntry(Long transactionPoid, @Valid DeleteReasonDto deleteReasonDto) {
        PdaRoRoEntryHdr hdr = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new com.asg.operations.exceptions.ResourceNotFoundException(
                        "PDA Ro-Ro Entry not found with ID: " + transactionPoid));

        documentDeleteService.deleteDocument(
                transactionPoid,
                "PDA_RORO_ENTRY_HDR",
                "TRANSACTION_POID",
                deleteReasonDto,
                hdr.getTransactionDate()
        );
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<RoRoVehicleListResponse> getRoRoVehicleList(
            Long groupPoid, Long companyPoid,
            GetAllRoRoVehicleFilterRequest filterRequest,
            int page, int size, String sort) {

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT PRE.TRANSACTION_POID, PRE.DELETED, PRE.COMPANY_POID, ");
        sqlBuilder.append("PRE.TRANSACTION_DATE, PRE.DOC_REF, SLM.LINE_NAME, ");
        sqlBuilder.append("SVH.VOYAGE_NO, SVM.VESSEL_NAME ");
        sqlBuilder.append("FROM PDA_RORO_ENTRY_HDR PRE ");
        sqlBuilder.append("INNER JOIN SHIP_VOYAGE_HDR SVH ON SVH.TRANSACTION_POID = PRE.VESSEL_VOYAGE_POID ");
        sqlBuilder.append("INNER JOIN SHIP_VESSEL_MASTER SVM ON SVM.VESSEL_POID = SVH.VESSEL_POID ");
        sqlBuilder.append("INNER JOIN SHIP_LINE_MASTER SLM ON SLM.LINE_POID = SVH.LINE_POID ");
        sqlBuilder.append("WHERE PRE.GROUP_POID = :groupPoid AND PRE.COMPANY_POID = :companyPoid ");

        if (filterRequest.getIsDeleted() != null && "N".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND (PRE.DELETED IS NULL OR PRE.DELETED != 'Y') ");
        } else if (filterRequest.getIsDeleted() != null && "Y".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND PRE.DELETED = 'Y' ");
        }

        if (StringUtils.hasText(filterRequest.getFrom())) {
            sqlBuilder.append("AND TRUNC(PRE.TRANSACTION_DATE) >= TO_DATE(:fromDate, 'YYYY-MM-DD') ");
        }
        if (StringUtils.hasText(filterRequest.getTo())) {
            sqlBuilder.append("AND TRUNC(PRE.TRANSACTION_DATE) <= TO_DATE(:toDate, 'YYYY-MM-DD') ");
        }

        List<String> filterConditions = new ArrayList<>();
        List<GetAllRoRoVehicleFilterRequest.FilterItem> validFilters = new ArrayList<>();
        if (filterRequest.getFilters() != null && !filterRequest.getFilters().isEmpty()) {
            for (GetAllRoRoVehicleFilterRequest.FilterItem filter : filterRequest.getFilters()) {
                if (StringUtils.hasText(filter.getSearchField()) && StringUtils.hasText(filter.getSearchValue())) {
                    validFilters.add(filter);
                    String columnName = mapSearchFieldToColumn(filter.getSearchField());
                    int paramIndex = validFilters.size() - 1;
                    filterConditions.add("LOWER(" + columnName + ") LIKE LOWER(:filterValue" + paramIndex + ")");
                }
            }
        }

        if (!filterConditions.isEmpty()) {
            String operator = "AND".equalsIgnoreCase(filterRequest.getOperator()) ? " AND " : " OR ";
            sqlBuilder.append("AND (").append(String.join(operator, filterConditions)).append(") ");
        }

        String orderBy = "ORDER BY PRE.TRANSACTION_DATE DESC";
        if (StringUtils.hasText(sort)) {
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

        String countSql = "SELECT COUNT(*) FROM (" + sqlBuilder.toString() + ")";
        Query query = entityManager.createNativeQuery(sqlBuilder.toString());
        Query countQuery = entityManager.createNativeQuery(countSql);

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

        if (!validFilters.isEmpty()) {
            for (int i = 0; i < validFilters.size(); i++) {
                GetAllRoRoVehicleFilterRequest.FilterItem filter = validFilters.get(i);
                String paramValue = "%" + filter.getSearchValue() + "%";
                query.setParameter("filterValue" + i, paramValue);
                countQuery.setParameter("filterValue" + i, paramValue);
            }
        }

        Long totalCount = ((Number) countQuery.getSingleResult()).longValue();
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<RoRoVehicleListResponse> dtos = results.stream()
                .map(this::mapToRoRoVehicleListResponse)
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(dtos, pageable, totalCount);
    }

    @Override
    public String uploadExcel(org.springframework.web.multipart.MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String docId = "110-162_1";
        ExcelConfig config = getExcelConfig(docId);
        
        jdbcTemplate.update("DELETE FROM " + config.tempTableName);
        
        List<List<Object>> rowsCollection = new ArrayList<>();

        try (org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(file.getInputStream())) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            
            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                List<Object> colCollection = new ArrayList<>();
                for (int cn = config.startColNumber - 1; cn <= config.endColNumber - 1; cn++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.getCell(cn, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    switch (cell.getCellType()) {
                        case NUMERIC -> colCollection.add(cell.getNumericCellValue());
                        case STRING -> colCollection.add(cell.getStringCellValue());
                        default -> colCollection.add(null);
                    }
                }
                rowsCollection.add(colCollection);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing Excel file: " + e.getMessage(), e);
        }

        saveImportedData(config.startRowNumber, rowsCollection, config.tempTableName);
        return "Successfully imported Excel data to temp table";
    }

    @Override
    public PdaRoroVehicleUploadResponse uploadVehicleDetails(PdaRoRoVehicleUploadRequest request) {
        hdrRepository.findById(request.getTransactionPoid())
                .orElseThrow(() -> new com.asg.operations.exceptions.ResourceNotFoundException(
                        "PDA Ro-Ro Entry not found with ID: " + request.getTransactionPoid()));

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
                                (rs, rowNum) -> PdaRoRoVehicleDtlResponseDto.builder()
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

        String status = (String) result.get("P_STATUS");
        List<PdaRoRoVehicleDtlResponseDto> vehicleDetails = (List<PdaRoRoVehicleDtlResponseDto>) result.get("OUTDATA");

        if (status != null && (status.contains("ERROR") || status.contains("WARNING"))) {
            return PdaRoroVehicleUploadResponse.builder()
                    .status(status)
                    .vehicleDetails(vehicleDetails)
                    .build();
        }

        List<PdaRoRoVehicleDtlResponseDto> savedDetails = null;
        if (vehicleDetails != null && !vehicleDetails.isEmpty()) {
            savedDetails = saveVehicleDetailsToTable(request.getTransactionPoid(), vehicleDetails);
        }

        return PdaRoroVehicleUploadResponse.builder()
                .status(status)
                .vehicleDetails(savedDetails != null ? savedDetails : vehicleDetails)
                .build();
    }

    private List<PdaRoRoVehicleDtlResponseDto> saveVehicleDetailsToTable(Long transactionPoid, List<PdaRoRoVehicleDtlResponseDto> vehicleDetails) {
        String sql = """
            INSERT INTO PDA_RORO_ENTRY_DTL 
            (TRANSACTION_POID, DET_ROW_ID, BL_NUMBER, SHIPPER, CONSIGNEE, 
             VIN_NUMBER, DESCRIPTION, BL_GWT, BL_CBM, PORT_OF_LOAD, AGENT,
             CREATED_BY, CREATED_DATE, LASTMODIFIED_BY, LASTMODIFIED_DATE)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE)
            """;

        List<Object[]> batchArgs = new ArrayList<>();
        List<PdaRoRoVehicleDtlResponseDto> savedDetails = new ArrayList<>();
        int detRowId = 1;

        for (PdaRoRoVehicleDtlResponseDto detail : vehicleDetails) {
            batchArgs.add(new Object[]{
                    transactionPoid,
                    detRowId,
                    detail.getBlNumber(),
                    detail.getShipper(),
                    detail.getConsignee(),
                    detail.getVinNumber(),
                    detail.getDescription(),
                    detail.getBlGwt(),
                    detail.getBlCbm(),
                    detail.getPortOfLoad(),
                    detail.getAgent(),
                    getCurrentUser(),
                    getCurrentUser()
            });

            savedDetails.add(PdaRoRoVehicleDtlResponseDto.builder()
                    .detRowId((long) detRowId++)
                    .blNumber(detail.getBlNumber())
                    .shipper(detail.getShipper())
                    .consignee(detail.getConsignee())
                    .vinNumber(detail.getVinNumber())
                    .description(detail.getDescription())
                    .blGwt(detail.getBlGwt())
                    .blCbm(detail.getBlCbm())
                    .portOfLoad(detail.getPortOfLoad())
                    .agent(detail.getAgent())
                    .build());
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
        return savedDetails;
    }

    @Override
    public String clearRoRoVehicleDetails(Long transactionPoid) {
        hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new com.asg.operations.exceptions.ResourceNotFoundException(
                        "PDA Ro-Ro Entry not found with ID: " + transactionPoid));
        
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

    private ExcelConfig getExcelConfig(String docId) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withSchemaName("PRODUCTION")
                .withProcedureName("PROC_GLOB_EXCEL_IMPORT_SHEETS")
                .declareParameters(
                        new SqlParameter("P_COMPANY_POID", Types.NUMERIC),
                        new SqlParameter("P_DOC_ID", Types.VARCHAR),
                        new SqlOutParameter("OUTDATA", OracleTypes.CURSOR),
                        new SqlOutParameter("P_STATUS", Types.VARCHAR)
                );

        Map<String, Object> result = jdbcCall.execute(
                new MapSqlParameterSource()
                        .addValue("P_COMPANY_POID", UserContext.getCompanyPoid())
                        .addValue("P_DOC_ID", docId)
        );

        String status = (String) result.get("P_STATUS");
        if (!"SUCCESS".equals(status)) {
            throw new RuntimeException("Failed to get Excel config: " + status);
        }

        List<Map<String, Object>> configs = (List<Map<String, Object>>) result.get("OUTDATA");
        if (configs == null || configs.isEmpty()) {
            throw new RuntimeException("No Excel configuration found for DOC_ID: " + docId);
        }

        Map<String, Object> configRow = configs.get(0);
        ExcelConfig config = new ExcelConfig();
        config.startRowNumber = ((Number) configRow.get("START_ROW_NUMBER")).intValue();
        config.startColNumber = ((Number) configRow.get("START_COL_NUMBER")).intValue();
        config.endColNumber = ((Number) configRow.get("END_COL_NUMBER")).intValue();
        config.tempTableName = (String) configRow.get("TEMP_TABLE_NAME");
        return config;
    }

    private void saveImportedData(int startRowNumber, List<List<Object>> rowsCollection, String tempTableName) {
        int rowNum = 0;
        
        for (List<Object> cols : rowsCollection) {
            rowNum++;
            if (startRowNumber <= rowNum) {
                StringBuilder insertQuery = new StringBuilder("INSERT INTO " + tempTableName + " VALUES (");
                for (Object col : cols) {
                    if (col == null) {
                        insertQuery.append("NULL,");
                    } else {
                        insertQuery.append("'").append(col.toString().replace("'", "''")).append("',");
                    }
                }
                insertQuery.setLength(insertQuery.length() - 1);
                insertQuery.append(")");
                
                jdbcTemplate.update(insertQuery.toString());
            }
        }
    }

    private static class ExcelConfig {
        int startRowNumber;
        int startColNumber;
        int endColNumber;
        String tempTableName;
    }

    private String mapSearchFieldToColumn(String searchField) {
        if (searchField == null) return null;
        
        String normalizedField = searchField.toUpperCase().replace("_", "");
        switch (normalizedField) {
            case "TRANSACTIONPOID": return "PRE.TRANSACTION_POID";
            case "DOCREF": return "PRE.DOC_REF";
            case "TRANSACTIONDATE": return "PRE.TRANSACTION_DATE";
            case "VOYAGENO": return "SVH.VOYAGE_NO";
            case "VESSELNAME": return "SVM.VESSEL_NAME";
            case "LINENAME": return "SLM.LINE_NAME";
            case "DELETED": return "PRE.DELETED";
            default: return "PRE." + searchField.toUpperCase().replace(" ", "_");
        }
    }

    private String mapSortFieldToColumn(String sortField) {
        if (sortField == null) return "PRE.TRANSACTION_DATE";
        
        String normalizedField = sortField.toUpperCase().replace("_", "");
        switch (normalizedField) {
            case "TRANSACTIONPOID": return "PRE.TRANSACTION_POID";
            case "DOCREF": return "PRE.DOC_REF";
            case "TRANSACTIONDATE": return "PRE.TRANSACTION_DATE";
            case "VOYAGENO": return "SVH.VOYAGE_NO";
            case "VESSELNAME": return "SVM.VESSEL_NAME";
            case "LINENAME": return "SLM.LINE_NAME";
            case "DELETED": return "PRE.DELETED";
            default: return "PRE." + sortField.toUpperCase().replace(" ", "_");
        }
    }

    private RoRoVehicleListResponse mapToRoRoVehicleListResponse(Object[] row) {
        RoRoVehicleListResponse dto = new RoRoVehicleListResponse();
        dto.setTransactionPoid(row[0] != null ? ((Number) row[0]).longValue() : null);
        dto.setDeleted(convertToString(row[1]));
        dto.setCompanyPoid(row[2] != null ? ((Number) row[2]).longValue() : null);
        dto.setTransactionDate(row[3] != null ? ((Timestamp) row[3]).toLocalDateTime().toLocalDate() : null);
        dto.setDocRef(convertToString(row[4]));
        dto.setLineName(convertToString(row[5]));
        dto.setVoyageNo(convertToString(row[6]));
        dto.setVesselName(convertToString(row[7]));
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
            return String.valueOf(value);
        }
        return value.toString();
    }

    public static String getCurrentUser() {
        return UserContext.getUserId() != null ? String.valueOf(UserContext.getUserId()) : "SYSTEM";
    }

    @Override
    public byte[] printTallySheet(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) throws Exception {
        logger.info("Generating Tally Sheet PDF for RoRo Entry: {}", transactionPoid);
        
        try {
            Map<String, Object> params = printService.buildBaseParams(transactionPoid, "110-162");
            params.put("SUB_RORO_DETAIL", printService.load("PDA/PDARoRoEntryTallySheetSubreport.jrxml"));

            net.sf.jasperreports.engine.JasperReport mainReport = printService.load("RORO/PDARoRoEntryTallySheetReport.jrxml");
            return printService.fillReportToPdf(mainReport, params, dataSource);
            
        } catch (RuntimeException e) {
            logger.error("Error generating Tally Sheet PDF for RoRo Entry: {}", transactionPoid, e);
            throw new RuntimeException("Tally Sheet PDF generation failed: " + e.getMessage(), e);
        }
    }
}
