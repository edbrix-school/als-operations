package com.asg.operations.shipprincipal.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "SHIP_PRINCIPAL_MASTER_PYMT_DTL")
@IdClass(ShipPrincipalMasterDtlId.class)
public class ShipPrincipalMasterPymtDtl extends BaseEntity {

    @Id
    @Column(name = "PRINCIPAL_POID", nullable = false)
    private Long principalPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;

    @Column(name = "BANK", length = 100)
    private String bank;

    @Column(name = "SWIFT_CODE", length = 50)
    private String swiftCode;

    @Column(name = "ACCOUNT_NUMBER", length = 50)
    private String accountNumber;

    @Column(name = "REMARKS", length = 250)
    private String remarks;

    @Column(name = "TYPE", length = 30)
    private String type;

    @Column(name = "BENEFICIARY_NAME", length = 50)
    private String beneficiaryName;

    @Column(name = "ADDRESS", length = 250)
    private String address;

    @Column(name = "BANK_ADDRESS", length = 250)
    private String bankAddress;

    @Column(name = "BANK_SWIFT_CODE", length = 50)
    private String bankSwiftCode;

    @Column(name = "IBAN", length = 100)
    private String iban;

    @Column(name = "INTERMEDIARY_BANK", length = 100)
    private String intermediaryBank;

    @Column(name = "BENEFICIARY_ID", length = 25)
    private String beneficiaryId;

    @Column(name = "INTERMEDIARY_ACCT", length = 50)
    private String intermediaryAcct;

    @Column(name = "INTERMEDIARY_OTH", length = 50)
    private String intermediaryOth;

    @Column(name = "SPECIAL_INSTRUCTION", length = 250)
    private String specialInstruction;

    @Column(name = "INTERMEDIARY_COUNTRY_POID")
    private Long intermediaryCountryPoid;

    @Column(name = "BENEFICIARY_COUNTRY")
    private Long beneficiaryCountry;

    @Column(name = "ACTIVE", length = 1)
    private String active = "N";

    @Column(name = "DEFAULTS", length = 1)
    private String defaults = "N";
}
