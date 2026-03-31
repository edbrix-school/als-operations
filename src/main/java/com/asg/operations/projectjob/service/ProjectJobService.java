package com.asg.operations.projectjob.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.operations.projectjob.dto.ProjectJobRequest;
import com.asg.operations.projectjob.dto.ProjectJobResponse;
import com.asg.operations.projectjob.dto.ProjectLoadInJobsProcResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface ProjectJobService {
    ProjectJobResponse create(ProjectJobRequest request);

    ProjectJobResponse update(Long transactionPoid, ProjectJobRequest request);

    ProjectJobResponse getById(Long transactionPoid);

    void deleteById(Long transactionPoid, Long groupPoid, Long companyPoid, Long userPoid,
                    @Valid DeleteReasonDto deleteReasonDto);

    String reopenJob(Long transactionPoid);

    ProjectLoadInJobsProcResponse loadJobs(Long transactionPoid);

    Map<String, Object> getAllProjectJobsWithFilters(String documentId, FilterRequestDto filters, Pageable pageable, LocalDate periodFrom, LocalDate periodTo);

}
