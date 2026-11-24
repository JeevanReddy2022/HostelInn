package com.example.hostelmanager.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class FormattersTest {

    private val rupee = "\u20B9"

    @Test
    fun `zero formats without a sign`() {
        assertEquals("${rupee}0", formatRupees(0))
        assertEquals("${rupee}0", 0L.paiseToText())
    }

    @Test
    fun `paiseToText converts paise to formatted rupee string`() {
        assertEquals("${rupee}0", 0L.paiseToText())
        assertEquals("${rupee}0", 99L.paiseToText())
        assertEquals("${rupee}1", 100L.paiseToText())
        assertEquals("${rupee}1,500", 150000L.paiseToText())
    }

    @Test
    fun `negative paiseToText converts negative paise to formatted negative rupee string`() {
        assertEquals("-${rupee}1,500", (-150000L).paiseToText())
    }

    @Test
    fun `formatDisplayDate formats iso date`() {
        assertEquals("24 Nov 2026", formatDisplayDate("2026-11-24"))
    }

    @Test
    fun `formatCycle formats start and end iso dates`() {
        assertEquals("24 Nov 2026 – 23 Dec 2026", formatCycle("2026-11-24", "2026-12-23"))
    }

    @Test
    fun `formatMonth formats iso year month string`() {
        assertEquals("March 2026", formatMonth("2026-03"))
    }

    @Test
    fun `toIsoMonth formats YearMonth to iso string`() {
        assertEquals("2026-03", YearMonth.of(2026, 3).toIsoMonth())
    }
}
