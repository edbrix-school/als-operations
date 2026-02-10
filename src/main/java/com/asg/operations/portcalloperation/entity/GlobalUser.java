package com.asg.operations.portcalloperation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(
        name = "GLOBAL_USERS",
        uniqueConstraints = {
                @UniqueConstraint(name = "GLOBAL_USERS_UK_USERID", columnNames = "USER_ID")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class GlobalUser {

    @Id
    @Column(name = "USER_POID", nullable = false)
    private Long userPoid;
    @AuditIgnore
    @Column(name = "GROUP_POID", nullable = false)
    private Long groupPoid;

    @Column(name = "USER_ID", nullable = false, length = 20)
    private String userId;

    @Column(name = "USER_NAME", nullable = false, length = 100)
    private String userName;

    @Column(name = "USER_NAME2", length = 100)
    private String userName2;

    @Column(name = "USER_MOBILE", length = 30)
    private String userMobile;

    @Column(name = "USER_EMAIL", nullable = false, length = 50)
    private String userEmail;

    @Column(name = "EXPIRY_DATE")
    @Temporal(TemporalType.DATE)
    private Date expiryDate;

    @Column(name = "USER_LOCKED", length = 1)
    private String userLocked;

    @Column(name = "USER_LOCKED_REASON", length = 100)
    private String userLockedReason;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO")
    private Integer seqNo;
    @AuditIgnore
    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;
    @AuditIgnore
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
    @AuditIgnore
    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;
    @AuditIgnore
    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "RESET_PWD_NEXT_LOGIN", length = 1)
    private String resetPwdNextLogin;

    @Column(name = "PWD", length = 256)
    private String pwd;

    @Column(name = "DEFAULT_COMPANY_POID")
    private Long defaultCompanyPoid;
    @AuditIgnore
    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "SECURITY_QUESTION1", length = 100)
    private String securityQuestion1;

    @Column(name = "SECURITY_ANSWER1", length = 100)
    private String securityAnswer1;

    @Column(name = "SECURITY_QUESTION2", length = 100)
    private String securityQuestion2;

    @Column(name = "SECURITY_ANSWER2", length = 100)
    private String securityAnswer2;

    @Column(name = "DEFAULT_LOCATION_POID")
    private Long defaultLocationPoid;

    @Column(name = "AUTHORIZATION_LEVEL")
    private Integer authorizationLevel;

    @Column(name = "CARD_SWAP_ID", length = 50)
    private String cardSwapId;

    @Column(name = "IS_CLIENT", length = 1)
    private String isClient;

    @Column(name = "IS_AGENT", length = 1)
    private String isAgent;

    @Column(name = "ALLOW_OFFICE365_LOGIN", length = 1)
    private String allowOffice365Login;
}
