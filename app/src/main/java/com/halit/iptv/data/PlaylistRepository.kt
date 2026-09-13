package com.halit.iptv.data

import com.halit.iptv.model.IptvItem
import com.halit.iptv.model.XtreamCredentials
import java.net.HttpURLConnection
import java.net.URL

class PlaylistRepository {

    private fun open(url: String, accept: String = "*/*"): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            // Büyük IPTV listelerinde veri geldikçe süre sıfırlanır; kısa bir toplam süre limiti yoktur.
            readTimeout = 60_000
            instanceFollowRedirects = true
            useCaches = false
            setRequestProperty("User-Agent", "VLC/3.0.21 LibVLC/3.0.21")
            setRequestProperty("Accept", accept)
            setRequestProperty("Accept-Encoding", "identity")
            setRequestProperty("Connection", "keep-alive")
        }
    }

    fun validateXtream(credentials: XtreamCredentials) {
        val conn = open(Xtream.api(credentials), "application/json,*/*")
        try {
            val code = conn.responseCode
            if (code !in 200..299) error("Sunucu hesap kontrolünde HTTP $code döndürdü")
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val compact = body.replace(" ", "").replace("\n", "").replace("\r", "")
            val authenticated = compact.contains("\"auth\":1") || compact.contains("\"auth\":\"1\"")
            if (!authenticated) {
                if (compact.contains("user_info")) error("Kullanıcı adı veya şifre kabul edilmedi")
                error("Sunucu Xtream hesap yanıtı vermedi")
            }
        } finally {
            conn.disconnect()
        }
    }

    /**
     * Windows sürümüne daha yakın davranış: M3U'nun tamamını readText() ile beklemek yerine
     * satır satır işler ve her batch geldiğinde UI'ya yayınlar.
     */
    fun loadM3uStreaming(
        url: String,
        batchSize: Int = 250,
        onBatch: (List<IptvItem>) -> Unit,
    ): List<IptvItem> {
        val conn = open(url)
        try {
            val code = conn.responseCode
            if (code !in 200..299) error("Sunucu HTTP $code döndürdü")

            val all = ArrayList<IptvItem>(4096)
            val batch = ArrayList<IptvItem>(minOf(batchSize, 4096))
            var extinf: String? = null

            conn.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { raw ->
                    val line = raw.trim()
                    when {
                        line.startsWith("#EXTINF", ignoreCase = true) -> extinf = line
                        line.isNotEmpty() && !line.startsWith("#") && extinf != null -> {
                            val item = M3uParser.parseEntry(extinf!!, line)
                            all += item
                            batch += item
                            extinf = null
                            if (batch.size >= batchSize) {
                                onBatch(batch.toList())
                                batch.clear()
                            }
                        }
                    }
                }
            }
            if (batch.isNotEmpty()) onBatch(batch.toList())
            if (all.isEmpty()) error("Sunucu boş bir kanal listesi döndürdü")
            return all
        } finally {
            conn.disconnect()
        }
    }

    fun loadM3u(url: String): List<IptvItem> = loadM3uStreaming(url, Int.MAX_VALUE) { }
}
