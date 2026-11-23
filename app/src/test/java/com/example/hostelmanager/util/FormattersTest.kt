package com.example.hostelmanager.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    private val rupee = "\u20B9"

    @Test
    fun `zero formats without a sign`() {
        assertEquals("${rupee}0", formatRupees(0))
    }

    @Test
    fun `paise are converted to whole rupees`() {
        assertEquals("${rupee}500", formatRupees(50_000))
        assertEquals("${rupee}1,234", formatRupees(123_400))
    }

    @Test
    fun `sub-rupee amounts truncate like the old screens did`() {
        assertEquals("${rupee}0", formatRupees(99))
    }

    @Test
    fun `negative amounts keep their sign`() {
        assertEquals("-${rupee}500", formatRupees(-50_000))
        assertEquals("-${rupee}1,234", formatRupees(-123_400))
    }
}
