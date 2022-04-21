package com.mangajet.mangajet.mangareader.formatchangeholder

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.mangajet.mangajet.mangareader.MangaReaderViewModel
import com.mangajet.mangajet.mangareader.manhwa.ManhwaPageChangeListener
import com.mangajet.mangajet.mangareader.manhwa.ManhwaReaderVPAdapter
import com.mangajet.mangajet.mangareader.reverse.ReversePageChangeListener
import com.mangajet.mangajet.mangareader.reverse.ReverseReaderVPAdapter
import com.mangajet.mangajet.mangareader.straight.StraightPageChangeListener
import com.mangajet.mangajet.mangareader.straight.StraightReaderVPAdapter

class FormatChangerHandler(mangaReaderViewModel : MangaReaderViewModel) {
    private val mMangaReaderViewModel = mangaReaderViewModel
    private var isInited = false

    private lateinit var mMangaReaderViewPager : ViewPager2

    private lateinit var  adapterManhwa : MangaReaderBaseAdapter
    private lateinit var  adapterReverse : MangaReaderBaseAdapter
    private lateinit var  adapterStraight : MangaReaderBaseAdapter

    private lateinit var  manhwaPageChangeListener : ViewPager2.OnPageChangeCallback
    private lateinit var  reversePageChangeListener : ViewPager2.OnPageChangeCallback
    private lateinit var  straightPageChangeListener : ViewPager2.OnPageChangeCallback

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

    fun updateReaderFormat() {
        initHandler()
        adapterChangeHandler()
        pageChangeListenerHandler()
    }
}
