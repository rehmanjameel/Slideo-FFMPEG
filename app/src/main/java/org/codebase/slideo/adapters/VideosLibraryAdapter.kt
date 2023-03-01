package org.codebase.slideo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import kotlinx.android.synthetic.main.video_view_layout.view.*
import org.codebase.slideo.R
import org.codebase.slideo.models.SaveVideoModel

class VideosLibraryAdapter(context: Context):
    RecyclerView.Adapter<VideosLibraryAdapter.VideosViewHolder>() {

    private val myContext = context
    private var videosArrayList = ArrayList<SaveVideoModel>()
    private lateinit var videoModel: SaveVideoModel
    private var exoPlayer: ExoPlayer? = null

    class VideosViewHolder(itemView: View) : ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.video_view_layout, parent, false)
        return VideosViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideosViewHolder, position: Int) {

        val videoModel = videosArrayList[position]

        exoPlayer = ExoPlayer.Builder(myContext).setSeekBackIncrementMs(5000L)
            .setSeekForwardIncrementMs(5000L)
            .build()
        exoPlayer?.playWhenReady = true
        holder.itemView.playerId.player = exoPlayer

        exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(videoModel.path))

            seekTo(0L)
            playWhenReady = playWhenReady
            prepare()
        }
    }

    override fun getItemCount(): Int {
        return videosArrayList.size
    }
}