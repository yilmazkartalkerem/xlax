package com.xlax.launcher

import android.content.Context
import android.text.Html
import java.net.HttpURLConnection
import java.net.URL

data class Video(val id: String, val title: String) {
    val url get() = "https://www.youtube.com/watch?v=$id"
    val thumb get() = "https://i.ytimg.com/vi/$id/hqdefault.jpg"
}

object YouTube {
    private fun get(u: String): String {
        val c = URL(u).openConnection() as HttpURLConnection
        c.connectTimeout = 8000; c.readTimeout = 8000
        c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile")
        c.setRequestProperty("Accept-Language", "tr-TR,tr;q=0.9")
        c.setRequestProperty("Cookie", "CONSENT=YES+1; SOCS=CAI")
        return c.inputStream.bufferedReader().use { it.readText() }
    }

    /** Kanalın son videolarını RSS'ten alır (API anahtarı gerekmez). Arka plan iş parçacığında çağır. */
    fun latest(ctx: Context): List<Video> {
        val prefs = ctx.getSharedPreferences("xlax", Context.MODE_PRIVATE)
        var id = Config.CHANNEL_ID.ifEmpty { prefs.getString("channel_id", "") ?: "" }
        if (id.isEmpty()) {
            val html = get(Config.CHANNEL_URL)
            id = Regex("\"(?:channelId|externalId)\":\"(UC[\\w-]{22})\"").find(html)?.groupValues?.get(1)
                ?: Regex("channel_id=(UC[\\w-]{22})").find(html)?.groupValues?.get(1) ?: return emptyList()
            prefs.edit().putString("channel_id", id).apply()
        }
        val xml = get("https://www.youtube.com/feeds/videos.xml?channel_id=$id")
        return Regex("<entry>(.*?)</entry>", RegexOption.DOT_MATCHES_ALL).findAll(xml).mapNotNull { m ->
            val e = m.groupValues[1]
            val vid = Regex("<yt:videoId>(.*?)</yt:videoId>").find(e)?.groupValues?.get(1) ?: return@mapNotNull null
            val title = Regex("<title>(.*?)</title>").find(e)?.groupValues?.get(1) ?: ""
            Video(vid, Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY).toString())
        }.take(Config.MAX_VIDEOS).toList()
    }
}
