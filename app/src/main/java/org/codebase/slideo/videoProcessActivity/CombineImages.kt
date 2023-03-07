package org.codebase.slideo.videoProcessActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.simform.videooperations.*
import kotlinx.android.synthetic.main.activity_combine_images.*
import kotlinx.android.synthetic.main.alert_dialog_layout.*
import kotlinx.android.synthetic.main.custom_controller.*
import org.codebase.slideo.MainActivity
import org.codebase.slideo.R
import org.codebase.slideo.db.RoomDB
import org.codebase.slideo.models.SaveVideoModel
import org.codebase.slideo.ui.AudioActivity
import org.codebase.slideo.utils.App
import kotlin.math.log

class CombineImages : AppCompatActivity() {

    private var playbackPosition = 0L
    private var exoPlayer: ExoPlayer? = null
    var mOrientationListener: OrientationEventListener? = null
    lateinit var videoPath: String
    val ffmpegQueryExtension = FFmpegQueryExtension()
    var videoOutPutPath = ""
    lateinit var roomDB: RoomDB
    private var intentAudioPath: String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        setContentView(R.layout.activity_combine_images)

        supportActionBar?.hide()

        videoPath = intent.getStringExtra("video_path").toString()
        Common.loadVideoFromInternalStorage(videoPath)
        Log.e("vedo path ", "$videoPath + $intentAudioPath")
        preparePlayer(videoPath)
//        setFullScreen()

        imageViewFullScreen.visibility = View.GONE

        textLayoutId.setOnClickListener {
//            alertDialogDemo()
            getText()
        }

        roomDB = RoomDB.getDataBase(this)
        saveVideoToDB.setOnClickListener {
            Log.e("internal path", videoOutPutPath)
            roomDB.saveVideoDao().addVideo(SaveVideoModel(0,
                App.getString("video_output_path"), System.currentTimeMillis().toString()))
            Snackbar.make(this, it, "Video Saved Successfully!",
                Snackbar.ANIMATION_MODE_SLIDE).show()
        }

        cancelVideoId.setOnClickListener {
            onBackPressed()
        }

        musicImageId.setOnClickListener {
            startActivity(Intent(this, AudioActivity::class.java))
        }

        intentAudioPath = intent.getStringExtra("audio_path").toString()
        Log.e("intent path", intentAudioPath)
        if (intentAudioPath != "null") {
            processStart()
            audioPlayerLayoutId.visibility = View.VISIBLE
            voicePlayerView.setAudio(intentAudioPath)
            preparePlayer(App.getString("video_output_path"))
            mergeAudioVideo(intentAudioPath)
        } else {
            audioPlayerLayoutId.visibility = View.GONE
        }

