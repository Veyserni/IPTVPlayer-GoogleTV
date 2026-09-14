package com.halit.iptv.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.halit.iptv.data.PlaylistRepository
import com.halit.iptv.data.Xtream
import com.halit.iptv.model.IptvItem
import com.halit.iptv.model.SportGroup
import com.halit.iptv.model.XtreamCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class CatalogFilter { ALL, LIVE, SPORT, MOVIE, SERIES }

data class SavedLogin(
    val server: String = "",
    val user: String = "",
    val pass: String = "",
)

sealed interface UiState {
    data object Login : UiState
    data class Loading(val message: String = "Liste yükleniyor…") : UiState
    data class Ready(
        val items: List<IptvItem>,
        val filter: CatalogFilter = CatalogFilter.ALL,
        val sportGroup: SportGroup? = null,
        val isLoading: Boolean = false,
    ) : UiState
    data class Error(val message: String) : UiState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PlaylistRepository()
    private val prefs = application.getSharedPreferences("iptv_login", 0)
    private val _state = MutableStateFlow<UiState>(UiState.Login)
    val state: StateFlow<UiState> = _state

    val savedM3u: String
        get() = prefs.getString("m3u", "").orEmpty()

    val savedAccounts: List<SavedLogin>
        get() {
            val stored = prefs.getString("accounts", null)
            if (!stored.isNullOrBlank()) {
                return runCatching {
                    val array = JSONArray(stored)
                    buildList {
                        for (i in 0 until array.length()) {
                            val obj = array.optJSONObject(i) ?: continue
                            val server = obj.optString("server")
                            val user = obj.optString("user")
                            val pass = obj.optString("pass")
                            if (server.isNotBlank() && user.isNotBlank()) {
                                add(SavedLogin(server, user, pass))
                            }
                        }
                    }
                }.getOrDefault(emptyList())
            }

            // v0.4.0'daki tek hesap kaydını v0.5.0 listesine otomatik taşı.
            val legacy = SavedLogin(
                server = prefs.getString("server", "").orEmpty(),
                user = prefs.getString("user", "").orEmpty(),
                pass = prefs.getString("pass", "").orEmpty(),
            )
            return if (legacy.server.isNotBlank() && legacy.user.isNotBlank()) listOf(legacy) else emptyList()
        }

    fun loadM3u(url: String) {
        if (url.isNotBlank()) prefs.edit().putString("m3u", url.trim()).apply()
        load(url.trim())
    }

    fun loadXtream(server: String, user: String, pass: String) {
        val cleanServer = server.trim()
        val cleanUser = user.trim()
        val account = SavedLogin(cleanServer, cleanUser, pass)
        if (cleanServer.isBlank() || cleanUser.isBlank() || pass.isBlank()) {
            _state.value = UiState.Error("Sunucu, kullanıcı adı ve şifre boş bırakılamaz")
            return
        }
        val credentials = XtreamCredentials(cleanServer, cleanUser, pass)

        // Windows sürümündeki gibi player_api kontrolünü girişin önüne koymuyoruz.
        // Önce gerçek playlist'i akış halinde açıyoruz; sağlayıcının API endpoint'i sorunlu olsa bile
        // canlı liste çalışabiliyorsa kullanıcı bekletilmiyor.
        loadStreaming(Xtream.playlist(credentials), afterFirstBatch = { saveAccount(account) })
    }

    fun loadSavedAccount(account: SavedLogin) {
        loadXtream(account.server, account.user, account.pass)
    }

    fun forgetAccount(account: SavedLogin) {
        val remaining = savedAccounts.filterNot { it.server == account.server && it.user == account.user }
        storeAccounts(remaining)
        clearLegacyIfMatching(account)
    }

