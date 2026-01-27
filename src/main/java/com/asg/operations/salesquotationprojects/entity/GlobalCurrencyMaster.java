package com.asg.operations.salesquotationprojects.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "GLOBAL_CURRENCY_MASTER",
        uniqueConstraints = {
                @UniqueConstraint(name = "GLOBAL_CURRENCY_MASTER_UK_CODE", columnNames = {"CURRENCY_CODE"}),
                @UniqueConstraint(name = "GLOBAL_CURRENCY_MASTER_UK_NAME", columnNames = {"CURRENCY_NAME"})
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalCurrencyMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_currency_master_seq")
    @SequenceGenerator(name = "global_currency_master_seq", sequenceName = "GLOBAL_CURRENCY_MASTER_SEQ", allocationSize = 1)
    @Column(name = "CURRENCY_POID", nullable = false)
    private Long currencyPoid;

    @Column(name = "GROUP_POID", nullable = false)
    private Long groupPoid;

    @Column(name = "CURRENCY_CODE", length = 20, nullable = false)
    private String currencyCode;

    @Column(name = "CURRENCY_NAME", length = 100, nullable = false)
    private String currencyName;

    @Column(name = "CURRENCY_NAME2", length = 100)
    private String currencyName2;

    @Column(name = "CURRENCY_DECIMALS")
    private Integer currencyDecimals;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO", precision = 5)
    private Integer seqNo;

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

    @Column(name = "CURRENCY_SHORT_NAME", length = 20)
    private String currencyShortName;

    @Column(name = "COIN_SHORT_NAME", length = 20)
    private String coinShortName;

    @Column(name = "NUMBER_FORMAT_CURRENCY", length = 100)
    private String numberFormatCurrency;
}
