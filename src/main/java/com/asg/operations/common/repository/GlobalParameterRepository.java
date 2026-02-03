package com.asg.operations.common.repository;

import com.asg.operations.common.entity.GlobalParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GlobalParameterRepository extends JpaRepository<GlobalParameterEntity, Long> {
    @Query("SELECT g.parameterValue FROM GlobalParameterEntity g WHERE g.parameterName = :parameterName")
    Optional<String> findParameterValueByName(@Param("parameterName") String parameterName);
}
