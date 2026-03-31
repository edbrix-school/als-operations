package com.asg.operations.salesquotationprojects.repository;

import com.asg.operations.salesquotationprojects.entity.ProjectsHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectsHdrRepository extends JpaRepository<ProjectsHdr, Long> {
    boolean existsByTransactionPoid(Long transactionPoid);

    boolean existsByDocRefIgnoreCase(String projectReference);
}
