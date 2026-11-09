package com.stopscroll.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.stopscroll.app.engine.AppRule
import com.stopscroll.app.engine.PackageRuntimeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stopscroll")

class StopScrollStore internal constructor(private val dataStore: DataStore<Preferences>) {
    constructor(context: Context) : this(context.applicationContext.dataStore)

    val rules: Flow<List<AppRule>> = dataStore.data.map { prefs ->
        decode(prefs[RULES_KEY], emptyList())
    }

    val runtime: Flow<Map<String, PackageRuntimeState>> = dataStore.data.map { prefs ->
        decode(prefs[RUNTIME_KEY], emptyMap())
    }

    suspend fun readRules(): List<AppRule> = rules.first()

    suspend fun readRuntime(): Map<String, PackageRuntimeState> = runtime.first()

    suspend fun readSnapshot(): StoredSnapshot {
        val prefs = dataStore.data.first()
        return StoredSnapshot(decode(prefs[RULES_KEY], emptyList()), decode(prefs[RUNTIME_KEY], emptyMap()))
    }

    suspend fun updateRules(transform: (List<AppRule>) -> List<AppRule>) {
        dataStore.edit { prefs ->
            val current = decode<List<AppRule>>(prefs[RULES_KEY], emptyList())
            prefs[RULES_KEY] = json.encodeToString(transform(current))
        }
    }

    suspend fun writeRuntime(value: Map<String, PackageRuntimeState>) {
        dataStore.edit { it[RUNTIME_KEY] = json.encodeToString<Map<String, PackageRuntimeState>>(value) }
    }

    private inline fun <reified T> decode(raw: String?, fallback: T): T =
        if (raw == null) fallback else runCatching { json.decodeFromString<T>(raw) }.getOrDefault(fallback)

    private companion object {
        val RULES_KEY = stringPreferencesKey("rules")
        val RUNTIME_KEY = stringPreferencesKey("runtime")
        val json = Json { ignoreUnknownKeys = true }
    }
}
