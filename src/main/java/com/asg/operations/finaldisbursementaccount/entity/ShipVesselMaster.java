package com.asg.operations.finaldisbursementaccount.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "SHIP_VESSEL_MASTER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipVesselMaster {

    @Id
    @Column(name = "VESSEL_POID")
    private Long vesselPoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "VESSEL_CODE", nullable = false)
    private String vesselCode;

    @Column(name = "VESSEL_NAME", nullable = false)
    private String vesselName;

    @Column(name = "VESSEL_NAME2")
    private String vesselName2;

    @Column(name = "LINE_POID")
    private Long linePoid;

    @Column(name = "OWNER")
    private String owner;

    @Column(name = "AGENT_POID")
    private Long agentPoid;

    @Column(name = "REGISTRATION_NO")
    private String registrationNo;

    @Column(name = "REGISTRATION_DATE")
    private LocalDate registrationDate;

    @Column(name = "COUNTRY_OF_REGISTRATION")
    private String countryOfRegistration;

    @Column(name = "VESSEL_TYPE_POID")
    private Long vesselTypePoid;

    @Column(name = "VESSEL_TYPE_CLASS")
    private String vesselTypeClass;

    @Column(name = "GRT")
    private BigDecimal grt;

    @Column(name = "NRT")
    private BigDecimal nrt;

    @Column(name = "DWT")
    private BigDecimal dwt;

    @Column(name = "VESSEL_LENGTH")
    private BigDecimal vesselLength;

    @Column(name = "BEAM")
    private BigDecimal beam;

    @Column(name = "DRAFT")
    private BigDecimal draft;

    @Column(name = "HATCHES")
    private Integer hatches;

    @Column(name = "BAYHATCH")
    private Integer bayHatch;

    @Column(name = "ACTIVE")
    private String active;

    @Column(name = "SEQNO")
    private Integer seqNo;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY")
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "IMO_NUMBER")
    private String imoNumber;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "LINE_NAME")
    private String lineName;

    @Column(name = "DELETED")
    private String deleted;

    @Column(name = "FLAG_OF_COUNTRY")
    private String flagOfCountry;
}
