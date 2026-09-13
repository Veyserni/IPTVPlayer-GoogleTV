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

enum class CatalogFilter { ALL, LIVE, SPORT, MOVIE, SERIES }

data class SavedLogin(
    val m3u: String = "",
    val server: String = "",
    val user: String = "",
    val pass: String = "",
)

sealed interface UiState {
    data object Login : UiState
    data object Loading : UiState
    data class Ready(
        val items: List<IptvItem>,
        val filter: CatalogFilter = CatalogFilter.ALL,
        val sportGroup: SportGroup? = null,
    ) : UiState
    data class Error(val message: String) : UiState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PlaylistRepository()
    private val prefs = application.getSharedPreferences("iptv_login", 0)
    private val _state = MutableStateFlow<UiState>(UiState.Login)
    val state: StateFlow<UiState> = _state

    val savedLogin: SavedLogin
        get() = SavedLogin(
            m3u = prefs.getString("m3u", "").orEmpty(),
            server = prefs.getString("server", "").orEmpty(),
            user = prefs.getString("user", "").orEmpty(),
            pass = prefs.getString("pass", "").orEmpty(),
        )

    fun loadM3u(url: String) {
        if (url.isNotBlank()) prefs.edit().putString("m3u", url.trim()).apply()
        load(url.trim())
    }

    fun loadXtream(server: String, user: String, pass: String) {
        val cleanServer = server.trim()
        val cleanUser = user.trim()
        prefs.edit()
            .putString("server", cleanServer)
            .putString("user", cleanUser)
            .putString("pass", pass)
            .apply()
        load(Xtream.playlist(XtreamCredentials(cleanServer, cleanUser, pass)))
    }

    fun forgetLogin() {
        prefs.edit().clear().apply()
        _state.value = UiState.Login
    }

    private fun load(url: String) {
        if (url.isBlank()) return
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            _state.value = UiState.Error("Adres http:// veya https:// ile başlamalı")
            return
        }
        _state.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repo.loadM3u(url) }
                .onSuccess { _state.value = UiState.Ready(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "Liste yüklenemedi") }
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
