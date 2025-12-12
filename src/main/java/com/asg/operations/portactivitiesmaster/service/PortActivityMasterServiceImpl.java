package com.asg.operations.portactivitiesmaster.service;

import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.portactivitiesmaster.dto.PageResponse;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterRequest;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterResponse;
import com.asg.operations.portactivitiesmaster.entity.PortActivityMaster;
import com.asg.operations.portactivitiesmaster.repository.PortActivityMasterRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PortActivityMasterServiceImpl implements PortActivityMasterService {

    private final PortActivityMasterRepository repository;
    private final LovService lovService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PortActivityMasterResponse> getPortActivityList(String code, String name, String active, Long groupPoid, Pageable pageable) {

        Page<PortActivityMaster> page = repository.findByGroupPoidAndFilters(groupPoid, code, name, active, pageable);

        return PageResponse.<PortActivityMasterResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PortActivityMasterResponse getPortActivityById(Long portActivityTypePoid, Long groupPoid) {
        PortActivityMaster entity = repository.findByPortActivityTypePoidAndGroupPoid(portActivityTypePoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Port activity not found"));

        return mapToResponse(entity);
    }

    @Override
    public PortActivityMasterResponse createPortActivity(PortActivityMasterRequest request, Long groupPoid, String userId) {
        PortActivityMaster entity = PortActivityMaster.builder()
                .groupPoid(groupPoid)
                .portActivityTypeCode(generatePortActivityTypeCode(groupPoid))
                .portActivityTypeName(request.getPortActivityTypeName())
                .portActivityTypeName2(request.getPortActivityTypeName2())
                .active(StringUtils.isNotBlank(request.getActive()) ? request.getActive() : "Y")
                .seqno(request.getSeqno())
                .remarks(request.getRemarks())
                .createdBy(userId)
                .createdDate(LocalDateTime.now())
                .lastModifiedBy(userId)
                .lastModifiedDate(LocalDateTime.now())
                .deleted("N")
                .build();

        entity = repository.save(entity);
        return mapToResponse(entity);
    }

    @Override
    public PortActivityMasterResponse updatePortActivity(Long portActivityTypePoid, PortActivityMasterRequest request, Long groupPoid, String userId) {
        PortActivityMaster entity = repository.findByPortActivityTypePoidAndGroupPoid(portActivityTypePoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Port activity not found"));

        entity.setPortActivityTypeName(request.getPortActivityTypeName());
        entity.setPortActivityTypeName2(request.getPortActivityTypeName2());
        entity.setActive(StringUtils.isNotBlank(request.getActive()) ? request.getActive() : entity.getActive());
        entity.setSeqno(request.getSeqno());
        entity.setRemarks(request.getRemarks());
        entity.setLastModifiedBy(userId);
        entity.setLastModifiedDate(LocalDateTime.now());

        entity = repository.save(entity);
        return mapToResponse(entity);
    }

    @Override
    public void deletePortActivity(Long portActivityTypePoid, Long groupPoid, String userId, boolean hardDelete) {
        PortActivityMaster entity = repository.findByPortActivityTypePoidAndGroupPoid(portActivityTypePoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Port activity not found"));

        if (hardDelete) {
            repository.delete(entity);
        } else {
            entity.setDeleted("Y");
            entity.setActive("N");
            entity.setLastModifiedBy(userId);
            entity.setLastModifiedDate(LocalDateTime.now());
            repository.save(entity);
        }
    }

    private PortActivityMasterResponse mapToResponse(PortActivityMaster entity) {
        return PortActivityMasterResponse.builder()
                .portActivityTypePoid(entity.getPortActivityTypePoid())
                .groupPoid(entity.getGroupPoid())
                .groupDet(lovService.getLovItemByPoid(entity.getGroupPoid(), "GROUP", UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid()))
                .portActivityTypeCode(entity.getPortActivityTypeCode())
                .portActivityTypeName(entity.getPortActivityTypeName())
                .portActivityTypeName2(entity.getPortActivityTypeName2())
                .active(entity.getActive())
                .seqno(entity.getSeqno())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .lastModifiedBy(entity.getLastModifiedBy())
                .lastModifiedDate(entity.getLastModifiedDate())
                .deleted(entity.getDeleted())
                .remarks(entity.getRemarks())
                .build();
    }

    private String generatePortActivityTypeCode(Long groupPoid) {
        String prefix = "PA";
        Integer maxSeq = repository.findMaxCodeSequence(prefix, groupPoid);
        int nextSeq = (maxSeq == null ? 1 : maxSeq + 1);

        return prefix + nextSeq;
    }


}