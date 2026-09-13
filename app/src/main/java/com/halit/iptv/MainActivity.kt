package com.halit.iptv

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import androidx.tv.material3.*
import com.halit.iptv.model.IptvItem
import com.halit.iptv.model.MediaKind
import com.halit.iptv.model.SportGroup
import com.halit.iptv.player.PlayerController
import com.halit.iptv.ui.MainViewModel
import com.halit.iptv.ui.UiState
import com.halit.iptv.ui.CatalogFilter
import com.halit.iptv.ui.SavedLogin

class MainActivity : ComponentActivity() {
    private val vm by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { App(vm) } }
    }
}

@Composable private fun App(vm: MainViewModel) {
    when (val state = vm.state.collectAsStateWithLifecycle().value) {
        UiState.Login -> LoginScreen(vm)
        UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Liste yükleniyor…") }
        is UiState.Error -> Column(Modifier.padding(48.dp)) { Text(state.message); Button(onClick = vm::backToLogin) { Text("Geri") } }
        is UiState.Ready -> CatalogScreen(state, vm)
    }
}

@Composable private fun LoginScreen(vm: MainViewModel) {
    var m3u by remember { mutableStateOf(vm.savedM3u) }
    var server by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var savedAccounts by remember { mutableStateOf(vm.savedAccounts) }

    val fieldColors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Color(0xFF1C222B),
        unfocusedContainerColor = Color(0xFF171C23),
        focusedBorderColor = Color(0xFF9E8CFF),
        unfocusedBorderColor = Color(0xFF7A8594),
        focusedLabelColor = Color(0xFFC8BEFF),
        unfocusedLabelColor = Color(0xFFD7DCE3),
        cursorColor = Color.White,
    )

    Row(
        Modifier.fillMaxSize().background(Color(0xFF0E1116)).padding(48.dp),
        horizontalArrangement = Arrangement.spacedBy(42.dp)
    ) {
        Column(Modifier.weight(0.9f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("M3U", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            androidx.compose.material3.OutlinedTextField(
                value = m3u,
                onValueChange = { m3u = it },
                label = { androidx.compose.material3.Text("M3U URL") },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { vm.loadM3u(m3u) }) { Text("Listeyi Aç") }
        }

        Column(Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Xtream Codes", style = MaterialTheme.typography.headlineMedium, color = Color.White)

            if (savedAccounts.isNotEmpty()) {
                Text("Kayıtlı Hesaplar", style = MaterialTheme.typography.titleMedium, color = Color.White)
                savedAccounts.forEach { account ->
                    SavedAccountRow(
                        account = account,
                        onLogin = { vm.loadSavedAccount(account) },
                        onForget = {
                            vm.forgetAccount(account)
                            savedAccounts = vm.savedAccounts
                        }
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text("Yeni hesap ekle", style = MaterialTheme.typography.titleMedium, color = Color(0xFFD7DCE3))
            }

            androidx.compose.material3.OutlinedTextField(
                value = server,
                onValueChange = { server = it },
                label = { androidx.compose.material3.Text("Sunucu") },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            androidx.compose.material3.OutlinedTextField(
                value = user,
                onValueChange = { user = it },
                label = { androidx.compose.material3.Text("Kullanıcı") },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            androidx.compose.material3.OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { androidx.compose.material3.Text("Şifre") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { vm.loadXtream(server, user, pass) }) { Text("Giriş") }
        }
    }
}

@Composable private fun SavedAccountRow(
    account: SavedLogin,
    onLogin: () -> Unit,
    onForget: () -> Unit,
) {
    Card(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(account.user, style = MaterialTheme.typography.titleMedium)
                Text(account.server, style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onLogin) { Text("Giriş Yap") }
                Button(onClick = onForget) { Text("Unut") }
            }
        }
    }
}

@Composable private fun CatalogScreen(state: UiState.Ready, vm: MainViewModel) {
    var playing by remember { mutableStateOf<IptvItem?>(null) }
    playing?.let { PlayerScreen(it) { playing = null }; return }

    val availableSportGroups = remember(state.items) {
        SportGroup.entries.filter { group -> state.items.any { it.sportGroup == group } }
    }

    val sportCounts = remember(state.items) {
        state.items.filter { it.sportGroup != null }
            .groupingBy { it.sportGroup!! }
            .eachCount()
    }

    val shown = remember(state.items, state.filter, state.sportGroup) {
        state.items.asSequence().filter { item ->
            when (state.filter) {
                CatalogFilter.ALL -> true
                CatalogFilter.LIVE -> item.kind == MediaKind.LIVE
                CatalogFilter.SPORT -> item.sportGroup != null && (state.sportGroup == null || item.sportGroup == state.sportGroup)
                CatalogFilter.MOVIE -> item.kind == MediaKind.MOVIE
                CatalogFilter.SERIES -> item.kind == MediaKind.SERIES
            }
        }.take(5000).toList()
    }

    Row(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1) Ana menü: kullanıcının istediği gibi Spor, Canlı Yayın'ın üstünde.
        Column(
            Modifier.width(220.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("IPTV Player", style = MaterialTheme.typography.headlineSmall)
            MainMenuButton("Spor", state.filter == CatalogFilter.SPORT) { vm.setFilter(CatalogFilter.SPORT) }
            MainMenuButton("Canlı Yayın", state.filter == CatalogFilter.LIVE) { vm.setFilter(CatalogFilter.LIVE) }
            MainMenuButton("Filmler", state.filter == CatalogFilter.MOVIE) { vm.setFilter(CatalogFilter.MOVIE) }
            MainMenuButton("Diziler", state.filter == CatalogFilter.SERIES) { vm.setFilter(CatalogFilter.SERIES) }
            MainMenuButton("Tümü", state.filter == CatalogFilter.ALL) { vm.setFilter(CatalogFilter.ALL) }
        }

        // 2) Spor seçiliyken ekran görüntüsündeki gibi ayrı bir yan kategori sütunu.
        if (state.filter == CatalogFilter.SPORT) {
            Column(
                Modifier.width(250.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Spor", style = MaterialTheme.typography.titleLarge)
                SportCategoryButton(
                    label = "Tümü",
                    count = state.items.count { it.sportGroup != null },
                    selected = state.sportGroup == null
                ) { vm.setSportGroup(null) }

                availableSportGroups.forEach { group ->
                    SportCategoryButton(
                        label = group.label,
                        count = sportCounts[group] ?: 0,
                        selected = state.sportGroup == group
                    ) { vm.setSportGroup(group) }
                }
            }
        }

        // 3) Sağ panel: seçilen ana/alt sekmenin kanalları.
        Column(Modifier.weight(1f)) {
            val title = when {
                state.filter == CatalogFilter.SPORT && state.sportGroup != null -> state.sportGroup.label
                state.filter == CatalogFilter.SPORT -> "Spor"
                state.filter == CatalogFilter.LIVE -> "Canlı Yayın"
                state.filter == CatalogFilter.MOVIE -> "Filmler"
                state.filter == CatalogFilter.SERIES -> "Diziler"
                else -> "Tümü"
            }
            Row(
                Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.headlineSmall)
                Text("${shown.size} öğe")
            }

            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(shown, key = { it.url }) { item ->
                    Card(onClick = { playing = item }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium)
                            Text(item.group, color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun MainMenuButton(label: String, selected: Boolean, click: () -> Unit) {
    Button(onClick = click, modifier = Modifier.fillMaxWidth()) {
        Text(if (selected) "● $label" else label)
    }
}

@Composable private fun SportCategoryButton(label: String, count: Int, selected: Boolean, click: () -> Unit) {
    Button(onClick = click, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (selected) "● $label" else label)
            Text(count.toString())
        }
    }
}

@Composable private fun PlayerScreen(item: IptvItem, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val controller = remember { PlayerController(context) }
    var overlayVisible by remember(item.url) { mutableStateOf(true) }

    DisposableEffect(item.url) {
        controller.play(item.url)
        onDispose { controller.release() }
    }

    LaunchedEffect(item.url, overlayVisible) {
        if (overlayVisible) {
            delay(3000)
            overlayVisible = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = controller.player
                    useController = true
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (overlayVisible) {
            Button(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart).padding(24.dp)
            ) { Text("← Liste") }
            Text(
                item.name,
                color = Color.White,
                modifier = Modifier.align(Alignment.TopCenter).padding(28.dp)
            )
        }
    }
}

