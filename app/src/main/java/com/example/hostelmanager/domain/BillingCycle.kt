package com.example.hostelmanager.domain

import java.time.LocalDate

/**
 * Billing-cycle math shared by resident onboarding, room transfer and the
 * payments screens.
 *
 * A resident's cycle restarts on [anchorDay] every month. When a month does not
 * have that day (for example 31 in February) the cycle starts on the month's
 * last day instead.
 */
object BillingCycle {

    /** Extracts the anchor day (1-31) from an ISO "YYYY-MM-DD" date. */
    fun anchorDayOf(isoDate: String): Int {
        val day = isoDate
            .takeIf { it.length >= 10 }
            ?.substring(8, 10)
            ?.toIntOrNull()
            ?: return 1
        return day.coerceIn(1, 31)
    }

    /** Start of the billing cycle that contains [referenceDate]. */
    fun cycleStart(referenceDate: LocalDate, anchorDay: Int): LocalDate {
        val day = anchorDay.coerceIn(1, 31)
        var start = referenceDate.withDayOfMonth(day.coerceAtMost(referenceDate.lengthOfMonth()))
        if (start.isAfter(referenceDate)) {
            val previousMonth = referenceDate.minusMonths(1)
            start = previousMonth.withDayOfMonth(day.coerceAtMost(previousMonth.lengthOfMonth()))
        }
        return start
    }

    /** Last day of the billing cycle that contains [referenceDate]. */
    fun cycleEnd(referenceDate: LocalDate, anchorDay: Int): LocalDate =
        cycleStart(referenceDate, anchorDay).plusMonths(1).minusDays(1)
}
