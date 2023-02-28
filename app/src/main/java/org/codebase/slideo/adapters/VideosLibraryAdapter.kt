package org.codebase.slideo.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class VideosLibraryAdapter(context: Context):
    RecyclerView.Adapter<VideosLibraryAdapter.VideosViewHolder>() {

    private val myContext = context

    class VideosViewHolder(itemView: View) : ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: VideosViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }
}