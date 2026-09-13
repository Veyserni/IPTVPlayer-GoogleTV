package com.halit.iptv.data

import com.halit.iptv.model.IptvItem
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class PlaylistRepository {
    fun loadM3u(url: String): List<IptvItem> {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 12_000
        conn.instanceFollowRedirects = true
        conn.setRequestProperty("User-Agent", "VLC/3.0.21 LibVLC/3.0.21")
        conn.setRequestProperty("Accept", "*/*")
        val deadlineNanos = System.nanoTime() + 45_000_000_000L
        return try {
            val code = conn.responseCode
            if (code !in 200..299) error("Sunucu HTTP $code döndürdü")
            conn.inputStream.bufferedReader().use { reader ->
                M3uParser.parse(reader, deadlineNanos)
            }.also {
                if (it.isEmpty()) error("Sunucudan kanal listesi gelmedi")
            }
        } catch (e: SocketTimeoutException) {
            error("Sunucu zaman aşımına uğradı. İnternet bağlantısını veya IPTV sunucusunu kontrol et.")
        } finally {
            conn.disconnect()
        }
    }
}
