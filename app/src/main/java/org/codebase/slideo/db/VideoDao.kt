package org.codebase.slideo.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.codebase.slideo.models.SaveVideoModel
import org.codebase.slideo.models.VideosModel

@Dao
interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addVideo(saveVideos: SaveVideoModel)

    @Query("Select * from save_videos_table order by video_id ASC")
    fun getAllVideos(): LiveData<List<SaveVideoModel>>

    @Query("Delete from save_videos_table where video_id= :deleteVideoId")
    fun deleteVideo(deleteVideoId: Int)

    @Query("Delete from save_videos_table")
    suspend fun deleteVideos()

    // insert firebase videos
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addFireVideo(videosModel: VideosModel)

    @Query("Select * from fire_videos_table order by video_id ASC")
    fun getAllFireVideos(): LiveData<List<VideosModel>>

    @Query("Delete from fire_videos_table where video_id= :deleteVideoId")
    fun deleteFireVideo(deleteVideoId: Int)

    @Query("Delete from fire_videos_table")
    fun deleteFireVideos()

}