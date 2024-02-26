package com.plcoding.spotifycloneyt.Exoplayer

import android.content.ComponentName
import android.content.Context
import android.media.session.PlaybackState
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.plcoding.spotifycloneyt.other.Constants.NETWORK_ERROR
import com.plcoding.spotifycloneyt.other.Event
import com.plcoding.spotifycloneyt.other.Resource

// 서비스와 액티비티를 연결 시켜주는 객체
// callback과 라이브데이터를 조합하여 액티비티에서 플레이 상태와 현재 재생중인 곡 데이터 변경을 관찰한다.
class MusicServiceConnection(
    context: Context
){
    private val _isConeected = MutableLiveData<Event<Resource<Boolean>>>() // 연결 여부에 대한 이벤트를 한번만 처리하기 위해 Event class사용
    val isConnected: LiveData<Event<Resource<Boolean>>> = _isConeected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError: LiveData<Event<Resource<Boolean>>> = _networkError

    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState: LiveData<PlaybackStateCompat?> = _playbackState

    private val _curPlayingSong = MutableLiveData<MediaMetadataCompat>()
    val curPlayingSong: LiveData<MediaMetadataCompat> = _curPlayingSong

    lateinit var mediaController: MediaControllerCompat

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    // mediaController의 instance가 생성되지 않을 수 있기 때문에 바로 넣어주지 않고 getter를 사용한다.
    // 미디어의 조작(재생, 다음곡 요청 등)을 위해 사용될것임
    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    // 뷰모델 등에서 특정 미디어 id에 대한 구독을 시작하거나 취소(unsubscribe)함으로
    // firebase에서 미디어 항목을 액세서할 수 있음
    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unSubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            MusicService::class.java
        ),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        // mediaBrowser 생성 후 connect 요청
        connect() }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ): MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            Log.d("MusicServiceConnection", "CONNECTED")

            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                //여기에 밑에서 만든 callback을 셋팅한다.
                registerCallback(MediaControllerCallback())
            }
            _isConeected.postValue(Event(Resource.success(true)))
        }

        override fun onConnectionSuspended() {
            Log.d("MusicServiceConnection", "SUSPENDED")

            _isConeected.postValue(Event(Resource.error(
                "The connection was suspended", false
            )))
        }

        override fun onConnectionFailed() {
            Log.d("MusicServiceConnection", "FAILED")

            _isConeected.postValue(Event(Resource.error(
                "Couldn't connect to media browser", false
            )
            ))
        }
    }

    private inner class MediaControllerCallback: MediaControllerCompat.Callback() {
        // 재생 상태
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        // 현재 재생 곡 정보 (metadata)
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _curPlayingSong.postValue(metadata)
        }

        // 세션
        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when(event) {
                NETWORK_ERROR -> _networkError.postValue(
                    Event(
                        Resource.error(
                            "Couldn't conncet to the server. Please check your internet connection",
                            null
                        )
                    )
                )
            }
        }

        // 세션 종료시 미디어 브라우져 연결 중단
        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}