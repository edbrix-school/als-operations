package com.asg.operations.portcalloperation.dto;

import com.asg.operations.portcallreport.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCallOperationActProgDetailDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long emailPoid;

    @NotBlank(message = "Cargo is required")
    @Size(max = 300, message = "Cargo should not exceed 300 characters")
    private String cargo;

    @NotNull(message = "Progress Date Time is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime progressDateTime;

    @NotNull(message = "Progress Quantity is required")
    private BigDecimal progressQty;

    @NotBlank(message = "Progress Status is required")
    @Size(max = 100, message = "Progress Status should not exceed 100 characters")
    private String progressStatus;

    @NotNull(message = "Balance Quantity is required")
    private BigDecimal balanceQty;

    @NotNull(message = "Unit POID is required")
    private Long unitPoid;
    private BigDecimal ratePerHr;

    @NotNull(message = "ETC is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime etc;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate estBlDate;

    private ActionType actionType;
}
