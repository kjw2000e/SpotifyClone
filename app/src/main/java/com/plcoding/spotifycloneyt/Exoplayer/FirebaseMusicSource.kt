package com.plcoding.spotifycloneyt.Exoplayer

import android.media.MediaMetadata.METADATA_KEY_ALBUM_ART_URI
import android.media.MediaMetadata.METADATA_KEY_ARTIST
import android.media.MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION
import android.media.MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI
import android.media.MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE
import android.media.MediaMetadata.METADATA_KEY_DISPLAY_TITLE
import android.media.MediaMetadata.METADATA_KEY_MEDIA_ID
import android.media.MediaMetadata.METADATA_KEY_MEDIA_URI
import android.media.MediaMetadata.METADATA_KEY_TITLE
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.net.toUri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.plcoding.spotifycloneyt.Exoplayer.State.STATE_CREATED
import com.plcoding.spotifycloneyt.Exoplayer.State.STATE_ERROR
import com.plcoding.spotifycloneyt.Exoplayer.State.STATE_INITIALIZED
import com.plcoding.spotifycloneyt.Exoplayer.State.STATE_INITIALIZING
import com.plcoding.spotifycloneyt.data.remote.MusicDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicSource @Inject constructor(
    private val musicDatabase: MusicDatabase
){
    var songs = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        state = STATE_INITIALIZING
        val allSongs = musicDatabase.getAllSongs()
        // List<song> -> List<MediaMetadataCompat>
        songs = allSongs.map { song ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST, song.subTitle)
                .putString(METADATA_KEY_MEDIA_ID, song.mediaId)
                .putString(METADATA_KEY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.imgUrl)
                .putString(METADATA_KEY_MEDIA_URI, song.songUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.songUrl)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.subTitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.subTitle)
                .build()
        }
        state = STATE_INITIALIZED
    }

    //song list를 mediaSource로 변경한다.
    // ConcatenatingMediaSource : multiple mediasource 만들때 사용.
    // 플레이리스트 처럼 동작하도록 하기 위해 사용한다.
    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).
            createMediaSource(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    // song list를 media items로 변경한다.
    fun asMediaItems() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()


    // firestore 로부터 song을 download 받을 때 처리하기 위한 작업들. state
    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    // state 값이 바뀌면 trigger발생. 위에서 곡을 다 가져온 후 state = STATE_INITIALIZED 했을때
    // 밑의 state == STATE_INITIALIZED 가 true가 되면서 각 리스너의 state값을 변경한다.
    private var state: State = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        if (state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListeners += action
            return false
        } else {
            //람다 실행
            action(state == STATE_INITIALIZED)
            return true
        }
    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}