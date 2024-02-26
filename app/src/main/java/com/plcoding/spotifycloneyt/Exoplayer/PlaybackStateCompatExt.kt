package com.plcoding.spotifycloneyt.Exoplayer

import android.media.session.PlaybackState
import android.support.v4.media.session.PlaybackStateCompat

// 상태 호환 확장
// 현재 재생 중인 상태를 전환하기 위해 노래를 재생하는 함수를 만들고
// 재생 상태 클래스에 대한 일부 확장 변수를 구현
// 실제 구글 uamp 프로젝트에서도 사용중임

inline val PlaybackStateCompat.isPrepared
    get() = state == PlaybackState.STATE_BUFFERING ||
            state == PlaybackState.STATE_PLAYING ||
            state == PlaybackState.STATE_PAUSED


inline val PlaybackStateCompat.isPlaying
    get() = state == PlaybackState.STATE_BUFFERING ||
            state == PlaybackState.STATE_PLAYING

inline val PlaybackStateCompat.isPlayEnabled
    get() = actions and PlaybackStateCompat.ACTION_PLAY != 0L ||
            (actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L &&
                    state == PlaybackStateCompat.STATE_PAUSED)