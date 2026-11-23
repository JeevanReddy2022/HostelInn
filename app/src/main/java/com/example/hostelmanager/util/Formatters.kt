package com.example.hostelmanager.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

private const val RUPEE = "\u20B9"

/**
 * Formats a paise amount as Indian rupees without decimals.
 * Examples: 0 -> "₹0", 123400 -> "₹1,234", -50000 -> "-₹500".
 */
fun formatRupees(amountPaise: Long): String {
    val rupees = abs(amountPaise) / 100
    val formatted = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(rupees)
    return if (amountPaise < 0) "-$RUPEE$formatted" else "$RUPEE$formatted"
}

/** "2026-11-24" -> "24 Nov 2026". Returns the input unchanged if it cannot be parsed. */
fun formatDisplayDate(iso: String): String {
    val date = runCatching { LocalDate.parse(iso) }.getOrNull() ?: return iso
    val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    return "${date.dayOfMonth} $month ${date.year}"
}
