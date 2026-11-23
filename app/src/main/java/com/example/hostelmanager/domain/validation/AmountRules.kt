package com.example.hostelmanager.domain.validation

import com.example.hostelmanager.util.rupeesToPaiseOrNull

object AmountRules {
    fun validateAmount(
        rawRupees: String,
        label: String = "Amount",
        minRupees: Long = 1,
        maxRupees: Long? = null
    ): String? {
        val clean = rawRupees.trim()
        if (clean.isEmpty()) return "$label is required."
        val paise = clean.rupeesToPaiseOrNull() ?: return "Please enter a valid $label."
        val rupees = paise / 100
        if (rupees < minRupees) {
            return "$label must be at least ₹$minRupees."
        }
        if (maxRupees != null && rupees > maxRupees) {
            return "$label cannot exceed ₹$maxRupees."
        }
        return null
    }
}
