package com.mangajet.mangajet.mangareader

import android.widget.ImageButton
import android.widget.SeekBar
import androidx.viewpager2.widget.ViewPager2
import com.mangajet.mangajet.mangareader.formatchangeholder.MangaReaderBaseAdapter

class MangaReaderNavPanelHandler(
    private val mangaReaderViewModel : MangaReaderViewModel,
    private val navigationPanel : androidx.appcompat.widget.LinearLayoutCompat,
    private val nextChapterButton : ImageButton,
    private val prevChapterButton : ImageButton,
    private val seekBar : SeekBar
) {
    companion object {
        const val NAVIGATION_PART = 3F / 4F
    }

    // lock seekbar callbacks if we changed seekbar progress by scrolling pages
    private var someKindOfMutexToLockCallback = false

    private fun goToNextChapter(viewPager: ViewPager2, pagerAdapter : MangaReaderBaseAdapter) {
        if (!mangaReaderViewModel.isOnLastChapter()) {
            mangaReaderViewModel.doToNextChapter(viewPager, pagerAdapter)
            seekBar.max = mangaReaderViewModel.pagesCount - 1
            seekBar.progress = 0

            if (mangaReaderViewModel.currentReaderFormat == MangaReaderViewModel.READER_FORMAT_MANGA)
                seekBar.progress = seekBar.max

            mangaReaderViewModel.setPageTitle()
        }
    }

    private fun goToPrevChapter(viewPager: ViewPager2, pagerAdapter : MangaReaderBaseAdapter) {
        if (!mangaReaderViewModel.isOnFirstChapter()) {
            mangaReaderViewModel.doToPrevChapter(viewPager, pagerAdapter)
            var startPage = if (mangaReaderViewModel.isOnFirstChapter()) 0 else 1
            seekBar.max = mangaReaderViewModel.pagesCount - 1
            seekBar.progress = 0

            if (mangaReaderViewModel.currentReaderFormat == MangaReaderViewModel.READER_FORMAT_MANGA) {
                startPage = viewPager.adapter!!.itemCount - startPage - 1
                seekBar.progress = seekBar.max
            }

            viewPager.setCurrentItem(startPage, false)
            mangaReaderViewModel.setPageTitle()
        }
    }

    fun updateSeekBar() {
        someKindOfMutexToLockCallback = true
        seekBar.max = mangaReaderViewModel.pagesCount - 1

        seekBar.progress = if (mangaReaderViewModel.currentReaderFormat != MangaReaderViewModel.READER_FORMAT_MANGA)
            mangaReaderViewModel.manga.
            chapters[mangaReaderViewModel.manga.lastViewedChapter].lastViewedPage
        else
            mangaReaderViewModel.pagesCount - mangaReaderViewModel.manga.
            chapters[mangaReaderViewModel.manga.lastViewedChapter].lastViewedPage - 1
        someKindOfMutexToLockCallback = false
    }

    fun initialize() {
        val viewPager = mangaReaderViewModel.mangaReaderVP2

        // bind prev button
        prevChapterButton.setOnClickListener {
            val pagerAdapter = viewPager.adapter as MangaReaderBaseAdapter
            if (mangaReaderViewModel.currentReaderFormat != MangaReaderViewModel.READER_FORMAT_MANGA) {
                goToPrevChapter(viewPager, pagerAdapter)
            }
            else {
                goToNextChapter(viewPager, pagerAdapter)
            }
        }

        // bind next button
        nextChapterButton.setOnClickListener {
            val pagerAdapter = viewPager.adapter as MangaReaderBaseAdapter
            if (mangaReaderViewModel.currentReaderFormat != MangaReaderViewModel.READER_FORMAT_MANGA) {
                goToNextChapter(viewPager, pagerAdapter)
            }
            else {
                goToPrevChapter(viewPager, pagerAdapter)
            }
        }

        // init navigation panel bar size
        val layoutParams = navigationPanel.layoutParams
        layoutParams.width = (mangaReaderViewModel.displayWidth * NAVIGATION_PART).toInt()
        navigationPanel.layoutParams = layoutParams

        // init seek bar
        seekBar.max = mangaReaderViewModel.pagesCount - 1
        seekBar.progress = mangaReaderViewModel.manga.
            chapters[mangaReaderViewModel.manga.lastViewedChapter].lastViewedPage - 1
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            private fun straightProgressChanged(p1: Int) {
                val delta = if (!mangaReaderViewModel.isOnFirstChapter())
                    1
                else
                    0
                mangaReaderViewModel.mangaReaderVP2.setCurrentItem(p1 + delta, true)
            }

            private fun reverseProgressChanged(p1: Int) {
                val delta = if (!mangaReaderViewModel.isOnLastChapter())
                    1
                else
                    0
                val pageToScroll = mangaReaderViewModel.pagesCount + delta - p1
                mangaReaderViewModel.mangaReaderVP2.setCurrentItem(pageToScroll, true)
            }

            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (!someKindOfMutexToLockCallback) {
                    if (mangaReaderViewModel.currentReaderFormat != MangaReaderViewModel.READER_FORMAT_MANGA)
                        straightProgressChanged(p1)
                    else
                        reverseProgressChanged(p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                p0.hashCode()
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                p0.hashCode()
            }
        })
    }
}
