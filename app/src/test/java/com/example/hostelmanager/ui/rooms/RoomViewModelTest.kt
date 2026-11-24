package com.example.hostelmanager.ui.rooms

import com.example.hostelmanager.testutil.FakeRoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val roomRepository = FakeRoomRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setting same hostelId preserves room filters`() = runTest {
        val viewModel = RoomViewModel(roomRepository)
        viewModel.setHostelId("h1")
        viewModel.setQuery("101")
        viewModel.setSort(RoomSort.RENT_DESC)

        assertTrue(viewModel.filters.value.isFiltered)
        assertEquals("101", viewModel.filters.value.query)
        assertEquals(RoomSort.RENT_DESC, viewModel.filters.value.sort)

        // Setting same hostelId must NOT reset filters
        viewModel.setHostelId("h1")
        assertTrue(viewModel.filters.value.isFiltered)
        assertEquals("101", viewModel.filters.value.query)
        assertEquals(RoomSort.RENT_DESC, viewModel.filters.value.sort)
    }

    @Test
    fun `changing hostelId resets room filters`() = runTest {
        val viewModel = RoomViewModel(roomRepository)
        viewModel.setHostelId("h1")
        viewModel.setQuery("101")
        viewModel.setSort(RoomSort.RENT_DESC)

        assertTrue(viewModel.filters.value.isFiltered)

        // Changing hostelId MUST reset filters
        viewModel.setHostelId("h2")
        assertFalse(viewModel.filters.value.isFiltered)
        assertEquals("", viewModel.filters.value.query)
        assertEquals(RoomSort.NUMBER_ASC, viewModel.filters.value.sort)
    }
}
