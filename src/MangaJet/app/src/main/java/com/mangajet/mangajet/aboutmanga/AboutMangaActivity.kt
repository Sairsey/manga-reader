package com.mangajet.mangajet.aboutmanga

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mangajet.mangajet.R

// Class which represents "About Manga" Activity
class AboutMangaActivity : AppCompatActivity() {
    companion object {
        const val PADDING_VERT = 5      // Vert padding tag value
        const val PADDING_HORZ = 30      // Horz padding tag value
    }

    // In methods 'onCreate' we only init data in viewport. All other actions -> in onStart() or onResume() overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_manga_second)

        // Call viewport to manage low speed downloading data in dat class
        val aboutMangaViewmodel = ViewModelProvider(this)[AboutMangaViewModel::class.java]
        aboutMangaViewmodel.initMangaData(intent)
        setTitle(aboutMangaViewmodel.manga.originalName)

        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        val viewPager2 = findViewById<ViewPager2>(R.id.fragmentContainerView)
        val adapter = AboutMangaVPAdapter(supportFragmentManager, lifecycle)

        viewPager2.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager2) {tab, position ->
            when(position) {
                0 -> {
                    tab.text = getString(R.string.fragment_title_about)
                }
                1 -> {
                    tab.text = getString(R.string.fragment_title_chapters)
                }
                else -> {
                tab.text = "PUPAAA!"
                }
            }
        }.attach()
    }
}
