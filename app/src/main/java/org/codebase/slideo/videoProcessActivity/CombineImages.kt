package org.codebase.slideo.videoProcessActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.simform.videooperations.*
import kotlinx.android.synthetic.main.activity_combine_images.*
import kotlinx.android.synthetic.main.activity_combine_images.mProgressView
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.alert_dialog_layout.*
import kotlinx.android.synthetic.main.custom_controller.*
import org.codebase.slideo.MainActivity
import org.codebase.slideo.R
import org.codebase.slideo.db.RoomDB
import org.codebase.slideo.models.SaveVideoModel
import org.codebase.slideo.models.UserModel
import org.codebase.slideo.models.VideosModel
import org.codebase.slideo.ui.AudioActivity
import org.codebase.slideo.ui.LoginActivity
import org.codebase.slideo.utils.App
import java.io.File
import java.util.*
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
            uploadImageToFirebaseStorage()
            Log.e("internal path", videoOutPutPath)
            roomDB.saveVideoDao().addVideo(SaveVideoModel(0,
                App.getString("video_output_path"), System.currentTimeMillis().toString()))
            Snackbar.make(this, it, "Video Saved Successfully!",
                Snackbar.ANIMATION_MODE_SLIDE).show()
        }

        cancelVideoId.setOnClickListener {
            App.removeKey("audio_path")
            onBackPressed()
        }

        musicImageId.setOnClickListener {
            startActivity(Intent(this, AudioActivity::class.java))
        }

        cancelAudioMerging.setOnClickListener {
            audioPlayerLayoutId.visibility = View.GONE
            App.removeKey("audio_path")
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
//            videoTextET.isFocusable = false
//            videoTextStartTimeET.isFocusable = false
//            videoTextEndTimeET.isFocusable = false
        } else {
            videoTextET.error = "Text is required"
        }
    }

    private fun mergeAudioVideo(intentAudioPath: String) {
        android.util.Log.e("path", intentAudioPath)

        videoOutPutPath = Common.getInternalPath(this, Common.VIDEO)
        val query =
            ffmpegQueryExtension.mergeAudioVideo(App.getString("video_output_path"),
            intentAudioPath, videoOutPutPath)
//            arrayOf("-i", App.getString("video_output_path"), "-i", intentAudioPath,
//            "-c:v", "copy", "-c:a", "aac", "-strict", "experimental", "-map",
//            "0:v:0", "-map", "1:a:0", "-shortest", videoOutPutPath)

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

    override fun onResume() {
        super.onResume()

        intentAudioPath = App.getString("audio_path")
        Log.e("intent path", intentAudioPath)
        if (intentAudioPath.isNotEmpty()) {
            audioPlayerLayoutId.visibility = View.VISIBLE
            voicePlayerView.setAudio(intentAudioPath)
            preparePlayer(App.getString("video_output_path"))
            mergeAudioId.setOnClickListener {
                processStart()
                mergeAudioVideo(intentAudioPath)
            }
        } else {
            audioPlayerLayoutId.visibility = View.GONE
        }

    }
    override fun onStop() {
        super.onStop()
        exoPlayer?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        App.removeKey("audio_path")

    }

    override fun onPause() {
        super.onPause()

        App.removeKey("audio_path")

        exoPlayer?.pause()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isLock) return
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imageViewFullScreen.performClick()
            App.removeKey("audio_path")
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

    private fun uploadImageToFirebaseStorage() {
        val videoPath = App.getString("video_output_path")
        Log.e("video out path in", videoPath)
        if (videoPath.isEmpty()) return
        val fileName = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("slideo_profile/$fileName")
        ref.putFile(File(videoPath).toUri())
            .addOnSuccessListener {
                android.util.Log.d("Video Storage", "Video successfully uploaded: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {videoUri ->
                    android.util.Log.d("video uri", "File Location: $videoUri")

//                    App.saveString("ProfileImageUrl", imageUri.toString())
                    saveUserToFireBaseDatabase(videoUri.toString())
                    mProgressView.visibility = View.GONE

//                    val intent = Intent(this, LoginActivity::class.java)
//                    startActivity(intent)
//                    finish()
                }.addOnFailureListener {e ->
                    mProgressView.visibility = View.GONE

                    Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                mProgressView.visibility = View.GONE
                android.util.Log.d("ImageUri", "File Location failed")
            }
    }

    private fun saveUserToFireBaseDatabase(videoUri: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        App.saveString("UID", uid)

        val userVideos = VideosModel(videoUri = videoUri)

        val ref = FirebaseDatabase.getInstance().getReference("slideo/${FirebaseAuth.getInstance().currentUser!!.uid}")
            .child(System.currentTimeMillis().toString())
            .child("videos")
        ref.setValue(userVideos).addOnCompleteListener{ videoSent ->
            if (videoSent.isSuccessful) {
                mProgressView.visibility = View.GONE
                Toast.makeText(this, "video saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                mProgressView.visibility = View.GONE
                Toast.makeText(this, "video sent failed ${videoSent.exception!!.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }


//        val vidRef = ref.child("videos")
//        vidRef.setValue(userVideos)
//        android.util.Log.e("login success", vidRef.toString())


//        ref.setValue(userMessages)
//            .addOnSuccessListener {
//                Log.d("Data to FBDB", "Successfully added")
//            }
    }

    companion object {
        private const val URL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

        private var isFullScreen = false
        private var isLock = false
        private const val INCREMENT_MILLIS = 5000L
    }
}