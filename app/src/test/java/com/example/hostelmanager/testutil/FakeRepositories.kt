package com.example.hostelmanager.testutil

import com.example.hostelmanager.data.Payment
import com.example.hostelmanager.data.PaymentWithDetails
import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.ResidentWithStayInfo
import com.example.hostelmanager.data.RoomEntity
import com.example.hostelmanager.data.RoomOccupant
import com.example.hostelmanager.data.RoomWithOccupancy
import com.example.hostelmanager.data.Stay
import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.data.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

open class FakeResidentRepository : ResidentRepository {
    var activeStay: Stay? = null

    var person: Person? = null
    override fun getAllPersons(): Flow<List<Person>> = flowOf(emptyList())
    override suspend fun getPersonById(id: String): Person? = person
    override fun getPersonByIdFlow(id: String): Flow<Person?> = flowOf(person)
    override fun getActiveResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>> = flowOf(emptyList())
    override fun getActiveResidentsByRoom(roomId: String): Flow<List<ResidentWithRoom>> = flowOf(emptyList())
    override fun getPastResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>> = flowOf(emptyList())
    override fun getAllResidentsWithLatestStay(hostelId: String): Flow<List<ResidentWithStayInfo>> = flowOf(emptyList())
    override fun getActiveResidentNamesByRoom(hostelId: String): Flow<List<RoomOccupant>> = flowOf(emptyList())
    override fun getStaysByPerson(personId: String): Flow<List<Stay>> = flowOf(emptyList())
    override fun getActiveStaysByHostel(hostelId: String): Flow<List<Stay>> = flowOf(emptyList())
    override fun getActiveStayByPerson(personId: String): Flow<Stay?> = flowOf(activeStay)
    override suspend fun getActiveStayForPerson(personId: String): Stay? = activeStay
    override suspend fun getStayById(id: String): Stay? = if (activeStay?.id == id) activeStay else null
    override suspend fun isPhoneDuplicate(phone: String, excludePersonId: String?): Boolean = false
    override suspend fun isGovtIdDuplicate(govtId: String, excludePersonId: String?): Boolean = false
    override suspend fun isEmergencyDuplicate(emergency: String, excludePersonId: String?): Boolean = false
    override suspend fun addResident(person: Person, stay: Stay) {}
    override suspend fun updatePerson(person: Person) {}
    override suspend fun checkoutResident(
        stay: Stay,
        checkoutDate: String,
        refundPaise: Long,
        refundMode: String?,
        refundDate: String?
    ) {
        activeStay = activeStay?.copy(isActive = false, checkoutDate = checkoutDate)
    }
    override suspend fun transferRoom(stay: Stay, newRoomId: String, transferDate: String, newRentPaise: Long) {}
    override suspend fun reAdmitResident(stay: Stay) {}
    override fun getDailyGuestsByHostel(hostelId: String): Flow<List<ResidentWithRoom>> = flowOf(emptyList())
    override fun getNoticeResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>> = flowOf(emptyList())
    override suspend fun updateNoticeDate(stay: Stay, noticeDate: String?) {}
}

open class FakeRoomRepository : RoomRepository {
    override fun getRoomsWithOccupancy(hostelId: String): Flow<List<RoomWithOccupancy>> = flowOf(emptyList())
    override fun getAllActiveRooms(): Flow<List<RoomEntity>> = flowOf(emptyList())
    override fun getRoomById(id: String): Flow<RoomEntity?> = flowOf(null)
    override fun getArchivedRooms(hostelId: String): Flow<List<RoomEntity>> = flowOf(emptyList())
    override suspend fun insertRoom(room: RoomEntity) {}
    override suspend fun updateRoom(room: RoomEntity, updateActiveStaysRent: Boolean) {}
    override suspend fun deleteRoom(room: RoomEntity) {}
    override suspend fun countRoomsWithNumber(hostelId: String, roomNumber: String): Int = 0
    override fun countStaysForRoom(roomId: String): Flow<Int> = flowOf(0)
}

open class FakePaymentRepository : PaymentRepository {
    var totalPaidOnce: Long = 0L
    var writeOffCount: Int = 0
    var stayPayments: List<Payment> = emptyList()

    override suspend fun insertPayment(payment: Payment) {}
    override suspend fun deletePayment(id: String) {}
    override fun getPaymentsForStay(stayId: String): Flow<List<Payment>> = flowOf(emptyList())
    override fun getPaymentsForPerson(personId: String): Flow<List<Payment>> = flowOf(emptyList())
    override fun getAllPaymentsForHostel(hostelId: String): Flow<List<Payment>> = flowOf(emptyList())
    override fun getPaymentsBetween(startDate: String, endDate: String): Flow<List<Payment>> = flowOf(emptyList())
    override fun getTotalPaymentsForHostel(hostelId: String): Flow<Long> = flowOf(0L)
    override fun getPaymentsCountForMonth(hostelId: String, monthPattern: String): Flow<Int> = flowOf(0)
    override fun getTotalRentCollected(hostelId: String, monthPattern: String): Flow<Long> = flowOf(0L)
    override fun getTotalPaymentsForDate(hostelId: String, date: String): Flow<Long> = flowOf(0L)
    override fun getTotalPaymentsBetween(hostelId: String, startDate: String, endDate: String): Flow<Long> = flowOf(0L)
    override fun getAllPaymentsWithDetailsForHostel(hostelId: String): Flow<List<PaymentWithDetails>> = flowOf(emptyList())
    override suspend fun getPaymentsInCycle(stayId: String, cycleStart: String, cycleEnd: String): List<Payment> = emptyList()
    override fun getTotalPaidForStay(stayId: String): Flow<Long> = flowOf(totalPaidOnce)
    override suspend fun getTotalPaidForStayOnce(stayId: String): Long = stayPayments.filter { it.stayId == stayId }.sumOf { it.amountPaise }
    override suspend fun countWriteOffsInRange(stayId: String, startDate: String, endDate: String): Int =
        stayPayments.count { it.stayId == stayId && it.amountPaise == 0L && it.note.trim().length >= 5 && it.paymentDate >= startDate && it.paymentDate <= endDate }
    override suspend fun getPaymentsForStayOnce(stayId: String): List<Payment> {
        if (stayPayments.isNotEmpty()) {
            return stayPayments.filter { it.stayId == stayId }
        }
        val list = mutableListOf<Payment>()
        if (totalPaidOnce > 0) {
            list.add(Payment(id = "p_paid", stayId = stayId, personId = "p1", hostelId = "h1", amountPaise = totalPaidOnce, mode = "Cash", paymentDate = "2026-02-01", date = "2026-02-01"))
        }
        if (writeOffCount > 0) {
            list.add(Payment(id = "p_waived", stayId = stayId, personId = "p1", hostelId = "h1", amountPaise = 0, waivedPaise = 10_000_000L, mode = "Cash", paymentDate = "2026-02-01", date = "2026-02-01", note = "Waived balance"))
        }
        return list
    }
    override suspend fun getUnpaidResidentsForHostel(
        hostelId: String,
        allResidents: List<ResidentWithStayInfo>,
        today: LocalDate
    ): List<ResidentWithRoom> = emptyList()
}