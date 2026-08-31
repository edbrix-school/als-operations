package com.asg.operations.portcalloperation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "OPS_PC_HUSBANDRY_CREW_DTL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PortCallOperationHusbandryCrewDtlId.class)
public class PortCallOperationHusbandryCrewDtl extends BaseEntity {

    @AuditIgnore
    @Id
    @Column(name = "TRANSACTION_POID")
    private Long transactionPoid;

    @AuditIgnore
    @Id
    @Column(name = "DET_ROW_ID")
    private Long detRowId;

    @Column(name = "CREW_NAME", length = 1000)
    private String crewName;

    @Column(name = "CREW_GENDER_POID")
    private Long crewGenderPoid;

    @Column(name = "CREW_NATIONALITY_POID")
    private Long crewNationalityPoid;

    @Column(name = "CREW_PPT_NUMBER", length = 300)
    private String crewPptNumber;

    @Column(name = "CREW_SEAMAN_NO", length = 300)
    private String crewSeamanNo;

    @Column(name = "CREW_RANK", length = 100)
    private String crewRank;

    @Column(name = "CREW_ATTACHMENTS", length = 4000)
    private String crewAttachments;

    @Column(name = "CREW_SIGN_STATUS", length = 50)
    private String crewSignStatus;
}
