package com.mangajet.mangajet.aboutmanga

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.lifecycle.Lifecycle
import com.mangajet.mangajet.aboutmanga.aboutMangaFragment.AboutMangaFragment
import com.mangajet.mangajet.aboutmanga.mangaChaptersFragment.MangaChaptersFragment

class AboutMangaVPAdapter(fragmentManager: FragmentManager, lifeCycle : Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifeCycle) {
    override fun getItemCount(): Int {
        return 1 + 1
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                AboutMangaFragment()
            }

            1 -> {
                MangaChaptersFragment()
            }
            else -> {
                Fragment()
            }
        }
    }
}
