package com.asg.operations.common.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "GLOBAL_PARAMETERS")
public class GlobalParameterEntity {

    @Id
    @Column(name = "PARAMETER_POID")
    private Long parameterPoid;

    @AuditIgnore
    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "PARAMETER_NAME")
    private String parameterName;

    @Column(name = "PARAMETER_KEYID_TYPE")
    private String parameterKeyIdType;

    @Column(name = "PARAMETER_KEYID")
    private String parameterKeyId;

    @Column(name = "PARAMETER_VALUE")
    private String parameterValue;

    @Column(name = "PARAMETER_DETAILS")
    private String parameterDetails;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "PARAMETER_LINUX_VALUE")
    private String parameterLinuxValue;

    @Column(name = "PARAMETER_TYPE")
    private String parameterType;
}
