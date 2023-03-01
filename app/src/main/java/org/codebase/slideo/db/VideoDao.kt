package org.codebase.slideo.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.codebase.slideo.models.SaveVideoModel

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

}