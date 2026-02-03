package com.asg.operations.shipprincipal.repository;

import com.asg.operations.shipprincipal.entity.AddressDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AddressDetailsRepository extends JpaRepository<AddressDetails, Long> {
    List<AddressDetails> findByAddressMasterPoid(Long addressMasterPoid);

    List<AddressDetails> findByAddressMasterPoidOrderByAddressType(Long poid);

    boolean existsByAddressPoid(BigDecimal addressPoid);

    /**
     * Get address details with address name by addressPoid
     * Returns Object[] with [ADDRESS_POID, DESCRIPTION (ADDRESS_NAME || ', TYPE-' || ADDRESS_TYPE), CONTACT_PERSON, EMAIL1, OFF_TEL1, MOBILE]
     * Single query with JOIN - faster than entity-based approach
     * Supports decimal addressPoid values
     */
    @Query(value = "SELECT AD.ADDRESS_POID, AM.ADDRESS_NAME || ', TYPE-' || AD.ADDRESS_TYPE as DESCRIPTION, AD.CONTACT_PERSON, AD.EMAIL1, AD.OFF_TEL1, AD.MOBILE, AD.PO_BOX, AD.WHATSAPP_NO " +
            "FROM GLOBAL_ADDRESS_DETAILS AD " +
            "INNER JOIN GLOBAL_ADDRESS_MASTER AM ON AD.ADDRESS_MASTER_POID = AM.ADDRESS_MASTER_POID " +
            "WHERE AD.ADDRESS_POID = :addressPoid", nativeQuery = true)
    Optional<Object[]> findAddressDetailsWithNameByAddressPoid(@Param("addressPoid") BigDecimal addressPoid);
}
