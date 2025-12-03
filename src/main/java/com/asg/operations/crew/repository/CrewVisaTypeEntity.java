package com.asg.operations.crew.repository;

import jakarta.persistence.*;

/**
 * Entity class for CREW_VISA_TYPE table
 */
@Entity
@Table(name = "CREW_VISA_TYPE")
public class CrewVisaTypeEntity {

    @Id
    @Column(name = "CODE", length = 50)
    private String code;

    @Column(name = "DESCRIPTION", length = 100)
    private String description;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }
}

