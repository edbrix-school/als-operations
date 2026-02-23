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

    @Column(name = "AF_ORIGIN")
    private Long origin;

    @Column(name = "AF_DESTINATION")
    private Long destination;

    @Column(name = "ETD")
    private LocalDate etd;

    @Column(name = "ETA")
    private LocalDate etaAta;

    @Column(name = "ARRIVAL_DATE")
    private LocalDate arrivalDate;

    @Column(name = "AF_NO_OF_PACKAGES")
    private Double noOfPackages;

    @Column(name = "WEIGHT")
    private Double weight;

    @Column(name = "CBM")
    private Double cbm;

    @Column(name = "AF_CARRIER_POID")
    private Long carrierCode;

    @Column(name = "SF_LINE_POID")
    private Long line;

    @Column(name = "RF_TRUCK_NUMBER", length = 100)
    private String truckNumber;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "SAIL_DATE")
    private LocalDate sailDate;

//    @Column(name = "JOB_STATUS", length = 50)
//    private String jobStatus;

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
