package com.example.hostelmanager.domain

import com.example.hostelmanager.data.Payment
import com.example.hostelmanager.data.Stay
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Running-balance math for one stay.
 *
 * Balance = money owed so far − money paid − money deliberately written off.
 *  - Monthly stays owe one full rent per elapsed billing cycle (arrears carry over).
 *  - Daily stays owe dailyRent per day stayed (both end dates included).
 *  - A negative balance is a CREDIT (advance payment) that offsets future rent.
 *
 * Pure functions only — no database, no flows — so it is fully unit-testable.
 */
object Balance {

    fun parseDateOrNull(iso: String?): LocalDate? =
        iso?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    /** The date the balance should be evaluated at: checkout day, or today. */
    fun referenceDateFor(stay: Stay, today: LocalDate): LocalDate =
        parseDateOrNull(stay.checkoutDate) ?: today

    /** Inclusive day count, e.g. join 1st + ref 3rd = 3 days. Never below 1. */
    fun daysStayed(joinDate: String, referenceDate: LocalDate): Long {
        val join = parseDateOrNull(joinDate) ?: return 1L
        return (ChronoUnit.DAYS.between(join, referenceDate) + 1).coerceAtLeast(1L)
    }

    /**
     * How many billing cycles have started so far (join cycle included).
     * Uses YearMonth distance between cycle starts so anchor clamping
     * (31st → 28th in February) can never miscount a month.
     */
    fun cyclesOwed(joinDate: String, referenceDate: LocalDate, anchorDay: Int): Int {
        val join = parseDateOrNull(joinDate) ?: return 1
        val firstStart = BillingCycle.cycleStart(join, anchorDay)
        val currentStart = BillingCycle.cycleStart(referenceDate, anchorDay)
        if (!currentStart.isAfter(firstStart)) return 1
        val months = (currentStart.year - firstStart.year) * 12 +
                (currentStart.monthValue - firstStart.monthValue)
        return (months + 1).coerceAtLeast(1)
    }

    /** Total rent accrued for this stay as of [referenceDate], in paise. */
    fun owedPaise(stay: Stay, referenceDate: LocalDate): Long {
        return if (stay.dailyRent > 0) {
            daysStayed(stay.joinDate, referenceDate) * stay.dailyRent
        } else {
            cyclesOwed(stay.joinDate, referenceDate, stay.billingAnchorDay) * stay.monthlyRent
        }
    }

    fun paidPaise(payments: List<Payment>): Long = payments.sumOf { it.amountPaise }

    fun waivedPaise(payments: List<Payment>): Long = payments.sumOf { it.waivedPaise }

    /**
     * The one number everything should show.
     *  > 0  → resident still owes this
     *  = 0  → settled (paid or written off)
     *  < 0  → credit in hand (advance payment)
     */
    fun balancePaise(stay: Stay, payments: List<Payment>, referenceDate: LocalDate): Long =
        owedPaise(stay, referenceDate) - paidPaise(payments) - waivedPaise(payments)
}