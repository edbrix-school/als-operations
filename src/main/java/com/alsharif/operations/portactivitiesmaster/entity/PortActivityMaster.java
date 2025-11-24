package com.alsharif.operations.portactivitiesmaster.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "OPS_PORT_ACTIVITY_MASTER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PortActivityMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PORT_ACTIVITY_TYPE_POID")
    private Long portActivityTypePoid;

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

    @CreatedBy
    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @LastModifiedBy
    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "DELETED", length = 1)
    @Builder.Default
    private String deleted = "N";

    @Column(name = "REMARKS", length = 500)
    private String remarks;
}
