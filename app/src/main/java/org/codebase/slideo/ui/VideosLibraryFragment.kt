package org.codebase.slideo.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.codebase.slideo.R

class VideosLibraryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_videos_library, container, false)

//        if (view.videosRVId == null) {
//            noVideoFoundId.visibility = View.VISIBLE
//        } else {
//            noVideoFoundId.visibility = View.INVISIBLE
//        }
        return view
    }

    fun newInstance() = VideosLibraryFragment()
}