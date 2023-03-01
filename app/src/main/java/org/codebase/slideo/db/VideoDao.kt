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
    suspend fun addVideos(saveVideos: SaveVideoModel)

    @Query("Select * from saveVideoTable order by videoId ASC")
    fun getAllVideos(): LiveData<List<SaveVideoModel>>

    @Query("Delete from saveVideoTable where videoId=: deleteVideoId")
    suspend fun deleteVideo(deleteVideoId: Int)

    @Query("Delete from saveVideTable")
    suspend fun deleteVideos()

}