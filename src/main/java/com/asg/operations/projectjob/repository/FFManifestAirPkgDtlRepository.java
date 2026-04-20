package com.asg.operations.projectjob.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.asg.operations.projectjob.entity.FFManifestAirPkgDtl;
import com.asg.operations.projectjob.entity.FFManifestAirPkgDtlId;

@Repository
public interface FFManifestAirPkgDtlRepository extends JpaRepository<FFManifestAirPkgDtl, FFManifestAirPkgDtlId> {

	List<FFManifestAirPkgDtl> findByTransactionPoid(Long transactionPoid);

	Optional<FFManifestAirPkgDtl> findByTransactionPoidAndDetRowId(Long transactionPoid, Long detRowId);

	boolean existsByTransactionPoid(Long transactionPoid);

	List<FFManifestAirPkgDtl> findByTransactionPoidIn(List<Long> transactionPoids);

	@Query("SELECT COALESCE(MAX(d.detRowId), 0) FROM FFManifestAirPkgDtl d WHERE d.transactionPoid = :transactionPoid")
	Long getMaxDetRowId(@Param("transactionPoid") Long transactionPoid);

	void deleteByTransactionPoidAndDetRowIdIn(Long transactionPoid, List<Long> toDelete);
}
