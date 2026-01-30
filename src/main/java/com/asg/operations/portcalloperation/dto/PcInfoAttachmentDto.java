package com.asg.operations.portcalloperation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for attachment info returned from common attachment service or stored in PC_INFO_ATTACHMENTS.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PcInfoAttachmentDto {
    private Long seqNo;
    private String originalFileName;
    private String storedFileName;
    private String remarks;
    private String checklistName;
    private String uploadedBy;
    private String createdDate;
    private boolean active;
    private boolean deleted;
}
