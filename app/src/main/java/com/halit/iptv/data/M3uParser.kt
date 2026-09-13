package com.halit.iptv.data

import java.io.BufferedReader
import java.io.IOException
import com.halit.iptv.model.IptvItem
import com.halit.iptv.model.MediaKind
import com.halit.iptv.model.SportGroup

object M3uParser {
    private val attr = Regex("([\\w-]+)=\"([^\"]*)\"")

    fun parse(text: String): List<IptvItem> = parse(text.reader().buffered())

    fun parse(reader: BufferedReader, deadlineNanos: Long = Long.MAX_VALUE): List<IptvItem> {
        val out = ArrayList<IptvItem>()
        var info: String? = null
        while (true) {
            if (System.nanoTime() > deadlineNanos) {
                throw IOException("Liste yükleme süresi aşıldı")
            }
            val raw = reader.readLine() ?: break
            val line = raw.trim()
            when {
                line.startsWith("#EXTINF", ignoreCase = true) -> info = line
                line.isNotEmpty() && !line.startsWith("#") && info != null -> {
                    out += fromPair(info, line)
                    info = null
                }
            }
        }
        return out
    }

    private fun fromPair(extinf: String, url: String): IptvItem {
        val attrs = attr.findAll(extinf).associate { it.groupValues[1].lowercase() to it.groupValues[2] }
        val title = extinf.substringAfterLast(',', "Kanal").trim().ifBlank { "Kanal" }
        val group = attrs["group-title"].orEmpty().ifBlank { inferGroup(title) }
        val kind = inferKind(url, group, title)
        val sportGroup = if (kind == MediaKind.LIVE) inferSportGroup(group, title) else null
        return IptvItem(title, url, group, attrs["tvg-logo"], attrs["tvg-id"], kind, sportGroup)
    }

    fun inferKind(url: String, group: String = "", title: String = ""): MediaKind {
        val path = url.lowercase()
        if ("/series/" in path || "dizi" in group.lowercase() || Regex("\\b\\d{1,2}x\\d{1,3}\\b|\\bS\\d{1,2}E\\d{1,3}\\b", RegexOption.IGNORE_CASE).containsMatchIn(title)) return MediaKind.SERIES
        if ("/movie/" in path || path.endsWith(".mkv") || path.endsWith(".mp4") || "film" in group.lowercase()) return MediaKind.MOVIE
        return MediaKind.LIVE
    }

    fun inferSportGroup(group: String, title: String): SportGroup? {
        val text = "$group $title"
            .lowercase()
            .replace('ı', 'i')
            .replace('ş', 's')
            .replace('ğ', 'g')
            .replace('ü', 'u')
            .replace('ö', 'o')
            .replace('ç', 'c')
            .replace(Regex("[^a-z0-9+]+"), " ")
            .trim()

        return when {
            Regex("\\b(bein sport|bein sports|beinsport|bein max)\\b").containsMatchIn(text) -> SportGroup.BEIN_SPORTS
            Regex("\\b(s sport|ssport|s sport plus)\\b").containsMatchIn(text) -> SportGroup.S_SPORT
            "tivibu spor" in text || "tivibu sport" in text -> SportGroup.TIVIBU_SPOR
            "tabii spor" in text || "tabii sport" in text || "tabi spor" in text -> SportGroup.TABII_SPOR
            "exxen spor" in text || "exxen sport" in text -> SportGroup.EXXEN_SPOR
            "dsmart spor" in text || "d smart spor" in text || "dsmart sport" in text || "d smart sport" in text -> SportGroup.DSMART_SPOR
            "trt spor" in text || "trt sport" in text -> SportGroup.TRT_SPOR
            Regex("\\ba spor\\b").containsMatchIn(text) -> SportGroup.A_SPOR
            "eurosport" in text -> SportGroup.EUROSPORT
            isSportsText(text) -> SportGroup.OTHER
            else -> null
        }
    }

    private fun isSportsText(text: String): Boolean {
        val keywords = listOf(
            "spor", "sport", "sports", "futbol", "football", "basketbol", "basketball",
            "nba", "nfl", "formula 1", "formula1", "motogp", "tenis", "tennis", "voleybol", "volleyball"
        )
        return keywords.any { keyword -> Regex("(^| )${Regex.escape(keyword)}( |$)").containsMatchIn(text) }
    }

    private fun inferGroup(title: String): String {
        val prefix = title.substringBefore(':', "").trim()
        return if (prefix.length in 2..16) prefix else "Diğer"
    }
}
