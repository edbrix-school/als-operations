package com.asg.operations.crew.service.impl;

import com.asg.operations.crew.dto.*;
import com.asg.operations.crew.entity.ContractCrew;
import com.asg.operations.crew.entity.ContractCrewDtl;
import com.asg.operations.crew.entity.ContractCrewDtlId;
import com.asg.operations.crew.repository.ContractCrewDtlRepository;
import com.asg.operations.crew.repository.ContractCrewRepository;
import com.asg.operations.crew.util.CrewCodeGenerator;
import com.asg.operations.crew.util.EntityMapper;
import com.asg.operations.crew.util.ValidationUtil;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.exceptions.ValidationException;
import com.asg.operations.crew.service.ContractCrewService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for Contract Crew Master operations
 */
@Slf4j
@Service
@Transactional
public class ContractCrewServiceImpl implements ContractCrewService {

    private final ContractCrewRepository crewRepository;
    private final ContractCrewDtlRepository crewDtlRepository;
    private final EntityMapper entityMapper;
    private final CrewCodeGenerator codeGenerator;

    @Autowired
    public ContractCrewServiceImpl(
            ContractCrewRepository crewRepository,
            ContractCrewDtlRepository crewDtlRepository,
            EntityMapper entityMapper,
            CrewCodeGenerator codeGenerator
    ) {
        this.crewRepository = crewRepository;
        this.crewDtlRepository = crewDtlRepository;
        this.entityMapper = entityMapper;
        this.codeGenerator = codeGenerator;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ContractCrewResponse> getCrewList(
            String crewName,
            Long nationalityPoid,
            String company,
            String active,
            Pageable pageable,
            Long companyPoid
    ) {
        // Normalize search strings (uppercase for case-insensitive search)
        //String normalizedCrewCode = crewCode != null ? crewCode.toUpperCase() : null;
        String normalizedCrewName = crewName != null ? crewName.toUpperCase() : null;
        String normalizedCompany = company != null ? company.toUpperCase() : null;

        BigDecimal nationalityBigDecimal = nationalityPoid != null ? BigDecimal.valueOf(nationalityPoid) : null;


        // Execute search query
        Page<ContractCrew> crewPage = crewRepository.searchCrews(
                normalizedCrewName,
                nationalityBigDecimal,
                normalizedCompany,
                active,
                companyPoid,
                pageable
        );

        // Map entities to responses with nationality lookup
        List<ContractCrewResponse> responses = crewPage.getContent().stream()
                .map(entityMapper::toContractCrewRes)
                .collect(Collectors.toList());

        return new PageResponse<>(
                responses,
                crewPage.getNumber(),
                crewPage.getSize(),
                crewPage.getTotalElements()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ContractCrewResponse getCrewById(Long crewPoid) {
        // BigDecimal companyPoid = securityContextUtil.getCurrentCompanyPoid();

        ContractCrew crew = crewRepository.findByCrewPoid(crewPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Crew master not found with id: " + crewPoid));

        // Get crew details
        List<ContractCrewDtl> details = crewDtlRepository.findByIdCrewPoidOrderByIdDetRowId(crewPoid);

        ContractCrewResponse response = entityMapper.toContractCrewRes(crew);
        response.setDetails(details.stream()
                .map(entityMapper::toContractCrewDtlResponse)
                .collect(Collectors.toList()));

        return response;
    }

    @Override
    public ContractCrewResponse createCrew(ContractCrewRequest request, Long companyPoid, Long groupPoid, String userId) {
        // Validate request
        validateCrewRequest(request);

        // Map request to entity
        ContractCrew crew = entityMapper.toContractCrewEntity(companyPoid, groupPoid, userId, request);

        // Generate crew code
        String crewCode = codeGenerator.generateCrewCode(
                companyPoid,
                groupPoid
        );
        //crew.setCrewCode(crewCode);

        // Save entity
        crew = crewRepository.save(crew);

        log.info("Crew created successfully with id: " + crew.getCrewPoid());
        for (ContractCrewDtlRequest det : request.getDetails()) {
            this.saveCrewDetail(companyPoid, userId, crew.getCrewPoid(), det);
        }


        return getCrewById(crew.getCrewPoid());
    }

    @Override
    public ContractCrewResponse updateCrew(Long companyPoid, String userId,Long crewPoid, ContractCrewRequest request) {
        // Validate request
        validateCrewRequest(request);

        // Check if crew exists

        ContractCrew crew = crewRepository.findByCrewPoidAndCompanyPoid(crewPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Crew master not found with id: " + crewPoid));

        // Update entity
        entityMapper.updateContractCrewEntity(crew, request);

        // Save updated entity
        crew = crewRepository.save(crew);

        for (ContractCrewDtlRequest det : request.getDetails()) {
            //this.saveCrewDetail(companyPoid,"",crew.getCrewPoid(),det);
            String action = det.getActionType();
            log.info("Action: " + action);

            if (StringUtils.isBlank(action)) {
                log.warn("Detail entry has NULL actionType. Skipping...");
                continue;
            }
            switch (action) {
                case "iscreated" -> this.saveCrewDetail(companyPoid, userId, crew.getCrewPoid(), det);
                case "isupdated" -> this.updateCrewDetail(companyPoid, userId, crew.getCrewPoid(), det);
                case "isdeleted" -> this.deleteCrewDetail(companyPoid, crew.getCrewPoid(), det.getDetRowId());
            }

        }


        return getCrewById(crew.getCrewPoid());
    }

    @Override
    public void deleteCrew(Long companyPoid, Long crewPoid) {


        ContractCrew crew = crewRepository.findByCrewPoidAndCompanyPoid(crewPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Crew master not found with id: " + crewPoid));
        crew.setActive("N");
        crewRepository.save(crew);
        crewDtlRepository.deleteByIdCrewPoid(crewPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public CrewDetailsResponse getCrewDetails(Long companyPoid, Long crewPoid) {
        // Verify crew exists

        boolean crewExists = crewRepository.findByCrewPoidAndCompanyPoid(crewPoid, companyPoid).isPresent();
        if (!crewExists) {
            throw new ResourceNotFoundException("Crew master not found with id: " + crewPoid);
        }

        // Get detail records
        List<ContractCrewDtl> details = crewDtlRepository.findByIdCrewPoidOrderByIdDetRowId(crewPoid);
        CrewDetailsResponse response = new CrewDetailsResponse();
        response.setCrewPoid(crewPoid);
        response.setDetails(this.toContractCrewDtlResponseList(details));

        return response;
    }

    private List<ContractCrewDtlResponse> toContractCrewDtlResponseList(List<ContractCrewDtl> details) {
        return details.stream()
                .map(entityMapper::toContractCrewDtlResponse)
                .collect(Collectors.toList());
    }

    private void saveCrewDetail(Long companyPoid, String userId, Long crewPoid, ContractCrewDtlRequest detailRequest) {

        ContractCrewDtl newDetail = entityMapper.toContractCrewDtlEntity(userId, detailRequest, crewPoid);
        Long max = crewDtlRepository.findMaxDetRowIdByCrewPoid(crewPoid);
        Long next = (max == null ? 0L : max) + 1L;
        newDetail.getId().setDetRowId(next);
        crewDtlRepository.save(newDetail);
    }

    public void updateCrewDetail(Long companyPoid, String userId, Long crewPoid, ContractCrewDtlRequest detailRequest) {
        ContractCrewDtlId id = new ContractCrewDtlId(crewPoid, detailRequest.getDetRowId());
        ContractCrewDtl detail = crewDtlRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Crew master not found with id: " + crewPoid));

        entityMapper.updateContractCrewDtlEntity(detail, detailRequest);
        crewDtlRepository.save(detail);

    }


    @Override
    public CrewDetailsResponse saveCrewDetails(Long companyPoid, String userId, Long crewPoid, BulkSaveDetailsRequest request) {
        // Verify crew exists

        ContractCrew crew = crewRepository.findByCrewPoidAndCompanyPoid(crewPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Crew master not found with id: " + crewPoid));


        // Validate detail records
        List<ValidationError> validationErrors = new ArrayList<>();

        // Validate dates
        if (request.getDetails() != null) {
            validationErrors.addAll(ValidationUtil.validateAllDetailDates(request.getDetails()));

            // Validate document types and other business rules
            for (int i = 0; i < request.getDetails().size(); i++) {
                ContractCrewDtlRequest detail = request.getDetails().get(i);

                String operation = detail.getActionType();

                // For UPDATE operations, verify record exists
                if ("isupdated".equals(operation) && detail.getDetRowId() != null) {
                    boolean recordExists = crewDtlRepository.existsByIdCrewPoidAndIdDetRowId(crewPoid, detail.getDetRowId());
                    if (!recordExists) {
                        validationErrors.add(new ValidationError(
                                i,
                                "detRowId",
                                "Detail record not found with detRowId: " + detail.getDetRowId()
                        ));
                    }
                }
            }
        }

        // If validation errors exist, throw exception
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(
                    "Validation errors occurred",
                    validationErrors
            );
        }

        // Execute bulk save within transaction
        List<ContractCrewDtl> savedDetails = new ArrayList<>();


        // Step 2: Update existing records
        if (request.getDetails() != null) {
            for (ContractCrewDtlRequest detailRequest : request.getDetails()) {
                String action = detailRequest.getActionType();

                if (action == null) {
                    log.warn("Detail entry has NULL actionType. Skipping...");
                    continue;
                }
                if ("isdeleted".equalsIgnoreCase(action)) {
                    this.deleteCrewDetail(companyPoid, crewPoid, detailRequest.getDetRowId());
                } else if ("isupdated".equalsIgnoreCase(action) && detailRequest.getDetRowId() != null) {
                    // Update existing record
                    ContractCrewDtlId id = new ContractCrewDtlId(
                            crewPoid,
                            detailRequest.getDetRowId()
                    );
                    Optional<ContractCrewDtl> existingDetail = crewDtlRepository.findById(id);
                    if (existingDetail.isPresent()) {
                        ContractCrewDtl detail = existingDetail.get();
                        entityMapper.updateContractCrewDtlEntity(detail, detailRequest);
                        detail = crewDtlRepository.save(detail);
                        savedDetails.add(detail);
                    }
                } else if ("iscreated".equalsIgnoreCase(action)) {
                    // Insert new record
                    ContractCrewDtl newDetail = entityMapper.toContractCrewDtlEntity(userId, detailRequest, crewPoid);
                    // Ensure embedded id has a detRowId. If missing, assign next available.
                    if (newDetail.getId() == null) {
                        newDetail.setId(new ContractCrewDtlId(crewPoid, null));
                    }
                    if (newDetail.getId().getDetRowId() == null) {
                        Long max = crewDtlRepository.findMaxDetRowIdByCrewPoid(crewPoid);
                        Long next = (max == null ? 0L : max) + 1L;
                        newDetail.getId().setDetRowId(next);
                    }
                    newDetail = crewDtlRepository.save(newDetail);
                    savedDetails.add(newDetail);
                }
            }
        }


        CrewDetailsResponse response = new CrewDetailsResponse();
        response.setCrewPoid(crewPoid);
        response.setDetails(this.toContractCrewDtlResponseList(savedDetails));

        return response;
    }

    @Override
    public void deleteCrewDetail(Long companyPoid, Long crewPoid, Long detRowId) {
        // Verify crew exists

        boolean crewExists = crewRepository.findByCrewPoidAndCompanyPoid(crewPoid, companyPoid).isPresent();
        if (!crewExists) {
            throw new ResourceNotFoundException("Crew master not found with id: " + crewPoid);
        }

        // Verify detail record exists
        ContractCrewDtl contractCrewDtl = crewDtlRepository.findByIdCrewPoidAndIdDetRowId(crewPoid, detRowId);
        if (contractCrewDtl == null || contractCrewDtl.getId().getDetRowId() == null) {
            throw new ResourceNotFoundException(
                    "Detail record not found with crewPoid: " + crewPoid + ", detRowId: " + detRowId
            );
        }

        crewDtlRepository.deleteById(contractCrewDtl.getId());
    }

    private void validateCrewRequest(ContractCrewRequest request) {
        List<ValidationError> errors = new ArrayList<>();

        // Validate passport dates
        ValidationError dateError = ValidationUtil.validatePassportDates(
                request.getCrewPassportIssueDate(),
                request.getCrewPassportExpiryDate()
        );
        if (dateError != null) {
            errors.add(dateError);
        }

        // If validation errors exist, throw exception
        if (!errors.isEmpty()) {
            throw new ValidationException(
                    "Validation errors occurred",
                    errors
            );
        }
    }
}

