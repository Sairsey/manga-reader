package com.mangajet.mangajet.mangareader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.mangajet.mangajet.R


// Class which represents "Manga Reader" Activity
class MangaReaderActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manga_reader_activity)

        val mangaReaderViewModel = ViewModelProvider(this).get(MangaReaderViewModel::class.java)

        mangaReaderViewModel.initMangaData()

        val viewPager = findViewById<ViewPager2>(R.id.mangaViewPager)
        val pagerAdapter = MangaReaderVPAdapter(mangaReaderViewModel)
        viewPager.adapter = pagerAdapter

        var delta = if (mangaReaderViewModel.isOnFirstChapter()) 0 else 1
        viewPager.setCurrentItem(mangaReaderViewModel.manga
            .chapters[mangaReaderViewModel.manga.lastViewedChapter]
            .lastViewedPage + delta, false)


        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            // Function which will load previous chapter after scroll
            private fun doToPrevChapter() {
                // update chapter
                mangaReaderViewModel.manga.lastViewedChapter--;

                // update pages count (and load chapter)
                mangaReaderViewModel.pagesCount =
                    mangaReaderViewModel.manga
                        .chapters[mangaReaderViewModel.manga.lastViewedChapter].getPagesNum()

                // set correct page
                mangaReaderViewModel.manga
                    .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                    .lastViewedPage = mangaReaderViewModel.pagesCount - 1

                // save manga state
                mangaReaderViewModel.manga.saveToFile()

                // start loading all pages
                mangaReaderViewModel.uploadPages()

                // update adapter
                viewPager.adapter = null
                pagerAdapter.notifyDataSetChanged()
                viewPager.adapter = pagerAdapter

                // determine delta
                var delta = 0
                if (mangaReaderViewModel.manga.lastViewedChapter == 0)
                    delta = -1

                // go to right page without animation
                viewPager.setCurrentItem(mangaReaderViewModel.pagesCount + delta, false)
            }

            // Function which will load next chapter after scroll
            private fun doToNextChapter() {
                // update chapter
                mangaReaderViewModel.manga.lastViewedChapter++;

                // update pages count (and load chapter)
                mangaReaderViewModel.pagesCount =
                    mangaReaderViewModel.manga
                        .chapters[mangaReaderViewModel.manga.lastViewedChapter].getPagesNum()

                // set correct page
                mangaReaderViewModel.manga
                    .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                    .lastViewedPage = 0

                // save manga state
                mangaReaderViewModel.manga.saveToFile()

                // start loading all pages
                mangaReaderViewModel.uploadPages()

                // update adapter
                viewPager.adapter = null
                pagerAdapter.notifyDataSetChanged()
                viewPager.adapter = pagerAdapter

                // go to right page without animation
                viewPager.setCurrentItem(1, false)
            }

            // Function which will be tried to load prev or next chapter
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mangaReaderViewModel.manga
                    .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                    .lastViewedPage = position

                // SPECIAL CASES
                // only one chapter
                if (mangaReaderViewModel.isSingleChapterManga())
                    return // do nothing
                // First chapter
                else if (mangaReaderViewModel.isOnFirstChapter()) {
                    if (position == pagerAdapter.itemCount - 1) {
                        doToNextChapter()
                    }
                }
                // Last chapter
                else if (mangaReaderViewModel.manga.lastViewedChapter == mangaReaderViewModel.manga.chapters.size - 1) {
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
