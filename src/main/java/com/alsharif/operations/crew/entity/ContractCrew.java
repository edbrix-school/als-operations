package com.alsharif.operations.crew.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity class for CONTRACT_CREW master table
 * Represents crew master information
 */
@Data
@Entity
@Table(name = "CONTRACT_CREW_MASTER")
public class ContractCrew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CREW_POID")
    private Long crewPoid;


    @Column(name = "CREW_NAME", length = 250, nullable = false)
    @NotBlank
    @Size(max = 250)
    private String crewName;

    @Column(name = "CREW_NATION_POID", nullable = false)
    @NotNull
    private Long crewNationPoid;

    @Column(name = "CREW_CDC_NUMBER", length = 50)
    @Size(max = 50)
    private String crewCdcNumber;

    @Column(name = "CREW_COMPANY", length = 150, nullable = false)
    @NotBlank
    @Size(max = 150)
    private String crewCompany;

    @Column(name = "CREW_DESIGNATION", length = 150, nullable = false)
    @NotBlank
    @Size(max = 150)
    private String crewDesignation;

    @Column(name = "CREW_PASSPORT_NUMBER", length = 50, nullable = false)
    @NotBlank
    @Size(max = 50)
    private String crewPassportNumber;

    @Column(name = "CREW_PASSPORT_ISS_DATE", nullable = false)
    @NotNull
    private LocalDate crewPassportIssueDate;

    @Column(name = "CREW_PASSPORT_EXP_DATE", nullable = false)
    @NotNull
    private LocalDate crewPassportExpiryDate;

    @Column(name = "CREW_PASSPORT_ISS_PLACE", length = 80)
    @Size(max = 80)
    private String crewPassportIssuePlace;

    @Column(name = "REMARKS", length = 450)
    @Size(max = 450)
    private String remarks;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "ACTIVE", length = 1)
    @Size(max = 1)
    private String active;

    @Column(name = "SEQNO")
    private Long seqno;

    @Column(name = "DELETED", length = 1)
    @Size(max = 1)
    private String deleted;

    @Column(name = "CREATED_BY", length = 20)
    @Size(max = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    @Size(max = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;



}

