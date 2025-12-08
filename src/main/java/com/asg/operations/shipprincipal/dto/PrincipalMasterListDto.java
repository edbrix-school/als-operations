package com.asg.operations.shipprincipal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Principal Master list view
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrincipalMasterListDto {
    private Long principalPoid;
    private String principalCode;
    private String principalName;
    private String active;
}