package com.mangajet.mangajet.mangareader

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.mangajet.mangajet.MangaJetApp.Companion.context
import com.mangajet.mangajet.R
import com.mangajet.mangajet.mangareader.formatchangeholder.MangaReaderBaseAdapter


// Class which represents "Manga Reader" Activity
@Suppress("TooManyFunctions")
class MangaReaderActivity : AppCompatActivity() {
    // Manga reader activity ViewModel variable
    lateinit var mangaReaderViewModel : MangaReaderViewModel
    // handler, which will provide behavior with toolbars
    lateinit var toolbarHandler : MangaReaderToolbarHandler
    // handler, which will provide behavior with menu on toolbars
    lateinit var menuHandler : MangaReaderMenuHandler

    // Function which will set title in Action bar
    fun setTitle () {
        var title = if (mangaReaderViewModel.manga.originalName != ""
            && mangaReaderViewModel.manga.originalName != null)
            mangaReaderViewModel.manga.originalName
        else
            mangaReaderViewModel.manga.russianName
        supportActionBar?.title = title
    }

    // Function which will get display width in pixels
    private fun getScreenWidth(): Float {
        val metrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(metrics)
        return metrics.widthPixels.toFloat()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.manga_reader_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reloadPage -> menuHandler.reloadCurrentPage()
            R.id.changeFormat -> menuHandler.callChangeFormatDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manga_reader_activity)

        setSupportActionBar(findViewById<MaterialToolbar>(R.id.headerToolbar))

        // init viewmodel, reader format and viewpager
        mangaReaderViewModel = ViewModelProvider(this)[MangaReaderViewModel::class.java]
        mangaReaderViewModel.displayWidth = getScreenWidth()
        val viewPager = findViewById<ViewPager2>(R.id.mangaViewPager)
        mangaReaderViewModel.mangaReaderVP2 = viewPager
        mangaReaderViewModel.navTextView = findViewById(R.id.currentPageText)
        mangaReaderViewModel.initMangaData()
        mangaReaderViewModel.setPageTitle()
        setTitle()

        // init start position
        var delta = if (mangaReaderViewModel.isOnFirstChapter()) 0 else 1
        viewPager.setCurrentItem(mangaReaderViewModel.manga
            .chapters[mangaReaderViewModel.manga.lastViewedChapter]
            .lastViewedPage + delta, false)

        // init toolbar handler
        val headerToolbar = findViewById<MaterialToolbar>(R.id.headerToolbar)
        val bottomToolbar = findViewById<MaterialToolbar>(R.id.bottomToolbar)
        toolbarHandler = MangaReaderToolbarHandler(headerToolbar, bottomToolbar)

        // init menu handler
        menuHandler = MangaReaderMenuHandler(mangaReaderViewModel, viewPager,
            supportFragmentManager)

        // init toolbars buttons
        val pagerAdapter = viewPager.adapter as MangaReaderBaseAdapter
        val prevChapterButton = findViewById<ImageButton>(R.id.prevChapter)
        val nextChapterButton = findViewById<ImageButton>(R.id.nextChapter)

        prevChapterButton.setOnClickListener {
            if (!mangaReaderViewModel.isOnFirstChapter()) {
                mangaReaderViewModel.doToPrevChapter(viewPager, pagerAdapter)
                val startPage = if (mangaReaderViewModel.isOnFirstChapter()) 0 else 1
                viewPager.setCurrentItem(startPage, false)
                mangaReaderViewModel.setPageTitle()
            }
        }

        nextChapterButton.setOnClickListener {
            if (!mangaReaderViewModel.isOnLastChapter()) {
                mangaReaderViewModel.doToNextChapter(viewPager, pagerAdapter)
                mangaReaderViewModel.setPageTitle()
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_UP ->
                toolbarHandler.touchEventDispatcher(event)

        }
        return super.dispatchTouchEvent(event)
    }
}
