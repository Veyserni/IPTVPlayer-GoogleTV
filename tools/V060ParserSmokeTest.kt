import com.halit.iptv.data.M3uParser
import com.halit.iptv.model.SportGroup

fun main() {
    val text = """#EXTM3U
#EXTINF:-1 group-title=\"TR | SPOR\",beIN SPORTS 1 HD
http://example/live/1.ts
#EXTINF:-1 group-title=\"TR | SPOR\",S SPORT 2
http://example/live/2.ts
#EXTINF:-1 group-title=\"Filmler\",Film A
http://example/movie/u/p/3.mkv
"""
    val items = M3uParser.parse(text)
    check(items.size == 3)
    check(items[0].sportGroup == SportGroup.BEIN_SPORTS)
    check(items[1].sportGroup == SportGroup.S_SPORT)
    check(items[2].kind.name == "MOVIE")
    println("V060_PARSER_SMOKE_OK items=${items.size}")
}
