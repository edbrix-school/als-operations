package com.asg.operations.portactivitiesmaster.repository;

import com.asg.operations.portactivitiesmaster.entity.PortActivityMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortActivityMasterRepository extends JpaRepository<PortActivityMaster, Long> {

    @Query("SELECT p FROM PortActivityMaster p WHERE p.groupPoid = :groupPoid AND p.deleted = 'N' " +
            "AND (:code IS NULL OR LOWER(p.portActivityTypeCode) LIKE LOWER(CONCAT('%', :code, '%'))) " +
            "AND (:name IS NULL OR LOWER(p.portActivityTypeName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:active IS NULL OR p.active = :active)")
    Page<PortActivityMaster> findByGroupPoidAndFilters(@Param("groupPoid") Long groupPoid, @Param("code") String code, @Param("name") String name, @Param("active") String active, Pageable pageable);

    Optional<PortActivityMaster> findByPortActivityTypePoidAndGroupPoidAndDeleted(Long portActivityTypePoid, Long groupPoid, String deleted);

    @Query(value = """
            SELECT MAX(TO_NUMBER(SUBSTR(PORT_ACTIVITY_TYPE_CODE, LENGTH(:prefix) + 1)))
            FROM OPS_PORT_ACTIVITY_MASTER
            WHERE PORT_ACTIVITY_TYPE_CODE LIKE :prefix || '%'
              AND GROUP_POID = :groupPoid
            """, nativeQuery = true)
    Integer findMaxCodeSequence(@Param("prefix") String prefix, @Param("groupPoid") Long groupPoid);

}