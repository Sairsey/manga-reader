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


// Class which represents "Manga Reader" Activity
@Suppress("TooManyFunctions")
class MangaReaderActivity : AppCompatActivity() {
    // Manga reader activity ViewModel variable
    lateinit var mangaReaderViewModel : MangaReaderViewModel
    // handler, which will provide behavior with toolbars
    lateinit var toolbarHandler : MangaReaderToolbarHandler
    // handler, which will provide behavior with menu on toolbars
    lateinit var menuHandler : MangaReaderMenuHandler

    // Function which will set activity title by current opened page
    fun setPageTitle() {
        var chapter = mangaReaderViewModel.manga.lastViewedChapter
        val page = mangaReaderViewModel.manga.chapters[chapter].lastViewedPage + 1
        val totalPages = mangaReaderViewModel.pagesCount
        val navTextView = findViewById<TextView>(R.id.currentPageText)

        // increase for correct output
        chapter++
        navTextView.text = "Chapter $chapter, page $page/$totalPages"
    }

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

        // init viewmodel and some params
        mangaReaderViewModel = ViewModelProvider(this).get(MangaReaderViewModel::class.java)
        mangaReaderViewModel.displayWidth = getScreenWidth()
        mangaReaderViewModel.initMangaData()
        setTitle()
        setPageTitle()

        // init toolbar handler
        val headerToolbar = findViewById<MaterialToolbar>(R.id.headerToolbar)
        val bottomToolbar = findViewById<MaterialToolbar>(R.id.bottomToolbar)
        toolbarHandler = MangaReaderToolbarHandler(headerToolbar, bottomToolbar)

        // init viewpager
        val viewPager = findViewById<ViewPager2>(R.id.mangaViewPager)
        mangaReaderViewModel.mangaReaderVP2 = viewPager
        val pagerAdapter = MangaReaderVPAdapter(mangaReaderViewModel)
        viewPager.adapter = pagerAdapter

        // init menu handler
        menuHandler = MangaReaderMenuHandler(mangaReaderViewModel, viewPager,
            supportFragmentManager)

        var delta = if (mangaReaderViewModel.isOnFirstChapter()) 0 else 1
        viewPager.setCurrentItem(mangaReaderViewModel.manga
            .chapters[mangaReaderViewModel.manga.lastViewedChapter]
            .lastViewedPage + delta, false)

        val prevChapterButton = findViewById<ImageButton>(R.id.prevChapter)
        prevChapterButton.setOnClickListener {
            if (!mangaReaderViewModel.isOnFirstChapter()) {
                mangaReaderViewModel.doToPrevChapter(viewPager, pagerAdapter)
                val startPage = if (mangaReaderViewModel.isOnFirstChapter()) 0 else 1
                viewPager.setCurrentItem(startPage, false)
                setPageTitle()
            }
        }

        val nextChapterButton = findViewById<ImageButton>(R.id.nextChapter)
        nextChapterButton.setOnClickListener {
            if (!mangaReaderViewModel.isOnLastChapter()) {
                mangaReaderViewModel.doToNextChapter(viewPager, pagerAdapter)
                setPageTitle()
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            private fun onPageSelectedInMangaFormat(position : Int) {
                mangaReaderViewModel.manga
                        .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                        .lastViewedPage = mangaReaderViewModel.manga
                    .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                    .getPagesNum() - position
                if (mangaReaderViewModel.isOnLastChapter())
                    mangaReaderViewModel.manga
                        .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                        .lastViewedPage -= 1


                // SPECIAL CASES
                // only one chapter
                if (mangaReaderViewModel.isSingleChapterManga()) {
                    // do nothing
                }
                // First chapter
                else if (mangaReaderViewModel.isOnFirstChapter()) {
                    if (position == 0)
                        mangaReaderViewModel.doToNextChapter(viewPager, pagerAdapter)
                }
                // Last chapter
                else if (mangaReaderViewModel.manga.lastViewedChapter == mangaReaderViewModel.manga.chapters.size - 1) {
                    if (position == pagerAdapter.itemCount - 1)
                        mangaReaderViewModel.doToPrevChapter(viewPager, pagerAdapter)
                }
                // Other cases
                else {
                    if (position == pagerAdapter.itemCount - 1)
                        mangaReaderViewModel.doToPrevChapter(viewPager, pagerAdapter)
                    else if (position == 0)
                        mangaReaderViewModel.doToNextChapter(viewPager, pagerAdapter)
                }
            }

            private fun onPageSelectedInBookFormat(position : Int) {
                mangaReaderViewModel.manga
                        .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                        .lastViewedPage = if (mangaReaderViewModel.isOnFirstChapter()) position
                    else position - 1

                // SPECIAL CASES
                // only one chapter
                if (mangaReaderViewModel.isSingleChapterManga()) {
                    // do nothing
                }
                // First chapter
                else if (mangaReaderViewModel.isOnFirstChapter()) {
                    if (position == pagerAdapter.itemCount - 1)
                        mangaReaderViewModel.doToNextChapter(viewPager, pagerAdapter)
                }
                // Last chapter
                else if (mangaReaderViewModel.manga.lastViewedChapter == mangaReaderViewModel.manga.chapters.size - 1) {
                    if (position == 0)
                        mangaReaderViewModel.doToPrevChapter(viewPager, pagerAdapter)
                }
                // Other cases
                else {
                    if (position == 0)
                        mangaReaderViewModel.doToPrevChapter(viewPager, pagerAdapter)
                    else if (position == pagerAdapter.itemCount - 1)
                        mangaReaderViewModel.doToNextChapter(viewPager, pagerAdapter)
                }
            }

            // Function which will be tried to load prev or next chapter
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (mangaReaderViewModel.currentReaderFormat == MangaReaderViewModel.READER_FORMAT_MANGA)
                    onPageSelectedInMangaFormat(position)
                else
                    onPageSelectedInBookFormat(position)

                setPageTitle()
            }
        })
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
