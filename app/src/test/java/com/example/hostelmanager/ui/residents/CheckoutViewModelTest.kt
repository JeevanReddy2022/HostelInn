package com.example.hostelmanager.ui.residents

import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.ResidentWithStayInfo
import com.example.hostelmanager.data.RoomEntity
import com.example.hostelmanager.data.RoomOccupant
import com.example.hostelmanager.data.Stay
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.data.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeResidentRepository : ResidentRepository {
    var activeStay: Stay? = null
    var person: Person? = null

    override fun getAllPersons(): Flow<List<Person>> = TODO()
    override suspend fun getPersonById(id: String): Person? = person
    override fun getPersonByIdFlow(id: String): Flow<Person?> = flowOf(person)
    override fun getActiveResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>> = TODO()
    override fun getActiveResidentsByRoom(roomId: String): Flow<List<ResidentWithRoom>> = TODO()
    override fun getPastResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>> = TODO()
    override fun getAllResidentsWithLatestStay(hostelId: String): Flow<List<ResidentWithStayInfo>> = TODO()
    override fun getActiveResidentNamesByRoom(hostelId: String): Flow<List<RoomOccupant>> = TODO()
    override fun getStaysByPerson(personId: String): Flow<List<Stay>> = TODO()
    override fun getActiveStaysByHostel(hostelId: String): Flow<List<Stay>> = TODO()
    override fun getActiveStayByPerson(personId: String): Flow<Stay?> = flowOf(activeStay)
    override suspend fun getActiveStayForPerson(personId: String): Stay? = activeStay
    override suspend fun isPhoneDuplicate(phone: String, excludePersonId: String?) = false
    override suspend fun isGovtIdDuplicate(govtId: String, excludePersonId: String?) = false
    override suspend fun isEmergencyDuplicate(emergency: String, excludePersonId: String?) = false
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
}

class FakeRoomRepository : RoomRepository {
    override fun getRoomsWithOccupancy(hostelId: String) = TODO()
    override fun getAllActiveRooms() = TODO()
    override fun getRoomById(id: String): Flow<RoomEntity?> = flowOf(null)
    override fun getArchivedRooms(hostelId: String) = TODO()
    override suspend fun insertRoom(room: RoomEntity) {}
    override suspend fun updateRoom(room: RoomEntity, updateActiveStaysRent: Boolean) {}
    override suspend fun deleteRoom(room: RoomEntity) {}
    override suspend fun countRoomsWithNumber(hostelId: String, roomNumber: String) = 0
    override fun countStaysForRoom(roomId: String): Flow<Int> = flowOf(0)
}

class CheckoutViewModelTest {

    @Test
    fun `checkoutResident fails if checkout date is before join date`() = runTest {
        val residentRepo = FakeResidentRepository()
        val roomRepo = FakeRoomRepository()

        residentRepo.activeStay = Stay(
            id = "s1",
            personId = "p1",
            hostelId = "h1",
            roomId = "r1",
            joinDate = "2026-02-01",
            monthlyRent = 500000,
            depositCollected = 1000000,
            billingAnchorDay = 1,
            isActive = true
        )

        val viewModel = CheckoutViewModel(residentRepo, roomRepo)
        viewModel.setPersonId("p1")

        val success = viewModel.checkoutResident(
            checkoutDate = "2026-01-15",
            refundPaise = 1000000,
            refundMode = "Cash",
            refundDate = "2026-01-15"
        )

        assertFalse(success)
        assertNotNull(viewModel.checkoutError.value)
    }

    @Test
    fun `checkoutResident succeeds with valid date and refund`() = runTest {
        val residentRepo = FakeResidentRepository()
        val roomRepo = FakeRoomRepository()

        residentRepo.activeStay = Stay(
            id = "s1",
            personId = "p1",
            hostelId = "h1",
            roomId = "r1",
            joinDate = "2026-02-01",
            monthlyRent = 500000,
            depositCollected = 1000000,
            billingAnchorDay = 1,
            isActive = true
        )

        val viewModel = CheckoutViewModel(residentRepo, roomRepo)
        viewModel.setPersonId("p1")

        val success = viewModel.checkoutResident(
            checkoutDate = "2026-03-01",
            refundPaise = 1000000,
            refundMode = "Cash",
            refundDate = "2026-03-01"
        )

        assertTrue(success)
    }
}
