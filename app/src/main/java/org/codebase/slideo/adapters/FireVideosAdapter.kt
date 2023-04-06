package org.codebase.slideo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.simform.videooperations.Common
import kotlinx.android.synthetic.main.custom_controller.view.*
import kotlinx.android.synthetic.main.video_view_layout.view.*
import org.codebase.slideo.R
import org.codebase.slideo.db.RoomDB
import org.codebase.slideo.models.SaveVideoModel
import org.codebase.slideo.models.VideosModel
import java.util.*
import kotlin.collections.ArrayList

class FireVideosAdapter(context: Context):
    RecyclerView.Adapter<FireVideosAdapter.FireViewHolder>() {

    private val myContext = context
    private var videosArrayList = ArrayList<VideosModel>()
    private lateinit var videoModel: VideosModel
    private var exoPlayer: ExoPlayer? = null
    lateinit var roomDB: RoomDB

    class FireViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FireViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.video_view_layout, parent, false)
        return FireViewHolder(view)
    }

    override fun onBindViewHolder(holder: FireViewHolder, position: Int) {
        videoModel = videosArrayList[position]

        roomDB = RoomDB.getDataBase(myContext)

        holder.itemView.imageViewFullScreen.visibility = View.GONE
        holder.itemView.fireImageId.visibility = View.VISIBLE
        exoPlayer = ExoPlayer.Builder(myContext).setSeekBackIncrementMs(5000L)
            .setSeekForwardIncrementMs(5000L)
            .build()
        exoPlayer?.playWhenReady = false
        holder.itemView.playerId.player = exoPlayer

        exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(videoModel.videoUri))

            seekTo(0L)
//            playWhenReady = playWhenReady
            prepare()
        }

        holder.itemView.shareVideoId.setOnClickListener {
            Common.startFileShareIntent(myContext, videoModel.videoUri, "video")
        }
        val fileName = UUID.randomUUID().toString()

        // get user info
        val rootRef = FirebaseDatabase.getInstance().reference
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        holder.itemView.deleteVideoId.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(myContext)

            builder.setIcon(R.drawable.ic_baseline_warning_amber_24)
            builder.setTitle("Delete Video!")
            builder.setMessage("Are you sure? Do you want to delete video?")
            builder.setPositiveButton(
                "Yes") { dialogInterface, i ->
                roomDB.saveVideoDao().deleteFireVideo(videoModel.videoId)
                rootRef.child("slideo").child(uid).child("videos").child(videoModel.videoKey)
                    .removeValue()
                FirebaseStorage.getInstance().getReference("slideo_profile/$fileName")
                    .child(videoModel.videoUri).delete()
                notifyDataSetChanged()
            }

            builder.setNegativeButton(
                "No"
            ) { dialogInterface, i ->
                dialogInterface.dismiss()
            }

            builder.show()
        }
    }

    override fun getItemCount(): Int {
        return videosArrayList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(videosList: ArrayList<VideosModel>) {
        this.videosArrayList = videosList
        notifyDataSetChanged()
    }
}