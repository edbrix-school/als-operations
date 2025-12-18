package com.asg.operations.pdaentryform.service.impl;

import com.asg.common.lib.security.util.UserContext;
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
import com.asg.operations.pdaporttariffmaster.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import oracle.jdbc.internal.OracleTypes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

    @Override
    @Transactional(readOnly = true)
    public PdaEntryResponse getPdaEntryById(Long transactionPoid, Long groupPoid, Long companyPoid) {

        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        callGetPdaRefWhereClause(BigDecimal.valueOf(groupPoid));

        return toResponse(entry);
    }

    @Override
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
        String docRef = docRefGenerator.generateDocRef(BigDecimal.valueOf(groupPoid));
        int retries = 0;
        while (entryHdrRepository.existsByDocRef(docRef) && retries < 5) {
            docRef = docRefGenerator.generateDocRef(BigDecimal.valueOf(groupPoid));
            retries++;
        }
        entry.setDocRef(docRef);
        logger.info("Generated unique docRef: {}", docRef);

        // Auto-populate vessel details if vesselPoid is provided
        if (request.getVesselPoid() != null) {
            VesselDetailsResponse vesselDetails = getVesselDetails(request.getVesselPoid(), groupPoid, companyPoid, userPoid);
            if (vesselDetails != null) {
                entry.setVesselTypePoid(vesselDetails.getVesselTypePoid());
                entry.setImoNumber(vesselDetails.getImoNumber());
                entry.setGrt(vesselDetails.getGrt());
                entry.setNrt(vesselDetails.getNrt());
                entry.setDwt(vesselDetails.getDwt());
            }
        }

        // Auto-populate currency if principalPoid is provided
        if (request.getPrincipalPoid() != null) {
            setDefaultCurrency(groupPoid, companyPoid, userPoid, entry.getTransactionPoid(), request.getPrincipalPoid(), entry);
        }

        // Set audit fields
        LocalDateTime now = LocalDateTime.now();
        entry.setCreatedBy(UserContext.getUserId());
        entry.setCreatedDate(now);
        entry.setLastModifiedBy(UserContext.getUserId());
        entry.setLastModifiedDate(now);

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

        return toResponse(entry);
    }

    @Override
    public PdaEntryResponse updatePdaEntry(Long transactionPoid, PdaEntryRequest request, Long groupPoid, Long companyPoid, Long userPoid) {

        // Find existing entry
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        // Check edit permissions
        if (!canEdit(entry)) {
            throw new ValidationException(
                    "Entry cannot be edited",
                    List.of(new ValidationError("status", "Entry is in a state that does not allow editing"))
            );
        }

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

        // Auto-populate vessel details if vesselPoid changed
        if (request.getVesselPoid() != null &&
                !Objects.equals(entry.getVesselPoid(), request.getVesselPoid())) {
            VesselDetailsResponse vesselDetails = getVesselDetails(request.getVesselPoid(), groupPoid, companyPoid, userPoid);
            if (vesselDetails != null) {
                entry.setVesselTypePoid(vesselDetails.getVesselTypePoid());
                entry.setImoNumber(vesselDetails.getImoNumber());
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

        // Update audit fields
        entry.setLastModifiedBy(UserContext.getUserId());
        entry.setLastModifiedDate(LocalDateTime.now());

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

        return toResponse(entry);
    }

    @Override
    public void deletePdaEntry(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {

        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        // Check if deletion is allowed
        if ("CONFIRMED".equals(entry.getStatus()) || "CLOSED".equals(entry.getStatus())) {
            throw new ValidationException(
                    "Entry cannot be deleted",
                    List.of(new ValidationError("status", "Entry is in a state that does not allow deletion"))
            );
        }

        if ("Y".equals(entry.getPrincipalApproved()) && "GENERAL".equals(entry.getRefType())) {
            throw new ValidationException(
                    "Entry cannot be deleted",
                    List.of(new ValidationError("status", "Principal approved entries cannot be deleted"))
            );
        }

        // Call cancel SP if needed
        callCancelPdaEntry(groupPoid, companyPoid, userPoid, transactionPoid, "Deleted by user");

        // Soft delete
        entry.setDeleted("Y");
        entry.setLastModifiedBy(String.valueOf(userPoid));
        entry.setLastModifiedDate(LocalDateTime.now());

        entryHdrRepository.save(entry);
    }

    // Charge Details Methods - Batch 5

    @Override
    public List<PdaEntryChargeDetailResponse> getChargeDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {

        // Validate transaction exists
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        entityManager.flush();

        // Get all charge details
        List<PdaEntryDtl> details = entryDtlRepository.findByTransactionPoidOrderBySeqnoAscDetRowIdAsc(transactionPoid);

        return details.stream()
                .map(this::toChargeDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PdaEntryChargeDetailResponse> bulkSaveChargeDetails(Long transactionPoid, BulkSaveChargeDetailsRequest request, Long groupPoid, Long companyPoid, String userId) {

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        if (!canEdit(entry)) {
            throw new ValidationException(
                    "Entry cannot be edited",
                    List.of(new ValidationError("status", "Entry is in a state that does not allow editing"))
            );
        }

        LocalDateTime now = LocalDateTime.now();

        // Process creates and updates
        if (request.getChargeDetails() != null) {
            for (PdaEntryChargeDetailRequest detailRequest : request.getChargeDetails()) {
                if (detailRequest.getDetRowId() == null) {
                    // Create new
                    createChargeDetail(transactionPoid, detailRequest, userId, now, companyPoid);
                } else {
                    // Update existing
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
    public void deleteChargeDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid, String userId) {

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        if (!canEdit(entry)) {
            throw new ValidationException(
                    "Entry cannot be edited",
                    List.of(new ValidationError("status", "Entry is in a state that does not allow editing"))
            );
        }

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
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        if (!canEdit(entry)) {
            throw new ValidationException(
                    "Entry cannot be edited",
                    List.of(new ValidationError("status", "Entry is in a state that does not allow editing"))
            );
        }

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
        entry.setLastModifiedBy(UserContext.getUserId());
        entry.setLastModifiedDate(LocalDateTime.now());
        entryHdrRepository.save(entry);
    }

    @Override
    public List<PdaEntryChargeDetailResponse> recalculateChargeDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        if (!canEdit(entry)) {
            throw new ValidationException(
                    "Entry cannot be edited",
                    List.of(new ValidationError("status", "Entry is in a state that does not allow editing"))
            );
        }

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

        // Return updated charge details
        return getChargeDetails(transactionPoid, groupPoid, companyPoid);
    }

    @Override
    public List<PdaEntryChargeDetailResponse> loadDefaultCharges(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        if (!canEdit(entry)) {
            throw new ValidationException(
                    "Entry cannot be edited",
                    List.of(new ValidationError("status", "Entry is in a state that does not allow editing"))
            );
        }

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

        // Return loaded charge details
        return getChargeDetails(transactionPoid, groupPoid, companyPoid);
    }

    // Vehicle Details Methods - Batch 6

    @Override
    public List<PdaEntryVehicleDetailResponse> getVehicleDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {

        // Validate transaction exists
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
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
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        if (!canEdit(entry)) {
            throw new ValidationException(
                    "Entry cannot be edited",
                    List.of(new ValidationError("status", "Entry is in a state that does not allow editing"))
            );
        }

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
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        if (!canEdit(entry)) {
            throw new ValidationException(
                    "Entry cannot be edited",
                    List.of(new ValidationError("status", "Entry is in a state that does not allow editing"))
            );
        }

        // Call stored procedure to import vehicle details
        callImportVehicleDetails(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    @Override
    public void clearVehicleDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        if (!canEdit(entry)) {
            throw new ValidationException(
                    "Entry cannot be edited",
                    List.of(new ValidationError("status", "Entry is in a state that does not allow editing"))
            );
        }

        // Call stored procedure to clear vehicle details
        callClearVehicleDetails(groupPoid, userPoid, companyPoid, transactionPoid);
    }

    public void clearTdrDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        if (!canEdit(entry)) {
            throw new ValidationException(
                    "Entry cannot be edited",
                    List.of(new ValidationError("status", "Entry is in a state that does not allow editing"))
            );
        }

        callClearTdrDetails(groupPoid, userPoid, companyPoid, transactionPoid);
    }

    public void importTdrDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        if (!canEdit(entry)) {
            throw new ValidationException(
                    "Entry cannot be edited",
                    List.of(new ValidationError("status", "Entry is in a state that does not allow editing"))
            );
        }

        callImportTdrDetail(groupPoid, userPoid, companyPoid, transactionPoid);
    }

    public void updateFdaFromPda(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        callUpdateFdaFromPda(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    public void submitPdaToFda(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        callSubmitPdaToFda(groupPoid, companyPoid, userPoid, transactionPoid);
    }

    public void rejectFdaDocs(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, String correctionRemarks) {
        callRejectFdaDocs(groupPoid, companyPoid, userPoid, transactionPoid, correctionRemarks);
    }

    public void uploadAcknowledgmentDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        callUploadAcknowledgmentDetails(groupPoid, userPoid, companyPoid, transactionPoid);
    }

    public void clearAcknowledgmentDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
        callClearAcknowledgmentDetails(groupPoid, userPoid, companyPoid, transactionPoid);
    }

    public Map<String, Object> getVoyageDefaults(Long groupPoid, Long companyPoid, Long userPoid, BigDecimal voyagePoid) {
        return callGetVoyageDefaults(groupPoid, companyPoid, userPoid, voyagePoid);
    }

    @Override
    public void publishVehicleDetailsForImport(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {

        // Validate transaction exists and is editable
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        if (!canEdit(entry)) {
            throw new ValidationException(
                    "Entry cannot be edited",
                    List.of(new ValidationError("status", "Entry is in a state that does not allow editing"))
            );
        }

        // Call stored procedure to publish for import
        callPublishForImport(groupPoid, userPoid, companyPoid, transactionPoid);
    }

    // TDR Details Methods - Batch 6

    @Override
    public List<PdaEntryTdrDetailResponse> getTdrDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {

        // Validate transaction exists
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
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
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        if (!canEdit(entry)) {
            throw new ValidationException(
                    "Entry cannot be edited",
                    List.of(new ValidationError("status", "Entry is in a state that does not allow editing"))
            );
        }

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
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
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
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
                "PDA Entry not found with id: " + transactionPoid
        ));

        if (!canEdit(entry)) {
            throw new ValidationException(
                    "Entry cannot be edited",
                    List.of(new ValidationError("status", "Entry is in a state that does not allow editing"))
            );
        }

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
        PdaEntryHdr entry = entryHdrRepository.findByTransactionPoidAndFilters(
                transactionPoid, groupPoid, companyPoid
        ).orElseThrow(() -> new ResourceNotFoundException(
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
                    );

            Map<String, Object> inParams = new HashMap<>();
            inParams.put("P_LOGIN_GROUP_POID", groupPoid);
            inParams.put("P_LOGIN_COMPANY_POID", companyPoid);
            inParams.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));
            inParams.put("P_VESSEL_POID", vesselPoid);

            Map<String, Object> result = jdbcCall.execute(inParams);

            List<Map<String, Object>> rows = (List<Map<String, Object>>) result.get("OUTDATA");

            VesselDetailsResponse response = new VesselDetailsResponse();
            if (!rows.isEmpty()) {
                Map<String, Object> row = rows.get(0);
                response.setVesselTypePoid((BigDecimal) row.get("VESSEL_TYPE_POID"));
                response.setImoNumber((String) row.get("IMO_NUMBER"));
                response.setGrt((BigDecimal) row.get("GRT"));
                response.setNrt((BigDecimal) row.get("NRT"));
                response.setDwt((BigDecimal) row.get("DWT"));
            }

            logger.info("[SP-20] PROC_PDA_DEFAULT_VESSEL_DTLS - Completed");
            return response;

        } catch (Exception e) {
            logger.error("[SP-20] PROC_PDA_DEFAULT_VESSEL_DTLS - Error: {}", e.getMessage(), e);
            return new VesselDetailsResponse();
        }
    }


    // Private helper methods

    private void validatePdaEntryRequest(PdaEntryRequest request, Long transactionPoid) {
        List<ValidationError> errors = new ArrayList<>();

        // Validate refType is mandatory
        if (request.getRefType() == null || request.getRefType().trim().isEmpty()) {
            errors.add(new ValidationError("refType", "Ref type is mandatory"));
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
                errors.add(new ValidationError("arrivalDate", "Arrival date is mandatory for GENERAL ref type"));
            }
            if (request.getSailDate() == null) {
                errors.add(new ValidationError("sailDate", "Sail date is mandatory for GENERAL ref type"));
            }
        }

        // Validate date logic: sailDate must be after or equal to arrivalDate
        if (request.getArrivalDate() != null && request.getSailDate() != null) {
            if (request.getSailDate().isBefore(request.getArrivalDate())) {
                errors.add(new ValidationError("sailDate", "ETD should not be before the ETA"));
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    private boolean canEdit(PdaEntryHdr entry) {
        // For GENERAL ref type
        if ("GENERAL".equals(entry.getRefType())) {
            if ("Y".equals(entry.getPrincipalApproved())) {
                return false;
            }
            if ("CONFIRMED".equals(entry.getStatus())) {
                return false;
            }
        }
        // For other ref types, check status
        if ("CONFIRMED".equals(entry.getStatus()) || "CLOSED".equals(entry.getStatus())) {
            return false;
        }
        return true;
    }

    private void mapRequestToEntity(PdaEntryRequest request, PdaEntryHdr entity) {
        // Map all fields from request to entity
        entity.setTransactionDate(request.getTransactionDate());
        entity.setPrincipalPoid(request.getPrincipalPoid());
        entity.setPrincipalName(request.getPrincipalName());
        entity.setPrincipalContact(request.getPrincipalContact());
        entity.setVoyagePoid(request.getVoyagePoid());
        entity.setVoyageNo(request.getVoyageNo());
        entity.setVesselPoid(request.getVesselPoid());
        entity.setVesselTypePoid(request.getVesselTypePoid());
        entity.setGrt(request.getGrt());
        entity.setNrt(request.getNrt());
        entity.setDwt(request.getDwt());
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
        response.setComodityDet(lovService.getLovItemByPoid(entity.getComodityPoid() != null ? Long.valueOf(entity.getComodityPoid()) : null, "COMODITY", entity.getGroupPoid(), entity.getCompanyPoid(), null));
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
            inParams.put("P_VESSEL_POID", vesselPoid.toString()); // VARCHAR2
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
        detail.setCreatedBy(userId);
        detail.setCreatedDate(now);
        detail.setLastModifiedBy(userId);
        detail.setLastModifiedDate(now);

        // Save
        entryDtlRepository.save(detail);
    }

    private void updateChargeDetail(Long transactionPoid, PdaEntryChargeDetailRequest request,
                                    String userId, LocalDateTime now, Long companyPoid) {
        // Validate detail exists
        PdaEntryDtlId detailId = new PdaEntryDtlId(transactionPoid, request.getDetRowId());
        PdaEntryDtl detail = entryDtlRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Charge detail not found with id: " + request.getDetRowId()
                ));

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
        detail.setLastModifiedBy(userId);
        detail.setLastModifiedDate(now);

        // Save
        entryDtlRepository.save(detail);
    }

    private void deleteChargeDetailRecord(Long transactionPoid, Long detRowId) {
        PdaEntryDtlId detailId = new PdaEntryDtlId(transactionPoid, detRowId);
        PdaEntryDtl detail = entryDtlRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Charge detail not found with id: " + detRowId
                ));
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

    private TaxInfo getChargeTaxInfo(Long companyPoid, Date transactionDate, String partyType, BigDecimal partyPoid, BigDecimal chargePoid) {
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
                            rs.getBigDecimal("TAX_PERCENTAGE")
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
        entry.setLastModifiedBy(userId);
        entry.setLastModifiedDate(LocalDateTime.now());
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

    private PdaEntryChargeDetailResponse toChargeDetailResponse(PdaEntryDtl entity) {
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

    private void callReCalculateCharges(
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

        } catch (Exception e) {
            logger.error("[SP-3] PROC_PDA_RE_CALCULATE - Error: {}", e.getMessage(), e);
        }
    }


    private void callLoadDefaultCharges(
            Long groupPoid, Long userPoid, Long companyPoid, Long transactionPoid,
            BigDecimal vesselPoid, BigDecimal vesselTypePoid, BigDecimal grt, BigDecimal nrt, BigDecimal dwt,
            BigDecimal portPoid, LocalDate arrivalDate, LocalDate sailDate,
            String harbourCallType, BigDecimal totalQuantity, BigDecimal numberOfDays, BigDecimal principalPoid
    ) {
        try {
            logger.info("[SP-2] PROC_PDA_LOAD_DEF_CHARGE - transactionPoid: {}, vesselPoid: {}", transactionPoid, vesselPoid);

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

        } catch (Exception e) {
            logger.error("[SP-2] PROC_PDA_LOAD_DEF_CHARGE - Error: {}", e.getMessage(), e);
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
        detail.setCreatedBy(userId);
        detail.setCreatedDate(now);
        detail.setLastModifiedBy(userId);
        detail.setLastModifiedDate(now);

        // Save
        vehicleDtlRepository.save(detail);
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
        detail.setLastModifiedBy(userId);
        detail.setLastModifiedDate(now);

        // Save
        vehicleDtlRepository.save(detail);
    }

    private void deleteVehicleDetailRecord(Long transactionPoid, Long detRowId) {
        PdaEntryVehicleDtlId detailId = new PdaEntryVehicleDtlId(transactionPoid, detRowId);
        PdaEntryVehicleDtl detail = vehicleDtlRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicle detail not found with id: " + detRowId
                ));
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
        detail.setCreatedBy(userId);
        detail.setCreatedDate(now);
        detail.setLastModifiedBy(userId);
        detail.setLastModifiedDate(now);

        // Save
        tdrDetailRepository.save(detail);
    }

    private void updateTdrDetail(Long transactionPoid, PdaEntryTdrDetailRequest request,
                                 String userId, LocalDateTime now) {
        // Validate detail exists
        PdaEntryTdrDetailId detailId = new PdaEntryTdrDetailId(transactionPoid, request.getDetRowId());
        PdaEntryTdrDetail detail = tdrDetailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TDR detail not found with id: " + request.getDetRowId()
                ));

        // Map request to entity
        mapTdrDetailRequestToEntity(request, detail);

        // Update audit fields
        detail.setLastModifiedBy(userId);
        detail.setLastModifiedDate(now);

        // Save
        tdrDetailRepository.save(detail);
    }

    private void deleteTdrDetailRecord(Long transactionPoid, Long detRowId) {
        PdaEntryTdrDetailId detailId = new PdaEntryTdrDetailId(transactionPoid, detRowId);
        PdaEntryTdrDetail detail = tdrDetailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TDR detail not found with id: " + detRowId
                ));
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
        // Create new entity
        PdaEntryAcknowledgmentDtl detail = new PdaEntryAcknowledgmentDtl();
        detail.setTransactionPoid(transactionPoid);
        long detRowId = System.nanoTime() % 1000000;
        detail.setDetRowId(detRowId > 0 ? detRowId : Math.abs(detRowId) + 1);

        // Map request to entity
        mapAcknowledgmentDetailRequestToEntity(request, detail);

        // Set audit fields
        detail.setCreatedBy(userId);
        detail.setCreatedDate(now);
        detail.setLastModifiedBy(userId);
        detail.setLastModifiedDate(now);

        // Save
        acknowledgmentDtlRepository.save(detail);
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
        detail.setLastModifiedBy(userId);
        detail.setLastModifiedDate(now);

        // Save
        acknowledgmentDtlRepository.save(detail);
    }

    private void deleteAcknowledgmentDetailRecord(Long transactionPoid, Long detRowId) {
        PdaEntryAcknowledgmentDtlId detailId = new PdaEntryAcknowledgmentDtlId(transactionPoid, detRowId);
        PdaEntryAcknowledgmentDtl detail = acknowledgmentDtlRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Acknowledgment detail not found with id: " + detRowId
                ));
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

    // Additional SP Methods with Logging

    public void callImportTdrDetail(Long groupPoid, Long userPoid, Long companyPoid, Long transactionPoid) {
        try {
            logger.info("[SP-3] PROC_PDA_IMPORT_TDR_DETAIL2 - transactionPoid: {}", transactionPoid);
            String sql = "{ call PROC_PDA_IMPORT_TDR_DETAIL2(?, ?, ?, ?) }";
            jdbcTemplate.update(sql, groupPoid, userPoid, companyPoid, transactionPoid);
            logger.info("[SP-3] PROC_PDA_IMPORT_TDR_DETAIL2 - Completed");
        } catch (Exception e) {
            logger.error("[SP-3] PROC_PDA_IMPORT_TDR_DETAIL2 - Error: {}", e.getMessage());
        }
    }

    public void callDefaultChargesFromTdr(Long groupPoid, Long userPoid, Long companyPoid, Long transactionPoid, LocalDate arrivalDate) {
        try {
            logger.info("[SP-4] PROC_PDA_DEFAULT_CH_FROM_TDR - transactionPoid: {}", transactionPoid);
            String sql = "{ call PROC_PDA_DEFAULT_CH_FROM_TDR(?, ?, ?, ?, ?) }";
            jdbcTemplate.update(sql, groupPoid, userPoid, companyPoid, transactionPoid, arrivalDate);
            logger.info("[SP-4] PROC_PDA_DEFAULT_CH_FROM_TDR - Completed");
        } catch (Exception e) {
            logger.error("[SP-4] PROC_PDA_DEFAULT_CH_FROM_TDR - Error: {}", e.getMessage());
        }
    }

    public void callClearTdrDetails(Long groupPoid, Long userPoid, Long companyPoid, Long transactionPoid) {
        try {
            logger.info("[SP-5] PROC_PDA_TDR_DETAIL_CLEAR - transactionPoid: {}", transactionPoid);
            String sql = "{ call PROC_PDA_TDR_DETAIL_CLEAR(?, ?, ?, ?) }";
            jdbcTemplate.update(sql, groupPoid, userPoid, companyPoid, transactionPoid);
            logger.info("[SP-5] PROC_PDA_TDR_DETAIL_CLEAR - Completed");
        } catch (Exception e) {
            logger.error("[SP-5] PROC_PDA_TDR_DETAIL_CLEAR - Error: {}", e.getMessage());
        }
    }

    public void callCancelPdaEntry(Long groupPoid, Long companyPoid, Long userPoid, Long pdaPoid, String cancelRemark) {
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
        } catch (Exception e) {
            logger.error("[SP-6] PROC_PDA_ENTRY_CANCEL - ERROR: {}", e.getMessage(), e);
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

    public String createFda(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid) {
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
                    List.of(new ValidationError("general", "Error creating FDA: " + e.getMessage()))
            );
        }
    }

    public void callUpdateFdaFromPda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {
        try {
            logger.info("[SP-8] PROC_PDA_DTL_UPDATE_FDA - transactionPoid: {}", transactionPoid);
            String sql = "{ call PROC_PDA_DTL_UPDATE_FDA(?, ?, ?, ?) }";
            jdbcTemplate.update(sql, groupPoid, companyPoid, userPoid, transactionPoid);
            logger.info("[SP-8] PROC_PDA_DTL_UPDATE_FDA - Completed");
        } catch (Exception e) {
            logger.error("[SP-8] PROC_PDA_DTL_UPDATE_FDA - Error: {}", e.getMessage());
        }
    }

    public void callSubmitPdaToFda(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid) {
        try {
            logger.info("[SP-9] PROC_PDA_TO_FDA_DOC_SUBMISSION - transactionPoid: {}", transactionPoid);
            String sql = "{ call PROC_PDA_TO_FDA_DOC_SUBMISSION(?, ?, ?, ?) }";
            jdbcTemplate.update(sql, groupPoid, companyPoid, userPoid, transactionPoid);
            logger.info("[SP-9] PROC_PDA_TO_FDA_DOC_SUBMISSION - Completed");
        } catch (Exception e) {
            logger.error("[SP-9] PROC_PDA_TO_FDA_DOC_SUBMISSION - Error: {}", e.getMessage());
        }
    }

    public void callRejectFdaDocs(Long groupPoid, Long companyPoid, Long userPoid, Long transactionPoid, String correctionRemarks) {
        try {
            logger.info("[SP-11] PROC_PDA_REJECT_THE_FDA_DOCS - transactionPoid: {}", transactionPoid);
            String sql = "{ call PROC_PDA_REJECT_THE_FDA_DOCS(?, ?, ?, ?, ?) }";
            jdbcTemplate.update(sql, groupPoid, companyPoid, userPoid, transactionPoid, correctionRemarks);
            logger.info("[SP-11] PROC_PDA_REJECT_THE_FDA_DOCS - Completed");
        } catch (Exception e) {
            logger.error("[SP-11] PROC_PDA_REJECT_THE_FDA_DOCS - Error: {}", e.getMessage());
        }
    }

    public void callUploadAcknowledgmentDetails(
            Long groupPoid,
            Long userPoid,
            Long companyPoid,
            Long transactionPoid
    ) {
        try {
            logger.info("[SP-12] PROC_PDA_ACKNOW_DTLS_UPLOAD - transactionPoid: {}", transactionPoid);

            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("PROC_PDA_ACKNOW_DTLS_UPLOAD")
                    .declareParameters(
                            new SqlParameter("P_LOGIN_GROUP_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_USER_POID", Types.NUMERIC),
                            new SqlParameter("P_LOGIN_COMPANY_POID", Types.NUMERIC),
                            new SqlParameter("P_TRANSACTION_POID", Types.NUMERIC),
                            new SqlOutParameter("P_STATUS", Types.VARCHAR)
                    );

            Map<String, Object> params = new HashMap<>();
            params.put("P_LOGIN_GROUP_POID", groupPoid);
            params.put("P_LOGIN_USER_POID", new BigDecimal(userPoid));
            params.put("P_LOGIN_COMPANY_POID", companyPoid);
            params.put("P_TRANSACTION_POID", new BigDecimal(transactionPoid));

            Map<String, Object> result = jdbcCall.execute(params);

            String status = (String) result.get("P_STATUS");

            logger.info("[SP-12] PROC_PDA_ACKNOW_DTLS_UPLOAD - Completed. Status: {}", status);

        } catch (Exception e) {
            logger.error("[SP-12] PROC_PDA_ACKNOW_DTLS_UPLOAD - Error: {}", e.getMessage(), e);
        }
    }


    public void callClearAcknowledgmentDetails(
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

        } catch (Exception e) {
            logger.error("[SP-13] PROC_PDA_ACKNOW_DTL_CLEAR - Error: {}", e.getMessage(), e);
        }
    }

    public Map<String, Object> callGetVoyageDefaults(Long groupPoid, Long companyPoid, Long userPoid, BigDecimal voyagePoid) {
        try {
            logger.info("[SP-21] PROC_PDA_VOYAGE_DEFAULT_DTLS - voyagePoid: {}", voyagePoid);
            String sql = "{ call PROC_PDA_VOYAGE_DEFAULT_DTLS(?, ?, ?, ?) }";
            try {
                Map<String, Object> result = jdbcTemplate.queryForMap(sql, groupPoid, companyPoid, userPoid, voyagePoid);
                logger.info("[SP-21] PROC_PDA_VOYAGE_DEFAULT_DTLS - Completed");
                return result;
            } catch (Exception ex) {
                logger.debug("[SP-21] PROC_PDA_VOYAGE_DEFAULT_DTLS - No result returned");
                jdbcTemplate.update(sql, groupPoid, companyPoid, userPoid, voyagePoid);
                logger.info("[SP-21] PROC_PDA_VOYAGE_DEFAULT_DTLS - Completed");
                return null;
            }
        } catch (Exception e) {
            logger.error("[SP-21] PROC_PDA_VOYAGE_DEFAULT_DTLS - Error: {}", e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<PdaEntryListResponse> getAllPdaWithFilters(
            Long groupPoid, Long companyPoid,
            GetAllPdaFilterRequest filterRequest,
            int page, int size, String sort) {

        // Build dynamic SQL query
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT p.TRANSACTION_POID, p.TRANSACTION_DATE, p.GROUP_POID, p.COMPANY_POID, ");
        sqlBuilder.append("p.DOC_REF, p.TRANSACTION_REF, p.PRINCIPAL_POID, p.PRINCIPAL_NAME, p.PRINCIPAL_CONTACT, ");
        sqlBuilder.append("p.VOYAGE_POID, p.VOYAGE_NO, p.VESSEL_POID, p.VESSEL_TYPE_POID, p.GRT, p.NRT, p.DWT, ");
        sqlBuilder.append("p.IMO_NUMBER, p.ARRIVAL_DATE, p.SAIL_DATE, p.ACTUAL_ARRIVAL_DATE, p.ACTUAL_SAIL_DATE, ");
        sqlBuilder.append("p.VESSEL_SAIL_DATE, p.PORT_POID, p.PORT_DESCRIPTION, p.LINE_POID, p.COMODITY_POID, ");
        sqlBuilder.append("p.OPERATION_TYPE, p.HARBOUR_CALL_TYPE, p.IMPORT_QTY, p.EXPORT_QTY, p.TRANSHIPMENT_QTY, ");
        sqlBuilder.append("p.TOTAL_QUANTITY, p.UNIT, p.NUMBER_OF_DAYS, p.CURRENCY_CODE, p.CURRENCY_RATE, ");
        sqlBuilder.append("p.TOTAL_AMOUNT, p.COST_CENTRE_POID, p.SALESMAN_POID, p.TERMS_POID, p.ADDRESS_POID, ");
        sqlBuilder.append("p.REF_TYPE, p.SUB_CATEGORY, p.STATUS, p.CARGO_DETAILS, p.REMARKS, ");
        sqlBuilder.append("p.VESSEL_VERIFIED, p.VESSEL_VERIFIED_DATE, p.VESSEL_VERIFIED_BY, p.VESSEL_HANDLED_BY, ");
        sqlBuilder.append("p.URGENT_APPROVAL, p.PRINCIPAL_APPROVED, p.PRINCIPAL_APPROVED_DATE, p.PRINCIPAL_APPROVED_BY, ");
        sqlBuilder.append("p.PRINCIPAL_APRVL_DAYS, p.REMINDER_MINUTES, p.PRINT_PRINCIPAL, p.FDA_REF, p.FDA_POID, ");
        sqlBuilder.append("p.MULTIPLE_FDA, p.NOMINATED_PARTY_TYPE, p.NOMINATED_PARTY_POID, p.BANK_POID, ");
        sqlBuilder.append("p.BUSINESS_REF_BY, p.PMI_DOCUMENT, p.CANCEL_REMARK, p.OLD_PORT_CODE, p.OLD_VESSEL_CODE, ");
        sqlBuilder.append("p.OLD_PRINCIPAL_CODE, p.OLD_VOYAGE_JOB, p.MENAS_DUES, p.DOCUMENT_SUBMITTED_DATE, ");
        sqlBuilder.append("p.DOCUMENT_SUBMITTED_BY, p.DOCUMENT_SUBMITTED_STATUS, p.DOCUMENT_RECEIVED_DATE, ");
        sqlBuilder.append("p.DOCUMENT_RECEIVED_FROM, p.DOCUMENT_RECEIVED_STATUS, p.SUBMISSION_ACCEPTED_DATE, ");
        sqlBuilder.append("p.SUBMISSION_ACCEPTED_BY, p.VERIFICATION_ACCEPTED_DATE, p.VERIFICATION_ACCEPTED_BY, ");
        sqlBuilder.append("p.ACCTS_CORRECTION_REMARKS, p.ACCTS_RETURNED_DATE, p.DELETED, p.CREATED_BY, ");
        sqlBuilder.append("p.CREATED_DATE, p.LASTMODIFIED_BY, p.LASTMODIFIED_DATE ");
        sqlBuilder.append("FROM PDA_ENTRY_HDR p ");
        sqlBuilder.append("WHERE p.GROUP_POID = :groupPoid AND p.COMPANY_POID = :companyPoid ");

        // Apply isDeleted filter
        if (filterRequest.getIsDeleted() != null && "N".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND (p.DELETED IS NULL OR p.DELETED != 'Y') ");
        } else if (filterRequest.getIsDeleted() != null && "Y".equalsIgnoreCase(filterRequest.getIsDeleted())) {
            sqlBuilder.append("AND p.DELETED = 'Y' ");
        }

        // Apply date range filters
        if (org.springframework.util.StringUtils.hasText(filterRequest.getFrom())) {
            sqlBuilder.append("AND TRUNC(p.TRANSACTION_DATE) >= TO_DATE(:fromDate, 'YYYY-MM-DD') ");
        }
        if (org.springframework.util.StringUtils.hasText(filterRequest.getTo())) {
            sqlBuilder.append("AND TRUNC(p.TRANSACTION_DATE) <= TO_DATE(:toDate, 'YYYY-MM-DD') ");
        }

        // Build filter conditions
        List<String> filterConditions = new java.util.ArrayList<>();
        List<GetAllPdaFilterRequest.FilterItem> validFilters = new java.util.ArrayList<>();
        if (filterRequest.getFilters() != null && !filterRequest.getFilters().isEmpty()) {
            for (GetAllPdaFilterRequest.FilterItem filter : filterRequest.getFilters()) {
                if (org.springframework.util.StringUtils.hasText(filter.getSearchField()) && org.springframework.util.StringUtils.hasText(filter.getSearchValue())) {
                    validFilters.add(filter);
                    String columnName = mapPdaSearchFieldToColumn(filter.getSearchField());
                    int paramIndex = validFilters.size() - 1;
                    filterConditions.add("LOWER(" + columnName + ") LIKE LOWER(:filterValue" + paramIndex + ")");
                }
            }
        }

        // Add filter conditions with operator
        if (!filterConditions.isEmpty()) {
            String operator = "AND".equalsIgnoreCase(filterRequest.getOperator()) ? " AND " : " OR ";
            sqlBuilder.append("AND (").append(String.join(operator, filterConditions)).append(") ");
        }

        // Apply sorting
        String orderBy = "ORDER BY p.TRANSACTION_DATE DESC";
        if (org.springframework.util.StringUtils.hasText(sort)) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String sortField = mapPdaSortFieldToColumn(sortParts[0].trim());
                String sortDirection = sortParts[1].trim().toUpperCase();
                if ("ASC".equals(sortDirection) || "DESC".equals(sortDirection)) {
                    orderBy = "ORDER BY " + sortField + " " + sortDirection + " NULLS LAST";
                }
            }
        }
        sqlBuilder.append(orderBy);

        // Create count query
        String countSql = "SELECT COUNT(*) FROM (" + sqlBuilder.toString() + ")";

        // Create query
        jakarta.persistence.Query query = entityManager.createNativeQuery(sqlBuilder.toString());
        jakarta.persistence.Query countQuery = entityManager.createNativeQuery(countSql);

        // Set parameters
        query.setParameter("groupPoid", groupPoid);
        query.setParameter("companyPoid", companyPoid);
        countQuery.setParameter("groupPoid", groupPoid);
        countQuery.setParameter("companyPoid", companyPoid);

        if (org.springframework.util.StringUtils.hasText(filterRequest.getFrom())) {
            query.setParameter("fromDate", filterRequest.getFrom());
            countQuery.setParameter("fromDate", filterRequest.getFrom());
        }
        if (org.springframework.util.StringUtils.hasText(filterRequest.getTo())) {
            query.setParameter("toDate", filterRequest.getTo());
            countQuery.setParameter("toDate", filterRequest.getTo());
        }

        // Set filter parameters
        if (!validFilters.isEmpty()) {
            for (int i = 0; i < validFilters.size(); i++) {
                GetAllPdaFilterRequest.FilterItem filter = validFilters.get(i);
                String paramValue = "%" + filter.getSearchValue() + "%";
                query.setParameter("filterValue" + i, paramValue);
                countQuery.setParameter("filterValue" + i, paramValue);
            }
        }

        // Get total count
        Long totalCount = ((Number) countQuery.getSingleResult()).longValue();

        // Apply pagination
        int offset = page * size;
        query.setFirstResult(offset);
        query.setMaxResults(size);

        // Execute query and map results
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<PdaEntryListResponse> dtos = results.stream()
                .map(this::mapToPdaListResponseDto)
                .collect(Collectors.toList());

        for (PdaEntryListResponse dto : dtos) {
            dto.setVesselName(lovService.getLovItemByPoid(dto.getVesselPoid() != null ? dto.getVesselPoid().longValue() : null, "VESSEL_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null).getLabel());
            dto.setPrincipalName(lovService.getLovItemByPoid(dto.getPrincipalPoid() != null ? dto.getPrincipalPoid().longValue() : null, "PRINCIPAL_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null).getLabel());
        }

        // Create page
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, totalCount);
    }

    private void setLovDetails(PdaEntryResponse response) {
        response.setPrincipalDet(lovService.getLovItemByPoid(response.getPrincipalPoid() != null ? response.getPrincipalPoid().longValue() : null, "PRINCIPAL_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        response.setVoyageDet(lovService.getLovItemByPoid(response.getVoyagePoid() != null ? response.getVoyagePoid().longValue() : null, "VESSAL_VOYAGE", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        response.setVesselDet(lovService.getLovItemByPoid(response.getVesselPoid() != null ? response.getVesselPoid().longValue() : null, "VESSEL_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        response.setVesselTypeDet(lovService.getLovItemByPoid(response.getVesselTypePoid() != null ? response.getVesselTypePoid().longValue() : null, "VESSEL_TYPE_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        response.setPortDet(lovService.getLovItemByPoid(response.getPortPoid() != null ? response.getPortPoid().longValue() : null, "PDA_PORT_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        response.setLineDet(lovService.getLovItemByPoid(response.getLinePoid() != null ? response.getLinePoid().longValue() : null, "LINE_MASTER_ALL", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        response.setComodityDet(lovService.getLovItemByPoid(response.getComodityPoid() != null ? Long.valueOf(response.getComodityPoid()) : null, "COMODITY", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        response.setOperationTypeDet(lovService.getLovItemByCode(response.getOperationType(), "PDA_OPERATION_TYPES", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        response.setUnitDet(lovService.getLovItemByCode(response.getUnit(), "UNIT_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        response.setCurrencyDet(lovService.getLovItemByCode(response.getCurrencyCode(), "CURRENCY", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        response.setSalesmanDet(lovService.getLovItemByPoid(response.getSalesmanPoid() != null ? response.getSalesmanPoid().longValue() : null, "SALESMAN", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        response.setRefTypeDet(lovService.getLovItemByCode(response.getRefType(), "PDA_REF_TYPE", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        response.setSubCategoryDet(lovService.getLovItemByCode(response.getSubCategory(), "PDA_SUB_CATEGORY", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        response.setVesselHandledByDet(lovService.getLovItemByPoid(response.getVesselHandledBy() != null ? response.getVesselHandledBy().longValue() : null, "PDA_USER_MASTER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        response.setPrintPrincipalDet(lovService.getLovItemByPoid(response.getPrintPrincipal() != null ? response.getPrintPrincipal().longValue() : null, "PDA_PRINCIPAL_PRINT", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        response.setNominatedPartyTypeDet(lovService.getLovItemByCode(response.getNominatedPartyType(), "PDA_NOMINATED_PARTY_TYPE", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        response.setBankDet(lovService.getLovItemByPoid(response.getBankPoid() != null ? response.getBankPoid().longValue() : null, "BANK_MASTER_COMPANYWISE", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null));
        if (StringUtils.isNotBlank(response.getNominatedPartyType()) && "CUSTOMER".equalsIgnoreCase(response.getNominatedPartyType())) {
            response.setNominatedPartyDet(lovService.getLovItemByPoid(Long.valueOf(String.valueOf(response.getNominatedPartyPoid())), "PDA_NOMINATED_PARTY_CUSTOMER", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        }
        if (StringUtils.isNotBlank(response.getNominatedPartyType()) && "PRINCIPAL".equalsIgnoreCase(response.getNominatedPartyType())) {
            response.setNominatedPartyDet(lovService.getLovItemByPoid(Long.valueOf(String.valueOf(response.getNominatedPartyPoid())), "PDA_NOMINATED_PARTY_PRINCIPAL", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()));
        }
    }

    private String mapPdaSearchFieldToColumn(String searchField) {
        if (searchField == null) {
            return null;
        }
        String normalizedField = searchField.toUpperCase().replace("_", "");

        switch (normalizedField) {
            case "DOCREF":
                return "p.DOC_REF";
            case "TRANSACTIONREF":
                return "p.TRANSACTION_REF";
            case "STATUS":
                return "p.STATUS";
            case "REFTYPE":
                return "p.REF_TYPE";
            case "SUBCATEGORY":
                return "p.SUB_CATEGORY";
            case "PRINCIPALPOID":
                return "p.PRINCIPAL_POID";
            case "PRINCIPALNAME":
                return "p.PRINCIPAL_NAME";
            case "PRINCIPALCONTACT":
                return "p.PRINCIPAL_CONTACT";
            case "VESSELPOID":
                return "p.VESSEL_POID";
            case "VESSELTYPEPOID":
                return "p.VESSEL_TYPE_POID";
            case "IMONUMBER":
                return "p.IMO_NUMBER";
            case "PORTPOID":
                return "p.PORT_POID";
            case "PORTDESCRIPTION":
                return "p.PORT_DESCRIPTION";
            case "LINEPOID":
                return "p.LINE_POID";
            case "VOYAGENO":
                return "p.VOYAGE_NO";
            case "VOYAGEPOID":
                return "p.VOYAGE_POID";
            case "COMODITYPOID":
                return "p.COMODITY_POID";
            case "OPERATIONTYPE":
                return "p.OPERATION_TYPE";
            case "HARBOURCALLTYPE":
                return "p.HARBOUR_CALL_TYPE";
            case "UNIT":
                return "p.UNIT";
            case "CURRENCYCODE":
                return "p.CURRENCY_CODE";
            case "COSTCENTREPOID":
                return "p.COST_CENTRE_POID";
            case "SALESMANPOID":
                return "p.SALESMAN_POID";
            case "TERMSPOID":
                return "p.TERMS_POID";
            case "ADDRESSPOID":
                return "p.ADDRESS_POID";
            case "CARGODETAILS":
                return "p.CARGO_DETAILS";
            case "REMARKS":
                return "p.REMARKS";
            case "VESSELVERIFIED":
                return "p.VESSEL_VERIFIED";
            case "VESSELVERIFIEDBY":
                return "p.VESSEL_VERIFIED_BY";
            case "VESSELHANDLEDBY":
                return "p.VESSEL_HANDLED_BY";
            case "URGENTAPPROVAL":
                return "p.URGENT_APPROVAL";
            case "PRINCIPALAPPROVED":
                return "p.PRINCIPAL_APPROVED";
            case "PRINCIPALAPPROVEDBY":
                return "p.PRINCIPAL_APPROVED_BY";
            case "PRINTPRINCIPAL":
                return "p.PRINT_PRINCIPAL";
            case "FDAREF":
                return "p.FDA_REF";
            case "FDAPOID":
                return "p.FDA_POID";
            case "MULTIPLEFDA":
                return "p.MULTIPLE_FDA";
            case "NOMINATEDPARTYTYPE":
                return "p.NOMINATED_PARTY_TYPE";
            case "NOMINATEDPARTYPOID":
                return "p.NOMINATED_PARTY_POID";
            case "BANKPOID":
                return "p.BANK_POID";
            case "BUSINESSREFBY":
                return "p.BUSINESS_REF_BY";
            case "PMIDOCUMENT":
                return "p.PMI_DOCUMENT";
            case "CANCELREMARK":
                return "p.CANCEL_REMARK";
            case "OLDPORTCODE":
                return "p.OLD_PORT_CODE";
            case "OLDVESSELCODE":
                return "p.OLD_VESSEL_CODE";
            case "OLDPRINCIPALCODE":
                return "p.OLD_PRINCIPAL_CODE";
            case "OLDVOYAGEJOB":
                return "p.OLD_VOYAGE_JOB";
            case "MENASDUES":
                return "p.MENAS_DUES";
            case "DOCUMENTSUBMITTEDBY":
                return "p.DOCUMENT_SUBMITTED_BY";
            case "DOCUMENTSUBMITTEDSTATUS":
                return "p.DOCUMENT_SUBMITTED_STATUS";
            case "DOCUMENTRECEIVEDFROM":
                return "p.DOCUMENT_RECEIVED_FROM";
            case "DOCUMENTRECEIVEDSTATUS":
                return "p.DOCUMENT_RECEIVED_STATUS";
            case "SUBMISSIONACCEPTEDBY":
                return "p.SUBMISSION_ACCEPTED_BY";
            case "VERIFICATIONACCEPTEDBY":
                return "p.VERIFICATION_ACCEPTED_BY";
            case "ACCTSCORRECTIONREMARKS":
                return "p.ACCTS_CORRECTION_REMARKS";
            case "CREATEDBY":
                return "p.CREATED_BY";
            case "LASTMODIFIEDBY":
                return "p.LASTMODIFIED_BY";
            case "DELETED":
                return "p.DELETED";
            default:
                String columnName = searchField.toUpperCase().replace(" ", "_");
                return "p." + columnName;
        }
    }

    private String mapPdaSortFieldToColumn(String sortField) {
        if (sortField == null) {
            return "p.TRANSACTION_DATE";
        }
        String normalizedField = sortField.toUpperCase().replace("_", "");

        switch (normalizedField) {
            case "TRANSACTIONPOID":
                return "p.TRANSACTION_POID";
            case "TRANSACTIONDATE":
                return "p.TRANSACTION_DATE";
            case "GROUPPOID":
                return "p.GROUP_POID";
            case "COMPANYPOID":
                return "p.COMPANY_POID";
            case "DOCREF":
                return "p.DOC_REF";
            case "TRANSACTIONREF":
                return "p.TRANSACTION_REF";
            case "PRINCIPALPOID":
                return "p.PRINCIPAL_POID";
            case "PRINCIPALNAME":
                return "p.PRINCIPAL_NAME";
            case "PRINCIPALCONTACT":
                return "p.PRINCIPAL_CONTACT";
            case "VOYAGEPOID":
                return "p.VOYAGE_POID";
            case "VOYAGENO":
                return "p.VOYAGE_NO";
            case "VESSELPOID":
                return "p.VESSEL_POID";
            case "VESSELTYPEPOID":
                return "p.VESSEL_TYPE_POID";
            case "GRT":
                return "p.GRT";
            case "NRT":
                return "p.NRT";
            case "DWT":
                return "p.DWT";
            case "IMONUMBER":
                return "p.IMO_NUMBER";
            case "ARRIVALDATE":
                return "p.ARRIVAL_DATE";
            case "SAILDATE":
                return "p.SAIL_DATE";
            case "ACTUALARRIVALDATE":
                return "p.ACTUAL_ARRIVAL_DATE";
            case "ACTUALSAILDATE":
                return "p.ACTUAL_SAIL_DATE";
            case "VESSELSAILDATE":
                return "p.VESSEL_SAIL_DATE";
            case "PORTPOID":
                return "p.PORT_POID";
            case "PORTDESCRIPTION":
                return "p.PORT_DESCRIPTION";
            case "LINEPOID":
                return "p.LINE_POID";
            case "COMODITYPOID":
                return "p.COMODITY_POID";
            case "OPERATIONTYPE":
                return "p.OPERATION_TYPE";
            case "HARBOURCALLTYPE":
                return "p.HARBOUR_CALL_TYPE";
            case "IMPORTQTY":
                return "p.IMPORT_QTY";
            case "EXPORTQTY":
                return "p.EXPORT_QTY";
            case "TRANSHIPMENTQTY":
                return "p.TRANSHIPMENT_QTY";
            case "TOTALQUANTITY":
                return "p.TOTAL_QUANTITY";
            case "UNIT":
                return "p.UNIT";
            case "NUMBEROFDAYS":
                return "p.NUMBER_OF_DAYS";
            case "CURRENCYCODE":
                return "p.CURRENCY_CODE";
            case "CURRENCYRATE":
                return "p.CURRENCY_RATE";
            case "TOTALAMOUNT":
                return "p.TOTAL_AMOUNT";
            case "COSTCENTREPOID":
                return "p.COST_CENTRE_POID";
            case "SALESMANPOID":
                return "p.SALESMAN_POID";
            case "TERMSPOID":
                return "p.TERMS_POID";
            case "ADDRESSPOID":
                return "p.ADDRESS_POID";
            case "REFTYPE":
                return "p.REF_TYPE";
            case "SUBCATEGORY":
                return "p.SUB_CATEGORY";
            case "STATUS":
                return "p.STATUS";
            case "CARGODETAILS":
                return "p.CARGO_DETAILS";
            case "REMARKS":
                return "p.REMARKS";
            case "VESSELVERIFIED":
                return "p.VESSEL_VERIFIED";
            case "VESSELVERIFIEDDATE":
                return "p.VESSEL_VERIFIED_DATE";
            case "VESSELVERIFIEDBY":
                return "p.VESSEL_VERIFIED_BY";
            case "VESSELHANDLEDBY":
                return "p.VESSEL_HANDLED_BY";
            case "URGENTAPPROVAL":
                return "p.URGENT_APPROVAL";
            case "PRINCIPALAPPROVED":
                return "p.PRINCIPAL_APPROVED";
            case "PRINCIPALAPPROVEDDATE":
                return "p.PRINCIPAL_APPROVED_DATE";
            case "PRINCIPALAPPROVEDBY":
                return "p.PRINCIPAL_APPROVED_BY";
            case "PRINCIPALAPRVLDAYS":
                return "p.PRINCIPAL_APRVL_DAYS";
            case "REMINDERMINUTES":
                return "p.REMINDER_MINUTES";
            case "PRINTPRINCIPAL":
                return "p.PRINT_PRINCIPAL";
            case "FDAREF":
                return "p.FDA_REF";
            case "FDAPOID":
                return "p.FDA_POID";
            case "MULTIPLEFDA":
                return "p.MULTIPLE_FDA";
            case "NOMINATEDPARTYTYPE":
                return "p.NOMINATED_PARTY_TYPE";
            case "NOMINATEDPARTYPOID":
                return "p.NOMINATED_PARTY_POID";
            case "BANKPOID":
                return "p.BANK_POID";
            case "BUSINESSREFBY":
                return "p.BUSINESS_REF_BY";
            case "PMIDOCUMENT":
                return "p.PMI_DOCUMENT";
            case "CANCELREMARK":
                return "p.CANCEL_REMARK";
            case "OLDPORTCODE":
                return "p.OLD_PORT_CODE";
            case "OLDVESSELCODE":
                return "p.OLD_VESSEL_CODE";
            case "OLDPRINCIPALCODE":
                return "p.OLD_PRINCIPAL_CODE";
            case "OLDVOYAGEJOB":
                return "p.OLD_VOYAGE_JOB";
            case "MENASDUES":
                return "p.MENAS_DUES";
            case "DOCUMENTSUBMITTEDDATE":
                return "p.DOCUMENT_SUBMITTED_DATE";
            case "DOCUMENTSUBMITTEDBY":
                return "p.DOCUMENT_SUBMITTED_BY";
            case "DOCUMENTSUBMITTEDSTATUS":
                return "p.DOCUMENT_SUBMITTED_STATUS";
            case "DOCUMENTRECEIVEDDATE":
                return "p.DOCUMENT_RECEIVED_DATE";
            case "DOCUMENTRECEIVEDFROM":
                return "p.DOCUMENT_RECEIVED_FROM";
            case "DOCUMENTRECEIVEDSTATUS":
                return "p.DOCUMENT_RECEIVED_STATUS";
            case "SUBMISSIONACCEPTEDDATE":
                return "p.SUBMISSION_ACCEPTED_DATE";
            case "SUBMISSIONACCEPTEDBY":
                return "p.SUBMISSION_ACCEPTED_BY";
            case "VERIFICATIONACCEPTEDDATE":
                return "p.VERIFICATION_ACCEPTED_DATE";
            case "VERIFICATIONACCEPTEDBY":
                return "p.VERIFICATION_ACCEPTED_BY";
            case "ACCTSCORRECTIONREMARKS":
                return "p.ACCTS_CORRECTION_REMARKS";
            case "ACCTSRETURNEDDATE":
                return "p.ACCTS_RETURNED_DATE";
            case "DELETED":
                return "p.DELETED";
            case "CREATEDBY":
                return "p.CREATED_BY";
            case "CREATEDDATE":
                return "p.CREATED_DATE";
            case "LASTMODIFIEDBY":
                return "p.LASTMODIFIED_BY";
            case "LASTMODIFIEDDATE":
                return "p.LASTMODIFIED_DATE";
            default:
                String columnName = sortField.toUpperCase().replace(" ", "_");
                return "p." + columnName;
        }
    }

    private String convertToString(Object value) {
        return value != null ? value.toString() : null;
    }

    private PdaEntryListResponse mapToPdaListResponseDto(Object[] row) {
        PdaEntryListResponse dto = new PdaEntryListResponse();

        dto.setTransactionPoid(row[0] != null ? ((Number) row[0]).longValue() : null);
        dto.setTransactionDate(row[1] != null ? ((java.sql.Timestamp) row[1]).toLocalDateTime().toLocalDate() : null);
        dto.setDocRef(convertToString(row[4]));
        dto.setTransactionRef(convertToString(row[5]));
        dto.setPrincipalPoid(row[6] != null ? (java.math.BigDecimal) row[6] : null);
        dto.setPrincipalName(convertToString(row[7]));
        dto.setVoyagePoid(row[9] != null ? (java.math.BigDecimal) row[9] : null);
        dto.setVoyageNo(convertToString(row[10]));
        dto.setVesselPoid(row[11] != null ? (java.math.BigDecimal) row[11] : null);
        dto.setPortPoid(row[22] != null ? (java.math.BigDecimal) row[22] : null);
        dto.setPortDescription(convertToString(row[23]));
        dto.setStatus(convertToString(row[43]));
        dto.setRefType(convertToString(row[41]));
        dto.setFdaRef(convertToString(row[57]));
        dto.setTotalAmount(row[36] != null ? (java.math.BigDecimal) row[36] : null);
        dto.setCurrencyCode(convertToString(row[34]));
        dto.setDeleted(convertToString(row[83]));
        dto.setCreatedBy(convertToString(row[84]));
        dto.setCreatedDate(row[85] != null ? ((java.sql.Timestamp) row[85]).toLocalDateTime() : null);
        dto.setLastModifiedBy(convertToString(row[86]));
        dto.setLastModifiedDate(row[87] != null ? ((java.sql.Timestamp) row[87]).toLocalDateTime() : null);

        return dto;
    }

    private PdaEntryResponse mapToPdaResponseDto(Object[] row) {
        PdaEntryResponse dto = new PdaEntryResponse();

        dto.setTransactionPoid(row[0] != null ? ((Number) row[0]).longValue() : null);
        dto.setTransactionDate(row[1] != null ? ((java.sql.Timestamp) row[1]).toLocalDateTime().toLocalDate() : null);
        // Skip GROUP_POID and COMPANY_POID as they're not in DTO
        dto.setDocRef(convertToString(row[4]));
        dto.setTransactionRef(convertToString(row[5]));
        dto.setPrincipalPoid(row[6] != null ? (java.math.BigDecimal) row[6] : null);
        dto.setPrincipalName(convertToString(row[7]));
        dto.setPrincipalContact(convertToString(row[8]));
        dto.setVoyagePoid(row[9] != null ? (java.math.BigDecimal) row[9] : null);
        dto.setVoyageNo(convertToString(row[10]));
        dto.setVesselPoid(row[11] != null ? (java.math.BigDecimal) row[11] : null);
        dto.setVesselTypePoid(row[12] != null ? (java.math.BigDecimal) row[12] : null);
        dto.setGrt(row[13] != null ? (java.math.BigDecimal) row[13] : null);
        dto.setNrt(row[14] != null ? (java.math.BigDecimal) row[14] : null);
        dto.setDwt(row[15] != null ? (java.math.BigDecimal) row[15] : null);
        dto.setImoNumber(convertToString(row[16]));
        dto.setArrivalDate(row[17] != null ? ((java.sql.Timestamp) row[17]).toLocalDateTime().toLocalDate() : null);
        dto.setSailDate(row[18] != null ? ((java.sql.Timestamp) row[18]).toLocalDateTime().toLocalDate() : null);
        dto.setActualArrivalDate(row[19] != null ? ((java.sql.Timestamp) row[19]).toLocalDateTime().toLocalDate() : null);
        dto.setActualSailDate(row[20] != null ? ((java.sql.Timestamp) row[20]).toLocalDateTime().toLocalDate() : null);
        dto.setVesselSailDate(row[21] != null ? ((java.sql.Timestamp) row[21]).toLocalDateTime().toLocalDate() : null);
        dto.setPortPoid(row[22] != null ? (java.math.BigDecimal) row[22] : null);
        dto.setPortDescription(convertToString(row[23]));
        dto.setLinePoid(row[24] != null ? (java.math.BigDecimal) row[24] : null);
        dto.setComodityPoid(convertToString(row[25]));
        dto.setOperationType(convertToString(row[26]));
        dto.setHarbourCallType(convertToString(row[27]));
        dto.setImportQty(row[28] != null ? (java.math.BigDecimal) row[28] : null);
        dto.setExportQty(row[29] != null ? (java.math.BigDecimal) row[29] : null);
        dto.setTranshipmentQty(row[30] != null ? (java.math.BigDecimal) row[30] : null);
        dto.setTotalQuantity(row[31] != null ? (java.math.BigDecimal) row[31] : null);
        dto.setUnit(convertToString(row[32]));
        dto.setNumberOfDays(row[33] != null ? (java.math.BigDecimal) row[33] : null);
        dto.setCurrencyCode(convertToString(row[34]));
        dto.setCurrencyRate(row[35] != null ? (java.math.BigDecimal) row[35] : null);
        dto.setTotalAmount(row[36] != null ? (java.math.BigDecimal) row[36] : null);
        dto.setCostCentrePoid(row[37] != null ? (java.math.BigDecimal) row[37] : null);
        dto.setSalesmanPoid(row[38] != null ? (java.math.BigDecimal) row[38] : null);
        dto.setTermsPoid(row[39] != null ? (java.math.BigDecimal) row[39] : null);
        dto.setAddressPoid(row[40] != null ? (java.math.BigDecimal) row[40] : null);
        dto.setRefType(convertToString(row[41]));
        dto.setSubCategory(convertToString(row[42]));
        dto.setStatus(convertToString(row[43]));
        dto.setCargoDetails(convertToString(row[44]));
        dto.setRemarks(convertToString(row[45]));
        dto.setVesselVerified(convertToString(row[46]));
        dto.setVesselVerifiedDate(row[47] != null ? ((java.sql.Timestamp) row[47]).toLocalDateTime().toLocalDate() : null);
        dto.setVesselVerifiedBy(convertToString(row[48]));
        dto.setVesselHandledBy(row[49] != null ? (java.math.BigDecimal) row[49] : null);
        dto.setUrgentApproval(convertToString(row[50]));
        dto.setPrincipalApproved(convertToString(row[51]));
        dto.setPrincipalApprovedDate(row[52] != null ? ((java.sql.Timestamp) row[52]).toLocalDateTime().toLocalDate() : null);
        dto.setPrincipalApprovedBy(convertToString(row[53]));
        dto.setPrincipalAprvlDays(row[54] != null ? (java.math.BigDecimal) row[54] : null);
        dto.setReminderMinutes(row[55] != null ? (java.math.BigDecimal) row[55] : null);
        dto.setPrintPrincipal(row[56] != null ? (java.math.BigDecimal) row[56] : null);
        dto.setFdaRef(convertToString(row[57]));
        dto.setFdaPoid(row[58] != null ? (java.math.BigDecimal) row[58] : null);
        dto.setMultipleFda(convertToString(row[59]));
        dto.setNominatedPartyType(convertToString(row[60]));
        dto.setNominatedPartyPoid(row[61] != null ? (java.math.BigDecimal) row[61] : null);
        dto.setBankPoid(row[62] != null ? (java.math.BigDecimal) row[62] : null);
        dto.setBusinessRefBy(convertToString(row[63]));
        dto.setPmiDocument(convertToString(row[64]));
        dto.setCancelRemark(convertToString(row[65]));
        // Skip old codes (row[66-69]) as they're not in DTO
        dto.setMenasDues(convertToString(row[70]));
        dto.setDocumentSubmittedDate(row[71] != null ? ((java.sql.Timestamp) row[71]).toLocalDateTime().toLocalDate() : null);
        dto.setDocumentSubmittedBy(convertToString(row[72]));
        dto.setDocumentSubmittedStatus(convertToString(row[73]));
        dto.setDocumentReceivedDate(row[74] != null ? ((java.sql.Timestamp) row[74]).toLocalDateTime().toLocalDate() : null);
        dto.setDocumentReceivedFrom(convertToString(row[75]));
        dto.setDocumentReceivedStatus(convertToString(row[76]));
        dto.setSubmissionAcceptedDate(row[77] != null ? ((java.sql.Timestamp) row[77]).toLocalDateTime().toLocalDate() : null);
        dto.setSubmissionAcceptedBy(convertToString(row[78]));
        dto.setVerificationAcceptedDate(row[79] != null ? ((java.sql.Timestamp) row[79]).toLocalDateTime().toLocalDate() : null);
        dto.setVerificationAcceptedBy(convertToString(row[80]));
        dto.setAcctsCorrectionRemarks(convertToString(row[81]));
        dto.setAcctsReturnedDate(row[82] != null ? ((java.sql.Timestamp) row[82]).toLocalDateTime().toLocalDate() : null);
        dto.setDeleted(convertToString(row[83]));
        dto.setCreatedBy(convertToString(row[84]));
        dto.setCreatedDate(row[85] != null ? ((java.sql.Timestamp) row[85]).toLocalDateTime() : null);
        dto.setLastModifiedBy(convertToString(row[86]));
        dto.setLastModifiedDate(row[87] != null ? ((java.sql.Timestamp) row[87]).toLocalDateTime() : null);

        return dto;
    }

    // Inner class for tax information
    private static class TaxInfo {
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
}

