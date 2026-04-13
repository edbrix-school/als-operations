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
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class FFProjectsServiceImpl implements FFProjectsService {

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
    private final LoggingService loggingService;
    private final ProjectMapper mapper;
    private final DocumentSearchService documentSearchService;
    private final LovDataService lovDataService;
    private final EntityManager entityManager;

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
        List<FFProjectsCtrlSheetDtl> ctrlSheetDetails = projectsCtrlSheetDtlRepository.findByTransactionPoid(transactionPoid);

        return  mapper.mapToResponse(projectsHdr, chargeDetails, ctrlSheetDetails);
    }

    @Override
    @Transactional
    public FFProjectsResponse createProject(FFProjectsRequest request) {

        FFProjectsHdr projectsHdr = ProjectMapper.buildCreateProject(request);

        projectsHdr = projectsHdrRepository.save(projectsHdr);
        entityManager.refresh(projectsHdr);
        Long transactionPoid = projectsHdr.getTransactionPoid();
        
        String key = transactionPoid.toString();
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), key, String.format("%s %s", LogDetailsEnum.CREATED, projectsHdr.getDocRef()));


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
        
        List<FFProjectsCtrlSheetDtl> details = projectsCtrlSheetDtlRepository.findByTransactionPoid(transactionPoid);
        return details.stream()
                .map(mapper::mapCtrlSheetDetailToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FFProjectsCtrlSheetDetailResponse> getControlSheetsByProject(Long transactionPoid, String freightType) {
        List<FFProjectsCtrlSheetDtl> details;
        
        if (freightType != null && !freightType.isEmpty()) {
            details = projectsCtrlSheetDtlRepository.findByTransactionPoidAndFreightType(transactionPoid, freightType);
        } else {
            details = projectsCtrlSheetDtlRepository.findByTransactionPoid(transactionPoid);
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
    @Transactional(readOnly = true)
    public List<UpcomingJobDTO> getUpcomingJobsList(Long transactionPoid, LocalDate fromDate, LocalDate toDate, String sortBy, String sortDir) {
        LocalDate from = fromDate != null ? fromDate : LocalDate.now();
        LocalDate to = toDate != null ? toDate : from.plusDays(30);
        List<FFProjectsCtrlSheetDtl> controlSheets = projectsCtrlSheetDtlRepository.findByTransactionPoid(transactionPoid);
        List<Long> linkedJobIds = controlSheets.stream()
                .map(FFProjectsCtrlSheetDtl::getJobNoPoid)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, FFManifestHdr> manifestById = manifestHdrRepository.findAllById(linkedJobIds).stream()
                .collect(Collectors.toMap(FFManifestHdr::getTransactionPoid, Function.identity()));

        List<UpcomingJobDTO> result = controlSheets.stream()
                .filter(cs -> cs.getEtaAta() != null)
                .filter(cs -> !cs.getEtaAta().isBefore(from) && !cs.getEtaAta().isAfter(to))
                .map(cs -> mapToUpcomingJobDTO(cs, manifestById.get(cs.getJobNoPoid())))
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
        List<FFProjectsCtrlSheetDtl> controlSheets = projectsCtrlSheetDtlRepository.findByTransactionPoid(transactionPoid);
        List<Long> linkedJobIds = controlSheets.stream()
                .map(FFProjectsCtrlSheetDtl::getJobNoPoid)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, FFManifestHdr> manifestById = manifestHdrRepository.findAllById(linkedJobIds).stream()
                .collect(Collectors.toMap(FFManifestHdr::getTransactionPoid, Function.identity()));

        List<FreightSummaryDTO> result = controlSheets.stream()
                .filter(cs -> fromDate == null || (cs.getEtaAta() != null && !cs.getEtaAta().isBefore(fromDate)))
                .filter(cs -> toDate == null || (cs.getEtaAta() != null && !cs.getEtaAta().isAfter(toDate)))
                .map(cs -> mapToFreightSummaryDTO(cs, manifestById.get(cs.getJobNoPoid())))
                .collect(Collectors.toList());
        applySorting(result, sortBy, sortDir);
        assignDetRowIds(result, FreightSummaryDTO::setDetRowId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobStatusPendingBillDTO> getJobStatusPendingBills(Long transactionPoid, String viewBy, LocalDate fromDate, LocalDate toDate, String sortBy, String sortDir) {
        String normalizedViewBy = viewBy == null ? "principal" : viewBy.trim().toLowerCase(Locale.ROOT);
        LocalDate from = fromDate != null ? fromDate : LocalDate.now();
        LocalDate to = toDate != null ? toDate : from.plusDays(30);

        Map<Long, FFProjectsCtrlSheetDtl> controlSheetByJobId = projectsCtrlSheetDtlRepository.findByTransactionPoid(transactionPoid).stream()
                .filter(cs -> cs.getJobNoPoid() != null)
                .filter(cs -> cs.getEtaAta() != null)
                .filter(cs -> !cs.getEtaAta().isBefore(from) && !cs.getEtaAta().isAfter(to))
                .collect(Collectors.toMap(
                        FFProjectsCtrlSheetDtl::getJobNoPoid,
                        Function.identity(),
                        (existing, ignored) -> existing,
                        LinkedHashMap::new
                ));

        if (controlSheetByJobId.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> jobIds = new ArrayList<>(controlSheetByJobId.keySet());
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

        List<JobStatusPendingBillDTO> result = controlSheetByJobId.entrySet().stream()
                .map(entry -> mapToJobStatusPendingBillDTO(
                        entry.getValue(),
                        manifestById.get(entry.getKey()),
                        bookedAmountByJobId.getOrDefault(entry.getKey(), BigDecimal.ZERO),
                        normalizedViewBy))
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
        dto.setDestination(job.getDestination());
        dto.setCarrier(job.getCarrierCode());
        dto.setEtd(job.getEtd());
        dto.setEta(job.getEtaAta());
        dto.setJobStatus(job.getJobStatus());
        dto.setDocumentStatus(job.getDocumentStatus());

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
        dto.setPod(job.getPod());
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
            dto.setOfoqManifestRef(header.getOfoqMnfRef());
            dto.setRadioActive(header.getRadioAction());
        }

        // Container fields
        if (container != null) {
            dto.setContainerNo(container.getContainerNo());
            dto.setContainerType(container.getContainerSize());
            dto.setSealNumber(container.getSealNo());
            dto.setCargoDescription(container.getCargoDescription());
            dto.setQty(container.getQuantity() != null ? container.getQuantity().doubleValue() : null);
            dto.setQtyPackages(container.getNoOfPacks() != null ? container.getNoOfPacks().doubleValue() : null);
            dto.setWeight(container.getNetWeight() != null ? container.getNetWeight().doubleValue() : null);
            dto.setCbm(container.getNetVolume() != null ? container.getNetVolume().doubleValue() : null);
            dto.setAppointmentDate(container.getCargoCollectionDate() != null ? container.getCargoCollectionDate().toLocalDate() : null);
            dto.setDeliveryDate(container.getDeliveryDate() != null ? container.getDeliveryDate().toLocalDate() : null);
            dto.setDetention(container.getDetention());
            dto.setDestuffingFull(container.getUnloadDate() != null ? "Destuffed" : "Full");
            dto.setDocStatus(container.getDocStatus());
            dto.setRemarks(container.getRemarks());
        } else {
            // Use header-level totals
            dto.setWeight(job.getWeight());
            dto.setCbm(job.getCbm());
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
        dto.setJobStatus(p.getJobStatus());
        dto.setPrincipalPoid(p.getPrincipalPoid());
        dto.setPrincipalLov(getLov(p.getPrincipalPoid(), "PRINCIPAL_MASTER"));
        dto.setPackages(p.getPackages());
        dto.setBlAwbNo(p.getBlAwbNo());
        dto.setOrigin(p.getOrigin());
        dto.setDestination(p.getDestination());
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

    private JobStatusPendingBillDTO mapToJobStatusPendingBillDTO(FFProjectsCtrlSheetDtl cs, FFManifestHdr manifest,
                                                                 BigDecimal bookedAmount, String viewBy) {
        JobStatusPendingBillDTO dto = new JobStatusPendingBillDTO();
        dto.setJobId(cs.getJobNoPoid());
        dto.setJobNo(manifest != null ? manifest.getFfJobNo() : null);
        dto.setBlNo(manifest != null ? manifest.getMasterBlNo() : null);
        dto.setEtaAta(cs.getEtaAta());
        dto.setViewBy(viewBy);

        if ("customer".equals(viewBy)) {
            Long customerPoid = manifest != null && manifest.getBillToCustomerPoid() != null
                    ? manifest.getBillToCustomerPoid().longValue() : null;
            dto.setPartyPoid(customerPoid);
            dto.setPartyLov(getLov(customerPoid, "CUSTOMER_SUPPLIER_MASTER"));
        } else {
            Long principalPoid = manifest != null && manifest.getPrincipalPoid() != null
                    ? manifest.getPrincipalPoid().longValue() : null;
            dto.setPartyPoid(principalPoid);
            dto.setPartyLov(getLov(principalPoid, "PRINCIPAL_MASTER"));
        }

        dto.setMode(manifest != null ? manifest.getShipmentMode() : cs.getFreightType());
        dto.setJobStatus(manifest != null ? manifest.getJobStatus() : null);
        dto.setCompletedOn(manifest != null && manifest.getJobClosedDate() != null
                ? manifest.getJobClosedDate().toLocalDate() : null);
        dto.setBookedAmount(bookedAmount);
        return dto;
    }

    private LovGetListDto getLov(Long poid, String lovName) {
        if (poid == null) {
            return null;
        }
        return lovDataService.getDetailsByPoidAndLovNameFast(poid, lovName);
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

    private UpcomingJobDTO mapToUpcomingJobDTO(FFProjectsCtrlSheetDtl cs, FFManifestHdr manifest) {
        UpcomingJobDTO dto = new UpcomingJobDTO();
        dto.setJobId(cs.getJobNoPoid());
        dto.setJobNo(manifest != null ? manifest.getFfJobNo() : null);
        dto.setBlAwbNo(manifest != null ? manifest.getMasterBlNo() : null);
        dto.setFreightType(cs.getFreightType());
        dto.setLine(cs.getLine() != null ? String.valueOf(cs.getLine()) : null);
        dto.setEta(cs.getEtaAta());
        dto.setEtd(cs.getEtd());
        dto.setPol(cs.getPol());
        dto.setPod(cs.getPod());
        dto.setOrigin(cs.getOrigin() != null ? String.valueOf(cs.getOrigin()) : null);
        dto.setDestination(cs.getDestination() != null ? String.valueOf(cs.getDestination()) : null);
        dto.setDescription(cs.getDescription());
        dto.setCbm(cs.getCbm());
        dto.setPackages(cs.getNoOfPackages());
        dto.setWeight(cs.getWeight());
        dto.setJobStatus(manifest != null ? manifest.getJobStatus() : null);
        dto.setCanCreateJob(cs.getJobNoPoid() == null);
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
