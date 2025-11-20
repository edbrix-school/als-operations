package com.alsharif.operations.shipprincipal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "GLOBAL_ADDRESS_DETAILS")
@Getter
@Setter
public class AddressDetails {
    @Id
    @Column(name = "ADDRESS_POID", nullable = false)
    private Long addressPoid;

    @Column(name = "ADDRESS_MASTER_POID", nullable = false)
    private Long addressMasterPoid;

    @Column(name = "ADDRESS_TYPE", length = 20)
    private String addressType;

    @Column(name = "OFF_TEL1", length = 30)
    private String offTel1;

    @Column(name = "OFF_TEL2", length = 30)
    private String offTel2;

    @Column(name = "CONTACT_PERSON", length = 50)
    private String contactPerson;

    @Column(name = "DESIGNATION", length = 50)
    private String designation;

    @Column(name = "MOBILE", length = 30)
    private String mobile;

    @Column(name = "FAX", length = 30)
    private String fax;

    @Column(name = "EMAIL1", length = 110)
    private String email1;

    @Column(name = "EMAIL2", length = 110)
    private String email2;

    @Column(name = "WEBSITE", length = 500)
    private String website;

    @Column(name = "PO_BOX", length = 30)
    private String poBox;

    @Column(name = "OFF_NO", length = 100)
    private String offNo;

    @Column(name = "BLDG", length = 100)
    private String bldg;

    @Column(name = "ROAD", length = 100)
    private String road;

    @Column(name = "AREA_CITY", length = 100)
    private String areaCity;

    @Column(name = "STATE", length = 100)
    private String state;

    @Column(name = "COUNTRY_POID")
    private Long countryPoid;

    @Column(name = "LAND_MARK", length = 250)
    private String landMark;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "OLD_ACCNO_REF", length = 20)
    private String oldAccnoRef;

    @Column(name = "OLD_GL_ACCTNO", length = 20)
    private String oldGlAcctNo;

    @Column(name = "OLD_GL_ACCTNO_SUPPLIER", length = 20)
    private String oldGlAcctNoSupplier;

    @Column(name = "VERIFIED", length = 1)
    private String verified;

    @Column(name = "VERIFIED_BY", length = 100)
    private String verifiedBy;

    @Column(name = "VERIFIED_DATE")
    private LocalDate verifiedDate;

    @Column(name = "CITY", length = 100)
    private String city;

    @Column(name = "WHATSAPP_NO", length = 30)
    private String whatsappNo;

    @Column(name = "LINKEDIN", length = 250)
    private String linkedIn;

    @Column(name = "INSTAGRAM", length = 250)
    private String instagram;

    @Column(name = "FACEBOOK", length = 250)
    private String facebook;
}
