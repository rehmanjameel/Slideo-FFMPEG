package org.codebase.slideo.videoProcessActivity

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Log
import com.simform.videooperations.Common
import kotlinx.android.synthetic.main.activity_combine_images.*
import kotlinx.android.synthetic.main.custom_controller.*
import org.codebase.slideo.R

class CombineImages : AppCompatActivity() {

    private var playbackPosition = 0L
    private var exoPlayer: ExoPlayer? = null
    var mOrientationListener: OrientationEventListener? = null
    lateinit var videoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_combine_images)

        supportActionBar?.hide()

        videoPath = intent.getStringExtra("video_path").toString()
        Common.loadVideoFromInternalStorage(videoPath)
        Log.e("vedo path ", videoPath)
        preparePlayer()
        setFullScreen()
    }

    private fun preparePlayer() {
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

//                buttonId.visibility = View.GONE
//                buttonIdFlip.visibility = View.GONE
//                pickVideoButtonId.visibility = View.GONE
//                window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
//                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
//                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                supportActionBar?.hide()
//                window.requestFeature(Window.FEATURE_ACTION_BAR)
//                supportActionBar?.hide()
            } else {
                imageViewFullScreen.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.ic_baseline_fullscreen
                    )
                )
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//                buttonId.visibility = View.VISIBLE
//                buttonIdFlip.visibility = View.VISIBLE
//                pickVideoButtonId.visibility = View.VISIBLE

            }
            isFullScreen = !isFullScreen
        }
    }

//    private fun setOrientation() {
//        mOrientationListener = object : OrientationEventListener(
//            this,
//            SensorManager.SENSOR_DELAY_NORMAL
//        ) {
//            override fun onOrientationChanged(orientation: Int) {
//                Log.e(
//                    "DEBUG_TAG",
//                    "Orientation changed to $orientation"
//                )
//
//                when (orientation) {
//                    in 1..89 -> {
//                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
//                        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
//                            buttonId.visibility = View.INVISIBLE
//                        }
//                    }
//                    in 180 .. 360 -> {
//                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//
//                        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
//                            buttonId.visibility = View.INVISIBLE
//                        }
//                    }
//                    in 90 .. 180 -> {
//                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
//                        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
//                            buttonId.visibility = View.INVISIBLE
//                        }
//                    }
//                }
//            }
//        }
//    }

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

    companion object {
        private const val URL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

        private var isFullScreen = false
        private var isLock = false
        private const val INCREMENT_MILLIS = 5000L
    }
}