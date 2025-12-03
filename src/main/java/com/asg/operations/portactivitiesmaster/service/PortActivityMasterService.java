package com.asg.operations.portactivitiesmaster.service;

import com.asg.operations.portactivitiesmaster.dto.PageResponse;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterRequest;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterResponse;
import org.springframework.data.domain.Pageable;

public interface PortActivityMasterService {

    PageResponse<PortActivityMasterResponse> getPortActivityList(String code, String name, String active, Long groupPoid, Pageable pageable);

    PortActivityMasterResponse getPortActivityById(Long portActivityTypePoid, Long groupPoid);

    PortActivityMasterResponse createPortActivity(PortActivityMasterRequest request, Long groupPoid, String userId);

    PortActivityMasterResponse updatePortActivity(Long portActivityTypePoid, PortActivityMasterRequest request, Long groupPoid, String userId);

    void deletePortActivity(Long portActivityTypePoid, Long groupPoid, String userId, boolean hardDelete);
}
