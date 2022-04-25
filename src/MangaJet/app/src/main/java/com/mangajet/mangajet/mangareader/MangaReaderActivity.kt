package com.mangajet.mangajet.mangareader

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.mangajet.mangajet.R
import com.mangajet.mangajet.mangareader.formatchangeholder.MangaReaderBaseAdapter
import com.mangajet.mangajet.log.Logger

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

    fun initialize() {
        // if we have some troubles
        val viewPager = findViewById<ViewPager2>(R.id.mangaViewPager)
        if (mangaReaderViewModel.pagesCount == 0)
        {
            val builder = AlertDialog.Builder(this)
            builder
                .setTitle("Error")
                .setMessage("We cannot access to pages of this chapter." +
                        "This could happen if you are not authorized or chapter is invalid.")
                .setPositiveButton("Return"
                ) { dialog, id ->
                    finish()
                }
            val dialog = builder.create()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            return
        }

        mangaReaderViewModel.setPageTitle()
        setTitle()

        // init start position
        var delta = if (mangaReaderViewModel.isOnFirstChapter()) 0 else 1
        viewPager.setCurrentItem(mangaReaderViewModel.manga
            .chapters[mangaReaderViewModel.manga.lastViewedChapter]
            .lastViewedPage + delta, false)
        Logger.log("Chapter " + (mangaReaderViewModel.manga.lastViewedChapter + 1).toString() + " opened")

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
        findViewById<CircularProgressIndicator>(R.id.loadIndicator2).hide()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.log("Manga Reader activity started")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manga_reader_activity)

        setSupportActionBar(findViewById<MaterialToolbar>(R.id.headerToolbar))

        // init viewmodel, reader format and viewpager
        mangaReaderViewModel = ViewModelProvider(this)[MangaReaderViewModel::class.java]
        mangaReaderViewModel.displayWidth = getScreenWidth()
        val viewPager = findViewById<ViewPager2>(R.id.mangaViewPager)
        mangaReaderViewModel.mangaReaderVP2 = viewPager
        mangaReaderViewModel.navTextView = findViewById(R.id.currentPageText)
        mangaReaderViewModel.activity = this
        findViewById<CircularProgressIndicator>(R.id.loadIndicator2).show()
        mangaReaderViewModel.initMangaData()
        viewPager.setCurrentItem(1, false)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (mangaReaderViewModel.isInited) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_UP ->
                    toolbarHandler.touchEventDispatcher(event)

            }
            return super.dispatchTouchEvent(event)
        }
        return false
    }
}
