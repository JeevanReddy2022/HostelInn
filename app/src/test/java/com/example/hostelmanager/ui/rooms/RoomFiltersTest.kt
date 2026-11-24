package com.example.hostelmanager.ui.rooms

import com.example.hostelmanager.data.RoomEntity
import com.example.hostelmanager.data.RoomWithOccupancy
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomFiltersTest {

    private val r1 = RoomWithOccupancy(
        room = RoomEntity(id = "r1", hostelId = "h1", roomNumber = "2", floor = 0, bedCount = 1, defaultRentPerBed = 500000),
        occupiedBeds = 1
    )
    private val r2 = RoomWithOccupancy(
        room = RoomEntity(id = "r2", hostelId = "h1", roomNumber = "10", floor = 1, bedCount = 2, defaultRentPerBed = 400000),
        occupiedBeds = 1
    )
    private val r3 = RoomWithOccupancy(
        room = RoomEntity(id = "r3", hostelId = "h1", roomNumber = "301", floor = 2, bedCount = 3, defaultRentPerBed = 600000),
        occupiedBeds = 0
    )

    private val sampleRooms = listOf(r2, r1, r3)

    @Test
    fun `numeric sorting orders room 2 before room 10 in NUMBER_ASC`() {
        val sorted = filterAndSortRooms(sampleRooms, RoomFilters(sort = RoomSort.NUMBER_ASC))
        assertEquals("2", sorted[0].room.roomNumber)
        assertEquals("10", sorted[1].room.roomNumber)
        assertEquals("301", sorted[2].room.roomNumber)
    }

    @Test
    fun `all 8 RoomSort values sort correctly`() {
        // NUMBER_ASC
        assertEquals("2", filterAndSortRooms(sampleRooms, RoomFilters(sort = RoomSort.NUMBER_ASC))[0].room.roomNumber)
        // NUMBER_DESC
        assertEquals("301", filterAndSortRooms(sampleRooms, RoomFilters(sort = RoomSort.NUMBER_DESC))[0].room.roomNumber)
        // FLOOR_ASC
        assertEquals(0, filterAndSortRooms(sampleRooms, RoomFilters(sort = RoomSort.FLOOR_ASC))[0].room.floor)
        // SHARING_ASC
        assertEquals(1, filterAndSortRooms(sampleRooms, RoomFilters(sort = RoomSort.SHARING_ASC))[0].room.bedCount)
        // SHARING_DESC
        assertEquals(3, filterAndSortRooms(sampleRooms, RoomFilters(sort = RoomSort.SHARING_DESC))[0].room.bedCount)
        // VACANCY_DESC
        assertEquals("301", filterAndSortRooms(sampleRooms, RoomFilters(sort = RoomSort.VACANCY_DESC))[0].room.roomNumber)
        // RENT_ASC
        assertEquals(400000L, filterAndSortRooms(sampleRooms, RoomFilters(sort = RoomSort.RENT_ASC))[0].room.defaultRentPerBed)
        // RENT_DESC
        assertEquals(600000L, filterAndSortRooms(sampleRooms, RoomFilters(sort = RoomSort.RENT_DESC))[0].room.defaultRentPerBed)
    }

    @Test
    fun `availability filters correctly`() {
        // ALL
        assertEquals(3, filterAndSortRooms(sampleRooms, RoomFilters(availability = RoomAvailability.ALL)).size)
        // AVAILABLE
        assertEquals(2, filterAndSortRooms(sampleRooms, RoomFilters(availability = RoomAvailability.AVAILABLE)).size)
        // FULL
        assertEquals(1, filterAndSortRooms(sampleRooms, RoomFilters(availability = RoomAvailability.FULL)).size)
    }

    @Test
    fun `floor and sharing filters work correctly`() {
        assertEquals(1, filterAndSortRooms(sampleRooms, RoomFilters(floor = 1)).size)
        assertEquals("10", filterAndSortRooms(sampleRooms, RoomFilters(floor = 1))[0].room.roomNumber)

        assertEquals(1, filterAndSortRooms(sampleRooms, RoomFilters(sharing = 3)).size)
        assertEquals("301", filterAndSortRooms(sampleRooms, RoomFilters(sharing = 3))[0].room.roomNumber)
    }

    @Test
    fun `case-insensitive query filtering works`() {
        assertEquals(1, filterAndSortRooms(sampleRooms, RoomFilters(query = " 301 ")).size)
    }

    @Test
    fun `summarizeRooms aggregates counts correctly`() {
        val summary = summarizeRooms(sampleRooms)
        assertEquals(3, summary.rooms)
        assertEquals(6, summary.beds)
        assertEquals(2, summary.occupied)
        assertEquals(1, summary.fullRooms)
        assertEquals(4, summary.vacant)
        assertEquals(2, summary.openRooms)
    }

    @Test
    fun `floorLabel formats correctly`() {
        assertEquals("Ground Floor", floorLabel(0))
        assertEquals("Floor 2", floorLabel(2))
    }

    @Test
    fun `sharingName formats correctly`() {
        assertEquals("Single occupancy", sharingName(1))
        assertEquals("Double occupancy", sharingName(2))
        assertEquals("Triple occupancy", sharingName(3))
        assertEquals("Four occupancy", sharingName(4))
        assertEquals("5 occupancy", sharingName(5))
    }
}
