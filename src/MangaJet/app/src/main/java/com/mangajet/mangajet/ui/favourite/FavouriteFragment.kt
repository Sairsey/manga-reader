package com.mangajet.mangajet.ui.favourite

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.R
import com.mangajet.mangajet.log.Logger

// Class which represents "Updates" fragment of MainActivity
class FavouriteFragment : Fragment() {

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            MangaJetApp.ABOUT_MANGA_CALLBACK -> {
                if (MangaJetApp.OverrideFragmentInMainActivity.FragmentSearch.needToBeOpened) {
                    val navigationBar = activity?.findViewById<BottomNavigationView>(R.id.nav_view)
                    val view: View = navigationBar!!.findViewById(
                        MangaJetApp.OverrideFragmentInMainActivity.FragmentSearch.mainFragmentId
                    )
                    view.performClick()
                }
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
