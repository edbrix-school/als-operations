package com.asg.operations.portactivitiesmaster.controller;

import com.asg.common.lib.security.util.UserContext;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.portactivitiesmaster.dto.PageResponse;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterRequest;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterResponse;
import com.asg.operations.portactivitiesmaster.service.PortActivityMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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

    @GetMapping
    public ResponseEntity<?> getPortActivityList(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String active,
            Pageable pageable
    ) {
        PageResponse<PortActivityMasterResponse> response = portActivityService.getPortActivityList(
                code, name, active, UserContext.getGroupPoid(), pageable);
        return ApiResponse.success("Port activity list retrieved successfully", response);
    }

    @GetMapping("/{portActivityTypePoid}")
    public ResponseEntity<?> getPortActivityById(
            @PathVariable @NotNull @Positive Long portActivityTypePoid
    ) {
        PortActivityMasterResponse response = portActivityService.getPortActivityById(portActivityTypePoid, UserContext.getGroupPoid());
        return ApiResponse.success("Port activity retrieved successfully", response);
    }

    @PostMapping
    public ResponseEntity<?> createPortActivity(
            @Valid @RequestBody PortActivityMasterRequest request
    ) {
        PortActivityMasterResponse response = portActivityService.createPortActivity(request, UserContext.getGroupPoid(), UserContext.getUserId());
        return ApiResponse.success("Port activity created successfully", response);
    }

    @PutMapping("/{portActivityTypePoid}")
    public ResponseEntity<?> updatePortActivity(
            @PathVariable @NotNull @Positive Long portActivityTypePoid,
            @Valid @RequestBody PortActivityMasterRequest request
    ) {
        PortActivityMasterResponse response = portActivityService.updatePortActivity(
                portActivityTypePoid, request, UserContext.getGroupPoid(), UserContext.getUserId());
        return ApiResponse.success("Port activity updated successfully", response);
    }

    @DeleteMapping("/{portActivityTypePoid}")
    public ResponseEntity<?> deletePortActivity(
            @PathVariable @NotNull @Positive Long portActivityTypePoid,
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        portActivityService.deletePortActivity(portActivityTypePoid, UserContext.getGroupPoid(), UserContext.getUserId(), hardDelete);
        return ApiResponse.success("Port activity deleted successfully");
    }
}
