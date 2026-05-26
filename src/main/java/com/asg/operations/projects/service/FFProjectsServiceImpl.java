package com.asg.operations.projects.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.LovGetListDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.dto.request.LogRequestDto;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.LovDataService;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.projectjob.util.ProjectJobMapper;
import com.asg.operations.projects.dto.*;
import com.asg.operations.projects.entity.FFProjectsChargesDtl;
import com.asg.operations.projects.entity.FFProjectsCtrlSheetDtl;
import com.asg.operations.projects.entity.FFProjectsHdr;
import com.asg.operations.projects.projection.*;
import com.asg.operations.projects.repository.FFProjectsChargesDtlRepository;
import com.asg.operations.projects.repository.FFProjectsCtrlSheetDtlRepository;
import com.asg.operations.projects.repository.FFProjectsHdrRepository;
import com.asg.operations.projects.repository.FFProjectsStoredProcRepository;
import com.asg.operations.projects.repository.FreightJobProjectionRepository;
import com.asg.operations.projects.util.ProjectMapper;
import com.asg.operations.projectjob.entity.*;
import com.asg.operations.projectjob.repository.*;
import com.asg.operations.crew.dto.ValidationError;
import com.asg.operations.exceptions.FFValidationException;
import jakarta.persistence.EntityManager;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class FFProjectsServiceImpl implements FFProjectsService {

    private static final DateTimeFormatter EXCEL_DATE_FORMAT =
            new java.time.format.DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("dd-MMM-yyyy")
                    .toFormatter(Locale.ENGLISH);
    private static final DateTimeFormatter EXCEL_DATE_FORMAT_SHORT =
            new java.time.format.DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("d-MMM-yyyy")
                    .toFormatter(Locale.ENGLISH);

    // Column index → expected header text (normalised: uppercase, newlines → space)
    private static final Map<Integer, String> REQUIRED_COLUMN_HEADERS;
    static {
        REQUIRED_COLUMN_HEADERS = new LinkedHashMap<>();
        REQUIRED_COLUMN_HEADERS.put(1,  "MODE");
        REQUIRED_COLUMN_HEADERS.put(8,  "BAHRAIN ETA/ATA");
        REQUIRED_COLUMN_HEADERS.put(9,  "VESSEL");
        REQUIRED_COLUMN_HEADERS.put(10, "POL");
        REQUIRED_COLUMN_HEADERS.put(12, "DESCRIPTION");
        REQUIRED_COLUMN_HEADERS.put(15, "NO OF PKGS");
        REQUIRED_COLUMN_HEADERS.put(16, "WEIGHT");
        REQUIRED_COLUMN_HEADERS.put(17, "VOLUME");
        REQUIRED_COLUMN_HEADERS.put(23, "DELIVERY DATE");
    }

    @Data
    private static class CtrlSheetExcelRow {
        private int rowNum;
        private String freightType;
        // common
        private LocalDate etaAta;
        private LocalDate arrivalDate;
        private Double weight;
        private Double cbm;
        private String description;
        private String duplicateKey;
        // AIR
        private Long origin;        // from POL col via FF_AIRPORTS
        private Long destination;   // no Excel column → null
        private Long carrierPoid;   // from VESSEL col via AIRLINE
        private Double noOfPackages;
        private LocalDate etd;      // no Excel column → null
        // SEA
        private String pol;         // from POL col via PORT_MASTER (stored as POID string)
        private String pod;         // no Excel column → null
        private LocalDate sailDate; // no Excel column → null
        private Long line;          // no Excel column → null
        // ROAD
        private String truckNumber; // no Excel column → null
        // raw values for LOV resolution (transient, not saved)
        private String polRaw;      // raw text from POL col (col 10)
        private String vesselRaw;   // raw text from VESSEL col (col 9)
    }

    private final FFProjectsHdrRepository projectsHdrRepository;
    private final FFProjectsChargesDtlRepository projectsChargesDtlRepository;
    private final FFProjectsCtrlSheetDtlRepository projectsCtrlSheetDtlRepository;
    private final FFProjectsStoredProcRepository projectsStoredProcRepository;
    private final FreightJobProjectionRepository freightJobProjectionRepository;
    private final FFManifestHdrRepository manifestHdrRepository;
    private final FFManifestAirPkgDtlRepository airPkgRepository;
    private final FFManifestContainerDtlRepository containerRepository;
    private final FFManifestTruckDtlRepository truckRepository;
    private final FFManifestChargesDtlRepository manifestChargesRepository;
    private final FFManifestBayanDtlRepository bayanRepository;
    private final com.asg.operations.finaldisbursementaccount.repository.ShipVesselMasterRepository shipVesselMasterRepository;
    private final LoggingService loggingService;
    private final ProjectMapper mapper;
    private final DocumentSearchService documentSearchService;
    private final LovDataService lovDataService;
    private final EntityManager entityManager;
    private final ProjectJobMapper projectJobMapper;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> listProjectsWithFilters(String documentId, FilterRequestDto filterRequest, Pageable pageable, LocalDate periodFrom, LocalDate periodTo) {
        String operator = documentSearchService.resolveOperator(filterRequest);
        String isDeleted = documentSearchService.resolveIsDeleted(filterRequest);

        List<FilterDto> filters = documentSearchService.resolveDateFilters(filterRequest, "TRANSACTION_DATE",
                periodFrom, periodTo);

        RawSearchResult raw = documentSearchService.search(documentId, filters, operator, pageable, isDeleted,
                "DOC_REF", "TRANSACTION_POID");

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Override
    @Transactional(readOnly = true)
    public FFProjectsResponse getProjectById(Long transactionPoid) {
        FFProjectsHdr projectsHdr = projectsHdrRepository.findByTransactionPoidAndDeleted(transactionPoid, "N")
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + transactionPoid));

        List<FFProjectsChargesDtl> chargeDetails = projectsChargesDtlRepository.findByTransactionPoid(transactionPoid);
        List<FFProjectsCtrlSheetDtl> ctrlSheetDetails = projectsCtrlSheetDtlRepository.findByTransactionPoidAndActive(transactionPoid, "Y");

        return  mapper.mapToResponse(projectsHdr, chargeDetails, ctrlSheetDetails);
    }

    @Override
    @Transactional
    public FFProjectsResponse createProject(FFProjectsRequest request) {

        FFProjectsHdr projectsHdr = ProjectMapper.buildCreateProject(request);

        projectsHdr = projectsHdrRepository.save(projectsHdr);
        entityManager.flush();
        entityManager.refresh(projectsHdr);
        Long transactionPoid = projectsHdr.getTransactionPoid();
        
        String key = transactionPoid.toString();
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), key, String.format("%s %s", LogDetailsEnum.CREATED.getDescription(), projectsHdr.getDocRef()));


        if (request.getChargeDetails() != null && !request.getChargeDetails().isEmpty()) {
            for (FFProjectsChargesDetailRequest chargeReq : request.getChargeDetails()) {
                if ("ISCREATED".equalsIgnoreCase(chargeReq.getActionType())) {
                    List<FFProjectsChargesDtl> existingCharges = projectsChargesDtlRepository.findByTransactionPoid(transactionPoid);
                    long nextDetRowId = existingCharges.stream().mapToLong(FFProjectsChargesDtl::getDetRowId).max().orElse(0L) + 1;
                    
                    FFProjectsChargesDtl detail = ProjectMapper.buildCreateCharge(chargeReq, transactionPoid, nextDetRowId);
                    projectsChargesDtlRepository.save(detail);
                    String logDetails = String.format("Charge Detail Row - Det Row ID: %s", detail.getDetRowId());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                            logDetails);
                }
            }
        }

        if (request.getControlSheetDetails() != null && !request.getControlSheetDetails().isEmpty()) {
            for (FFProjectsCtrlSheetDetailRequest ctrlReq : request.getControlSheetDetails()) {
                if ("ISCREATED".equalsIgnoreCase(ctrlReq.getActionType())) {
                    List<FFProjectsCtrlSheetDtl> existingSheets = projectsCtrlSheetDtlRepository.findByTransactionPoid(transactionPoid);
                    long nextDetRowId = existingSheets.stream().mapToLong(FFProjectsCtrlSheetDtl::getDetRowId).max().orElse(0L) + 1;
                    
                    FFProjectsCtrlSheetDtl detail = ProjectMapper.createCtrlSheet(ctrlReq, transactionPoid, nextDetRowId);
                    projectsCtrlSheetDtlRepository.save(detail);
                    String logDetails = String.format("Control Sheet Row - Det Row ID: %s, Freight Type: %s", detail.getDetRowId(), detail.getFreightType());
                    loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                            logDetails);
                }
            }
        }

        return getProjectById(transactionPoid);
    }

    @Override
    @Transactional
    public FFProjectsResponse updateProject(Long transactionPoid, FFProjectsRequest request) {
        FFProjectsHdr existingProjectsHdr = projectsHdrRepository.findByTransactionPoidAndDeleted(transactionPoid, "N")
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + transactionPoid));

        FFProjectsHdr backUpProjectsHdr = new FFProjectsHdr();
        BeanUtils.copyProperties(existingProjectsHdr, backUpProjectsHdr);

        ProjectMapper.applyUpdate(request, existingProjectsHdr);

        loggingService.logChanges(backUpProjectsHdr, existingProjectsHdr, FFProjectsHdr.class, UserContext.getDocumentId(),
                transactionPoid.toString(), LogDetailsEnum.MODIFIED, "TRANSACTION_POID");

        projectsHdrRepository.save(existingProjectsHdr);

        if (request.getChargeDetails() != null && !request.getChargeDetails().isEmpty()) {
            updateProjectCharges(request.getChargeDetails(), transactionPoid);
        }

        if (request.getControlSheetDetails() != null && !request.getControlSheetDetails().isEmpty()) {
            updateProjectControlSheets(request.getControlSheetDetails(), transactionPoid);
        }

        return getProjectById(transactionPoid);
    }

    @Override
    @Transactional
    public void deleteProject(Long transactionPoid, DeleteReasonDto deleteReasonDto) {
        FFProjectsHdr projectsHdr = projectsHdrRepository.findByTransactionPoidAndDeleted(transactionPoid, "N")
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + transactionPoid));

        projectsHdr.setDeleted("Y");
        projectsHdr.setLastModifiedBy(UserContext.getUserName());
        projectsHdr.setLastModifiedDate(LocalDateTime.now());
        projectsHdrRepository.save(projectsHdr);

        loggingService.createLogSummaryEntry(LogDetailsEnum.DELETED, UserContext.getDocumentId(), transactionPoid.toString());
    }

    @Override
    public Map<String, Object> loadQuotationDetails(Long quotationPoid, String quoteFlag) {
        // Validate and normalize quoteFlag parameter
        String normalizedQuoteFlag = (quoteFlag != null && "Y".equalsIgnoreCase(quoteFlag.trim())) ? "Y" : "N";
        return projectsStoredProcRepository.loadProjectsQuotation(quotationPoid, normalizedQuoteFlag);
    }

    @Override
    public Map<String, Object> loadJobDetails(Long transactionPoid) {
        return projectsStoredProcRepository.loadProjectsJobs(transactionPoid);
    }

    @Override
    @Transactional
    public List<FFProjectsCtrlSheetDetailResponse> batchControlSheetOperations(Long transactionPoid, List<FFProjectsCtrlSheetDetailRequest> requests) {
        FFProjectsHdr projectsHdr = projectsHdrRepository.findByTransactionPoidAndDeleted(transactionPoid, "N")
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + transactionPoid));

        updateProjectControlSheets(requests, transactionPoid);
        
        List<FFProjectsCtrlSheetDtl> details = projectsCtrlSheetDtlRepository.findByTransactionPoidAndActive(transactionPoid, "Y");
        return details.stream()
                .map(mapper::mapCtrlSheetDetailToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FFProjectsCtrlSheetDetailResponse> getControlSheetsByProject(Long transactionPoid, String freightType) {
        List<FFProjectsCtrlSheetDtl> details;
        
        if (freightType != null && !freightType.isEmpty()) {
            details = projectsCtrlSheetDtlRepository.findByTransactionPoidAndFreightTypeAndActive(transactionPoid, freightType, "Y");
        } else {
            details = projectsCtrlSheetDtlRepository.findByTransactionPoidAndActive(transactionPoid, "Y");
        }

        return details.stream()
                .map(mapper::mapCtrlSheetDetailToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FFProjectsCtrlSheetDetailResponse createControlSheet(Long transactionPoid, FFProjectsCtrlSheetDetailRequest request) {
        FFProjectsHdr projectsHdr = projectsHdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + transactionPoid));
        
        if ("Y".equals(projectsHdr.getDeleted())) {
            throw new ResourceNotFoundException("Project not found with ID: " + transactionPoid);
        }

        List<FFProjectsCtrlSheetDtl> existingSheets = projectsCtrlSheetDtlRepository.findByTransactionPoid(transactionPoid);
        long nextDetRowId = existingSheets.stream().mapToLong(FFProjectsCtrlSheetDtl::getDetRowId).max().orElse(0L) + 1;

        FFProjectsCtrlSheetDtl detail = ProjectMapper.createCtrlSheet(request, transactionPoid, nextDetRowId);
        projectsCtrlSheetDtlRepository.save(detail);

        String logDetails = String.format("Control Sheet Row - Det Row ID: %s, Freight Type: %s", detail.getDetRowId(), detail.getFreightType());
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetails);

        return mapper.mapCtrlSheetDetailToResponse(detail);
    }

    @Override
    @Transactional
    public FFProjectsCtrlSheetDetailResponse updateControlSheet(Long transactionPoid, Long detRowId, FFProjectsCtrlSheetDetailRequest request) {
        projectsHdrRepository.findByTransactionPoidAndDeleted(transactionPoid, "N")
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + transactionPoid));

        FFProjectsCtrlSheetDtl existing = projectsCtrlSheetDtlRepository
                .findByTransactionPoidAndDetRowId(transactionPoid, detRowId)
                .orElseThrow(() -> new ResourceNotFoundException("Control sheet row not found with Det Row ID: " + detRowId));

        FFProjectsCtrlSheetDtl oldCtrl = FFProjectsCtrlSheetDtl.builder()
                .transactionPoid(existing.getTransactionPoid())
                .detRowId(existing.getDetRowId())
                .freightType(existing.getFreightType())
                .jobNoPoid(existing.getJobNoPoid())
                .origin(existing.getOrigin())
                .destination(existing.getDestination())
                .etd(existing.getEtd())
                .etaAta(existing.getEtaAta())
                .arrivalDate(existing.getArrivalDate())
                .noOfPackages(existing.getNoOfPackages())
                .weight(existing.getWeight())
                .cbm(existing.getCbm())
                .carrierPoid(existing.getCarrierPoid())
                .line(existing.getLine())
                .truckNumber(existing.getTruckNumber())
                .description(existing.getDescription())
                .sailDate(existing.getSailDate())
                .pol(existing.getPol())
                .pod(existing.getPod())
                .createdBy(existing.getCreatedBy())
                .createdDate(existing.getCreatedDate())
                .lastModifiedBy(existing.getLastModifiedBy())
                .lastModifiedDate(existing.getLastModifiedDate())
                .build();

        if ("N".equals(request.getActive()) && existing.getJobNoPoid() != null) {
            throw new FFValidationException("Cannot deactivate control sheet: it is linked to a job.",
                    List.of(new ValidationError(null, "ACTIVE", "Cannot deactivate a control sheet that is linked to a job.")));
        }

        existing.setFreightType(request.getFreightType());
        existing.setJobNoPoid(request.getJobNoPoid());
        existing.setOrigin(request.getOriginPoid());
        existing.setDestination(request.getDestinationPoid());
        existing.setEtd(request.getEtd());
        existing.setEtaAta(request.getEtaAta());
        existing.setArrivalDate(request.getArrivalDate());
        existing.setNoOfPackages(request.getNoOfPackages());
        existing.setWeight(request.getWeight());
        existing.setCbm(request.getCbm());
        existing.setCarrierPoid(request.getCarrierPoid());
        existing.setLine(request.getLinePoid());
        existing.setTruckNumber(request.getTruckNumber());
        existing.setDescription(request.getDescription());
        existing.setSailDate(request.getSailDate());
        existing.setPol(request.getSfPOL());
        existing.setPod(request.getSfPOD());
        if (request.getActive() != null) {
            existing.setActive(request.getActive());
        }
        existing.setLastModifiedBy(UserContext.getUserName());
        existing.setLastModifiedDate(LocalDateTime.now());

        projectsCtrlSheetDtlRepository.save(existing);

        String logDetail = String.format("KeyId = TRANSACTION_POID: %s DET_ROW_ID: %s", transactionPoid, detRowId);
        loggingService.createLogBatch(List.of(new com.asg.common.lib.dto.request.LogRequestDto<>(oldCtrl, existing, FFProjectsCtrlSheetDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail)));

        return mapper.mapCtrlSheetDetailToResponse(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UpcomingJobDTO> getUpcomingJobsList(Long transactionPoid, LocalDate fromDate, LocalDate toDate, String sortBy, String sortDir) {
        List<FreightJobSummaryProjection> jobs = (fromDate != null && toDate != null)
                ? freightJobProjectionRepository.findAllFreightJobsByDateRange(transactionPoid, fromDate, toDate)
                : freightJobProjectionRepository.findAllFreightJobs(transactionPoid);

        List<UpcomingJobDTO> result = jobs.stream()
                .map(this::mapToUpcomingJobDTO)
                .collect(Collectors.toList());
        applySorting(result, sortBy, sortDir);
        assignDetRowIds(result, UpcomingJobDTO::setDetRowId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public FreightJobsSummaryDTO getAllFreightsSummary(Long transactionPoid, FreightFilterRequest filter) {
        List<FreightJobSummaryProjection> allJobs = freightJobProjectionRepository.findAllFreightJobs(transactionPoid);
        
        List<FreightSummaryDTO> allFreights = allJobs.stream().map(this::mapToFreightSummaryDTO).collect(Collectors.toList());
        assignDetRowIds(allFreights, FreightSummaryDTO::setDetRowId);
        List<AirFreightSummaryDTO> airFreights = getAirFreightsSummary(transactionPoid, null, null, null, null);
        List<SeaFreightSummaryDTO> seaFreights = getSeaFreightsSummary(transactionPoid, null, null, null, null);
        List<RoadFreightSummaryDTO> roadFreights = getRoadFreightsSummary(transactionPoid, null, null, null, null);
        List<UpcomingJobDTO> upcomingJobs = getUpcomingJobsList(transactionPoid, null, null, null, null);
        
        FreightJobsSummaryDTO.SummaryTotalsDTO totals = new FreightJobsSummaryDTO.SummaryTotalsDTO(
                allJobs.size(),
                airFreights.size(),
                seaFreights.size(),
                roadFreights.size(),
                upcomingJobs.size(),
                allJobs.stream().mapToDouble(j -> j.getWeight() != null ? j.getWeight() : 0.0).sum(),
                allJobs.stream().mapToDouble(j -> j.getCbm() != null ? j.getCbm() : 0.0).sum()
        );
        
        return new FreightJobsSummaryDTO(allFreights, airFreights, seaFreights, roadFreights, upcomingJobs, totals);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FreightSummaryDTO> getAllFreights(Long transactionPoid, LocalDate fromDate, LocalDate toDate, String sortBy, String sortDir) {
        List<FreightJobSummaryProjection> jobs = (fromDate != null && toDate != null)
                ? freightJobProjectionRepository.findAllFreightJobsByDateRange(transactionPoid, fromDate, toDate)
                : freightJobProjectionRepository.findAllFreightJobs(transactionPoid);

        List<FreightSummaryDTO> result = jobs.stream()
                .map(this::mapToFreightSummaryDTO)
                .collect(Collectors.toList());
        applySorting(result, sortBy, sortDir);
        assignDetRowIds(result, FreightSummaryDTO::setDetRowId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobStatusPendingBillDTO> getJobStatusPendingBills(Long transactionPoid, LocalDate fromDate, LocalDate toDate, String sortBy, String sortDir) {
        List<FreightJobSummaryProjection> jobs = (fromDate != null && toDate != null)
                ? freightJobProjectionRepository.findAllFreightJobsByDateRange(transactionPoid, fromDate, toDate)
                : freightJobProjectionRepository.findAllFreightJobs(transactionPoid);

        if (jobs.isEmpty()) return Collections.emptyList();

        List<Long> jobIds = jobs.stream().map(FreightJobSummaryProjection::getJobId).collect(Collectors.toList());
        Map<Long, FFManifestHdr> manifestById = manifestHdrRepository.findAllById(jobIds).stream()
                .collect(Collectors.toMap(FFManifestHdr::getTransactionPoid, Function.identity()));
        Map<Long, BigDecimal> bookedAmountByJobId = manifestChargesRepository.findByTransactionPoidIn(jobIds).stream()
                .collect(Collectors.groupingBy(
                        FFManifestChargesDtl::getTransactionPoid,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                charge -> charge.getTotalSellingCharge() != null ? charge.getTotalSellingCharge() : BigDecimal.ZERO,
                                BigDecimal::add
                        )
                ));

        List<JobStatusPendingBillDTO> result = jobs.stream()
                .map(job -> manifestById.get(job.getJobId()))
                .filter(Objects::nonNull)
                .map(manifest -> mapToJobStatusPendingBillDTO(manifest, bookedAmountByJobId.getOrDefault(manifest.getTransactionPoid(), BigDecimal.ZERO)))
                .collect(Collectors.toList());
        applySorting(result, sortBy, sortDir);
        assignDetRowIds(result, JobStatusPendingBillDTO::setDetRowId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AirFreightSummaryDTO> getAirFreightsSummary(Long transactionPoid, LocalDate fromDate, LocalDate toDate, String sortBy, String sortDir) {
        List<AirFreightJobProjection> airJobs = freightJobProjectionRepository.findAirFreightJobsFiltered(transactionPoid, fromDate, toDate);
        if (airJobs.isEmpty()) return Collections.emptyList();

        List<Long> jobIds = airJobs.stream().map(AirFreightJobProjection::getJobId).collect(Collectors.toList());

        // Batch fetch air package details
        Map<Long, List<FFManifestAirPkgDtl>> airPkgMap = airPkgRepository.findByTransactionPoidIn(jobIds)
                .stream().collect(Collectors.groupingBy(FFManifestAirPkgDtl::getTransactionPoid));

        List<AirFreightSummaryDTO> result = new ArrayList<>();
        for (AirFreightJobProjection job : airJobs) {
            Long jobId = job.getJobId();
            List<FFManifestAirPkgDtl> airPkgs = airPkgMap.getOrDefault(jobId, Collections.emptyList());

            if (airPkgs.isEmpty()) {
                result.add(buildAirFreightRow(job, null));
            } else {
                for (FFManifestAirPkgDtl pkg : airPkgs) {
                    result.add(buildAirFreightRow(job, pkg));
                }
            }
        }
        applySorting(result, sortBy, sortDir);
        assignDetRowIds(result, AirFreightSummaryDTO::setDetRowId);
        return result;
    }

    private AirFreightSummaryDTO buildAirFreightRow(AirFreightJobProjection job, FFManifestAirPkgDtl pkg) {
        AirFreightSummaryDTO dto = new AirFreightSummaryDTO();
        // Header fields
        dto.setJobId(job.getJobId());
        dto.setJobNo(job.getJobNo());
        dto.setMawbNo(job.getMawbNo());
        dto.setHawbNo(job.getHawbNo());
        dto.setFlightNo(job.getFlightNo());
        dto.setOrigin(job.getOrigin());
        dto.setOriginLov(getLov(parseLong(job.getOrigin()), "FF_AIRPORTS"));
        dto.setDestination(job.getDestination());
        dto.setDestinationLov(getLov(parseLong(job.getDestination()), "FF_AIRPORTS"));
        dto.setCarrier(job.getCarrierCode());
        dto.setCarrierLov(getLov(parseLong(job.getCarrierCode()), "AIRLINE"));
        dto.setEtd(job.getEtd());
        dto.setEta(job.getEtaAta());
        dto.setJobStatus(job.getJobStatus());
        dto.setDocumentStatus(job.getDocumentStatus() != null ? job.getDocumentStatus() : "NA");

        // Air package fields
        if (pkg != null) {
            dto.setNoOfPackages(pkg.getNoOfPacks() != null ? pkg.getNoOfPacks().doubleValue() : null);
            dto.setWeight(pkg.getTotalWeight() != null ? pkg.getTotalWeight().doubleValue() : null);
            dto.setCbm(pkg.getTotalVolume() != null ? pkg.getTotalVolume().doubleValue() : null);
            dto.setTotalVolume(pkg.getTotalVolume() != null ? pkg.getTotalVolume().doubleValue() : null);
            dto.setChargeableWeight(pkg.getChargeableWeight() != null ? pkg.getChargeableWeight().doubleValue() : null);
            dto.setDescription(pkg.getDescription());
            dto.setAppointmentDate(pkg.getAppointmentDate() != null ? pkg.getAppointmentDate().toLocalDate() : null);
            dto.setDeliveryDate(pkg.getDeliveryDate() != null ? pkg.getDeliveryDate().toLocalDate() : null);
            dto.setImcoClassUnno(pkg.getImcoClassUnno());
            dto.setDetention(pkg.getDetention());
            dto.setRemarks(pkg.getRemarks());
            dto.setActualArrivalDate(pkg.getDeliveryDate() != null ? pkg.getDeliveryDate().toLocalDate() : null);
        } else {
            // Use header-level totals if no air packages
            dto.setNoOfPackages(job.getNoOfPackages());
            dto.setWeight(job.getWeight());
            dto.setCbm(job.getCbm());
            dto.setTotalVolume(job.getCbm());
            dto.setDescription(job.getDescription());
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeaFreightSummaryDTO> getSeaFreightsSummary(Long transactionPoid, LocalDate fromDate, LocalDate toDate, String sortBy, String sortDir) {
        List<SeaFreightJobProjection> seaJobs = freightJobProjectionRepository.findSeaFreightJobsFiltered(transactionPoid, fromDate, toDate);
        if (seaJobs.isEmpty()) return Collections.emptyList();

        List<Long> jobIds = seaJobs.stream().map(SeaFreightJobProjection::getJobId).collect(Collectors.toList());

        Map<Long, FFManifestHdr> headerMap = manifestHdrRepository.findAllById(jobIds)
                .stream().collect(Collectors.toMap(FFManifestHdr::getTransactionPoid, Function.identity()));
        Map<Long, List<FFManifestContainerDtl>> containerMap = containerRepository.findByTransactionPoidIn(jobIds)
                .stream().collect(Collectors.groupingBy(FFManifestContainerDtl::getTransactionPoid));

        List<SeaFreightSummaryDTO> result = new ArrayList<>();
        for (SeaFreightJobProjection job : seaJobs) {
            Long jobId = job.getJobId();
            List<FFManifestContainerDtl> containers = containerMap.getOrDefault(jobId, Collections.emptyList());
            FFManifestHdr header = headerMap.get(jobId);

            if (containers.isEmpty()) {
                result.add(buildSeaFreightRow(job, header, null));
            } else {
                for (FFManifestContainerDtl container : containers) {
                    result.add(buildSeaFreightRow(job, header, container));
                }
            }
        }
        applySorting(result, sortBy, sortDir);
        assignDetRowIds(result, SeaFreightSummaryDTO::setDetRowId);
        return result;
    }

    private SeaFreightSummaryDTO buildSeaFreightRow(SeaFreightJobProjection job, FFManifestHdr header, FFManifestContainerDtl container) {
        SeaFreightSummaryDTO dto = new SeaFreightSummaryDTO();
        // Header fields
        dto.setJobId(job.getJobId());
        dto.setJobNo(job.getJobNo());
        dto.setVesselName(job.getVesselName());
        dto.setPol(job.getPol());
        dto.setPolLov(getLov(parseLong(job.getPol()), "PORT_MASTER"));
        dto.setPod(job.getPod());
        dto.setPodLov(getLov(parseLong(job.getPod()), "PORT_MASTER"));
        dto.setMasterBlNo(job.getMasterBlNo());
        dto.setHouseBlNo(job.getHouseBlNo());
        dto.setEta(job.getEtaAta());
        dto.setEtd(job.getEtd());
        dto.setArrivalDate(job.getArrivalDate());
        dto.setSailDate(job.getSailDate());
        dto.setJobStatus(job.getJobStatus());
        dto.setDocumentStatus(job.getDocumentStatus());
        dto.setDescription(job.getDescription());
        dto.setLine(job.getLine() != null ? String.valueOf(job.getLine()) : null);
        if (header != null) {
            dto.setHouseBlNo2(header.getHouseBlNo2());
            dto.setMotherVesselName(header.getMotherVslName());
            dto.setMotherVoyageNo(header.getMotherVslVoyageNo());
            dto.setVoyageNo(header.getFeederVoyageNo());
            dto.setReleaseType(header.getReleasedType());
            dto.setReleaseLov(getLov(parseLong(header.getReleasedType()), "BL_RELEASE_TYPE"));
            dto.setOfoqManifestRef(header.getOfoqMnfRef());
            dto.setRadioActive(header.getRadioAction());
        }

        // Container fields
        if (container != null) {
            dto.setContainerNo(container.getContainerNo());
            dto.setContainerType(container.getContainerSize());
            dto.setContainerTypeLov(container.getContainerTypePoid() != null
                    ? getLov(container.getContainerTypePoid().longValue(), "LINE_CONTAINER_TYPE_MASTER") : null);
            dto.setSealNumber(container.getSealNo());
            dto.setCargoDescription(container.getCargoDescription());
            dto.setQty(container.getQuantity() != null ? container.getQuantity().doubleValue() : null);
            dto.setQtyPackages(container.getNoOfPacks() != null ? container.getNoOfPacks().doubleValue() : null);
            dto.setWeight(container.getNetWeight() != null ? container.getNetWeight().doubleValue() : null);
            dto.setCbm(container.getNetVolume() != null ? container.getNetVolume().doubleValue() : null);
            dto.setAppointmentDate(container.getCargoCollectionDate() != null ? container.getCargoCollectionDate().toLocalDate() : null);
            dto.setDeliveryDate(container.getDeliveryDate() != null ? container.getDeliveryDate().toLocalDate() : null);
            dto.setDetention("Y".equalsIgnoreCase(container.getDetention()) ? "YES" : "NO");
            dto.setDestuffingFull(container.getUnloadDate() != null ? "Destuffed" : "Full");
            dto.setDocStatus(container.getDocStatus());
            dto.setRemarks(container.getRemarks());
        } else {
            // Use header-level totals
            dto.setWeight(job.getWeight());
            dto.setCbm(job.getCbm());
            dto.setQtyPackages(job.getNoOfPacks());
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoadFreightSummaryDTO> getRoadFreightsSummary(Long transactionPoid, LocalDate fromDate, LocalDate toDate, String sortBy, String sortDir) {
        List<RoadFreightJobProjection> roadJobs = freightJobProjectionRepository.findRoadFreightJobsFiltered(transactionPoid, fromDate, toDate);
        if (roadJobs.isEmpty()) return Collections.emptyList();

        List<Long> jobIds = roadJobs.stream().map(RoadFreightJobProjection::getJobId).collect(Collectors.toList());

        // Batch fetch truck details (truck details already contain bayan data)
        Map<Long, List<FFManifestTruckDtl>> truckMap = truckRepository.findByTransactionPoidIn(jobIds)
                .stream().collect(Collectors.groupingBy(FFManifestTruckDtl::getTransactionPoid));

        List<RoadFreightSummaryDTO> result = new ArrayList<>();
        for (RoadFreightJobProjection job : roadJobs) {
            Long jobId = job.getJobId();
            List<FFManifestTruckDtl> trucks = truckMap.getOrDefault(jobId, Collections.emptyList());

            if (trucks.isEmpty()) {
                // No truck details - still create one row with header data
                result.add(buildRoadFreightRow(job, null));
            } else {
                // One row per truck
                for (FFManifestTruckDtl truck : trucks) {
                    result.add(buildRoadFreightRow(job, truck));
                }
            }
        }
        applySorting(result, sortBy, sortDir);
        assignDetRowIds(result, RoadFreightSummaryDTO::setDetRowId);
        return result;
    }

    private RoadFreightSummaryDTO buildRoadFreightRow(RoadFreightJobProjection job, FFManifestTruckDtl truck) {
        RoadFreightSummaryDTO dto = new RoadFreightSummaryDTO();
        // Header fields
        dto.setJobId(job.getJobId());
        dto.setJobNo(job.getJobNo());
        dto.setTransportFrom(job.getTransportFrom());
        dto.setTransportTo(job.getTransportTo());
        dto.setWeight(job.getWeight());
        dto.setCbm(job.getCbm());
        dto.setDescription(job.getDescription());
        dto.setJobStatus(job.getJobStatus());
        dto.setDocumentStatus(job.getDocumentStatus());
        dto.setOrigin(job.getTransportFrom());

        // Truck detail fields (include bayan data from truck entity)
        if (truck != null) {
            dto.setBlAwbNumber(truck.getBlAwbNumber());
            dto.setTruckNumber(truck.getTruckNumber());
            dto.setBayanNumber(truck.getBayanNumber());
            dto.setBayanMode(truck.getBayanCode());
            dto.setEta(truck.getEta() != null ? truck.getEta().toLocalDate() : job.getEta());
            dto.setDuty(truck.getDutyAmount() != null ? truck.getDutyAmount().doubleValue() : null);
            dto.setVat(truck.getVatAmount() != null ? truck.getVatAmount().doubleValue() : null);
            dto.setTotalPaid(truck.getTotalPaidAmount() != null ? truck.getTotalPaidAmount().doubleValue() : null);
            dto.setExpiryDate(truck.getExpiryDate() != null ? truck.getExpiryDate().toLocalDate() : null);
            dto.setSubmittedDate(truck.getSubmittedDate() != null ? truck.getSubmittedDate().toLocalDate() : null);
            dto.setPaymentDate(truck.getPaymentDate() != null ? truck.getPaymentDate().toLocalDate() : null);
        } else {
            dto.setBlAwbNumber(job.getBlAwbNumber());
            dto.setEta(job.getEta());
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public AirFreightDetailedDTO getAirFreightDetails(Long projectId, Long jobId) {
        FFManifestHdr job = manifestHdrRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        
        if (!projectId.equals(job.getProjectPoid() != null ? job.getProjectPoid().longValue() : null)) {
            throw new ResourceNotFoundException("Job does not belong to this project");
        }
        
        AirFreightDetailedDTO dto = new AirFreightDetailedDTO();
        dto.setFlightNo(job.getFlightNo());
        dto.setFlightDate(job.getFlightDate());
        dto.setFlightNo2(job.getFlightNo2());
        dto.setFlightDate2(job.getFlightDate2());
        dto.setCarrier(job.getCarrierCode());
        dto.setHawbNo(job.getHouseBlNo());
        dto.setAgentPoid(job.getAgentPoid() != null ? job.getAgentPoid().longValue() : null);
        dto.setAgentAcctNo(job.getAgentAcctNo());
        dto.setAgentIataNo(job.getAgentIataNo());
        dto.setPackages(airPkgRepository.findByTransactionPoid(jobId).stream()
                .map(this::mapAirPkgToDTO).collect(Collectors.toList()));
        dto.setBayanDetails(bayanRepository.findByTransactionPoid(jobId).stream()
                .map(this::mapBayanToDTO).collect(Collectors.toList()));
        dto.setCharges(getJobCharges(jobId));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public SeaFreightDetailedDTO getSeaFreightDetails(Long projectId, Long jobId) {
        FFManifestHdr job = manifestHdrRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        
        if (!projectId.equals(job.getProjectPoid() != null ? job.getProjectPoid().longValue() : null)) {
            throw new ResourceNotFoundException("Job does not belong to this project");
        }
        
        SeaFreightDetailedDTO dto = new SeaFreightDetailedDTO();
        dto.setFeederVesselName(job.getFeederVslName());
        dto.setFeederVoyageNo(job.getFeederVoyageNo());
        dto.setFeederSailDate(job.getFeederVslSailDate());
        dto.setFeederEta(job.getFeederVslEta());
        dto.setFeederArrivalDate(job.getFeederVslArrivalDate());
        dto.setMotherVesselName(job.getMotherVslName());
        dto.setMotherVoyageNo(job.getMotherVslVoyageNo());
        dto.setMotherSailDate(job.getMotherVslSailDate());
        dto.setMotherEta(job.getMotherVslEta());
        dto.setMasterBlNo(job.getMasterBlNo());
        dto.setHouseBlNo(job.getHouseBlNo());
        dto.setHouseBlNo2(job.getHouseBlNo2());
        dto.setBlStatus(job.getBlStatus());
        dto.setBlIssueDate(job.getBlIssueDate());
        dto.setReleasedType(job.getReleasedType());
        dto.setOfoqManifestRef(job.getOfoqMnfRef());
        dto.setRadioActive(job.getRadioAction());
        dto.setContainers(containerRepository.findByTransactionPoid(jobId).stream()
                .map(this::mapContainerToDTO).collect(Collectors.toList()));
        dto.setBayanDetails(bayanRepository.findByTransactionPoid(jobId).stream()
                .map(this::mapBayanToDTO).collect(Collectors.toList()));
        dto.setCharges(getJobCharges(jobId));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public RoadFreightDetailedDTO getRoadFreightDetails(Long projectId, Long jobId) {
        FFManifestHdr job = manifestHdrRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        
        if (!projectId.equals(job.getProjectPoid() != null ? job.getProjectPoid().longValue() : null)) {
            throw new ResourceNotFoundException("Job does not belong to this project");
        }
        
        RoadFreightDetailedDTO dto = new RoadFreightDetailedDTO();
        dto.setTransportFrom(job.getTruckTransportFrom());
        dto.setTransportTo(job.getTruckTransportTo());
        dto.setTruckCargoDetails(truckRepository.findByTransactionPoid(jobId).stream()
                .map(this::mapTruckToDTO).collect(Collectors.toList()));
        dto.setCharges(getJobCharges(jobId));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public JobChargesDTO getJobCharges(Long jobId) {
        List<FFManifestChargesDtl> charges = manifestChargesRepository.findByTransactionPoid(jobId);
        List<ChargeDTO> chargeDTOs = charges.stream().map(this::mapChargeToDTO).collect(Collectors.toList());
        
        BigDecimal totalBuying = charges.stream()
                .map(c -> c.getTotalBuyingCharge() != null ? c.getTotalBuyingCharge() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSelling = charges.stream()
                .map(c -> c.getTotalSellingCharge() != null ? c.getTotalSellingCharge() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTax = charges.stream()
                .map(c -> c.getTaxAmount() != null ? c.getTaxAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grandTotal = totalSelling.add(totalTax);
        BigDecimal margin = totalSelling.subtract(totalBuying);
        
        return new JobChargesDTO(chargeDTOs, totalBuying, totalSelling, totalTax, grandTotal, margin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BayanDTO> getProjectBayanDetails(Long projectId, String sortBy, String sortDir) {
        List<FreightJobSummaryProjection> jobs = freightJobProjectionRepository.findAllFreightJobs(projectId);
        if (jobs.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, String> jobNoById = jobs.stream()
                .collect(Collectors.toMap(FreightJobSummaryProjection::getJobId, FreightJobSummaryProjection::getJobNo));

        List<Long> jobIds = new ArrayList<>(jobNoById.keySet());

        List<BayanDTO> result = bayanRepository.findByTransactionPoidIn(jobIds).stream()
                .sorted(Comparator
                        .comparing(FFManifestBayanDtl::getTransactionPoid)
                        .thenComparing(FFManifestBayanDtl::getDetRowId))
                .map(bayan -> mapBayanToDTO(bayan, jobNoById.get(bayan.getTransactionPoid())))
                .collect(Collectors.toList());
        applySorting(result, sortBy, sortDir);
        return result;
    }

    @Override
    @Transactional
    public List<FFProjectsCtrlSheetDetailResponse> uploadControlSheetExcel(Long transactionPoid, MultipartFile file) {
        projectsHdrRepository.findByTransactionPoidAndDeleted(transactionPoid, "N")
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + transactionPoid));

        // Fast-fail: if any existing row is linked to a job, reject before doing any further work
        List<FFProjectsCtrlSheetDtl> existing = projectsCtrlSheetDtlRepository.findByTransactionPoid(transactionPoid);
        boolean anyLinkedToJob = existing.stream().anyMatch(cs -> cs.getJobNoPoid() != null);
        if (anyLinkedToJob) {
            throw new FFValidationException("Cannot upload control sheet: one or more existing entries are linked to jobs. Please unlink them before uploading.",
                    List.of(new ValidationError(null, "CONTROL_SHEET", "One or more existing control sheet entries are linked to jobs. Please unlink them before uploading.")));
        }

        List<CtrlSheetExcelRow> parsedRows;
        try {
            parsedRows = parseControlSheetExcel(file);
        } catch (IOException e) {
            throw new FFValidationException("Failed to read Excel file: " + e.getMessage(),
                    List.of(new ValidationError(null, "FILE", "Failed to read Excel file: " + e.getMessage())));
        }

        if (parsedRows.isEmpty()) {
            throw new FFValidationException("Excel file contains no data rows.",
                    List.of(new ValidationError(null, "FILE", "Excel file contains no data rows.")));
        }

        Map<String, LovGetListDto> portDescMap    = loadLovDescMap("PORT_MASTER");
        Map<String, LovGetListDto> airportDescMap = loadLovDescMap("FF_AIRPORTS");
        Map<String, LovGetListDto> airlineDescMap = loadLovDescMap("AIRLINE");
        List<ValidationError> errors = validateExcelRows(parsedRows, portDescMap, airportDescMap, airlineDescMap);
        if (!errors.isEmpty()) {
            throw new FFValidationException("Excel pre-validation failed", errors);
        }

        if (!existing.isEmpty()) {
            projectsCtrlSheetDtlRepository.deleteByTransactionPoid(transactionPoid);
        }

        String currentUser = UserContext.getUserName();
        LocalDateTime now = LocalDateTime.now();
        List<FFProjectsCtrlSheetDtl> toSave = new ArrayList<>();
        long detRowId = 0;
        for (CtrlSheetExcelRow row : parsedRows) {
            String ft = row.getFreightType();
            FFProjectsCtrlSheetDtl.FFProjectsCtrlSheetDtlBuilder builder = FFProjectsCtrlSheetDtl.builder()
                    .transactionPoid(transactionPoid)
                    .detRowId(++detRowId)
                    .freightType(ft)
                    .active("Y")
                    .createdBy(currentUser)
                    .createdDate(now)
                    .lastModifiedBy(currentUser)
                    .lastModifiedDate(now);

            if ("AIR FREIGHT".equals(ft)) {
                builder.origin(row.getOrigin())
                        .destination(row.getDestination())
                        .etd(row.getEtd())
                        .etaAta(row.getEtaAta())
                        .arrivalDate(row.getArrivalDate())
                        .noOfPackages(row.getNoOfPackages())
                        .weight(row.getWeight())
                        .cbm(row.getCbm())
                        .carrierPoid(row.getCarrierPoid())
                        .description(row.getDescription());
            } else if ("SEA FREIGHT".equals(ft)) {
                builder.pol(row.getPol())
                        .pod(row.getPod())
                        .arrivalDate(row.getArrivalDate())
                        .etaAta(row.getEtaAta())
                        .etd(row.getEtd())
                        .sailDate(row.getSailDate())
                        .weight(row.getWeight())
                        .cbm(row.getCbm())
                        .line(row.getLine())
                        .description(row.getDescription());
            } else {
                builder.description(row.getDescription())
                        .etaAta(row.getEtaAta())
                        .etd(row.getEtd())
                        .arrivalDate(row.getArrivalDate())
                        .weight(row.getWeight())
                        .cbm(row.getCbm())
                        .truckNumber(row.getTruckNumber());
            }

            toSave.add(builder.build());
        }
        projectsCtrlSheetDtlRepository.saveAll(toSave);

        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(),
                String.format("Control sheet uploaded from Excel '%s': %d rows saved", file.getOriginalFilename(), toSave.size()));

        return getControlSheetsByProject(transactionPoid, null);
    }

    private List<CtrlSheetExcelRow> parseControlSheetExcel(MultipartFile file) throws IOException {
        List<CtrlSheetExcelRow> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            List<ValidationError> columnErrors = validateColumnHeaders(sheet);
            if (!columnErrors.isEmpty()) {
                throw new FFValidationException("Excel template column structure is invalid", columnErrors);
            }

            for (int rowIdx = 3; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (isRowEmpty(row)) continue;

                CtrlSheetExcelRow parsed = new CtrlSheetExcelRow();
                parsed.setRowNum(rowIdx + 1);

                StringBuilder keyBuilder = new StringBuilder();
                for (int col = 1; col <= 25; col++) {
                    keyBuilder.append(getCellStringValue(row, col)).append("|");
                }
                parsed.setDuplicateKey(keyBuilder.toString());

                parsed.setFreightType(getCellStringValue(row, 1));
                parsed.setEtaAta(parseDateCell(row, 8));
                parsed.setVesselRaw(getCellStringValue(row, 9));
                parsed.setPolRaw(getCellStringValue(row, 10));
                parsed.setDescription(getCellStringValue(row, 12));
                parsed.setNoOfPackages(getNumericCellValue(row, 15));
                parsed.setWeight(getNumericCellValue(row, 16));
                parsed.setCbm(getNumericCellValue(row, 17));
                parsed.setArrivalDate(parseDateCell(row, 23));

                rows.add(parsed);
            }
        }
        return rows;
    }

    private List<ValidationError> validateColumnHeaders(Sheet sheet) {
        List<ValidationError> errors = new ArrayList<>();
        Row headerRow = sheet.getRow(1);
        if (headerRow == null) {
            errors.add(new ValidationError(null, "HEADER",
                    "Column header row (row 2) is missing from the Excel file"));
            return errors;
        }
        REQUIRED_COLUMN_HEADERS.forEach((colIndex, expectedHeader) -> {
            String actual = getCellStringValue(headerRow, colIndex)
                    .replace("\n", " ").trim().toUpperCase();
            if (!expectedHeader.equalsIgnoreCase(actual)) {
                errors.add(new ValidationError(2, "COLUMN_" + (colIndex + 1),
                        String.format("Column %d header mismatch: expected '%s' but found '%s'",
                                colIndex + 1, expectedHeader, actual.isEmpty() ? "(empty)" : actual)));
            }
        });
        return errors;
    }

    private List<ValidationError> validateExcelRows(List<CtrlSheetExcelRow> rows,
                                                     Map<String, LovGetListDto> portDescMap,
                                                     Map<String, LovGetListDto> airportDescMap,
                                                     Map<String, LovGetListDto> airlineDescMap) {
        List<ValidationError> errors = new ArrayList<>();

        // Duplicate check (exclude SN column) — return immediately if any duplicates found
        Map<String, Integer> seenKeys = new LinkedHashMap<>();
        for (CtrlSheetExcelRow row : rows) {
            String key = row.getDuplicateKey();
            if (seenKeys.containsKey(key)) {
                errors.add(new ValidationError(row.getRowNum(), "row",
                        String.format("Row %d is a duplicate of row %d (all columns except SN are identical)",
                                row.getRowNum(), seenKeys.get(key))));
            } else {
                seenKeys.put(key, row.getRowNum());
            }
        }
        if (!errors.isEmpty()) {
            return errors;
        }

        for (CtrlSheetExcelRow row : rows) {
            String modeRaw = row.getFreightType();
            if (modeRaw == null || modeRaw.isBlank()) {
                errors.add(new ValidationError(row.getRowNum(), "MODE", "MODE is required"));
                continue;
            }
            String modeCode = modeRaw.trim().toUpperCase();
            row.setFreightType(switch (modeCode) {
                case "AIR" -> "AIR FREIGHT";
                case "SEA" -> "SEA FREIGHT";
                default    -> modeCode;
            });

            // Freight-type-specific validation
            switch (modeCode) {
                case "AIR"  -> validateAirRow(row, airportDescMap, airlineDescMap, errors);
                case "SEA"  -> validateSeaRow(row, portDescMap, errors);
                case "ROAD" -> validateRoadRow(row, errors);
                default     -> validateRoadRow(row, errors);
            }
        }

        return errors;
    }

    private void validateAirRow(CtrlSheetExcelRow row,
                                 Map<String, LovGetListDto> airportDescMap,
                                 Map<String, LovGetListDto> airlineDescMap,
                                 List<ValidationError> errors) {
        // Origin → FF_AIRPORTS (from POL column, optional — validate if present)
        String originRaw = row.getPolRaw();
        if (originRaw != null && !originRaw.isBlank()) {
            LovGetListDto matched = airportDescMap.get(originRaw.trim().toLowerCase());
            if (matched == null) {
                errors.add(new ValidationError(row.getRowNum(), "ORIGIN",
                        String.format("Invalid origin airport '%s'. Value must match a valid FF_AIRPORTS entry.", originRaw.trim())));
            } else {
                row.setOrigin(matched.getPoid());
            }
        }

        // Air carrier → AIRLINE (from VESSEL column, optional — validate if present)
        String carrierRaw = row.getVesselRaw();
        if (carrierRaw != null && !carrierRaw.isBlank()) {
            LovGetListDto matched = airlineDescMap.get(carrierRaw.trim().toLowerCase());
            if (matched == null) {
                errors.add(new ValidationError(row.getRowNum(), "AIR_CARRIER",
                        String.format("Invalid air carrier '%s'. Value must match a valid AIRLINE entry.", carrierRaw.trim())));
            } else {
                row.setCarrierPoid(matched.getPoid());
            }
        }

        // Required numeric fields
        if (row.getNoOfPackages() == null) {
            errors.add(new ValidationError(row.getRowNum(), "NO_OF_PKGS", "Number of packages is required for AIR freight"));
        }
        if (row.getWeight() == null) {
            errors.add(new ValidationError(row.getRowNum(), "WEIGHT", "Weight is required for AIR freight"));
        }
        if (row.getCbm() == null) {
            errors.add(new ValidationError(row.getRowNum(), "VOLUME", "Volume (CBM) is required for AIR freight"));
        }
    }

    private void validateSeaRow(CtrlSheetExcelRow row,
                                 Map<String, LovGetListDto> portDescMap,
                                 List<ValidationError> errors) {
        // POL → PORT_MASTER (from POL column, optional — validate if present)
        String polRaw = row.getPolRaw();
        if (polRaw != null && !polRaw.isBlank()) {
            LovGetListDto matched = portDescMap.get(polRaw.trim().toLowerCase());
            if (matched == null) {
                errors.add(new ValidationError(row.getRowNum(), "POL",
                        String.format("Invalid POL value '%s'. Value must match a valid PORT_MASTER entry.", polRaw.trim())));
            } else {
                row.setPol(matched.getPoid().toString());
            }
        }

        // LINE → SHIP_VESSEL_MASTER (from VESSEL column — look up linePoid by vessel name)
        String vesselRaw = row.getVesselRaw();
        if (vesselRaw == null || vesselRaw.isBlank()) {
            errors.add(new ValidationError(row.getRowNum(), "VESSEL",
                    "Vessel is required for SEA freight to determine the Line"));
        } else {
            com.asg.operations.finaldisbursementaccount.entity.ShipVesselMaster vessel =
                    shipVesselMasterRepository.findFirstByVesselNameTrimmedIgnoreCase(vesselRaw.trim()).orElse(null);
            if (vessel == null) {
                errors.add(new ValidationError(row.getRowNum(), "VESSEL",
                        String.format("Vessel '%s' not found in the system.", vesselRaw.trim())));
            } else if (vessel.getLinePoid() == null) {
                errors.add(new ValidationError(row.getRowNum(), "VESSEL",
                        String.format("Vessel '%s' has no Line associated. Please update the vessel record first.", vesselRaw.trim())));
            } else {
                row.setLine(vessel.getLinePoid());
            }
        }

        // Required numeric fields
        if (row.getWeight() == null) {
            errors.add(new ValidationError(row.getRowNum(), "WEIGHT", "Weight is required for SEA freight"));
        }
        if (row.getCbm() == null) {
            errors.add(new ValidationError(row.getRowNum(), "VOLUME", "Volume (CBM) is required for SEA freight"));
        }
    }

    private void validateRoadRow(CtrlSheetExcelRow row, List<ValidationError> errors) {
        // TRUCK_NUMBER has no Excel column — remains null

        // Required numeric fields
        if (row.getWeight() == null) {
            errors.add(new ValidationError(row.getRowNum(), "WEIGHT", "Weight is required for ROAD freight"));
        }
        if (row.getCbm() == null) {
            errors.add(new ValidationError(row.getRowNum(), "VOLUME", "Volume (CBM) is required for ROAD freight"));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, LovGetListDto> loadLovDescMap(String lovName) {
        Map<String, Object> result = lovDataService.getLovList(
                "", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid(),
                lovName, 0, 0, "", "");
        List<LovGetListDto> items = result != null
                ? (List<LovGetListDto>) result.getOrDefault("data", Collections.emptyList())
                : Collections.emptyList();
        return items.stream()
                .filter(item -> item.getDescription() != null && item.getCode() != null)
                .collect(Collectors.toMap(
                        item -> item.getDescription().trim().toLowerCase(),
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    private String getCellStringValue(Row row, int colIndex) {
        if (row == null) return "";
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                double val = cell.getNumericCellValue();
                yield val == Math.floor(val) ? String.valueOf((long) val) : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private Double getNumericCellValue(Row row, int colIndex) {
        if (row == null) return null;
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                String val = cell.getStringCellValue().trim();
                if (val.isEmpty()) yield null;
                try {
                    yield Double.parseDouble(val);
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            default -> null;
        };
    }

    private LocalDate parseDateCell(Row row, int colIndex) {
        if (row == null) return null;
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim();
            if (val.isEmpty()) return null;
            try {
                return LocalDate.parse(val, EXCEL_DATE_FORMAT);
            } catch (DateTimeParseException ignored) {
                try {
                    return LocalDate.parse(val, EXCEL_DATE_FORMAT_SHORT);
                } catch (DateTimeParseException ignored2) {
                    return null;
                }
            }
        }
        return null;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = 0; i <= 25; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                if (!getCellStringValue(row, i).isBlank()) return false;
            }
        }
        return true;
    }

    @Override
    public byte[] exportControlSheetToExcel(Long transactionPoid, String freightType, LocalDate fromDate, LocalDate toDate) {
        throw new UnsupportedOperationException("Excel export not yet implemented");
    }

    @Override
    public byte[] exportControlSheetToPdf(Long transactionPoid, String freightType, LocalDate fromDate, LocalDate toDate) {
        throw new UnsupportedOperationException("PDF export not yet implemented");
    }

    @Override
    public void emailControlSheet(Long transactionPoid, String emailAddress, String freightType, LocalDate fromDate, LocalDate toDate) {
        throw new UnsupportedOperationException("Email functionality not yet implemented");
    }

    @Override
    public Long createJobFromUpcoming(Long transactionPoid, Long controlSheetDetRowId) {
        throw new UnsupportedOperationException("Create job from control sheet not applicable for freight jobs");
    }

//    private AirPackageDTO mapAirPkgToDTO(FFManifestAirPkgDtl e) {
//        AirPackageDTO dto = new AirPackageDTO();
//        dto.setDetRowId(e.getDetRowId());
//        dto.setNoOfPacks(e.getNoOfPacks());
//        dto.setPackUnit(e.getPackUnit());
//        dto.setTotalWeight(e.getTotalWeight());
//        dto.setTotalVolume(e.getTotalVolume());
//        dto.setChargeableWeight(e.getChargeableWeight());
//        dto.setDescription(e.getDescription());
//        return dto;
//    }
//
//    private ContainerDTO mapContainerToDTO(FFManifestContainerDtl e) {
//        ContainerDTO dto = new ContainerDTO();
//        dto.setDetRowId(e.getDetRowId());
//        dto.setContainerNo(e.getContainerNo());
//        dto.setContainerSealNo(e.getContainerSealNo());
//        dto.setContainerIsoCode(e.getContainerIsoCode());
//        dto.setContainerSize(e.getContainerSize());
//        dto.setGrossWeight(e.getGrossWeight());
//        dto.setGrossVolume(e.getGrossVolume());
//        dto.setNoOfPacks(e.getNoOfPacks());
//        dto.setPackUnit(e.getPackUnit());
//        return dto;
//    }
//
//    private BayanDTO mapBayanToDTO(FFManifestBayanDtl e) {
//        BayanDTO dto = new BayanDTO();
//        dto.setDetRowId(e.getDetRowId());
//        dto.setBayanNumber(e.getBayanNumber());
//        dto.setBayanMode(e.getBayanMode());
//        dto.setDutyAmount(e.getDutyAmount());
//        dto.setVatAmount(e.getVatAmount());
//        dto.setTotalPaidAmount(e.getTotalPaidAmount());
//        return dto;
//    }
//
//    private TruckCargoDTO mapTruckToDTO(FFManifestTruckDtl e) {
//        TruckCargoDTO dto = new TruckCargoDTO();
//        dto.setDetRowId(e.getDetRowId());
//        dto.setTruckNumber(e.getTruckNumber());
//        dto.setBayanNumber(e.getBayanNumber());
//        dto.setBayanCode(e.getBayanCode());
//        return dto;
//    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> List<T> applySorting(List<T> list, String sortBy, String sortDir) {
        if (sortBy == null || sortBy.isBlank() || list.isEmpty()) return list;

        try {
            Field field = list.get(0).getClass().getDeclaredField(sortBy);
            field.setAccessible(true);

            Comparator<T> comparator = (a, b) -> {
                try {
                    Object valA = field.get(a);
                    Object valB = field.get(b);
                    if (valA == null && valB == null) return 0;
                    if (valA == null) return 1;
                    if (valB == null) return -1;
                    if (valA instanceof Comparable) {
                        return ((Comparable) valA).compareTo(valB);
                    }
                    return valA.toString().compareToIgnoreCase(valB.toString());
                } catch (IllegalAccessException e) {
                    return 0;
                }
            };

            if ("desc".equalsIgnoreCase(sortDir)) {
                comparator = comparator.reversed();
            }

            list.sort(comparator);
        } catch (NoSuchFieldException e) {
            log.warn("Invalid sortBy field '{}', returning unsorted results", sortBy);
        }

        return list;
    }

    private <T> void assignDetRowIds(List<T> list, java.util.function.BiConsumer<T, Long> setter) {
        for (int i = 0; i < list.size(); i++) {
            setter.accept(list.get(i), (long) (i + 1));
        }
    }

    private FreightSummaryDTO mapToFreightSummaryDTO(FreightJobSummaryProjection p) {
        FreightSummaryDTO dto = new FreightSummaryDTO();
        dto.setJobId(p.getJobId());
        dto.setJobNo(p.getJobNo());
        dto.setFreightType(p.getFreightMode());
        dto.setDescription(p.getDescription());
        dto.setWeight(p.getWeight());
        dto.setCbm(p.getCbm());
        dto.setEta(p.getEtaAta());
        dto.setEtd(p.getEtd());
        dto.setArrivalDate(p.getArrivalDate());
        dto.setSailDate(p.getSailDate());
        dto.setJobStatus(p.getJobStatus());
        dto.setDocumentStatus(p.getDocumentStatus());
        dto.setPrincipalPoid(p.getPrincipalPoid());
        dto.setPrincipalLov(getLov(p.getPrincipalPoid(), "PRINCIPAL_MASTER"));
        dto.setPackages(p.getPackages());
        dto.setBlAwbNo(p.getBlAwbNo());
        dto.setOrigin(p.getOrigin());
        dto.setOriginLov(getLovByCode(p.getOrigin(), "FF_AIRPORTS"));
        dto.setDestination(p.getDestination());
        dto.setDestinationLov(getLovByCode(p.getDestination(), "FF_AIRPORTS"));
        dto.setCarrier(p.getCarrierCode());
        dto.setCarrierLov(getLovByCode(p.getCarrierCode(), "AIRLINE"));
        dto.setVesselName(p.getVesselName());
        dto.setTransportFrom(p.getTransportFrom());
        dto.setTransportTo(p.getTransportTo());
        dto.setPol(p.getPol());
        dto.setPod(p.getPod());
        dto.setLine(p.getLine());
        dto.setLineLov(getLov(parseLong(p.getLine()), "LINE_MASTER"));
        dto.setPolLov(getLov(parseLong(p.getPol()), "PORT_MASTER"));
        dto.setPodLov(getLov(parseLong(p.getPod()), "PORT_MASTER"));
        return dto;
    }

    private FreightSummaryDTO mapToFreightSummaryDTO(FFProjectsCtrlSheetDtl cs, FFManifestHdr manifest) {
        FreightSummaryDTO dto = new FreightSummaryDTO();
        dto.setJobId(cs.getJobNoPoid());
        dto.setJobNo(manifest != null ? manifest.getFfJobNo() : null);
        Long principalPoid = manifest != null && manifest.getPrincipalPoid() != null
                ? manifest.getPrincipalPoid().longValue() : null;
        dto.setPrincipalPoid(principalPoid);
        dto.setPrincipalLov(getLov(principalPoid, "PRINCIPAL_MASTER"));
        dto.setFreightType(cs.getFreightType());
        dto.setDescription(cs.getDescription());
        dto.setWeight(cs.getWeight());
        dto.setCbm(cs.getCbm());
        dto.setEta(cs.getEtaAta());
        dto.setEtd(cs.getEtd());
        dto.setArrivalDate(cs.getArrivalDate());
        dto.setJobStatus(manifest != null ? manifest.getJobStatus() : null);
        dto.setOrigin(cs.getOrigin() != null ? String.valueOf(cs.getOrigin()) : null);
        dto.setOriginLov(getLov(cs.getOrigin(), "FF_AIRPORTS"));
        dto.setDestination(cs.getDestination() != null ? String.valueOf(cs.getDestination()) : null);
        dto.setDestinationLov(getLov(cs.getDestination(), "FF_AIRPORTS"));
        dto.setCarrier(manifest != null ? manifest.getCarrierCode() : null);
        dto.setCarrierLov(getLov(cs.getCarrierPoid(), "AIRLINE"));
        dto.setPackages(cs.getNoOfPackages());
        dto.setPol(cs.getPol());
        dto.setPod(cs.getPod());
        dto.setPolLov(getLov(parseLong(cs.getPol()), "PORT_MASTER"));
        dto.setPodLov(getLov(parseLong(cs.getPod()), "PORT_MASTER"));
        dto.setLine(cs.getLine() != null ? String.valueOf(cs.getLine()) : null);
        dto.setLineLov(getLov(cs.getLine(), "LINE_MASTER"));
        dto.setSailDate(cs.getSailDate());
        dto.setTruckNumber(cs.getTruckNumber());
        dto.setBlAwbNo(manifest != null ? manifest.getMasterBlNo() : null);
        dto.setVesselName(manifest != null ? manifest.getMotherVslName() : null);
        return dto;
    }

    private JobStatusPendingBillDTO mapToJobStatusPendingBillDTO(FFManifestHdr manifest, BigDecimal bookedAmount) {
        JobStatusPendingBillDTO dto = new JobStatusPendingBillDTO();
        dto.setJobId(manifest.getTransactionPoid());
        dto.setJobNo(manifest.getFfJobNo());
        dto.setBlNo(manifest.getMasterBlNo());
        dto.setEtaAta(manifest.getMotherVslEta() != null ? manifest.getMotherVslEta().toLocalDate() : null);

        Long principalPoid = manifest.getPrincipalPoid() != null ? manifest.getPrincipalPoid().longValue() : null;
        dto.setPrincipalPoid(principalPoid);
        dto.setPrincipalLov(getLov(principalPoid, "PRINCIPAL_MASTER"));

        Long customerPoid = manifest.getBillToCustomerPoid() != null ? manifest.getBillToCustomerPoid().longValue() : null;
        dto.setCustomerPoid(customerPoid);
        dto.setCustomerLov(projectJobMapper.getCustomerSupplierLov(customerPoid, manifest.getBillingTo()));

        dto.setMode(manifest.getShipmentMode());
        dto.setJobStatus(manifest.getJobStatus());
        dto.setCompletedOn(manifest.getJobClosedDate() != null ? manifest.getJobClosedDate().toLocalDate() : null);
        dto.setBookedAmount(bookedAmount);
        return dto;
    }

    private LovGetListDto getLov(Long poid, String lovName) {
        if (poid == null) {
            return null;
        }
        return lovDataService.getDetailsByPoidAndLovNameFast(poid, lovName);
    }

    private LovGetListDto getLovByCode(String code, String lovName) {
        if (code == null || code.isBlank()) return null;
        return lovDataService.getLovItemByCodeFast(code, lovName);
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private UpcomingJobDTO mapToUpcomingJobDTO(FreightJobSummaryProjection p) {
        UpcomingJobDTO dto = new UpcomingJobDTO();
        dto.setJobId(p.getJobId());
        dto.setJobNo(p.getJobNo());
        dto.setBlAwbNo(p.getBlAwbNo());
        dto.setFreightType(p.getFreightMode());
        dto.setLine(p.getLine());
        dto.setLineLov(getLov(parseLong(p.getLine()), "LINE_MASTER"));
        dto.setEta(p.getEtaAta());
        dto.setPol(p.getPol());
        dto.setPolLov(getLov(parseLong(p.getPol()), "PORT_MASTER"));
        dto.setPod(p.getPod());
        dto.setOrigin(p.getOrigin());
        dto.setOriginLov(getLov(parseLong(p.getOrigin()), "FF_AIRPORTS"));
        dto.setDestination(p.getDestination());
        dto.setDestinationLov(getLov(parseLong(p.getDestination()), "FF_AIRPORTS"));
        dto.setDescription(p.getDescription());
        dto.setCbm(p.getCbm());
        dto.setPackages(p.getPackages());
        dto.setWeight(p.getWeight());
        dto.setJobStatus(p.getJobStatus());
        dto.setCanCreateJob(false);
        return dto;
    }

    private AirControlSheetDTO mapCtrlSheetToAirDTO(FFProjectsCtrlSheetDtl cs) {
        AirControlSheetDTO dto = new AirControlSheetDTO();
        dto.setDetRowId(cs.getDetRowId());
        dto.setJobId(cs.getJobNoPoid());
        dto.setEtd(cs.getEtd());
        dto.setEta(cs.getEtaAta());
        dto.setArrivalDate(cs.getArrivalDate());
        dto.setNoOfPackages(cs.getNoOfPackages());
        dto.setWeight(cs.getWeight());
        dto.setCbm(cs.getCbm());
        dto.setDescription(cs.getDescription());
        return dto;
    }

    private SeaControlSheetDTO mapCtrlSheetToSeaDTO(FFProjectsCtrlSheetDtl cs) {
        SeaControlSheetDTO dto = new SeaControlSheetDTO();
        dto.setDetRowId(cs.getDetRowId());
        dto.setJobId(cs.getJobNoPoid());
        dto.setPol(cs.getPol());
        dto.setPod(cs.getPod());
        dto.setEtd(cs.getEtd());
        dto.setEta(cs.getEtaAta());
        dto.setArrivalDate(cs.getArrivalDate());
        dto.setSailDate(cs.getSailDate());
        dto.setWeight(cs.getWeight());
        dto.setCbm(cs.getCbm());
        dto.setDescription(cs.getDescription());
        return dto;
    }

    private RoadControlSheetDTO mapCtrlSheetToRoadDTO(FFProjectsCtrlSheetDtl cs) {
        RoadControlSheetDTO dto = new RoadControlSheetDTO();
        dto.setDetRowId(cs.getDetRowId());
        dto.setJobId(cs.getJobNoPoid());
        dto.setTruckNumber(cs.getTruckNumber());
        dto.setEta(cs.getEtaAta());
        dto.setWeight(cs.getWeight());
        dto.setCbm(cs.getCbm());
        dto.setDescription(cs.getDescription());
        return dto;
    }

    private AirPackageDTO mapAirPkgToDTO(FFManifestAirPkgDtl e) {
        AirPackageDTO dto = new AirPackageDTO();
        dto.setDetRowId(e.getDetRowId());
//        dto.setNoOfPacks(e.getNoOfPacks());
        dto.setPackUnit(e.getPackUnit());
        dto.setTotalWeight(e.getTotalWeight());
        dto.setTotalVolume(e.getTotalVolume());
        dto.setChargeableWeight(e.getChargeableWeight());
        dto.setDescription(e.getDescription());
        return dto;
    }

    private ContainerDTO mapContainerToDTO(FFManifestContainerDtl e) {
        ContainerDTO dto = new ContainerDTO();
        dto.setDetRowId(e.getDetRowId());
        dto.setContainerNo(e.getContainerNo());
        dto.setContainerSealNo(e.getContainerSealNo());
        dto.setContainerIsoCode(e.getContainerIsoCode());
        dto.setContainerSize(e.getContainerSize());
//        dto.setGrossWeight(e.getGrossWeight());
//        dto.setGrossVolume(e.getGrossVolume());
        dto.setNoOfPacks(e.getNoOfPacks());
        dto.setPackUnit(e.getPackUnit());
        return dto;
    }

    private BayanDTO mapBayanToDTO(FFManifestBayanDtl e) {
        return mapBayanToDTO(e, null);
    }

    private BayanDTO mapBayanToDTO(FFManifestBayanDtl e, String jobNo) {
        BayanDTO dto = new BayanDTO();
        dto.setJobId(e.getTransactionPoid());
        dto.setJobNo(jobNo);
        dto.setDetRowId(e.getDetRowId());
        dto.setBlAwbNumber(e.getBlAwbNumber());
        dto.setBayanNumber(e.getBayanNumber());
        dto.setBayanMode(e.getBayanMode());
        dto.setDutyAmount(e.getDutyAmount());
        dto.setVatAmount(e.getVatAmount());
        dto.setTotalPaidAmount(e.getTotalPaidAmount());
        dto.setExpiryDate(e.getExpiryDate() != null ? e.getExpiryDate().toLocalDate() : null);
        dto.setSubmittedDate(e.getSubmittedDate() != null ? e.getSubmittedDate().toLocalDate() : null);
        dto.setPaymentDate(e.getPaymentDate() != null ? e.getPaymentDate().toLocalDate() : null);
        return dto;
    }

    private TruckCargoDTO mapTruckToDTO(FFManifestTruckDtl e) {
        TruckCargoDTO dto = new TruckCargoDTO();
        dto.setDetRowId(e.getDetRowId());
        dto.setTruckNumber(e.getTruckNumber());
        dto.setBayanNumber(e.getBayanNumber());
        dto.setBayanCode(e.getBayanCode());
        return dto;
    }

    private ChargeDTO mapChargeToDTO(FFManifestChargesDtl e) {
        ChargeDTO dto = new ChargeDTO();
        dto.setDetRowId(e.getDetRowId());
        dto.setChargePoid(e.getChargePoid() != null ? e.getChargePoid().longValue() : null);
        dto.setQuantity(e.getQuantity());
        dto.setBuyingCurrencyCode(e.getCurrencyCode());
        dto.setCurrencyRate(e.getCurrencyExchange());
        dto.setBuyingUnitRate(e.getBuyingPercharge());
        dto.setBuyingTotalBhd(e.getTotalBuyingCharge());
        dto.setSellingUnitRate(e.getBillingPrecharge());
        dto.setSellingTotal(e.getTotalSellingCharge());
        dto.setTaxPoid(e.getTaxPoid() != null ? e.getTaxPoid().longValue() : null);
        dto.setTaxPercentage(e.getTaxPercentage());
        dto.setTaxAmount(e.getTaxAmount());
        dto.setRemarks(e.getRemarks());
        return dto;
    }

    public void updateProjectCharges(List<FFProjectsChargesDetailRequest> charges, Long transactionPoid) {
        String currentUser = UserContext.getUserName();
        LocalDateTime now = LocalDateTime.now();
        String docId = UserContext.getDocumentId();
        String docKeyPoid = transactionPoid.toString();

        List<FFProjectsChargesDtl> toSave = new ArrayList<>();
        List<FFProjectsChargesDtl> toUpdate = new ArrayList<>();
        List<Long> toDelete = new ArrayList<>();
        List<LogRequestDto<FFProjectsChargesDtl>> logRequests = new ArrayList<>();

        List<FFProjectsChargesDtl> existingCharges = projectsChargesDtlRepository.findByTransactionPoid(transactionPoid);
        long nextChargeDetRowId = existingCharges.stream().mapToLong(FFProjectsChargesDtl::getDetRowId).max().orElse(0L);

        for (FFProjectsChargesDetailRequest charge : charges) {
            switch (charge.getActionType().toUpperCase()) {
                case "ISCREATED":
                    toSave.add(FFProjectsChargesDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(++nextChargeDetRowId)
                            .quotationReferencePoid(charge.getQuotationReferencePoid())
                            .chargeDetailsPoid(charge.getChargePoid())
                            .printableChargeDescription(charge.getPrintableChargeDescription())
                            .quantity(charge.getQuantity())
                            .unit(charge.getUnit())
                            .buyingCurrencyCode(charge.getBuyingCurrencyCode())
                            .currencyRate(charge.getCurrencyRate())
                            .buyingUnitRate(charge.getBuyingUnitRate())
                            .buyingTotalBhd(charge.getBuyingTotalBhd())
                            .sellingUnitRate(charge.getSellingUnitRate())
                            .sellingTotal(charge.getSellingTotal())
                            .taxIdPoid(charge.getTaxIdPoid())
                            .taxPercentage(charge.getTaxPercentage())
                            .taxAmount(charge.getTaxAmount())
                            .sellingGrandTotal(charge.getSellingGrandTotal())
                            .sellingGrandTotalBhd(charge.getSellingGrandTotalBhd())
                            .marginBhd(charge.getMarginBhd())
                            .remarks(charge.getRemarks())
                            .createdBy(currentUser)
                            .createdDate(now)
                            .lastModifiedBy(currentUser)
                            .lastModifiedDate(now)
                            .build());
                    break;

                case "ISUPDATED":
                    FFProjectsChargesDtl existingCharge = projectsChargesDtlRepository
                            .findByTransactionPoidAndDetRowId(transactionPoid, charge.getDetRowId())
                            .orElseThrow(() -> new ResourceNotFoundException("Charge not found"));

                    FFProjectsChargesDtl oldCharge = FFProjectsChargesDtl.builder()
                            .transactionPoid(existingCharge.getTransactionPoid())
                            .detRowId(existingCharge.getDetRowId())
                            .quotationReferencePoid(existingCharge.getQuotationReferencePoid())
                            .chargeDetailsPoid(existingCharge.getChargeDetailsPoid())
                            .printableChargeDescription(existingCharge.getPrintableChargeDescription())
                            .quantity(existingCharge.getQuantity())
                            .unit(existingCharge.getUnit())
                            .buyingCurrencyCode(existingCharge.getBuyingCurrencyCode())
                            .currencyRate(existingCharge.getCurrencyRate())
                            .buyingUnitRate(existingCharge.getBuyingUnitRate())
                            .buyingTotalBhd(existingCharge.getBuyingTotalBhd())
                            .sellingUnitRate(existingCharge.getSellingUnitRate())
                            .sellingTotal(existingCharge.getSellingTotal())
                            .taxIdPoid(existingCharge.getTaxIdPoid())
                            .taxPercentage(existingCharge.getTaxPercentage())
                            .taxAmount(existingCharge.getTaxAmount())
                            .sellingGrandTotal(existingCharge.getSellingGrandTotal())
                            .sellingGrandTotalBhd(existingCharge.getSellingGrandTotalBhd())
                            .marginBhd(existingCharge.getMarginBhd())
                            .remarks(existingCharge.getRemarks())
                            .createdBy(existingCharge.getCreatedBy())
                            .createdDate(existingCharge.getCreatedDate())
                            .lastModifiedBy(existingCharge.getLastModifiedBy())
                            .lastModifiedDate(existingCharge.getLastModifiedDate())
                            .build();

                    existingCharge.setQuotationReferencePoid(charge.getQuotationReferencePoid());
                    existingCharge.setChargeDetailsPoid(charge.getChargePoid());
                    existingCharge.setPrintableChargeDescription(charge.getPrintableChargeDescription());
                    existingCharge.setQuantity(charge.getQuantity());
                    existingCharge.setUnit(charge.getUnit());
                    existingCharge.setBuyingCurrencyCode(charge.getBuyingCurrencyCode());
                    existingCharge.setCurrencyRate(charge.getCurrencyRate());
                    existingCharge.setBuyingUnitRate(charge.getBuyingUnitRate());
                    existingCharge.setBuyingTotalBhd(charge.getBuyingTotalBhd());
                    existingCharge.setSellingUnitRate(charge.getSellingUnitRate());
                    existingCharge.setSellingTotal(charge.getSellingTotal());
                    existingCharge.setTaxIdPoid(charge.getTaxIdPoid());
                    existingCharge.setTaxPercentage(charge.getTaxPercentage());
                    existingCharge.setTaxAmount(charge.getTaxAmount());
                    existingCharge.setSellingGrandTotal(charge.getSellingGrandTotal());
                    existingCharge.setSellingGrandTotalBhd(charge.getSellingGrandTotalBhd());
                    existingCharge.setMarginBhd(charge.getMarginBhd());
                    existingCharge.setRemarks(charge.getRemarks());
                    existingCharge.setLastModifiedBy(currentUser);
                    existingCharge.setLastModifiedDate(now);
                    toUpdate.add(existingCharge);

                    String logDetail = String.format("KeyId = TRANSACTION_POID: %s DET_ROW_ID: %s", transactionPoid, charge.getDetRowId());
                    logRequests.add(new LogRequestDto<>(oldCharge, existingCharge, FFProjectsChargesDtl.class, docId, docKeyPoid, logDetail));
                    break;

                case "ISDELETED":
                    toDelete.add(charge.getDetRowId());
                    loggingService.logDelete(charge, docId, docKeyPoid);
                    break;
            }
        }

        if (!toSave.isEmpty()) {
            projectsChargesDtlRepository.saveAll(toSave);
            toSave.forEach(e -> {
                String logDetail = String.format("Row Created on Charge Detail with detRowId: %s", e.getDetRowId());
                loggingService.createLogSummaryEntry(docId, docKeyPoid, logDetail);
            });
        }

        if (!toUpdate.isEmpty()) {
            projectsChargesDtlRepository.saveAll(toUpdate);
            if (!logRequests.isEmpty()) {
                loggingService.createLogBatch(logRequests);
            }
        }

        if (!toDelete.isEmpty()) {
            projectsChargesDtlRepository.deleteByTransactionPoidAndDetRowIdIn(transactionPoid, toDelete);
        }
    }

    public void updateProjectControlSheets(List<FFProjectsCtrlSheetDetailRequest> controlSheets, Long transactionPoid) {
        String currentUser = UserContext.getUserName();
        LocalDateTime now = LocalDateTime.now();
        String docId = UserContext.getDocumentId();
        String docKeyPoid = transactionPoid.toString();

        List<FFProjectsCtrlSheetDtl> toSave = new ArrayList<>();
        List<FFProjectsCtrlSheetDtl> toUpdate = new ArrayList<>();
        List<Long> toDelete = new ArrayList<>();
        List<LogRequestDto<FFProjectsCtrlSheetDtl>> logRequests = new ArrayList<>();

        List<FFProjectsCtrlSheetDtl> existingSheets = projectsCtrlSheetDtlRepository.findByTransactionPoid(transactionPoid);
        long nextSheetDetRowId = existingSheets.stream().mapToLong(FFProjectsCtrlSheetDtl::getDetRowId).max().orElse(0L);

        for (FFProjectsCtrlSheetDetailRequest ctrl : controlSheets) {
            switch (ctrl.getActionType().toUpperCase()) {
                case "ISCREATED":
                    toSave.add(FFProjectsCtrlSheetDtl.builder()
                            .transactionPoid(transactionPoid)
                            .detRowId(++nextSheetDetRowId)
                            .freightType(ctrl.getFreightType())
                            .jobNoPoid(ctrl.getJobNoPoid())
                            .origin(ctrl.getOriginPoid())
                            .destination(ctrl.getDestinationPoid())
                            .etd(ctrl.getEtd())
                            .etaAta(ctrl.getEtaAta())
                            .arrivalDate(ctrl.getArrivalDate())
                            .noOfPackages(ctrl.getNoOfPackages())
                            .weight(ctrl.getWeight())
                            .cbm(ctrl.getCbm())
                            .carrierPoid(ctrl.getCarrierPoid())
                            .line(ctrl.getLinePoid())
                            .truckNumber(ctrl.getTruckNumber())
                            .description(ctrl.getDescription())
                            .sailDate(ctrl.getSailDate())
                            .active("Y")
                            .createdBy(currentUser)
                            .createdDate(now)
                            .lastModifiedBy(currentUser)
                            .lastModifiedDate(now)
                            .build());
                    break;

                case "ISUPDATED":
                    FFProjectsCtrlSheetDtl existingCtrl = projectsCtrlSheetDtlRepository
                            .findByTransactionPoidAndDetRowId(transactionPoid, ctrl.getDetRowId())
                            .orElseThrow(() -> new ResourceNotFoundException("Control sheet not found"));

                    FFProjectsCtrlSheetDtl oldCtrl = FFProjectsCtrlSheetDtl.builder()
                            .transactionPoid(existingCtrl.getTransactionPoid())
                            .detRowId(existingCtrl.getDetRowId())
                            .freightType(existingCtrl.getFreightType())
                            .jobNoPoid(existingCtrl.getJobNoPoid())
                            .origin(existingCtrl.getOrigin())
                            .destination(existingCtrl.getDestination())
                            .etd(existingCtrl.getEtd())
                            .etaAta(existingCtrl.getEtaAta())
                            .arrivalDate(existingCtrl.getArrivalDate())
                            .noOfPackages(existingCtrl.getNoOfPackages())
                            .weight(existingCtrl.getWeight())
                            .cbm(existingCtrl.getCbm())
                            .carrierPoid(existingCtrl.getCarrierPoid())
                            .line(existingCtrl.getLine())
                            .truckNumber(existingCtrl.getTruckNumber())
                            .description(existingCtrl.getDescription())
                            .sailDate(existingCtrl.getSailDate())
                            .pod(existingCtrl.getPod())
                            .pol(existingCtrl.getPol())
                            .createdBy(existingCtrl.getCreatedBy())
                            .createdDate(existingCtrl.getCreatedDate())
                            .lastModifiedBy(existingCtrl.getLastModifiedBy())
                            .lastModifiedDate(existingCtrl.getLastModifiedDate())
                            .build();

                    if ("N".equals(ctrl.getActive()) && existingCtrl.getJobNoPoid() != null) {
                        throw new FFValidationException("Cannot deactivate control sheet: it is linked to a job.",
                                List.of(new ValidationError(null, "ACTIVE", "Cannot deactivate a control sheet that is linked to a job.")));
                    }

                    existingCtrl.setFreightType(ctrl.getFreightType());
                    existingCtrl.setJobNoPoid(ctrl.getJobNoPoid());
                    existingCtrl.setOrigin(ctrl.getOriginPoid());
                    existingCtrl.setDestination(ctrl.getDestinationPoid());
                    existingCtrl.setEtd(ctrl.getEtd());
                    existingCtrl.setEtaAta(ctrl.getEtaAta());
                    existingCtrl.setArrivalDate(ctrl.getArrivalDate());
                    existingCtrl.setNoOfPackages(ctrl.getNoOfPackages());
                    existingCtrl.setWeight(ctrl.getWeight());
                    existingCtrl.setCbm(ctrl.getCbm());
                    existingCtrl.setCarrierPoid(ctrl.getCarrierPoid());
                    existingCtrl.setLine(ctrl.getLinePoid());
                    existingCtrl.setTruckNumber(ctrl.getTruckNumber());
                    existingCtrl.setDescription(ctrl.getDescription());
                    existingCtrl.setSailDate(ctrl.getSailDate());
                    existingCtrl.setPod(ctrl.getSfPOD());
                    existingCtrl.setPol(ctrl.getSfPOL());
                    if (ctrl.getActive() != null) {
                        existingCtrl.setActive(ctrl.getActive());
                    }
                    existingCtrl.setLastModifiedBy(currentUser);
                    existingCtrl.setLastModifiedDate(now);
                    toUpdate.add(existingCtrl);

                    String logDetail = String.format("KeyId = TRANSACTION_POID: %s DET_ROW_ID: %s", transactionPoid, ctrl.getDetRowId());
                    logRequests.add(new LogRequestDto<>(oldCtrl, existingCtrl, FFProjectsCtrlSheetDtl.class, docId, docKeyPoid, logDetail));
                    break;

                case "ISDELETED":
                    toDelete.add(ctrl.getDetRowId());
                    loggingService.logDelete(ctrl, docId, docKeyPoid);
                    break;
            }
        }

        if (!toSave.isEmpty()) {
            projectsCtrlSheetDtlRepository.saveAll(toSave);
            toSave.forEach(e -> {
                String logDetail = String.format("Row Created on Control Sheet with detRowId: %s", e.getDetRowId());
                loggingService.createLogSummaryEntry(docId, docKeyPoid, logDetail);
            });
        }

        if (!toUpdate.isEmpty()) {
            projectsCtrlSheetDtlRepository.saveAll(toUpdate);
            if (!logRequests.isEmpty()) {
                loggingService.createLogBatch(logRequests);
            }
        }

        if (!toDelete.isEmpty()) {
            projectsCtrlSheetDtlRepository.deleteByTransactionPoidAndDetRowIdIn(transactionPoid, toDelete);
        }
    }
}
