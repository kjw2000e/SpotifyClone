package com.plcoding.spotifycloneyt.Exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.plcoding.spotifycloneyt.data.entities.Song

// mediaMeta data를 Song으로 변환하는 확장함수?

fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(
            it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.iconUri.toString(),
            it.mediaUri.toString(),
        )
    }

}