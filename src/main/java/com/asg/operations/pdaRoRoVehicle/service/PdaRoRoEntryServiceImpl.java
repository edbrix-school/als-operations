package com.asg.operations.pdaRoRoVehicle.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.utility.DateUtil;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.enums.LogDetailsEnum;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import com.asg.operations.pdaRoRoVehicle.dto.*;
import com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryHdr;
import com.asg.operations.pdaRoRoVehicle.repository.PdaRoroEntryDtlRepository;
import com.asg.operations.pdaRoRoVehicle.repository.PdaRoRoEntryHdrRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import oracle.jdbc.internal.OracleTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor

public class PdaRoRoEntryServiceImpl implements PdaRoRoEntryService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PdaRoRoEntryServiceImpl.class);

    private final PdaRoRoEntryHdrRepository hdrRepository;
    private final PdaRoroEntryDtlRepository dtlRepository;
    private final JdbcTemplate jdbcTemplate;
    private final com.asg.operations.commonlov.service.LovService lovService;
    private final com.asg.common.lib.service.PrintService printService;
    private final javax.sql.DataSource dataSource;
    private final LoggingService loggingService;
    private final DocumentDeleteService documentDeleteService;
    private final DocumentSearchService documentSearchService;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public PdaRoRoEntryHdrResponseDto createRoRoEntry(PdaRoroEntryHdrRequestDto request) {
        Map<String, Object> voyageDetails = getVoyageDetails(request.getVesselVoyagePoid());

        LocalDate transactionDate = request.getTransactionDate() != null
                ? request.getTransactionDate()
                : DateUtil.getCurrentDateInUserTimeZone();
        PdaRoRoEntryHdr entity = PdaRoRoEntryHdr.builder()
                .vesselVoyagePoid(request.getVesselVoyagePoid())
                .vesselName((String) voyageDetails.get("VESSEL_NAME"))
                .voyageNo((String) voyageDetails.get("VOYAGE_NO"))
                .transactionDate(transactionDate)
                .deleted("N")
                .companyPoid(UserContext.getCompanyPoid())
                .groupPoid(UserContext.getGroupPoid())
                .remarks(request.getRemarks())
                .build();

        hdrRepository.save(entity);
        entityManager.flush();
        entityManager.refresh(entity);
        String key = entity.getTransactionPoid().toString();
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), key, String.format("%s %s", LogDetailsEnum.CREATED, entity.getDocRef()));
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

        LocalDate transactionDate = request.getTransactionDate() != null
                ? request.getTransactionDate()
                : DateUtil.getCurrentDateInUserTimeZone();
        entity.setVesselVoyagePoid(request.getVesselVoyagePoid());
        entity.setVesselName((String) voyageDetails.get("VESSEL_NAME"));
        entity.setVoyageNo((String) voyageDetails.get("VOYAGE_NO"));
        entity.setTransactionDate(transactionDate);
        entity.setRemarks(request.getRemarks());
        entity.setDeleted("N");
        entity = hdrRepository.save(entity);
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
    public Map<String, Object> getRoRoVehicleList(
            String documentId, FilterRequestDto filterRequestDto, Pageable pageable, LocalDate periodFrom, LocalDate periodTo) {

        String operator = documentSearchService.resolveOperator(filterRequestDto);
        String isDeleted = documentSearchService.resolveIsDeleted(filterRequestDto);
        List<FilterDto> filters = documentSearchService.resolveDateFilters(filterRequestDto,"TRANSACTION_DATE", periodFrom, periodTo);

        RawSearchResult raw = documentSearchService.search(documentId, filters, operator, pageable, isDeleted,
                "DOC_REF",
                "TRANSACTION_POID");

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
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
            throw new ValidationException(status);
        }


        List<PdaRoRoVehicleDtlResponseDto> savedDetails = null;
        if (vehicleDetails != null && !vehicleDetails.isEmpty()) {
            savedDetails = saveVehicleDetailsToTable(request.getTransactionPoid(), vehicleDetails);
        }

        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), request.getTransactionPoid().toString(), "Vehicle details uploaded successfully");
        return PdaRoroVehicleUploadResponse.builder()
                .status(status)
                .vehicleDetails(savedDetails != null ? savedDetails : vehicleDetails)
                .build();
    }

    private List<PdaRoRoVehicleDtlResponseDto> saveVehicleDetailsToTable(Long transactionPoid, List<PdaRoRoVehicleDtlResponseDto> vehicleDetails) {
        List<PdaRoRoVehicleDtlResponseDto> savedDetails = new ArrayList<>();
        int detRowId = 1;

        for (PdaRoRoVehicleDtlResponseDto detail : vehicleDetails) {
            com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryDtlId id = 
                new com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryDtlId(transactionPoid, (long) detRowId);
            
            com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryDtl entity = 
                dtlRepository.findById(id).orElse(null);
            
            boolean isUpdate = entity != null;
            com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryDtl oldEntity = null;
            
            if (isUpdate) {
                oldEntity = new com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryDtl();
                BeanUtils.copyProperties(entity, oldEntity);
            } else {
                entity = new com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryDtl();
                entity.setId(id);
            }
            
            entity.setBlNumber(detail.getBlNumber());
            entity.setShipper(detail.getShipper());
            entity.setConsignee(detail.getConsignee());
            entity.setVinNumber(detail.getVinNumber());
            entity.setDescription(detail.getDescription());
            entity.setBlGwt(detail.getBlGwt());
            entity.setBlCbm(detail.getBlCbm());
            entity.setPortOfLoad(detail.getPortOfLoad());
            entity.setAgent(detail.getAgent());
            
            entity = dtlRepository.save(entity);
            
            if (isUpdate && oldEntity != null) {
                String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", 
                    entity.getId().getTransactionPoid(), entity.getId().getDetRowId());
                loggingService.createLog(oldEntity, entity, com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryDtl.class, 
                    UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
            } else if (!isUpdate) {
                String logDetail = String.format("Row Created on [PDA RoRo Vehicle Details] with detRowId: %s", entity.getId().getDetRowId());
                loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
            }

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

        return savedDetails;
    }

    @Override
    public String clearRoRoVehicleDetails(Long transactionPoid) {
        hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new com.asg.operations.exceptions.ResourceNotFoundException(
                        "PDA Ro-Ro Entry not found with ID: " + transactionPoid));
        
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
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

        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(),"Vehicle details cleared successfully");
        return (String) result.get("P_STATUS");
    }

    private ExcelConfig getExcelConfig(String docId) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
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

        Map<String, Object> configRow = configs.getFirst();
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
