package org.codebase.slideo.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "save_videos_table")
class SaveVideoModel (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "video_id") val videoId: Int,
    @ColumnInfo(name = "video_path") val path: String,
    @ColumnInfo(name = "created_time") val time: String
    ): Serializable