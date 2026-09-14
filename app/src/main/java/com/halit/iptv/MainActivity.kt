package com.halit.iptv

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.halit.iptv.model.IptvItem
import com.halit.iptv.model.MediaKind
import com.halit.iptv.model.SportGroup
import com.halit.iptv.player.PlayerController
import com.halit.iptv.ui.CatalogFilter
import com.halit.iptv.ui.MainViewModel
import com.halit.iptv.ui.SavedLogin
import com.halit.iptv.ui.UiState
import kotlinx.coroutines.delay
import kotlin.math.max

private val Purple = Color(0xFF7C3CFF)
private val PurpleLight = Color(0xFFA56BFF)
private val Blue = Color(0xFF5267FF)
private val Panel = Color(0xD9141C3A)
private val PanelSoft = Color(0xB30F1730)
private val TextSoft = Color(0xFFB8C2E3)
private val Line = Color(0xFF33447A)
private val BgTop = Color(0xFF070B1D)
private val BgBottom = Color(0xFF11103A)

class MainActivity : ComponentActivity() {
    private val vm by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { App(vm) } }
    }
}

@Composable
private fun App(vm: MainViewModel) {
    when (val state = vm.state.collectAsStateWithLifecycle().value) {
        UiState.Login -> LoginScreen(vm)
        is UiState.Loading -> NeonBackground {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                NeonPanel(Modifier.width(560.dp)) {
                    Column(
                        Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        BrandTitle()
                        Text(state.message, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = PurpleLight,
                            trackColor = Color(0xFF202A53)
                        )
                    }
                }
            }
        }
        is UiState.Error -> NeonBackground {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                NeonPanel(Modifier.width(680.dp)) {
                    Column(Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Text("Liste açılamadı", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text(state.message, color = TextSoft, fontSize = 18.sp)
                        NeonButton("Geri", selected = true, onClick = vm::backToLogin)
                    }
                }
            }
        }
        is UiState.Ready -> CatalogScreen(state, vm)
    }
}

@Composable
private fun NeonBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BgTop, Color(0xFF0B1230), BgBottom)
                )
            ),
        content = content
    )
}

@Composable
private fun BrandTitle() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(listOf(Blue, PurpleLight))),
            contentAlignment = Alignment.Center
        ) {
            Text("▶", color = Color.White, fontSize = 18.sp)
        }
        Row {
            Text("IPTV ", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Player", color = PurpleLight, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NeonPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Panel)
            .border(1.dp, Line, RoundedCornerShape(22.dp))
    ) { content() }
}

