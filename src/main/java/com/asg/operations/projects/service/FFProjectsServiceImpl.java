package com.asg.operations.projects.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.dto.request.LogRequestDto;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        Long transactionPoid = projectsHdr.getTransactionPoid();
        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), transactionPoid.toString());


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
        BeanUtils.copyProperties(request, backUpProjectsHdr);

        ProjectMapper.applyUpdate(request, existingProjectsHdr);

        loggingService.logChanges(backUpProjectsHdr, existingProjectsHdr, FFProjectsHdr.class, UserContext.getDocumentId(),
                transactionPoid.toString(), LogDetailsEnum.MODIFIED, "Project POID");

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
    public Map<String, Object> loadQuotationDetails(Long quotationPoid) {
        return projectsStoredProcRepository.loadProjectsQuotation(quotationPoid, null);
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
    public List<UpcomingJobDTO> getUpcomingJobsList(Long transactionPoid, LocalDate fromDate, LocalDate toDate) {
        LocalDate from = fromDate != null ? fromDate : LocalDate.now();
        LocalDate to = toDate != null ? toDate : from.plusDays(30);
        return freightJobProjectionRepository.findUpcomingJobs(transactionPoid, from, to).stream()
                .map(this::mapToUpcomingJobDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FreightJobsSummaryDTO getAllFreightsSummary(Long transactionPoid, FreightFilterRequest filter) {
        List<FreightJobSummaryProjection> allJobs = freightJobProjectionRepository.findAllFreightJobs(transactionPoid);
        
        List<FreightSummaryDTO> allFreights = allJobs.stream().map(this::mapToFreightSummaryDTO).collect(Collectors.toList());
        List<AirFreightSummaryDTO> airFreights = getAirFreightsSummary(transactionPoid);
        List<SeaFreightSummaryDTO> seaFreights = getSeaFreightsSummary(transactionPoid);
        List<RoadFreightSummaryDTO> roadFreights = getRoadFreightsSummary(transactionPoid);
        List<UpcomingJobDTO> upcomingJobs = getUpcomingJobsList(transactionPoid, null, null);
        
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
    public List<AirFreightSummaryDTO> getAirFreightsSummary(Long transactionPoid) {
        return freightJobProjectionRepository.findAirFreightJobs(transactionPoid).stream()
                .map(this::mapToAirSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeaFreightSummaryDTO> getSeaFreightsSummary(Long transactionPoid) {
        return freightJobProjectionRepository.findSeaFreightJobs(transactionPoid).stream()
                .map(this::mapToSeaSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoadFreightSummaryDTO> getRoadFreightsSummary(Long transactionPoid) {
        return freightJobProjectionRepository.findRoadFreightJobs(transactionPoid).stream()
                .map(this::mapToRoadSummaryDTO)
                .collect(Collectors.toList());
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
        return dto;
    }

    private UpcomingJobDTO mapToUpcomingJobDTO(FreightJobSummaryProjection p) {
        UpcomingJobDTO dto = new UpcomingJobDTO();
        dto.setJobId(p.getJobId());
        dto.setJobNo(p.getJobNo());
        dto.setFreightType(p.getFreightMode());
        dto.setLine(p.getLine());
        dto.setEta(p.getEtaAta());
        dto.setPol(p.getPol());
        dto.setPod(p.getPod());
        dto.setDescription(p.getDescription());
        dto.setCbm(p.getCbm());
        dto.setPackages(p.getPackages());
        dto.setWeight(p.getWeight());
        dto.setJobStatus(p.getJobStatus());
        dto.setCanCreateJob(false);
        return dto;
    }

    private AirFreightSummaryDTO mapToAirSummaryDTO(AirFreightJobProjection p) {
        AirFreightSummaryDTO dto = new AirFreightSummaryDTO();
        dto.setJobId(p.getJobId());
        dto.setJobNo(p.getJobNo());
        dto.setMawbNo(p.getMawbNo());
        dto.setHawbNo(p.getHawbNo());
        dto.setFlightNo(p.getFlightNo());
        dto.setEtd(p.getEtd());
        dto.setEta(p.getEtaAta());
        dto.setNoOfPackages(p.getNoOfPackages());
        dto.setWeight(p.getWeight());
        dto.setCbm(p.getCbm());
        dto.setDescription(p.getDescription());
        dto.setJobStatus(p.getJobStatus());
        dto.setDocumentStatus(p.getDocumentStatus());
        return dto;
    }

    private SeaFreightSummaryDTO mapToSeaSummaryDTO(SeaFreightJobProjection p) {
        SeaFreightSummaryDTO dto = new SeaFreightSummaryDTO();
        dto.setJobId(p.getJobId());
        dto.setJobNo(p.getJobNo());
        dto.setVesselName(p.getVesselName());
        dto.setPol(p.getPol());
        dto.setPod(p.getPod());
        dto.setMasterBlNo(p.getMasterBlNo());
        dto.setHouseBlNo(p.getHouseBlNo());
        dto.setEta(p.getEtaAta());
        dto.setEtd(p.getEtd());
        dto.setArrivalDate(p.getArrivalDate());
        dto.setSailDate(p.getSailDate());
        dto.setWeight(p.getWeight());
        dto.setCbm(p.getCbm());
        dto.setDescription(p.getDescription());
        dto.setJobStatus(p.getJobStatus());
        dto.setDocumentStatus(p.getDocumentStatus());
        return dto;
    }

    private RoadFreightSummaryDTO mapToRoadSummaryDTO(RoadFreightJobProjection p) {
        RoadFreightSummaryDTO dto = new RoadFreightSummaryDTO();
        dto.setJobId(p.getJobId());
        dto.setJobNo(p.getJobNo());
        dto.setBlAwbNumber(p.getBlAwbNumber());
        dto.setTransportFrom(p.getTransportFrom());
        dto.setTransportTo(p.getTransportTo());
        dto.setEta(p.getEta());
        dto.setWeight(p.getWeight());
        dto.setCbm(p.getCbm());
        dto.setDescription(p.getDescription());
        dto.setJobStatus(p.getJobStatus());
        dto.setDocumentStatus(p.getDocumentStatus());
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
        BayanDTO dto = new BayanDTO();
        dto.setDetRowId(e.getDetRowId());
        dto.setBayanNumber(e.getBayanNumber());
        dto.setBayanMode(e.getBayanMode());
        dto.setDutyAmount(e.getDutyAmount());
//        dto.setVatAmount(e.getVatAmount());
        dto.setTotalPaidAmount(e.getTotalPaidAmount());
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

                    FFProjectsChargesDtl oldCharge = new FFProjectsChargesDtl();
                    BeanUtils.copyProperties(existingCharge, oldCharge);

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

                    FFProjectsCtrlSheetDtl oldCtrl = new FFProjectsCtrlSheetDtl();
                    BeanUtils.copyProperties(existingCtrl, oldCtrl);

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
