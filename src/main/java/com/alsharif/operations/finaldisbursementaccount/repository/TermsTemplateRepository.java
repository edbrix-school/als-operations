package com.alsharif.operations.finaldisbursementaccount.repository;

import com.alsharif.operations.finaldisbursementaccount.entity.TermsTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermsTemplateRepository extends JpaRepository<TermsTemplateEntity, Long> {
    boolean existsByTermsPoid(Long termsPoid);
}
