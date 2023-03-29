package org.codebase.slideo.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "fire_videos_table")
class VideosModel (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "video_id") val videoId: Int,
    @ColumnInfo(name = "video_uri") val videoUri: String

) : Serializable