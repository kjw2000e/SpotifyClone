package com.plcoding.spotifycloneyt.ui

import android.media.MediaMetadata.METADATA_KEY_MEDIA_ID
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.SubscriptionCallback
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.plcoding.spotifycloneyt.Exoplayer.MusicServiceConnection
import com.plcoding.spotifycloneyt.Exoplayer.isPlayEnabled
import com.plcoding.spotifycloneyt.Exoplayer.isPlaying
import com.plcoding.spotifycloneyt.Exoplayer.isPrepared
import com.plcoding.spotifycloneyt.data.entities.Song
import com.plcoding.spotifycloneyt.other.Constants.MEDIA_ROOT_ID
import com.plcoding.spotifycloneyt.other.Resource

class MainViewModel @ViewModelInject constructor(
   private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val curPlayingSong = musicServiceConnection.curPlayingSong
    val playbackState = musicServiceConnection.playbackState

    // media_root id 구독하기 (특정 id를 구독하는 기능)
    init {
        // 가져오기전 로딩
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(MEDIA_ROOT_ID, object : SubscriptionCallback() {
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                // children의 type을 List<Song>으로 mapping하는 작업 필요.
                val items = children.map { mediaItem ->
                    Song(
                        mediaId = mediaItem.mediaId!!,
                        title = mediaItem.description.title.toString(),
                        subTitle = mediaItem.description.subtitle.toString(),
                        songUrl = mediaItem.description.mediaUri.toString(),
                        imageUrl = mediaItem.description.iconUri.toString()
                    )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })
    }

    // 다음곡으로 건너띄기
    // 플레이리스트 가져오고 현재 재생곡 인덱스값 가져온 다음 다음 인덱스에 해당하는 곡으로 prepare()요청 하면될듯?
    // -> 는 TransportControls의 skipToNext()함수를 호출하면됨. 인덱스 관리 필요없음
    // 재생 정보를 가져오기 위헤 컨트롤러에 접근할 수 있어야한다.
    fun skipToNextSong() {
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong() {
        musicServiceConnection.transportControls.skipToPrevious()
    }

    // 재생 위치 탐색
    fun seekTo(pos: Long) {
        musicServiceConnection.transportControls.seekTo(pos)
    }

    // 현재 곡과 동일한 곡을 재생 요청 시 toggle (play or pause)
    // 다른 곡이면 재생 요청
    // 여기에서 toggle값을 인자로 넘겨주지 않으면 false로 넣었는데 toggle을 하지 않고 새로운 곡을 재생요청한다는 의미이다.
    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        // 준비가 되었고 요청곡과 현재 재생곡의 mediaId와 같은지 확인
        if (isPrepared && mediaItem.mediaId ==
            curPlayingSong?.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    // 뷰모델이 destroy될때 musicServiceConnection 구독 취소
    // callback을 빈값으로 넣었다.
    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unSubscribe(MEDIA_ROOT_ID, object : SubscriptionCallback() {})
    }
}