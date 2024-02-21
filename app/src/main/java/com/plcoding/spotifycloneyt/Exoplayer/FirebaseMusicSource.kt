package com.plcoding.spotifycloneyt.Exoplayer

import com.plcoding.spotifycloneyt.Exoplayer.State.STATE_CREATED
import com.plcoding.spotifycloneyt.Exoplayer.State.STATE_ERROR
import com.plcoding.spotifycloneyt.Exoplayer.State.STATE_INITIALIZED
import com.plcoding.spotifycloneyt.Exoplayer.State.STATE_INITIALIZING

class FirebaseMusicSource {
    // firestore 로부터 song을 download 받을 때 처리하기 위한 작업들. state
    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = STATE_CREATED
        set(value) {
            if (state == STATE_INITIALIZED || state == STATE_ERROR) {
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