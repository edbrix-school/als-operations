package com.asg.operations.vesseltype.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHIP_VESSEL_TYPE_MASTER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VesselType {
    
    @Id
    @Column(name = "VESSEL_TYPE_POID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vesselTypePoid;
    
    @Column(name = "GROUP_POID")
    private Long groupPoid;
    
    @Column(name = "VESSEL_TYPE_CODE", length = 20, nullable = false)
    private String vesselTypeCode;
    
    @Column(name = "VESSEL_TYPE_NAME", length = 100, nullable = false)
    private String vesselTypeName;
    
    @Column(name = "VESSEL_TYPE_NAME2", length = 100)
    private String vesselTypeName2;
    
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
}
