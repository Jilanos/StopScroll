package com.stopscroll.app.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.stopscroll.app.engine.AppRule
import com.stopscroll.app.engine.PackageRuntimeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class StopScrollStoreTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun `overlapping rule edits preserve both changes and unrelated runtime`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val store = StopScrollStore(PreferenceDataStoreFactory.create(scope = scope) {
                folder.root.resolve("rules.preferences_pb")
            })
            val rules = (1..2).map { AppRule("app.$it", "App $it", 60_000L, 60_000L) }
            store.updateRules { rules }
            val savedRuntime = mapOf("app.1" to PackageRuntimeState(42_000L))
            store.writeRuntime(savedRuntime)
            rules.map { rule ->
                async(start = CoroutineStart.UNDISPATCHED) {
                    store.updateRules { current ->
                        current.map { if (it.packageName == rule.packageName) it.copy(enabled = false) else it }
                    }
                }
            }.awaitAll()
            val saved = store.readSnapshot()
            assertEquals(2, saved.rules.size)
            assertTrue(saved.rules.none { it.enabled })
            assertEquals(savedRuntime, saved.runtime)

            listOf(
                async(start = CoroutineStart.UNDISPATCHED) { store.updateRules { it.filterNot { r -> r.packageName == "app.1" } } },
                async(start = CoroutineStart.UNDISPATCHED) {
                    store.updateRules { it + AppRule("app.3", "App 3", 60_000L, 60_000L) }
                },
            ).awaitAll()
            assertEquals(setOf("app.2", "app.3"), store.readRules().map { it.packageName }.toSet())
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `runtime saved by the previous version still decodes with the new fields defaulted`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val dataStore = PreferenceDataStoreFactory.create(scope = scope) {
                folder.root.resolve("legacy.preferences_pb")
            }
            val legacy = """{"app.1":{"remainingUsageMillis":42000,"warned":true},""" +
                """"app.2":{"cooldownEndsAtWallMillis":1700000060000,"cooldownMillis":120000}}"""
            dataStore.edit { it[stringPreferencesKey("runtime")] = legacy }

            val runtime = StopScrollStore(dataStore).readRuntime()

            assertEquals(PackageRuntimeState(remainingUsageMillis = 42_000L, warned = true), runtime["app.1"])
            assertNull(runtime["app.1"]?.pausedSinceWallMillis)
            assertEquals(0L, runtime["app.1"]?.usedTodayMillis)
            assertEquals(
                PackageRuntimeState(cooldownEndsAtWallMillis = 1_700_000_060_000L, cooldownMillis = 120_000L),
                runtime["app.2"],
            )
        } finally {
            scope.cancel()
        }
    }
}
