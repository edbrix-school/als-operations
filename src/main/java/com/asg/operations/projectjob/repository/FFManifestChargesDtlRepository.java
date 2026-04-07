package com.asg.operations.projectjob.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.asg.operations.projectjob.entity.FFManifestChargesDtl;
import com.asg.operations.projectjob.entity.FFManifestChargesDtlId;

@Repository
public interface FFManifestChargesDtlRepository extends JpaRepository<FFManifestChargesDtl, FFManifestChargesDtlId> {

	List<FFManifestChargesDtl> findByTransactionPoid(Long transactionPoid);

	@Query("SELECT COALESCE(MAX(d.detRowId), 0) FROM FFManifestChargesDtl d WHERE d.transactionPoid = :transactionPoid")
	Long getMaxDetRowId(@Param("transactionPoid")Long transactionPoid);

	void deleteByTransactionPoidAndDetRowIdIn(Long transactionPoid, List<Long> toDelete);

	Optional<FFManifestChargesDtl> findByTransactionPoidAndDetRowId(Long transactionPoid, Long detRowId);

	List<FFManifestChargesDtl> findByTransactionPoidIn(List<Long> transactionPoids);
}
