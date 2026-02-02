package com.asg.operations.portcalloperation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response DTO mirroring common attachment service upload response.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PcInfoAttachmentUploadResponseDto {
    private List<PcInfoAttachmentDto> uploadedFiles;
    private List<String> errors;
    private boolean hasErrors;
}
