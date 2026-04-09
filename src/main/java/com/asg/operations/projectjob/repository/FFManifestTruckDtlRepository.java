package com.asg.operations.projectjob.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.asg.operations.projectjob.entity.FFManifestTruckDtl;
import com.asg.operations.projectjob.entity.FFManifestTruckDtlId;

@Repository
public interface FFManifestTruckDtlRepository extends JpaRepository<FFManifestTruckDtl, FFManifestTruckDtlId> {

	List<FFManifestTruckDtl> findByTransactionPoid(Long transactionPoid);

	Optional<FFManifestTruckDtl> findByTransactionPoidAndDetRowId(Long transactionPoid, Long detRowId);

	void deleteByTransactionPoidAndDetRowIdIn(Long transactionPoid, List<Long> toDelete);

	List<FFManifestTruckDtl> findByTransactionPoidIn(List<Long> transactionPoids);

	@Query("SELECT COALESCE(MAX(d.detRowId), 0) FROM FFManifestTruckDtl d WHERE d.transactionPoid = :transactionPoid")
	Long getMaxDetRowId(@Param("transactionPoid") Long transactionPoid);
}
