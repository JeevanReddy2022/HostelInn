package com.example.hostelmanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentEditDao {
    @Insert
    suspend fun insertEdit(edit: PaymentEdit)

    @Query("SELECT * FROM payment_edits WHERE paymentId = :paymentId ORDER BY editedAt DESC")
    fun getEditsForPayment(paymentId: String): Flow<List<PaymentEdit>>
}