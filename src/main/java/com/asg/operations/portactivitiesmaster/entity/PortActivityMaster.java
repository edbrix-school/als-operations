package com.asg.operations.portactivitiesmaster.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "OPS_PORT_ACTIVITY_MASTER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortActivityMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PORT_ACTIVITY_TYPE_POID")
    private Long portActivityTypePoid;

    @AuditIgnore
    @Column(name = "GROUP_POID", nullable = false)
    private Long groupPoid;

    @Column(name = "PORT_ACTIVITY_TYPE_CODE", nullable = false, length = 50)
    private String portActivityTypeCode;

    @Column(name = "PORT_ACTIVITY_TYPE_NAME", nullable = false, length = 300)
    private String portActivityTypeName;

    @Column(name = "PORT_ACTIVITY_TYPE_NAME2", length = 300)
    private String portActivityTypeName2;

    @Column(name = "ACTIVE", length = 1)
    @Builder.Default
    private String active = "Y";

    @Column(name = "SEQNO")
    private Long seqno;

    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    @Builder.Default
    private String deleted = "N";

    @Column(name = "REMARKS", length = 500)
    private String remarks;
}
