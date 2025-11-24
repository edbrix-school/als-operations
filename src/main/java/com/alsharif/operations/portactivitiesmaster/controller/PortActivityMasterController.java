package com.alsharif.operations.portactivitiesmaster.controller;

import com.alsharif.operations.common.ApiResponse;
import com.alsharif.operations.portactivitiesmaster.dto.*;
import com.alsharif.operations.portactivitiesmaster.service.PortActivityMasterService;
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
@RequestMapping("/api/v1/port-activities")
@Tag(name = "Port Activity Master", description = "APIs for managing Port Activity Master records")
public class PortActivityMasterController {

    private final PortActivityMasterService portActivityService;

    @GetMapping
    public ResponseEntity<?> getPortActivityList(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String active,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            Pageable pageable
    ) {
        PageResponse<PortActivityMasterResponse> response = portActivityService.getPortActivityList(
                code, name, active, groupPoid, pageable);
        return ApiResponse.success("Port activity list retrieved successfully", response);
    }

    @GetMapping("/{portActivityTypePoid}")
    public ResponseEntity<?> getPortActivityById(
            @PathVariable @NotNull @Positive Long portActivityTypePoid,
            @RequestHeader("X-Group-Poid") Long groupPoid
    ) {
        PortActivityMasterResponse response = portActivityService.getPortActivityById(portActivityTypePoid, groupPoid);
        return ApiResponse.success("Port activity retrieved successfully", response);
    }

    @PostMapping
    public ResponseEntity<?> createPortActivity(
            @Valid @RequestBody PortActivityMasterRequest request,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        PortActivityMasterResponse response = portActivityService.createPortActivity(request, groupPoid, userId);
        return ApiResponse.success("Port activity created successfully", response);
    }

    @PutMapping("/{portActivityTypePoid}")
    public ResponseEntity<?> updatePortActivity(
            @PathVariable @NotNull @Positive Long portActivityTypePoid,
            @Valid @RequestBody PortActivityMasterRequest request,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-User-Id") String userId
    ) {
        PortActivityMasterResponse response = portActivityService.updatePortActivity(
                portActivityTypePoid, request, groupPoid, userId);
        return ApiResponse.success("Port activity updated successfully", response);
    }

    @DeleteMapping("/{portActivityTypePoid}")
    public ResponseEntity<?> deletePortActivity(
            @PathVariable @NotNull @Positive Long portActivityTypePoid,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        portActivityService.deletePortActivity(portActivityTypePoid, groupPoid, userId, hardDelete);
        return ApiResponse.success("Port activity deleted successfully");
    }
}