package com.example.hostelmanager.domain.validation

object RoomRules {

    fun roomNumberError(raw: String, isNewRoom: Boolean): String? {
        val clean = raw.trim()
        if (clean.isEmpty()) return if (isNewRoom) null else "Room number is required"
        val num = clean.toLongOrNull()
        if (num != null && num <= 0) return "Room number must be greater than zero"
        return null
    }

    fun floorError(raw: String): String? {
        val clean = raw.trim()
        if (clean.isEmpty()) return "Floor is required"
        val num = clean.toIntOrNull()
        if (num == null || num < 0) return "Floor cannot be negative or empty"
        return null
    }

    fun bedsError(raw: String, occupiedBeds: Int): String? {
        val clean = raw.trim()
        val num = clean.toIntOrNull()
        if (num == null || num < 1) return "Must be at least 1"
        if (num < occupiedBeds) return "$occupiedBeds resident(s) live here. Beds cannot be below $occupiedBeds."
        return null
    }

    fun rentError(raw: String, isNewRoom: Boolean): String? {
        val clean = raw.trim()
        if (clean.isEmpty() && isNewRoom) return null
        return AmountRules.validateAmount(raw, "Rent per bed")
    }
}
