package com.asg.operations.portactivitiesmaster.service;

import com.asg.operations.portactivitiesmaster.dto.GetAllPortActivityFilterRequest;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterRequest;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterResponse;

public interface PortActivityMasterService {

    org.springframework.data.domain.Page<PortActivityMasterResponse> getAllPortActivitiesWithFilters(Long groupPoid, GetAllPortActivityFilterRequest filterRequest, int page, int size, String sort);

    PortActivityMasterResponse getPortActivityById(Long portActivityTypePoid, Long groupPoid);

    PortActivityMasterResponse createPortActivity(PortActivityMasterRequest request, Long groupPoid, String userId);

    PortActivityMasterResponse updatePortActivity(Long portActivityTypePoid, PortActivityMasterRequest request, Long groupPoid, String userId);

    void deletePortActivity(Long portActivityTypePoid, Long groupPoid, String userId, boolean hardDelete);
}
