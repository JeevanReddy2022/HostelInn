package com.example.hostelmanager.ui.residents

import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.ui.BaseHostelViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal fun searchResidents(
    residents: List<ResidentWithRoom>,
    rawQuery: String
): List<ResidentWithRoom> {
    val q = rawQuery.trim()
    if (q.isEmpty()) return residents

    return residents.filter { r ->
        r.person.name.contains(q, ignoreCase = true) ||
                r.roomNumber.contains(q, ignoreCase = true) ||
                r.person.govtIdNumber.contains(q, ignoreCase = true) ||
                r.person.phone.contains(q) ||
                r.person.emergencyContact.contains(q)
    }
}

class ResidentListViewModel(
    private val residentRepository: ResidentRepository
) : BaseHostelViewModel() {

    val residents: StateFlow<List<ResidentWithRoom>> = hostelFlow(emptyList<ResidentWithRoom>()) { id ->
        residentRepository.getActiveResidentsByHostel(id)
    }

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    override fun onHostelChanged() {
        _query.value = ""
    }

    fun setQuery(value: String) {
        _query.value = value.take(40)
    }

    fun clearSearch() {
        _query.value = ""
    }
}
