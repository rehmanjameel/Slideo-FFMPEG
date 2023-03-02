package org.codebase.slideo.adapters

import android.content.Context
import android.net.Uri
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.audio_items.view.*
import org.codebase.slideo.R
import org.codebase.slideo.models.AudioModel

class AudioAdapter(context: Context, audioArrayList: ArrayList<AudioModel>): RecyclerView.Adapter<AudioAdapter.ViewHolder>() {

    val mContext = context
    private var audioArrayList = audioArrayList
    private lateinit var audioModel: AudioModel
    private lateinit var byte: ByteArray


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.audio_items, parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        audioModel = audioArrayList[position]

        val uri: Uri = Uri.parse(audioModel.uriString)
        val albumId = audioModel.albumId
        val imagePath = audioModel.imagePath
        val imagePathUri = Uri.parse(imagePath)

        holder.itemView.audioNameTextId.text = audioModel.name

        Thread {
            android.os.Handler(Looper.getMainLooper()).post(Runnable {
                if (imagePathUri != null) {
                    Glide.with(mContext)
                        .load(imagePathUri)
                        .placeholder(R.drawable.placeholder)
                        .into(holder.itemView.audioThumbNail)
                }
            })
        }.start()
    }

    override fun getItemCount(): Int {
        return audioArrayList.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    }
}