@Composable
private fun LoginScreen(vm: MainViewModel) {
    var m3u by remember { mutableStateOf(vm.savedM3u) }
    var server by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var savedAccounts by remember { mutableStateOf(vm.savedAccounts) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Color(0xFF121B3D),
        unfocusedContainerColor = Color(0xFF101731),
        focusedBorderColor = PurpleLight,
        unfocusedBorderColor = Line,
        focusedLabelColor = PurpleLight,
        unfocusedLabelColor = TextSoft,
        cursorColor = PurpleLight,
    )

    NeonBackground {
        Column(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 30.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BrandTitle()
                    Text("Hoş geldin", color = TextSoft, fontSize = 19.sp)
                }
                Text("DAHA FAZLA EĞLENCE  •  HER ZAMAN SENİNLE", color = Color(0xFF8390D7), fontSize = 14.sp)
            }

            Spacer(Modifier.height(24.dp))

            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                NeonPanel(Modifier.weight(0.9f).fillMaxHeight()) {
                    Column(
                        Modifier.fillMaxSize().padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Text("M3U", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text("M3U bağlantı linki ile kanal listenizi yükleyin.", color = TextSoft, fontSize = 16.sp)
                        OutlinedTextField(
                            value = m3u,
                            onValueChange = { m3u = it },
                            label = { androidx.compose.material3.Text("M3U URL") },
                            placeholder = { androidx.compose.material3.Text("http://…") },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                        NeonButton("Listeyi Aç", selected = false, onClick = { vm.loadM3u(m3u) })
                        Spacer(Modifier.weight(1f))
                        Box(
                            Modifier.fillMaxWidth().height(104.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0x332C1BFF), Color(0x553D26CC), Color.Transparent)
                                    )
                                )
                        ) {
                            Column(
                                Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "TV için optimize edildi",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    "Kumanda ile kolay gezinme • okunaklı arayüz",
                                    color = TextSoft,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                NeonPanel(Modifier.weight(1.25f).fillMaxHeight()) {
                    Column(
                        Modifier.fillMaxSize().padding(28.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Xtream Codes", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text("Sunucu bilgileri ile giriş yapın.", color = TextSoft, fontSize = 16.sp)

                        OutlinedTextField(
                            value = server,
                            onValueChange = { server = it },
                            label = { androidx.compose.material3.Text("Sunucu") },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = user,
                            onValueChange = { user = it },
                            label = { androidx.compose.material3.Text("Kullanıcı adı") },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = pass,
                            onValueChange = { pass = it },
                            label = { androidx.compose.material3.Text("Şifre") },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.weight(1f)) {
                                NeonButton("Giriş Yap", selected = false, onClick = { vm.loadXtream(server, user, pass) })
                            }
                            savedAccounts.firstOrNull()?.let { account ->
                                Box(Modifier.weight(0.9f)) {
                                    SavedAccountQuickButton(
                                        account = account,
                                        onLogin = { vm.loadSavedAccount(account) }
                                    )
                                }
                            }
                        }

                        if (savedAccounts.size > 1) {
                            Text("Diğer kayıtlı hesaplar", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                            savedAccounts.drop(1).take(2).forEach { account ->
                                SavedAccountQuickButton(
                                    account = account,
                                    onLogin = { vm.loadSavedAccount(account) }
                                )
                            }
                        } else if (savedAccounts.isEmpty()) {
                            Text("Başarılı giriş yaptığında hesap otomatik kaydedilir.", color = TextSoft, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.tvAction(onClick: () -> Unit): Modifier =
    this
        .onPreviewKeyEvent { event ->
            if (event.type == KeyEventType.KeyUp &&
                (event.key == Key.Enter || event.key == Key.NumPadEnter || event.key == Key.DirectionCenter)
            ) {
                onClick()
                true
            } else false
        }
        .focusable()
        .clickable(onClick = onClick)

@Composable
private fun NeonButton(label: String, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    val borderColor = if (focused || selected) PurpleLight else Line
    val background = if (selected || focused) {
        Brush.linearGradient(listOf(Color(0xFF4A2DFF), Purple))
    } else {
        Brush.linearGradient(listOf(Color(0xFF141D3C), Color(0xFF101731)))
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .onFocusChanged { focused = it.isFocused }
            .tvAction(onClick)
            .clip(shape)
            .background(background)
            .border(if (focused) 2.dp else 1.dp, borderColor, shape)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("›", color = Color.White, fontSize = 30.sp)
        }
    }
}

@Composable
private fun SavedAccountQuickButton(account: SavedLogin, onLogin: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .onFocusChanged { focused = it.isFocused }
            .tvAction(onLogin)
            .clip(shape)
            .background(
                if (focused) Brush.linearGradient(listOf(Color(0xFF4A2DFF), Purple))
                else Brush.linearGradient(listOf(Color(0xFF141D3C), Color(0xFF101731)))
            )
            .border(if (focused) 2.dp else 1.dp, if (focused) PurpleLight else Line, shape)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(account.user, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("Kayıtlı hesap", color = TextSoft, fontSize = 12.sp, maxLines = 1)
            }
            Text("›", color = Color.White, fontSize = 30.sp)
        }
    }
}

@Composable
private fun SmallAction(label: String, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Box(
        Modifier
            .onFocusChanged { focused = it.isFocused }
            .tvAction(onClick)
            .clip(RoundedCornerShape(12.dp))
            .background(if (focused) Purple else Color(0xFF202A55))
            .border(1.dp, if (focused) PurpleLight else Line, RoundedCornerShape(12.dp))
            .padding(horizontal = 13.dp, vertical = 9.dp)
    ) {
        Text(label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun CatalogScreen(state: UiState.Ready, vm: MainViewModel) {
    var playing by remember { mutableStateOf<IptvItem?>(null) }
    var search by remember { mutableStateOf("") }
    playing?.let { PlayerScreen(it) { playing = null }; return }

    val availableSportGroups = remember(state.items) {
        SportGroup.entries.filter { group -> state.items.any { it.sportGroup == group } }
    }
    val sportCounts = remember(state.items) {
        state.items.filter { it.sportGroup != null }.groupingBy { it.sportGroup!! }.eachCount()
    }

    val shown = remember(state.items, state.filter, state.sportGroup, search) {
        val q = search.trim()
        state.items.asSequence().filter { item ->
            val categoryMatch = when (state.filter) {
                CatalogFilter.ALL -> true
                CatalogFilter.LIVE -> item.kind == MediaKind.LIVE
                CatalogFilter.SPORT -> item.sportGroup != null && (state.sportGroup == null || item.sportGroup == state.sportGroup)
                CatalogFilter.MOVIE -> item.kind == MediaKind.MOVIE
                CatalogFilter.SERIES -> item.kind == MediaKind.SERIES
            }
            categoryMatch && (q.isBlank() || item.name.contains(q, ignoreCase = true) || item.group.contains(q, ignoreCase = true))
        }.take(5000).toList()
    }

    NeonBackground {
        Row(Modifier.fillMaxSize()) {
            Sidebar(state.filter, vm)

            Column(Modifier.weight(1f).fillMaxHeight().padding(start = 20.dp, top = 20.dp, end = 24.dp, bottom = 20.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("⌂", color = TextSoft, fontSize = 24.sp)
                        Text("›", color = TextSoft, fontSize = 26.sp)
                        Text(
                            when (state.filter) {
                                CatalogFilter.SPORT -> "Spor"
                                CatalogFilter.LIVE -> "Canlı Yayın"
                                CatalogFilter.MOVIE -> "Filmler"
                                CatalogFilter.SERIES -> "Diziler"
                                CatalogFilter.ALL -> "Tümü"
                            },
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = search,
                            onValueChange = { search = it },
                            placeholder = { androidx.compose.material3.Text("Kanal ara…") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF111A38),
                                unfocusedContainerColor = Color(0xFF101731),
                                focusedBorderColor = PurpleLight,
                                unfocusedBorderColor = Line,
                                cursorColor = PurpleLight,
                            ),
                            modifier = Modifier.width(300.dp)
                        )
                        Text(
                            if (state.isLoading) "${shown.size} kanal • yükleniyor…" else "${shown.size} kanal",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                if (state.filter == CatalogFilter.SPORT) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            SportChip("Tümü", state.sportGroup == null) { vm.setSportGroup(null) }
                        }
                        items(availableSportGroups) { group ->
                            SportChip(group.label, state.sportGroup == group) { vm.setSportGroup(group) }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                if (shown.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Bu bölümde kanal bulunamadı", color = TextSoft, fontSize = 20.sp)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(shown, key = { index, item -> "${item.url}#$index" }) { index, item ->
                            ChannelCard(index + 1, item) { playing = item }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Sidebar(filter: CatalogFilter, vm: MainViewModel) {
    Column(
        Modifier.width(250.dp).fillMaxHeight().background(Color(0xAA080E22)).padding(horizontal = 18.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BrandTitle()
        Spacer(Modifier.height(8.dp))
        NavTile("Spor", filter == CatalogFilter.SPORT) { vm.setFilter(CatalogFilter.SPORT) }
        NavTile("Canlı Yayın", filter == CatalogFilter.LIVE) { vm.setFilter(CatalogFilter.LIVE) }
        NavTile("Filmler", filter == CatalogFilter.MOVIE) { vm.setFilter(CatalogFilter.MOVIE) }
        NavTile("Diziler", filter == CatalogFilter.SERIES) { vm.setFilter(CatalogFilter.SERIES) }
        NavTile("Tümü", filter == CatalogFilter.ALL) { vm.setFilter(CatalogFilter.ALL) }
        Spacer(Modifier.weight(1f))
        Text("DAHA FAZLA EĞLENCE", color = Color(0xFF7D8BD3), fontSize = 12.sp)
        Text("HER ZAMAN SENİNLE", color = Color(0xFF7D8BD3), fontSize = 12.sp)
    }
}

@Composable
private fun NavTile(label: String, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    val bg = if (selected || focused) Brush.linearGradient(listOf(Color(0xFF552DFF), Purple)) else Brush.linearGradient(listOf(Color(0xFF111A36), Color(0xFF0D142B)))
    Box(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .onFocusChanged { focused = it.isFocused }
            .tvAction(onClick)
            .clip(shape)
            .background(bg)
            .border(if (focused) 2.dp else 1.dp, if (selected || focused) PurpleLight else Color(0xFF26345F), shape)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color.White, fontSize = 18.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
            Text("›", color = Color.White, fontSize = 28.sp)
        }
    }
}

@Composable
private fun SportChip(label: String, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier
            .onFocusChanged { focused = it.isFocused }
            .tvAction(onClick)
            .clip(shape)
            .background(if (selected || focused) Brush.linearGradient(listOf(Color(0xFF5B32FF), PurpleLight)) else Brush.linearGradient(listOf(Color(0xFF111A38), Color(0xFF101731))))
            .border(if (focused) 2.dp else 1.dp, if (selected || focused) PurpleLight else Line, shape)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(label, color = Color.White, fontSize = 15.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun ChannelCard(number: Int, item: IptvItem, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(104.dp)
            .onFocusChanged { focused = it.isFocused }
            .tvAction(onClick)
            .clip(shape)
            .background(if (focused) Color(0xFF23205A) else PanelSoft)
            .border(if (focused) 2.dp else 1.dp, if (focused) PurpleLight else Color(0xFF26345F), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(30.dp), contentAlignment = Alignment.CenterStart) {
                Text(number.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF151B35)),
                contentAlignment = Alignment.Center
            ) {
                Text(logoLabel(item), color = PurpleLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                val nameModifier = if (focused) {
                    Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE)
                } else {
                    Modifier.fillMaxWidth()
                }
                Text(
                    item.name,
                    modifier = nameModifier,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = if (focused) 1 else 2,
                    overflow = if (focused) TextOverflow.Clip else TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    item.group.ifBlank { item.sportGroup?.label ?: "Kanal" },
                    color = TextSoft,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF25205A))
                    .border(1.dp, PurpleLight, RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(qualityLabel(item.name), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(6.dp))
            Text("›", color = Color.White, fontSize = 22.sp)
        }
    }
}

private fun logoLabel(item: IptvItem): String {
    val n = item.name.lowercase()
    return when {
        "bein" in n -> "beIN"
        "s sport" in n -> "S"
        "tivibu" in n -> "tivibu"
        "tabii" in n -> "tabii"
        "trt" in n -> "TRT"
        "exxen" in n -> "EXXEN"
        else -> item.name.take(4).uppercase()
    }
}

private fun qualityLabel(name: String): String {
    val n = name.lowercase()
    return when {
        "4k" in n -> "4K"
        "1080" in n || "hd" in n -> "HD"
        "720" in n -> "720p"
        "576" in n -> "576p"
        "360" in n -> "360p"
        else -> "CANLI"
    }
}

@Composable
private fun PlayerScreen(item: IptvItem, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val controller = remember { PlayerController(context) }
    val focusRequester = remember { FocusRequester() }
    var overlayVisible by remember(item.url) { mutableStateOf(true) }
    var overlayToken by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var muted by remember { mutableStateOf(false) }
    var settingsVisible by remember { mutableStateOf(false) }

    DisposableEffect(item.url) {
        controller.play(item.url)
        onDispose { controller.release() }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    LaunchedEffect(item.url, overlayToken) {
        overlayVisible = true
        delay(2000)
        if (!settingsVisible) overlayVisible = false
    }

    LaunchedEffect(item.url) {
        while (true) {
            val p = controller.player
            isPlaying = p.isPlaying
            position = max(0L, p.currentPosition)
            duration = if (p.duration == C.TIME_UNSET || p.duration < 0) 0L else p.duration
            delay(500)
        }
    }

    fun showOverlay() {
        overlayVisible = true
        overlayToken++
    }

    Box(
        Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                showOverlay()
                when (event.key) {
                    Key.DirectionLeft -> {
                        if (overlayVisible) false
                        else {
                            controller.player.seekTo(max(0L, controller.player.currentPosition - 10_000L)); true
                        }
                    }
                    Key.DirectionRight -> {
                        if (overlayVisible) false
                        else {
                            controller.player.seekTo(controller.player.currentPosition + 10_000L); true
                        }
                    }
                    Key.DirectionCenter, Key.Enter -> {
                        if (controller.player.isPlaying) controller.player.pause() else controller.player.play(); true
                    }
                    else -> false
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = controller.player
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (overlayVisible) {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color(0x88040A20), Color.Transparent, Color.Transparent, Color(0xDD06091A))
                    )
                )
            )

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 26.dp, vertical = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallPlayerButton("← Liste") { onBack() }
                Text(item.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (item.kind == MediaKind.LIVE) "CANLI" else qualityLabel(item.name), color = PurpleLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 30.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(formatTime(position), color = Color.White, fontSize = 14.sp)
                    LinearProgressIndicator(
                        progress = { if (duration > 0) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f },
                        modifier = Modifier.weight(1f).height(5.dp),
                        color = PurpleLight,
                        trackColor = Color(0xFF53608D)
                    )
                    Text(if (duration > 0) formatTime(duration) else "CANLI", color = Color.White, fontSize = 14.sp)
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayerControlButton("↶ 10", compact = true) {
                        controller.player.seekTo(max(0L, controller.player.currentPosition - 10_000L)); showOverlay()
                    }
                    Spacer(Modifier.width(10.dp))
                    PlayerControlButton(if (isPlaying) "Ⅱ" else "▶", compact = false) {
                        if (controller.player.isPlaying) controller.player.pause() else controller.player.play(); showOverlay()
                    }
                    Spacer(Modifier.width(10.dp))
                    PlayerControlButton("10 ↷", compact = true) {
                        controller.player.seekTo(controller.player.currentPosition + 10_000L); showOverlay()
                    }
                    Spacer(Modifier.width(18.dp))
                    PlayerControlButton(if (muted) "Ses Aç" else "Sessiz", compact = true) {
                        muted = !muted
                        controller.player.volume = if (muted) 0f else 1f
                        showOverlay()
                    }
                    Spacer(Modifier.width(10.dp))
                    PlayerControlButton("Ayarlar", compact = true) {
                        settingsVisible = !settingsVisible
                        overlayVisible = true
                    }
                }
            }

            if (settingsVisible) {
                NeonPanel(Modifier.align(Alignment.CenterEnd).padding(end = 30.dp).width(300.dp)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Oynatıcı Ayarları", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("• Sol / sağ: 10 saniye geri / ileri", color = TextSoft, fontSize = 14.sp)
                        Text("• OK: oynat / duraklat", color = TextSoft, fontSize = 14.sp)
                        Text("• Kontroller 2 saniye sonra gizlenir", color = TextSoft, fontSize = 14.sp)
                        SmallAction("Kapat") {
                            settingsVisible = false
                            showOverlay()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallPlayerButton(label: String, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Box(
        Modifier
            .onFocusChanged { focused = it.isFocused }
            .tvAction(onClick)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xAA131C3C))
            .border(if (focused) 2.dp else 1.dp, if (focused) PurpleLight else Line, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(label, color = Color.White, fontSize = 15.sp)
    }
}

@Composable
private fun PlayerControlButton(label: String, compact: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val size = if (compact) 52.dp else 62.dp
    Box(
        Modifier
            .size(size)
            .onFocusChanged { focused = it.isFocused }
            .tvAction(onClick)
            .clip(RoundedCornerShape(50))
            .background(if (focused) Brush.radialGradient(listOf(PurpleLight, Purple)) else Brush.radialGradient(listOf(Color(0xFF1A2550), Color(0xFF11182E))))
            .border(if (focused) 3.dp else 2.dp, if (focused) Color.White else PurpleLight, RoundedCornerShape(50)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = if (compact) 11.sp else 24.sp, fontWeight = FontWeight.Bold)
    }
}

private fun formatTime(ms: Long): String {
    val total = ms / 1000L
    val h = total / 3600L
    val m = (total % 3600L) / 60L
    val s = total % 60L
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
