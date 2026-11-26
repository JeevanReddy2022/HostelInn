package com.example.hostelmanager.data.repository

import androidx.room.withTransaction
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.PersonDao
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.ResidentWithStayInfo
import com.example.hostelmanager.data.RoomOccupant
import com.example.hostelmanager.data.Stay
import com.example.hostelmanager.data.StayDao
import kotlinx.coroutines.flow.Flow

interface ResidentRepository {
    fun getAllPersons(): Flow<List<Person>>
    suspend fun getPersonById(id: String): Person?
    fun getPersonByIdFlow(id: String): Flow<Person?>
    fun getActiveResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>>
    fun getActiveResidentsByRoom(roomId: String): Flow<List<ResidentWithRoom>>
    fun getPastResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>>
    fun getAllResidentsWithLatestStay(hostelId: String): Flow<List<ResidentWithStayInfo>>
    fun getActiveResidentNamesByRoom(hostelId: String): Flow<List<RoomOccupant>>
    fun getStaysByPerson(personId: String): Flow<List<Stay>>
    fun getActiveStaysByHostel(hostelId: String): Flow<List<Stay>>
    fun getActiveStayByPerson(personId: String): Flow<Stay?>
    suspend fun getActiveStayForPerson(personId: String): Stay?

    // NEW
    fun getDailyGuestsByHostel(hostelId: String): Flow<List<ResidentWithRoom>>
    fun getNoticeResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>>

    suspend fun isPhoneDuplicate(phone: String, excludePersonId: String? = null): Boolean
    suspend fun isGovtIdDuplicate(govtId: String, excludePersonId: String? = null): Boolean
    suspend fun isEmergencyDuplicate(emergency: String, excludePersonId: String? = null): Boolean
    suspend fun addResident(person: Person, stay: Stay)
    suspend fun updatePerson(person: Person)
    suspend fun checkoutResident(stay: Stay, checkoutDate: String, refundPaise: Long, refundMode: String?, refundDate: String?)
    suspend fun transferRoom(stay: Stay, newRoomId: String, transferDate: String, newRentPaise: Long)
    suspend fun reAdmitResident(stay: Stay)
    suspend fun updateNoticeDate(stay: Stay, noticeDate: String?)
}

class ResidentRepositoryImpl(
    private val database: AppDatabase,
    private val personDao: PersonDao,
    private val stayDao: StayDao
) : ResidentRepository {

    override fun getAllPersons(): Flow<List<Person>> = personDao.getAllPersons()
    override suspend fun getPersonById(id: String): Person? = personDao.getPersonById(id)
    override fun getPersonByIdFlow(id: String): Flow<Person?> = personDao.getPersonByIdFlow(id)
    override fun getActiveResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>> = personDao.getActiveResidentsByHostel(hostelId)
    override fun getActiveResidentsByRoom(roomId: String): Flow<List<ResidentWithRoom>> = personDao.getActiveResidentsByRoom(roomId)
    override fun getPastResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>> = personDao.getPastResidentsByHostel(hostelId)
    override fun getAllResidentsWithLatestStay(hostelId: String): Flow<List<ResidentWithStayInfo>> = personDao.getAllResidentsWithLatestStay(hostelId)
    override fun getActiveResidentNamesByRoom(hostelId: String): Flow<List<RoomOccupant>> = personDao.getActiveResidentNamesByRoom(hostelId)
    override fun getStaysByPerson(personId: String): Flow<List<Stay>> = stayDao.getStaysByPerson(personId)
    override fun getActiveStaysByHostel(hostelId: String): Flow<List<Stay>> = stayDao.getActiveStaysByHostel(hostelId)
    override fun getActiveStayByPerson(personId: String): Flow<Stay?> = stayDao.getActiveStayByPerson(personId)
    override suspend fun getActiveStayForPerson(personId: String): Stay? = stayDao.getActiveStayForPerson(personId)

    // NEW
    override fun getDailyGuestsByHostel(hostelId: String): Flow<List<ResidentWithRoom>> = personDao.getDailyGuestsByHostel(hostelId)
    override fun getNoticeResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>> = personDao.getNoticeResidentsByHostel(hostelId)

    override suspend fun isPhoneDuplicate(phone: String, excludePersonId: String?): Boolean = personDao.isPhoneDuplicate(phone, excludePersonId)
    override suspend fun isGovtIdDuplicate(govtId: String, excludePersonId: String?): Boolean = personDao.isGovtIdDuplicate(govtId, excludePersonId)
    override suspend fun isEmergencyDuplicate(emergency: String, excludePersonId: String?): Boolean = personDao.isEmergencyDuplicate(emergency, excludePersonId)

    override suspend fun addResident(person: Person, stay: Stay) {
        database.withTransaction {
            personDao.insertPerson(person)
            stayDao.insertStay(stay)
        }
    }

    override suspend fun updatePerson(person: Person) {
        personDao.updatePerson(person)
    }

    override suspend fun checkoutResident(stay: Stay, checkoutDate: String, refundPaise: Long, refundMode: String?, refundDate: String?) {
        database.withTransaction {
            stayDao.updateStay(
                stay.copy(
                    isActive = false,
                    checkoutDate = checkoutDate,
                    depositRefunded = refundPaise,
                    depositRefundMode = if (refundPaise > 0) refundMode else null,
                    depositRefundDate = if (refundPaise > 0) refundDate else null,
                    noticeDate = null,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun transferRoom(stay: Stay, newRoomId: String, transferDate: String, newRentPaise: Long) {
        database.withTransaction {
            stayDao.updateStay(stay.copy(isActive = false, checkoutDate = transferDate, updatedAt = System.currentTimeMillis()))
            stayDao.insertStay(
                Stay(
                    personId = stay.personId, hostelId = stay.hostelId, roomId = newRoomId,
                    joinDate = transferDate, monthlyRent = newRentPaise,
                    depositCollected = stay.depositCollected, billingAnchorDay = stay.billingAnchorDay,
                    dailyRent = stay.dailyRent, isActive = true
                )
            )
        }
    }

    override suspend fun reAdmitResident(stay: Stay) {
        database.withTransaction { stayDao.insertStay(stay) }
    }

    override suspend fun updateNoticeDate(stay: Stay, noticeDate: String?) {
        database.withTransaction {
            stayDao.updateStay(stay.copy(noticeDate = noticeDate, updatedAt = System.currentTimeMillis()))
        }
    }
}