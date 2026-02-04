package com.asg.operations.salesquotationprojects.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDetailsDto {
    private BigDecimal addressPoid;
    private String addressName;
    private String contactPerson;
    private String email;
    private String telephone;
    private String mobile;
    private String poBox;
    private String whatsAppNumber;
}