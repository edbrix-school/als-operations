package com.asg.operations.projectjob.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.asg.operations.projectjob.entity.FFManifestBayanDtl;
import com.asg.operations.projectjob.entity.FFManifestBayanDtlId;

@Repository
public interface FFManifestBayanDtlRepository extends JpaRepository<FFManifestBayanDtl, FFManifestBayanDtlId> {

	List<FFManifestBayanDtl> findByTransactionPoid(Long transactionPoid);

	Optional<FFManifestBayanDtl> findByTransactionPoidAndDetRowId(Long transactionPoid, Long detRowId);

	void deleteByTransactionPoidAndDetRowIdIn(Long transactionPoid, List<Long> toDelete);

	@Query("SELECT COALESCE(MAX(d.detRowId), 0) FROM FFManifestBayanDtl d WHERE d.transactionPoid = :transactionPoid")
    Long getMaxDetRowId(Long transactionPoid);
}
