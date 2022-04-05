package com.mangajet.mangajet.mangareader

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import androidx.viewpager2.widget.ViewPager2
import com.mangajet.mangajet.MangaJetApp.Companion.context
import com.mangajet.mangajet.R


// Class which represents "Manga Reader" Activity
class MangaReaderActivity : AppCompatActivity() {
    companion object {
        const val SWAP_RIGHT_BORDER = 0.95
        const val SWAP_LEFT_BORDER  = 0.2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manga_reader_activity)

        val mangaReaderViewmodel = ViewModelProvider(this).get(MangaReaderViewModel::class.java)

        mangaReaderViewmodel.initMangaData()

        val viewPager = findViewById<ViewPager2>(R.id.mangaViewPager)
        val pagerAdapter = MangaReaderVPAdapter(mangaReaderViewmodel)
        viewPager.adapter = pagerAdapter

        var delta = 1
        if (mangaReaderViewmodel.manga.lastViewedChapter == 0)
            delta = 0

        viewPager.setCurrentItem(mangaReaderViewmodel.manga
            .chapters[mangaReaderViewmodel.manga.lastViewedChapter]
            .lastViewedPage + delta, false)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            private fun doToPrevChapter() {
                // update chapter
                mangaReaderViewmodel.manga.lastViewedChapter--;

                // update pages count (and load chapter)
                mangaReaderViewmodel.pagesCount =
                    mangaReaderViewmodel.manga
                        .chapters[mangaReaderViewmodel.manga.lastViewedChapter].getPagesNum()

                // set correct page
                mangaReaderViewmodel.manga
                    .chapters[mangaReaderViewmodel.manga.lastViewedChapter]
                    .lastViewedPage = mangaReaderViewmodel.pagesCount - 1

                // save manga state
                mangaReaderViewmodel.manga.saveToFile()

                // start loading all pages
                mangaReaderViewmodel.uploadPages()

                // update adapter
                viewPager.adapter = null
                pagerAdapter.notifyDataSetChanged()
                viewPager.adapter = pagerAdapter

                // determine delta
                var delta = 0
                if (mangaReaderViewmodel.manga.lastViewedChapter == 0)
                    delta = -1

                // go to right page without animation
                viewPager.setCurrentItem(mangaReaderViewmodel.pagesCount + delta, false)
            }

            private fun doToNextChapter() {
                // update chapter
                mangaReaderViewmodel.manga.lastViewedChapter++;

                // update pages count (and load chapter)
                mangaReaderViewmodel.pagesCount =
                    mangaReaderViewmodel.manga
                        .chapters[mangaReaderViewmodel.manga.lastViewedChapter].getPagesNum()

                // set correct page
                mangaReaderViewmodel.manga
                    .chapters[mangaReaderViewmodel.manga.lastViewedChapter]
                    .lastViewedPage = 0

                // save manga state
                mangaReaderViewmodel.manga.saveToFile()

                // start loading all pages
                mangaReaderViewmodel.uploadPages()

                // update adapter
                viewPager.adapter = null
                pagerAdapter.notifyDataSetChanged()
                viewPager.adapter = pagerAdapter

                // go to right page without animation
                viewPager.setCurrentItem(1, false)
            }


            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mangaReaderViewmodel.manga
                    .chapters[mangaReaderViewmodel.manga.lastViewedChapter]
                    .lastViewedPage = position

                //Toast.makeText(context, "onPageSelected", Toast.LENGTH_SHORT).show()

                // SPECIAL CASES
                // only one chapter
                if (mangaReaderViewmodel.manga.chapters.size == 1)
                    return // do nothing

                // First chapter
                else if (mangaReaderViewmodel.manga.lastViewedChapter == 0) {
                    if (position == pagerAdapter.itemCount - 1) {
                        doToNextChapter()
                    }
                }
                // Last chapter
                else if (mangaReaderViewmodel.manga.lastViewedChapter == mangaReaderViewmodel.manga.chapters.size - 1) {
                    if (position == 0)
                        doToPrevChapter()
                }
                // Other cases
                else {
                    if (position == 0)
                        doToPrevChapter()
                    else if (position == pagerAdapter.itemCount - 1)
                        doToNextChapter()
                }
            }
        })
    }
}
