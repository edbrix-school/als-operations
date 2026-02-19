package com.asg.operations.projectjob.repository;

import com.asg.operations.projectjob.dto.ProjectLoadInJobsProcResponse;

public interface ProjectJobStoredProcRepository {

	ProjectLoadInJobsProcResponse callProjectsLoadInJobsProc(Long transactionPoid);
	String callReopenJobProc(Long loginUserPoid, Long docKeyPoid);
}
