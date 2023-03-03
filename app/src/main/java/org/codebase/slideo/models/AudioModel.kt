package org.codebase.slideo.models

import java.io.Serializable
import java.net.IDN

class AudioModel(
    val id: String,
    val title: String,
    val album: String,
    val artist: String,
    val duration: Long = 0,
    val path: String
//    val uriString: String,
//    val albumId: String,
): Serializable