    private fun saveAccount(account: SavedLogin) {
        if (account.server.isBlank() || account.user.isBlank()) return
        val updated = buildList {
            add(account)
            addAll(savedAccounts.filterNot { it.server == account.server && it.user == account.user })
        }.take(8)
        storeAccounts(updated)
        // Eski sürümle uyumluluk için son hesabı ayrıca tutuyoruz.
        prefs.edit()
            .putString("server", account.server)
            .putString("user", account.user)
            .putString("pass", account.pass)
            .commit()
    }

    private fun storeAccounts(accounts: List<SavedLogin>) {
        val array = JSONArray()
        accounts.forEach { account ->
            array.put(JSONObject().apply {
                put("server", account.server)
                put("user", account.user)
                put("pass", account.pass)
            })
        }
        // commit() kullanıyoruz: kullanıcı başarılı girişten hemen sonra uygulamayı
        // kapatsa bile kayıt diske yazılmış olsun.
        prefs.edit().putString("accounts", array.toString()).commit()
    }

    private fun clearLegacyIfMatching(account: SavedLogin) {
        val sameLegacy = prefs.getString("server", "").orEmpty() == account.server &&
            prefs.getString("user", "").orEmpty() == account.user
        if (sameLegacy) {
            prefs.edit().remove("server").remove("user").remove("pass").apply()
        }
    }

    private fun load(url: String, afterSuccess: (() -> Unit)? = null) {
        loadStreaming(url, afterFirstBatch = afterSuccess)
    }

    private fun loadStreaming(url: String, afterFirstBatch: (() -> Unit)? = null) {
        if (url.isBlank()) return
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            _state.value = UiState.Error("Adres http:// veya https:// ile başlamalı")
            return
        }
        _state.value = UiState.Loading("Liste bağlanıyor…")
        viewModelScope.launch(Dispatchers.IO) {
            val accumulated = ArrayList<IptvItem>(4096)
            var firstBatch = true
            runCatching {
                repo.loadM3uStreaming(url) { batch ->
                    accumulated.addAll(batch)
                    if (firstBatch) {
                        firstBatch = false
                        afterFirstBatch?.invoke()
                    }
                    // Liste bitmeden katalog görünür olur; yeni batch'ler geldikçe büyür.
                    // Kullanıcı Spor / Canlı / Film gibi bir sekmeye geçtiyse, yeni batch geldiğinde
                    // seçimi ALL'a sıfırlama. v0.6.0'daki sekme zıplama sorununun nedeni buydu.
                    val current = _state.value as? UiState.Ready
                    _state.value = UiState.Ready(
                        items = accumulated.toList(),
                        filter = current?.filter ?: CatalogFilter.ALL,
                        sportGroup = current?.sportGroup,
                        isLoading = true,
                    )
                }
            }.onSuccess { finalItems ->
                if (firstBatch && finalItems.isNotEmpty()) afterFirstBatch?.invoke()
                val current = _state.value as? UiState.Ready
                _state.value = UiState.Ready(
                    items = finalItems,
                    filter = current?.filter ?: CatalogFilter.ALL,
                    sportGroup = current?.sportGroup,
                    isLoading = false,
                )
            }.onFailure { e ->
                if (accumulated.isNotEmpty()) {
                    // Kısmi liste varsa onu kullanılabilir bırak; ağın sonradan kesilmesi tüm kataloğu çöpe atmasın.
                    val current = _state.value as? UiState.Ready
                    _state.value = UiState.Ready(
                        items = accumulated.toList(),
                        filter = current?.filter ?: CatalogFilter.ALL,
                        sportGroup = current?.sportGroup,
                        isLoading = false,
                    )
                } else {
                    _state.value = UiState.Error(e.message ?: "Liste yüklenemedi")
                }
            }
        }
    }

    fun setFilter(filter: CatalogFilter) {
        (_state.value as? UiState.Ready)?.let { _state.value = it.copy(filter = filter, sportGroup = null) }
    }

    fun setSportGroup(group: SportGroup?) {
        (_state.value as? UiState.Ready)?.let { _state.value = it.copy(filter = CatalogFilter.SPORT, sportGroup = group) }
    }

    fun backToLogin() { _state.value = UiState.Login }
}
