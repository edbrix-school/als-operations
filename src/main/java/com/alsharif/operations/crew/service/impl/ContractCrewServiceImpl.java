package com.alsharif.operations.crew.service.impl;

import com.alsharif.operations.crew.dto.ContractCrewResponse;
import com.alsharif.operations.crew.dto.*;
import com.alsharif.operations.crew.entity.ContractCrew;
import com.alsharif.operations.crew.entity.ContractCrewDtl;
import com.alsharif.operations.crew.entity.ContractCrewDtlId;
import com.alsharif.operations.exceptions.ResourceNotFoundException;
import com.alsharif.operations.exceptions.ValidationException;
import com.alsharif.operations.crew.repository.*;
import com.alsharif.operations.crew.service.ContractCrewService;
import com.alsharif.operations.crew.util.*;
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
                .map(entityMapper::toContractCrewResponse)
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


        return entityMapper.toContractCrewResponse(crew);
    }

    @Override
    public ContractCrewResponse createCrew(ContractCrewRequest request, Long companyPoid, Long groupPoid, String userId) {
        // Validate request
        validateCrewRequest(request);

        // Map request to entity
        ContractCrew crew = entityMapper.toContractCrewEntity(companyPoid, groupPoid,userId, request);

        // Generate crew code
        String crewCode = codeGenerator.generateCrewCode(
               companyPoid,
               groupPoid
        );
        //crew.setCrewCode(crewCode);

        // Save entity
        crew = crewRepository.save(crew);

        return entityMapper.toContractCrewRes(crew);
    }

    @Override
    public ContractCrewResponse updateCrew(Long companyPoid,Long crewPoid, ContractCrewRequest request) {
        // Validate request
        validateCrewRequest(request);

        // Check if crew exists

        ContractCrew crew = crewRepository.findByCrewPoidAndCompanyPoid(crewPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Crew master not found with id: " + crewPoid));

        // Update entity
        entityMapper.updateContractCrewEntity(crew, request);

        // Save updated entity
        crew = crewRepository.save(crew);
        return entityMapper.toContractCrewResponse(crew);
    }

    @Override
    public void deleteCrew(Long companyPoid,Long crewPoid) {



        ContractCrew crew = crewRepository.findByCrewPoidAndCompanyPoid(crewPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Crew master not found with id: " + crewPoid));
        crew.setActive("N");
        crewRepository.save(crew);
        crewDtlRepository.deleteByIdCrewPoid(crewPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public CrewDetailsResponse getCrewDetails(Long companyPoid,Long crewPoid) {
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


    @Override
    public CrewDetailsResponse saveCrewDetails(Long companyPoid,String userId,Long crewPoid, BulkSaveDetailsRequest request) {
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

                // Determine operation (INSERT or UPDATE)
                String operation = detail.getOperation();
                if (operation == null || operation.isEmpty()) {
                    operation = detail.getDetRowId() != null ? "UPDATE" : "INSERT";
                }

                // For UPDATE operations, verify record exists
                if ("UPDATE".equals(operation) && detail.getDetRowId() != null) {
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

        // Step 1: Delete records
        if (request.getDeletedRowIds() != null && !request.getDeletedRowIds().isEmpty()) {
            for (Long detRowId : request.getDeletedRowIds()) {
                ContractCrewDtlId id = new ContractCrewDtlId(crewPoid, detRowId);
                if (crewDtlRepository.existsById(id)) {
                    crewDtlRepository.deleteById(id);
                }
            }
        }

        // Step 2: Update existing records
        if (request.getDetails() != null) {
            for (ContractCrewDtlRequest detailRequest : request.getDetails()) {
                String operation = detailRequest.getOperation();
                if (operation == null || operation.isEmpty()) {
                    operation = detailRequest.getDetRowId() != null ? "UPDATE" : "INSERT";
                }

                if ("UPDATE".equals(operation) && detailRequest.getDetRowId() != null) {
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
                } else if ("INSERT".equals(operation)) {
                    // Insert new record
                    ContractCrewDtl newDetail = entityMapper.toContractCrewDtlEntity(userId,detailRequest, crewPoid);
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
    public void deleteCrewDetail(Long companyPoid,Long crewPoid, Long detRowId) {
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

