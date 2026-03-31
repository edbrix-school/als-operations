package com.asg.operations.portcalloperation.dto;

import lombok.*;

/**
 * DTO for attachment info returned from common attachment service or stored in PC_INFO_ATTACHMENTS.
 */
@Getter
@Setter
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
