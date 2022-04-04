package com.mangajet.mangajet.mangareader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.mangajet.mangajet.R


// Class which represents "Manga Reader" Activity
class MangaReaderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manga_reader_activity)

        val mangaReaderViewmodel = ViewModelProvider(this).get(MangaReaderViewModel::class.java)

        mangaReaderViewmodel.initMangaData()

        var detektFuckUper = ""
        val pagerAdapter = MangaReaderVPAdapter(mangaReaderViewmodel)
        val viewPager = findViewById<ViewPager>(R.id.mangaViewPager)
        viewPager.adapter = pagerAdapter
        viewPager.currentItem = mangaReaderViewmodel.getStartedPosition()

        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (position > mangaReaderViewmodel.maxPos) {
                    mangaReaderViewmodel.saveLastViewedData()
                    mangaReaderViewmodel.maxPos = position
                }
            }

            override fun onPageSelected(position: Int) {
                detektFuckUper += ""
            }

            override fun onPageScrollStateChanged(state: Int) {
                detektFuckUper += ""
            }
        })
    }
}
