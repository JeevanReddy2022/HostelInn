package com.example.hostelmanager.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Append-only audit record: one row per edit, never updated.
 * Deleting a payment removes its audit rows (CASCADE) — that's why
 * "wrong amount → edit" and "duplicate entry → delete".
 */
@Entity(
    tableName = "payment_edits",
    foreignKeys = [
        ForeignKey(
            entity = Payment::class,
            parentColumns = ["id"],
            childColumns = ["paymentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("paymentId")]
)
data class PaymentEdit(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val paymentId: String,
    val editedAt: Long,
    val reason: String,
    val amountBefore: Long,
    val amountAfter: Long,
    val waivedBefore: Long,
    val waivedAfter: Long,
    val modeBefore: String,
    val modeAfter: String,
    val paymentDateBefore: String,
    val paymentDateAfter: String,
    val recordedDateBefore: String,
    val recordedDateAfter: String,
    val noteBefore: String,
    val noteAfter: String
)