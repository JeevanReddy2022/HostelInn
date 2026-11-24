package com.example.hostelmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

abstract class BaseHostelViewModel : ViewModel() {

    protected val _hostelId = MutableStateFlow("")

    open fun setHostelId(id: String) {
        if (_hostelId.value != id) {
            _hostelId.value = id
            onHostelChanged()
        }
    }

    protected open fun onHostelChanged() {}

    @OptIn(ExperimentalCoroutinesApi::class)
    protected fun <T> hostelFlow(
        initialValue: T,
        transform: (hostelId: String) -> Flow<T>
    ): StateFlow<T> {
        return _hostelId.flatMapLatest { id ->
            if (id.isEmpty()) flowOf(initialValue)
            else transform(id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)
    }
}
