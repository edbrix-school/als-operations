package com.asg.operations.finaldisbursementaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PartyGlResponse {
    private Long glPoid;
    private Integer creditPeriodDays;
}
