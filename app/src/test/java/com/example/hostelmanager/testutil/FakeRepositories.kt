package com.example.hostelmanager.testutil

import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.ResidentWithStayInfo
import com.example.hostelmanager.data.RoomEntity
import com.example.hostelmanager.data.RoomOccupant
import com.example.hostelmanager.data.RoomWithOccupancy
import com.example.hostelmanager.data.Stay
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.data.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

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
