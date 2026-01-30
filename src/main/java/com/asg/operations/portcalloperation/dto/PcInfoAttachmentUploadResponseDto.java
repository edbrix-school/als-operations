package com.asg.operations.portcalloperation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO mirroring common attachment service upload response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PcInfoAttachmentUploadResponseDto {
    private List<PcInfoAttachmentDto> uploadedFiles;
    private List<String> errors;
    private boolean hasErrors;
}
