package com.asg.operations.portactivitiesmaster.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.portactivitiesmaster.dto.GetAllPortActivityFilterRequest;
import com.asg.operations.portactivitiesmaster.dto.PortActivityListResponse;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterRequest;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface PortActivityMasterService {

    Map<String, Object> getAllPortActivitiesWithFilters(String documentId, FilterRequestDto filters, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);

    PortActivityMasterResponse getPortActivityById(Long portActivityTypePoid, Long groupPoid);

    PortActivityMasterResponse createPortActivity(PortActivityMasterRequest request, Long groupPoid, String userId);

    PortActivityMasterResponse updatePortActivity(Long portActivityTypePoid, PortActivityMasterRequest request, Long groupPoid, String userId);

    void deletePortActivity(Long portActivityTypePoid, Long groupPoid, String userId, boolean hardDelete, @Valid DeleteReasonDto deleteReasonDto);
}
