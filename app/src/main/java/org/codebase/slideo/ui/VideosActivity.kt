package org.codebase.slideo.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_videos.*
import org.codebase.slideo.R

class VideosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos)

        if (videosRVId.equals("")) {
            noVideoFoundId.visibility = View.VISIBLE
        } else {
            noVideoFoundId.visibility = View.GONE
        }
    }
}