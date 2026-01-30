package com.routinetool.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.routinetool.domain.model.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesDataStore(private val context: Context) {

    // Sort preference
    private val SORT_PREFERENCE_KEY = stringPreferencesKey("sort_preference")

    val sortPreference: Flow<SortOption> = context.dataStore.data.map { prefs ->
        prefs[SORT_PREFERENCE_KEY]?.let {
            try { SortOption.valueOf(it) } catch (e: IllegalArgumentException) { SortOption.URGENCY }
        } ?: SortOption.URGENCY
    }

    suspend fun saveSortPreference(option: SortOption) {
        context.dataStore.edit { prefs ->
            prefs[SORT_PREFERENCE_KEY] = option.name
        }
    }

    // Focus config
    private val FOCUS_TASK_LIMIT_KEY = intPreferencesKey("focus_task_limit")
    private val FOCUS_PINNED_TASK_IDS_KEY = stringSetPreferencesKey("focus_pinned_task_ids")

    val focusTaskLimit: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[FOCUS_TASK_LIMIT_KEY] ?: 5  // Default 5 tasks (cognitive comfort zone)
    }

    val focusPinnedTaskIds: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[FOCUS_PINNED_TASK_IDS_KEY] ?: emptySet()
    }

    suspend fun saveFocusTaskLimit(limit: Int) {
        context.dataStore.edit { prefs ->
            prefs[FOCUS_TASK_LIMIT_KEY] = limit
        }
    }

    suspend fun saveFocusPinnedTaskIds(ids: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[FOCUS_PINNED_TASK_IDS_KEY] = ids
        }
    }

    // Edit screen expansion states
    private val NOTES_EXPANDED_KEY = booleanPreferencesKey("notes_expanded")
    private val DEADLINES_EXPANDED_KEY = booleanPreferencesKey("deadlines_expanded")
    private val SUBTASKS_EXPANDED_KEY = booleanPreferencesKey("subtasks_expanded")

    val notesExpanded: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTES_EXPANDED_KEY] ?: false
    }

    val deadlinesExpanded: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DEADLINES_EXPANDED_KEY] ?: false
    }

    val subtasksExpanded: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SUBTASKS_EXPANDED_KEY] ?: false
    }

    suspend fun saveNotesExpanded(expanded: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTES_EXPANDED_KEY] = expanded
        }
    }

    suspend fun saveDeadlinesExpanded(expanded: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DEADLINES_EXPANDED_KEY] = expanded
        }
    }

    suspend fun saveSubtasksExpanded(expanded: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SUBTASKS_EXPANDED_KEY] = expanded
        }
    }
}
