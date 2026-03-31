package com.asg.operations.salesquotationprojects.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "FF_AIR_LINE_MASTER",
        uniqueConstraints = {
                @UniqueConstraint(name = "AIR_LINE_MASTER_UK1", columnNames = {"AIRLINE_POID", "AIRLINE_CODE"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirLineMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "airline_seq")
    @SequenceGenerator(name = "airline_seq", sequenceName = "FF_AIR_LINE_MASTER_SEQ", allocationSize = 1)
    @Column(name = "AIRLINE_POID", nullable = false)
    private Long airlinePoid;

    @Column(name = "AIRLINE_CODE", length = 50)
    private String airlineCode;

    @Column(name = "AIRLINE_NAME", length = 100)
    private String airlineName;

    @Column(name = "LINE_NAME2", length = 100)
    private String lineName2;

    @Column(name = "LINE_ADDRESS", length = 500)
    private String lineAddress;

    @Column(name = "COUNTRY_POID")
    private Long countryPoid;

    @Column(name = "AGENCY_STARTED_DATE")
    private LocalDate agencyStartedDate;

    @Column(name = "NEXT_RENEWAL_DATE")
    private LocalDate nextRenewalDate;

    @Column(name = "ACTIVE", length = 1)
    private String active = "Y";

    @Column(name = "SEQNO", precision = 5)
    private Integer seqNo;

    @AuditIgnore
    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @AuditIgnore
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @AuditIgnore
    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @AuditIgnore
    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    private String deleted = "N";

    @Column(name = "AIRLINE_PREFIX")
    private Long airlinePrefix;

    @Column(name = "AIR_LINE_COST_POID", length = 100)
    private String airLineCostPoid;
}
