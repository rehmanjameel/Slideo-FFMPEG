package org.codebase.slideo.adapters

import android.content.Context
import android.media.MediaPlayer
import android.media.browse.MediaBrowser.MediaItem
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsSeekBar
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.audio_items.view.*
import org.codebase.slideo.R
import org.codebase.slideo.models.AudioModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Handler
import kotlin.collections.ArrayList

class AudioAdapter(context: Context, private val audioArrayList: ArrayList<AudioModel>) :
    RecyclerView.Adapter<AudioAdapter.ViewHolder>() {

    val mContext = context

    //    private var audioArrayList = audioArrayList
    private lateinit var audioModel: AudioModel
    private lateinit var byte: ByteArray
    lateinit var mediaPlayer: MediaPlayer


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.audio_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        audioModel = audioArrayList[position]

        val audioDuration = getDate(audioModel.duration, "mm:ss")
        holder.itemView.audioNameTextId.text = audioModel.title
        holder.itemView.audioAlbumId.text = audioModel.album
        holder.itemView.audioDurationId.text = audioDuration

        holder.itemView.voicePlayerView.setAudio(audioModel.path)

//        holder.itemView.playAudioButton.setOnClickListener {
//            createMediaPlayer(
//                Uri.parse(audioModel.path), holder.itemView.pauseAudioButton,
//                holder.itemView.playAudioButton, holder.itemView.audio_seek_bar
//            )
//        }

//        holder.itemView.pauseAudioButton.setOnClickListener {
//            stopPlayer(holder.itemView.playAudioButton, holder.itemView.pauseAudioButton)
//        }


//        holder.itemView.audio_seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                if (fromUser)
//                    mediaPlayer.seekTo(progress)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//            }
//
//        })

//        val uri: Uri = Uri.parse(audioModel.uriString)
//        val albumId = audioModel.albumId
//        val imagePath = audioModel.imagePath
//        val imagePathUri = Uri.parse(imagePath)

        Log.e("audios", audioModel.path)
//        holder.itemView.audioNameTextId.text = audioModel.name

//        Thread {
//            android.os.Handler(Looper.getMainLooper()).post(Runnable {
//                if (imagePathUri != null) {
//                    Glide.with(mContext)
//                        .load(imagePathUri)
//                        .placeholder(R.mipmap.ic_launcher)
//                        .into(holder.itemView.audioThumbNail)
//                }
//            })
//        }.start()
    }

    fun getDate(milliSeconds: Long, dateFormat: String?): String? {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    override fun getItemCount(): Int {
        return audioArrayList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


    }

    fun createMediaPlayer(
        uri: Uri,
        pauseButton: ImageView,
        playButton: ImageView,
        seekBar: SeekBar
    ) {
//        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer = MediaPlayer.create(mContext, uri)

            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()

                playButton.visibility = View.GONE
                pauseButton.visibility = View.VISIBLE
                initializeSeekBar(seekBar)
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopPlayer(playButton: ImageView, pauseButton: ImageView) {

        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.release()
            pauseButton.visibility = View.GONE
            playButton.visibility = View.VISIBLE

        }
    }

    fun initializeSeekBar(seekBar: SeekBar) {
        seekBar.max = mediaPlayer.duration

        try {
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                seekBar.progress = mediaPlayer.currentPosition
            }, 1000)
        } catch (e: Exception) {
            seekBar.progress = 0

            e.printStackTrace()
        }
    }
}