package com.example.hostelmanager.data

import androidx.room.Embedded

data class ResidentWithRoom(
    @Embedded val person: Person,
    val roomNumber: String,
    val monthlyRent: Long,
    val dailyRent: Long = 0,
    val noticeDate: String? = null,
    val joinDate: String = "", // NEW: Needed to calculate days stayed
    val stayId: String = ""    // NEW: Needed to link payments
)