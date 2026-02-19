package com.asg.operations.projectjob.dto;

/**
 * Common base DTO for all detail records. Used by generic processDetails() to
 * determine operation type.
 */
public interface BaseDetailDto {

	Long getDetRowId();

	void setDetRowId(Long detRowId);

	String getActionType();

	void setActionType(String actionType);

}