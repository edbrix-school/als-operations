package com.asg.operations.common.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "GLOBAL_STATE_MASTER")
public class State extends BaseEntity {

    @Id
    @Column(name = "STATE_POID")
    private Long statePoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "STATE_NAME", length = 100)
    private String stateName;

    @Column(name = "COUNTRY_POID")
    private Long countryPoid;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO")
    private Integer seqNo;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "STATE_REMARK", length = 250)
    private String stateRemark;
}