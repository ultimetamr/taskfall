package com.openai.taskfall.data.repository

import android.content.Context
import com.openai.taskfall.domain.model.TaskBucket
import com.openai.taskfall.domain.model.SessionSummary
import org.json.JSONArray
import org.json.JSONObject

interface TaskfallRepository {
    suspend fun loadRecentSessions(): List<SessionSummary>
    suspend fun saveSession(session: SessionSummary)
    suspend fun loadLastInput(): String
    suspend fun saveLastInput(input: String)
    suspend fun loadReduceMotion(): Boolean
    suspend fun saveReduceMotion(enabled: Boolean)
}

class InMemoryTaskfallRepository : TaskfallRepository {
    private val sessions = ArrayDeque<SessionSummary>()
    private var lastInput: String = ""
    private var reduceMotion: Boolean = false

    override suspend fun loadRecentSessions(): List<SessionSummary> = sessions.toList()

    override suspend fun saveSession(session: SessionSummary) {
        sessions.addFirst(session)
        while (sessions.size > 30) sessions.removeLast()
    }

    override suspend fun loadLastInput(): String = lastInput

    override suspend fun saveLastInput(input: String) {
        lastInput = input
    }

    override suspend fun loadReduceMotion(): Boolean = reduceMotion

    override suspend fun saveReduceMotion(enabled: Boolean) {
        reduceMotion = enabled
    }
}

class SharedPreferencesTaskfallRepository(context: Context) : TaskfallRepository {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override suspend fun loadRecentSessions(): List<SessionSummary> = runCatching {
        val stored = JSONArray(preferences.getString(KEY_SESSIONS, "[]"))
        buildList {
            repeat(stored.length()) { index ->
                val item = stored.getJSONObject(index)
                val counts = TaskBucket.values().associateWith { bucket ->
                    item.optInt(bucket.name, 0)
                }
                add(
                    SessionSummary(
                        createdAtEpochMs = item.getLong("createdAtEpochMs"),
                        total = item.getInt("total"),
                        counts = counts,
                    ),
                )
            }
        }
    }.getOrDefault(emptyList())

    override suspend fun saveSession(session: SessionSummary) {
        val updated = (listOf(session) + loadRecentSessions()).take(MAX_RECENT_SESSIONS)
        val stored = JSONArray()
        updated.forEach { summary ->
            stored.put(
                JSONObject().apply {
                    put("createdAtEpochMs", summary.createdAtEpochMs)
                    put("total", summary.total)
                    TaskBucket.values().forEach { bucket -> put(bucket.name, summary.counts[bucket] ?: 0) }
                },
            )
        }
        preferences.edit().putString(KEY_SESSIONS, stored.toString()).apply()
    }

    override suspend fun loadLastInput(): String = preferences.getString(KEY_LAST_INPUT, "").orEmpty()

    override suspend fun saveLastInput(input: String) {
        preferences.edit().putString(KEY_LAST_INPUT, input).apply()
    }

    override suspend fun loadReduceMotion(): Boolean = preferences.getBoolean(KEY_REDUCE_MOTION, false)

    override suspend fun saveReduceMotion(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_REDUCE_MOTION, enabled).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "taskfall"
        const val KEY_SESSIONS = "recent_sessions"
        const val KEY_LAST_INPUT = "last_input"
        const val KEY_REDUCE_MOTION = "reduce_motion"
        const val MAX_RECENT_SESSIONS = 30
    }
}
