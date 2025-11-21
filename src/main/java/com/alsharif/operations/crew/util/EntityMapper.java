package com.alsharif.operations.crew.util;


import com.alsharif.operations.crew.dto.ContractCrewDtlRequest;
import com.alsharif.operations.crew.dto.ContractCrewDtlResponse;
import com.alsharif.operations.crew.dto.ContractCrewRequest;
import com.alsharif.operations.crew.dto.ContractCrewResponse;
import com.alsharif.operations.crew.entity.ContractCrew;
import com.alsharif.operations.crew.entity.ContractCrewDtl;
import com.alsharif.operations.crew.entity.ContractCrewDtlId;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between entities and DTOs
 */
@Component
public class EntityMapper {


    /**
     * Map ContractCrew entity to ContractCrewResponse DTO
     */
    public ContractCrewResponse toContractCrewResponse(ContractCrew entity, String nationalityCode, String nationalityName) {
        if (entity == null) {
            return null;
        }

        ContractCrewResponse response = new ContractCrewResponse();
        response.setCrewPoid(entity.getCrewPoid());
        //response.setCrewCode(entity.getCrewCode());
        response.setCrewName(entity.getCrewName());
        response.setCrewNationalityPoid(entity.getCrewNationPoid());
        response.setCrewNationalityCode(nationalityCode);
        response.setCrewNationalityName(nationalityName);
        response.setCrewCdcNumber(entity.getCrewCdcNumber());
        response.setCrewCompany(entity.getCrewCompany());
        response.setCrewDesignation(entity.getCrewDesignation());
        response.setCrewPassportNumber(entity.getCrewPassportNumber());
        response.setCrewPassportIssueDate(entity.getCrewPassportIssueDate());
        response.setCrewPassportExpiryDate(entity.getCrewPassportExpiryDate());
        response.setCrewPassportIssuePlace(entity.getCrewPassportIssuePlace());
        response.setRemarks(entity.getRemarks());
        response.setActive(entity.getActive());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());


