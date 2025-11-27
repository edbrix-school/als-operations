package com.alsharif.operations.pdaentryform.service;

import com.alsharif.operations.pdaentryform.dto.*;
import com.alsharif.operations.pdaporttariffmaster.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for PDA Entry operations
 */
public interface PdaEntryService {

    /**
     * Get paginated list of PDA entries with filters
     */
    PageResponse<PdaEntryResponse> getPdaEntryList(
            String docRef,
            String transactionRef,
            BigDecimal principalPoid,
            String status,
            String refType,
            BigDecimal vesselPoid,
            BigDecimal portPoid,
            LocalDate transactionDateFrom,
            LocalDate transactionDateTo,
            String deleted,
            Pageable pageable,
            BigDecimal groupPoid,
            BigDecimal companyPoid
    );

    /**
     * Get single PDA entry by transaction POID with all related details
     */
    PdaEntryResponse getPdaEntryById(Long transactionPoid, BigDecimal groupPoid, BigDecimal companyPoid);

    /**
     * Create new PDA entry
     */
    PdaEntryResponse createPdaEntry(PdaEntryRequest request, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Update existing PDA entry
     */
    PdaEntryResponse updatePdaEntry(Long transactionPoid, PdaEntryRequest request, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Soft delete PDA entry
     */
    void deletePdaEntry(Long transactionPoid, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Get charge details for a PDA entry
     */
    List<PdaEntryChargeDetailResponse> getChargeDetails(Long transactionPoid, BigDecimal groupPoid, BigDecimal companyPoid);

    /**
     * Bulk save charge details (create, update, delete)
     */
    List<PdaEntryChargeDetailResponse> bulkSaveChargeDetails(Long transactionPoid, BulkSaveChargeDetailsRequest request, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Delete single charge detail
     */
    void deleteChargeDetail(Long transactionPoid, Long detRowId, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Clear all charge details
     */
    void clearChargeDetails(Long transactionPoid, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Recalculate all charge details
     */
    List<PdaEntryChargeDetailResponse> recalculateChargeDetails(Long transactionPoid, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Load default charges
     */
    List<PdaEntryChargeDetailResponse> loadDefaultCharges(Long transactionPoid, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Get vehicle details for a PDA entry
     */
    List<PdaEntryVehicleDetailResponse> getVehicleDetails(Long transactionPoid, BigDecimal groupPoid, BigDecimal companyPoid);

    /**
     * Bulk save vehicle details (create, update, delete)
     */
    List<PdaEntryVehicleDetailResponse> bulkSaveVehicleDetails(Long transactionPoid, BulkSaveVehicleDetailsRequest request, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Import vehicle details
     */
    void importVehicleDetails(Long transactionPoid, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Clear vehicle details
     */
    void clearVehicleDetails(Long transactionPoid, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Publish vehicle details for import
     */
    void publishVehicleDetailsForImport(Long transactionPoid, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Get TDR details for a PDA entry
     */
    List<PdaEntryTdrDetailResponse> getTdrDetails(Long transactionPoid, BigDecimal groupPoid, BigDecimal companyPoid);

    /**
     * Bulk save TDR details (create, update, delete)
     */
    List<PdaEntryTdrDetailResponse> bulkSaveTdrDetails(Long transactionPoid, BulkSaveTdrDetailsRequest request, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Get acknowledgment details for a PDA entry
     */
    List<PdaEntryAcknowledgmentDetailResponse> getAcknowledgmentDetails(Long transactionPoid, BigDecimal groupPoid, BigDecimal companyPoid);

    /**
     * Bulk save acknowledgment details (create, update, delete)
     */
    List<PdaEntryAcknowledgmentDetailResponse> bulkSaveAcknowledgmentDetails(Long transactionPoid, BulkSaveAcknowledgmentDetailsRequest request, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Validate PDA entry before save
     */
    ValidationResponse validateBeforeSave(Long transactionPoid, PdaEntryRequest request, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Validate PDA entry after save
     */
    ValidationResponse validateAfterSave(Long transactionPoid, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Get vessel details (auto-population from LOV change)
     */
    VesselDetailsResponse getVesselDetails(BigDecimal vesselPoid, BigDecimal groupPoid, BigDecimal companyPoid, String userId);

    /**
     * Create FDA from PDA entry
     */
    String createFda(Long transactionPoid, BigDecimal groupPoid, BigDecimal companyPoid, String userId);
}

