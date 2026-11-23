package com.example.hostelmanager.util

const val PAISE_PER_RUPEE = 100L

fun Long.paiseToRupees(): Long = this / PAISE_PER_RUPEE

fun String.rupeesToPaiseOrNull(): Long? {
    val clean = this.trim().replace(",", "")
    val rupees = clean.toLongOrNull() ?: return null
    if (rupees < 0) return null
    return rupees * PAISE_PER_RUPEE
}

fun String.rupeesToPaise(): Long = rupeesToPaiseOrNull() ?: 0L
