package com.asg.operations.pdaporttariffmaster.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DateOverlapValidator {

    /**
     * Check if two date ranges overlap
     * Two ranges overlap if: start1 <= end2 AND end1 >= start2
     */
    public boolean isOverlapping(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    /**
     * Check if any items in two lists overlap (used for port/vessel type overlap)
     */
    public boolean hasOverlappingItems(List<String> list1, List<String> list2) {
        if (list1 == null || list2 == null || list1.isEmpty() || list2.isEmpty()) {
            return false;
        }
        return list1.stream().anyMatch(list2::contains);
    }

    /**
     * Check if two quantity ranges overlap
     */
    public boolean isQuantityRangeOverlapping(java.math.BigDecimal from1, java.math.BigDecimal to1,
                                              java.math.BigDecimal from2, java.math.BigDecimal to2) {
        if (from1 == null || to1 == null || from2 == null || to2 == null) {
            return false;
        }
        return from1.compareTo(to2) <= 0 && to1.compareTo(from2) >= 0;
    }
}


