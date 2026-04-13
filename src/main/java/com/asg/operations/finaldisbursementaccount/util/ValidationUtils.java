package com.asg.operations.finaldisbursementaccount.util;

import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.operations.exceptions.CustomException;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.finaldisbursementaccount.dto.CreateFdaHeaderRequest;
import com.asg.operations.finaldisbursementaccount.dto.FdaChargeDto;
import com.asg.operations.finaldisbursementaccount.dto.UpdateFdaHeaderRequest;
import com.asg.operations.finaldisbursementaccount.entity.PdaFdaDtl;
import com.asg.operations.finaldisbursementaccount.entity.PdaFdaHdr;
import com.asg.operations.finaldisbursementaccount.key.PdaFdaDtlId;
import com.asg.operations.finaldisbursementaccount.repository.*;
import com.asg.operations.pdaporttariffmaster.repository.ShipPortMasterRepository;
import com.asg.operations.pdaporttariffmaster.repository.ShipVesselTypeMasterRepository;
import com.asg.operations.shipprincipal.repository.AddressMasterRepository;
import com.asg.operations.shipprincipal.repository.ShipPrincipalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidationUtils {

    private final PdaFdaHdrRepository pdaFdaHdrRepository;
    private final PdaFdaDtlRepository pdaFdaDtlRepository;
    private final ShipPrincipalRepository shipPrincipalRepository;
    private final ShipVoyageHdrRepository shipVoyageHdrRepository;
    private final ShipPortMasterRepository shipPortMasterRepository;
    private final ShipLineMasterRepository shipLineMasterRepository;
    private final ShipVesselMasterRepository shipVesselMasterRepository;
    private final SalesSalesmanMasterRepository salesSalesmanMasterRepository;
    private final ShipVesselTypeMasterRepository shipVesselTypeMasterRepository;
    private final AddressMasterRepository addressMasterRepository;
    private final TermsTemplateRepository termsTemplateRepository;
    private final GLBankMasterRepository glBankMasterRepository;
    private final CostCenterRepository costCenterRepository;
    private final LoggingService loggingService;
    private final GlobalValidationRepository globalValidationRepository;
    private final SalesCustomerMasterRepository customerMasterRepository;

    public void validateHeaderBeforeSave(CreateFdaHeaderRequest dto) {
        if (dto.getPrincipalPoid() != null && !shipPrincipalRepository.existsByPrincipalPoid(dto.getPrincipalPoid())) {
                throw new ResourceNotFoundException("Principal not found for: " + dto.getPrincipalPoid());
            }

        if (dto.getSalesmanPoid() != null && !salesSalesmanMasterRepository.existsBySalesmanPoid(dto.getSalesmanPoid())) {
                throw new ResourceNotFoundException("Salesman not found for: " + dto.getSalesmanPoid());
            }

        if (dto.getVoyagePoid() != null && !shipVoyageHdrRepository.existsByTransactionPoid(dto.getVoyagePoid())) {
                throw new ResourceNotFoundException("Voyage not found for: " + dto.getVoyagePoid());
            }

        if (dto.getPortPoid() != null && !shipPortMasterRepository.existsByIdPortPoid(BigDecimal.valueOf(dto.getPortPoid()))) {
                throw new ResourceNotFoundException("Port not found for: " + dto.getPortPoid());
            }

        if (dto.getLinePoid() != null && !shipLineMasterRepository.existsByLinePoid(dto.getLinePoid())) {
                throw new ResourceNotFoundException("Line not found for: " + dto.getLinePoid());
            }

        if (dto.getVesselPoid() != null && !shipVesselMasterRepository.existsByVesselPoid(dto.getVesselPoid())) {
                throw new ResourceNotFoundException("Vessel not found for: " + dto.getVesselPoid());
            }

        if (dto.getVesselTypePoid() != null && !shipVesselTypeMasterRepository.existsByVesselTypePoid(BigDecimal.valueOf(Long.parseLong(dto.getVesselTypePoid())))) {
                throw new ResourceNotFoundException("Vessel Type not found for: " + dto.getVesselTypePoid());
            }

        if (dto.getPrintBankPoid() != null && !glBankMasterRepository.existsByBankPoid(dto.getPrintBankPoid())) {
                throw new ResourceNotFoundException("Bank not found for: " + dto.getPrintBankPoid());
            }

        if (dto.getCostCentrePoid() != null && !costCenterRepository.existsByCostCenterPoid(dto.getCostCentrePoid())) {
                throw new ResourceNotFoundException("Cost Center not found for: " + dto.getCostCentrePoid());
            }

        if (dto.getAddressPoid() != null && !addressMasterRepository.existsByAddressMasterPoid(dto.getAddressPoid())) {
                throw new ResourceNotFoundException("Address not found for: " + dto.getAddressPoid());
            }

        if (dto.getTermsPoid() != null && !termsTemplateRepository.existsByTermsPoid(dto.getTermsPoid())) {
                throw new ResourceNotFoundException("Terms not found for: " + dto.getTermsPoid());
            }

        if (StringUtils.isNotBlank(dto.getNominatedPartyType())) {
            if (!"PRINCIPAL".equalsIgnoreCase(dto.getNominatedPartyType()) && !"CUSTOMER".equalsIgnoreCase(dto.getNominatedPartyType())) {
                throw new CustomException("Nominated Party Type should be either PRINCIPAL or CUSTOMER", 400);
            }
            if ("CUSTOMER".equalsIgnoreCase(dto.getNominatedPartyType()) && !customerMasterRepository.existsByCustomerPoid(dto.getNominatedPartyPoid())) {
                throw new ResourceNotFoundException("Customer not found for: " + dto.getNominatedPartyPoid());
            }
            if ("PRINCIPAL".equalsIgnoreCase(dto.getNominatedPartyType()) && !shipPrincipalRepository.existsByPrincipalPoid(dto.getNominatedPartyPoid())) {
                throw new ResourceNotFoundException("Principal not found for: " + dto.getNominatedPartyPoid());
            }
        }

        // Validate FDA_SUB_TYPE and PDA_REF relationship (mirrors trigger logic PDA_FDA_HDR_TRG)
        // The trigger logic:
        // - If FDA_SUB_TYPE = 'MAIN_FDA', generates DOC_REF using RTN_GLOBAL_SEQ_NO
        // - If FDA_SUB_TYPE != 'MAIN_FDA' (including null), tries to find MAIN_FDA with same PDA_REF
        // - Maximum 3 FDAs per PDA_REF (1 MAIN_FDA + 2 supplementary, trigger only handles _A, _B, _C)

        if (StringUtils.isNotBlank(dto.getFdaSubType()) && !"MAIN_FDA".equalsIgnoreCase(dto.getFdaSubType())) {

            if (dto.getPdaRef() == null) {
                throw new CustomException("PDA Reference is required when FDA Sub Type is not 'MAIN_FDA'. Supplementary FDAs must reference an existing MAIN_FDA.", 400);
            }

            boolean mainFdaExists = pdaFdaHdrRepository.existsMainFdaByPdaRef(dto.getPdaRef());
            if (!mainFdaExists) {
                throw new CustomException("No MAIN_FDA found for PDA Reference '" + dto.getPdaRef() + "'. Supplementary FDAs can only be created when a MAIN_FDA exists for the same PDA Reference.", 400);
            }

            // Validate maximum count: trigger only handles up to 3 FDAs per PDA_REF (1 MAIN_FDA + 2 supplementary)
            // The trigger sets DOC_REF suffix based on count: count=1 -> _A, count=2 -> _B, count=3 -> _C
            // If count > 3, trigger doesn't set DOC_REF, which would cause an error
            long existingCount = pdaFdaHdrRepository.countExistingFdasByPdaRef(dto.getPdaRef());

            if (existingCount >= 3) {
                throw new CustomException("Maximum limit reached for PDA Reference '" + dto.getPdaRef() + "'. Only 3 FDAs are allowed per PDA Reference (1 MAIN_FDA + 2 supplementary).", 400);
            }

        } else if ("MAIN_FDA".equalsIgnoreCase(dto.getFdaSubType()) && dto.getPdaRef() != null) {
            // If creating MAIN_FDA, validate that no MAIN_FDA already exists for this PDA_REF
            boolean mainFdaExists = pdaFdaHdrRepository.existsMainFdaByPdaRef(dto.getPdaRef());
            if (mainFdaExists) {
                throw new CustomException("A MAIN_FDA already exists for PDA Reference '" + dto.getPdaRef() + "'. Only one MAIN_FDA is allowed per PDA Reference.", 400);
            }
        }
    }

    public void validateHeaderBeforeUpdate(UpdateFdaHeaderRequest dto, PdaFdaHdr existingEntity) {
        if (dto.getPrincipalPoid() != null && !shipPrincipalRepository.existsByPrincipalPoid(dto.getPrincipalPoid())) {
                throw new ResourceNotFoundException("Principal not found for: " + dto.getPrincipalPoid());
            }

        if (dto.getSalesmanPoid() != null && !salesSalesmanMasterRepository.existsBySalesmanPoid(dto.getSalesmanPoid())) {
                throw new ResourceNotFoundException("Salesman not found for: " + dto.getSalesmanPoid());
            }

        if (dto.getPortPoid() != null && !shipPortMasterRepository.existsByIdPortPoid(BigDecimal.valueOf(dto.getPortPoid()))) {
                throw new ResourceNotFoundException("Port not found for: " + dto.getPortPoid());
            }


        // Validate fdaSubType changes (if being updated)
        // Note: The trigger PDA_FDA_HDR_TRG only runs on INSERT, not UPDATE
        // So DOC_REF won't be regenerated on update, but we should still validate business rules
        if (StringUtils.isNotBlank(dto.getFdaSubType()) && existingEntity != null) {
            String oldFdaSubType = existingEntity.getFdaSubType();
            String newFdaSubType = dto.getFdaSubType();
            Long pdaRef = existingEntity.getPdaRef();

            // Prevent changing from MAIN_FDA to non-MAIN_FDA (would break DOC_REF relationship)
            if ("MAIN_FDA".equalsIgnoreCase(oldFdaSubType) && !"MAIN_FDA".equalsIgnoreCase(newFdaSubType)) {
                throw new CustomException("Cannot change FDA Sub Type from 'MAIN_FDA' to '" + newFdaSubType + "'. This would invalidate the document reference structure.", 400);
            }

            // If changing to MAIN_FDA, validate no other MAIN_FDA exists for this PDA_REF
            if (!"MAIN_FDA".equalsIgnoreCase(oldFdaSubType) && "MAIN_FDA".equalsIgnoreCase(newFdaSubType) && pdaRef != null && pdaFdaHdrRepository.existsMainFdaByPdaRef(pdaRef)) {
                    throw new CustomException("Cannot change FDA Sub Type to 'MAIN_FDA'. A MAIN_FDA already exists for PDA Reference '" + pdaRef + "'.", 400);
                }

        }
    }

    public void recalculateHeaderTotals(Long transactionPoid, Long groupPoid, Long companyPoid) {
        PdaFdaHdr hdr = pdaFdaHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Header", "transactionPoid", transactionPoid));

        List<PdaFdaDtl> details = pdaFdaDtlRepository.findByIdTransactionPoid(transactionPoid);

        BigDecimal totalFda = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (PdaFdaDtl d : details) {
            totalFda = totalFda.add(CalculationUtils.zero(d.getFdaAmount() != null ? d.getFdaAmount() : d.getAmount()));
            totalCost = totalCost.add(CalculationUtils.zero(d.getCostAmount()));
        }

        hdr.setTotalAmount(totalFda);
        hdr.setProfitLossAmount(totalFda.subtract(totalCost));

        pdaFdaHdrRepository.save(hdr);
    }

    public void handleCreate(Long transactionPoid, FdaChargeDto dto, String userId) {
        if (dto.getChargePoid() == null) throw new CustomException("Charge is required", 400);
        if (dto.getQty() == null) throw new CustomException("Quantity is required", 400);
        if (dto.getPdaRate() == null) throw new CustomException("Rate is required", 400);

        if (dto.getDetRowId() == null) {
            dto.setDetRowId(generateNextDetRowId(transactionPoid));
        }

        PdaFdaDtlId id = new PdaFdaDtlId(transactionPoid, dto.getDetRowId());
        PdaFdaDtl entity = ChargesMapper.createNewCharge(id, dto, userId);
        CalculationUtils.recalculateAmounts(entity);
        PdaFdaDtl saved = pdaFdaDtlRepository.save(entity);
        String logDetail = String.format("Row Created on [FDA Charge Details] with detRowId: %s", saved.getId().getDetRowId());
        loggingService.createLogSummaryEntry(UserContext.getDocumentId(), saved.getId().getTransactionPoid().toString(), logDetail);
    }

    public void handleUpdate(Long transactionPoid, FdaChargeDto dto, String userId) {
        if (dto.getDetRowId() == null) throw new CustomException("Det Row Id is required for update", 400);

        PdaFdaDtlId id = new PdaFdaDtlId(transactionPoid, dto.getDetRowId());
        PdaFdaDtl existing = pdaFdaDtlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FDA Detail", "detRowId", dto.getDetRowId()));

        if (StringUtils.isNotBlank(existing.getManual()) && "N".equalsIgnoreCase(existing.getManual())) {
            throw new CustomException("System-generated charge lines cannot be modified", 403);
        }

        PdaFdaDtl oldEntity = new PdaFdaDtl();
        BeanUtils.copyProperties(existing, oldEntity);

        ChargesMapper.updateChargeEntityFromDto(dto, existing, userId);
        CalculationUtils.recalculateAmounts(existing);

        existing = pdaFdaDtlRepository.save(existing);

        String logDetail = String.format("KeyId = TRANSACTION_POID %s: DET_ROW_ID %s", existing.getId().getTransactionPoid(), existing.getId().getDetRowId());
        loggingService.createLog(oldEntity, existing, PdaFdaDtl.class, UserContext.getDocumentId(), transactionPoid.toString(), logDetail);
    }

    private Long generateNextDetRowId(Long transactionPoid) {
        Long maxDetRowId = pdaFdaDtlRepository.findMaxDetRowId(transactionPoid);
        return (maxDetRowId != null ? maxDetRowId + 1 : 1L);
    }

    /**
     * Validates financial year and transaction period for FDA header creation
     * Mirrors the logic from PDA_FDA_HDR_GTTRG trigger
     *
     * @param companyPoid     Company identifier
     * @param transactionDate Transaction date to validate
     */
    public void validateFinancialAndTransactionPeriodForCreate(Long companyPoid, LocalDate transactionDate) {
        String userId = UserContext.getUserId();
        log.info("Starting financial period validation for create - companyPoid: {}, transactionDate: {}, userId: {}", companyPoid, transactionDate, userId);

        if (transactionDate == null) {
            return; // Skip validation if date is not provided
        }

        // Check financial year validity (mirrors trigger logic)
        String financialYearResult = globalValidationRepository.checkFinancialYearValid(companyPoid, transactionDate);

        if (StringUtils.isNotBlank(financialYearResult) && financialYearResult.toUpperCase().contains("ERROR")) {
            throw new CustomException("Changes allowed only within current Financial Period. Please select a date within the allowed financial period.", 400);
        }
    }

    /**
     * Validates financial year and transaction period for FDA header update
     * Mirrors the logic from PDA_FDA_HDR_GTTRG trigger
     * <p>
     * The trigger checks:
     * 1. If transaction date is changing, validate new date against financial year and transaction year
     * 2. Always validate old date against transaction year (prevents updates to records outside current period)
     *
     * @param companyPoid        Company identifier
     * @param oldTransactionDate Existing transaction date from the database
     * @param newTransactionDate New transaction date from the update request (can be null if not changing)
     */
    public void validateFinancialAndTransactionPeriodForUpdate(Long companyPoid, LocalDate oldTransactionDate, LocalDate newTransactionDate) {
        String userId = UserContext.getUserId();
        log.info("Starting financial/transaction period validation for update - companyPoid: {}, oldTransactionDate: {}, newTransactionDate: {}, userId: {}", companyPoid, oldTransactionDate, newTransactionDate, userId);

        // Always check transaction year validity for old date (prevents updates to records outside current period)
        // This mirrors: IF UPDATING AND FUNC_GLOB_TRANSACTN_YEAR_VALID(:NEW.COMPANY_POID, TO_DATE(:OLD.TRANSACTION_DATE)) LIKE '%ERROR%'
        if (oldTransactionDate != null) {
            String transactionYearOldResult = globalValidationRepository.checkTransactionYearValid(companyPoid, oldTransactionDate);

            if (StringUtils.isNotBlank(transactionYearOldResult) && transactionYearOldResult.toUpperCase().contains("ERROR")) {
                throw new CustomException("Changes allowed only within current Transaction Period. This record's transaction date is outside the allowed period.", 400);
            }
        }

        // If new date is null or same as old date, skip further validation (date is not changing)
        if (newTransactionDate == null || (oldTransactionDate != null && oldTransactionDate.equals(newTransactionDate))) {
            return;
        }

        // Date is changing - validate new date against financial year and transaction year
        // This mirrors: IF :NEW.TRANSACTION_DATE IS NOT NULL AND :OLD.TRANSACTION_DATE != :NEW.TRANSACTION_DATE

        // Check financial year validity for new date
        String financialYearResult = globalValidationRepository.checkFinancialYearValid(companyPoid, newTransactionDate);

        if (StringUtils.isNotBlank(financialYearResult) && financialYearResult.toUpperCase().contains("ERROR")) {
            throw new CustomException("Changes allowed only within current Financial Period. Please select a date within the allowed financial period.", 400);
        }

        // Check transaction year validity for new date (only if date is changing)
        // This mirrors: IF UPDATING AND FUNC_GLOB_TRANSACTN_YEAR_VALID(:NEW.COMPANY_POID, TO_DATE(:NEW.TRANSACTION_DATE)) LIKE '%ERROR%'
        String transactionYearNewResult = globalValidationRepository.checkTransactionYearValid(companyPoid, newTransactionDate);

        if (StringUtils.isNotBlank(transactionYearNewResult) && transactionYearNewResult.toUpperCase().contains("ERROR")) {
            throw new CustomException("Transaction date cannot be updated to the selected date. Please select a date within the allowed transaction period.", 400);
        }
    }

}
