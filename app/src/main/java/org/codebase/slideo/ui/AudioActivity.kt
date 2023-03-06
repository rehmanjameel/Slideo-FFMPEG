package org.codebase.slideo.ui

import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_audio.*
import kotlinx.coroutines.DelicateCoroutinesApi
import org.codebase.slideo.R
import org.codebase.slideo.adapters.AudioAdapter
import org.codebase.slideo.models.AudioModel
import java.io.File

class AudioActivity : AppCompatActivity() {

    companion object{
        private lateinit var audioList: ArrayList<AudioModel>
    }

    private lateinit var audioAdapter: AudioAdapter

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

//        audioList = ArrayList()

        audioList = getAllAudios()
        audioRecyclerViewId.layoutManager = LinearLayoutManager(this@AudioActivity)
        audioAdapter = AudioAdapter(this@AudioActivity, audioList)

        audioRecyclerViewId.adapter = audioAdapter

//        asyncTask.execute(null.toString())
//        val looper = Looper.getMainLooper()
//        GlobalScope.launch(Dispatchers.IO) {
//            getAllAudios()
//
//            launch(Dispatchers.Main) {
//                audioAdapter = AudioAdapter(this@AudioActivity, audioList)
//
//                audioRecyclerViewId.layoutManager = LinearLayoutManager(this@AudioActivity)
//                audioRecyclerViewId.adapter = audioAdapter
//
//            }
//        }

    }

    private fun getAllAudios(): ArrayList<AudioModel> {
        val tempAudioList: ArrayList<AudioModel> = ArrayList()

        val externalUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val columns = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION,
         MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.DATA)

        val where: String = MediaStore.Audio.Media.IS_MUSIC + "!=0"
        val cursor: Cursor? = this.contentResolver.query(externalUri, columns, where, null,
            MediaStore.Audio.Media.DATE_ADDED+ " DESC", null)

        if (cursor!!.moveToFirst()) {
            do {
                val titleC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val idC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val albumC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val artistC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val pathC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
//                val albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
//                val imagePath = Uri.parse("content://media/external/audio/albumart")
//                val imagePathUri = ContentUris.withAppendedId(imagePath, albumId)

                val audioModel = AudioModel(id = idC, title = titleC, album = albumC, artist = artistC,
                    duration = duration, path = pathC)

                val file = File(audioModel.path)
                if (file.exists())
                    tempAudioList.add(audioModel)

            } while (cursor.moveToNext())
            cursor.close()
        }

        return tempAudioList
    }

//    val asyncTask = object : AsyncTaskResolver<String, Boolean, Boolean>() {
//        override fun doInBackground(vararg many: String): Boolean {
//            return try {
//                audioList = getAllAudios()
//                true
//            } catch (e: java.lang.Exception) {
//
//                Log.e("erreor uploading", e.message.toString())
//                false
//            }
//        }
//
//        override fun onPostExecute(data: Boolean) {
//            audioAdapter = AudioAdapter(this@AudioActivity, audioList)
//
//            audioRecyclerViewId.adapter = audioAdapter
//        }
//
//        override fun onPreExecute() {
////            TODO("Not yet implemented")
//        }
//
//        override fun onProgressUpdate(progress: Boolean) {
////            TODO("Not yet implemented")
//        }
//
//    }
}