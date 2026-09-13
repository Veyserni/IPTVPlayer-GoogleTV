package com.halit.iptv.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

class PlayerController(context: Context) {
    private val http = DefaultHttpDataSource.Factory()
        .setUserAgent("VLC/3.0.21 LibVLC/3.0.21")
        .setAllowCrossProtocolRedirects(true)
        .setDefaultRequestProperties(mapOf("Accept" to "*/*"))
    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(http))
        .build().apply { repeatMode = Player.REPEAT_MODE_OFF }

    fun play(url: String) {
        player.stop()
        player.clearMediaItems()
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.playWhenReady = true
    }
    fun release() = player.release()
}
