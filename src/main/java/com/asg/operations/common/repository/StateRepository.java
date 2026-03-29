package com.asg.operations.common.repository;

import com.asg.operations.common.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {

    /**
     * Find states by country POID and active status
     */
    List<State> findByCountryPoidAndActive(Long countryPoid, String active);

    /**
     * Find state by country POID and state POID
     */
    State findByCountryPoidAndStatePoid(Long countryPoid, Long statePoid);

    /**
     * Check if states exist for a country
     */
    boolean existsByCountryPoid(Long countryPoid);

    /**
     * Check if a specific state exists for a country
     */
    Boolean existsByCountryPoidAndStatePoid(Long countryPoid, Long statePoid);

    /**
     * Find active states by country POID
     */
    @Query("SELECT s FROM State s WHERE s.countryPoid = :countryPoid AND s.active = 'Y' AND (s.deleted IS NULL OR s.deleted != 'Y') ORDER BY s.seqNo, s.stateName")
    List<State> findActiveStatesByCountry(@Param("countryPoid") Long countryPoid);

    /**
     * Find states by multiple state POIDs
     */
    @Query("SELECT s FROM State s WHERE s.statePoid IN :statePoids AND s.active = 'Y' AND (s.deleted IS NULL OR s.deleted != 'Y')")
    List<State> findByStatePoidIn(@Param("statePoids") List<Long> statePoids);
}