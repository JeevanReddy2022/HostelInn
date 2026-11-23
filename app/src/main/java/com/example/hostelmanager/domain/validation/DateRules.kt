package com.example.hostelmanager.domain.validation

import java.time.LocalDate

object DateRules {
    fun parseIsoOrNull(iso: String): LocalDate? {
        val clean = iso.trim()
        if (!Patterns.IsoDate.matches(clean)) return null
        return runCatching { LocalDate.parse(clean) }.getOrNull()
    }

    fun validateDate(
        raw: String,
        label: String = "Date",
        minDateIso: String? = null,
        notFuture: Boolean = true
    ): String? {
        val clean = raw.trim()
        if (clean.isEmpty()) return "$label is required."
        val date = parseIsoOrNull(clean) ?: return "Please enter a valid $label (YYYY-MM-DD)."
        val today = LocalDate.now()

        if (notFuture && date.isAfter(today)) {
            return "$label cannot be in the future."
        }

        if (minDateIso != null) {
            val minDate = parseIsoOrNull(minDateIso)
            if (minDate != null && date.isBefore(minDate)) {
                return "$label cannot be before $minDateIso."
            }
        }

        return null
    }
}
