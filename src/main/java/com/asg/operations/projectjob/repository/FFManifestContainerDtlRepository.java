package com.asg.operations.projectjob.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.asg.operations.projectjob.entity.FFManifestContainerDtl;
import com.asg.operations.projectjob.entity.FFManifestContainerDtlId;

@Repository
public interface FFManifestContainerDtlRepository
		extends JpaRepository<FFManifestContainerDtl, FFManifestContainerDtlId> {

	List<FFManifestContainerDtl> findByTransactionPoid(Long transactionPoid);

	List<FFManifestContainerDtl> findByTransactionPoidIn(List<Long> transactionPoids);

	@Query("SELECT COALESCE(MAX(d.detRowId), 0) FROM FFManifestContainerDtl d WHERE d.transactionPoid = :transactionPoid")
	Long getMaxDetRowId(Long transactionPoid);

	Optional<FFManifestContainerDtl> findByTransactionPoidAndDetRowId(Long transactionPoid, Long detRowId);

	void deleteByTransactionPoidAndDetRowIdIn(Long transactionPoid, List<Long> toDelete);
}
