package com.halit.iptv.data

import com.halit.iptv.model.XtreamCredentials
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Xtream {
    private fun enc(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8.toString())
    fun api(c: XtreamCredentials, action: String? = null): String = buildString {
        append(c.baseUrl).append("/player_api.php?username=").append(enc(c.username))
        append("&password=").append(enc(c.password))
        if (!action.isNullOrBlank()) append("&action=").append(enc(action))
    }
    fun playlist(c: XtreamCredentials): String = "${c.baseUrl}/get.php?username=${enc(c.username)}&password=${enc(c.password)}&type=m3u_plus&output=ts"
    fun stream(c: XtreamCredentials, id: String, kind: String, extension: String = "ts"): String {
        val folder = when (kind.lowercase()) { "movie" -> "movie"; "series" -> "series"; else -> "live" }
        return "${c.baseUrl}/$folder/${enc(c.username)}/${enc(c.password)}/${enc(id)}.$extension"
    }
}
