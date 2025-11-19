package com.alsharif.operations.group.repository;


import com.alsharif.operations.group.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    boolean existsByGroupPoid(Long groupPoid);
}

