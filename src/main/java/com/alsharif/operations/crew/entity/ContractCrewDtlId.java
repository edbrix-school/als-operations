package com.alsharif.operations.crew.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key class for ContractCrewDtl
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ContractCrewDtlId implements Serializable {

    @Column(name = "CREW_POID", nullable = false)
    private Long crewPoid;
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;


}

