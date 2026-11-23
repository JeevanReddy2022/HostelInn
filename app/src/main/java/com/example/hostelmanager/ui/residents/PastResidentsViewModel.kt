package com.example.hostelmanager.ui.residents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.repository.ResidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class PastResidentsViewModel(
    private val residentRepository: ResidentRepository
) : ViewModel() {

    private val _hostelId = MutableStateFlow("")

    val pastResidents: StateFlow<List<ResidentWithRoom>> = _hostelId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else residentRepository.getPastResidentsByHostel(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setHostelId(id: String) {
        _hostelId.value = id
    }
}
