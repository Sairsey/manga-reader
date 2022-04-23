package com.mangajet.mangajet.ui.favourite

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mangajet.mangajet.R
import com.mangajet.mangajet.log.Logger

// Class which represents "Updates" fragment of MainActivity
class FavouriteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.log("Favourite opened")
        return inflater.inflate(R.layout.favourite_fragment, container, false)
    }
}
