package com.asg.operations.projectjob.entity;

import java.time.LocalDateTime;

public interface BaseDetailEntity {

    void setDetRowId(Long detRowId);

    void setCreatedBy(String createdBy);

    void setCreatedDate(LocalDateTime createdDate);

    void setLastModifiedBy(String lastModifiedBy);

    void setLastModifiedDate(LocalDateTime lastModifiedDate);
}
