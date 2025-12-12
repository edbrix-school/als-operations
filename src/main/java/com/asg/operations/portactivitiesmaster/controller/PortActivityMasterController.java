package com.asg.operations.portactivitiesmaster.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.portactivitiesmaster.dto.GetAllPortActivityFilterRequest;
import com.asg.operations.portactivitiesmaster.dto.PageResponse;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterRequest;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterResponse;
import com.asg.operations.portactivitiesmaster.service.PortActivityMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/port-activities")
@Tag(name = "Port Activity Master", description = "APIs for managing Port Activity Master records")
public class PortActivityMasterController {

    private final PortActivityMasterService portActivityService;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/search")
    public ResponseEntity<?> getPortActivityList(
            @RequestBody(required = false) GetAllPortActivityFilterRequest filterRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        if (filterRequest == null) {
            filterRequest = new GetAllPortActivityFilterRequest();
            filterRequest.setIsDeleted("N");
            filterRequest.setOperator("AND");
            filterRequest.setFilters(new java.util.ArrayList<>());
        }

        org.springframework.data.domain.Page<PortActivityMasterResponse> portActivityPage = portActivityService
                .getAllPortActivitiesWithFilters(UserContext.getGroupPoid(), filterRequest, page, size, sort);

        java.util.Map<String, String> displayFields = new java.util.HashMap<>();
        displayFields.put("PORT_ACTIVITY_TYPE_CODE", "text");
        displayFields.put("PORT_ACTIVITY_TYPE_NAME", "text");
        displayFields.put("PORT_ACTIVITY_TYPE_NAME2", "text");

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", portActivityPage.getContent());
        response.put("pageNumber", portActivityPage.getNumber());
        response.put("displayFields", displayFields);
        response.put("pageSize", portActivityPage.getSize());
        response.put("totalElements", portActivityPage.getTotalElements());
        response.put("totalPages", portActivityPage.getTotalPages());
        response.put("last", portActivityPage.isLast());

        return ApiResponse.success("Port activity list retrieved successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{portActivityTypePoid}")
    public ResponseEntity<?> getPortActivityById(
            @PathVariable @NotNull @Positive Long portActivityTypePoid
    ) {
        PortActivityMasterResponse response = portActivityService.getPortActivityById(portActivityTypePoid, UserContext.getGroupPoid());
        return ApiResponse.success("Port activity retrieved successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.CREATE)
    @PostMapping
    public ResponseEntity<?> createPortActivity(
            @Valid @RequestBody PortActivityMasterRequest request
    ) {
        PortActivityMasterResponse response = portActivityService.createPortActivity(request, UserContext.getGroupPoid(), UserContext.getUserId());
        return ApiResponse.success("Port activity created successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.EDIT)
    @PutMapping("/{portActivityTypePoid}")
    public ResponseEntity<?> updatePortActivity(
            @PathVariable @NotNull @Positive Long portActivityTypePoid,
            @Valid @RequestBody PortActivityMasterRequest request
    ) {
        PortActivityMasterResponse response = portActivityService.updatePortActivity(
                portActivityTypePoid, request, UserContext.getGroupPoid(), UserContext.getUserId());
        return ApiResponse.success("Port activity updated successfully", response);
    }

    @AllowedAction(UserRolesRightsEnum.DELETE)
    @DeleteMapping("/{portActivityTypePoid}")
    public ResponseEntity<?> deletePortActivity(
            @PathVariable @NotNull @Positive Long portActivityTypePoid,
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        portActivityService.deletePortActivity(portActivityTypePoid, UserContext.getGroupPoid(), UserContext.getUserId(), hardDelete);
        return ApiResponse.success("Port activity deleted successfully");
    }
}
