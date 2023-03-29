package org.codebase.slideo.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.codebase.slideo.models.SaveVideoModel
import org.codebase.slideo.models.VideosModel

@Database(entities = [SaveVideoModel::class, VideosModel::class], version = 2, exportSchema = false)
abstract class RoomDB: RoomDatabase() {

    abstract fun saveVideoDao(): VideoDao

    companion object {
        @Volatile
        private var INSTANCE: RoomDB? = null

        fun getDataBase(context: Context): RoomDB {
            val tempInstance = INSTANCE

            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoomDB::class.java,
                    "saveVideoDataBase"
                ).allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }

    }
}