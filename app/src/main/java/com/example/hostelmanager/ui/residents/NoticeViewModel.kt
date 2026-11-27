package com.example.hostelmanager.ui.residents

import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.ui.BaseHostelViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NoticeViewModel(
    private val residentRepository: ResidentRepository
) : BaseHostelViewModel() {

    val noticeResidents: StateFlow<List<ResidentWithRoom>> =
        hostelFlow(emptyList<ResidentWithRoom>()) { id ->
            residentRepository.getNoticeResidentsByHostel(id)
        }

    fun cancelNotice(personId: String) {
        viewModelScope.launch {
            val stay = residentRepository.getActiveStayForPerson(personId) ?: return@launch
            residentRepository.updateNoticeDate(stay, null)
        }
    }
}