package org.codebase.slideo.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simform.videooperations.Common
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
        val fullScreen: ImageView = itemView.findViewById(R.id.imageViewFullScreen)
        val shareVideo: ImageView = itemView.findViewById(R.id.shareVideoId)
        val deleteVideo: ImageView = itemView.findViewById(R.id.deleteVideoId)
        val player: PlayerView = itemView.findViewById(R.id.playerId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.video_view_layout, parent, false)
        return VideosViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideosViewHolder, position: Int) {

        videoModel = videosArrayList[position]

        roomDB = RoomDB.getDataBase(myContext)

        holder.fullScreen.visibility = View.GONE
        exoPlayer = ExoPlayer.Builder(myContext).setSeekBackIncrementMs(5000L)
            .setSeekForwardIncrementMs(5000L)
            .build()
        exoPlayer?.playWhenReady = false
        holder.player.player = exoPlayer

        exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(videoModel.path))

            seekTo(0L)
//            playWhenReady = playWhenReady
            prepare()
        }

        holder.shareVideo.setOnClickListener {
            Common.startFileShareIntent(myContext, videoModel.path, "video")
        }
        holder.deleteVideo.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(myContext)

            builder.setIcon(R.drawable.ic_baseline_warning_amber_24)
            builder.setTitle("Delete Video!")
            builder.setMessage("Are you sure? Do you want to delete video?")
            builder.setPositiveButton(
                    "Yes") { dialogInterface, i ->
                roomDB.saveVideoDao().deleteVideo(videoModel.videoId)
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
    fun setData(videosList: ArrayList<SaveVideoModel>) {
        this.videosArrayList = videosList
        notifyDataSetChanged()
    }
}