        cancelAudioMerging.setOnClickListener {
            audioPlayerLayoutId.visibility = View.GONE
            voicePlayerView.onStop()
        }

    }

    private fun preparePlayer(videoPath: String) {
        exoPlayer = ExoPlayer.Builder(this).setSeekBackIncrementMs(INCREMENT_MILLIS)
            .setSeekForwardIncrementMs(INCREMENT_MILLIS)
            .build()
        exoPlayer?.playWhenReady = true
        player.player = exoPlayer

        exoPlayer?.apply {
            player.scaleX = 1f
            setMediaItem(MediaItem.fromUri(videoPath))
//            setMediaSource(buildMediaSource())
            seekTo(playbackPosition)
            playWhenReady = playWhenReady
            prepare()
        }
    }

    private fun addTextProcess(textInput: String, startTime: String, endTime: String) {
        videoOutPutPath = Common.getInternalPath(this, Common.VIDEO)
//        val xPos = width?.let {
//            (edtXPos.text.toString().toFloat().times(it)).div(100)
//        }
//        val yPos = height?.let {
//            (edtYPos.text.toString().toFloat().times(it)).div(100)
//        }
        val fontPath = Common.getFileFromAssets(this, "little_lord.ttf").absolutePath
        android.util.Log.e("test text is here5454", "$startTime,,.,.$endTime")

        val query = ffmpegQueryExtension.addTextOnVideo(
            App.getString("video_output_path"),
            textInput, 200f, 1000f,
            fontPath = fontPath, isTextBackgroundDisplay = true,
            fontSize = 70, fontcolor = "red", output = videoOutPutPath, startTime, endTime)
        CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
//                tvOutputPath.text = logMessage.text
                processStart()
            }

            override fun success() {
//                tvOutputPath.text = String.format(getString(R.string.output_path), outputPath)
                if (exoPlayer != null) {
                    exoPlayer?.clearVideoSurface()
                }
                preparePlayer(videoOutPutPath)
                Log.e("process mesage success", "isSuccess???? $videoOutPutPath")

                App.saveString("video_output_path", videoOutPutPath)
                videoTextET.isFocusable = true
                videoTextStartTimeET.isFocusable = true
                videoTextEndTimeET.isFocusable = true
                processStop()
            }

            override fun cancel() {
                processStop()
            }

            override fun failed() {
                processStop()
            }
        })
    }

    private fun getText() {
        val videoText = videoTextET.text.toString()
        val videoTextStartTime = videoTextStartTimeET.text.toString()
        val videoTextEndTime = videoTextEndTimeET.text.toString()

        if (videoText.isNotEmpty()) {
            addTextProcess(textInput = videoText, startTime = videoTextStartTime, endTime = videoTextEndTime)
            videoTextET.setText("")
            videoTextStartTimeET.setText("")
            videoTextEndTimeET.setText("")
            videoTextET.isFocusable = false
            videoTextStartTimeET.isFocusable = false
            videoTextEndTimeET.isFocusable = false
        } else {
            videoTextET.error = "Text is required"
        }
    }

    private fun mergeAudioVideo(intentAudioPath: String) {
        android.util.Log.e("path", intentAudioPath)

        videoOutPutPath = Common.getInternalPath(this, Common.VIDEO)
        val query = ffmpegQueryExtension.mergeAudioVideo(App.getString("video_output_path"),
            intentAudioPath, videoOutPutPath)
//            arrayOf("-i", App.getString("video_output_path"), "-i", intentAudioPath,
//            "-c:v", "copy", "-c:a", "aac", "-strict", "experimental", "-map",
//            "0:v:0", "-map", "1:a:0", "-shortest", outputPath)

        CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
//                tvOutputPath.text = logMessage.text
                Log.e("process mesage", logMessage.text)
                processStart()
            }

            override fun success() {
//                tvOutputPath.text = String.format(getString(R.string.output_path), outputPath)
                Log.e("process mesage success", "isSuccess???? $videoOutPutPath")
                preparePlayer(videoOutPutPath)
                App.saveString("video_output_path", videoOutPutPath)
                processStop()
            }

            override fun cancel() {
                Log.e("process mesage", "canecl")

                processStop()
            }

            override fun failed() {
                Log.e("process mesage", "failed")
                processStop()
            }

        })
    }

    private fun processStop() {
        mProgressView.visibility = View.GONE
    }

    private fun processStart() {
        mProgressView.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isLock) return
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imageViewFullScreen.performClick()
        } else super.onBackPressed()
    }

    private fun alertDialogDemo() {
        // get alert_dialog.xml view
        val li: LayoutInflater = LayoutInflater.from(applicationContext)
        val promptsView: View = li.inflate(R.layout.alert_dialog_layout, null)
        val alertDialogBuilder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)

        // set alert_dialog.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView)
        val userInput: EditText = promptsView.findViewById<View>(R.id.etUserInput) as EditText
        val startTime: EditText = promptsView.findViewById<View>(R.id.etStartTime) as EditText
        val endTime: EditText = promptsView.findViewById<View>(R.id.etEndTime) as EditText

        // set dialog message
        alertDialogBuilder
            .setCancelable(false)
            .setPositiveButton("Add") { dialog, id -> // get user input and set it to result
                // edit text
                Toast.makeText(
                    applicationContext,
                    "Entered: " + userInput.text.toString(),
                    Toast.LENGTH_LONG
                ).show()
                processStart()

//                addTextProcess(userInput.text.toString(), startTime.text.toString(), endTime.text.toString())
            }
            .setNegativeButton("Cancel"
            ) { dialog, id -> dialog.cancel() }

        // create alert dialog
        val alertDialog: AlertDialog = alertDialogBuilder.create()

        // show it
        alertDialog.show()
    }

    //creating mediaSource
    private fun buildMediaSource(): MediaSource {
        // Create a data source factory.
        val dataSourceFactory: DefaultHttpDataSource.Factory = DefaultHttpDataSource.Factory()

        // Create a progressive media source pointing to a stream uri.

        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(URL))
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setFullScreen() {
        imageViewFullScreen.setOnClickListener {

            if (!isFullScreen) {
                imageViewFullScreen.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_baseline_fullscreen_exit
                    )
                )
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

            } else {
                imageViewFullScreen.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_baseline_fullscreen
                    )
                )
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            isFullScreen = !isFullScreen
        }
    }

    companion object {
        private const val URL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

        private var isFullScreen = false
        private var isLock = false
        private const val INCREMENT_MILLIS = 5000L
    }
}