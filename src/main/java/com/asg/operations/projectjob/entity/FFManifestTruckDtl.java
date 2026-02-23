package com.asg.operations.projectjob.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "FF_MANIFEST_TRUCK_DTL")
@IdClass(FFManifestTruckDtlId.class)
public class FFManifestTruckDtl implements BaseDetailEntity {

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

    @Column(name = "BL_AWB_NUMBER")
    @Size(max = 50)
    private String blAwbNumber;

    @Column(name = "BAYAN_NUMBER")
    @Size(max = 50)
    private String bayanNumber;

    @Column(name = "BAYAN_MODE")
    @Size(max = 50)
    private String bayanCode;

    @Column(name = "ETA")
    private LocalDateTime eta;

    @Column(name = "DUTY_AMOUNT")
    private BigDecimal dutyAmount;

    @Column(name = "VAT_AMOUNT")
    private BigDecimal vatAmount;

    @Column(name = "TOTAL_PAID_AMOUNT")
    private BigDecimal totalPaidAmount;

    @Column(name = "EXPIRY_DATE")
    private LocalDateTime expiryDate;

    @Column(name = "SUBMITTED_DATE")
    private LocalDateTime submittedDate;

    @Column(name = "PAYMENT_DATE")
    private LocalDateTime paymentDate;

    @Column(name = "DOCUMENT_STATUS")
    @Size(max = 50)
    private String documentStatus;

    @AuditIgnore
    @Column(name = "CREATED_BY")
    @Size(max = 20)
    private String createdBy;

    @AuditIgnore
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @AuditIgnore
    @Column(name = "LASTMODIFIED_BY")
    @Size(max = 20)
    private String lastModifiedBy;

    @AuditIgnore
    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "TRUCK_NUMBER")
    @Size(max = 100)
    private String truckNumber;

}
