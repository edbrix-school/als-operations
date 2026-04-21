package com.asg.operations.pdaentryform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitDocumentsRequest {
    
    @NotNull(message = "Vessel sail date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate vesselSailDate;
}
