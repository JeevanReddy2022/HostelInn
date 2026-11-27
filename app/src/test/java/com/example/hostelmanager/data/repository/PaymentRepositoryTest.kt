package com.example.hostelmanager.data.repository

import com.example.hostelmanager.data.Payment
import com.example.hostelmanager.data.PaymentDao
import com.example.hostelmanager.data.PaymentWithDetails
import com.example.hostelmanager.data.ResidentWithStayInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FakePaymentDao : PaymentDao {
    override suspend fun getPaymentsForStayOnce(stayId: String): List<Payment> =
        payments.filter { it.stayId == stayId }
    val payments = mutableListOf<Payment>()

    override suspend fun insertPayment(payment: Payment) {
        payments.add(payment)
    }

    override suspend fun updatePayment(payment: Payment) {
        val index = payments.indexOfFirst { it.id == payment.id }
        if (index != -1) {
            payments[index] = payment
        } else {
            payments.add(payment)
        }
    }

    override suspend fun getPaymentById(id: String): Payment? {
        return payments.find { it.id == id }
    }

    override suspend fun deletePayment(id: String) {
        payments.removeIf { it.id == id }
    }

    override fun getPaymentsForStay(stayId: String): Flow<List<Payment>> = TODO()
    override fun getPaymentsForPerson(personId: String): Flow<List<Payment>> = TODO()
    override fun getAllPaymentsForHostel(hostelId: String): Flow<List<Payment>> = TODO()
    override fun getPaymentsBetween(startDate: String, endDate: String): Flow<List<Payment>> = TODO()
    override fun getTotalPaymentsForHostel(hostelId: String): Flow<Long> = TODO()
    override fun getPaymentsCountForMonth(hostelId: String, monthPattern: String): Flow<Int> = TODO()
    override fun getTotalRentCollected(hostelId: String, monthPattern: String): Flow<Long> = TODO()
    override fun getTotalPaymentsForDate(hostelId: String, date: String): Flow<Long> = TODO()
    override fun getTotalPaymentsBetween(hostelId: String, startDate: String, endDate: String): Flow<Long> = TODO()
    override fun getAllPaymentsWithDetailsForHostel(hostelId: String): Flow<List<PaymentWithDetails>> = TODO()
    override suspend fun getPaymentsInCycle(stayId: String, cycleStart: String, cycleEnd: String): List<Payment> = TODO()
    override fun getTotalPaidForStay(stayId: String): Flow<Long> = TODO()
    override suspend fun getTotalPaidForStayOnce(stayId: String): Long =
        payments.filter { it.stayId == stayId }.sumOf { it.amountPaise }
    override suspend fun getAllPaymentsForHostelOnce(hostelId: String): List<Payment> {
        return payments.filter { it.hostelId == hostelId }
    }
    override suspend fun countWriteOffsInRange(stayId: String, startDate: String, endDate: String): Int =
        payments.count {
            it.stayId == stayId && it.amountPaise == 0L &&
                    it.note.trim().length >= 5 &&
                    it.paymentDate >= startDate && it.paymentDate <= endDate
        }
}

class PaymentRepositoryTest {

    @Test
    fun `getUnpaidResidentsForHostel returns residents with no payments in billing cycle`() = runTest {
        val fakeDao = FakePaymentDao()
        val repository = PaymentRepositoryImpl(fakeDao)

        val resident1 = ResidentWithStayInfo(
            personId = "p1",
            name = "John Doe",
            phone = "1234567890",
            profilePhotoPath = "",
            roomNumber = "101",
            monthlyRent = 500000,
            stayId = "s1",
            isActive = true,
            checkoutDate = null,
            billingAnchorDay = 1
        )

        val unpaid = repository.getUnpaidResidentsForHostel(
            hostelId = "h1",
            allResidents = listOf(resident1),
            today = LocalDate.of(2026, 3, 15)
        )

        assertEquals(1, unpaid.size)
        assertEquals("John Doe", unpaid[0].person.name)
    }

    @Test
    fun `getUnpaidResidentsForHostel excludes residents who have paid in billing cycle`() = runTest {
        val fakeDao = FakePaymentDao()
        val repository = PaymentRepositoryImpl(fakeDao)

        fakeDao.insertPayment(
            Payment(
                id = "pay1",
                stayId = "s1",
                personId = "p1",
                hostelId = "h1",
                amountPaise = 500000,
                paymentDate = "2026-03-05",
                mode = "Cash",
                date = "2026-03-05"
            )
        )

        val resident1 = ResidentWithStayInfo(
            personId = "p1",
            name = "John Doe",
            phone = "1234567890",
            profilePhotoPath = "",
            roomNumber = "101",
            monthlyRent = 500000,
            stayId = "s1",
            isActive = true,
            checkoutDate = null,
            billingAnchorDay = 1
        )

        val unpaid = repository.getUnpaidResidentsForHostel(
            hostelId = "h1",
            allResidents = listOf(resident1),
            today = LocalDate.of(2026, 3, 15)
        )

        assertEquals(0, unpaid.size)
    }
}
