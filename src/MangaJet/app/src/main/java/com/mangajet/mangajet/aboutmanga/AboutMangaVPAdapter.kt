package com.mangajet.mangajet.aboutmanga

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.lifecycle.Lifecycle
import com.mangajet.mangajet.aboutmanga.aboutMangaFragment.AboutMangaFragment
import com.mangajet.mangajet.aboutmanga.mangaChaptersFragment.MangaChaptersFragment
import com.mangajet.mangajet.log.Logger

// ViewPager2 adapter for About manga activity
class AboutMangaVPAdapter(fragmentManager: FragmentManager, lifeCycle : Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifeCycle) {
    // Function which will return total amount of tabs
    override fun getItemCount(): Int {
        return AboutMangaActivity.TOTAL_TABS
    }

    // Function which will construct required fragment
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            AboutMangaActivity.ABOUT_TAB_ID -> {
                Logger.log("About manga opened")
                AboutMangaFragment()
            }
            AboutMangaActivity.CHAPTERS_TAB_ID -> {
                Logger.log("Manga chapters opened")
                MangaChaptersFragment()
            }
            else -> {
                Fragment()
            }
        }
    }
}
