package com.leplay.syd

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local, on-device watch history for videos, music and Shorts.
 *
 * The list is kept newest-first and every entry carries the moment it was
 * watched. That timestamp is what lets the History page group entries into
 * "Today", "Yesterday", weekdays and older dates the way YouTube does.
 * The server (FEhistory) feed is merged in the UI; local entries win because
 * they are the ones recorded on this device and have a real watch time.
 */
object WatchHistory {
    const val KIND_VIDEO = 0
    const val KIND_MUSIC = 1
    const val KIND_SHORT = 2

    private const val PREFS = "watch_history"
    private const val KEY = "entries"
    private const val MAX = 200

    private val prefs by lazy {
        LePlayApplication.appContext.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
    }

    var items by mutableStateOf<List<VideoItem>>(emptyList())
        private set

    /** id -> KIND_* for entries recorded locally; server items have no entry here. */
    var kinds by mutableStateOf<Map<String, Int>>(emptyMap())
        private set

    /** id -> epoch millis of the most recent watch, for local entries. */
    var watchedAt by mutableStateOf<Map<String, Long>>(emptyMap())
        private set

    private var loaded = false

    fun load() {
        if (loaded) return
        loaded = true
        val raw = prefs.getString(KEY, null) ?: return
        runCatching {
            val array = JSONArray(raw)
            val out = ArrayList<VideoItem>(array.length())
            val kindMap = HashMap<String, Int>()
            val timeMap = HashMap<String, Long>()
            for (i in 0 until array.length()) {
                val json = array.optJSONObject(i) ?: continue
                val id = json.optString("id")
                if (id.isBlank()) continue
                out += VideoItem(
                    id = id,
                    title = json.optString("title"),
                    channel = json.optString("channel"),
                    channelUrl = json.optString("url").takeIf { it.isNotBlank() && it != "null" },
                    thumbnail = json.optString("thumb").takeIf { it.isNotBlank() && it != "null" },
                    avatar = json.optString("avatar").takeIf { it.isNotBlank() && it != "null" },
                    durationSec = json.optLong("duration"),
                    views = if (json.has("views")) json.optLong("views") else -1,
                    uploaded = json.optString("uploaded").takeIf { it.isNotBlank() },
                    isLive = json.optBoolean("live"),
                    verified = json.optBoolean("verified"),
                )
                kindMap[id] = json.optInt("kind", KIND_VIDEO)
                timeMap[id] = json.optLong("at", System.currentTimeMillis())
            }
            items = out
            kinds = kindMap
            watchedAt = timeMap
        }
    }

    fun record(video: VideoItem, kind: Int) {
        if (video.id.isBlank() || video.id.length != 11) return
        load()
        val fresh = video.copy(localUri = null)
        items = (listOf(fresh) + items.filter { it.id != fresh.id }).take(MAX)
        kinds = kinds + (fresh.id to kind)
        watchedAt = watchedAt + (fresh.id to System.currentTimeMillis())
        persist()
    }

    /** Remove one video from the local history. Safe to call for unknown ids. */
    fun remove(id: String) {
        load()
        if (items.none { it.id == id }) return
        items = items.filter { it.id != id }
        kinds = kinds - id
        watchedAt = watchedAt - id
        persist()
    }

    /** Remove several videos at once (used when clearing a date group). */
    fun remove(ids: Collection<String>) {
        if (ids.isEmpty()) return
        load()
        val set = ids.toHashSet()
        items = items.filter { it.id !in set }
        kinds = kinds.filterKeys { it !in set }
        watchedAt = watchedAt.filterKeys { it !in set }
        persist()
    }

    fun clear() {
        load()
        items = emptyList()
        kinds = emptyMap()
        watchedAt = emptyMap()
        persist()
    }

    fun contains(id: String): Boolean {
        load()
        return id in watchedAt
    }

    /** Watch time for a local entry; unknown for server-only entries. */
    fun timeOf(item: VideoItem): Long? = watchedAt[item.id]

    /** Kind for any history item: the locally recorded tag wins; server-only
     *  items fall back to the music heuristic, then to a short-duration guess. */
    fun kindOf(item: VideoItem): Int = kinds[item.id] ?: when {
        isMusicTrack(item) -> KIND_MUSIC
        item.durationSec in 1..62 -> KIND_SHORT
        else -> KIND_VIDEO
    }

    private fun persist() {
        runCatching {
            val array = JSONArray()
            items.forEach { item ->
                array.put(
                    JSONObject()
                        .put("id", item.id)
                        .put("title", item.title)
                        .put("channel", item.channel)
                        .put("url", item.channelUrl ?: "")
                        .put("thumb", item.thumbnail ?: "")
                        .put("avatar", item.avatar ?: "")
                        .put("duration", item.durationSec)
                        .put("views", item.views)
                        .put("uploaded", item.uploaded ?: "")
                        .put("live", item.isLive)
                        .put("verified", item.verified)
                        .put("at", watchedAt[item.id] ?: System.currentTimeMillis())
                        .put("kind", kinds[item.id] ?: KIND_VIDEO),
                )
            }
            prefs.edit().putString(KEY, array.toString()).apply()
        }
    }
}
