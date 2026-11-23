package com.example.hostelmanager.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTransactionTest {

    private lateinit var database: AppDatabase
    private lateinit var hostelDao: HostelDao
    private lateinit var roomDao: RoomDao
    private lateinit var personDao: PersonDao
    private lateinit var stayDao: StayDao
    private lateinit var paymentDao: PaymentDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        hostelDao = database.hostelDao()
        roomDao = database.roomDao()
        personDao = database.personDao()
        stayDao = database.stayDao()
        paymentDao = database.paymentDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertHostelAndRoomAndPersonAtomically() = runBlocking {
        val hostel = Hostel(id = "h1", name = "Sunshine Hostel", address = "Main St", phone = "9999999999")
        hostelDao.insertHostel(hostel)

        val room = RoomEntity(id = "r1", hostelId = "h1", roomNumber = "101", floor = 1, bedCount = 2, defaultRentPerBed = 500000)
        roomDao.insertRoom(room)

        val person = Person(id = "p1", name = "Alice", phone = "9876543210", govtIdNumber = "ID123", emergencyContact = "911", dateOfBirth = "2000-01-01", address = "123 Main St", gender = "Female", guardianName = "Bob")
        personDao.insertPerson(person)

        val stay = Stay(id = "s1", personId = "p1", hostelId = "h1", roomId = "r1", joinDate = "2026-01-01", monthlyRent = 500000, depositCollected = 1000000, billingAnchorDay = 1, isActive = true)
        stayDao.insertStay(stay)

        val fetchedPerson = personDao.getPersonById("p1")
        assertNotNull(fetchedPerson)
        assertEquals("Alice", fetchedPerson?.name)

        val activeStay = stayDao.getActiveStayForPerson("p1")
        assertNotNull(activeStay)
        assertEquals("r1", activeStay?.roomId)
    }

    @Test
    fun updateStayAndCheckoutTransaction() = runBlocking {
        val hostel = Hostel(id = "h1", name = "Sunshine Hostel", address = "Main St", phone = "9999999999")
        hostelDao.insertHostel(hostel)

        val room = RoomEntity(id = "r1", hostelId = "h1", roomNumber = "101", floor = 1, bedCount = 2, defaultRentPerBed = 500000)
        roomDao.insertRoom(room)

        val person = Person(id = "p1", name = "Alice", phone = "9876543210", govtIdNumber = "ID123", emergencyContact = "911", dateOfBirth = "2000-01-01", address = "123 Main St", gender = "Female", guardianName = "Bob")
        personDao.insertPerson(person)

        val stay = Stay(id = "s1", personId = "p1", hostelId = "h1", roomId = "r1", joinDate = "2026-01-01", monthlyRent = 500000, depositCollected = 1000000, billingAnchorDay = 1, isActive = true)
        stayDao.insertStay(stay)

        stayDao.updateStay(stay.copy(isActive = false, checkoutDate = "2026-03-01"))

        val activeStay = stayDao.getActiveStayForPerson("p1")
        assertEquals(null, activeStay)
    }
}
