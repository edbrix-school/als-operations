package com.alsharif.operations.shipprincipal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "GLOBAL_ADDRESS_DETAILS")
@Getter
@Setter
public class AddressDetails {
    @Id
    @Column(name = "ADDRESS_DETAILS_POID")
    private Long addressDetailsPoid;

    @Column(name = "ADDRESS_MASTER_POID")
    private Long addressMasterPoid;

    @Column(name = "CONTACT_PERSON", length = 100)
    private String contactPerson;

    @Column(name = "TELEPHONE", length = 50)
    private String telephone;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "FAX", length = 50)
    private String fax;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;
}
