package com.example.hostelmanager.ui.residents

import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.ui.BaseHostelViewModel
import kotlinx.coroutines.flow.StateFlow

class PastResidentsViewModel(
    private val residentRepository: ResidentRepository
) : BaseHostelViewModel() {

    val pastResidents: StateFlow<List<ResidentWithRoom>> = hostelFlow(emptyList<ResidentWithRoom>()) { id ->
        residentRepository.getPastResidentsByHostel(id)
    }
}
