package com.asg.operations.projects.key;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.Column;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FFProjectsChargesDtlId implements Serializable {

    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;
}
