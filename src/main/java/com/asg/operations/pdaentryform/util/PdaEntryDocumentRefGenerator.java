package com.asg.operations.pdaentryform.util;

import com.asg.operations.pdaentryform.repository.PdaEntryHdrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Year;

/**
 * Utility class for generating PDA Entry document references
 * Format: PDA-YYYY-NNNNN (e.g., PDA-2024-00001)
 */
@Component
public class PdaEntryDocumentRefGenerator {

    private final PdaEntryHdrRepository entryHdrRepository;

    @Autowired
    public PdaEntryDocumentRefGenerator(PdaEntryHdrRepository entryHdrRepository) {
        this.entryHdrRepository = entryHdrRepository;
    }

    /**
     * Generate document reference in format: PDA-YYYY-NNNNN
     * Where YYYY is the current year and NNNNN is a sequential number
     */
    public String generateDocRef(BigDecimal groupPoid) {
        String year = String.valueOf(Year.now().getValue());
        String prefix = "PDA-" + year + "-";
        int maxSeq = findMaxSequenceForYear(prefix, groupPoid);
        int nextSeq = maxSeq + 1;
        return prefix + String.format("%05d", nextSeq) + System.nanoTime() % 1000;
    }

    /**
     * Find the maximum sequence number for the given year prefix and group POID
     *
     * @param prefix The prefix (e.g., "PDA-2024-")
     * @param groupPoid Group POID for multi-tenancy
     * @return Maximum sequence number found, or 0 if none exists
     */
    private int findMaxSequenceForYear(String prefix, BigDecimal groupPoid) {
        Integer maxSeq = entryHdrRepository.findMaxSequenceByPrefixAndGroup(prefix, groupPoid);
        return maxSeq != null ? maxSeq : 0;
    }
}

