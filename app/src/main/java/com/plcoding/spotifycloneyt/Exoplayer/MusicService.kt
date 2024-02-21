package com.plcoding.spotifycloneyt.Exoplayer

import android.media.browse.MediaBrowser
import android.os.Bundle
import android.service.media.MediaBrowserService
import com.google.android.exoplayer2.SimpleExoPlayer
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject

@ServiceScoped
class MusicService : MediaBrowserService() {
    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    override fun onCreate() {
        super.onCreate()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowser.MediaItem>>
    ) {
        TODO("Not yet implemented")
    }
}