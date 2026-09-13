import com.halit.iptv.data.M3uParser
import com.halit.iptv.model.SportGroup

fun main() {
    val cases = linkedMapOf(
        "TR • beIN Sports / beIN Sports 1 HD" to SportGroup.BEIN_SPORTS,
        "TR • S Sport / S Sport 2 HD" to SportGroup.S_SPORT,
        "TR • Tivibu Spor / Tivibu Spor 1" to SportGroup.TIVIBU_SPOR,
        "TR • Tabii Spor / Tabii Spor" to SportGroup.TABII_SPOR,
        "TR • Exxen Spor / Exxen Spor 1" to SportGroup.EXXEN_SPOR,
        "TR • Spor / Dsmart / D-Smart Spor" to SportGroup.DSMART_SPOR,
    )
    for ((text, expected) in cases) {
        val actual = M3uParser.inferSportGroup(text.substringBefore(" / "), text.substringAfter(" / "))
        check(actual == expected) { "$text => $actual, expected $expected" }
    }
    println("SPORT_GROUPING_OK ${cases.size}")
}
