package com.asg.operations.projectjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.asg.operations.projectjob.entity.FFManifestHdr;

@Repository
public interface FFManifestHdrRepository extends JpaRepository<FFManifestHdr, Long> {
}