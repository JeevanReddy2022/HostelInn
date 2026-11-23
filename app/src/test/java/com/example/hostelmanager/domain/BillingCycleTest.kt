package com.example.hostelmanager.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class BillingCycleTest {

    private fun date(value: String) = LocalDate.parse(value)

    @Test
    fun `anchor day is read from an iso join date`() {
        assertEquals(15, BillingCycle.anchorDayOf("2026-02-15"))
        assertEquals(1, BillingCycle.anchorDayOf("2026-02-01"))
        assertEquals(31, BillingCycle.anchorDayOf("2026-01-31"))
    }

    @Test
    fun `anchor day falls back to 1 for malformed dates`() {
        assertEquals(1, BillingCycle.anchorDayOf(""))
        assertEquals(1, BillingCycle.anchorDayOf("2026-02"))
        assertEquals(1, BillingCycle.anchorDayOf("2026-02-ab"))
    }

    @Test
    fun `cycle starts on anchor day when it has already passed`() {
        assertEquals(
            date("2026-11-15"),
            BillingCycle.cycleStart(date("2026-11-20"), anchorDay = 15)
        )
    }

    @Test
    fun `cycle starts on anchor day when today is the anchor day`() {
        assertEquals(
            date("2026-11-15"),
            BillingCycle.cycleStart(date("2026-11-15"), anchorDay = 15)
        )
    }

    @Test
    fun `cycle rolls back a month before the anchor day`() {
        assertEquals(
            date("2026-10-15"),
            BillingCycle.cycleStart(date("2026-11-10"), anchorDay = 15)
        )
    }

    @Test
    fun `anchor 31 uses the last day of a short month`() {
        // February has no 31st, so the cycle starts on its last day.
        assertEquals(
            date("2026-02-28"),
            BillingCycle.cycleStart(date("2026-02-28"), anchorDay = 31)
        )
        // Before Feb 28 we fall back to January's cycle start (Jan 31).
        assertEquals(
            date("2026-01-31"),
            BillingCycle.cycleStart(date("2026-02-10"), anchorDay = 31)
        )
    }

    @Test
    fun `cycle end is the day before the next cycle starts`() {
        assertEquals(
            date("2026-12-14"),
            BillingCycle.cycleEnd(date("2026-11-20"), anchorDay = 15)
        )
        assertEquals(
            date("2026-12-29"),
            BillingCycle.cycleEnd(date("2026-12-15"), anchorDay = 31)
        )
    }

    @Test
    fun `anchor day is clamped to a valid range`() {
        assertEquals(
            date("2026-11-30"),
            BillingCycle.cycleStart(date("2026-11-30"), anchorDay = 99)
        )
    }
}
