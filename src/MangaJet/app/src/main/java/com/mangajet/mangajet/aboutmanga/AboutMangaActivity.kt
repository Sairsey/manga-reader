package com.mangajet.mangajet.aboutmanga

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.Manga

// Class which represents "About Manga" Activity
class AboutMangaActivity : AppCompatActivity() {
    companion object {
        const val PADDING_VERT = 5      // Vert padding tag value
        const val PADDING_HORZ = 30     // Horz padding tag value
        const val ABOUT_TAB_ID = 0      // "About" tab button id
        const val CHAPTERS_TAB_ID = 1   // "Chapters" tab button id
        const val TOTAL_TABS = 2        // Total tabs count
    }

    // about manga view model
    lateinit var aboutMangaViewmodel : AboutMangaViewModel

    // In methods 'onCreate' we only init data in viewport. All other actions -> in onStart() or onResume() overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_manga_activity)
        setSupportActionBar(findViewById<MaterialToolbar>(R.id.aboutMangaToolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Call viewport to manage low speed downloading data in dat class
        aboutMangaViewmodel = ViewModelProvider(this)[AboutMangaViewModel::class.java]
        aboutMangaViewmodel.manga = MangaJetApp.currentManga!!
        supportActionBar?.title = aboutMangaViewmodel.manga.originalName

        aboutMangaViewmodel.progressIndicator = findViewById(R.id.progressBar)
        aboutMangaViewmodel.initMangaData()

        val tabLayout = findViewById<TabLayout>(R.id.aboutMangaTabs)
        val viewPager2 = findViewById<ViewPager2>(R.id.fragmentContainerView)
        val adapter = AboutMangaVPAdapter(supportFragmentManager, lifecycle)

        viewPager2.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager2) {tab, position ->
            when(position) {
                ABOUT_TAB_ID -> {
                    tab.text = getString(R.string.fragment_title_about)
                }
                CHAPTERS_TAB_ID -> {
                    tab.text = getString(R.string.fragment_title_chapters)
                }
                else -> {
                    tab.text = "None."
                }
            }
        }.attach()

        supportFragmentManager.setFragmentResultListener("TAG_TAPPED", this ) { requestKey, bundle ->
            val tag = bundle.getString("tag")
            MangaJetApp.OverrideFragmentInMainActivity.FragmentSearch.needToBeOpened = true
            MangaJetApp.tagSearchInfo = Pair(tag!!, aboutMangaViewmodel.manga.library.getURL())
            setResult(RESULT_OK)
            finish()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return true
    }
}
