package com.leplay.syd

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Tiny on-disk cache for the feeds. The home feed is opened on every cold
 * start, so keeping the last page locally makes the app open instantly and
 * lets it fall back to something useful when the network is down. Everything
 * is plain SharedPreferences + org.json; no new dependencies and no threads.
 *
 * The cache is only a snapshot: [FeedState] still refreshes from YouTube in
 * the background (stale-while-revalidate) and overwrites it on success.
 */
object FeedCache {
    private const val PREFS = "feed_cache"
    private const val HOME = "home"
    private const val HISTORY = "history"
    private const val ANON_SEEN = "anon_seen"
    private const val HOME_TTL_MS = 30 * 60 * 1000L
    private const val HISTORY_TTL_MS = 10 * 60 * 1000L
    private const val ANON_SEEN_MAX = 500

    private val prefs by lazy {
        LePlayApplication.appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    fun loadHome(): FeedPage? = load(HOME, HOME_TTL_MS)

    fun saveHome(page: FeedPage) = save(HOME, page)

    fun loadHistory(): FeedPage? = load(HISTORY, HISTORY_TTL_MS)

    fun saveHistory(page: FeedPage) = save(HISTORY, page)

    fun clearHome() = prefs.edit().remove(HOME).apply()

    fun clearHistory() = prefs.edit().remove(HISTORY).apply()

    /** Video ids already shown in the signed-out feed, kept across restarts. */
    fun loadAnonSeen(): LinkedHashSet<String> {
        val raw = prefs.getString(ANON_SEEN, null) ?: return LinkedHashSet()
        val result = LinkedHashSet<String>()
        runCatching {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val id = array.optString(i)
                if (id.isNotBlank()) result += id
            }
        }
        return result
    }

    fun saveAnonSeen(ids: Collection<String>) {
        val array = JSONArray()
        ids.take(ANON_SEEN_MAX).forEach { array.put(it) }
        prefs.edit().putString(ANON_SEEN, array.toString()).apply()
    }

    private fun load(key: String, ttlMs: Long): FeedPage? {
        val raw = prefs.getString(key, null) ?: return null
        val json = runCatching { JSONObject(raw) }.getOrNull() ?: return null
        if (System.currentTimeMillis() - json.optLong("at", 0L) > ttlMs) return null
        val array = json.optJSONArray("videos") ?: return null
        val videos = ArrayList<VideoItem>(array.length())
        for (i in 0 until array.length()) {
            val v = array.optJSONObject(i) ?: continue
            val id = v.optString("id")
            if (id.isBlank()) continue
            videos += VideoItem(
                id = id,
                title = v.optString("title"),
                channel = v.optString("channel"),
                channelUrl = v.optString("url").takeIf { it.isNotBlank() && it != "null" },
                thumbnail = v.optString("thumb").takeIf { it.isNotBlank() && it != "null" },
                avatar = v.optString("avatar").takeIf { it.isNotBlank() && it != "null" },
                durationSec = v.optLong("duration"),
                views = if (v.has("views")) v.optLong("views") else -1,
                uploaded = v.optString("uploaded").takeIf { it.isNotBlank() },
                isLive = v.optBoolean("live"),
                verified = v.optBoolean("verified"),
            )
        }
        if (videos.isEmpty()) return null
        return FeedPage(videos, json.optString("next").takeIf { it.isNotBlank() && it != "null" })
    }

    private fun save(key: String, page: FeedPage) {
        if (page.videos.isEmpty()) return
        runCatching {
            val array = JSONArray()
            page.videos.forEach { v ->
                array.put(
                    JSONObject()
                        .put("id", v.id)
                        .put("title", v.title)
                        .put("channel", v.channel)
                        .put("url", v.channelUrl ?: "")
                        .put("thumb", v.thumbnail ?: "")
                        .put("avatar", v.avatar ?: "")
                        .put("duration", v.durationSec)
                        .put("views", v.views)
                        .put("uploaded", v.uploaded ?: "")
                        .put("live", v.isLive)
                        .put("verified", v.verified),
                )
            }
            prefs.edit().putString(
                key,
                JSONObject()
                    .put("at", System.currentTimeMillis())
                    .put("videos", array)
                    .put("next", page.next ?: "")
                    .toString(),
            ).apply()
        }
    }
}
