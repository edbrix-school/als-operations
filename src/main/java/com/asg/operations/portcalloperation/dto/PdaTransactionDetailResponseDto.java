package com.asg.operations.portcalloperation.dto;

import com.asg.operations.commonlov.dto.LovItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response payload for the PDA transaction detail lookup.
 * <p>
 * The prefixed {@code Poid / Code / Desc} columns from the query are grouped into
 * {@link LovItem} (poid, code, description) so they can be consumed directly as LOV values.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdaTransactionDetailResponseDto {

    private String pdaDocRef;

    // LOV details ({prefix}Poid, {prefix}Code, {prefix}Desc)
    private LovItem job;
    private LovItem principal;
    private LovItem vessel;
    private LovItem vesselType;
    private LovItem portOfCall;
    private LovItem previousPort;
    private LovItem nextPort;
    private LovItem comodity;
    private LovItem product;
    private LovItem typeOfCall;

    private BigDecimal grt;
    private BigDecimal nrt;
    private BigDecimal dwt;
    private String imoNumber;
    private BigDecimal beam;
    private BigDecimal loa;
    private String flagOfCountry;
    private String voyageNo;

    private LocalDate eta;
    private LocalDate etd;
    private String operator;
    private List<String> berths;

    private Long qntyInCBM;
    private Long qntyInMT;
    private Long qntyInNo;

    private String type;
    private Long port;
}