package com.mangajet.mangajet.mangareader.formatchangeholder

import androidx.viewpager2.widget.ViewPager2
import com.mangajet.mangajet.mangareader.MangaReaderViewModel
import com.mangajet.mangajet.mangareader.manhwa.ManhwaPageChangeListener
import com.mangajet.mangajet.mangareader.manhwa.ManhwaReaderVPAdapter
import com.mangajet.mangajet.mangareader.reverse.ReversePageChangeListener
import com.mangajet.mangajet.mangareader.reverse.ReverseReaderVPAdapter
import com.mangajet.mangajet.mangareader.straight.StraightPageChangeListener
import com.mangajet.mangajet.mangareader.straight.StraightReaderVPAdapter

// Class which will handle changes in viewPager2 when changes reader format
class FormatChangerHandler(
    // mangaReader viewModel reference with data
    private val mangaReaderViewModel: MangaReaderViewModel
) {
    // reference to viewPager2 (which we will change)
    private lateinit var mangaReaderViewPager : ViewPager2

    // adapter for current reader format
    private lateinit var  currentAdapter : MangaReaderBaseAdapter

    // page change listener for current reader format
    private var currentPageChangeListener : ViewPager2.OnPageChangeCallback? = null

    // Function which will init all 'lateinit' variables
    private fun initHandler() {
        mangaReaderViewPager = mangaReaderViewModel.mangaReaderVP2

        if (currentPageChangeListener != null)
            mangaReaderViewPager.unregisterOnPageChangeCallback(currentPageChangeListener!!)

        when (mangaReaderViewModel.currentReaderFormat) {
            MangaReaderViewModel.READER_FORMAT_BOOK -> {
                currentAdapter = StraightReaderVPAdapter(mangaReaderViewModel)
                currentPageChangeListener = StraightPageChangeListener(mangaReaderViewModel,
                    mangaReaderViewPager)
            }
            MangaReaderViewModel.READER_FORMAT_MANGA -> {
                currentAdapter = ReverseReaderVPAdapter(mangaReaderViewModel)
                currentPageChangeListener = ReversePageChangeListener(mangaReaderViewModel,
                    mangaReaderViewPager)
            }
            MangaReaderViewModel.READER_FORMAT_MANHWA -> {
                currentAdapter = ManhwaReaderVPAdapter(mangaReaderViewModel)
                currentPageChangeListener = ManhwaPageChangeListener(mangaReaderViewModel,
                    mangaReaderViewPager)
            }
        }
    }

    // Function which will change adapter
    private fun adapterChangeHandler() {
        currentAdapter?.notifyDataSetChanged()
        mangaReaderViewPager.adapter = currentAdapter
        mangaReaderViewPager.orientation = when(mangaReaderViewModel.currentReaderFormat) {
            MangaReaderViewModel.READER_FORMAT_MANHWA -> {
                ViewPager2.ORIENTATION_VERTICAL
            }
            else ->
                ViewPager2.ORIENTATION_HORIZONTAL
        }

        when(mangaReaderViewModel.currentReaderFormat) {
            MangaReaderViewModel.READER_FORMAT_BOOK -> {
                (currentPageChangeListener as StraightPageChangeListener).pagerAdapter =
                    mangaReaderViewPager.adapter as MangaReaderBaseAdapter
            }
            MangaReaderViewModel.READER_FORMAT_MANHWA -> {
                (currentPageChangeListener as ManhwaPageChangeListener).pagerAdapter =
                    mangaReaderViewPager.adapter as MangaReaderBaseAdapter
            }
            MangaReaderViewModel.READER_FORMAT_MANGA -> {
                (currentPageChangeListener as ReversePageChangeListener).pagerAdapter =
                    mangaReaderViewPager.adapter as MangaReaderBaseAdapter
            }
        }

        mangaReaderViewPager.registerOnPageChangeCallback(currentPageChangeListener!!)
    }

    // Function which will update viewPager2
    fun updateReaderFormat() {
        initHandler()
        adapterChangeHandler()
    }
}
