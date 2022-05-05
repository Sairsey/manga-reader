package com.mangajet.mangajet.ui.favourite

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.R
import com.mangajet.mangajet.log.Logger
import com.mangajet.mangajet.ui.history.HistoryFragment

// Class which represents "Updates" fragment of MainActivity
class FavouriteFragment : Fragment() {

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            MangaJetApp.SEARCH_BY_TAG -> {
                if (data == null) {
                    super.onActivityResult(requestCode, resultCode, data)
                    return
                }
                // send info for search

                // collect info for search
                val tag = data!!.getCharSequenceExtra("tag").toString()
                val src = data!!.getCharSequenceExtra("src").toString()

                MangaJetApp.isNeedToTagSearch = true
                MangaJetApp.tagSearchInfo = Pair(tag, src)
                val navigationBar = activity?.findViewById<BottomNavigationView>(R.id.nav_view)
                val view: View = navigationBar!!.findViewById(R.id.navigation_search)
                view.performClick()
            }
            else ->
                super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.log("Favourite opened")
        return inflater.inflate(R.layout.favourite_fragment, container, false)
    }
}
