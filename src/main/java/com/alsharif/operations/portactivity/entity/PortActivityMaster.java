package com.alsharif.operations.portactivity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "OPS_PORT_ACTIVITY_MASTER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortActivityMaster {
    
    @Id
    @Column(name = "PORT_ACTIVITY_TYPE_POID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long portActivityTypePoid;
    
    @Column(name = "GROUP_POID")
    private Long groupPoid;
    
    @Column(name = "PORT_ACTIVITY_TYPE_CODE", length = 50, nullable = false)
    private String portActivityTypeCode;
    
    @Column(name = "PORT_ACTIVITY_TYPE_NAME", length = 300, nullable = false)
    private String portActivityTypeName;
    
    @Column(name = "PORT_ACTIVITY_TYPE_NAME2", length = 300)
    private String portActivityTypeName2;
    
    @Column(name = "ACTIVE", length = 1)
    private String active;
    
    @Column(name = "SEQNO")
    private Long seqno;
    
    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;
    
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
    
    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;
    
    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;
    
    @Column(name = "DELETED", length = 1)
    private String deleted;
    
    @Column(name = "REMARKS", length = 500)
    private String remarks;
}
