package com.asg.operations.projectjob.service.impl;

import com.asg.common.lib.dto.*;
import com.asg.common.lib.dto.request.LogRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.LovDataService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.operations.common.entity.GlobalAddressDetails;
import com.asg.operations.common.entity.GlobalAddressMaster;
import com.asg.operations.common.repository.GlobalAddressDetailsRepository;
import com.asg.operations.common.repository.GlobalAddressMasterRepository;
import com.asg.operations.commonlov.dto.LovItem;
import com.asg.operations.crew.dto.ValidationError;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.exceptions.ValidationException;
import com.asg.operations.projectjob.dto.*;
import com.asg.operations.projectjob.entity.*;
import com.asg.operations.projectjob.repository.*;
import com.asg.operations.projectjob.service.ProjectJobService;
import com.asg.operations.projectjob.util.ProjectJobMapper;
import com.asg.operations.projectjob.util.TriConsumer;
import com.asg.operations.projects.entity.FFProjectsCtrlSheetDtl;
import com.asg.operations.projects.repository.FFProjectsCtrlSheetDtlRepository;
import jakarta.validation.Valid;
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
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectJobServiceImpl implements ProjectJobService {
    private final FFManifestHdrRepository hdrRepository;
    private final FFManifestChargesDtlRepository chargesRepository;
    private final FFManifestAirPkgDtlRepository airPkgRepository;
    private final FFManifestBayanDtlRepository bayanRepository;
    private final FFManifestContainerDtlRepository containerRepository;
    private final FFManifestTruckDtlRepository truckRepository;
    private final ProjectJobStoredProcRepository spRepostirory;
    private final LoggingService loggingService;
    private final LovDataService lovDataService;
    private final DocumentDeleteService documentDeleteService;
    private final DocumentSearchService documentSearchService;
    private final FFProjectsCtrlSheetDtlRepository ctrlSheetDtlRepository;
    private final GlobalAddressMasterRepository addressMasterRepository;
    private final GlobalAddressDetailsRepository addressDetailsRepository;
    private final ProjectJobMapper projectJobMapper;

    private static final String TRANSACTION_POID = "TRANSACTION_POID";

    @Override
    @Transactional
    public ProjectJobResponse create(ProjectJobRequest request) {

        Long controlSheetTxnPoid = request.getControlSheetTransactionPoid();
        Long controlSheetDetRowId = request.getControlSheetDetRowId();

        FFProjectsCtrlSheetDtl ctrlSheetRow = null;
        if (controlSheetTxnPoid != null && controlSheetDetRowId != null) {
            ctrlSheetRow = ctrlSheetDtlRepository
                    .findByTransactionPoidAndDetRowId(controlSheetTxnPoid, controlSheetDetRowId)
                    .orElseThrow(() -> new ResourceNotFoundException("Control Sheet", "id",
                            controlSheetTxnPoid + "/" + controlSheetDetRowId));

            if (ctrlSheetRow.getJobNoPoid() != null) {
                List<ValidationError> errors = new ArrayList<>();
                errors.add(new ValidationError(0, "controlSheetTransactionPoid",
                        "This control sheet row already has a job assigned"));
                throw new ValidationException("Validation errors occurred", errors);
            }
        }

        FFManifestHdr hdr = new FFManifestHdr();
        projectJobMapper.mapHdrFromDto(request, hdr);
        Long groupPoid = UserContext.getGroupPoid();
        Long companyPoid = UserContext.getCompanyPoid();

        hdr.setCompanyPoid(companyPoid);
        hdr.setGroupPoid(groupPoid);
        hdr.setDocId(request.getDocId());

        // saveAndFlush ensures the INSERT hits the DB immediately so the trigger runs
        FFManifestHdr savedHdr = hdrRepository.saveAndFlush(hdr);

        // Refresh entity to pick up the trigger-generated ffJobNo
        FFManifestHdr refreshedHdr = hdrRepository.findById(savedHdr.getTransactionPoid())
                .orElseThrow(() -> new RuntimeException("Job not found after save"));

        saveDetails(refreshedHdr.getTransactionPoid(), request.getAirPackages(), request.getBayanDetails(), request.getCharges(),
                request.getContainers(), request.getTruckDetails());

        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), refreshedHdr.getTransactionPoid().toString(), "Project Job Created");

        if (ctrlSheetRow != null) {
            ctrlSheetRow.setJobNoPoid(refreshedHdr.getTransactionPoid());
            ctrlSheetDtlRepository.save(ctrlSheetRow);

            String ctrlDocId = request.getControlSheetDocId();
            if (ctrlDocId != null && !ctrlDocId.isBlank()) {
                loggingService.createLogSummaryEntry(ctrlDocId, controlSheetTxnPoid.toString(), "Job number updated from Project Job creation");
            }
        }

        return getById(refreshedHdr.getTransactionPoid());
    }

    @Override
    @Transactional
    public ProjectJobResponse update(Long transactionPoid, ProjectJobRequest request) {

        FFManifestHdr hdr = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        FFManifestHdr oldHdr = new FFManifestHdr();
        BeanUtils.copyProperties(hdr, oldHdr);

        projectJobMapper.mapHdrFromDto(request, hdr);

        hdrRepository.save(hdr);

        saveDetails(hdr.getTransactionPoid(), request.getAirPackages(), request.getBayanDetails(), request.getCharges(),
                request.getContainers(), request.getTruckDetails());

        loggingService.logChanges(oldHdr, hdr, FFManifestHdr.class, UserContext.getDocumentId(), hdr.getTransactionPoid().toString(), LogDetailsEnum.MODIFIED, TRANSACTION_POID);

        return getById(transactionPoid);
    }

    private <D extends BaseDetailDto, E extends BaseDetailEntity> void processDetails(Long transactionPoid,
                                                                                      List<D> details, Function<Long, Long> getMaxRowIdFn, Supplier<E> entitySupplier,
                                                                                      TriConsumer<D, E, Long> mapperFn, BiFunction<Long, Long, Optional<E>> findFn,
                                                                                      Function<List<E>, List<E>> saveAllFn, BiConsumer<Long, List<Long>> deleteFn, String entityName,
                                                                                      String docId, String docKeyPoid) {

        if (details == null || details.isEmpty())
            return;

        Long maxDetRowId = getMaxRowIdFn.apply(transactionPoid);

        List<E> toSave = new ArrayList<>();
        List<E> toUpdate = new ArrayList<>();
        List<Long> toDelete = new ArrayList<>();
        List<LogRequestDto<E>> logRequests = new ArrayList<>();

        for (D dto : details) {

            if (dto.getActionType() == null || dto.getActionType().trim().isEmpty()) {
                List<ValidationError> validationErrors = new ArrayList<>();
                validationErrors.add(new ValidationError(
                        0,
                        "actionType",
                        "ActionType cannot be null"
                ));
                throw new ValidationException("Validation errors occurred", validationErrors);
            }

            String action = dto.getActionType().toUpperCase();
            Long detRowId = dto.getDetRowId();

            switch (action) {

                case "ISCREATED":

                    E newEntity = entitySupplier.get();
                    mapperFn.accept(dto, newEntity, transactionPoid);

                    newEntity.setDetRowId(detRowId != null ? detRowId : ++maxDetRowId);

                    toSave.add(newEntity);
                    break;

                case "ISUPDATED":

                    E existing = findFn.apply(transactionPoid, detRowId)
                            .orElseThrow(() -> new ResourceNotFoundException(entityName, "detRowId", detRowId));

                    E oldEntity = entitySupplier.get();
                    BeanUtils.copyProperties(existing, oldEntity);

                    mapperFn.accept(dto, existing, transactionPoid);

                    toUpdate.add(existing);
                    logRequests.add(new LogRequestDto<>(oldEntity, existing, (Class<E>) existing.getClass(), docId,
                            docKeyPoid, entityName + " DET_ROW_ID: " + detRowId));
                    break;

                case "ISDELETED":

                    toDelete.add(detRowId);
                    loggingService.logDelete(dto, docId, docKeyPoid);
                    break;

                default:
                    break;
            }
        }

        if (!toSave.isEmpty()) {
            List<E> saved = saveAllFn.apply(toSave);
            saved.forEach(e -> loggingService.createLogSummaryEntry(docId, docKeyPoid, entityName + " created"));
        }

        if (!toUpdate.isEmpty()) {
            saveAllFn.apply(toUpdate);
            if (!logRequests.isEmpty())
                loggingService.createLogBatch(logRequests);
        }

        if (!toDelete.isEmpty())
            deleteFn.accept(transactionPoid, toDelete);
    }

    /*
     * ==================== SAVE ALL DETAILS=====================
     */

    private void saveDetails(Long transactionPoid, List<ProjectJobAirPkgDtoRequest> airpkgDetails,
                             List<ProjectJobBayanDtoRequest> bayanDetails, List<ProjectJobChargesDtoRequest> chargeDetails,
                             List<ProjectJobContainerDtoRequest> containerDetails, List<ProjectJobTruckDtoRequest> truckDetails) {

        String docId = UserContext.getDocumentId();
        String docKeyPoid = transactionPoid.toString();

        processDetails(transactionPoid, airpkgDetails, airPkgRepository::getMaxDetRowId, FFManifestAirPkgDtl::new,
                projectJobMapper::mapAirPkgFromDto, airPkgRepository::findByTransactionPoidAndDetRowId, airPkgRepository::saveAll,
                airPkgRepository::deleteByTransactionPoidAndDetRowIdIn, "AIR PKG", docId, docKeyPoid);

        processDetails(transactionPoid, bayanDetails, bayanRepository::getMaxDetRowId, FFManifestBayanDtl::new,
                projectJobMapper::mapBayanFromDto, bayanRepository::findByTransactionPoidAndDetRowId, bayanRepository::saveAll,
                bayanRepository::deleteByTransactionPoidAndDetRowIdIn, "BAYAN", docId, docKeyPoid);

        processDetails(transactionPoid, chargeDetails, chargesRepository::getMaxDetRowId, FFManifestChargesDtl::new,
                projectJobMapper::mapChargesFromDto, chargesRepository::findByTransactionPoidAndDetRowId,
                chargesRepository::saveAll, chargesRepository::deleteByTransactionPoidAndDetRowIdIn, "CHARGES", docId,
                docKeyPoid);

        processDetails(transactionPoid, containerDetails, containerRepository::getMaxDetRowId,
                FFManifestContainerDtl::new, projectJobMapper::mapContainerFromDto,
                containerRepository::findByTransactionPoidAndDetRowId, containerRepository::saveAll,
                containerRepository::deleteByTransactionPoidAndDetRowIdIn, "CONTAINER", docId, docKeyPoid);

        processDetails(transactionPoid, truckDetails, truckRepository::getMaxDetRowId, FFManifestTruckDtl::new,
                projectJobMapper::mapTruckFromDto, truckRepository::findByTransactionPoidAndDetRowId, truckRepository::saveAll,
                truckRepository::deleteByTransactionPoidAndDetRowIdIn, "CONTAINER", docId, docKeyPoid);
    }

    @Override
    public ProjectJobResponse getById(Long transactionPoid) {

        FFManifestHdr hdr = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new RuntimeException("Not found"));

        ProjectJobResponse response = new ProjectJobResponse();

        projectJobMapper.toHdrDto(hdr, response);

        Optional.ofNullable(response.getProjectPoid())
                .map(spRepostirory::callProjectsLoadInJobsProc)
                .map(ProjectLoadInJobsProcResponse::getHeader)
                .filter(list -> list != null && !list.isEmpty())
                .map(list -> list.get(0))
                .map(h -> h.getProjectCustomerPoid())
                .ifPresent(response::setProjectCustomerPoid);

        List<FFManifestAirPkgDtl> airPkg = airPkgRepository.findByTransactionPoid(transactionPoid);
        List<FFManifestBayanDtl> bayan = bayanRepository.findByTransactionPoid(transactionPoid);
        List<FFManifestChargesDtl> charges = chargesRepository.findByTransactionPoid(transactionPoid);
        List<FFManifestContainerDtl> containers = containerRepository.findByTransactionPoid(transactionPoid);
        List<FFManifestTruckDtl> trucks = truckRepository.findByTransactionPoid(transactionPoid);

        List<ProjectJobAirPkgDto> airPkgDto = new ArrayList<>();
        airPkg.forEach(entity -> {
            ProjectJobAirPkgDto dto = new ProjectJobAirPkgDto();
            projectJobMapper.toAirPkgDto(entity, dto);
            airPkgDto.add(dto);

        });

        List<ProjectJobBayanDto> bayanDto = new ArrayList<>();
        bayan.forEach(entity -> {
            ProjectJobBayanDto dto = new ProjectJobBayanDto();
            projectJobMapper.toBayanDto(entity, dto);
            bayanDto.add(dto);

        });

        List<ProjectJobChargesDto> chargesDto = new ArrayList<>();
        charges.forEach(entity -> {
            ProjectJobChargesDto dto = new ProjectJobChargesDto();
            projectJobMapper.toChargesDto(entity, dto);
            if (entity.getHouseBlPoid() != null) {
                LovGetListDto lov = lovDataService.getDetailsByPoidAndLovNameFast(
                        entity.getHouseBlPoid().longValue(),
                        "HOUSE_BL_LOV"
                );
                dto.setHouseBlPoidDel(lov);
            }
            chargesDto.add(dto);

        });

        List<ProjectJobContainerDto> conationersDto = new ArrayList<>();
        containers.forEach(entity -> {
            ProjectJobContainerDto dto = new ProjectJobContainerDto();
            projectJobMapper.toContainerDto(entity, dto);
            conationersDto.add(dto);

        });

        List<ProjectJobTruckDto> trucksDto = new ArrayList<>();
        trucks.forEach(entity -> {
            ProjectJobTruckDto dto = new ProjectJobTruckDto();
            projectJobMapper.toTruckDto(entity, dto);
            trucksDto.add(dto);

        });

        response.setAirPackages(airPkgDto);
        response.setBayanDetails(bayanDto);
        response.setCharges(chargesDto);
        response.setContainers(conationersDto);
        response.setTruckDetails(trucksDto);

        return response;
    }

    @Override
    public LovItem getNotifyById(BigDecimal notifyPoid) {

        if (notifyPoid == null) {
            return new LovItem(null, "", "", "", null, 0);
        }

        GlobalAddressMaster addressMaster = addressMasterRepository
                .findById(notifyPoid.longValue())
                .orElse(new GlobalAddressMaster());

        GlobalAddressDetails addressDetails = addressDetailsRepository
                .findById(notifyPoid)
                .orElse(new GlobalAddressDetails());

        String address = String.format(
                "%s, Type-%s, CONTACT-%s, TEL1-%s TEL2-%s, MOB-%s EMAIL1-%s EMAIL2-%s",
                safe(addressMaster.getAddressName()),
                safe(addressDetails.getAddressType()),
                safe(addressDetails.getContactPerson()),
                safe(addressDetails.getOffTel1()),
                safe(addressDetails.getOffTel2()),
                safe(addressDetails.getMobile()),
                safe(addressDetails.getEmail1()),
                safe(addressDetails.getEmail2())
        );

        return new LovItem(
                notifyPoid.longValue(),
                addressDetails.getAddressPoid() != null ? addressDetails.getAddressPoid().toString() : "",
                address,
                address,
                addressMaster.getAddressMasterPoid(),
                0
        );
    }

    private String safe(Object value) {
        return value != null ? value.toString() : "";
    }

    @Override
    public void deleteById(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid,
                           @Valid DeleteReasonDto deleteReasonDto) {

        FFManifestHdr hdr = hdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Project Job", "transactionPoid", transactionPoid));

        documentDeleteService.deleteDocument(transactionPoid, "FF_MANIEST_HDR", TRANSACTION_POID, deleteReasonDto,
                hdr.getTransactionDate());

    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAllProjectJobsWithFilters(String documentId, FilterRequestDto filterRequestDto,
                                                            Pageable pageable, LocalDate periodFrom, LocalDate periodTo) {

        String operator = documentSearchService.resolveOperator(filterRequestDto);
        String isDeleted = documentSearchService.resolveIsDeleted(filterRequestDto);

        List<FilterDto> filters = documentSearchService.resolveDateFilters(filterRequestDto, "TRANSACTION_DATE",
                periodFrom, periodTo);

        RawSearchResult raw = documentSearchService.search(documentId, filters, operator, pageable, isDeleted,
                "DOC_REF", TRANSACTION_POID);

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    @Override
    public String reopenJob(Long transactionPoid) {

        Long loginUserPoid = UserContext.getUserPoid();

        return spRepostirory.callReopenJobProc(loginUserPoid, transactionPoid);
    }

    @Override
    public ProjectLoadInJobsProcResponse loadJobs(Long transactionPoid) {

        return spRepostirory.callProjectsLoadInJobsProc(transactionPoid);
    }
}
