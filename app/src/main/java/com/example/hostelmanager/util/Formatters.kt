package com.example.hostelmanager.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

private const val RUPEE = "\u20B9"

/**
 * Formats a paise amount as Indian rupees without decimals.
 * Examples: 0 -> "₹0", 123400 -> "₹1,234", -50000 -> "-₹500".
 * Input unit: paise.
 */
fun formatRupees(amountPaise: Long): String {
    val rupees = abs(amountPaise).paiseToRupees()
    val formatted = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(rupees)
    return if (amountPaise < 0) "-$RUPEE$formatted" else "$RUPEE$formatted"
}

/** Formats a paise amount as Indian Rupee text (e.g., 150000L -> "₹1,500"). Input unit: paise. */
fun Long.paiseToText(): String = formatRupees(this)

/** "2026-11-24" -> "24 Nov 2026". Returns the input unchanged if it cannot be parsed. */
fun formatDisplayDate(iso: String): String {
    val date = runCatching { LocalDate.parse(iso) }.getOrNull() ?: return iso
    val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    return "${date.dayOfMonth} $month ${date.year}"
}

/** Formats YearMonth as "Month YYYY", e.g., "March 2026". */
fun YearMonth.label(): String {
    val monthName = month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    return "$monthName $year"
}

/** Formats YearMonth as "Month - YYYY", e.g., "March - 2026". */
fun YearMonth.labelDashed(): String {
    val monthName = month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    return "$monthName - $year"
}

/** Formats "YYYY-MM" string to "Month YYYY". Returns original string if invalid. */
fun formatMonth(yyyyMm: String): String {
    val parsed = runCatching { YearMonth.parse(yyyyMm) }.getOrNull() ?: return yyyyMm
    return parsed.label()
}

/** Formats YearMonth to ISO string "YYYY-MM" (e.g., YearMonth.of(2026, 3) -> "2026-03"). */
fun YearMonth.toIsoMonth(): String = toString()

/** Formats cycle date range: "24 Nov 2026 – 23 Dec 2026". */
fun formatCycle(cycleStartIso: String, cycleEndIso: String): String {
    return "${formatDisplayDate(cycleStartIso)} – ${formatDisplayDate(cycleEndIso)}"
}
