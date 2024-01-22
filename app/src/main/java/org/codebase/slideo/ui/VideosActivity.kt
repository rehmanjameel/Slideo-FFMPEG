package org.codebase.slideo.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.codebase.slideo.R
import org.codebase.slideo.adapters.FireVideosAdapter
import org.codebase.slideo.adapters.VideosLibraryAdapter
import org.codebase.slideo.databinding.ActivityVideosBinding
import org.codebase.slideo.db.RoomDB
import org.codebase.slideo.models.SaveVideoModel
import org.codebase.slideo.models.VideosModel

class VideosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideosBinding
    lateinit var videoAdapter: VideosLibraryAdapter
    lateinit var fireAdapter: FireVideosAdapter
    lateinit var roomDB: RoomDB
    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideosBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (binding.videosRVId.equals("")) {
            binding.noVideoFoundId.visibility = View.VISIBLE
        } else {
            binding.noVideoFoundId.visibility = View.GONE
        }

        videoAdapter = VideosLibraryAdapter(this)
        binding.videosRVId.layoutManager = LinearLayoutManager(this)
        binding.videosRVId.adapter = videoAdapter

        roomDB = RoomDB.getDataBase(this)

        roomDB.saveVideoDao().getAllVideos().observe(this, Observer { list ->
            videoAdapter.setData(list as ArrayList<SaveVideoModel>)
        })

        fireAdapter = FireVideosAdapter(this)
        binding.fireVideosRVId.layoutManager = LinearLayoutManager(this)
        binding.fireVideosRVId.adapter = fireAdapter

        roomDB = RoomDB.getDataBase(this)

        // get user info
        val rootRef = FirebaseDatabase.getInstance().reference
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val uidRef = rootRef.child("slideo").child(uid).child("videos")
            com.google.android.exoplayer2.util.Log.e("time child", uidRef.toString())
            uidRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.e(
                        "videos of firebase",
                        snapshot.child("videoUri").value.toString()
                    )
                    for (d in snapshot.children) {
                        count++
                        Log.e("videos of furebase ${count}", d.value.toString() + d.key)

                        Log.e(
                            "videos of firebase",
                            d.child("videos").value.toString()
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
                    Log.e("cancelled", "error")
                }

            })

            uidRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.e("childListener", "${snapshot.child("videoUri").value}")
                    roomDB.saveVideoDao().deleteFireVideos()
                    for (d in snapshot.children) {
                        roomDB.saveVideoDao().addFireVideo(
                            VideosModel(
                                videoId = 0,
                                videoUri = "${snapshot.child("videoUri").value}",
                                videoKey = snapshot.key.toString()
                            )
                        )
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }

        roomDB.saveVideoDao().getAllFireVideos().observe(this, Observer { list ->
            fireAdapter.setData(list as ArrayList<VideosModel>)
        })

        binding.videoBackIcon.setOnClickListener {
            onBackPressed()
        }
    }
}