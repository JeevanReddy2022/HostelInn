package com.example.hostelmanager.ui.hostels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.Hostel
import com.example.hostelmanager.data.repository.HostelRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HostelViewModel(
    private val hostelRepository: HostelRepository
) : ViewModel() {

    val hostels: StateFlow<List<Hostel>> = hostelRepository.getAllHostels()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addHostel(name: String, address: String, phone: String) {
        viewModelScope.launch {
            hostelRepository.insertHostel(
                Hostel(
                    name = name,
                    address = address,
                    phone = phone
                )
            )
        }
    }
}
