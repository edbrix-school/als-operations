package com.asg.operations.portactivitiesmaster.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.operations.common.ApiResponse;
import com.asg.operations.portactivitiesmaster.dto.GetAllPortActivityFilterRequest;
import com.asg.operations.portactivitiesmaster.dto.PortActivityListResponse;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterRequest;
import com.asg.operations.portactivitiesmaster.dto.PortActivityMasterResponse;
import com.asg.operations.portactivitiesmaster.service.PortActivityMasterService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.Map;

import static com.asg.common.lib.dto.response.ApiResponse.internalServerError;
import static com.asg.common.lib.dto.response.ApiResponse.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/port-activities")
@Tag(name = "Port Activity Master", description = "APIs for managing Port Activity Master records")
public class PortActivityMasterController {

    private final PortActivityMasterService portActivityService;
    private final LoggingService loggingService;

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @PostMapping("/search")
    public ResponseEntity<?> getPortActivityList(
            @RequestBody(required = false) FilterRequestDto filterRequest,
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo
    ) {

        try {
            Map<String, Object> portActivityPage = portActivityService.getAllPortActivitiesWithFilters(UserContext.getDocumentId(), filterRequest, pageable, periodFrom, periodTo);
            return success("Port activity list retrieved successfully", portActivityPage);
        }
        catch (Exception ex){
            return internalServerError("Unable to fetch Port activity list: " + ex.getMessage());
        }
    }

    @AllowedAction(UserRolesRightsEnum.VIEW)
    @GetMapping("/{portActivityTypePoid}")
    public ResponseEntity<?> getPortActivityById(
            @PathVariable @NotNull @Positive Long portActivityTypePoid
    ) {
        PortActivityMasterResponse response = portActivityService.getPortActivityById(portActivityTypePoid, UserContext.getGroupPoid());
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), portActivityTypePoid.toString());
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
            @RequestParam(defaultValue = "false") boolean hardDelete,
            @Valid @RequestBody(required = false) DeleteReasonDto deleteReasonDto
    ) {
        portActivityService.deletePortActivity(portActivityTypePoid, UserContext.getGroupPoid(), UserContext.getUserId(), hardDelete,deleteReasonDto);
        return ApiResponse.success("Port activity deleted successfully");
    }
}
