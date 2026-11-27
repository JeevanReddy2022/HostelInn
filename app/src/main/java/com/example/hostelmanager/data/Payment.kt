package com.example.hostelmanager.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Stay::class,
            parentColumns = ["id"],
            childColumns = ["stayId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("stayId"), Index("personId"), Index("hostelId"), Index("paymentDate"), Index("date")]
)
data class Payment(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val stayId: String,
    val personId: String,
    val hostelId: String,
    val amountPaise: Long,
    val mode: String,
    val paymentDate: String,
    val date: String,
    val note: String = "",
    @ColumnInfo(defaultValue = "0")
    val waivedPaise: Long = 0,
    // ✅ NEW: audit markers. The full before/after history lives in payment_edits.
    @ColumnInfo(defaultValue = "0")
    val editCount: Int = 0,
    val lastEditedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)