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
        viewPager.setCurrentItem(mangaReaderViewmodel.manga
                 .chapters[mangaReaderViewmodel.manga.lastViewedChapter]
                 .lastViewedPage + 1, false)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mangaReaderViewmodel.manga
                    .chapters[mangaReaderViewmodel.manga.lastViewedChapter]
                    .lastViewedPage = position
                Toast.makeText(context, "onPageSelected", Toast.LENGTH_SHORT).show()
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                // prev chapter
                if (position == 0
                    && positionOffset > SWAP_RIGHT_BORDER
                    && mangaReaderViewmodel.manga.lastViewedChapter > 0)
                {
                    mangaReaderViewmodel.manga.lastViewedChapter--;

                    mangaReaderViewmodel.pagesCount =
                        mangaReaderViewmodel.manga
                            .chapters[mangaReaderViewmodel.manga.lastViewedChapter].getPagesNum()

                    mangaReaderViewmodel.manga
                        .chapters[mangaReaderViewmodel.manga.lastViewedChapter].lastViewedPage =
                        mangaReaderViewmodel.pagesCount - 1

                    mangaReaderViewmodel.uploadPages()
                    pagerAdapter.notifyDataSetChanged()
                    viewPager.setCurrentItem(mangaReaderViewmodel.pagesCount + 1, false)

                }

                // next chapter
                if (position == mangaReaderViewmodel.pagesCount
                    && positionOffset < SWAP_LEFT_BORDER
                    && mangaReaderViewmodel.manga.lastViewedChapter <
                       mangaReaderViewmodel.manga.chapters.size - 1)
                {
                    mangaReaderViewmodel.manga.lastViewedChapter++

                    mangaReaderViewmodel.pagesCount =
                        mangaReaderViewmodel.manga
                            .chapters[mangaReaderViewmodel.manga.lastViewedChapter].getPagesNum()

                    mangaReaderViewmodel.manga
                        .chapters[mangaReaderViewmodel.manga.lastViewedChapter].lastViewedPage = 0

                    mangaReaderViewmodel.uploadPages()
                    pagerAdapter.notifyDataSetChanged()
                    viewPager.setCurrentItem(1, false)
                }

                // if we go to end or begin -> block scroll
            }
        })
    }
}
