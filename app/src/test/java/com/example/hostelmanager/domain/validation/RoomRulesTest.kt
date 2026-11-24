package com.example.hostelmanager.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RoomRulesTest {

    @Test
    fun `roomNumberError validates room numbers`() {
        assertNull(RoomRules.roomNumberError("", isNewRoom = true))
        assertNull(RoomRules.roomNumberError("   ", isNewRoom = true))
        assertEquals("Room number is required", RoomRules.roomNumberError("", isNewRoom = false))
        assertEquals("Room number is required", RoomRules.roomNumberError("   ", isNewRoom = false))
        assertEquals("Room number must be greater than zero", RoomRules.roomNumberError("0", isNewRoom = true))
        assertEquals("Room number must be greater than zero", RoomRules.roomNumberError("-5", isNewRoom = false))
        assertNull(RoomRules.roomNumberError("101", isNewRoom = true))
        assertNull(RoomRules.roomNumberError("A-12", isNewRoom = false))
    }

    @Test
    fun `floorError validates floor numbers`() {
        assertEquals("Floor is required", RoomRules.floorError(""))
        assertEquals("Floor is required", RoomRules.floorError("   "))
        assertEquals("Floor cannot be negative or empty", RoomRules.floorError("-1"))
        assertEquals("Floor cannot be negative or empty", RoomRules.floorError("abc"))
        assertNull(RoomRules.floorError("0"))
        assertNull(RoomRules.floorError("2"))
    }

    @Test
    fun `bedsError validates bed counts and occupied bounds`() {
        assertEquals("Must be at least 1", RoomRules.bedsError("", 0))
        assertEquals("Must be at least 1", RoomRules.bedsError("0", 0))
        assertEquals("Must be at least 1", RoomRules.bedsError("-1", 0))
        assertEquals("2 resident(s) live here. Beds cannot be below 2.", RoomRules.bedsError("1", 2))
        assertNull(RoomRules.bedsError("2", 2))
        assertNull(RoomRules.bedsError("3", 2))
    }

    @Test
    fun `rentError validates rent for new vs existing rooms`() {
        assertNull(RoomRules.rentError("", isNewRoom = true))
        assertNotNull(RoomRules.rentError("", isNewRoom = false))
        assertNotNull(RoomRules.rentError("0", isNewRoom = true))
        assertNull(RoomRules.rentError("5000", isNewRoom = true))
        assertNull(RoomRules.rentError("5000", isNewRoom = false))
    }
}
