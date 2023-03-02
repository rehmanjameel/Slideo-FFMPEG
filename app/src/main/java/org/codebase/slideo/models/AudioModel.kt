package org.codebase.slideo.models

import java.io.Serializable

class AudioModel(
    val name: String,
    val album: String,
    val artist: String,
    val duration: String,
    val uriString: String,
    val albumId: String,
    val imagePath: String
): Serializable