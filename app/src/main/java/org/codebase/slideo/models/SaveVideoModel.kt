package org.codebase.slideo.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "saveVideosTable")
class SaveVideoModel (
    @PrimaryKey(autoGenerate = true)
    val videoId: Int,
    val path: String,
    val time: String
    ): Serializable