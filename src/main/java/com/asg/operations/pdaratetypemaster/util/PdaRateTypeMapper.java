package com.asg.operations.pdaratetypemaster.util;

import com.asg.operations.pdaratetypemaster.dto.PdaRateTypeRequestDTO;
import com.asg.operations.pdaratetypemaster.dto.PdaRateTypeResponseDTO;
import com.asg.operations.pdaratetypemaster.entity.PdaRateTypeMaster;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PdaRateTypeMapper {

//    private final SecurityContextUtil securityContextUtil;
//
//    public PdaRateTypeMapper(SecurityContextUtil securityContextUtil) {
//        this.securityContextUtil = securityContextUtil;
//    }

    /**
     * Map PdaRateTypeMaster entity to PdaRateTypeMasterResponse DTO
     */
    public PdaRateTypeResponseDTO toResponse(PdaRateTypeMaster entity) {
        if (entity == null) {
            return null;
        }

        PdaRateTypeResponseDTO response = new PdaRateTypeResponseDTO();
        response.setRateTypeId(entity.getRateTypePoid());
        response.setRateTypeCode(entity.getRateTypeCode());
        response.setRateTypeName(entity.getRateTypeName());
        response.setRateTypeName2(entity.getRateTypeName2());
        response.setRateTypeFormula(entity.getRateTypeFormula());
        response.setDefQty(entity.getDefQty());
        response.setDefDays(entity.getDefDays());
        response.setSeqNo(entity.getSeqno());
        response.setActive(entity.getActive());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setModifiedBy(entity.getLastmodifiedBy());
        response.setModifiedDate(entity.getLastmodifiedDate());

        return response;
    }

    /**
     * Map PdaRateTypeMasterRequest DTO to PdaRateTypeMaster entity (for create)
     */
    public PdaRateTypeMaster toEntity(PdaRateTypeRequestDTO request, BigDecimal groupPoid, String userId) {
        if (request == null) {
            return null;
        }

        PdaRateTypeMaster entity = new PdaRateTypeMaster();

        String code = request.getRateTypeCode() != null
                ? request.getRateTypeCode().trim().toUpperCase()
                : null;
        entity.setRateTypeCode(code);

        entity.setRateTypeName(request.getRateTypeName() != null
                ? request.getRateTypeName().trim()
                : null);
        entity.setRateTypeName2(request.getRateTypeName2() != null
                ? request.getRateTypeName2().trim()
                : null);

        entity.setRateTypeFormula(request.getRateTypeFormula() != null
                ? request.getRateTypeFormula().trim()
                : null);

        entity.setDefQty(request.getDefQty() != null
                ? request.getDefQty().trim()
                : null);
        entity.setDefDays(request.getDefDays());
        entity.setSeqno(request.getSeqNo());
        String activeValue = request.getActive() != null ? request.getActive() : "Y";
        entity.setActive(activeValue.equals("true") || activeValue.equals("Y") ? "Y" : "N");
        entity.setDeleted("N");

        entity.setCreatedBy(userId);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setLastmodifiedBy(userId);
        entity.setLastmodifiedDate(LocalDateTime.now());

        entity.setGroupPoid(groupPoid);

        return entity;
    }

    /**
     * Update PdaRateTypeMaster entity from PdaRateTypeMasterRequest DTO (for update)
     */
    public void updateEntityFromRequest(PdaRateTypeMaster entity, PdaRateTypeRequestDTO request, String userId) {
        if (entity == null || request == null) {
            return;
        }

        if (request.getRateTypeName() != null) {
            entity.setRateTypeName(request.getRateTypeName().trim());
        }
        if (request.getRateTypeName2() != null) {
            entity.setRateTypeName2(request.getRateTypeName2().trim());
        }

        if (request.getRateTypeFormula() != null) {
            entity.setRateTypeFormula(request.getRateTypeFormula().trim());
        }

        if (request.getDefQty() != null) {
            entity.setDefQty(request.getDefQty().trim());
        }
        entity.setDefDays(request.getDefDays());
        entity.setSeqno(request.getSeqNo());
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }

        entity.setLastmodifiedBy(userId);
        entity.setLastmodifiedDate(LocalDateTime.now());
    }

    /**
     * Map list of entities to responses
     */
    public List<PdaRateTypeResponseDTO> toResponseList(List<PdaRateTypeMaster> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
