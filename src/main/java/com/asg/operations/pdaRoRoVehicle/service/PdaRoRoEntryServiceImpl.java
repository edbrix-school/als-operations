package com.asg.operations.pdaRoRoVehicle.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.exception.ValidationException;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.PrintService;
import com.asg.common.lib.utility.DateUtil;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.operations.commonlov.dto.LovItem;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.pdaRoRoVehicle.dto.*;
import com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryDtl;
import com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryDtlId;
import com.asg.operations.pdaRoRoVehicle.entity.PdaRoRoEntryHdr;
import com.asg.operations.pdaRoRoVehicle.repository.PdaRoRoEntryHdrRepository;
import com.asg.operations.pdaRoRoVehicle.repository.PdaRoroEntryDtlRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JasperReport;
import oracle.jdbc.internal.OracleTypes;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdaRoRoEntryServiceImpl implements PdaRoRoEntryService {

    private static final Logger logger = LoggerFactory.getLogger(PdaRoRoEntryServiceImpl.class);

    private final PdaRoRoEntryHdrRepository hdrRepository;
    private final PdaRoroEntryDtlRepository dtlRepository;
    private final JdbcTemplate jdbcTemplate;
    private final LovService lovService;
    private final PrintService printService;
    private final DataSource dataSource;
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

        if (request.getVehicleDetails() != null && !request.getVehicleDetails().isEmpty()) {
            long detRowId = 1L;
            for (PdaRoRoVehicleDtlRequestDto detail : request.getVehicleDetails()) {
                PdaRoRoEntryDtlId dtlId = new PdaRoRoEntryDtlId(entity.getTransactionPoid(), detRowId++);
                PdaRoRoEntryDtl dtl = new PdaRoRoEntryDtl();
                dtl.setId(dtlId);
                applyDetailFields(dtl, detail);
                dtlRepository.save(dtl);
            }
        }

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
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Voyage details not found for vessel voyage POID: " + vesselVoyagePoid);
        }
    }

    @Override
    @Transactional
    public PdaRoRoEntryHdrResponseDto updateRoRoEntry(Long transactionPoid, PdaRoroEntryHdrRequestDto request) {
        PdaRoRoEntryHdr entity = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("PDA Ro-Ro Entry not found with ID: " + transactionPoid));

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

        if (request.getVehicleDetails() != null) {
            List<PdaRoRoEntryDtl> existingList = dtlRepository.findByIdTransactionPoid(transactionPoid);
            Map<Long, PdaRoRoEntryDtl> existingMap = existingList.stream()
                    .collect(Collectors.toMap(d -> d.getId().getDetRowId(), d -> d));

            Set<Long> incomingDetRowIds = request.getVehicleDetails().stream()
                    .filter(d -> d.getDetRowId() != null)
                    .map(PdaRoRoVehicleDtlRequestDto::getDetRowId)
                    .collect(Collectors.toSet());

            for (PdaRoRoEntryDtl existing : existingList) {
                if (!incomingDetRowIds.contains(existing.getId().getDetRowId())) {
                    dtlRepository.delete(existing);
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                            String.format("Row Deleted on [PDA RoRo Vehicle Details] with detRowId: %s", existing.getId().getDetRowId()));
                }
            }

            long nextDetRowId = existingMap.isEmpty() ? 1L
                    : existingMap.keySet().stream().max(Long::compareTo).get() + 1;

            for (PdaRoRoVehicleDtlRequestDto detail : request.getVehicleDetails()) {
                if (detail.getDetRowId() != null && existingMap.containsKey(detail.getDetRowId())) {
                    PdaRoRoEntryDtl existing = existingMap.get(detail.getDetRowId());
                    PdaRoRoEntryDtl oldDtl = new PdaRoRoEntryDtl();
                    BeanUtils.copyProperties(existing, oldDtl);
                    applyDetailFields(existing, detail);
                    dtlRepository.save(existing);
                    String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s",
                            transactionPoid, existing.getId().getDetRowId());
                    loggingService.createLog(oldDtl, existing, PdaRoRoEntryDtl.class,
                            UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
                } else {
                    PdaRoRoEntryDtlId dtlId = new PdaRoRoEntryDtlId(transactionPoid, nextDetRowId++);
                    PdaRoRoEntryDtl dtl = new PdaRoRoEntryDtl();
                    dtl.setId(dtlId);
                    applyDetailFields(dtl, detail);
                    dtlRepository.save(dtl);
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                            String.format("Row Created on [PDA RoRo Vehicle Details] with detRowId: %s", dtlId.getDetRowId()));
                }
            }
        }

        return mapToResponse(entity);
    }

    @Override
    @Transactional
    public PdaRoRoEntryHdrResponseDto getRoRoEntry(Long transactionPoid) {
        PdaRoRoEntryHdr hdr = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("PDA Ro-Ro Entry not found with ID: " + transactionPoid));
        return mapToResponse(hdr);
    }

    private PdaRoRoEntryHdrResponseDto mapToResponse(PdaRoRoEntryHdr hdr) {
        List<PdaRoRoVehicleDtlResponseDto> dtls = dtlRepository.findByIdTransactionPoid(hdr.getTransactionPoid())
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

        LovItem vesselVoyageLov = null;
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
                .orElseThrow(() -> new ResourceNotFoundException("PDA Ro-Ro Entry not found with ID: " + transactionPoid));

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
        List<FilterDto> filters = documentSearchService.resolveDateFilters(filterRequestDto, "TRANSACTION_DATE", periodFrom, periodTo);

        RawSearchResult raw = documentSearchService.search(documentId, filters, operator, pageable, isDeleted,
                "DOC_REF",
                "TRANSACTION_POID");

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());
        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Override
    public String uploadExcel(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String docId = "110-162_1";
        ExcelConfig config = getExcelConfig(docId);

        jdbcTemplate.update("DELETE FROM " + config.tempTableName);

        List<List<Object>> rowsCollection = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            if (workbook == null) {
                throw new RuntimeException("Excel Workbook not able to open...");
            }

            Sheet sheet = config.excelSheetName != null
                    ? workbook.getSheet(config.excelSheetName)
                    : workbook.getSheetAt(0);

            if (sheet == null) {
                String sheetName = config.excelSheetName != null ? config.excelSheetName : "at index 0";
                throw new RuntimeException("Excel sheet " + sheetName + " not able to open...");
            }

            for (Row row : sheet) {
                List<Object> colCollection = new ArrayList<>();
                for (int cn = config.startColNumber - 1; cn <= config.endColNumber - 1; cn++) {
                    Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
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
                .orElseThrow(() -> new ResourceNotFoundException("PDA Ro-Ro Entry not found with ID: " + request.getTransactionPoid()));

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

        return PdaRoroVehicleUploadResponse.builder()
                .status(status)
                .vehicleDetails(vehicleDetails)
                .build();
    }

    private void applyDetailFields(PdaRoRoEntryDtl dtl, PdaRoRoVehicleDtlRequestDto src) {
        dtl.setBlNumber(src.getBlNumber());
        dtl.setShipper(src.getShipper());
        dtl.setConsignee(src.getConsignee());
        dtl.setVinNumber(src.getVinNumber());
        dtl.setDescription(src.getDescription());
        dtl.setBlGwt(src.getBlGwt());
        dtl.setBlCbm(src.getBlCbm());
        dtl.setPortOfLoad(src.getPortOfLoad());
        dtl.setAgent(src.getAgent());
    }

    @Override
    public String clearRoRoVehicleDetails(Long transactionPoid) {
        hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("PDA Ro-Ro Entry not found with ID: " + transactionPoid));

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

        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), "Vehicle details cleared successfully");
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
        config.excelSheetName = (String) configRow.get("EXCEL_SHEET_NAME");
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
        String excelSheetName;
    }

    @Override
    public byte[] printTallySheet(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) throws Exception {
        logger.info("Generating Tally Sheet PDF for RoRo Entry: {}", transactionPoid);
        try {
            Map<String, Object> params = printService.buildBaseParams(transactionPoid, "110-162");
            params.put("SUB_RORO_DETAIL", printService.load("PDA/PDARoRoEntryTallySheetSubreport.jrxml"));

            JasperReport mainReport = printService.load("RORO/PDARoRoEntryTallySheetReport.jrxml");
            return printService.fillReportToPdf(mainReport, params, dataSource);
        } catch (RuntimeException e) {
            logger.error("Error generating Tally Sheet PDF for RoRo Entry: {}", transactionPoid, e);
            throw new RuntimeException("Tally Sheet PDF generation failed: " + e.getMessage(), e);
        }
    }
}
