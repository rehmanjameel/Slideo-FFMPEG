package org.codebase.slideo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.simform.videooperations.Common
import kotlinx.android.synthetic.main.custom_controller.view.*
import kotlinx.android.synthetic.main.video_view_layout.view.*
import org.codebase.slideo.R
import org.codebase.slideo.db.RoomDB
import org.codebase.slideo.models.SaveVideoModel

class VideosLibraryAdapter(context: Context):
    RecyclerView.Adapter<VideosLibraryAdapter.VideosViewHolder>() {

    private val myContext = context
    private var videosArrayList = ArrayList<SaveVideoModel>()
    private lateinit var videoModel: SaveVideoModel
    private var exoPlayer: ExoPlayer? = null
    lateinit var roomDB: RoomDB

    class VideosViewHolder(itemView: View) : ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.video_view_layout, parent, false)
        return VideosViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideosViewHolder, position: Int) {

        val videoModel = videosArrayList[position]

        roomDB = RoomDB.getDataBase(myContext)

        holder.itemView.imageViewFullScreen.visibility = View.GONE
        exoPlayer = ExoPlayer.Builder(myContext).setSeekBackIncrementMs(5000L)
            .setSeekForwardIncrementMs(5000L)
            .build()
        exoPlayer?.playWhenReady = false
        holder.itemView.playerId.player = exoPlayer

        exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(videoModel.path))

            seekTo(0L)
//            playWhenReady = playWhenReady
            prepare()
        }

        holder.itemView.shareVideoId.setOnClickListener {
            Common.shareVideo(myContext, videoModel.path)
        }
        holder.itemView.deleteVideoId.setOnClickListener {
            roomDB.saveVideoDao().deleteVideo(videoModel.videoId)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return videosArrayList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(videosList: ArrayList<SaveVideoModel>) {
        this.videosArrayList = videosList
        notifyDataSetChanged()
    }
}