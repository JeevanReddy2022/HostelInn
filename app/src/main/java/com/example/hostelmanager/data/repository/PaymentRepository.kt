package com.example.hostelmanager.data.repository

import com.example.hostelmanager.data.Payment
import com.example.hostelmanager.data.PaymentDao
import com.example.hostelmanager.data.PaymentWithDetails
import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.ResidentWithStayInfo
import com.example.hostelmanager.domain.BillingCycle
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface PaymentRepository {
    suspend fun insertPayment(payment: Payment)
    suspend fun deletePayment(id: String)
    fun getPaymentsForStay(stayId: String): Flow<List<Payment>>
    fun getPaymentsForPerson(personId: String): Flow<List<Payment>>
    fun getAllPaymentsForHostel(hostelId: String): Flow<List<Payment>>
    fun getPaymentsBetween(startDate: String, endDate: String): Flow<List<Payment>>
    fun getTotalPaymentsForHostel(hostelId: String): Flow<Long>
    fun getPaymentsCountForMonth(hostelId: String, monthPattern: String): Flow<Int>
    fun getTotalRentCollected(hostelId: String, monthPattern: String): Flow<Long>
    fun getTotalPaymentsForDate(hostelId: String, date: String): Flow<Long>
    fun getTotalPaymentsBetween(hostelId: String, startDate: String, endDate: String): Flow<Long>
    fun getAllPaymentsWithDetailsForHostel(hostelId: String): Flow<List<PaymentWithDetails>>
    suspend fun getPaymentsInCycle(stayId: String, cycleStart: String, cycleEnd: String): List<Payment>
    suspend fun getUnpaidResidentsForHostel(hostelId: String, allResidents: List<ResidentWithStayInfo>, today: LocalDate): List<ResidentWithRoom>

    // ✅ NEW
    fun getTotalPaidForStay(stayId: String): Flow<Long>
}

class PaymentRepositoryImpl(private val paymentDao: PaymentDao) : PaymentRepository {
    override suspend fun insertPayment(payment: Payment) = paymentDao.insertPayment(payment)
    override suspend fun deletePayment(id: String) = paymentDao.deletePayment(id)
    override fun getPaymentsForStay(stayId: String): Flow<List<Payment>> = paymentDao.getPaymentsForStay(stayId)
    override fun getPaymentsForPerson(personId: String): Flow<List<Payment>> = paymentDao.getPaymentsForPerson(personId)
    override fun getAllPaymentsForHostel(hostelId: String): Flow<List<Payment>> = paymentDao.getAllPaymentsForHostel(hostelId)
    override fun getPaymentsBetween(startDate: String, endDate: String): Flow<List<Payment>> = paymentDao.getPaymentsBetween(startDate, endDate)
    override fun getTotalPaymentsForHostel(hostelId: String): Flow<Long> = paymentDao.getTotalPaymentsForHostel(hostelId)
    override fun getPaymentsCountForMonth(hostelId: String, monthPattern: String): Flow<Int> = paymentDao.getPaymentsCountForMonth(hostelId, monthPattern)
    override fun getTotalRentCollected(hostelId: String, monthPattern: String): Flow<Long> = paymentDao.getTotalRentCollected(hostelId, monthPattern)
    override fun getTotalPaymentsForDate(hostelId: String, date: String): Flow<Long> = paymentDao.getTotalPaymentsForDate(hostelId, date)
    override fun getTotalPaymentsBetween(hostelId: String, startDate: String, endDate: String): Flow<Long> = paymentDao.getTotalPaymentsBetween(hostelId, startDate, endDate)
    override fun getAllPaymentsWithDetailsForHostel(hostelId: String): Flow<List<PaymentWithDetails>> = paymentDao.getAllPaymentsWithDetailsForHostel(hostelId)
    override suspend fun getPaymentsInCycle(stayId: String, cycleStart: String, cycleEnd: String): List<Payment> = paymentDao.getPaymentsInCycle(stayId, cycleStart, cycleEnd)
    override fun getTotalPaidForStay(stayId: String): Flow<Long> = paymentDao.getTotalPaidForStay(stayId) // ✅ NEW

    override suspend fun getUnpaidResidentsForHostel(hostelId: String, allResidents: List<ResidentWithStayInfo>, today: LocalDate): List<ResidentWithRoom> {
        if (allResidents.isEmpty()) return emptyList()
        val allHostelPayments = paymentDao.getAllPaymentsForHostelOnce(hostelId)
        val paymentsByStayId = allHostelPayments.groupBy { it.stayId }
        val result = mutableListOf<ResidentWithRoom>()
        for (resident in allResidents) {
            val anchorDay = resident.billingAnchorDay.coerceIn(1, 31)
            val cycleStart: LocalDate = if (resident.isActive) {
                BillingCycle.cycleStart(today, anchorDay)
            } else {
                val checkoutDate = resident.checkoutDate?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) } ?: continue
                BillingCycle.cycleStart(checkoutDate, anchorDay)
            }
            val cycleEnd = cycleStart.plusMonths(1).minusDays(1)
            val cycleStartStr = cycleStart.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val cycleEndStr = cycleEnd.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val stayPayments = paymentsByStayId[resident.stayId] ?: emptyList()
            val hasPaymentInCycle = stayPayments.any { p -> p.paymentDate >= cycleStartStr && p.paymentDate <= cycleEndStr }
            if (!hasPaymentInCycle) {
                val person = Person(id = resident.personId, name = resident.name, phone = resident.phone, profilePhotoPath = resident.profilePhotoPath, govtIdNumber = "", emergencyContact = "", dateOfBirth = "", address = "", gender = "", guardianName = "", govtIdPhotoPath = "", govtIdBackPath = "")
                result.add(ResidentWithRoom(person = person, roomNumber = resident.roomNumber, monthlyRent = resident.monthlyRent))
            }
        }
        return result.sortedBy { it.person.name }
    }
}