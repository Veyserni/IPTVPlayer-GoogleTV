package com.halit.iptv.model

enum class MediaKind { LIVE, MOVIE, SERIES }

enum class SportGroup(val label: String) {
    BEIN_SPORTS("beIN Sports"),
    S_SPORT("S Sport"),
    TIVIBU_SPOR("Tivibu Spor"),
    TABII_SPOR("Tabii Spor"),
    EXXEN_SPOR("Exxen Spor"),
    DSMART_SPOR("D-Smart Spor"),
    TRT_SPOR("TRT Spor"),
    A_SPOR("A Spor"),
    EUROSPORT("Eurosport"),
    OTHER("Diğer Spor")
}

data class IptvItem(
    val name: String,
    val url: String,
    val group: String = "Diğer",
    val logo: String? = null,
    val tvgId: String? = null,
    val kind: MediaKind = MediaKind.LIVE,
    val sportGroup: SportGroup? = null,
)

data class XtreamCredentials(val server: String, val username: String, val password: String) {
    val baseUrl: String get() = server.trim().trimEnd('/')
}
