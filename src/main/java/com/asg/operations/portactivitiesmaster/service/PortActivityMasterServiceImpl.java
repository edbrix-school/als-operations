package com.asg.operations.portactivitiesmaster.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.utility.PaginationUtil;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import com.asg.operations.commonlov.service.LovService;
import com.asg.operations.exceptions.ResourceNotFoundException;
import com.asg.operations.portactivitiesmaster.dto.*;
import com.asg.operations.portactivitiesmaster.entity.PortActivityMaster;
import com.asg.operations.portactivitiesmaster.repository.PortActivityMasterRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PortActivityMasterServiceImpl implements PortActivityMasterService {

    private final PortActivityMasterRepository repository;
    private final LovService lovService;
    private final LoggingService loggingService;
    private final DocumentDeleteService documentDeleteService;
    private final DocumentSearchService documentSearchService;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAllPortActivitiesWithFilters(
            String documentId, FilterRequestDto filterRequestDto, Pageable pageable, LocalDate periodFrom, LocalDate periodTo) {

        String operator = documentSearchService.resolveOperator(filterRequestDto);
        String isDeleted = documentSearchService.resolveIsDeleted(filterRequestDto);
        List<FilterDto> filters = documentSearchService.resolveDateFilters(filterRequestDto,"TRANSACTION_DATE", periodFrom, periodTo);

        RawSearchResult raw = documentSearchService.search(documentId, filters, operator, pageable, isDeleted,
                "PORT_ACTIVITY_TYPE_CODE",
                "PORT_ACTIVITY_TYPE_POID");

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
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
                .deleted("N")
                .build();

        entity = repository.save(entity);
        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, UserContext.getDocumentId(), entity.getPortActivityTypePoid().toString());
        return mapToResponse(entity);
    }

    @Override
    public PortActivityMasterResponse updatePortActivity(Long portActivityTypePoid, PortActivityMasterRequest request, Long groupPoid, String userId) {
        PortActivityMaster entity = repository.findByPortActivityTypePoidAndGroupPoid(portActivityTypePoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Port activity not found"));

        PortActivityMaster oldEntity = new PortActivityMaster();
        BeanUtils.copyProperties(entity, oldEntity);

        entity.setPortActivityTypeName(request.getPortActivityTypeName());
        entity.setPortActivityTypeName2(request.getPortActivityTypeName2());
        entity.setActive(StringUtils.isNotBlank(request.getActive()) ? request.getActive() : entity.getActive());
        entity.setSeqno(request.getSeqno());
        entity.setRemarks(request.getRemarks());

        entity = repository.save(entity);
        loggingService.logChanges(oldEntity, entity, PortActivityMaster.class, UserContext.getDocumentId(), entity.getPortActivityTypePoid().toString(), LogDetailsEnum.MODIFIED, "PORT_ACTIVITY_TYPE_POID");
        return mapToResponse(entity);
    }

    @Override
    public void deletePortActivity(Long portActivityTypePoid, Long groupPoid, String userId, @Valid DeleteReasonDto deleteReasonDto) {
        PortActivityMaster entity = repository.findByPortActivityTypePoidAndGroupPoid(portActivityTypePoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Port activity not found"));

        documentDeleteService.deleteDocument(
                portActivityTypePoid,
                "OPS_PORT_ACTIVITY_MASTER",
                "PORT_ACTIVITY_TYPE_POID",
                deleteReasonDto,
                LocalDate.now()
        );
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