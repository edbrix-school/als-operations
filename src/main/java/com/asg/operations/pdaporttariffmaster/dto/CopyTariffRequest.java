package com.asg.operations.pdaporttariffmaster.dto;

import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CopyTariffRequest {

    @NotNull(message = "New period from date is mandatory")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate newPeriodFrom;

    @NotNull(message = "New period to date is mandatory")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate newPeriodTo;

}
