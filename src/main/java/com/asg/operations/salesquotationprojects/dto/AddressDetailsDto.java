package com.asg.operations.salesquotationprojects.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @Email(message = "Invalid email format")
    private String email;

    private String telephone;
    private String mobile;
    private String poBox;
    private String whatsAppNumber;

    @Size(max = 500, message = "Website must not exceed 500 characters")
    @Pattern(regexp = "^$|^(https?://).+", message = "Website must be a valid URL starting with http:// or https://")
    private String website;
}