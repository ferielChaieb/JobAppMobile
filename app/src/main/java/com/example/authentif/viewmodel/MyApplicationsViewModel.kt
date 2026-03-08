package com.example.authentif.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authentif.data.FirebaseMyApplicationsRepository
import com.example.authentif.data.MyApplicationsRepository
import com.example.authentif.data.models.ApplicationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MyApplicationsUiState(
    val loading: Boolean = false,
    val allItems: List<ApplicationItem> = emptyList(),
    val filteredItems: List<ApplicationItem> = emptyList(),
    val error: String? = null,
    val selectedTab: Int = 0
)

class MyApplicationsViewModel(
    private val repo: MyApplicationsRepository = FirebaseMyApplicationsRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(MyApplicationsUiState())
    val ui: StateFlow<MyApplicationsUiState> = _ui

    fun loadMyApplications() {
        val candidateId = repo.getCurrentUserId()
        if (candidateId.isNullOrBlank()) {
            _ui.value = _ui.value.copy(error = "Pas connecté")
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)

            val result = repo.getMyApplications(candidateId)
            if (result.isSuccess) {
                val items = result.getOrNull().orEmpty()
                val currentTab = _ui.value.selectedTab
                _ui.value = _ui.value.copy(
                    loading = false,
                    allItems = items,
                    filteredItems = filterByTab(items, currentTab),
                    error = null
                )
            } else {
                _ui.value = _ui.value.copy(
                    loading = false,
                    error = result.exceptionOrNull()?.message ?: "Erreur"
                )
            }
        }
    }

    fun selectTab(tabPosition: Int) {
        val items = _ui.value.allItems
        _ui.value = _ui.value.copy(
            selectedTab = tabPosition,
            filteredItems = filterByTab(items, tabPosition)
        )
    }

    private fun filterByTab(items: List<ApplicationItem>, tabPosition: Int): List<ApplicationItem> {
        return when (tabPosition) {
            1 -> items.filter { it.status == "pending" }
            2 -> items.filter { it.status == "accepted" }
            3 -> items.filter { it.status == "rejected" }
            else -> items
        }
    }

    fun totalCount(): Int = _ui.value.allItems.size
    fun pendingCount(): Int = _ui.value.allItems.count { it.status == "pending" }
    fun acceptedCount(): Int = _ui.value.allItems.count { it.status == "accepted" }
    fun rejectedCount(): Int = _ui.value.allItems.count { it.status == "rejected" }
}