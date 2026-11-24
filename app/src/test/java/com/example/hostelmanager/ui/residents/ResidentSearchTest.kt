package com.example.hostelmanager.ui.residents

import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.ResidentWithRoom
import org.junit.Assert.assertEquals
import org.junit.Test

class ResidentSearchTest {

    private val sampleList = listOf(
        ResidentWithRoom(
            person = Person(
                id = "p1",
                name = "John Doe",
                phone = "9876543210",
                govtIdNumber = "234567890123",
                emergencyContact = "7777777777",
                dateOfBirth = "2000-01-01",
                address = "123 Main St",
                gender = "Male",
                guardianName = "Bob Doe"
            ),
            roomNumber = "101",
            monthlyRent = 500000
        ),
        ResidentWithRoom(
            person = Person(
                id = "p2",
                name = "Alice Smith",
                phone = "9123456780",
                govtIdNumber = "987654321098",
                emergencyContact = "8123456789",
                dateOfBirth = "2001-02-02",
                address = "456 Side St",
                gender = "Female",
                guardianName = "Carol Smith"
            ),
            roomNumber = "202",
            monthlyRent = 600000
        )
    )

    @Test
    fun `blank or whitespace query returns all residents`() {
        assertEquals(2, searchResidents(sampleList, "").size)
        assertEquals(2, searchResidents(sampleList, "   ").size)
    }

    @Test
    fun `search Residents matches name case-insensitively and trims query`() {
        assertEquals(1, searchResidents(sampleList, " john ").size)
        assertEquals("John Doe", searchResidents(sampleList, "JOHN")[0].person.name)
    }

    @Test
    fun `search Residents matches room number case-insensitively`() {
        assertEquals(1, searchResidents(sampleList, "202").size)
        assertEquals("Alice Smith", searchResidents(sampleList, "202")[0].person.name)
    }

    @Test
    fun `search Residents matches govt ID`() {
        assertEquals(1, searchResidents(sampleList, "234567890123").size)
        assertEquals("John Doe", searchResidents(sampleList, "234567890123")[0].person.name)
    }

    @Test
    fun `search Residents matches phone number`() {
        assertEquals(1, searchResidents(sampleList, "9123456780").size)
        assertEquals("Alice Smith", searchResidents(sampleList, "9123456780")[0].person.name)
    }

    @Test
    fun `search Residents matches emergency contact`() {
        assertEquals(1, searchResidents(sampleList, "7777777777").size)
        assertEquals("John Doe", searchResidents(sampleList, "7777777777")[0].person.name)
    }
}
