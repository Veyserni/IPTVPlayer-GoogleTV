package com.halit.iptv.data

import com.halit.iptv.model.IptvItem
import java.net.HttpURLConnection
import java.net.URL

class PlaylistRepository {
    fun loadM3u(url: String): List<IptvItem> {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000
        conn.readTimeout = 30_000
        conn.instanceFollowRedirects = true
        conn.setRequestProperty("User-Agent", "VLC/3.0.21 LibVLC/3.0.21")
        conn.setRequestProperty("Accept", "*/*")
        return try {
            val code = conn.responseCode
            if (code !in 200..299) error("Sunucu HTTP $code döndürdü")
            conn.inputStream.bufferedReader().use { M3uParser.parse(it.readText()) }
        } finally { conn.disconnect() }
    }
}
