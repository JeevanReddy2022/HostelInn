package com.example.hostelmanager.data.repository

import androidx.room.withTransaction
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.PaymentDao
import com.example.hostelmanager.data.PaymentEdit
import com.example.hostelmanager.data.PaymentEditDao
import com.example.hostelmanager.data.StayDao
import com.example.hostelmanager.domain.Balance
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface PaymentEditRepository {
    fun getEditsForPayment(paymentId: String): Flow<List<PaymentEdit>>

    /** @return null on success, otherwise a human-readable reason it was refused. */
    suspend fun editPayment(
        paymentId: String,
        newAmountPaise: Long,
        newMode: String,
        newPaymentDate: String,
        newRecordedDate: String,
        newNote: String,
        reason: String
    ): String?
}

class PaymentEditRepositoryImpl(
    private val database: AppDatabase,
    private val paymentDao: PaymentDao,
    private val paymentEditDao: PaymentEditDao,
    private val stayDao: StayDao
) : PaymentEditRepository {

    override fun getEditsForPayment(paymentId: String): Flow<List<PaymentEdit>> =
        paymentEditDao.getEditsForPayment(paymentId)

    override suspend fun editPayment(
        paymentId: String,
        newAmountPaise: Long,
        newMode: String,
        newPaymentDate: String,
        newRecordedDate: String,
        newNote: String,
        reason: String
    ): String? {
        val cleanReason = reason.trim()
        if (cleanReason.length < 5) return "A reason of at least 5 characters is required."
        if (newAmountPaise < 0) return "Amount cannot be negative."
        if (newMode.isBlank()) return "Choose a payment mode."
        if (newPaymentDate.isBlank() || newRecordedDate.isBlank()) return "Both dates are required."

        val original = paymentDao.getPaymentById(paymentId) ?: return "This payment no longer exists."
        val stay = stayDao.getStayById(original.stayId) ?: return "The stay for this payment no longer exists."

        // What would the stay owe if this payment did not exist at all?
        val all = paymentDao.getPaymentsForStayOnce(original.stayId)
        val ref = Balance.referenceDateFor(stay, LocalDate.now())
        val owed = Balance.owedPaise(stay, ref)
        val paidOthers = all.filter { it.id != paymentId }.sumOf { it.amountPaise }
        val waivedOthers = all.filter { it.id != paymentId }.sumOf { it.waivedPaise }
        val balanceWithoutThis = owed - paidOthers - waivedOthers

        // ₹0 becomes a write-off and absorbs exactly the remaining balance.
        val newWaived = if (newAmountPaise == 0L) {
            if (balanceWithoutThis <= 0) return "Nothing left to write off for this stay."
            balanceWithoutThis
        } else {
            0L
        }

        val cleanNote = newNote.trim()
        val unchanged = newAmountPaise == original.amountPaise &&
                newWaived == original.waivedPaise &&
                newMode == original.mode &&
                newPaymentDate == original.paymentDate &&
                newRecordedDate == original.date &&
                cleanNote == original.note
        if (unchanged) return "Nothing changed — edit at least one field."

        val now = System.currentTimeMillis()

        database.withTransaction {
            paymentEditDao.insertEdit(
                PaymentEdit(
                    paymentId = original.id,
                    editedAt = now,
                    reason = cleanReason,
                    amountBefore = original.amountPaise,
                    amountAfter = newAmountPaise,
                    waivedBefore = original.waivedPaise,
                    waivedAfter = newWaived,
                    modeBefore = original.mode,
                    modeAfter = newMode,
                    paymentDateBefore = original.paymentDate,
                    paymentDateAfter = newPaymentDate,
                    recordedDateBefore = original.date,
                    recordedDateAfter = newRecordedDate,
                    noteBefore = original.note,
                    noteAfter = cleanNote
                )
            )
            paymentDao.updatePayment(
                original.copy(
                    amountPaise = newAmountPaise,
                    waivedPaise = newWaived,
                    mode = newMode,
                    paymentDate = newPaymentDate,
                    date = newRecordedDate,
                    note = cleanNote,
                    editCount = original.editCount + 1,
                    lastEditedAt = now
                )
            )
        }
        return null
    }
}