        return response;
    }

    public ContractCrewResponse toContractCrewRes(ContractCrew entity) {
        if (entity == null) {
            return null;
        }

        ContractCrewResponse response = new ContractCrewResponse();
        response.setCrewPoid(entity.getCrewPoid());
        //response.setCrewCode(entity.getCrewCode());
        response.setCrewName(entity.getCrewName());
        response.setCrewNationalityPoid(entity.getCrewNationPoid());
        response.setCrewCdcNumber(entity.getCrewCdcNumber());
        response.setCrewCompany(entity.getCrewCompany());
        response.setCrewDesignation(entity.getCrewDesignation());
        response.setCrewPassportNumber(entity.getCrewPassportNumber());
        response.setCrewPassportIssueDate(entity.getCrewPassportIssueDate());
        response.setCrewPassportExpiryDate(entity.getCrewPassportExpiryDate());
        response.setCrewPassportIssuePlace(entity.getCrewPassportIssuePlace());
        response.setRemarks(entity.getRemarks());
        response.setActive(entity.getActive());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());


        return response;
    }

    /**
     * Convenience overload: map detail without external document type lookup.
     * Uses the entity's documentType as both code and name placeholders.
     */
    public ContractCrewDtlResponse toContractCrewDtlResponse(ContractCrewDtl entity) {
        String code = entity != null ? entity.getDocumentType() : null;
        return toContractCrewDtlResponse(entity, code, code);
    }

    /**
     * Map ContractCrewRequest DTO to ContractCrew entity (for create)
     */
    public ContractCrew toContractCrewEntity(Long companyPoid, Long groupPoid,String userId,ContractCrewRequest request) {
        if (request == null) {
            return null;
        }

        ContractCrew entity = new ContractCrew();
        entity.setCrewName(request.getCrewName());
        entity.setCrewNationPoid(request.getCrewNationalityPoid());
        entity.setCrewCdcNumber(request.getCrewCdcNumber());
        entity.setCrewCompany(request.getCrewCompany());
        entity.setCrewDesignation(request.getCrewDesignation());
        entity.setCrewPassportNumber(request.getCrewPassportNumber());
        entity.setCrewPassportIssueDate(request.getCrewPassportIssueDate());
        entity.setCrewPassportExpiryDate(request.getCrewPassportExpiryDate());
        entity.setCrewPassportIssuePlace(request.getCrewPassportIssuePlace());
        entity.setRemarks(request.getRemarks());
        entity.setActive(request.getActive() != null ? request.getActive() : "Y");

        // Set audit fields

        entity.setCreatedBy(userId);
        entity.setCreatedDate(LocalDateTime.now());

        // Set multi-tenant fields
        entity.setCompanyPoid(companyPoid);
        entity.setGroupPoid(groupPoid);

        return entity;
    }

    /**
     * Update ContractCrew entity from ContractCrewRequest DTO (for update)
     */
    public void updateContractCrewEntity(ContractCrew entity, ContractCrewRequest request) {
        if (entity == null || request == null) {
            return;
        }

        // Update fields (do not update CREW_CODE and CREW_POID)
        entity.setCrewName(request.getCrewName());
        entity.setCrewNationPoid(request.getCrewNationalityPoid());
        entity.setCrewCdcNumber(request.getCrewCdcNumber());
        entity.setCrewCompany(request.getCrewCompany());
        entity.setCrewDesignation(request.getCrewDesignation());
        entity.setCrewPassportNumber(request.getCrewPassportNumber());
        entity.setCrewPassportIssueDate(request.getCrewPassportIssueDate());
        entity.setCrewPassportExpiryDate(request.getCrewPassportExpiryDate());
        entity.setCrewPassportIssuePlace(request.getCrewPassportIssuePlace());
        entity.setRemarks(request.getRemarks());
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }

        // Update audit fields (only modified fields)

    }

    /**
     * Map ContractCrewDtl entity to ContractCrewDtlResponse DTO
     */
    public ContractCrewDtlResponse toContractCrewDtlResponse(
            ContractCrewDtl entity,
            String documentTypeCode,
            String documentTypeName
    ) {
        if (entity == null) {
            return null;
        }

        ContractCrewDtlResponse response = new ContractCrewDtlResponse();
        response.setCrewPoid(entity.getId().getCrewPoid());
        response.setDetRowId(entity.getId().getDetRowId());
        response.setDocumentType(entity.getDocumentType());
        response.setDocumentTypeCode(documentTypeCode);
        response.setDocumentTypeName(documentTypeName);
        response.setDocumentNumber(entity.getDocumentNumber());
        response.setDocumentAppliedDate(entity.getDocumentAppliedDate());
        response.setDocumentIssueDate(entity.getDocumentIssueDate());
        response.setDocumentExpiryDate(entity.getDocumentExpiryDate());
        response.setPptReceiptDate(entity.getPptReceiptDate());
        response.setPptReturnDate(entity.getPptReturnDate());
        response.setRemarks(entity.getRemarks());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastModifiedBy(entity.getLastModifiedBy());
        response.setLastModifiedDate(entity.getLastModifiedDate());
        response.setActive(entity.getActive());
        return response;
    }

    /**
     * Map ContractCrewDtlRequest DTO to ContractCrewDtl entity (for create)
     */
    public ContractCrewDtl toContractCrewDtlEntity(String userId,ContractCrewDtlRequest request, Long crewPoid) {
        if (request == null) {
            return null;
        }

        ContractCrewDtl entity = new ContractCrewDtl();
        ContractCrewDtlId id = new ContractCrewDtlId();
        id.setCrewPoid(crewPoid);
        // If caller provided a detRowId (for updates or client-controlled inserts), set it here
        if (request.getDetRowId() != null) {
            id.setDetRowId(request.getDetRowId());
        }
        entity.setId(id);
        entity.setDocumentType(request.getDocumentType());
        entity.setDocumentNumber(request.getDocumentNumber());
        entity.setDocumentAppliedDate(request.getDocumentAppliedDate());
        entity.setDocumentIssueDate(request.getDocumentIssueDate());
        entity.setDocumentExpiryDate(request.getDocumentExpiryDate());
        entity.setPptReceiptDate(request.getPptReceiptDate());
        entity.setPptReturnDate(request.getPptReturnDate());
        entity.setRemarks(request.getRemarks());

        // Set audit fields

        entity.setCreatedBy(userId);
        entity.setCreatedDate(LocalDateTime.now());
        //entity.setModifiedBy(currentUser);

        return entity;
    }

    /**
     * Update ContractCrewDtl entity from ContractCrewDtlRequest DTO (for update)
     */
    public void updateContractCrewDtlEntity(ContractCrewDtl entity, ContractCrewDtlRequest request) {
        if (entity == null || request == null) {
            return;
        }

        // Update fields
        entity.setDocumentType(request.getDocumentType());
        entity.setDocumentNumber(request.getDocumentNumber());
        entity.setDocumentAppliedDate(request.getDocumentAppliedDate());
        entity.setDocumentIssueDate(request.getDocumentIssueDate());
        entity.setDocumentExpiryDate(request.getDocumentExpiryDate());
        entity.setPptReceiptDate(request.getPptReceiptDate());
        entity.setPptReturnDate(request.getPptReturnDate());
        entity.setRemarks(request.getRemarks());

        // Update audit fields
       // entity.setModifiedBy(securityContextUtil.getCurrentUserId());

    }

    /**
     * Map list of detail entities to responses
     */
    public List<ContractCrewDtlResponse> toContractCrewDtlResponseList(
            List<ContractCrewDtl> entities,
            java.util.Map<String, String[]> documentTypeMap // code -> [code, description]
    ) {
        return entities.stream()
                .map(entity -> {
                    String code = entity.getDocumentType();
                    String[] typeInfo = documentTypeMap.getOrDefault(code, new String[]{code, code});
                    return toContractCrewDtlResponse(entity, typeInfo[0], typeInfo[1]);
                })
                .collect(Collectors.toList());
    }


}

