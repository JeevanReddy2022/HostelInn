package com.example.hostelmanager.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseHostelViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class TestHostelViewModel : BaseHostelViewModel() {
        var hostelChangedCount = 0

        override fun onHostelChanged() {
            hostelChangedCount++
        }

        val items: StateFlow<List<String>> = hostelFlow(emptyList<String>()) { id ->
            flowOf(listOf("item_for_$id"))
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty id emits initial value`() = runTest {
        val viewModel = TestHostelViewModel()
        backgroundScope.launch { viewModel.items.collect() }
        assertEquals(emptyList<String>(), viewModel.items.value)
    }

    @Test
    fun `setting id emits transform result`() = runTest {
        val viewModel = TestHostelViewModel()
        backgroundScope.launch { viewModel.items.collect() }
        viewModel.setHostelId("h1")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf("item_for_h1"), viewModel.items.value)
    }

    @Test
    fun `changing id switches to the new flow`() = runTest {
        val viewModel = TestHostelViewModel()
        backgroundScope.launch { viewModel.items.collect() }
        viewModel.setHostelId("h1")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf("item_for_h1"), viewModel.items.value)

        viewModel.setHostelId("h2")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf("item_for_h2"), viewModel.items.value)
        assertEquals(2, viewModel.hostelChangedCount)
    }

    @Test
    fun `setting the same id does not call onHostelChanged`() = runTest {
        val viewModel = TestHostelViewModel()
        backgroundScope.launch { viewModel.items.collect() }
        viewModel.setHostelId("h1")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.hostelChangedCount)

        viewModel.setHostelId("h1")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.hostelChangedCount)
    }
}
