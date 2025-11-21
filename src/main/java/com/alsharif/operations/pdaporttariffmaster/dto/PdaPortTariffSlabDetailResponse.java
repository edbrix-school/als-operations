package com.alsharif.operations.pdaporttariffmaster.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
public class PdaPortTariffSlabDetailResponse {

    private Long detRowId;

    private BigDecimal quantityFrom;

    private BigDecimal quantityTo;

    private Long days1;

    private BigDecimal rate1;

    private Long days2;

    private BigDecimal rate2;

    private Long days3;

    private BigDecimal rate3;

    private Long days4;

    private BigDecimal rate4;

    private String callByPort;

    private String remarks;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    private String lastModifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;

}
