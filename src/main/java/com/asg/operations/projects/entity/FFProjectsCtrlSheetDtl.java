package com.asg.operations.projects.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROJECTS_CTRL_SHEET_DTL")
@IdClass(FFProjectsCtrlSheetDtl.FFProjectsCtrlSheetDtlId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FFProjectsCtrlSheetDtl {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @AuditIgnore
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;

    @Column(name = "FREIGHT_TYPE", length = 50)
    private String freightType;

    @Column(name = "FF_JOB_POID")
    private Long jobNoPoid;

    // common
    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "CBM")
    private Double cbm;

    @Column(name = "ETA")
    private LocalDate etaAta;

    @Column(name = "WEIGHT")
    private Double weight;

    //    AIR
    @Column(name = "AF_ORIGIN")
    private Long origin;

    @Column(name = "AF_DESTINATION")
    private Long destination;

    @Column(name = "AF_NO_OF_PACKAGES")
    private Double noOfPackages;

    @Column(name = "AF_CARRIER_POID")
    private Long carrierPoid;

    //  SEA
    @Column(name = "SF_POL")
    private String pol;

    @Column(name = "SF_POD")
    private String pod;

    @Column(name = "SF_LINE_POID")
    private Long line;

    @Column(name = "SAIL_DATE")
    private LocalDate sailDate;

    // AIR and SEA
    @Column(name = "ETD")
    private LocalDate etd;

    @Column(name = "ARRIVAL_DATE")
    private LocalDate arrivalDate;

   // ROAD
    @Column(name = "RF_TRUCK_NUMBER", length = 100)
    private String truckNumber;


    @Column(name = "CREATED_BY", length = 20)
    @AuditIgnore
    private String createdBy;

    @Column(name = "CREATED_DATE")
    @AuditIgnore
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    @AuditIgnore
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    @AuditIgnore
    private LocalDateTime lastModifiedDate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class FFProjectsCtrlSheetDtlId implements Serializable {
        private Long transactionPoid;
        private Long detRowId;
    }
}
