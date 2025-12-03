package com.asg.operations.finaldisbursementaccount.util;

import com.asg.operations.exceptions.CustomException;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.finaldisbursementaccount.dto.FdaChargeDto;
import com.asg.operations.finaldisbursementaccount.dto.FdaHeaderDto;
import com.asg.operations.finaldisbursementaccount.dto.UpdateFdaHeaderRequest;
import com.asg.operations.finaldisbursementaccount.entity.PdaFdaDtl;
import com.asg.operations.finaldisbursementaccount.entity.PdaFdaHdr;
import com.asg.operations.finaldisbursementaccount.key.PdaFdaDtlId;
import com.asg.operations.finaldisbursementaccount.repository.*;
import com.asg.operations.group.repository.GroupRepository;
import com.asg.operations.pdaporttariffmaster.repository.ShipPortMasterRepository;
import com.asg.operations.pdaporttariffmaster.repository.ShipVesselTypeMasterRepository;
import com.asg.operations.shipprincipal.repository.AddressMasterRepository;
import com.asg.operations.shipprincipal.repository.ShipPrincipalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final CompanyRepository companyRepository;
    private final GroupRepository groupRepository;

    public void validateHeaderBeforeSave(FdaHeaderDto dto) {
        if (dto.getPrincipalPoid() != null) {
            if (!shipPrincipalRepository.existsByPrincipalPoid(dto.getPrincipalPoid())) {
                throw new ResourceNotFoundException("Principal not found for: " + dto.getPrincipalPoid());
            }
        }
        if (dto.getSalesmanPoid() != null) {
            if (!salesSalesmanMasterRepository.existsBySalesmanPoid(dto.getSalesmanPoid())) {
                throw new ResourceNotFoundException("Salesman not found for: " + dto.getSalesmanPoid());
            }
        }
        if (dto.getVoyagePoid() != null) {
            if (!shipVoyageHdrRepository.existsByTransactionPoid(dto.getVoyagePoid())) {
                throw new ResourceNotFoundException("Voyage not found for: " + dto.getVoyagePoid());
            }
        }
        if (dto.getPortPoid() != null) {
            if (!shipPortMasterRepository.existsByIdPortPoid(BigDecimal.valueOf(dto.getPortPoid()))) {
                throw new ResourceNotFoundException("Port not found for: " + dto.getPortPoid());
            }
        }
        if (dto.getLinePoid() != null) {
            if (!shipLineMasterRepository.existsByLinePoid(dto.getLinePoid())) {
                throw new ResourceNotFoundException("Line not found for: " + dto.getLinePoid());
            }
        }
        if (dto.getVesselPoid() != null) {
            if (!shipVesselMasterRepository.existsByVesselPoid(dto.getVesselPoid())) {
                throw new ResourceNotFoundException("Vessel not found for: " + dto.getVesselPoid());
            }
        }
        if (dto.getVesselTypePoid() != null) {
            if (!shipVesselTypeMasterRepository.existsByVesselTypePoid(BigDecimal.valueOf(Long.parseLong(dto.getVesselTypePoid())))) {
                throw new ResourceNotFoundException("Vessel Type not found for: " + dto.getVesselTypePoid());
            }
        }
        if (dto.getPrintBankPoid() != null) {
            if (!glBankMasterRepository.existsByBankPoid(dto.getPrintBankPoid())) {
                throw new ResourceNotFoundException("Bank not found for: " + dto.getPrintBankPoid());
            }
        }
        if (dto.getCostCentrePoid() != null) {
            if (!costCenterRepository.existsByCostCenterPoid(dto.getCostCentrePoid())) {
                throw new ResourceNotFoundException("Cost Center not found for: " + dto.getCostCentrePoid());
            }
        }
        if (dto.getGroupPoid() != null) {
            if (!groupRepository.existsByGroupPoid(dto.getGroupPoid())) {
                throw new ResourceNotFoundException("Group not found for: " + dto.getGroupPoid());
            }
        }
        if (dto.getCompanyPoid() != null) {
            if (!companyRepository.existsByCompanyPoid(dto.getCompanyPoid())) {
                throw new ResourceNotFoundException("Company not found for: " + dto.getCompanyPoid());
            }
        }
        if (dto.getAddressPoid() != null) {
            if (!addressMasterRepository.existsByAddressMasterPoid(dto.getAddressPoid())) {
                throw new ResourceNotFoundException("Address not found for: " + dto.getAddressPoid());
            }
        }
        if (dto.getTermsPoid() != null) {
            if (!termsTemplateRepository.existsByTermsPoid(dto.getTermsPoid())) {
                throw new ResourceNotFoundException("Terms not found for: " + dto.getTermsPoid());
            }
        }
    }

    public void validateHeaderBeforeUpdate(UpdateFdaHeaderRequest dto) {
        if (dto.getPrincipalPoid() != null) {
            if (!shipPrincipalRepository.existsByPrincipalPoid(dto.getPrincipalPoid())) {
                throw new ResourceNotFoundException("Principal not found for: " + dto.getPrincipalPoid());
            }
        }
        if (dto.getSalesmanPoid() != null) {
            if (!salesSalesmanMasterRepository.existsBySalesmanPoid(dto.getSalesmanPoid())) {
                throw new ResourceNotFoundException("Salesman not found for: " + dto.getSalesmanPoid());
            }
        }
        if (dto.getPortPoid() != null) {
            if (!shipPortMasterRepository.existsByIdPortPoid(BigDecimal.valueOf(dto.getPortPoid()))) {
                throw new ResourceNotFoundException("Port not found for: " + dto.getPortPoid());
            }
        }
    }

    public void recalculateHeaderTotals(Long transactionPoid, String userId, Long groupPoid, Long companyPoid) {
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
        hdr.setLastModifiedBy(userId);
        hdr.setLastModifiedDate(LocalDateTime.now());

        pdaFdaHdrRepository.save(hdr);
    }

    public void handleCreateOrUpdate(Long transactionPoid, FdaChargeDto dto, List<PdaFdaDtl> toSave, String userId) {
        if (dto.getChargePoid() == null) {
            throw new CustomException("Charge is required", 422);
        }
        if (dto.getQty() == null) {
            throw new CustomException("Quantity is required", 422);
        }
        if (dto.getPdaRate() == null) {
            throw new CustomException("Rate is required", 422);
        }
        if (dto.getDetRowId() == null) {
            dto.setDetRowId(generateNextDetRowId(transactionPoid));
        }

        PdaFdaDtlId id = new PdaFdaDtlId(transactionPoid, dto.getDetRowId());

        PdaFdaDtl entity = pdaFdaDtlRepository.findById(id).orElseGet(() -> ChargesMapper.createNewCharge(id, dto, userId));

        if (entity.getManual() != null && "N".equalsIgnoreCase(entity.getManual()) && !"iscreated".equalsIgnoreCase(dto.getActionType() != null ? dto.getActionType().toLowerCase() : "")) {
            throw new CustomException("System-generated charge lines cannot be modified", 403);
        }
        ChargesMapper.updateChargeEntityFromDto(dto, entity, userId);

        CalculationUtils.recalculateAmounts(entity);

        toSave.add(entity);
    }

    private Long generateNextDetRowId(Long transactionPoid) {
        Long maxDetRowId = pdaFdaDtlRepository.findMaxDetRowId(transactionPoid);
        return (maxDetRowId != null ? maxDetRowId + 1 : 1L);
    }

    public String generateDocRef(Long groupPoid) {
        String prefix = "ASG";
        Integer maxSeq = pdaFdaHdrRepository.findMaxSequence(prefix, groupPoid);
        int nextSeq = (maxSeq == null) ? 1 : maxSeq + 1;
        return prefix + nextSeq;
    }

}
