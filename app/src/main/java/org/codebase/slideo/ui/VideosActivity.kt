package org.codebase.slideo.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import kotlinx.android.synthetic.main.activity_videos.*
import org.codebase.slideo.R
import org.codebase.slideo.adapters.VideosLibraryAdapter
import org.codebase.slideo.db.RoomDB
import org.codebase.slideo.models.SaveVideoModel

class VideosActivity : AppCompatActivity() {

    lateinit var videoAdapter: VideosLibraryAdapter
    lateinit var roomDB: RoomDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos)

        if (videosRVId.equals("")) {
            noVideoFoundId.visibility = View.VISIBLE
        } else {
            noVideoFoundId.visibility = View.GONE
        }

        videoAdapter = VideosLibraryAdapter(this)
        videosRVId.layoutManager = LinearLayoutManager(this)
        videosRVId.adapter = videoAdapter

        roomDB = RoomDB.getDataBase(this)

        roomDB.saveVideoDao().getAllVideos().observe(this, Observer { list ->
            videoAdapter.setData(list as ArrayList<SaveVideoModel>)
        })

        videoBackIcon.setOnClickListener {
            onBackPressed()
        }
    }
}