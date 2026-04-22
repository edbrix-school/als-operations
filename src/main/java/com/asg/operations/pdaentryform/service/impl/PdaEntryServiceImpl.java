package com.asg.operations.pdaentryform.service.impl;

import com.asg.common.lib.dto.DeleteReasonDto;
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
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.crew.dto.ValidationError;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.exceptions.ValidationException;
import com.asg.operations.pdaentryform.dto.*;
import com.asg.operations.pdaentryform.entity.*;
import com.asg.operations.pdaentryform.repository.*;
import jakarta.persistence.EntityManager;
import com.asg.operations.pdaentryform.service.PdaEntryService;
import com.asg.operations.pdaentryform.util.PdaEntryDocumentRefGenerator;
import lombok.RequiredArgsConstructor;
import oracle.jdbc.internal.OracleTypes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for PDA Entry operations
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PdaEntryServiceImpl implements PdaEntryService {

    private static final Logger logger = LoggerFactory.getLogger(PdaEntryServiceImpl.class);

    private final PdaEntryHdrRepository entryHdrRepository;
    private final PdaEntryDtlRepository entryDtlRepository;
    private final PdaEntryVehicleDtlRepository vehicleDtlRepository;
    private final PdaEntryTdrDetailRepository tdrDetailRepository;
    private final PdaEntryAcknowledgmentDtlRepository acknowledgmentDtlRepository;
    //private final SecurityContextUtil securityContextUtil;
    private final PdaEntryDocumentRefGenerator docRefGenerator;
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;
    private final LovService lovService;
    private final com.asg.common.lib.service.PrintService printService;
    private final javax.sql.DataSource dataSource;
    private final LoggingService loggingService;
    private final DocumentDeleteService documentDeleteService;
    private final DocumentSearchService documentSearchService;

    @Override
    @Transactional(readOnly = true)
    public PdaEntryResponse getPdaEntryById(Long transactionPoid, Long groupPoid, Long companyPoid) {

        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        callGetPdaRefWhereClause(BigDecimal.valueOf(groupPoid));

        return toResponse(entry);
    }

    @Override
    @Transactional
    public PdaEntryResponse createPdaEntry(PdaEntryRequest request, Long groupPoid, Long companyPoid, Long userPoid) {
        // Validate request
        validatePdaEntryRequest(request, null);

        // Create entity from request
        PdaEntryHdr entry = new PdaEntryHdr();
        mapRequestToEntity(request, entry);

        // Set default values
        entry.setGroupPoid(groupPoid);
        entry.setCompanyPoid(companyPoid);
        entry.setStatus("PROPOSAL");
        entry.setDeleted("N");
        if (entry.getUrgentApproval() == null) {
            entry.setUrgentApproval("N");
        }
        entry.setPrincipalApproved("N");
        entry.setVesselVerified("N");
        if (entry.getMultipleFda() == null) {
            entry.setMultipleFda("N");
        }
        if (entry.getMenasDues() == null) {
            entry.setMenasDues("N");
        }
        if (entry.getPmiDocument() == null) {
            entry.setPmiDocument("N");
        }

        // Generate unique document reference (ignore request docRef)
//        String docRef = docRefGenerator.generateDocRef(BigDecimal.valueOf(groupPoid));
//        int retries = 0;
//        while (entryHdrRepository.existsByDocRef(docRef) && retries < 5) {
//            docRef = docRefGenerator.generateDocRef(BigDecimal.valueOf(groupPoid));
//            retries++;
//        }
//        entry.setDocRef(docRef);
//        logger.info("Generated unique docRef: {}", docRef);

        // Auto-populate voyage details if voyagePoid is provided
        if (request.getVoyagePoid() != null) {
            Map<String, Object> voyageDetails = getVoyageDetails(request.getVoyagePoid(), groupPoid, companyPoid, userPoid);
            if (!voyageDetails.isEmpty()) {
                // Auto-populate voyage number if not provided in request
                if (request.getVoyageNo() == null || request.getVoyageNo().trim().isEmpty()) {
                    entry.setVoyageNo((String) voyageDetails.get("voyageNo"));
                }
                // Auto-populate vessel details
                if (voyageDetails.get("vesselPoid") != null) {
                    entry.setVesselPoid((BigDecimal) voyageDetails.get("vesselPoid"));
                    entry.setVesselTypePoid((BigDecimal) voyageDetails.get("vesselTypePoid"));
                    if (request.getImoNumber() == null || request.getImoNumber().trim().isEmpty()) {
                        entry.setImoNumber((String) voyageDetails.get("imoNumber"));
                    }
                    entry.setGrt((BigDecimal) voyageDetails.get("grt"));
                    entry.setNrt((BigDecimal) voyageDetails.get("nrt"));
                    entry.setDwt((BigDecimal) voyageDetails.get("dwt"));
                }
                // Auto-populate other voyage details
                if (voyageDetails.get("linePoid") != null) {
                    entry.setLinePoid((BigDecimal) voyageDetails.get("linePoid"));
                }
                if (voyageDetails.get("portPoid") != null) {
                    entry.setPortPoid((BigDecimal) voyageDetails.get("portPoid"));
                }
                if (voyageDetails.get("arrivalDate") != null) {
                    Object arrivalDateObj = voyageDetails.get("arrivalDate");
                    if (arrivalDateObj instanceof java.sql.Date) {
                        entry.setArrivalDate(((java.sql.Date) arrivalDateObj).toLocalDate());
                    } else if (arrivalDateObj instanceof java.sql.Timestamp) {
                        entry.setArrivalDate(((java.sql.Timestamp) arrivalDateObj).toLocalDateTime().toLocalDate());
                    }
                }
                if (voyageDetails.get("sailDate") != null) {
                    Object sailDateObj = voyageDetails.get("sailDate");
                    if (sailDateObj instanceof java.sql.Date) {
                        entry.setSailDate(((java.sql.Date) sailDateObj).toLocalDate());
                    } else if (sailDateObj instanceof java.sql.Timestamp) {
                        entry.setSailDate(((java.sql.Timestamp) sailDateObj).toLocalDateTime().toLocalDate());
                    }
                }
                if (voyageDetails.get("totalQuantity") != null) {
                    entry.setTotalQuantity((BigDecimal) voyageDetails.get("totalQuantity"));
                }
                if (voyageDetails.get("numberOfDays") != null) {
                    entry.setNumberOfDays((BigDecimal) voyageDetails.get("numberOfDays"));
                }
            }
        }

        // Auto-populate vessel details if vesselPoid is provided (fallback if not from voyage)
        // Only auto-populate if values are not already provided in the request
        if (request.getVesselPoid() != null && 
                (entry.getGrt() == null || entry.getNrt() == null || entry.getDwt() == null || entry.getVesselTypePoid() == null)) {
            VesselDetailsResponse vesselDetails = getVesselDetails(request.getVesselPoid(), groupPoid, companyPoid, userPoid);
            if (vesselDetails != null) {
                logger.info("Auto-populating vessel details - GRT: {}, NRT: {}, DWT: {}", 
                        vesselDetails.getGrt(), vesselDetails.getNrt(), vesselDetails.getDwt());
                
                // Only set values that are null (not provided in request)
                if (entry.getVesselTypePoid() == null) {
                    entry.setVesselTypePoid(vesselDetails.getVesselTypePoid());
                }
                if (entry.getGrt() == null) {
                    entry.setGrt(vesselDetails.getGrt());
                }
                if (entry.getNrt() == null) {
                    entry.setNrt(vesselDetails.getNrt());
                }
                if (entry.getDwt() == null) {
                    entry.setDwt(vesselDetails.getDwt());
                }
                
                // Only set IMO number if not provided in request
                if ((request.getImoNumber() == null || request.getImoNumber().trim().isEmpty()) && entry.getImoNumber() == null) {
                    entry.setImoNumber(vesselDetails.getImoNumber());
                }
                
                logger.info("Vessel details set on entry - GRT: {}, NRT: {}, DWT: {}", 
                        entry.getGrt(), entry.getNrt(), entry.getDwt());
            } else {
                logger.warn("No vessel details returned for vesselPoid: {}", request.getVesselPoid());
            }
        }

        // Auto-populate currency if principalPoid is provided
        if (request.getPrincipalPoid() != null) {
            setDefaultCurrency(groupPoid, companyPoid, userPoid, entry.getTransactionPoid(), request.getPrincipalPoid(), entry);
        }

        // Save entity
        entry = entryHdrRepository.save(entry);
        entityManager.flush();
        entityManager.refresh(entry);
        logger.info("After initial save - salesmanPoid: {}", entry.getSalesmanPoid());

        // Call before save validation stored procedure
        String validationStatus = callBeforeSaveValidation(
                groupPoid, companyPoid, userPoid, entry.getTransactionPoid(),
                entry.getPrincipalPoid(), entry.getLinePoid(), entry.getVesselPoid(),
                entry.getVoyageNo(), entry.getVoyagePoid(),
                entry.getArrivalDate(), entry.getSailDate()
        );

        if (validationStatus != null && validationStatus.startsWith("ERROR")) {
            throw new ValidationException(
                    "Validation failed",
                    List.of(new ValidationError("general", validationStatus))
            );
        }

        // Call after save validation stored procedure
        callAfterSaveValidation(
                groupPoid, companyPoid, userPoid, entry.getTransactionPoid(),
                entry.getPrincipalPoid(), entry.getLinePoid(), entry.getVesselPoid(),
                entry.getVoyageNo(), entry.getVoyagePoid()
        );

        String key = entry.getTransactionPoid().toString();
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), key, String.format("%s %s", LogDetailsEnum.CREATED, entry.getDocRef()));
        return toResponse(entry);
    }

    @Override
    public PdaEntryResponse updatePdaEntry(Long transactionPoid, PdaEntryRequest request, Long groupPoid, Long companyPoid, Long userPoid) {

        // Find existing entry
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        PdaEntryHdr oldEntry = new PdaEntryHdr();
        BeanUtils.copyProperties(entry, oldEntry);

        // Check edit permissions
        canEdit(entry);

        // For TDR ref type, call edit validation stored procedure
        if ("TDR".equals(entry.getRefType())) {
            String editValidationStatus = callEditValidation(
                    groupPoid, companyPoid, userPoid, transactionPoid
            );
            if (editValidationStatus != null &&
                    (editValidationStatus.startsWith("ERROR") || editValidationStatus.startsWith("WARNING"))) {
                throw new ValidationException(
                        "Edit validation failed",
                        List.of(new ValidationError("general", editValidationStatus))
                );
            }
        }

        // Validate request
        validatePdaEntryRequest(request, transactionPoid);

        // Map request to entity (preserve read-only fields)
        mapRequestToEntity(request, entry);

        // Auto-populate voyage details if voyagePoid changed
        if (request.getVoyagePoid() != null &&
                !Objects.equals(entry.getVoyagePoid(), request.getVoyagePoid())) {
            Map<String, Object> voyageDetails = getVoyageDetails(request.getVoyagePoid(), groupPoid, companyPoid, userPoid);
            if (!voyageDetails.isEmpty()) {
                // Auto-populate voyage number if not provided in request
                if (request.getVoyageNo() == null || request.getVoyageNo().trim().isEmpty()) {
                    entry.setVoyageNo((String) voyageDetails.get("voyageNo"));
                }
                // Auto-populate vessel details
                if (voyageDetails.get("vesselPoid") != null) {
                    entry.setVesselPoid((BigDecimal) voyageDetails.get("vesselPoid"));
                    entry.setVesselTypePoid((BigDecimal) voyageDetails.get("vesselTypePoid"));
                    if (request.getImoNumber() == null || request.getImoNumber().trim().isEmpty()) {
                        entry.setImoNumber((String) voyageDetails.get("imoNumber"));
                    }
                    entry.setGrt((BigDecimal) voyageDetails.get("grt"));
                    entry.setNrt((BigDecimal) voyageDetails.get("nrt"));
                    entry.setDwt((BigDecimal) voyageDetails.get("dwt"));
                }
                // Auto-populate other voyage details
                if (voyageDetails.get("linePoid") != null) {
                    entry.setLinePoid((BigDecimal) voyageDetails.get("linePoid"));
                }
                if (voyageDetails.get("portPoid") != null) {
                    entry.setPortPoid((BigDecimal) voyageDetails.get("portPoid"));
                }
                if (voyageDetails.get("arrivalDate") != null) {
                    Object arrivalDateObj = voyageDetails.get("arrivalDate");
                    if (arrivalDateObj instanceof java.sql.Date) {
                        entry.setArrivalDate(((java.sql.Date) arrivalDateObj).toLocalDate());
                    } else if (arrivalDateObj instanceof java.sql.Timestamp) {
                        entry.setArrivalDate(((java.sql.Timestamp) arrivalDateObj).toLocalDateTime().toLocalDate());
                    }
                }
                if (voyageDetails.get("sailDate") != null) {
                    Object sailDateObj = voyageDetails.get("sailDate");
                    if (sailDateObj instanceof java.sql.Date) {
                        entry.setSailDate(((java.sql.Date) sailDateObj).toLocalDate());
                    } else if (sailDateObj instanceof java.sql.Timestamp) {
                        entry.setSailDate(((java.sql.Timestamp) sailDateObj).toLocalDateTime().toLocalDate());
                    }
                }
                if (voyageDetails.get("totalQuantity") != null) {
                    entry.setTotalQuantity((BigDecimal) voyageDetails.get("totalQuantity"));
                }
                if (voyageDetails.get("numberOfDays") != null) {
                    entry.setNumberOfDays((BigDecimal) voyageDetails.get("numberOfDays"));
                }
            }
        }

        // Auto-populate vessel details if vesselPoid changed (fallback if not from voyage)
        if (request.getVesselPoid() != null &&
                !Objects.equals(entry.getVesselPoid(), request.getVesselPoid()) &&
                entry.getVesselTypePoid() == null) {
            VesselDetailsResponse vesselDetails = getVesselDetails(request.getVesselPoid(), groupPoid, companyPoid, userPoid);
            if (vesselDetails != null) {
                entry.setVesselTypePoid(vesselDetails.getVesselTypePoid());
                // Only set IMO number if not provided in request
                if (request.getImoNumber() == null || request.getImoNumber().trim().isEmpty()) {
                    entry.setImoNumber(vesselDetails.getImoNumber());
                }
                entry.setGrt(vesselDetails.getGrt());
                entry.setNrt(vesselDetails.getNrt());
                entry.setDwt(vesselDetails.getDwt());
            }
        }

        // Auto-populate currency if principalPoid changed
        if (request.getPrincipalPoid() != null &&
                !Objects.equals(entry.getPrincipalPoid(), request.getPrincipalPoid())) {
            setDefaultCurrency(groupPoid, companyPoid, userPoid, transactionPoid, request.getPrincipalPoid(), entry);
        }

        // Save entity
        entry = entryHdrRepository.save(entry);

        // Call before save validation stored procedure
        String validationStatus = callBeforeSaveValidation(
                groupPoid, companyPoid, userPoid, entry.getTransactionPoid(),
                entry.getPrincipalPoid(), entry.getLinePoid(), entry.getVesselPoid(),
                entry.getVoyageNo(), entry.getVoyagePoid(),
                entry.getArrivalDate(), entry.getSailDate()
        );

        if (validationStatus != null && validationStatus.startsWith("ERROR")) {
            throw new ValidationException(
                    "Validation failed",
                    List.of(new ValidationError("general", validationStatus))
            );
        }

        // Call after save validation stored procedure
        callAfterSaveValidation(
                groupPoid, companyPoid, userPoid, entry.getTransactionPoid(),
                entry.getPrincipalPoid(), entry.getLinePoid(), entry.getVesselPoid(),
                entry.getVoyageNo(), entry.getVoyagePoid()
        );
        
        // Reload entity from database to get any changes made by stored procedures
        entry = entryHdrRepository.findById(entry.getTransactionPoid())
                .orElse(entry);

        loggingService.logChanges(oldEntry, entry, PdaEntryHdr.class, UserContext.getDocumentId(), entry.getTransactionPoid().toString(), LogDetailsEnum.MODIFIED, "TRANSACTION_POID");
        return toResponse(entry);
    }

    @Override
    public void deletePdaEntry(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, @Valid DeleteReasonDto deleteReasonDto) {

        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        documentDeleteService.deleteDocument(
                transactionPoid,
                "PDA_ENTRY_HDR",
                "TRANSACTION_POID",
                deleteReasonDto,
                entry.getTransactionDate()
        );
    }

    // Charge Details Methods - Batch 5

    @Override
    public List<PdaEntryChargeDetailResponse> getChargeDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {

        // Validate transaction exists
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        entityManager.flush();

        // Get all charge details
        List<PdaEntryDtl> details = entryDtlRepository.findByTransactionPoidOrderBySeqnoAscDetRowIdAsc(transactionPoid);

        return details.stream()
                .map(detail -> toChargeDetailResponse(detail, groupPoid, companyPoid))
                .collect(Collectors.toList());
    }

    @Override
    public List<PdaEntryChargeDetailResponse> bulkSaveChargeDetails(Long transactionPoid, BulkSaveChargeDetailsRequest request, Long groupPoid, Long companyPoid, String userId) {
        logger.info("[AUDIT-LOG] bulkSaveChargeDetails called - transactionPoid: {}, chargeDetails count: {}", 
            transactionPoid, request.getChargeDetails() != null ? request.getChargeDetails().size() : 0);

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        canEdit(entry);

        LocalDateTime now = LocalDateTime.now();

        // Process creates and updates
        if (request.getChargeDetails() != null) {
            for (PdaEntryChargeDetailRequest detailRequest : request.getChargeDetails()) {
                logger.info("[AUDIT-LOG] Processing charge detail - detRowId: {}, chargePoid: {}, actionType: {}", 
                    detailRequest.getDetRowId(), detailRequest.getChargePoid(), detailRequest.getActionType());
                
                // Handle deletion via actionType
                if ("Deleted".equalsIgnoreCase(detailRequest.getActionType()) && detailRequest.getDetRowId() != null) {
                    logger.info("[AUDIT-LOG] Deleting charge detail with detRowId: {} via actionType", detailRequest.getDetRowId());
                    deleteChargeDetailRecord(transactionPoid, detailRequest.getDetRowId());
                } else if (detailRequest.getDetRowId() == null) {
                    // Create new
                    logger.info("[AUDIT-LOG] Creating new charge detail");
                    createChargeDetail(transactionPoid, detailRequest, userId, now, companyPoid);
                } else {
                    // Update existing
                    logger.info("[AUDIT-LOG] Updating existing charge detail with detRowId: {}", detailRequest.getDetRowId());
                    updateChargeDetail(transactionPoid, detailRequest, userId, now, companyPoid);
                }
            }
        }

        // Process deletes
        if (request.getDeleteDetRowIds() != null && !request.getDeleteDetRowIds().isEmpty()) {
            for (Long detRowId : request.getDeleteDetRowIds()) {
                deleteChargeDetailRecord(transactionPoid, detRowId);
            }
        }

        // Recalculate header total amount
        recalculateHeaderTotalAmount(transactionPoid, userId);

        // Return updated list
        return getChargeDetails(transactionPoid, groupPoid, companyPoid);
    }

    @Override
    public PdaEntryChargeDetailResponse updateChargeDetail(Long transactionPoid, Long detRowId, 
                                                           PdaEntryChargeDetailRequest request, 
                                                           Long groupPoid, Long companyPoid, String userId) {
        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        canEdit(entry);

        // Set detRowId from path parameter to prevent mismatch
        request.setDetRowId(detRowId);
        
        // Update the charge detail
        updateChargeDetail(transactionPoid, request, userId, LocalDateTime.now(), companyPoid);
        
        // Recalculate header total
        recalculateHeaderTotalAmount(transactionPoid, userId);
        
        // Return updated detail
        PdaEntryDtlId detailId = new PdaEntryDtlId(transactionPoid, detRowId);
        PdaEntryDtl detail = entryDtlRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Charge detail not found with id: " + detRowId
                ));
        return toChargeDetailResponse(detail, groupPoid, companyPoid);
    }

    @Override
    public void deleteChargeDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid, String userId) {

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        canEdit(entry);

        // Validate detail exists
        PdaEntryDtlId detailId = new PdaEntryDtlId(transactionPoid, detRowId);
        PdaEntryDtl detail = entryDtlRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Charge detail not found with id: " + detRowId
                ));

        // Delete detail
        entryDtlRepository.delete(detail);

        // Recalculate header total amount
        recalculateHeaderTotalAmount(transactionPoid, userId);
    }

    @Override
    public void clearChargeDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        canEdit(entry);

        // Check if clearing is allowed
        if ("GENERAL".equals(entry.getRefType()) && !"PROPOSAL".equals(entry.getStatus())) {
            throw new ValidationException(
                    "Charges cannot be cleared",
                    List.of(new ValidationError("status", "Charges can only be cleared when status is PROPOSAL for GENERAL ref type"))
            );
        }

        if ("CONFIRMED".equals(entry.getStatus()) || "CLOSED".equals(entry.getStatus())) {
            throw new ValidationException(
                    "Charges cannot be cleared",
                    List.of(new ValidationError("status", "Charges cannot be cleared when status is CONFIRMED or CLOSED"))
            );
        }

        // Call stored procedure to clear charge details
        callClearChargeDetails(groupPoid, userPoid, companyPoid, transactionPoid);

        // Update header total amount to 0
        entry.setTotalAmount(BigDecimal.ZERO);
        // Audit is handled by BaseEntity
        entryHdrRepository.save(entry);
    }

    @Override
    public List<PdaEntryChargeDetailResponse> recalculateChargeDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        canEdit(entry);

        // Validate required header fields
        validateRecalculateFields(entry);

        // Call stored procedure to recalculate
        callReCalculateCharges(
                groupPoid, userPoid, companyPoid, transactionPoid,
                entry.getVesselPoid(), entry.getVesselTypePoid(),
                entry.getGrt(), entry.getNrt(), entry.getDwt(),
                entry.getPortPoid(), entry.getArrivalDate(), entry.getSailDate(),
                entry.getHarbourCallType(), entry.getTotalQuantity(),
                entry.getNumberOfDays(), entry.getPrincipalPoid()
        );

        // Recalculate header total amount
        recalculateHeaderTotalAmount(transactionPoid, UserContext.getUserId());

        // Flush to ensure all changes are persisted
        entityManager.flush();

        // Return updated charge details
        return getChargeDetails(transactionPoid, groupPoid, companyPoid);
    }

    @Override
    public List<PdaEntryChargeDetailResponse> loadDefaultCharges(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        canEdit(entry);

        // Validate required header fields
        validateRecalculateFields(entry);

        // Call stored procedure to load default charges
        callLoadDefaultCharges(
                groupPoid, userPoid, companyPoid, transactionPoid,
                entry.getVesselPoid(), entry.getVesselTypePoid(),
                entry.getGrt(), entry.getNrt(), entry.getDwt(),
                entry.getPortPoid(), entry.getArrivalDate(), entry.getSailDate(),
                entry.getHarbourCallType(), entry.getTotalQuantity(),
                entry.getNumberOfDays(), entry.getPrincipalPoid()
        );

        // Recalculate header total amount
        recalculateHeaderTotalAmount(transactionPoid, UserContext.getUserId());

        // Flush to ensure all changes are persisted
        entityManager.flush();

        // Return loaded charge details
        return getChargeDetails(transactionPoid, groupPoid, companyPoid);
    }

    // Vehicle Details Methods - Batch 6

    @Override
    public List<PdaEntryVehicleDetailResponse> getVehicleDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {

        // Validate transaction exists
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        entityManager.flush();

        // Get all vehicle details
        List<PdaEntryVehicleDtl> details = vehicleDtlRepository.findByTransactionPoidOrderByDetRowIdAsc(transactionPoid);

        return details.stream()
                .map(this::toVehicleDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PdaEntryVehicleDetailResponse> bulkSaveVehicleDetails(Long transactionPoid, BulkSaveVehicleDetailsRequest request, Long groupPoid, Long companyPoid, String userId) {

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        canEdit(entry);

        LocalDateTime now = LocalDateTime.now();

        // Process creates and updates
        if (request.getVehicleDetails() != null) {
            for (PdaEntryVehicleDetailRequest detailRequest : request.getVehicleDetails()) {
                if (detailRequest.getDetRowId() == null) {
                    // Create new
                    createVehicleDetail(transactionPoid, detailRequest, userId, now);
                } else {
                    // Update existing
                    updateVehicleDetail(transactionPoid, detailRequest, userId, now);
                }
            }
        }

        // Process deletes
        if (request.getDeleteDetRowIds() != null && !request.getDeleteDetRowIds().isEmpty()) {
            for (Long detRowId : request.getDeleteDetRowIds()) {
                deleteVehicleDetailRecord(transactionPoid, detRowId);
            }
        }

        // Return updated list
        return getVehicleDetails(transactionPoid, groupPoid, companyPoid);
    }

    @Override
    public void importVehicleDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        canEdit(entry);

        // Call stored procedure to import vehicle details
        callImportVehicleDetails(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    @Override
    public void clearVehicleDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        canEdit(entry);

        // Call stored procedure to clear vehicle details
        callClearVehicleDetails(groupPoid, userPoid, companyPoid, transactionPoid);
    }

    public String importTdrDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        canEdit(entry);

        return callImportTdrDetail(groupPoid, userPoid, companyPoid, transactionPoid);
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public String createFdaFromPda(Long groupPoid, Long companyPoid, Long userPoid, String pdaPoid) {
        try {
            logger.info("[SP-10] PROC_PDA_FDA_CREATE_FROM_PDA - START - pdaPoid: {}", pdaPoid);

            Long transactionPoid = Long.parseLong(pdaPoid);
            
            // Get PDA entry to check validations and get createdBy
            PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid)
                    .orElseThrow(() -> new ResourceNotFoundException("PDA Entry not found with id: " + transactionPoid));

            
            String createdBy = entry.getCreatedBy() != null ? entry.getCreatedBy() : "";

            // Try with schema prefix first
            String sqlWithSchema = "{ call PROC_PDA_FDA_CREATE_FROM_PDA(?, ?, ?, ?, ?) }";
            
            try {
                String result = jdbcTemplate.execute(sqlWithSchema, (java.sql.CallableStatement cs) -> {
                    cs.setBigDecimal(1, new BigDecimal(groupPoid));
                    cs.setBigDecimal(2, new BigDecimal(companyPoid));
                    cs.setBigDecimal(3, new BigDecimal(userPoid));
                    cs.setString(4, pdaPoid);
                    cs.registerOutParameter(5, Types.VARCHAR);
                    cs.execute();
                    return cs.getString(5);
                });
                
                logger.info("[SP-10] PROC_PDA_FDA_CREATE_FROM_PDA - Completed with schema. Status: {}", result);
                
                // Check for warnings or errors from stored procedure
                if (result != null && (result.startsWith("WARNING") || result.startsWith("ERROR"))) {
                    throw new ValidationException(
                            result,
                            List.of(new ValidationError("general", result))
                    );
                }
                
                return result != null ? result + "|createdBy:" + createdBy : "Success|createdBy:" + createdBy;
                
            } catch (ValidationException ve) {
                throw ve;
            } catch (Exception schemaCallException) {
                logger.warn("[SP-10] Schema call failed, trying without schema: {}", schemaCallException.getMessage());
                
                // Try without schema prefix
                String sql = "{ call PROC_PDA_FDA_CREATE_FROM_PDA(?, ?, ?, ?, ?) }";
                
                try {
                    String result = jdbcTemplate.execute(sql, (java.sql.CallableStatement cs) -> {
                        cs.setBigDecimal(1, new BigDecimal(groupPoid));
                        cs.setBigDecimal(2, new BigDecimal(companyPoid));
                        cs.setBigDecimal(3, new BigDecimal(userPoid));
                        cs.setString(4, pdaPoid);
                        cs.registerOutParameter(5, Types.VARCHAR);
                        cs.execute();
                        return cs.getString(5);
                    });
                    
                    logger.info("[SP-10] PROC_PDA_FDA_CREATE_FROM_PDA - Completed without schema. Status: {}", result);
                    
                    // Check for warnings or errors from stored procedure
                    if (result != null && (result.startsWith("WARNING") || result.startsWith("ERROR"))) {
                        throw new ValidationException(
                                result,
                                List.of(new ValidationError("general", result))
                        );
                    }
                    
                    return result != null ? result + "|createdBy:" + createdBy : "Success|createdBy:" + createdBy;
                    
                } catch (ValidationException ve) {
                    throw ve;
                } catch (Exception directCallException) {
                    logger.warn("[SP-10] Direct call failed, trying SimpleJdbcCall: {}", directCallException.getMessage());
                    
                    // Final fallback to SimpleJdbcCall
                    SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                            .withProcedureName("PROC_PDA_FDA_CREATE_FROM_PDA")
                            .withoutProcedureColumnMetaDataAccess()
                            .declareParameters(
                                    new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                                    new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                                    new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                                    new SqlParameter("P_PDA_POID", Types.VARCHAR),
                                    new SqlOutParameter("P_RESULT", Types.VARCHAR)
                            );

                    Map<String, Object> inputMap = new HashMap<>();
                    inputMap.put("P_LOGIN_GROUP_POID", new BigDecimal(groupPoid));
                    inputMap.put("P_LOGIN_COMPANY_POID", new BigDecimal(companyPoid));
                    inputMap.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));
                    inputMap.put("P_PDA_POID", pdaPoid);

                    Map<String, Object> result = jdbcCall.execute(inputMap);
                    String status = (String) result.get("P_RESULT");

                    logger.info("[SP-10] PROC_PDA_FDA_CREATE_FROM_PDA - Completed via SimpleJdbcCall. Status: {}", status);
                    
                    // Check for warnings or errors from stored procedure
                    if (status != null && (status.startsWith("WARNING") || status.startsWith("ERROR"))) {
                        throw new ValidationException(
                                status,
                                List.of(new ValidationError("general", status))
                        );
                    }
                    
                    return status != null ? status + "|createdBy:" + createdBy : "Success|createdBy:" + createdBy;
                }
            }

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[SP-10] PROC_PDA_FDA_CREATE_FROM_PDA - Error: {}", e.getMessage(), e);
            throw new ValidationException(
                    "FDA creation failed",
                    List.of(new ValidationError("general", "Error: " + e.getMessage()))
            );
        }
    }

    // Helper method to extract FDA reference from stored procedure result
    public Map<String, String> parseFdaCreationResult(String spResult) {
        Map<String, String> result = new HashMap<>();
        
        if (spResult != null) {
            // Check if createdBy is appended
            String actualResult = spResult;
            String createdBy = null;
            
            if (spResult.contains("|createdBy:")) {
                String[] parts = spResult.split("\\|createdBy:");
                actualResult = parts[0];
                if (parts.length > 1) {
                    createdBy = parts[1];
                }
            }
            
            result.put("message", actualResult);
            
            if (createdBy != null && !createdBy.isEmpty()) {
                result.put("ApprovedBy", createdBy);
            }
            
            // Extract FDA reference using regex pattern
            // Pattern matches: "FDA Ref: CSA926" or "FDA REF - CSA926" etc.
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("FDA\\s+(?:Ref|REF)\\s*[:-]?\\s*([A-Z0-9,\\s]+)");
            java.util.regex.Matcher matcher = pattern.matcher(actualResult);
            
            if (matcher.find()) {
                String fdaRef = matcher.group(1).trim();
                // Clean up any trailing characters like ')' or '...'
                fdaRef = fdaRef.replaceAll("[)\\.].*$", "").trim();
                result.put("fdaRef", fdaRef);
                logger.info("[SP-10] Extracted FDA Reference: {}", fdaRef);
            } else {
                logger.warn("[SP-10] Could not extract FDA reference from result: {}", actualResult);
            }
        }
        
        return result;
    }

    public void updateFdaFromPda(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        callUpdateFdaFromPda(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    public Map<String, Object> submitPdaToFda(Long transactionPoid, LocalDate vesselSailDate, Long groupPoid, Long companyPoid, Long userPoid) {
        // Validate transaction exists
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("PDA Entry not found with id: " + transactionPoid));

        // Validate vessel sail date is provided
        if (vesselSailDate == null) {
            throw new ValidationException(
                    "Vessel sail date is required",
                    List.of(new ValidationError("vesselSailDate", "Vessel sail date must be provided before submitting document to accounts"))
            );
        }

        return callSubmitPdaToFda(groupPoid, companyPoid, userPoid, transactionPoid, vesselSailDate);
    }

    public Map<String, Object> rejectFdaDocs(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, String correctionRemarks) {
        return callRejectFdaDocs(groupPoid, companyPoid, userPoid, transactionPoid, correctionRemarks);
    }

    public List<PdaEntryAcknowledgmentDetailResponse> uploadAcknowledgmentDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        logger.info("Calling acknowledgment upload SP for transactionPoid: {}", transactionPoid);
        callUploadAcknowledgmentDetails(groupPoid, userPoid, companyPoid, transactionPoid);
        List<PdaEntryAcknowledgmentDtl> list = acknowledgmentDtlRepository.findByTransactionPoid(transactionPoid);
        return toAcknowledgmentDetailResponseList(list);
    }

    public void clearAcknowledgmentDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        callClearAcknowledgmentDetails(groupPoid, userPoid, companyPoid, transactionPoid);
    }


    @Override
    public void publishVehicleDetailsForImport(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        canEdit(entry);

        // Call stored procedure to publish for import
        callPublishForImport(groupPoid, userPoid, companyPoid, transactionPoid);
    }

    // TDR Details Methods - Batch 6

    @Override
    public List<PdaEntryTdrDetailResponse> getTdrDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {

        // Validate transaction exists
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        entityManager.flush();

        // Get all TDR details
        List<PdaEntryTdrDetail> details = tdrDetailRepository.findByTransactionPoidOrderByDetRowIdAsc(transactionPoid);

        return details.stream()
                .map(this::toTdrDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PdaEntryTdrDetailResponse> bulkSaveTdrDetails(Long transactionPoid, BulkSaveTdrDetailsRequest request, Long groupPoid, Long companyPoid, String userId) {

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        canEdit(entry);

        LocalDateTime now = LocalDateTime.now();

        // Process creates and updates
        if (request.getTdrDetails() != null) {
            for (PdaEntryTdrDetailRequest detailRequest : request.getTdrDetails()) {
                if (detailRequest.getDetRowId() == null) {
                    // Create new
                    createTdrDetail(transactionPoid, detailRequest, userId, now);
                } else {
                    // Update existing
                    updateTdrDetail(transactionPoid, detailRequest, userId, now);
                }
            }
        }

        // Process deletes
        if (request.getDeleteDetRowIds() != null && !request.getDeleteDetRowIds().isEmpty()) {
            for (Long detRowId : request.getDeleteDetRowIds()) {
                deleteTdrDetailRecord(transactionPoid, detRowId);
            }
        }

        // Call default charges from TDR
        callDefaultChargesFromTdr(groupPoid, UserContext.getUserPoid(), companyPoid, transactionPoid, entry.getArrivalDate());

        // Return updated list
        return getTdrDetails(transactionPoid, groupPoid, companyPoid);
    }

    // Acknowledgment Details Methods - Batch 6

    @Override
    public List<PdaEntryAcknowledgmentDetailResponse> getAcknowledgmentDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {

        // Validate transaction exists
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        entityManager.flush();

        // Get all acknowledgment details
        List<PdaEntryAcknowledgmentDtl> details = acknowledgmentDtlRepository.findByTransactionPoidOrderByDetRowIdAsc(transactionPoid);

        return details.stream()
                .map(this::toAcknowledgmentDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PdaEntryAcknowledgmentDetailResponse> bulkSaveAcknowledgmentDetails(Long transactionPoid, BulkSaveAcknowledgmentDetailsRequest request, Long groupPoid, Long companyPoid, String userId) {

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        canEdit(entry);

        LocalDateTime now = LocalDateTime.now();

        // Process creates and updates
        if (request.getAcknowledgmentDetails() != null) {
            for (PdaEntryAcknowledgmentDetailRequest detailRequest : request.getAcknowledgmentDetails()) {
                if (detailRequest.getDetRowId() == null) {
                    // Create new
                    createAcknowledgmentDetail(transactionPoid, detailRequest, userId, now);
                } else {
                    // Update existing
                    updateAcknowledgmentDetail(transactionPoid, detailRequest, userId, now);
                }
            }
        }

        // Process deletes
        if (request.getDeleteDetRowIds() != null && !request.getDeleteDetRowIds().isEmpty()) {
            for (Long detRowId : request.getDeleteDetRowIds()) {
                deleteAcknowledgmentDetailRecord(transactionPoid, detRowId);
            }
        }

        // Return updated list
        return getAcknowledgmentDetails(transactionPoid, groupPoid, companyPoid);
    }

    // Special Operations Methods - Batch 7

    @Override
    public ValidationResponse validateBeforeSave(Long transactionPoid, PdaEntryRequest request, Long groupPoid, Long companyPoid, Long userPoid) {

        List<ValidationError> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try {
            // Perform all validations from create/update API
            validatePdaEntryRequest(request, transactionPoid);
        } catch (ValidationException e) {
            // Collect validation errors
            errors.addAll(e.getFieldErrors());
        }

        // If there are field validation errors, return them
        if (!errors.isEmpty()) {
            ValidationResponse response = new ValidationResponse(false, "ERROR", "Validation failed", new ArrayList<>(), new ArrayList<>());
            response.setErrors(errors);
            return response;
        }

        // Call stored procedure for additional validations
        String validationStatus = callBeforeSaveValidation(
                groupPoid,
                companyPoid,
                userPoid,
                transactionPoid,
                request.getPrincipalPoid(),
                request.getLinePoid(),
                request.getVesselPoid(),
                request.getVoyageNo(),
                request.getVoyagePoid(),
                request.getArrivalDate(),
                request.getSailDate()
        );

        // Process validation status from stored procedure
        ValidationResponse response = new ValidationResponse();
        if (validationStatus == null || validationStatus.trim().isEmpty() || validationStatus.startsWith("SUCCESS")) {
            response.setValid(true);
            response.setStatus("SUCCESS");
            response.setMessage("Validation passed");
        } else if (validationStatus.startsWith("ERROR")) {
            response.setValid(false);
            response.setStatus("ERROR");
            response.setMessage(validationStatus);
            errors.add(new ValidationError("general", validationStatus));
            response.setErrors(errors);
        } else if (validationStatus.startsWith("WARNING")) {
            response.setValid(true);
            response.setStatus("WARNING");
            response.setMessage(validationStatus);
            warnings.add(validationStatus);
            response.setWarnings(warnings);
        } else {
            response.setValid(true);
            response.setStatus("SUCCESS");
            response.setMessage(validationStatus);
        }

        return response;
    }

    @Override
    public ValidationResponse validateAfterSave(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {

        // Validate transaction exists
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        // Call stored procedure for post-save validation
        callAfterSaveValidation(
                groupPoid,
                companyPoid,
                userPoid,
                transactionPoid,
                entry.getPrincipalPoid(),
                entry.getLinePoid(),
                entry.getVesselPoid(),
                entry.getVoyageNo(),
                entry.getVoyagePoid()
        );

        // Reload entry to get any changes made by stored procedure
        entry = entryHdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PDA Entry not found with id: " + transactionPoid
                ));

        ValidationResponse response = new ValidationResponse();
        response.setValid(true);
        response.setStatus("SUCCESS");
        response.setMessage("Post-save validation completed successfully");

        return response;
    }

    @Override
    public VesselDetailsResponse getVesselDetails(BigDecimal vesselPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        if (vesselPoid == null) {
            throw new ValidationException(
                    "Vessel POID is required",
                    List.of(new ValidationError("vesselPoid", "Vessel POID is mandatory"))
            );
        }

        try {
            logger.info("[SP-20] PROC_PDA_DEFAULT_VESSEL_DTLS - vesselPoid: {}", vesselPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_DEFAULT_VESSEL_DTLS")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_VESSEL_POID", Types.NUMERIC),
                            new SqlOutParameter("OUTDATA", OracleTypes.CURSOR)
                    )
                    .returningResultSet("OUTDATA", (rs, rowNum) -> {
                        VesselDetailsResponse response = new VesselDetailsResponse();
                        response.setVesselTypePoid(rs.getBigDecimal("VESSEL_TYPE_POID"));
                        response.setImoNumber(rs.getString("IMO_NUMBER"));
                        response.setGrt(rs.getBigDecimal("GRT"));
                        response.setNrt(rs.getBigDecimal("NRT"));
                        response.setDwt(rs.getBigDecimal("DWT"));
                        return response;
                    });

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("P_LOGIN_GROUP_POID", groupPoid);
            inParams.put("P_LOGIN_COMPANY_POID", companyPoid);
            inParams.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));
            inParams.put("P_VESSEL_POID", vesselPoid);

            Map<String, Object> result = jdbcCall.execute(inParams);

            List<VesselDetailsResponse> vesselDetailsList = (List<VesselDetailsResponse>) result.get("OUTDATA");

            if (vesselDetailsList != null && !vesselDetailsList.isEmpty()) {
                VesselDetailsResponse vesselDetails = vesselDetailsList.get(0);
                logger.info("[SP-20] PROC_PDA_DEFAULT_VESSEL_DTLS - Completed. GRT: {}, NRT: {}, DWT: {}", 
                        vesselDetails.getGrt(), vesselDetails.getNrt(), vesselDetails.getDwt());
                return vesselDetails;
            }

            logger.warn("[SP-20] No vessel details returned for vesselPoid: {}", vesselPoid);
            return new VesselDetailsResponse();

        } catch (Exception e) {
            logger.error("[SP-20] PROC_PDA_DEFAULT_VESSEL_DTLS - Error: {}", e.getMessage(), e);
            return new VesselDetailsResponse();
        }
    }

    @Override
    public Map<String, Object> getVoyageDetails(BigDecimal voyagePoid, Long groupPoid, Long companyPoid, Long userPoid) {
        if (voyagePoid == null) {
            throw new ValidationException(
                    "Voyage POID is required",
                    List.of(new ValidationError("voyagePoid", "Voyage POID is mandatory"))
            );
        }

        try {
            logger.info("[SP-21] PROC_PDA_VOYAGE_DEFAULT_DTLS - voyagePoid: {}", voyagePoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_VOYAGE_DEFAULT_DTLS")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_VOYAGE_POID", Types.NUMERIC),
                            new SqlOutParameter("OUTDATA", OracleTypes.CURSOR)
                    );

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("P_LOGIN_GROUP_POID", groupPoid);
            inParams.put("P_LOGIN_COMPANY_POID", companyPoid);
            inParams.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));
            inParams.put("P_VOYAGE_POID", voyagePoid);

            Map<String, Object> result = jdbcCall.execute(inParams);

            List<Map<String, Object>> rows = (List<Map<String, Object>>) result.get("OUTDATA");

            Map<String, Object> response = new HashMap<>();
            if (!rows.isEmpty()) {
                Map<String, Object> row = rows.getFirst();
                response.put("voyageNo", row.get("VOYAGE_NO"));
                response.put("vesselPoid", row.get("VESSEL_POID"));
                response.put("linePoid", row.get("LINE_POID"));
                response.put("portPoid", row.get("PORT_POID"));
                response.put("arrivalDate", row.get("ARRIVAL_DATE"));
                response.put("sailDate", row.get("SAIL_DATE"));
                response.put("vesselTypePoid", row.get("VESSEL_TYPE_POID"));
                response.put("imoNumber", row.get("IMO_NUMBER"));
                response.put("grt", row.get("GRT"));
                response.put("nrt", row.get("NRT"));
                response.put("dwt", row.get("DWT"));
                response.put("totalQuantity", row.get("TOTAL_QUANTITY"));
                response.put("numberOfDays", row.get("NUMBER_OF_DAYS"));
            }

            logger.info("[SP-21] PROC_PDA_VOYAGE_DEFAULT_DTLS - Completed");
            return response;

        } catch (Exception e) {
            logger.error("[SP-21] PROC_PDA_VOYAGE_DEFAULT_DTLS - Error: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }


    // Private helper methods

    private void validatePdaEntryRequest(PdaEntryRequest request, Long transactionPoid) {
        List<ValidationError> errors = new ArrayList<>();

        // Validate refType is mandatory
        if (request.getRefType() == null || request.getRefType().trim().isEmpty()) {
            errors.add(new ValidationError("refType", "Ref type is mandatory"));
        }

        // Validate subCategory (Category) is mandatory
        if (request.getSubCategory() == null || request.getSubCategory().trim().isEmpty()) {
            errors.add(new ValidationError("subCategory", "Category is mandatory"));
        }

        // Validate required fields for GENERAL ref type
        if ("GENERAL".equals(request.getRefType())) {
            if (request.getPrincipalPoid() == null) {
                errors.add(new ValidationError("principalPoid", "Principal is mandatory for GENERAL ref type"));
            }
            if (request.getPortPoid() == null) {
                errors.add(new ValidationError("portPoid", "Port is mandatory for GENERAL ref type"));
            }
            if (request.getVoyageNo() == null || request.getVoyageNo().trim().isEmpty()) {
                errors.add(new ValidationError("voyageNo", "Voyage number is mandatory for GENERAL ref type"));
            }
            if (request.getLinePoid() == null) {
                errors.add(new ValidationError("linePoid", "Line is mandatory for GENERAL ref type"));
            }
            if (request.getVesselPoid() == null) {
                errors.add(new ValidationError("vesselPoid", "Vessel is mandatory for GENERAL ref type"));
            }
            if (request.getArrivalDate() == null) {
                errors.add(new ValidationError("arrivalDate", "ETA (Arrival date) is mandatory for GENERAL ref type"));
            }
            if (request.getSailDate() == null) {
                errors.add(new ValidationError("sailDate", "ETD (Sail date) is mandatory for GENERAL ref type"));
            }
            // Validate customer/nominated party for GENERAL ref type
            if (request.getNominatedPartyPoid() == null) {
                errors.add(new ValidationError("nominatedPartyPoid", "Customer is mandatory for GENERAL ref type"));
            }
        }

        // Validate date logic: sailDate (ETD) must be after or equal to arrivalDate (ETA)
        if (request.getArrivalDate() != null && request.getSailDate() != null) {
            if (request.getSailDate().isBefore(request.getArrivalDate())) {
                errors.add(new ValidationError("sailDate", "ETD should not be before the ETA"));
            }
        }

        // Validate vessel sail date: must be after or equal to sail date (ETD)
        if (request.getSailDate() != null && request.getVesselSailDate() != null) {
            if (request.getVesselSailDate().isBefore(request.getSailDate())) {
                errors.add(new ValidationError("vesselSailDate", "Vessel Sail Date should not be before the ETD"));
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    private boolean canEdit(PdaEntryHdr entry) {
        String status = entry.getStatus() != null ? entry.getStatus().trim() : null;
        String refType = entry.getRefType() != null ? entry.getRefType().trim() : null;
        String principalApproved = entry.getPrincipalApproved() != null ? entry.getPrincipalApproved().trim() : null;
        
        // For GENERAL ref type
        if ("GENERAL".equals(refType)) {
            if ("Y".equals(principalApproved)) {
                throw new ValidationException(
                        "Already Pricipal Aprroved",
                        List.of(new ValidationError("principalApproved", "Entry cannot be edited because it is already approved by principal"))
                );
            }
            if ("CONFIRMED".equalsIgnoreCase(status)) {
                throw new ValidationException(
                        "Status is Confirmed",
                        List.of(new ValidationError("status", "Entry cannot be edited because status is CONFIRMED"))
                );
            }
        }
        // For other ref types, check status
        if ("CONFIRMED".equalsIgnoreCase(status)) {
            throw new ValidationException(
                    "Status is Confirmed",
                    List.of(new ValidationError("status", "Entry cannot be edited because status is CONFIRMED"))
            );
        }
        if ("CLOSED".equalsIgnoreCase(status)) {
            throw new ValidationException(
                    "Status is Closed",
                    List.of(new ValidationError("status", "Entry cannot be edited because status is CLOSED"))
            );
        }
        return true;
    }

    private void mapRequestToEntity(PdaEntryRequest request, PdaEntryHdr entity) {
        // Map all fields from request to entity
        LocalDate transactionDate = request.getTransactionDate() != null
                ? request.getTransactionDate()
                : DateUtil.getCurrentDateInUserTimeZone();
        entity.setTransactionDate(transactionDate);
        entity.setPrincipalPoid(request.getPrincipalPoid());
        entity.setPrincipalName(request.getPrincipalName());
        entity.setPrincipalContact(request.getPrincipalContact());
        entity.setVoyagePoid(request.getVoyagePoid());
        entity.setVoyageNo(request.getVoyageNo());
        entity.setVesselPoid(request.getVesselPoid());
        entity.setVesselTypePoid(request.getVesselTypePoid());
        
        // Log vessel details from request
        logger.info("Mapping request to entity - GRT from request: {}, NRT from request: {}, DWT from request: {}",
                request.getGrt(), request.getNrt(), request.getDwt());
        
        entity.setGrt(request.getGrt());
        entity.setNrt(request.getNrt());
        entity.setDwt(request.getDwt());
        
        logger.info("After mapping - GRT on entity: {}, NRT on entity: {}, DWT on entity: {}",
                entity.getGrt(), entity.getNrt(), entity.getDwt());
        
        entity.setImoNumber(request.getImoNumber());
        entity.setArrivalDate(request.getArrivalDate());
        entity.setSailDate(request.getSailDate());
        entity.setActualArrivalDate(request.getActualArrivalDate());
        entity.setActualSailDate(request.getActualSailDate());
        entity.setVesselSailDate(request.getVesselSailDate());
        entity.setPortPoid(request.getPortPoid());
        entity.setPortDescription(request.getPortDescription());
        entity.setLinePoid(request.getLinePoid());
        entity.setComodityPoid(request.getComodityPoid());
        entity.setOperationType(request.getOperationType());
        entity.setHarbourCallType(request.getHarbourCallType());
        entity.setImportQty(request.getImportQty());
        entity.setExportQty(request.getExportQty());
        entity.setTranshipmentQty(request.getTranshipmentQty());
        entity.setTotalQuantity(request.getTotalQuantity());
        entity.setUnit(request.getUnit());
        entity.setNumberOfDays(request.getNumberOfDays());
        entity.setCurrencyCode(request.getCurrencyCode());
        entity.setCurrencyRate(request.getCurrencyRate());
        entity.setTotalAmount(request.getTotalAmount());
        // Note: totalTax and totalAmountFc are calculated fields, not set from request
        entity.setCostCentrePoid(request.getCostCentrePoid());
        entity.setSalesmanPoid(request.getSalesmanPoid());
        entity.setTermsPoid(request.getTermsPoid());
        entity.setAddressPoid(request.getAddressPoid());
        entity.setRefType(request.getRefType());
        entity.setSubCategory(request.getSubCategory());
        entity.setStatus(request.getStatus());
        entity.setCargoDetails(request.getCargoDetails());
        entity.setRemarks(request.getRemarks());
        entity.setVesselVerified(request.getVesselVerified());
        entity.setVesselVerifiedDate(request.getVesselVerifiedDate());
        entity.setVesselVerifiedBy(request.getVesselVerifiedBy());
        entity.setVesselHandledBy(request.getVesselHandledBy());
        entity.setUrgentApproval(request.getUrgentApproval());
        entity.setPrincipalApproved(request.getPrincipalApproved());
        entity.setPrincipalApprovedDate(request.getPrincipalApprovedDate());
        entity.setPrincipalApprovedBy(request.getPrincipalApprovedBy());
        entity.setPrincipalAprvlDays(request.getPrincipalAprvlDays());
        entity.setReminderMinutes(request.getReminderMinutes());
        entity.setPrintPrincipal(request.getPrintPrincipal());
        entity.setFdaRef(request.getFdaRef());
        entity.setFdaPoid(request.getFdaPoid());
        entity.setMultipleFda(request.getMultipleFda());
        entity.setNominatedPartyType(request.getNominatedPartyType());
        entity.setNominatedPartyPoid(request.getNominatedPartyPoid());
        entity.setBankPoid(request.getBankPoid());
        entity.setBusinessRefBy(request.getBusinessRefBy());
        entity.setPmiDocument(request.getPmiDocument());
        entity.setCancelRemark(request.getCancelRemark());
        entity.setMenasDues(request.getMenasDues());
        entity.setDocumentSubmittedDate(request.getDocumentSubmittedDate());
        entity.setDocumentSubmittedBy(request.getDocumentSubmittedBy());
        entity.setDocumentSubmittedStatus(request.getDocumentSubmittedStatus());
        entity.setDocumentReceivedDate(request.getDocumentReceivedDate());
        entity.setDocumentReceivedFrom(request.getDocumentReceivedFrom());
        entity.setDocumentReceivedStatus(request.getDocumentReceivedStatus());
        entity.setSubmissionAcceptedDate(request.getSubmissionAcceptedDate());
        entity.setSubmissionAcceptedBy(request.getSubmissionAcceptedBy());
        entity.setVerificationAcceptedDate(request.getVerificationAcceptedDate());
        entity.setVerificationAcceptedBy(request.getVerificationAcceptedBy());
        entity.setAcctsCorrectionRemarks(request.getAcctsCorrectionRemarks());
        entity.setAcctsReturnedDate(request.getAcctsReturnedDate());
    }

    private PdaEntryResponse toResponse(PdaEntryHdr entity) {
        PdaEntryResponse response = new PdaEntryResponse();
        response.setTransactionPoid(entity.getTransactionPoid());
        response.setDocRef(entity.getDocRef());
        response.setTransactionRef(entity.getTransactionRef());
        response.setDeleted(entity.getDeleted());
        response.setTransactionDate(entity.getTransactionDate());
        response.setPrincipalPoid(entity.getPrincipalPoid());
        response.setPrincipalDet(lovService.getLovItemByPoid(entity.getPrincipalPoid() != null ? entity.getPrincipalPoid().longValue() : null, "PRINCIPAL_MASTER", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        response.setPrincipalName(entity.getPrincipalName());
        response.setPrincipalContact(entity.getPrincipalContact());
        response.setVoyagePoid(entity.getVoyagePoid());
        response.setVoyageDet(lovService.getLovItemByPoid(entity.getVoyagePoid() != null ? entity.getVoyagePoid().longValue() : null, "VESSAL_VOYAGE", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        response.setVoyageNo(entity.getVoyageNo());
        response.setVesselPoid(entity.getVesselPoid());
        response.setVesselDet(lovService.getLovItemByPoid(entity.getVesselPoid() != null ? entity.getVesselPoid().longValue() : null, "VESSEL_MASTER", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        response.setVesselTypePoid(entity.getVesselTypePoid());
        response.setVesselTypeDet(lovService.getLovItemByPoid(entity.getVesselTypePoid() != null ? entity.getVesselTypePoid().longValue() : null, "VESSEL_TYPE_MASTER", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        response.setGrt(entity.getGrt());
        response.setNrt(entity.getNrt());
        response.setDwt(entity.getDwt());
        response.setImoNumber(entity.getImoNumber());
        response.setArrivalDate(entity.getArrivalDate());
        response.setSailDate(entity.getSailDate());
        response.setActualArrivalDate(entity.getActualArrivalDate());
        response.setActualSailDate(entity.getActualSailDate());
        response.setVesselSailDate(entity.getVesselSailDate());
        response.setPortPoid(entity.getPortPoid());
        response.setPortDet(lovService.getLovItemByPoid(entity.getPortPoid() != null ? entity.getPortPoid().longValue() : null, "PDA_PORT_MASTER", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        response.setPortDescription(entity.getPortDescription());
        response.setLinePoid(entity.getLinePoid());
        response.setLineDet(lovService.getLovItemByPoid(entity.getLinePoid() != null ? entity.getLinePoid().longValue() : null, "LINE_MASTER_ALL", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        response.setComodityPoid(entity.getComodityPoid());
        try {
            response.setComodityDet(lovService.getLovItemByPoid(entity.getComodityPoid() != null ? Long.valueOf(entity.getComodityPoid()) : null, "COMODITY", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        } catch (Exception e) {
            try {
                response.setComodityDet(lovService.getLovItemByCode(entity.getComodityPoid() != null ? entity.getComodityPoid() : null, "COMODITY", entity.getGroupPoid(), entity.getCompanyPoid(), null));
            } catch (Exception ex) {
                //Do not do anything
            }
        }
        response.setOperationType(entity.getOperationType());
        response.setOperationTypeDet(lovService.getLovItemByCode(entity.getOperationType(), "PDA_OPERATION_TYPES", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        response.setHarbourCallType(entity.getHarbourCallType());
        response.setImportQty(entity.getImportQty());
        response.setExportQty(entity.getExportQty());
        response.setTranshipmentQty(entity.getTranshipmentQty());
        response.setTotalQuantity(entity.getTotalQuantity());
        response.setUnit(entity.getUnit());
        response.setUnitDet(lovService.getLovItemByCode(entity.getUnit(), "UNIT_MASTER", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        response.setNumberOfDays(entity.getNumberOfDays());
        response.setCurrencyCode(entity.getCurrencyCode());
        response.setCurrencyDet(lovService.getLovItemByCode(entity.getCurrencyCode(), "CURRENCY", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        response.setCurrencyRate(entity.getCurrencyRate());
        response.setTotalAmount(entity.getTotalAmount());
        response.setCostCentrePoid(entity.getCostCentrePoid());
        response.setSalesmanPoid(entity.getSalesmanPoid());
        response.setSalesmanDet(lovService.getLovItemByPoid(entity.getSalesmanPoid() != null ? entity.getSalesmanPoid().longValue() : null, "SALESMAN", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        response.setTermsPoid(entity.getTermsPoid());
        response.setAddressPoid(entity.getAddressPoid());
        response.setRefType(entity.getRefType());
        response.setRefTypeDet(lovService.getLovItemByCode(entity.getRefType(), "PDA_REF_TYPE", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        response.setSubCategory(entity.getSubCategory());
        response.setSubCategoryDet(lovService.getLovItemByCode(entity.getSubCategory(), "PDA_SUB_CATEGORY", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        response.setStatus(entity.getStatus());
        response.setCargoDetails(entity.getCargoDetails());
        response.setRemarks(entity.getRemarks());
        response.setVesselVerified(entity.getVesselVerified());
        response.setVesselVerifiedDate(entity.getVesselVerifiedDate());
        response.setVesselVerifiedBy(entity.getVesselVerifiedBy());
        response.setVesselHandledBy(entity.getVesselHandledBy());
        response.setVesselHandledByDet(lovService.getLovItemByPoid(entity.getVesselHandledBy() != null ? entity.getVesselHandledBy().longValue() : null, "PDA_USER_MASTER", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        response.setUrgentApproval(entity.getUrgentApproval());
        response.setPrincipalApproved(entity.getPrincipalApproved());
        response.setPrincipalApprovedDate(entity.getPrincipalApprovedDate());
        response.setPrincipalApprovedBy(entity.getPrincipalApprovedBy());
        response.setPrincipalAprvlDays(entity.getPrincipalAprvlDays());
        response.setReminderMinutes(entity.getReminderMinutes());
        response.setPrintPrincipal(entity.getPrintPrincipal());
        response.setPrintPrincipalDet(lovService.getLovItemByPoid(entity.getPrintPrincipal() != null ? entity.getPrintPrincipal().longValue() : null, "PDA_PRINCIPAL_PRINT", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        response.setFdaRef(entity.getFdaRef());
        response.setFdaPoid(entity.getFdaPoid());
        response.setMultipleFda(entity.getMultipleFda());
        response.setNominatedPartyType(entity.getNominatedPartyType());
        response.setNominatedPartyTypeDet(lovService.getLovItemByCode(entity.getNominatedPartyType(), "PDA_NOMINATED_PARTY_TYPE", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        response.setNominatedPartyPoid(entity.getNominatedPartyPoid());
        response.setBankPoid(entity.getBankPoid());
        response.setBankDet(lovService.getLovItemByPoid(entity.getBankPoid() != null ? entity.getBankPoid().longValue() : null, "BANK_MASTER_COMPANYWISE", entity.getGroupPoid(), entity.getCompanyPoid(), null));
        response.setBusinessRefBy(entity.getBusinessRefBy());
        response.setPmiDocument(entity.getPmiDocument());
        response.setCancelRemark(entity.getCancelRemark());
        response.setMenasDues(entity.getMenasDues());
        response.setDocumentSubmittedDate(entity.getDocumentSubmittedDate());
        response.setDocumentSubmittedBy(entity.getDocumentSubmittedBy());
        response.setDocumentSubmittedStatus(entity.getDocumentSubmittedStatus());
        response.setDocumentReceivedDate(entity.getDocumentReceivedDate());
        response.setDocumentReceivedFrom(entity.getDocumentReceivedFrom());
        response.setDocumentReceivedStatus(entity.getDocumentReceivedStatus());
        response.setSubmissionAcceptedDate(entity.getSubmissionAcceptedDate());
        response.setSubmissionAcceptedBy(entity.getSubmissionAcceptedBy());
        response.setVerificationAcceptedDate(entity.getVerificationAcceptedDate());
        response.setVerificationAcceptedBy(entity.getVerificationAcceptedBy());
        response.setAcctsCorrectionRemarks(entity.getAcctsCorrectionRemarks());
        response.setAcctsReturnedDate(entity.getAcctsReturnedDate());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());
        response.setChargeDetails(getChargeDetails(entity.getTransactionPoid(), entity.getGroupPoid(), entity.getCompanyPoid()));
        response.setVehicleDetails(getVehicleDetails(entity.getTransactionPoid(), entity.getGroupPoid(), entity.getCompanyPoid()));
        response.setTdrDetails(getTdrDetails(entity.getTransactionPoid(), entity.getGroupPoid(), entity.getCompanyPoid()));
        response.setAcknowledgmentDetails(getAcknowledgmentDetails(entity.getTransactionPoid(), Long.valueOf(entity.getGroupPoid().toString()), entity.getCompanyPoid()));

        if (StringUtils.isNotBlank(response.getNominatedPartyType()) && "CUSTOMER".equalsIgnoreCase(response.getNominatedPartyType())) {
            response.setNominatedPartyDet(lovService.getLovItemByPoid(Long.valueOf(String.valueOf(response.getNominatedPartyPoid())), "PDA_NOMINATED_PARTY_CUSTOMER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        }
        if (StringUtils.isNotBlank(response.getNominatedPartyType()) && "PRINCIPAL".equalsIgnoreCase(response.getNominatedPartyType())) {
            response.setNominatedPartyDet(lovService.getLovItemByPoid(Long.valueOf(String.valueOf(response.getNominatedPartyPoid())), "PDA_NOMINATED_PARTY_PRINCIPAL", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        }
        return response;
    }

    private void setDefaultCurrency(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid, BigDecimal principalPoid, PdaEntryHdr entry) {
        try {
            logger.info("[SP-Custom] PROC_PDA_SET_DEFAULT_CURRENCY - transactionPoid: {}, principalPoid: {}", transactionPoid, principalPoid);
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_SET_DEFAULT_CURRENCY")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.VARCHAR),
                            new SqlParameter("P_DOC_ID", Types.VARCHAR),
                            new SqlParameter("P_DOC_KEY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOV_NAME", Types.VARCHAR),
                            new SqlParameter("P_LOV_VALUE", Types.NUMERIC)
                    );
            Map<String, Object> inParams = new HashMap<>();
            inParams.put("P_LOGIN_GROUP_POID", groupPoid);
            inParams.put("P_LOGIN_COMPANY_POID", companyPoid);
            inParams.put("P_LOGIN_USER_POID", userPoid);
            inParams.put("P_DOC_ID", "PDA_ENTRY");
            inParams.put("P_DOC_KEY_POID", transactionPoid);
            inParams.put("P_LOV_NAME", "CURRENCY");
            inParams.put("P_LOV_VALUE", principalPoid);
            jdbcCall.execute(inParams);
            logger.info("[SP-Custom] PROC_PDA_SET_DEFAULT_CURRENCY - Completed");
        } catch (Exception e) {
            logger.error("[SP-Custom] PROC_PDA_SET_DEFAULT_CURRENCY - Error: {}", e.getMessage());
        }
    }

    private String callBeforeSaveValidation(
            Long groupPoid, Long companyPoid, Long userPoid, Long pdaPoid,
            BigDecimal principalPoid, BigDecimal linePoid, BigDecimal vesselPoid,
            String voyageNo, BigDecimal voyagePoid,
            LocalDate arrivalDate, LocalDate sailDate
    ) {
        try {
            logger.info("[SP-18] PROC_PDA_BEFORE_SAVE_VALIDATE - pdaPoid: {}, principalPoid: {}", pdaPoid, principalPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_BEFORE_SAVE_VALIDATE");

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("P_LOGIN_GROUP_POID", groupPoid);
            inParams.put("P_LOGIN_COMPANY_POID", companyPoid);
            inParams.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));  // NUMBER
            inParams.put("P_PDA_POID", new BigDecimal(pdaPoid));           // NUMBER

            inParams.put("P_PRINCIPAL_POID", principalPoid.toString());    // VARCHAR2
            inParams.put("P_LINE_POID", linePoid.toString());              // VARCHAR2
            inParams.put("P_VESSEL_POID", vesselPoid.toString());          // VARCHAR2
            inParams.put("P_VOYAGE_NO", voyageNo);                         // VARCHAR2
            inParams.put("P_VESSEL_VOYAGE_POID", voyagePoid.toString());   // VARCHAR2

            // Extra date parameters arriving just after voyagePoid
            inParams.put("P_ARRIVAL_DATE", arrivalDate);                   // DATE
            inParams.put("P_SAIL_DATE", sailDate);                         // DATE

            Map<String, Object> result = jdbcCall.execute(inParams);

            String spResult = (String) result.get("P_RESULT");

            logger.info("[SP-18] PROC_PDA_BEFORE_SAVE_VALIDATE - Completed. Result: {}", spResult);

            return spResult;

        } catch (Exception e) {
            logger.error("[SP-18] PROC_PDA_BEFORE_SAVE_VALIDATE - Error: {}", e.getMessage(), e);
            return null;
        }
    }


    private void callAfterSaveValidation(
            Long groupPoid, Long companyPoid, Long userPoid, Long pdaPoid,
            BigDecimal principalPoid, BigDecimal linePoid, BigDecimal vesselPoid,
            String voyageNo, BigDecimal voyagePoid
    ) {
        try {
            logger.info("[SP-1] PROC_PDA_AFTER_SAVE_VALIDATE - transactionPoid: {}", pdaPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_AFTER_SAVE_VALIDATE");

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("P_LOGIN_GROUP_POID", groupPoid);
            inParams.put("P_LOGIN_COMPANY_POID", companyPoid);
            inParams.put("P_LOGIN_USER_POID", new BigDecimal(userPoid)); // matches NUMBER
            inParams.put("P_PDA_POID", new BigDecimal(pdaPoid));

            inParams.put("P_PRINCIPAL_POID", principalPoid.toString()); // VARCHAR2
            inParams.put("P_LINE_POID", linePoid.toString()); // VARCHAR2
            inParams.put("P_VESSEL_POID", vesselPoid.toString()); // VARCListHAR2
            inParams.put("P_VOYAGE_NO", voyageNo);
            inParams.put("P_VESSEL_VOYAGE_POID", voyagePoid.toString()); // VARCHAR2

            Map<String, Object> result = jdbcCall.execute(inParams);

            String spResult = (String) result.get("P_RESULT");

            logger.info("[SP-1] PROC_PDA_AFTER_SAVE_VALIDATE - Completed. Result: {}", spResult);

        } catch (Exception e) {
            logger.error("[SP-1] PROC_PDA_AFTER_SAVE_VALIDATE - Error: {}", e.getMessage(), e);
        }
    }


    private String callEditValidation(
            Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid
    ) {
        try {
            logger.info("[SP-19] PROC_PDA_EDIT_VALIDATION - transactionPoid: {}", transactionPoid);
            String sql = "{ call PROC_PDA_EDIT_VALIDATION(?, ?, ?, ?) }";
            try {
                String result = jdbcTemplate.queryForObject(sql, String.class, groupPoid, companyPoid, userPoid, transactionPoid);
                logger.info("[SP-19] PROC_PDA_EDIT_VALIDATION - Result: {}", result);
                return result;
            } catch (Exception ex) {
                logger.debug("[SP-19] PROC_PDA_EDIT_VALIDATION - No result returned, executing as void");
                jdbcTemplate.update(sql, groupPoid, companyPoid, userPoid, transactionPoid);
                logger.info("[SP-19] PROC_PDA_EDIT_VALIDATION - Completed");
                return null;
            }
        } catch (Exception e) {
            logger.error("[SP-19] PROC_PDA_EDIT_VALIDATION - Error: {}", e.getMessage());
            return null;
        }
    }

    // Charge Details Helper Methods

    private void createChargeDetail(Long transactionPoid, PdaEntryChargeDetailRequest request,
                                    String userId, LocalDateTime now, Long companyPoid) {
        // Validate required fields
        validateChargeDetailRequest(request);

        // Create new entity
        PdaEntryDtl detail = new PdaEntryDtl();
        detail.setTransactionPoid(transactionPoid);
        long detRowId = System.nanoTime() % 1000000;
        detail.setDetRowId(detRowId > 0 ? detRowId : Math.abs(detRowId) + 1);

        // Map request to entity
        mapChargeDetailRequestToEntity(request, detail);

        // Auto-populate tax if chargePoid is provided
        if (request.getChargePoid() != null && request.getPrincipalPoid() != null) {
            TaxInfo taxInfo = getChargeTaxInfo(companyPoid, new java.util.Date(), "PRINCIPAL", request.getPrincipalPoid(), request.getChargePoid());
            if (taxInfo != null) {
                detail.setTaxPoid(taxInfo.getTaxPoid());
                detail.setTaxPercentage(taxInfo.getTaxPercentage());
            }
        }

        // Calculate amounts
        calculateAmounts(detail);

        // Set audit fields

        // Save
        PdaEntryDtl saved = entryDtlRepository.save(detail);
        String logDetail = String.format("Row Created on [PDA Entry Charge Details] with detRowId: %s", saved.getDetRowId());
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
    }

    private void updateChargeDetail(Long transactionPoid, PdaEntryChargeDetailRequest request,
                                    String userId, LocalDateTime now, Long companyPoid) {
        logger.info("[AUDIT-LOG] Updating charge detail - transactionPoid: {}, detRowId: {}", transactionPoid, request.getDetRowId());
        
        // Validate detail exists
        PdaEntryDtlId detailId = new PdaEntryDtlId(transactionPoid, request.getDetRowId());
        PdaEntryDtl detail = entryDtlRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Charge detail not found with id: " + request.getDetRowId()
                ));

        // Create old detail copy with proper ID
        PdaEntryDtl oldDetail = new PdaEntryDtl();
        oldDetail.setTransactionPoid(detail.getTransactionPoid());
        oldDetail.setDetRowId(detail.getDetRowId());
        BeanUtils.copyProperties(detail, oldDetail);
        logger.info("[AUDIT-LOG] Old detail copied - transactionPoid: {}, detRowId: {}, qty: {}, rate: {}", 
            oldDetail.getTransactionPoid(), oldDetail.getDetRowId(), oldDetail.getQty(), oldDetail.getPdaRate());

        // Validate required fields
        validateChargeDetailRequest(request);

        // Store old values for comparison
        BigDecimal oldChargePoid = detail.getChargePoid();
        BigDecimal oldQty = detail.getQty();
        BigDecimal oldDays = detail.getDays();
        BigDecimal oldPdaRate = detail.getPdaRate();
        BigDecimal oldTaxPercentage = detail.getTaxPercentage();

        // Map request to entity
        mapChargeDetailRequestToEntity(request, detail);

        // Auto-populate tax if chargePoid changed
        if (request.getChargePoid() != null && request.getPrincipalPoid() != null &&
                !Objects.equals(oldChargePoid, request.getChargePoid())) {
            TaxInfo taxInfo = getChargeTaxInfo(companyPoid, new java.util.Date(), "PRINCIPAL", request.getPrincipalPoid(), request.getChargePoid());
            if (taxInfo != null) {
                detail.setTaxPoid(taxInfo.getTaxPoid());
                detail.setTaxPercentage(taxInfo.getTaxPercentage());
            }
        }

        // Recalculate amounts if relevant fields changed
        boolean needsRecalculation = !Objects.equals(oldQty, request.getQty()) ||
                !Objects.equals(oldDays, request.getDays()) ||
                !Objects.equals(oldPdaRate, request.getPdaRate()) ||
                !Objects.equals(oldTaxPercentage, request.getTaxPercentage()) ||
                request.getTaxAmount() == null;

        if (needsRecalculation) {
            calculateAmounts(detail);
        } else if (request.getTaxAmount() != null) {
            // If tax amount is manually set, recalculate total amount
            BigDecimal baseAmount = detail.getQty()
                    .multiply(detail.getDays())
                    .multiply(detail.getPdaRate())
                    .setScale(3, java.math.RoundingMode.HALF_UP);
            detail.setAmount(baseAmount.add(request.getTaxAmount()).setScale(3, java.math.RoundingMode.HALF_UP));
        }

        // Update audit fields

        // Save and log
        detail = entryDtlRepository.save(detail);
        entityManager.flush();
        logger.info("[AUDIT-LOG] Detail saved and flushed - qty: {}, rate: {}", detail.getQty(), detail.getPdaRate());

        String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", detail.getTransactionPoid(), detail.getDetRowId());
        logger.info("[AUDIT-LOG] Calling loggingService.createLog with logDetail: {}", logDetail);
        loggingService.createLog(oldDetail, detail, PdaEntryDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
        logger.info("[AUDIT-LOG] Logging completed for detRowId: {}", detail.getDetRowId());
    }

    private void deleteChargeDetailRecord(Long transactionPoid, Long detRowId) {
        PdaEntryDtlId detailId = new PdaEntryDtlId(transactionPoid, detRowId);
        PdaEntryDtl detail = entryDtlRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Charge detail not found with id: " + detRowId
                ));
        loggingService.logDelete(detail, UserContext.getDocumentId(), transactionPoid.toString());
        entryDtlRepository.delete(detail);
    }

    private void validateChargeDetailRequest(PdaEntryChargeDetailRequest request) {
        List<ValidationError> errors = new ArrayList<>();

        if (request.getChargePoid() == null) {
            errors.add(new ValidationError("chargePoid", "Charge POID is mandatory"));
        }
        if (request.getQty() == null) {
            errors.add(new ValidationError("qty", "Quantity is mandatory"));
        }
        if (request.getDays() == null) {
            errors.add(new ValidationError("days", "Days is mandatory"));
        }
        if (request.getPdaRate() == null) {
            errors.add(new ValidationError("pdaRate", "PDA Rate is mandatory"));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    private void mapChargeDetailRequestToEntity(PdaEntryChargeDetailRequest request, PdaEntryDtl entity) {
        entity.setChargePoid(request.getChargePoid());
        if (request.getRateTypePoid() != null && request.getRateTypePoid().signum() > 0) {
            entity.setRateTypePoid(request.getRateTypePoid());
        }
        entity.setPrincipalPoid(request.getPrincipalPoid());
        entity.setCurrencyCode(request.getCurrencyCode());
        entity.setCurrencyRate(request.getCurrencyRate());
        entity.setQty(request.getQty());
        entity.setDays(request.getDays());
        entity.setPdaRate(request.getPdaRate());
        entity.setTaxPoid(request.getTaxPoid());
        entity.setTaxPercentage(request.getTaxPercentage());
        entity.setTaxAmount(request.getTaxAmount());
        entity.setAmount(request.getAmount());
        entity.setFdaAmount(request.getFdaAmount());
        entity.setFdaDocRef(request.getFdaDocRef());
        entity.setFdaPoid(request.getFdaPoid());
        entity.setFdaCreationType(request.getFdaCreationType());
        entity.setDataSource(request.getDataSource());
        entity.setDetailFrom(request.getDetailFrom());
        entity.setManual(request.getManual());
        entity.setSeqno(request.getSeqno());
        entity.setRemarks(request.getRemarks());
    }

    private void calculateAmounts(PdaEntryDtl detail) {
        if (detail.getQty() == null || detail.getDays() == null || detail.getPdaRate() == null) {
            return;
        }

        // Calculate base amount: QTY × DAYS × PDA_RATE
        BigDecimal baseAmount = detail.getQty()
                .multiply(detail.getDays())
                .multiply(detail.getPdaRate())
                .setScale(3, java.math.RoundingMode.HALF_UP);

        // Calculate tax amount if tax percentage is provided
        BigDecimal taxAmount = detail.getTaxAmount();
        if (taxAmount == null && detail.getTaxPercentage() != null) {
            taxAmount = baseAmount
                    .multiply(detail.getTaxPercentage())
                    .divide(BigDecimal.valueOf(100), 3, java.math.RoundingMode.HALF_UP);
            detail.setTaxAmount(taxAmount);
        }

        // Calculate total amount: BASE_AMOUNT + TAX_AMOUNT
        if (taxAmount == null) {
            taxAmount = BigDecimal.ZERO;
        }
        BigDecimal totalAmount = baseAmount.add(taxAmount).setScale(3, java.math.RoundingMode.HALF_UP);
        detail.setAmount(totalAmount);
    }

    public TaxInfo getChargeTaxInfo(Long companyPoid, Date transactionDate, String partyType, BigDecimal partyPoid, BigDecimal chargePoid) {
        try {
            logger.info("[SP-7] PROC_GET_CHARGE_TAX_PER_V3 - chargePoid: {}, partyPoid: {}", chargePoid, partyPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_GET_CHARGE_TAX_PER_V3")
                    .declareParameters(
                            new SqlParameter("P_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_TRANSACTION_DATE", Types.DATE),
                            new SqlParameter("P_PARTY_TYPE", Types.VARCHAR),
                            new SqlParameter("P_PARTY_POID", Types.NUMERIC),
                            new SqlParameter("P_CHARGE_POID", Types.NUMERIC),
                            new SqlOutParameter("OUTDATA", OracleTypes.CURSOR)
                    )
                    .returningResultSet("OUTDATA", (rs, rowNum) -> new TaxInfo(
                            rs.getBigDecimal("TAX_POID"),
                            rs.getBigDecimal("PERCENTAGE")
                    ));

            Map<String, Object> params = new HashMap<>();
            params.put("P_COMPANY_POID", companyPoid);
            params.put("P_TRANSACTION_DATE", transactionDate);
            params.put("P_PARTY_TYPE", partyType);
            params.put("P_PARTY_POID", partyPoid);
            params.put("P_CHARGE_POID", chargePoid);

            Map<String, Object> result = jdbcCall.execute(params);
            List<TaxInfo> taxInfoList = (List<TaxInfo>) result.get("OUTDATA");

            if (!taxInfoList.isEmpty()) {
                TaxInfo taxInfo = taxInfoList.get(0);
                logger.info("[SP-7] PROC_GET_CHARGE_TAX_PER_V3 - Tax %: {}", taxInfo.getTaxPercentage());
                return taxInfo;
            }

            logger.warn("[SP-7] No tax info returned");
            return null;

        } catch (Exception e) {
            logger.error("[SP-7] PROC_GET_CHARGE_TAX_PER_V3 - Error: {}", e.getMessage(), e);
            return null;
        }
    }

    private void recalculateHeaderTotalAmount(Long transactionPoid, String userId) {
        BigDecimal totalAmount = entryDtlRepository.calculateTotalAmount(transactionPoid);

        PdaEntryHdr entry = entryHdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PDA Entry not found with id: " + transactionPoid
                ));

        entry.setTotalAmount(totalAmount);
        entryHdrRepository.save(entry);
    }

    private void validateRecalculateFields(PdaEntryHdr entry) {
        List<ValidationError> errors = new ArrayList<>();

        if (entry.getVesselTypePoid() == null) {
            errors.add(new ValidationError("vesselTypePoid", "Vessel type is required for recalculation"));
        }
        if (entry.getGrt() == null) {
            errors.add(new ValidationError("grt", "GRT is required for recalculation"));
        }
        if (entry.getNrt() == null) {
            errors.add(new ValidationError("nrt", "NRT is required for recalculation"));
        }
        if (entry.getDwt() == null) {
            errors.add(new ValidationError("dwt", "DWT is required for recalculation"));
        }
        if (entry.getPortPoid() == null) {
            errors.add(new ValidationError("portPoid", "Port is required for recalculation"));
        }
        if (entry.getSailDate() == null) {
            errors.add(new ValidationError("sailDate", "Sail date is required for recalculation"));
        }
        if (entry.getNumberOfDays() == null) {
            errors.add(new ValidationError("numberOfDays", "Number of days is required for recalculation"));
        }
        if (entry.getHarbourCallType() == null || entry.getHarbourCallType().trim().isEmpty()) {
            errors.add(new ValidationError("harbourCallType", "Harbour call type is required for recalculation"));
        }
        if (entry.getTotalQuantity() == null) {
            errors.add(new ValidationError("totalQuantity", "Total quantity is required for recalculation"));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    private PdaEntryChargeDetailResponse toChargeDetailResponse(PdaEntryDtl entity, Long groupPoid, Long companyPoid) {
        PdaEntryChargeDetailResponse response = new PdaEntryChargeDetailResponse();
        response.setTransactionPoid(entity.getTransactionPoid());
        response.setDetRowId(entity.getDetRowId());
        response.setChargePoid(entity.getChargePoid());
        response.setRateTypePoid(entity.getRateTypePoid());
        response.setPrincipalPoid(entity.getPrincipalPoid());
        response.setCurrencyCode(entity.getCurrencyCode());
        response.setCurrencyRate(entity.getCurrencyRate());
        response.setQty(entity.getQty());
        response.setDays(entity.getDays());
        response.setPdaRate(entity.getPdaRate());
        response.setTaxPoid(entity.getTaxPoid());
        response.setTaxPercentage(entity.getTaxPercentage());
        response.setTaxAmount(entity.getTaxAmount());
        response.setAmount(entity.getAmount());
        response.setFdaAmount(entity.getFdaAmount());
        response.setFdaDocRef(entity.getFdaDocRef());
        response.setFdaPoid(entity.getFdaPoid());
        response.setFdaCreationType(entity.getFdaCreationType());
        
        // Get FDA creation type detail using LOV service
        response.setFdaCreationTypeDet(lovService.getLovItemByCode(
            entity.getFdaCreationType(), 
            "FDA_CREATION_TYPE", 
            groupPoid, 
            companyPoid, 
            null));
        
        response.setDataSource(entity.getDataSource());
        response.setDetailFrom(entity.getDetailFrom());
        response.setManual(entity.getManual());
        response.setSeqno(entity.getSeqno());
        response.setRemarks(entity.getRemarks());
        response.setOldChargeCode(entity.getOldChargeCode());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());
        return response;
    }

    private void callClearChargeDetails(Long groupPoid, Long userPoid, Long companyPoid, Long transactionPoid) {
        try {
            logger.info("[SP-17] PROC_PDA_ENTRY_DTL_CLEAR - transactionPoid: {}", transactionPoid);
            String sql = "{ call PROC_PDA_ENTRY_DTL_CLEAR(?, ?, ?, ?) }";
            jdbcTemplate.update(sql, groupPoid, userPoid, companyPoid, transactionPoid);
            logger.info("[SP-17] PROC_PDA_ENTRY_DTL_CLEAR - Completed");
        } catch (Exception e) {
            logger.error("[SP-17] PROC_PDA_ENTRY_DTL_CLEAR - Error: {}, falling back to direct delete", e.getMessage());
            entryDtlRepository.deleteByTransactionPoid(transactionPoid);
        }
    }

    private String callReCalculateCharges(
            Long groupPoid, Long userPoid, Long companyPoid, Long transactionPoid,
            BigDecimal vesselPoid, BigDecimal vesselTypePoid, BigDecimal grt, BigDecimal nrt, BigDecimal dwt,
            BigDecimal portPoid, LocalDate arrivalDate, LocalDate sailDate,
            String harbourCallType, BigDecimal totalQuantity, BigDecimal numberOfDays, BigDecimal principalPoid
    ) {
        try {
            logger.info("[SP-3] PROC_PDA_RE_CALCULATE - transactionPoid: {}, vesselPoid: {}",
                    transactionPoid, vesselPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_RE_CALCULATE_DTLS")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_PDA_POID", Types.NUMERIC),
                            new SqlParameter("P_VESSEL_POID", Types.NUMERIC),
                            new SqlParameter("P_VESSEL_TYPE_POID", Types.NUMERIC),
                            new SqlParameter("P_GRT", Types.NUMERIC),
                            new SqlParameter("P_NRT", Types.NUMERIC),
                            new SqlParameter("P_DWT", Types.NUMERIC),
                            new SqlParameter("P_PORT_POID", Types.NUMERIC),
                            new SqlParameter("P_ARRIVAL_DATE", Types.DATE),
                            new SqlParameter("P_SAIL_DATE", Types.DATE),
                            new SqlParameter("P_HARBOR_CALL_TYPE", Types.VARCHAR),
                            new SqlParameter("P_TOTAL_QTY", Types.NUMERIC),
                            new SqlParameter("P_DAYS", Types.NUMERIC),
                            new SqlParameter("P_PRINCIPAL_POID", Types.NUMERIC),
                            new SqlOutParameter("P_STATUS", Types.VARCHAR)
                    );

            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("P_LOGIN_GROUP_POID", groupPoid);
            inputMap.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));
            inputMap.put("P_LOGIN_COMPANY_POID", companyPoid);
            inputMap.put("P_PDA_POID", new BigDecimal(transactionPoid));
            inputMap.put("P_VESSEL_POID", vesselPoid);
            inputMap.put("P_VESSEL_TYPE_POID", vesselTypePoid);
            inputMap.put("P_GRT", grt);
            inputMap.put("P_NRT", nrt);
            inputMap.put("P_DWT", dwt);
            inputMap.put("P_PORT_POID", portPoid);
            inputMap.put("P_ARRIVAL_DATE", java.sql.Date.valueOf(arrivalDate));
            inputMap.put("P_SAIL_DATE", java.sql.Date.valueOf(sailDate));
            inputMap.put("P_HARBOR_CALL_TYPE", harbourCallType);
            inputMap.put("P_TOTAL_QTY", totalQuantity);
            inputMap.put("P_DAYS", numberOfDays);
            inputMap.put("P_PRINCIPAL_POID", principalPoid);

            Map<String, Object> result = jdbcCall.execute(inputMap);

            String status = (String) result.get("P_STATUS");

            logger.info("[SP-3] PROC_PDA_RE_CALCULATE - Completed. Status: {}", status);
            return status != null ? status : "Success";

        } catch (Exception e) {
            logger.error("[SP-3] PROC_PDA_RE_CALCULATE - Error: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }


    private String callLoadDefaultCharges(
            Long groupPoid, Long userPoid, Long companyPoid, Long transactionPoid,
            BigDecimal vesselPoid, BigDecimal vesselTypePoid, BigDecimal grt, BigDecimal nrt, BigDecimal dwt,
            BigDecimal portPoid, LocalDate arrivalDate, LocalDate sailDate,
            String harbourCallType, BigDecimal totalQuantity, BigDecimal numberOfDays, BigDecimal principalPoid
    ) {
        try {
            logger.info("[SP-2] PROC_PDA_LOAD_DEF_CHARGE - transactionPoid: {}, vesselPoid: {}", transactionPoid, vesselPoid);
            logger.info("[SP-2] Parameters - vesselTypePoid: {}, grt: {}, nrt: {}, dwt: {}", vesselTypePoid, grt, nrt, dwt);
            logger.info("[SP-2] Parameters - portPoid: {}, arrivalDate: {}, sailDate: {}", portPoid, arrivalDate, sailDate);
            logger.info("[SP-2] Parameters - harbourCallType: {}, totalQuantity: {}, numberOfDays: {}, principalPoid: {}", 
                    harbourCallType, totalQuantity, numberOfDays, principalPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_LOAD_DEF_CHARGE")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_PDA_POID", Types.NUMERIC),
                            new SqlParameter("P_VESSEL_POID", Types.NUMERIC),
                            new SqlParameter("P_VESSEL_TYPE_POID", Types.NUMERIC),
                            new SqlParameter("P_GRT", Types.NUMERIC),
                            new SqlParameter("P_NRT", Types.NUMERIC),
                            new SqlParameter("P_DWT", Types.NUMERIC),
                            new SqlParameter("P_PORT_POID", Types.NUMERIC),
                            new SqlParameter("P_ARRIVAL_DATE", Types.DATE),
                            new SqlParameter("P_SAIL_DATE", Types.DATE),
                            new SqlParameter("P_HARBOR_CALL_TYPE", Types.VARCHAR),
                            new SqlParameter("P_TOTAL_QTY", Types.NUMERIC),
                            new SqlParameter("P_DAYS", Types.NUMERIC),
                            new SqlParameter("P_PRINCIPAL_POID", Types.NUMERIC),
                            new SqlOutParameter("P_STATUS", Types.VARCHAR)
                    );

            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("P_LOGIN_GROUP_POID", groupPoid);
            inputMap.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));
            inputMap.put("P_LOGIN_COMPANY_POID", companyPoid);
            inputMap.put("P_PDA_POID", new BigDecimal(transactionPoid));
            inputMap.put("P_VESSEL_POID", vesselPoid);
            inputMap.put("P_VESSEL_TYPE_POID", vesselTypePoid);
            inputMap.put("P_GRT", grt);
            inputMap.put("P_NRT", nrt);
            inputMap.put("P_DWT", dwt);
            inputMap.put("P_PORT_POID", portPoid);
            inputMap.put("P_ARRIVAL_DATE", java.sql.Date.valueOf(arrivalDate));
            inputMap.put("P_SAIL_DATE", java.sql.Date.valueOf(sailDate));
            inputMap.put("P_HARBOR_CALL_TYPE", harbourCallType);
            inputMap.put("P_TOTAL_QTY", totalQuantity);
            inputMap.put("P_DAYS", numberOfDays);
            inputMap.put("P_PRINCIPAL_POID", principalPoid);

            Map<String, Object> result = jdbcCall.execute(inputMap);

            String status = (String) result.get("P_STATUS");

            logger.info("[SP-2] PROC_PDA_LOAD_DEF_CHARGE - Completed. Status: {}", status);
            
            // Check for warnings or errors from stored procedure
            if (status != null && (status.startsWith("WARNING") || status.startsWith("ERROR"))) {
                throw new ValidationException(
                        status,
                        List.of(new ValidationError("general", status))
                );
            }
            
            // Count inserted charges for logging
            Integer chargeCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM PDA_ENTRY_DTL WHERE TRANSACTION_POID = ? AND DATA_SOURCE = 'TARIFF'",
                    Integer.class, transactionPoid);
            logger.info("[SP-2] PROC_PDA_LOAD_DEF_CHARGE - Inserted {} charges", chargeCount);
            
            return status != null ? status : "Success";

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[SP-2] PROC_PDA_LOAD_DEF_CHARGE - Error: {}", e.getMessage(), e);
            throw new ValidationException(
                    "Failed to load default charges",
                    List.of(new ValidationError("general", "Error: " + e.getMessage()))
            );
        }
    }

    // Vehicle Details Helper Methods

    private void createVehicleDetail(Long transactionPoid, PdaEntryVehicleDetailRequest request,
                                     String userId, LocalDateTime now) {
        // Create new entity
        PdaEntryVehicleDtl detail = new PdaEntryVehicleDtl();
        detail.setTransactionPoid(transactionPoid);
        long detRowId = System.nanoTime() % 1000000;
        detail.setDetRowId(detRowId > 0 ? detRowId : Math.abs(detRowId) + 1);

        // Map request to entity
        mapVehicleDetailRequestToEntity(request, detail);

        // Set audit fields

        // Save
        PdaEntryVehicleDtl saved = vehicleDtlRepository.save(detail);
        String logDetail = String.format("Row Created on [PDA Entry Vehicle Details] with detRowId: %s", saved.getDetRowId());
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
    }

    private void updateVehicleDetail(Long transactionPoid, PdaEntryVehicleDetailRequest request,
                                     String userId, LocalDateTime now) {
        // Validate detail exists
        PdaEntryVehicleDtlId detailId = new PdaEntryVehicleDtlId(transactionPoid, request.getDetRowId());
        PdaEntryVehicleDtl detail = vehicleDtlRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle detail not found with id: " + request.getDetRowId()
                ));

        // Map request to entity
        mapVehicleDetailRequestToEntity(request, detail);

        // Update audit fields
        // Audit is handled by BaseEntity

        // Save
        vehicleDtlRepository.save(detail);
    }

    private void deleteVehicleDetailRecord(Long transactionPoid, Long detRowId) {
        PdaEntryVehicleDtlId detailId = new PdaEntryVehicleDtlId(transactionPoid, detRowId);
        PdaEntryVehicleDtl detail = vehicleDtlRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle detail not found with id: " + detRowId
                ));
        loggingService.logDelete(detail, UserContext.getDocumentId(), transactionPoid.toString());
        vehicleDtlRepository.delete(detail);
    }

    private void mapVehicleDetailRequestToEntity(PdaEntryVehicleDetailRequest request, PdaEntryVehicleDtl entity) {
        entity.setVesselName(request.getVesselName());
        entity.setVoyageRef(request.getVoyageRef());
        entity.setInOutMode(request.getInOutMode());
        entity.setVehicleModel(request.getVehicleModel());
        entity.setVinNumber(request.getVinNumber());
        entity.setScanDate(request.getScanDate());
        entity.setDamage(request.getDamage());
        entity.setStatus(request.getStatus());
        entity.setPublishForImport(request.getPublishForImport());
        entity.setRemarks(request.getRemarks());
    }

    private PdaEntryVehicleDetailResponse toVehicleDetailResponse(PdaEntryVehicleDtl entity) {
        PdaEntryVehicleDetailResponse response = new PdaEntryVehicleDetailResponse();
        response.setTransactionPoid(entity.getTransactionPoid());
        response.setDetRowId(entity.getDetRowId());
        response.setVesselName(entity.getVesselName());
        response.setVoyageRef(entity.getVoyageRef());
        response.setInOutMode(entity.getInOutMode());
        response.setVehicleModel(entity.getVehicleModel());
        response.setVinNumber(entity.getVinNumber());
        response.setScanDate(entity.getScanDate());
        response.setDamage(entity.getDamage());
        response.setStatus(entity.getStatus());
        response.setPublishForImport(entity.getPublishForImport());
        response.setRemarks(entity.getRemarks());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());
        return response;
    }

    private void callImportVehicleDetails(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {
        try {
            logger.info("[SP-14] PROC_PDA_IMPORT_VEHICLE_DTL - START - transactionPoid: {}", transactionPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_IMPORT_VEHICLE_DTL")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_PDA_POID", Types.NUMERIC),
                            new SqlOutParameter("P_RESULT", Types.VARCHAR)
                    );

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("P_LOGIN_GROUP_POID", groupPoid);
            inParams.put("P_LOGIN_COMPANY_POID", companyPoid);
            inParams.put("P_LOGIN_USER_POID", userPoid);
            inParams.put("P_PDA_POID", transactionPoid);

            Map<String, Object> result = jdbcCall.execute(inParams);
            String spResult = (String) result.get("P_RESULT");

            logger.info("[SP-14] PROC_PDA_IMPORT_VEHICLE_DTL - END - Result: {}", spResult);
        } catch (Exception e) {
            logger.error("[SP-14] PROC_PDA_IMPORT_VEHICLE_DTL - ERROR: {}", e.getMessage(), e);
        }
    }

    private void callClearVehicleDetails(Long groupPoid, Long userPoid, Long companyPoid, Long transactionPoid) {
        try {
            logger.info("[SP-15] PROC_PDA_VEHICLE_DTL_CLEAR - START - transactionPoid: {}", transactionPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_VEHICLE_DTL_CLEAR")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_PDA_POID", Types.NUMERIC),
                            new SqlOutParameter("P_STATUS", Types.VARCHAR)
                    );

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("P_LOGIN_GROUP_POID", groupPoid);
            inParams.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));
            inParams.put("P_LOGIN_COMPANY_POID", companyPoid);
            inParams.put("P_PDA_POID", transactionPoid);

            Map<String, Object> result = jdbcCall.execute(inParams);
            String spStatus = (String) result.get("P_STATUS");

            logger.info("[SP-15] PROC_PDA_VEHICLE_DTL_CLEAR - END - Status: {}", spStatus);

        } catch (Exception e) {
            logger.error("[SP-15] PROC_PDA_VEHICLE_DTL_CLEAR - ERROR: {}, falling back to direct delete", e.getMessage(), e);

            // Fallback delete logic
            vehicleDtlRepository.deleteByTransactionPoid(transactionPoid);
            logger.info("[SP-15] Fallback delete executed successfully for transactionPoid: {}", transactionPoid);
        }
    }


    private void callPublishForImport(Long groupPoid, Long userPoid, Long companyPoid, Long transactionPoid) {
        try {
            logger.info("[SP-16] PROC_PDA_PUBLISH_FOR_IMPORT - START - transactionPoid: {}", transactionPoid);
            logger.debug("[SP-16] Input params - groupPoid: {}, userId: {}, companyPoid: {}, transactionPoid: {}", groupPoid, userPoid, companyPoid, transactionPoid);

            if (groupPoid == null || userPoid == null || companyPoid == null || transactionPoid == null) {
                logger.error("[SP-16] NULL parameter detected - groupPoid: {}, userId: {}, companyPoid: {}, transactionPoid: {}", groupPoid, userPoid, companyPoid, transactionPoid);
                return;
            }

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_PUBLISH_FOR_IMPORT")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_PDA_POID", Types.NUMERIC),
                            new SqlOutParameter("P_STATUS", Types.VARCHAR)
                    );

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("P_LOGIN_GROUP_POID", groupPoid);
            inParams.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));
            inParams.put("P_LOGIN_COMPANY_POID", companyPoid);
            inParams.put("P_PDA_POID", transactionPoid);

            logger.debug("[SP-16] Executing with params: {}", inParams);

            Map<String, Object> result = jdbcCall.execute(inParams);
            String spStatus = (String) result.get("P_STATUS");

            logger.info("[SP-16] PROC_PDA_PUBLISH_FOR_IMPORT - END - Status: {}", spStatus);
        } catch (Exception e) {
            logger.error("[SP-16] PROC_PDA_PUBLISH_FOR_IMPORT - ERROR: {}", e.getMessage(), e);
        }
    }

    // TDR Details Helper Methods

    private void createTdrDetail(Long transactionPoid, PdaEntryTdrDetailRequest request,
                                 String userId, LocalDateTime now) {
        // Create new entity
        PdaEntryTdrDetail detail = new PdaEntryTdrDetail();
        detail.setTransactionPoid(transactionPoid);
        long detRowId = System.nanoTime() % 1000000;
        detail.setDetRowId(detRowId > 0 ? detRowId : Math.abs(detRowId) + 1);

        // Map request to entity
        mapTdrDetailRequestToEntity(request, detail);

        // Set audit fields
        // Audit is handled by BaseEntity

        // Save
        PdaEntryTdrDetail saved = tdrDetailRepository.save(detail);
        String logDetail = String.format("Row Created on [PDA Entry TDR Details] with detRowId: %s", saved.getDetRowId());
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
    }

    private void updateTdrDetail(Long transactionPoid, PdaEntryTdrDetailRequest request,
                                 String userId, LocalDateTime now) {
        // Validate detail exists
        PdaEntryTdrDetailId detailId = new PdaEntryTdrDetailId(transactionPoid, request.getDetRowId());
        PdaEntryTdrDetail detail = tdrDetailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TDR detail not found with id: " + request.getDetRowId()
                ));

        // Create old detail copy for logging
        PdaEntryTdrDetail oldDetail = new PdaEntryTdrDetail();
        oldDetail.setTransactionPoid(detail.getTransactionPoid());
        oldDetail.setDetRowId(detail.getDetRowId());
        BeanUtils.copyProperties(detail, oldDetail);

        // Map request to entity
        mapTdrDetailRequestToEntity(request, detail);

        // Update audit fields
        // Audit is handled by BaseEntity

        // Save and log
        tdrDetailRepository.save(detail);
        
        String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", detail.getTransactionPoid(), detail.getDetRowId());
        loggingService.createLog(oldDetail, detail, PdaEntryTdrDetail.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
    }

    private void deleteTdrDetailRecord(Long transactionPoid, Long detRowId) {
        PdaEntryTdrDetailId detailId = new PdaEntryTdrDetailId(transactionPoid, detRowId);
        PdaEntryTdrDetail detail = tdrDetailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TDR detail not found with id: " + detRowId
                ));
        loggingService.logDelete(detail, UserContext.getDocumentId(), transactionPoid.toString());
        tdrDetailRepository.delete(detail);
    }

    private void mapTdrDetailRequestToEntity(PdaEntryTdrDetailRequest request, PdaEntryTdrDetail entity) {
        entity.setMlo(request.getMlo());
        entity.setPol(request.getPol());
        entity.setSlot(request.getSlot());
        entity.setSubSlot(request.getSubSlot());
        entity.setDisch20fl(request.getDisch20fl());
        entity.setDisch20mt(request.getDisch20mt());
        entity.setDisch40fl(request.getDisch40fl());
        entity.setDisch40mt(request.getDisch40mt());
        entity.setDisch45fl(request.getDisch45fl());
        entity.setDisch45mt(request.getDisch45mt());
        entity.setDischTot20(request.getDischTot20());
        entity.setDischTot40(request.getDischTot40());
        entity.setDischTot45(request.getDischTot45());
        entity.setLoad20fl(request.getLoad20fl());
        entity.setLoad20mt(request.getLoad20mt());
        entity.setLoad40fl(request.getLoad40fl());
        entity.setLoad40mt(request.getLoad40mt());
        entity.setLoad45fl(request.getLoad45fl());
        entity.setLoad45mt(request.getLoad45mt());
        entity.setLoadTot20(request.getLoadTot20());
        entity.setLoadTot40(request.getLoadTot40());
        entity.setLoadTot45(request.getLoadTot45());
        entity.setLoadAlm20(request.getLoadAlm20());
        entity.setLoadAlm40(request.getLoadAlm40());
        entity.setLoadAlm45(request.getLoadAlm45());
        entity.setFull20dc(request.getFull20dc());
        entity.setFull20tk(request.getFull20tk());
        entity.setFull20fr(request.getFull20fr());
        entity.setFull20ot(request.getFull20ot());
        entity.setFull40dc(request.getFull40dc());
        entity.setFull40ot(request.getFull40ot());
        entity.setFull40fr(request.getFull40fr());
        entity.setFull40rf(request.getFull40rf());
        entity.setFull40rh(request.getFull40rh());
        entity.setFull40hc(request.getFull40hc());
        entity.setFull45(request.getFull45());
        entity.setDg20dc(request.getDg20dc());
        entity.setDg20tk(request.getDg20tk());
        entity.setDg40dc(request.getDg40dc());
        entity.setDg40hc(request.getDg40hc());
        entity.setDg20rf(request.getDg20rf());
        entity.setDg40rf(request.getDg40rf());
        entity.setDg40hr(request.getDg40hr());
        entity.setOog20ot(request.getOog20ot());
        entity.setOog20fr(request.getOog20fr());
        entity.setOog40ot(request.getOog40ot());
        entity.setOog40fr(request.getOog40fr());
        entity.setMt20dc(request.getMt20dc());
        entity.setMt20tk(request.getMt20tk());
        entity.setMt20fr(request.getMt20fr());
        entity.setMt20ot(request.getMt20ot());
        entity.setMt40dc(request.getMt40dc());
        entity.setMt40ot(request.getMt40ot());
        entity.setMt40fr(request.getMt40fr());
        entity.setMt40rf(request.getMt40rf());
        entity.setMt40rh(request.getMt40rh());
        entity.setMt40hc(request.getMt40hc());
        entity.setMt45(request.getMt45());
        entity.setRemarks(request.getRemarks());
    }

    private PdaEntryTdrDetailResponse toTdrDetailResponse(PdaEntryTdrDetail entity) {
        PdaEntryTdrDetailResponse response = new PdaEntryTdrDetailResponse();
        response.setTransactionPoid(entity.getTransactionPoid());
        response.setDetRowId(entity.getDetRowId());
        response.setMlo(entity.getMlo());
        response.setPol(entity.getPol());
        response.setSlot(entity.getSlot());
        response.setSubSlot(entity.getSubSlot());
        response.setDisch20fl(entity.getDisch20fl());
        response.setDisch20mt(entity.getDisch20mt());
        response.setDisch40fl(entity.getDisch40fl());
        response.setDisch40mt(entity.getDisch40mt());
        response.setDisch45fl(entity.getDisch45fl());
        response.setDisch45mt(entity.getDisch45mt());
        response.setDischTot20(entity.getDischTot20());
        response.setDischTot40(entity.getDischTot40());
        response.setDischTot45(entity.getDischTot45());
        response.setLoad20fl(entity.getLoad20fl());
        response.setLoad20mt(entity.getLoad20mt());
        response.setLoad40fl(entity.getLoad40fl());
        response.setLoad40mt(entity.getLoad40mt());
        response.setLoad45fl(entity.getLoad45fl());
        response.setLoad45mt(entity.getLoad45mt());
        response.setLoadTot20(entity.getLoadTot20());
        response.setLoadTot40(entity.getLoadTot40());
        response.setLoadTot45(entity.getLoadTot45());
        response.setLoadAlm20(entity.getLoadAlm20());
        response.setLoadAlm40(entity.getLoadAlm40());
        response.setLoadAlm45(entity.getLoadAlm45());
        response.setFull20dc(entity.getFull20dc());
        response.setFull20tk(entity.getFull20tk());
        response.setFull20fr(entity.getFull20fr());
        response.setFull20ot(entity.getFull20ot());
        response.setFull40dc(entity.getFull40dc());
        response.setFull40ot(entity.getFull40ot());
        response.setFull40fr(entity.getFull40fr());
        response.setFull40rf(entity.getFull40rf());
        response.setFull40rh(entity.getFull40rh());
        response.setFull40hc(entity.getFull40hc());
        response.setFull45(entity.getFull45());
        response.setDg20dc(entity.getDg20dc());
        response.setDg20tk(entity.getDg20tk());
        response.setDg40dc(entity.getDg40dc());
        response.setDg40hc(entity.getDg40hc());
        response.setDg20rf(entity.getDg20rf());
        response.setDg40rf(entity.getDg40rf());
        response.setDg40hr(entity.getDg40hr());
        response.setOog20ot(entity.getOog20ot());
        response.setOog20fr(entity.getOog20fr());
        response.setOog40ot(entity.getOog40ot());
        response.setOog40fr(entity.getOog40fr());
        response.setMt20dc(entity.getMt20dc());
        response.setMt20tk(entity.getMt20tk());
        response.setMt20fr(entity.getMt20fr());
        response.setMt20ot(entity.getMt20ot());
        response.setMt40dc(entity.getMt40dc());
        response.setMt40ot(entity.getMt40ot());
        response.setMt40fr(entity.getMt40fr());
        response.setMt40rf(entity.getMt40rf());
        response.setMt40rh(entity.getMt40rh());
        response.setMt40hc(entity.getMt40hc());
        response.setMt45(entity.getMt45());
        response.setRemarks(entity.getRemarks());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());
        return response;
    }

    // Acknowledgment Details Helper Methods

    private void createAcknowledgmentDetail(Long transactionPoid, PdaEntryAcknowledgmentDetailRequest request,
                                            String userId, LocalDateTime now) {
        // Get next available detRowId
        Long maxDetRowId = acknowledgmentDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid);
        Long nextDetRowId = (maxDetRowId != null) ? maxDetRowId + 1 : 1;

        // Create new entity
        PdaEntryAcknowledgmentDtl detail = new PdaEntryAcknowledgmentDtl();
        detail.setTransactionPoid(transactionPoid);
        detail.setDetRowId(nextDetRowId);

        // Map request to entity
        mapAcknowledgmentDetailRequestToEntity(request, detail);

        // Set audit fields
        // Audit is handled by BaseEntity

        // Save
        PdaEntryAcknowledgmentDtl saved = acknowledgmentDtlRepository.save(detail);
        String logDetail = String.format("Row Created on [PDA Entry Acknowledgment Details] with detRowId: %s", saved.getDetRowId());
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
    }

    private void updateAcknowledgmentDetail(Long transactionPoid, PdaEntryAcknowledgmentDetailRequest request,
                                            String userId, LocalDateTime now) {
        // Validate detail exists
        PdaEntryAcknowledgmentDtlId detailId = new PdaEntryAcknowledgmentDtlId(transactionPoid, request.getDetRowId());
        PdaEntryAcknowledgmentDtl detail = acknowledgmentDtlRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Acknowledgment detail not found with id: " + request.getDetRowId()
                ));

        // Map request to entity
        mapAcknowledgmentDetailRequestToEntity(request, detail);

        // Update audit fields
        // Audit is handled by BaseEntity

        // Save
        acknowledgmentDtlRepository.save(detail);
    }

    private void deleteAcknowledgmentDetailRecord(Long transactionPoid, Long detRowId) {
        PdaEntryAcknowledgmentDtlId detailId = new PdaEntryAcknowledgmentDtlId(transactionPoid, detRowId);
        PdaEntryAcknowledgmentDtl detail = acknowledgmentDtlRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Acknowledgment detail not found with id: " + detRowId
                ));
        loggingService.logDelete(detail, UserContext.getDocumentId(), transactionPoid.toString());
        acknowledgmentDtlRepository.delete(detail);
    }

    private void mapAcknowledgmentDetailRequestToEntity(PdaEntryAcknowledgmentDetailRequest request, PdaEntryAcknowledgmentDtl entity) {
        entity.setParticulars(request.getParticulars());
        entity.setSelected(request.getSelected());
        entity.setRemarks(request.getRemarks());
    }

    private PdaEntryAcknowledgmentDetailResponse toAcknowledgmentDetailResponse(PdaEntryAcknowledgmentDtl entity) {
        PdaEntryAcknowledgmentDetailResponse response = new PdaEntryAcknowledgmentDetailResponse();
        response.setTransactionPoid(entity.getTransactionPoid());
        response.setDetRowId(entity.getDetRowId());
        response.setParticulars(entity.getParticulars());
        response.setSelected(entity.getSelected());
        response.setRemarks(entity.getRemarks());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());
        return response;
    }

    private List<PdaEntryAcknowledgmentDetailResponse> toAcknowledgmentDetailResponseList(List<PdaEntryAcknowledgmentDtl> details) {
        return details.stream()
                .map(this::toAcknowledgmentDetailResponse)
                .collect(Collectors.toList());
    }

    // Additional SP Methods with Logging

    public String callImportTdrDetail(Long groupPoid, Long userPoid, Long companyPoid, Long transactionPoid) {
        try {
            logger.info("[SP-3] PROC_PDA_IMPORT_TDR_DETAIL2 - transactionPoid: {}", transactionPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_IMPORT_TDR_DETAIL2")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_TRANSACTION_POID", Types.NUMERIC),
                            new SqlOutParameter("P_STATUS", Types.VARCHAR)
                    );

            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("P_LOGIN_GROUP_POID", groupPoid);
            inputMap.put("P_LOGIN_USER_POID", userPoid);
            inputMap.put("P_LOGIN_COMPANY_POID", companyPoid);
            inputMap.put("P_TRANSACTION_POID", transactionPoid);

            Map<String, Object> result = jdbcCall.execute(inputMap);

            String status = (String) result.get("P_STATUS");

            logger.info("[SP-3] PROC_PDA_IMPORT_TDR_DETAIL2 - Completed. Status: {}", status);
            return status != null ? status : "Success";

        } catch (Exception e) {
            logger.error("[SP-3] PROC_PDA_IMPORT_TDR_DETAIL2 - Error: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    private String callImportTdrFileFromExcel(Long groupPoid, Long companyPoid, Long userPoid, org.springframework.web.multipart.MultipartFile file) {
        try {
            logger.info("[SP] PROC_PDA_IMPORT_TDR_FILE - Processing Excel file: {}", file.getOriginalFilename());

            // Process Excel file and call stored procedure
            // This is a simplified implementation - you may need to process the Excel file first
            // and then call the appropriate stored procedure with the data
            
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_IMPORT_TDR_DETAIL2")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_TRANSACTION_POID", Types.NUMERIC),
                            new SqlOutParameter("P_STATUS", Types.VARCHAR)
                    );

            // For file import, we might not have a specific transaction POID
            // This depends on your business logic - you may need to process the Excel file
            // to extract transaction information or handle it differently
            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("P_LOGIN_GROUP_POID", groupPoid);
            inputMap.put("P_LOGIN_USER_POID", userPoid);
            inputMap.put("P_LOGIN_COMPANY_POID", companyPoid);
            inputMap.put("P_TRANSACTION_POID", null); // May need to be extracted from file

            Map<String, Object> result = jdbcCall.execute(inputMap);
            String status = (String) result.get("P_STATUS");

            logger.info("[SP] PROC_PDA_IMPORT_TDR_FILE - Completed. Status: {}", status);
            return status != null ? status : "TDR file imported successfully";

        } catch (Exception e) {
            logger.error("[SP] PROC_PDA_IMPORT_TDR_FILE - Error: {}", e.getMessage(), e);
            return "Error importing TDR file: " + e.getMessage();
        }
    }

    public String callDefaultChargesFromTdr(Long groupPoid, Long userPoid, Long companyPoid, Long transactionPoid, LocalDate arrivalDate) {
        try {
            logger.info("[SP-4] PROC_PDA_DEFAULT_CH_FROM_TDR - transactionPoid: {}", transactionPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_DEFAULT_CH_FROM_TDR")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_TRANSACTION_POID", Types.NUMERIC),
                            new SqlParameter("P_ARRAIVAL_DATE", Types.DATE),
                            new SqlOutParameter("P_STATUS", Types.VARCHAR)
                    );

            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("P_LOGIN_GROUP_POID", groupPoid);
            inputMap.put("P_LOGIN_USER_POID", userPoid);
            inputMap.put("P_LOGIN_COMPANY_POID", companyPoid);
            inputMap.put("P_TRANSACTION_POID", transactionPoid);
            inputMap.put("P_ARRAIVAL_DATE", java.sql.Date.valueOf(arrivalDate));

            Map<String, Object> result = jdbcCall.execute(inputMap);

            String status = (String) result.get("P_STATUS");

            logger.info("[SP-4] PROC_PDA_DEFAULT_CH_FROM_TDR - Completed. Status: {}", status);
            return status != null ? status : "Success";

        } catch (Exception e) {
            logger.error("[SP-4] PROC_PDA_DEFAULT_CH_FROM_TDR - Error: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    public String callClearTdrDetails(Long groupPoid, Long userPoid, Long companyPoid, Long transactionPoid) {
        try {
            logger.info("[SP-5] PROC_PDA_TDR_DETAIL_CLEAR - transactionPoid: {}", transactionPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_TDR_DETAIL_CLEAR")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_PDA_POID", Types.NUMERIC),
                            new SqlOutParameter("P_STATUS", Types.VARCHAR)
                    );

            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("P_LOGIN_GROUP_POID", groupPoid);
            inputMap.put("P_LOGIN_USER_POID", userPoid);
            inputMap.put("P_LOGIN_COMPANY_POID", companyPoid);
            inputMap.put("P_PDA_POID", transactionPoid);

            Map<String, Object> result = jdbcCall.execute(inputMap);

            String status = (String) result.get("P_STATUS");

            logger.info("[SP-5] PROC_PDA_TDR_DETAIL_CLEAR - Completed. Status: {}", status);
            return status != null ? status : "Success";

        } catch (Exception e) {
            logger.error("[SP-5] PROC_PDA_TDR_DETAIL_CLEAR - Error: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    public String callCancelPdaEntry(Long groupPoid, Long companyPoid, Long userPoid, Long pdaPoid, String cancelRemark) {
        try {
            logger.info("[SP-6] PROC_PDA_ENTRY_CANCEL - START - pdaPoid: {}", pdaPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_ENTRY_CANCEL")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_PDA_POID", Types.NUMERIC),
                            new SqlParameter("P_CANCEL_REMARK", Types.VARCHAR),
                            new SqlOutParameter("P_RESULT", Types.VARCHAR)
                    );

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("P_LOGIN_GROUP_POID", groupPoid);
            inParams.put("P_LOGIN_COMPANY_POID", companyPoid);
            inParams.put("P_LOGIN_USER_POID", userPoid);
            inParams.put("P_PDA_POID", pdaPoid);
            inParams.put("P_CANCEL_REMARK", cancelRemark);

            Map<String, Object> result = jdbcCall.execute(inParams);
            String spResult = (String) result.get("P_RESULT");

            logger.info("[SP-6] PROC_PDA_ENTRY_CANCEL - END - Result: {}", spResult);
            return spResult != null ? spResult : "Success";
        } catch (Exception e) {
            logger.error("[SP-6] PROC_PDA_ENTRY_CANCEL - ERROR: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    public void callGetPdaRefWhereClause(BigDecimal loginPoid) {
        try {
            logger.info("[SP-7] PROC_GL_PDA_REF_WHERE_CLAUSE - START - loginPoid: {}", loginPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_GL_PDA_REF_WHERE_CLAUSE")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_POID", Types.NUMERIC),
                            new SqlOutParameter("P_STATUS", Types.VARCHAR)
                    );

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("P_LOGIN_POID", loginPoid);

            Map<String, Object> result = jdbcCall.execute(inParams);
            String status = (String) result.get("P_STATUS");

            logger.info("[SP-7] PROC_GL_PDA_REF_WHERE_CLAUSE - END - Status: {}", status);
        } catch (Exception e) {
            logger.error("[SP-7] PROC_GL_PDA_REF_WHERE_CLAUSE - ERROR: {}", e.getMessage(), e);
        }
    }

    public String updateFda(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        try {
            logger.info("[SP-FDA] PROC_PDA_DTL_UPDATE_FDA - START - transactionPoid: {}", transactionPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_DTL_UPDATE_FDA")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_PDA_POID", Types.VARCHAR),
                            new SqlOutParameter("P_RESULT", Types.VARCHAR)
                    );

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("P_LOGIN_GROUP_POID", groupPoid);
            inParams.put("P_LOGIN_COMPANY_POID", companyPoid);
            inParams.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));
            inParams.put("P_PDA_POID", transactionPoid.toString());

            Map<String, Object> result = jdbcCall.execute(inParams);
            String spResult = (String) result.get("P_RESULT");

            logger.info("[SP-FDA] PROC_PDA_DTL_UPDATE_FDA - END - Result: {}", spResult);
            return spResult;

        } catch (Exception e) {
            logger.error("[SP-FDA] PROC_PDA_DTL_UPDATE_FDA - ERROR: {}", e.getMessage(), e);
            throw new ValidationException(
                    "FDA creation failed",
                    List.of(new ValidationError("general", "Error updating FDA: " + e.getMessage()))
            );
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public String callUpdateFdaFromPda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {
        try {
            logger.info("[SP-8] PROC_PDA_DTL_UPDATE_FDA - transactionPoid: {}", transactionPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_DTL_UPDATE_FDA")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_PDA_POID", Types.VARCHAR),
                            new SqlOutParameter("P_RESULT", Types.VARCHAR)
                    );

            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("P_LOGIN_GROUP_POID", groupPoid);
            inputMap.put("P_LOGIN_COMPANY_POID", companyPoid);
            inputMap.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));
            inputMap.put("P_PDA_POID", transactionPoid.toString());

            Map<String, Object> result = jdbcCall.execute(inputMap);

            String status = (String) result.get("P_RESULT");

            logger.info("[SP-8] PROC_PDA_DTL_UPDATE_FDA - Completed. Status: {}", status);
            return status != null ? status : "Success";

        } catch (Exception e) {
            logger.error("[SP-8] PROC_PDA_DTL_UPDATE_FDA - Error: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();

        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public Map<String, Object> callSubmitPdaToFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid, LocalDate vesselSailDate) {
        try {
            logger.info("[SP-9] PROC_PDA_TO_FDA_DOC_SUBMISSION - transactionPoid: {}", transactionPoid);

            // Get PDA entry to retrieve vessel dates
            PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid)
                    .orElseThrow(() -> new ResourceNotFoundException("PDA Entry not found with id: " + transactionPoid));

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_TO_FDA_DOC_SUBMISSION")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_PDA_POID", Types.NUMERIC),
                            new SqlParameter("P_VESSEL_ARRIVAL_DATE", Types.DATE),
                            new SqlParameter("P_VESSEL_SAIL_DATE", Types.DATE),
                            new SqlOutParameter("P_RESULT", Types.VARCHAR),
                            new SqlOutParameter("OUTDATA", OracleTypes.CURSOR)
                    );

            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("P_LOGIN_GROUP_POID", groupPoid);
            inputMap.put("P_LOGIN_COMPANY_POID", companyPoid);
            inputMap.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));
            inputMap.put("P_PDA_POID", new BigDecimal(transactionPoid));
            inputMap.put("P_VESSEL_ARRIVAL_DATE", entry.getArrivalDate() != null ? java.sql.Date.valueOf(entry.getArrivalDate()) : null);
            inputMap.put("P_VESSEL_SAIL_DATE", vesselSailDate != null ? java.sql.Date.valueOf(vesselSailDate) : null);

            Map<String, Object> result = jdbcCall.execute(inputMap);

            String status = (String) result.get("P_RESULT");
            List<Map<String, Object>> outData = (List<Map<String, Object>>) result.get("OUTDATA");

            logger.info("[SP-9] PROC_PDA_TO_FDA_DOC_SUBMISSION - Completed. Status: {}", status);

            // Check for warnings or errors from stored procedure
            if (status != null && (status.startsWith("WARNING") || status.startsWith("ERROR"))) {
                throw new ValidationException(
                        status,
                        List.of(new ValidationError("general", status))
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", status != null ? status : "Success");
            response.put("vesselArrivalDate", entry.getArrivalDate());
            response.put("vesselSailDate", vesselSailDate);
            
            if (outData != null && !outData.isEmpty()) {
                Map<String, Object> cursorData = outData.get(0);
                response.put("documentSubmittedDate", cursorData.get("DOCUMENT_SUBMITTED_DATE"));
                response.put("documentSubmittedBy", cursorData.get("DOCUMENT_SUBMITTED_BY"));
                response.put("documentSubmittedStatus", cursorData.get("DOCUMENT_SUBMITTED_STATUS"));
            }
            
            return response;

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[SP-9] PROC_PDA_TO_FDA_DOC_SUBMISSION - Error: {}", e.getMessage(), e);
            throw new ValidationException(
                    "PDA to FDA document submission failed",
                    List.of(new ValidationError("general", "Error: " + e.getMessage()))
            );
        }
    }

    public Map<String, Object> callRejectFdaDocs(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid, String correctionRemarks) {
        try {
            logger.info("[SP-11] PROC_PDA_REJECT_THE_FDA_DOCS - transactionPoid: {}", transactionPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_REJECT_THE_FDA_DOCS")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_PDA_POID", Types.NUMERIC),
                            new SqlParameter("P_CORRECTION_REMARKS", Types.VARCHAR),
                            new SqlOutParameter("P_RESULT", Types.VARCHAR),
                            new SqlOutParameter("OUTDATA", OracleTypes.CURSOR)
                    );

            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("P_LOGIN_GROUP_POID", groupPoid);
            inputMap.put("P_LOGIN_COMPANY_POID", companyPoid);
            inputMap.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));
            inputMap.put("P_PDA_POID", new BigDecimal(transactionPoid));
            inputMap.put("P_CORRECTION_REMARKS", correctionRemarks);

            Map<String, Object> result = jdbcCall.execute(inputMap);

            String status = (String) result.get("P_RESULT");
            List<Map<String, Object>> outData = (List<Map<String, Object>>) result.get("OUTDATA");

            logger.info("[SP-11] PROC_PDA_REJECT_THE_FDA_DOCS - Completed. Status: {}", status);

            // Check for warnings or errors
            if (status != null && (status.startsWith("WARNING") || status.startsWith("ERROR"))) {
                throw new ValidationException(
                        "FDA document rejection failed",
                        List.of(new ValidationError("general", status))
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", status != null ? status : "Success");
            response.put("correctionRemarks", correctionRemarks);
            
            if (outData != null && !outData.isEmpty()) {
                Map<String, Object> cursorData = outData.get(0);
                response.put("documentReceivedStatus", cursorData.get("DOCUMENT_RECEIVED_STATUS"));
            }
            
            return response;

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[SP-11] PROC_PDA_REJECT_THE_FDA_DOCS - Error: {}", e.getMessage(), e);
            throw new ValidationException(
                    "FDA document rejection failed",
                    List.of(new ValidationError("general", "Error rejecting FDA documents: " + e.getMessage()))
            );
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public String callUploadAcknowledgmentDetails(
            Long groupPoid,
            Long userPoid,
            Long companyPoid,
            Long transactionPoid
    ) {
        try {
            logger.info("[SP-12] PROC_PDA_ACKNOW_DTLS_UPLOAD - transactionPoid: {}", transactionPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_ACKNOW_DTLS_UPLOAD")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_TRANSACTION_POID", Types.NUMERIC),
                            new SqlOutParameter("P_STATUS", Types.VARCHAR)
                    );

            Map<String, Object> params = new HashMap<>();
            params.put("P_LOGIN_GROUP_POID", groupPoid);
            params.put("P_LOGIN_USER_POID", userPoid);
            params.put("P_LOGIN_COMPANY_POID", companyPoid);
            params.put("P_TRANSACTION_POID", transactionPoid);

            Map<String, Object> result = jdbcCall.execute(params);
            String status = (String) result.get("P_STATUS");

            logger.info("[SP-12] PROC_PDA_ACKNOW_DTLS_UPLOAD - Completed. Status: {}", status);

            // Check for warnings or errors from stored procedure
            if (status != null && (status.startsWith("WARNING") || status.startsWith("ERROR"))) {
                throw new ValidationException(
                        status,
                        List.of(new ValidationError("general", status))
                );
            }

            return status != null ? status : "Success";

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[SP-12] PROC_PDA_ACKNOW_DTLS_UPLOAD - Error: {}", e.getMessage(), e);
            throw new ValidationException(
                    "Failed to upload acknowledgment details",
                    List.of(new ValidationError("general", "Error: " + e.getMessage()))
            );
        }
    }


    public String callClearAcknowledgmentDetails(
            Long groupPoid,
            Long userPoid,
            Long companyPoid,
            Long transactionPoid
    ) {
        try {
            logger.info("[SP-13] PROC_PDA_ACKNOW_DTL_CLEAR - transactionPoid: {}", transactionPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_ACKNOW_DTL_CLEAR")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_PDA_POID", Types.NUMERIC),
                            new SqlOutParameter("P_STATUS", Types.VARCHAR)
                    );

            Map<String, Object> params = new HashMap<>();
            params.put("P_LOGIN_GROUP_POID", groupPoid);
            params.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));
            params.put("P_LOGIN_COMPANY_POID", companyPoid);
            params.put("P_PDA_POID", new BigDecimal(transactionPoid));

            Map<String, Object> result = jdbcCall.execute(params);

            String status = (String) result.get("P_STATUS");

            logger.info("[SP-13] PROC_PDA_ACKNOW_DTL_CLEAR - Completed. Status: {}", status);

            // Check for warnings or errors from stored procedure
            if (status != null && (status.startsWith("WARNING") || status.startsWith("ERROR"))) {
                throw new ValidationException(
                        status,
                        List.of(new ValidationError("general", status))
                );
            }

            return status != null ? status : "Success";

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[SP-13] PROC_PDA_ACKNOW_DTL_CLEAR - Error: {}", e.getMessage(), e);
            throw new ValidationException(
                    "Failed to clear acknowledgment details",
                    List.of(new ValidationError("general", "Error: " + e.getMessage()))
            );
        }
    }

    public String callVerifyFdaDocs(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {
        try {
            logger.info("[SP-VERIFY] PROC_PDA_VERIFY_THE_FDA_DOCS - transactionPoid: {}", transactionPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_VERIFY_THE_FDA_DOCS")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_PDA_POID", Types.NUMERIC),
                            new SqlOutParameter("P_RESULT", Types.VARCHAR)
                    );

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("P_LOGIN_GROUP_POID", groupPoid);
            inParams.put("P_LOGIN_COMPANY_POID", companyPoid);
            inParams.put("P_LOGIN_USER_POID", userPoid);
            inParams.put("P_PDA_POID", transactionPoid);

            Map<String, Object> result = jdbcCall.execute(inParams);
            String spResult = (String) result.get("P_RESULT");

            logger.info("[SP-VERIFY] PROC_PDA_VERIFY_THE_FDA_DOCS - Result: {}", spResult);
            return spResult != null ? spResult : "Success";
        } catch (Exception e) {
            logger.error("[SP-VERIFY] PROC_PDA_VERIFY_THE_FDA_DOCS - Error: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAllPdaWithFilters(
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

    // Inner class for tax information
    public static class TaxInfo {
        private BigDecimal taxPoid;
        private BigDecimal taxPercentage;

        public TaxInfo(BigDecimal taxPoid, BigDecimal taxPercentage) {
            this.taxPoid = taxPoid;
            this.taxPercentage = taxPercentage;
        }

        public BigDecimal getTaxPoid() {
            return taxPoid;
        }

        public BigDecimal getTaxPercentage() {
            return taxPercentage;
        }
    }

    // Inner class for Excel configuration
    private static class ExcelConfig {
        int startRowNumber;
        int startColNumber;
        int endColNumber;
        String tempTableName;
        String excelSheetName;
    }

    @Override
    public byte[] printPda(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, BigDecimal otherPrincipalPoid) throws Exception {
        logger.info("Generating PDF for PDA Entry: {}", transactionPoid);
        
        try {
            Map<String, Object> params = printService.buildBaseParams(transactionPoid, "110-160");
            
            if (otherPrincipalPoid != null) {
                params.put("P_PRINCIPAL_POID", otherPrincipalPoid.toString());
            }
            
            params.put("SUB_HEADER", printService.load("Templates/DocHeaderSubReport.jrxml"));
            params.put("SUB_FOOTER", printService.load("Templates/DocFooterSubReport.jrxml"));
            params.put("SUB_TERMS", printService.load("Templates/TermsConditionsSubReport.jrxml"));

            
            net.sf.jasperreports.engine.JasperReport mainReport = printService.load("PDA/PdaEntryReport.jrxml");
            return printService.fillReportToPdf(mainReport, params, dataSource);
            
        } catch (RuntimeException e) {
            logger.error("Error generating PDF for PDA Entry: {}", transactionPoid, e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    // New FDA Document Methods
    @Override
    public FdaDocumentViewResponse getFdaDocumentInfo(Long transactionPoid, Long groupPoid, Long companyPoid) {
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException("PDA Entry not found"));

        if (entry.getFdaPoid() == null) {
            throw new ValidationException("No FDA document found for this PDA entry",
                    List.of(new ValidationError("fdaPoid", "FDA document not created yet")));
        }

        return FdaDocumentViewResponse.builder()
                .fdaPoid(entry.getFdaPoid())
                .fdaRef(entry.getFdaRef())
                .fdaUrl("/fda-entry/" + entry.getFdaPoid())
                .status(entry.getDocumentReceivedStatus())
                .build();
    }

    @Override
    public Map<String, Object> acceptFdaDocuments(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        logger.info("[SP-VERIFY] PROC_PDA_VERIFY_THE_FDA_DOCS - transactionPoid: {}", transactionPoid);

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_VERIFY_THE_FDA_DOCS")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_PDA_POID", Types.NUMERIC),
                            new SqlOutParameter("P_RESULT", Types.VARCHAR),
                            new SqlOutParameter("OUTDATA", OracleTypes.CURSOR)
                    );

            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("P_LOGIN_GROUP_POID", groupPoid);
            inputMap.put("P_LOGIN_COMPANY_POID", companyPoid);
            inputMap.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));
            inputMap.put("P_PDA_POID", new BigDecimal(transactionPoid));

            Map<String, Object> result = jdbcCall.execute(inputMap);
            String status = (String) result.get("P_RESULT");
            List<Map<String, Object>> outData = (List<Map<String, Object>>) result.get("OUTDATA");

            logger.info("[SP-VERIFY] PROC_PDA_VERIFY_THE_FDA_DOCS - Completed. Status: {}", status);

            // Check for warnings or errors
            if (status != null && (status.startsWith("WARNING") || status.startsWith("ERROR"))) {
                throw new ValidationException(
                        "FDA document verification failed",
                        List.of(new ValidationError("general", status))
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", status != null ? status : "Success");
            
            if (outData != null && !outData.isEmpty()) {
                Map<String, Object> cursorData = outData.get(0);
                response.put("verificationAcceptedDate", cursorData.get("VERIFICATION_ACCEPTED_DATE"));
                response.put("verificationAcceptedBy", cursorData.get("VERIFICATION_ACCEPTED_BY"));
                response.put("documentReceivedStatus", cursorData.get("DOCUMENT_RECEIVED_STATUS"));
            }
            
            return response;

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[SP-VERIFY] PROC_PDA_VERIFY_THE_FDA_DOCS - Error: {}", e.getMessage(), e);
            throw new ValidationException(
                    "FDA document verification failed",
                    List.of(new ValidationError("general", "Error verifying FDA documents: " + e.getMessage()))
            );
        }
    }


    @Override
    public SubmissionLogResponse getSubmissionLogInfo(Long transactionPoid, Long groupPoid, Long companyPoid) {
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException("PDA Entry not found"));

        String reportUrl = "/reports/pda-fda-submission-log?" +
                "docRef=" + entry.getDocRef() +
                "&fdaRef=" + (entry.getFdaRef() != null ? entry.getFdaRef() : "") +
                "&transactionPoid=" + transactionPoid;

        return SubmissionLogResponse.builder()
                .transactionPoid(transactionPoid)
                .docRef(entry.getDocRef())
                .fdaRef(entry.getFdaRef())
                .fdaPoid(entry.getFdaPoid())
                .reportUrl(reportUrl)
                .build();
    }

    public String callClearPdaEntryDetails(Long groupPoid, Long userPoid, Long companyPoid, Long pdaPoid) {
        try {
            logger.info("[SP-8] PROC_PDA_ENTRY_DTL_CLEAR - pdaPoid: {}", pdaPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_ENTRY_DTL_CLEAR")
                    .withoutProcedureColumnMetaDataAccess()
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_PDA_POID", Types.NUMERIC),
                            new SqlOutParameter("P_STATUS", Types.VARCHAR)
                    );

            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("P_LOGIN_GROUP_POID", groupPoid);
            inputMap.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));
            inputMap.put("P_LOGIN_COMPANY_POID", companyPoid);
            inputMap.put("P_PDA_POID", new BigDecimal(pdaPoid));

            Map<String, Object> result = jdbcCall.execute(inputMap);

            String status = (String) result.get("P_STATUS");

            logger.info("[SP-8] PROC_PDA_ENTRY_DTL_CLEAR - Completed. Status: {}", status);
            return status != null ? status : "Success";

        } catch (Exception e) {
            logger.error("[SP-8] PROC_PDA_ENTRY_DTL_CLEAR - Error: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    public String cancelPdaEntry(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, String cancelRemark) {
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException("PDA Entry not found"));

        String result = callCancelPdaEntry(groupPoid, companyPoid, userPoid, transactionPoid, cancelRemark);

        // Update entity status
        entry.setCancelRemark(cancelRemark);
        entry.setStatus("CANCELLED");
        entry.setDeleted("Y");
        entryHdrRepository.save(entry);

        // Return the actual stored procedure result or success message
        return (result != null && !result.trim().isEmpty()) ? result : "PDA entry cancelled successfully";
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(timeout = 300)
    public String uploadAcknowledgmentDetailsFromExcel(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, org.springframework.web.multipart.MultipartFile file) {
        logger.info("Starting acknowledgment upload from Excel - transactionPoid: {}, file: {}", transactionPoid, file.getOriginalFilename());
        
        if (file.isEmpty()) {
            throw new ValidationException(
                    "File is empty",
                    List.of(new ValidationError("file", "Please select a valid Excel file"))
            );
        }

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException("PDA Entry not found"));
        
        logger.info("Entry details - Status: '{}', RefType: '{}', PrincipalApproved: '{}'", 
                entry.getStatus(), entry.getRefType(), entry.getPrincipalApproved());

        String docId = "110-160_3";
        ExcelConfig config = getExcelConfig(docId);

        jdbcTemplate.update("DELETE FROM " + config.tempTableName);

        List<List<Object>> rowsCollection = new ArrayList<>();

        try (org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(file.getInputStream())) {
            if (workbook == null) {
                throw new ValidationException(
                        "Excel Workbook not able to open...",
                        List.of(new ValidationError("file", "Excel Workbook not able to open..."))
                );
            }
            
            org.apache.poi.ss.usermodel.Sheet sheet = config.excelSheetName != null 
                ? workbook.getSheet(config.excelSheetName) 
                : workbook.getSheetAt(0);
            
            if (sheet == null) {
                String sheetName = config.excelSheetName != null ? config.excelSheetName : "at index 0";
                throw new ValidationException(
                        "Excel sheet " + sheetName + " not able to open...",
                        List.of(new ValidationError("file", "Excel sheet " + sheetName + " not able to open..."))
                );
            }

            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                List<Object> colCollection = new ArrayList<>();
                for (int cn = config.startColNumber - 1; cn <= config.endColNumber - 1; cn++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.getCell(cn, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    switch (cell.getCellType()) {
                        case NUMERIC -> colCollection.add(cell.getNumericCellValue());
                        case STRING -> colCollection.add(cell.getStringCellValue());
                        case BOOLEAN -> colCollection.add(cell.getBooleanCellValue());
                        default -> colCollection.add("");
                    }
                }
                rowsCollection.add(colCollection);
            }
        } catch (Exception e) {
            throw new ValidationException(
                    "Error processing Excel file",
                    List.of(new ValidationError("file", "Failed to read Excel file: " + e.getMessage()))
            );
        }

        saveImportedDataAsync(config.startRowNumber, rowsCollection, config.tempTableName);
        return "Successfully imported Excel data to temp table";
    }

    private ExcelConfig getExcelConfig(String docId) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("PROC_GLOB_EXCEL_IMPORT_SHEETS")
                .declareParameters(
                        new SqlParameter("P_COMPANY_POID", Types.NUMERIC),
                        new SqlParameter("P_DOC_ID", Types.VARCHAR),
                        new SqlOutParameter("OUTDATA", oracle.jdbc.internal.OracleTypes.CURSOR),
                        new SqlOutParameter("P_STATUS", Types.VARCHAR)
                );

        Map<String, Object> result = jdbcCall.execute(
                Map.of(
                        "P_COMPANY_POID", UserContext.getCompanyPoid(),
                        "P_DOC_ID", docId
                )
        );

        List<Map<String, Object>> configs = (List<Map<String, Object>>) result.get("OUTDATA");
        if (configs == null || configs.isEmpty()) {
            throw new ValidationException(
                    "Excel configuration not found",
                    List.of(new ValidationError("file", "No Excel configuration found for DOC_ID: " + docId))
            );
        }

        Map<String, Object> configRow = configs.get(0);
        ExcelConfig config = new ExcelConfig();
        config.startRowNumber = ((Number) configRow.get("START_ROW_NUMBER")).intValue();
        config.startColNumber = ((Number) configRow.get("START_COL_NUMBER")).intValue();
        config.endColNumber = ((Number) configRow.get("END_COL_NUMBER")).intValue();
        config.tempTableName = (String) configRow.get("TEMP_TABLE_NAME");
        config.excelSheetName = (String) configRow.get("EXCEL_SHEET_NAME");
        return config;
    }

    protected void saveImportedDataAsync(int startRowNumber, List<List<Object>> rowsCollection, String tempTableName) {
        List<String> batchQueries = new ArrayList<>();
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
        
//        // Execute in batches of 50
//        int batchSize = 50;
//        for (int i = 0; i < batchQueries.size(); i += batchSize) {
//            int endIndex = Math.min(i + batchSize, batchQueries.size());
//            List<String> batch = batchQueries.subList(i, endIndex);
//            jdbcTemplate.batchUpdate(batch.toArray(new String[0]));
//        }
    }

   
    public String uploadAcknowledgmentDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, org.springframework.web.multipart.MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            processAcknowledgmentFileAsync(transactionPoid, groupPoid, companyPoid, userPoid, file);
            return "Acknowledgment file upload started. Processing in background...";
        } else {
            return callUploadAcknowledgmentDetails(groupPoid, userPoid, companyPoid, transactionPoid);
        }
    }

    @org.springframework.scheduling.annotation.Async
    public void processAcknowledgmentFileAsync(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, org.springframework.web.multipart.MultipartFile file) {
        try {
            uploadAcknowledgmentDetailsFromExcel(transactionPoid, groupPoid, companyPoid, userPoid, file);
        } catch (Exception e) {
            logger.error("Async acknowledgment file processing failed for transaction {}: {}", transactionPoid, e.getMessage(), e);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(timeout = 600)
    public String importTdrFileWithTransaction(org.springframework.web.multipart.MultipartFile file, Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        if (file.isEmpty()) {
            throw new ValidationException(
                    "TDR file is empty",
                    List.of(new ValidationError("file", "Please select a valid Excel file"))
            );
        }

        String result = uploadTdrDetailsFromExcel(transactionPoid, groupPoid, companyPoid, userPoid, file, false);
        
        // Log TDR file import action
        String logDetail = String.format("TDR file imported: %s", file.getOriginalFilename());
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
        
        return result;
    }

    @Override
    public List<PdaEntryTdrDetailResponse> uploadTdrDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, org.springframework.web.multipart.MultipartFile file) {
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        canEdit(entry);

        // Clear existing TDR details before importing new data
        callClearTdrDetails(groupPoid, userPoid, companyPoid, transactionPoid);
        
        callImportTdrDetail(groupPoid, userPoid, companyPoid, transactionPoid);
        
        // Log TDR details upload action
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), "TDR details uploaded");
        
        // Fetch and return the loaded TDR details
        List<PdaEntryTdrDetail> details = tdrDetailRepository.findByTransactionPoidOrderByDetRowIdAsc(transactionPoid);
        return details.stream()
                .map(this::toTdrDetailResponse)
                .collect(Collectors.toList());
    }

    @org.springframework.scheduling.annotation.Async
    public void processTdrFileAsync(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, org.springframework.web.multipart.MultipartFile file) {
        try {
            uploadTdrDetailsFromExcel(transactionPoid, groupPoid, companyPoid, userPoid, file, true);
            
            // Log successful async processing completion
            String logDetail = String.format("TDR file processing completed successfully: %s", file.getOriginalFilename());
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
            
        } catch (Exception e) {
            logger.error("Async TDR file processing failed for transaction {}: {}", transactionPoid, e.getMessage(), e);
            
            // Log failed async processing
            String logDetail = String.format("TDR file processing failed: %s - Error: %s", 
                    file.getOriginalFilename(), e.getMessage());
            loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
        }
    }

    @org.springframework.transaction.annotation.Transactional(timeout = 600)
    public String uploadTdrDetailsFromExcel(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, org.springframework.web.multipart.MultipartFile file, boolean callStoredProcedure) {
        if (file.isEmpty()) {
            throw new ValidationException(
                    "File is empty",
                    List.of(new ValidationError("file", "Please select a valid Excel file"))
            );
        }

        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException("PDA Entry not found"));

        canEdit(entry);

        String docId = "110-160_1";
        ExcelConfig config = getExcelConfig(docId);
        logger.info("Excel config - startRowNumber: {}, startColNumber: {}, endColNumber: {}, tempTable: {}", 
                config.startRowNumber, config.startColNumber, config.endColNumber, config.tempTableName);

        jdbcTemplate.update("DELETE FROM " + config.tempTableName);

        List<List<Object>> rowsCollection = new ArrayList<>();

        try (org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(file.getInputStream())) {
            if (workbook == null) {
                throw new ValidationException(
                        "Excel Workbook not able to open...",
                        List.of(new ValidationError("file", "Excel Workbook not able to open..."))
                );
            }
            
            org.apache.poi.ss.usermodel.Sheet sheet = config.excelSheetName != null 
                ? workbook.getSheet(config.excelSheetName) 
                : workbook.getSheetAt(0);
            
            if (sheet == null) {
                String sheetName = config.excelSheetName != null ? config.excelSheetName : "at index 0";
                throw new ValidationException(
                        "Excel sheet " + sheetName + " not able to open...",
                        List.of(new ValidationError("file", "Excel sheet " + sheetName + " not able to open..."))
                );
            }

            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                List<Object> colCollection = new ArrayList<>();
                for (int cn = config.startColNumber - 1; cn <= config.endColNumber - 1; cn++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.getCell(cn, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    switch (cell.getCellType()) {
                        case NUMERIC -> colCollection.add(cell.getNumericCellValue());
                        case STRING -> colCollection.add(cell.getStringCellValue());
                        case BOOLEAN -> colCollection.add(cell.getBooleanCellValue());
                        default -> colCollection.add("");
                    }
                }
                rowsCollection.add(colCollection);
            }
        } catch (Exception e) {
            throw new ValidationException(
                    "Error processing Excel file",
                    List.of(new ValidationError("file", "Failed to read Excel file: " + e.getMessage()))
            );
        }

        logger.info("Total rows read from Excel: {}, Rows to be inserted (after startRowNumber {}): {}", 
                rowsCollection.size(), config.startRowNumber, Math.max(0, rowsCollection.size() - config.startRowNumber + 1));
        
        saveImportedDataAsync(config.startRowNumber, rowsCollection, config.tempTableName);
        
        // Verify data was inserted
        Integer insertedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + config.tempTableName, Integer.class);
        logger.info("Rows inserted into temp table {}: {}", config.tempTableName, insertedCount);

        if (callStoredProcedure) {
            String result = callImportTdrDetail(groupPoid, userPoid, companyPoid, transactionPoid);
            if (result != null && result.startsWith("ERROR")) {
                throw new ValidationException(
                        "Failed to upload TDR details from Excel",
                        List.of(new ValidationError("file", result))
                );
            }
            return result != null ? result : "TDR details uploaded successfully from Excel";
        } else {
            return String.format("Successfully imported %d rows to temp table. Click 'Load Details' to process.", insertedCount);
        }
    }

    @Override
    public String clearTdrDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        canEdit(entry);

        String result = callClearTdrDetails(groupPoid, userPoid, companyPoid, transactionPoid);
        
        // Log TDR details clear action
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), "TDR details cleared");
        
        return result;
    }

    @Override
    public String processTdrCharges(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoid(transactionPoid).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        canEdit(entry);

        String result = callDefaultChargesFromTdr(groupPoid, userPoid, companyPoid, transactionPoid, entry.getArrivalDate());
        
        // Log TDR charges processing action
        logger.info("TDR charges processed for transactionPoid: {}", transactionPoid);
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), transactionPoid.toString(), "TDR charges processed");
        
        return result;
    }
}

