package com.alsharif.operations.portactivitiesmaster.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortActivityMasterRequest {
    @NotBlank(message = "Port activity type name is mandatory")
    @Size(max = 200, message = "Port activity type name cannot exceed 200 characters")
    private String portActivityTypeName;

    @Size(max = 200, message = "Port activity type name2 cannot exceed 200 characters")
    private String portActivityTypeName2;

    @Size(max = 1, message = "Active flag must be single character")
    private String active;

    private Long seqno;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;
}