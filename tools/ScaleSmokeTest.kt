import com.halit.iptv.data.M3uParser
import kotlin.system.measureTimeMillis

fun main() {
    val n = 200_000
    val text = buildString(n * 90) {
        append("#EXTM3U\n")
        for (i in 1..n) {
            append("#EXTINF:-1 group-title=\"TR\",TR: Kanal ").append(i).append('\n')
            append("http://demo/live/u/p/").append(i).append(".ts\n")
        }
    }
    var count = 0
    val ms = measureTimeMillis { count = M3uParser.parse(text).size }
    check(count == n)
    println("SCALE_SMOKE_TEST_OK items=$count ms=$ms")
}
