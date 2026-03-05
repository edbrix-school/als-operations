package com.asg.operations.pdaentryform.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "PDA_ENTRY_VEHICLE_DTL")
@IdClass(PdaEntryVehicleDtlId.class)
public class PdaEntryVehicleDtl extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @NotNull
    private Long transactionPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    @NotNull
    private Long detRowId;

    @Column(name = "VESSEL_NAME", length = 100)
    @Size(max = 100)
    private String vesselName;

    @Column(name = "VOYAGE_REF", length = 50)
    @Size(max = 50)
    private String voyageRef;

    @Column(name = "IN_OUT_MODE", length = 5)
    @Size(max = 5)
    private String inOutMode;

    @Column(name = "VEHICLE_MODEL", length = 100)
    @Size(max = 100)
    private String vehicleModel;

    @Column(name = "VIN_NUMBER", length = 100)
    @Size(max = 100)
    private String vinNumber;

    @Column(name = "SCAN_DATE")
    private LocalDateTime scanDate;

    @Column(name = "DAMAGE", length = 5)
    @Size(max = 5)
    private String damage;

    @Column(name = "STATUS", length = 50)
    @Size(max = 50)
    private String status;

    @Column(name = "PUBLISH_FOR_IMPORT", length = 1)
    @Size(max = 1)
    private String publishForImport;

    @Column(name = "REMARKS", length = 500)
    @Size(max = 500)
    private String remarks;
}

