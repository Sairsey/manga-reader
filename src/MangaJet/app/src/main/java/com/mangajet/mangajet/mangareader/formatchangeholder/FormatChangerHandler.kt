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
class FormatChangerHandler(mangaReaderViewModel : MangaReaderViewModel) {
    // mangaReader viewModel reference with data
    private val mMangaReaderViewModel = mangaReaderViewModel
    private var isInited = false            // Flag : if was inited = true, else = false

    // reference to viewPager2 (which we will change)
    private lateinit var mMangaReaderViewPager : ViewPager2

    // adapter for 'Manhwa' reader format
    private lateinit var  adapterManhwa : MangaReaderBaseAdapter
    // adapter for 'Manga (reverse)' reader format
    private lateinit var  adapterReverse : MangaReaderBaseAdapter
    // adapter for 'Book (straight)' reader format
    private lateinit var  adapterStraight : MangaReaderBaseAdapter

    // page change listener for 'Manhwa' reader format
    private lateinit var  manhwaPageChangeListener : ViewPager2.OnPageChangeCallback
    // page change listener for 'Manga (reverse)' reader format
    private lateinit var  reversePageChangeListener : ViewPager2.OnPageChangeCallback
    // page change listener for 'Book (straight)' reader format
    private lateinit var  straightPageChangeListener : ViewPager2.OnPageChangeCallback

    // Function which will init all 'lateinit' variables
    private fun initHandler() {
        if (!isInited) {
            mMangaReaderViewPager = mMangaReaderViewModel.mangaReaderVP2

            adapterManhwa   = ManhwaReaderVPAdapter(mMangaReaderViewModel)
            adapterReverse  = ReverseReaderVPAdapter(mMangaReaderViewModel)
            adapterStraight = StraightReaderVPAdapter(mMangaReaderViewModel)

            manhwaPageChangeListener =   ManhwaPageChangeListener(mMangaReaderViewModel,
                mMangaReaderViewPager)
            reversePageChangeListener =  ReversePageChangeListener(mMangaReaderViewModel,
                mMangaReaderViewPager)
            straightPageChangeListener = StraightPageChangeListener(mMangaReaderViewModel,
                mMangaReaderViewPager)

            isInited = true
        }
    }

    // Function which will change adapter
    private fun adapterChangeHandler() {
        var newAdapter = when(mMangaReaderViewModel.currentReaderFormat) {
            MangaReaderViewModel.READER_FORMAT_BOOK -> {
                adapterStraight
            }
            MangaReaderViewModel.READER_FORMAT_MANHWA -> {
                adapterManhwa
            }
            MangaReaderViewModel.READER_FORMAT_MANGA -> {
                adapterReverse
            }
            else ->
                mMangaReaderViewPager.adapter
        }

        mMangaReaderViewPager.adapter = null
        newAdapter?.notifyDataSetChanged()
        mMangaReaderViewPager.adapter = newAdapter
        mMangaReaderViewPager.orientation = when(mMangaReaderViewModel.currentReaderFormat) {
            MangaReaderViewModel.READER_FORMAT_MANHWA -> {
                ViewPager2.ORIENTATION_VERTICAL
            }
            else ->
                ViewPager2.ORIENTATION_HORIZONTAL
        }
    }

    // Function which will change page change listener
    private fun pageChangeListenerHandler() {
        mMangaReaderViewPager.unregisterOnPageChangeCallback(straightPageChangeListener)
        mMangaReaderViewPager.unregisterOnPageChangeCallback(manhwaPageChangeListener)
        mMangaReaderViewPager.unregisterOnPageChangeCallback(reversePageChangeListener)

        when(mMangaReaderViewModel.currentReaderFormat) {
            MangaReaderViewModel.READER_FORMAT_BOOK -> {
                (straightPageChangeListener as StraightPageChangeListener).pagerAdapter =
                    mMangaReaderViewPager.adapter as MangaReaderBaseAdapter
                mMangaReaderViewPager.registerOnPageChangeCallback(straightPageChangeListener)
            }
            MangaReaderViewModel.READER_FORMAT_MANHWA -> {
                (manhwaPageChangeListener as ManhwaPageChangeListener).pagerAdapter =
                    mMangaReaderViewPager.adapter as MangaReaderBaseAdapter
                mMangaReaderViewPager.registerOnPageChangeCallback(manhwaPageChangeListener)
            }
            MangaReaderViewModel.READER_FORMAT_MANGA -> {
                (reversePageChangeListener as ReversePageChangeListener).pagerAdapter =
                    mMangaReaderViewPager.adapter as MangaReaderBaseAdapter
                mMangaReaderViewPager.registerOnPageChangeCallback(reversePageChangeListener)
            }
        }
    }

    // Function which will update viewPager2
    fun updateReaderFormat() {
        initHandler()
        adapterChangeHandler()
        pageChangeListenerHandler()
    }

    // Function which will notify some adapters for 'doToPrevChapter'
    fun notifyAdaptersForPrevChapter() {
        initHandler()
        (adapterManhwa as ManhwaReaderVPAdapter).wasPrevReload = true
    }
}
