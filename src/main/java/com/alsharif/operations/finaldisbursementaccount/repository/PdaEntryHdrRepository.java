package com.alsharif.operations.finaldisbursementaccount.repository;

import com.alsharif.operations.finaldisbursementaccount.entity.PdaEntryHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PdaEntryHdrRepository extends JpaRepository<PdaEntryHdr, Long> {

    Optional<PdaEntryHdr> findByDocRef(String docRef);

    Optional<PdaEntryHdr> findByTransactionPoid(Long transactionPoid);
}
