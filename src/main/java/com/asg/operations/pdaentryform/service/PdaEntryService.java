package com.asg.operations.pdaentryform.service;

import com.asg.operations.pdaentryform.dto.*;
import com.asg.operations.pdaporttariffmaster.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for PDA Entry operations
 */
public interface PdaEntryService {

    /**
     * Get single PDA entry by transaction POID with all related details
     */
    PdaEntryResponse getPdaEntryById(Long transactionPoid, Long groupPoid, Long companyPoid);

    /**
     * Create new PDA entry
     */
    PdaEntryResponse createPdaEntry(PdaEntryRequest request, Long groupPoid, Long companyPoid, Long userId);

    /**
     * Update existing PDA entry
     */
    PdaEntryResponse updatePdaEntry(Long transactionPoid, PdaEntryRequest request, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Soft delete PDA entry
     */
    void deletePdaEntry(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Get charge details for a PDA entry
     */
    List<PdaEntryChargeDetailResponse> getChargeDetails(Long transactionPoid, Long groupPoid, Long companyPoid);

    /**
     * Bulk save charge details (create, update, delete)
     */
    List<PdaEntryChargeDetailResponse> bulkSaveChargeDetails(Long transactionPoid, BulkSaveChargeDetailsRequest request, Long groupPoid, Long companyPoid, String userId);

    /**
     * Delete single charge detail
     */
    void deleteChargeDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid, String userId);

    /**
     * Clear all charge details
     */
    void clearChargeDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Recalculate all charge details
     */
    List<PdaEntryChargeDetailResponse> recalculateChargeDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Load default charges
     */
    List<PdaEntryChargeDetailResponse> loadDefaultCharges(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Get vehicle details for a PDA entry
     */
    List<PdaEntryVehicleDetailResponse> getVehicleDetails(Long transactionPoid, Long groupPoid, Long companyPoid);

    /**
     * Bulk save vehicle details (create, update, delete)
     */
    List<PdaEntryVehicleDetailResponse> bulkSaveVehicleDetails(Long transactionPoid, BulkSaveVehicleDetailsRequest request, Long groupPoid, Long companyPoid, String userId);

    /**
     * Import vehicle details
     */
    void importVehicleDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userId);

    /**
     * Clear vehicle details
     */
    void clearVehicleDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Publish vehicle details for import
     */
    void publishVehicleDetailsForImport(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Get TDR details for a PDA entry
     */
    List<PdaEntryTdrDetailResponse> getTdrDetails(Long transactionPoid, Long groupPoid, Long companyPoid);

    /**
     * Bulk save TDR details (create, update, delete)
     */
    List<PdaEntryTdrDetailResponse> bulkSaveTdrDetails(Long transactionPoid, BulkSaveTdrDetailsRequest request, Long groupPoid, Long companyPoid, String userId);

    /**
     * Get acknowledgment details for a PDA entry
     */
    List<PdaEntryAcknowledgmentDetailResponse> getAcknowledgmentDetails(Long transactionPoid, Long groupPoid, Long companyPoid);

    /**
     * Bulk save acknowledgment details (create, update, delete)
     */
    List<PdaEntryAcknowledgmentDetailResponse> bulkSaveAcknowledgmentDetails(Long transactionPoid, BulkSaveAcknowledgmentDetailsRequest request, Long groupPoid, Long companyPoid, String userId);

    /**
     * Validate PDA entry before save
     */
    ValidationResponse validateBeforeSave(Long transactionPoid, PdaEntryRequest request, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Validate PDA entry after save
     */
    ValidationResponse validateAfterSave(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Get vessel details (auto-population from LOV change)
     */
    VesselDetailsResponse getVesselDetails(BigDecimal vesselPoid, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Create FDA from PDA entry
     */
    String createFda(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Upload acknowledgment details
     */
    void uploadAcknowledgmentDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Clear acknowledgment details
     */
    void clearAcknowledgmentDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Get all PDA entries with filters
     */
    org.springframework.data.domain.Page<PdaEntryListResponse> getAllPdaWithFilters(Long groupPoid, Long companyPoid, GetAllPdaFilterRequest filterRequest, int page, int size, String sort);

    /**
     * Get FDA document info for viewing
     */
    FdaDocumentViewResponse getFdaDocumentInfo(Long transactionPoid, Long groupPoid, Long companyPoid);

    /**
     * Accept FDA documents
     */
    void acceptFdaDocuments(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Cancel PDA entry
     */
    String cancelPdaEntry(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, String cancelRemark);

    /**
     * Get submission log info
     */
    SubmissionLogResponse getSubmissionLogInfo(Long transactionPoid, Long groupPoid, Long companyPoid);

    /**
     * Reject FDA documents
     */
    void rejectFdaDocs(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, String correctionRemarks);

    /**
     * Submit PDA to FDA
     */
    void submitPdaToFda(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Upload acknowledgment details from Excel file
     */
    String uploadAcknowledgmentDetailsFromExcel(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, org.springframework.web.multipart.MultipartFile file);

    /**
     * Import TDR file with transaction
     */
    String importTdrFileWithTransaction(org.springframework.web.multipart.MultipartFile file, Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Upload TDR details
     */
    String uploadTdrDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid, org.springframework.web.multipart.MultipartFile file);

    /**
     * Clear TDR details
     */
    String clearTdrDetails(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

    /**
     * Process TDR charges
     */
    String processTdrCharges(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid);

}

