import com.halit.iptv.data.M3uParser
import com.halit.iptv.data.Xtream
import com.halit.iptv.model.MediaKind
import com.halit.iptv.model.SportGroup
import com.halit.iptv.model.XtreamCredentials

fun main() {
    val sample = """#EXTM3U
#EXTINF:-1 tvg-id="trt1" tvg-logo="https://logo/trt.png" group-title="TR",TR: TRT 1
http://demo/live/u/p/101.ts
#EXTINF:-1 group-title="Filmler",Örnek Film
http://demo/movie/u/p/202.mkv
#EXTINF:-1 group-title="Diziler",Örnek Dizi S02E03
http://demo/series/u/p/303.mkv
#EXTINF:-1 group-title="TR | SPOR",S SPORT 1 HD
http://demo/live/u/p/401.ts
#EXTINF:-1 group-title="TR | BEIN SPORTS",beIN SPORTS 1 HD
http://demo/live/u/p/402.ts
#EXTINF:-1 group-title="TR | SPOR",TRT SPOR HD
http://demo/live/u/p/403.ts
""".trimIndent()
    val items = M3uParser.parse(sample)
    check(items.size == 6)
    check(items[0].kind == MediaKind.LIVE && items[0].tvgId == "trt1")
    check(items[1].kind == MediaKind.MOVIE)
    check(items[2].kind == MediaKind.SERIES)
    check(items[3].sportGroup == SportGroup.S_SPORT)
    check(items[4].sportGroup == SportGroup.BEIN_SPORTS)
    check(items[5].sportGroup == SportGroup.TRT_SPOR)
    check(M3uParser.inferSportGroup("TR SPORTS", "NBA TV") == SportGroup.OTHER)
    val c = XtreamCredentials("http://server.test/", "a+b", "p@ss")
    check(Xtream.playlist(c).contains("username=a%2Bb"))
    check(Xtream.stream(c, "42", "live") == "http://server.test/live/a%2Bb/p%40ss/42.ts")
    println("CORE_SMOKE_TEST_OK items=${items.size} sports=${items.count { it.sportGroup != null }}")
}
