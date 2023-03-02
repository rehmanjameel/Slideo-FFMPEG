package org.codebase.slideo.ui

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_audio.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.codebase.slideo.R
import org.codebase.slideo.adapters.AudioAdapter
import org.codebase.slideo.models.AudioModel

class AudioActivity : AppCompatActivity() {

    private lateinit var audioList: ArrayList<AudioModel>

    private lateinit var audioAdapter: AudioAdapter

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

        audioList = ArrayList()


//        val looper = Looper.getMainLooper()
        GlobalScope.launch(Dispatchers.IO) {
            getAllAudios()

            launch(Dispatchers.Main) {
                audioAdapter = AudioAdapter(this@AudioActivity, audioList)

                audioRecyclerViewId.layoutManager = LinearLayoutManager(this@AudioActivity)
                audioRecyclerViewId.adapter = audioAdapter

            }
        }

    }

    private fun getAllAudios(): ArrayList<AudioModel> {
        val tempAudioList: ArrayList<AudioModel> = ArrayList()

        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val columns = listOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST).toTypedArray()

        val where: String = MediaStore.Audio.Media.IS_MUSIC + "=1"
        val cursor: Cursor? = this.contentResolver.query(uri, columns, where, null, null)

        while (cursor!!.moveToFirst()) {
            val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
            val album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
            val audioUri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
            val albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
            val duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
            val imagePath = Uri.parse("content://media/external/audio/albumart")
            val imagePathUri = ContentUris.withAppendedId(imagePath, albumId)

            val audioModel = AudioModel(title, album, artist, duration.toString(), uri.toString(),
                albumId.toString(), imagePathUri.toString())

            tempAudioList.add(audioModel)
        }

        return tempAudioList
    }
}