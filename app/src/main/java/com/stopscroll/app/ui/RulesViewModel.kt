package com.stopscroll.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stopscroll.app.data.StopScrollStore
import com.stopscroll.app.engine.AppRule
import com.stopscroll.app.engine.TimerStatus
import com.stopscroll.app.engine.localEpochDay
import com.stopscroll.app.engine.timerStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RulesViewModel(private val store: StopScrollStore) : ViewModel() {

    val rules: StateFlow<List<AppRule>> =
        store.rules.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Wall time every second, so a running pause or block moves while the screen is visible. */
    private val now = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(TICK_MILLIS)
        }
    }

    /** Timers and today's usage per package, read from the state the service persists. */
    val statuses: StateFlow<Map<String, TimerStatus>> =
        combine(store.rules, store.runtime, now) { rules, runtime, wallNow ->
            val today = localEpochDay(wallNow)
            rules.associate { it.packageName to timerStatus(it, runtime[it.packageName], wallNow, today) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

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
        viewModelScope.launch { store.updateRules(transform) }
    }

    companion object {
        private const val TICK_MILLIS = 1_000L

        fun factory(store: StopScrollStore) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = RulesViewModel(store) as T
        }
    }
}
