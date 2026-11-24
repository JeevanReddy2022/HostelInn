package com.example.hostelmanager.ui.residents

import com.example.hostelmanager.testutil.FakeResidentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResidentListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val residentRepository = FakeResidentRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setting same hostelId preserves search query`() = runTest {
        val viewModel = ResidentListViewModel(residentRepository)
        viewModel.setHostelId("h1")
        viewModel.setQuery("John")
        assertEquals("John", viewModel.query.value)

        // Setting same hostelId must NOT clear query
        viewModel.setHostelId("h1")
        assertEquals("John", viewModel.query.value)
    }

    @Test
    fun `changing hostelId resets search query`() = runTest {
        val viewModel = ResidentListViewModel(residentRepository)
        viewModel.setHostelId("h1")
        viewModel.setQuery("John")
        assertEquals("John", viewModel.query.value)

        // Changing hostelId MUST clear query
        viewModel.setHostelId("h2")
        assertEquals("", viewModel.query.value)
    }
}
