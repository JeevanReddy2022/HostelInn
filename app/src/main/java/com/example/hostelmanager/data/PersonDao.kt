package com.example.hostelmanager.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Upsert
    suspend fun insertPerson(person: Person)

    @Update
    suspend fun updatePerson(person: Person)

    @Query("SELECT * FROM persons ORDER BY name ASC")
    fun getAllPersons(): Flow<List<Person>>

    @Query("SELECT * FROM persons WHERE id = :id")
    suspend fun getPersonById(id: String): Person?

    @Query("SELECT * FROM persons WHERE id = :id")
    fun getPersonByIdFlow(id: String): Flow<Person?>

    @Query("""
        SELECT p.id, p.name, p.phone, p.govtIdNumber, p.emergencyContact, p.dateOfBirth,
        p.address, p.gender, p.guardianName, p.govtIdPhotoPath, p.govtIdBackPath, p.profilePhotoPath,
        p.createdAt, p.updatedAt, r.roomNumber, s.monthlyRent, s.dailyRent, s.noticeDate, s.joinDate, s.id AS stayId
        FROM persons p
        INNER JOIN stays s ON p.id = s.personId
        INNER JOIN rooms r ON s.roomId = r.id
        WHERE s.hostelId = :hostelId AND s.isActive = 1
        ORDER BY p.name ASC
    """)
    fun getActiveResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>>

    @Query("""
        SELECT p.id, p.name, p.phone, p.govtIdNumber, p.emergencyContact, p.dateOfBirth,
        p.address, p.gender, p.guardianName, p.govtIdPhotoPath, p.govtIdBackPath, p.profilePhotoPath,
        p.createdAt, p.updatedAt, r.roomNumber, s.monthlyRent, s.dailyRent, s.noticeDate, s.joinDate, s.id AS stayId
        FROM persons p
        INNER JOIN stays s ON p.id = s.personId
        INNER JOIN rooms r ON s.roomId = r.id
        WHERE s.roomId = :roomId AND s.isActive = 1
        ORDER BY p.name ASC
    """)
    fun getActiveResidentsByRoom(roomId: String): Flow<List<ResidentWithRoom>>

    @Query("""
        SELECT p.id, p.name, p.phone, p.govtIdNumber, p.emergencyContact, p.dateOfBirth,
        p.address, p.gender, p.guardianName, p.govtIdPhotoPath, p.govtIdBackPath, p.profilePhotoPath,
        p.createdAt, p.updatedAt,
        (SELECT r.roomNumber FROM stays s2 INNER JOIN rooms r ON s2.roomId = r.id
         WHERE s2.personId = p.id AND s2.hostelId = :hostelId
         ORDER BY s2.joinDate DESC LIMIT 1) AS roomNumber,
        (SELECT s3.monthlyRent FROM stays s3
         WHERE s3.personId = p.id AND s3.hostelId = :hostelId
         ORDER BY s3.joinDate DESC LIMIT 1) AS monthlyRent,
        (SELECT s4.dailyRent FROM stays s4
         WHERE s4.personId = p.id AND s4.hostelId = :hostelId
         ORDER BY s4.joinDate DESC LIMIT 1) AS dailyRent,
        (SELECT s5.noticeDate FROM stays s5
         WHERE s5.personId = p.id AND s5.hostelId = :hostelId
         ORDER BY s5.joinDate DESC LIMIT 1) AS noticeDate,
        (SELECT s6.joinDate FROM stays s6
         WHERE s6.personId = p.id AND s6.hostelId = :hostelId
         ORDER BY s6.joinDate DESC LIMIT 1) AS joinDate,
        (SELECT s7.id FROM stays s7
         WHERE s7.personId = p.id AND s7.hostelId = :hostelId
         ORDER BY s7.joinDate DESC LIMIT 1) AS stayId
        FROM persons p
        WHERE p.id IN (
            SELECT personId FROM stays WHERE hostelId = :hostelId
        )
        AND p.id NOT IN (
            SELECT personId FROM stays WHERE hostelId = :hostelId AND isActive = 1
        )
        ORDER BY p.name ASC
    """)
    fun getPastResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>>

    @Query("SELECT COUNT(*) FROM persons p INNER JOIN stays s ON p.id = s.personId WHERE s.isActive = 1 AND p.phone = :phone AND (:excludePersonId IS NULL OR p.id != :excludePersonId)")
    suspend fun isPhoneDuplicate(phone: String, excludePersonId: String? = null): Boolean

    @Query("SELECT COUNT(*) FROM persons p INNER JOIN stays s ON p.id = s.personId WHERE s.isActive = 1 AND p.govtIdNumber = :govtId AND (:excludePersonId IS NULL OR p.id != :excludePersonId)")
    suspend fun isGovtIdDuplicate(govtId: String, excludePersonId: String? = null): Boolean

    @Query("SELECT COUNT(*) FROM persons p INNER JOIN stays s ON p.id = s.personId WHERE s.isActive = 1 AND p.emergencyContact = :emergency AND (:excludePersonId IS NULL OR p.id != :excludePersonId)")
    suspend fun isEmergencyDuplicate(emergency: String, excludePersonId: String? = null): Boolean

    @Query("""
        SELECT p.id AS personId, p.name, p.phone, p.profilePhotoPath,
        r.roomNumber, s.monthlyRent, s.dailyRent, s.id AS stayId,
        s.isActive, s.checkoutDate, s.billingAnchorDay,s.joinDate
        FROM persons p
        INNER JOIN stays s ON p.id = s.personId
        INNER JOIN rooms r ON s.roomId = r.id
        WHERE s.hostelId = :hostelId
        AND s.id = (
            SELECT s2.id FROM stays s2
            WHERE s2.personId = p.id AND s2.hostelId = :hostelId
            ORDER BY s2.joinDate DESC LIMIT 1
        )
        ORDER BY p.name ASC
    """)
    fun getAllResidentsWithLatestStay(hostelId: String): Flow<List<ResidentWithStayInfo>>

    @Query("""
        SELECT s.roomId AS roomId, p.name AS name
        FROM persons p
        INNER JOIN stays s ON p.id = s.personId
        WHERE s.hostelId = :hostelId AND s.isActive = 1
        ORDER BY p.name ASC
    """)
    fun getActiveResidentNamesByRoom(hostelId: String): Flow<List<RoomOccupant>>

    @Query("""
        SELECT p.id, p.name, p.phone, p.govtIdNumber, p.emergencyContact, p.dateOfBirth,
        p.address, p.gender, p.guardianName, p.govtIdPhotoPath, p.govtIdBackPath, p.profilePhotoPath,
        p.createdAt, p.updatedAt, r.roomNumber, s.monthlyRent, s.dailyRent, s.noticeDate, s.joinDate, s.id AS stayId
        FROM persons p
        INNER JOIN stays s ON p.id = s.personId
        INNER JOIN rooms r ON s.roomId = r.id
        WHERE s.hostelId = :hostelId AND s.isActive = 1 AND s.dailyRent > 0
        ORDER BY p.name ASC
    """)
    fun getDailyGuestsByHostel(hostelId: String): Flow<List<ResidentWithRoom>>

    @Query("""
        SELECT p.id, p.name, p.phone, p.govtIdNumber, p.emergencyContact, p.dateOfBirth,
        p.address, p.gender, p.guardianName, p.govtIdPhotoPath, p.govtIdBackPath, p.profilePhotoPath,
        p.createdAt, p.updatedAt, r.roomNumber, s.monthlyRent, s.dailyRent, s.noticeDate, s.joinDate, s.id AS stayId
        FROM persons p
        INNER JOIN stays s ON p.id = s.personId
        INNER JOIN rooms r ON s.roomId = r.id
        WHERE s.hostelId = :hostelId AND s.isActive = 1 AND s.noticeDate IS NOT NULL
        ORDER BY s.noticeDate ASC
    """)
    fun getNoticeResidentsByHostel(hostelId: String): Flow<List<ResidentWithRoom>>
}

data class RoomOccupant(val roomId: String, val name: String)