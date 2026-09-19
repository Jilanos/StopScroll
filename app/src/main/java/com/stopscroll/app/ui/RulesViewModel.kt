package com.stopscroll.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stopscroll.app.data.StopScrollStore
import com.stopscroll.app.engine.AppRule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RulesViewModel(private val store: StopScrollStore) : ViewModel() {

    val rules: StateFlow<List<AppRule>> =
        store.rules.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun upsert(rule: AppRule) = mutate { current ->
        val others = current.filterNot { it.packageName == rule.packageName }
        (others + rule).sortedBy { it.label.lowercase() }
    }

    fun remove(packageName: String) = mutate { current ->
        current.filterNot { it.packageName == packageName }
    }

    fun setEnabled(packageName: String, enabled: Boolean) = mutate { current ->
        current.map { if (it.packageName == packageName) it.copy(enabled = enabled) else it }
    }

    private fun mutate(transform: (List<AppRule>) -> List<AppRule>) {
        viewModelScope.launch { store.writeRules(transform(store.readRules())) }
    }

    companion object {
        fun factory(store: StopScrollStore) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = RulesViewModel(store) as T
        }
    }
}
