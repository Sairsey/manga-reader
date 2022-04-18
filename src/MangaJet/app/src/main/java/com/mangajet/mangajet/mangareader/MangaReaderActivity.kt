package com.mangajet.mangajet.mangareader

import android.animation.ObjectAnimator
import android.os.Bundle
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
    companion object {
        const val SINGLE_TOUCH_RAD = 100

        const val TRANSLATE_MENU_UP = -200F
        const val TRANSLATE_MENU_DOWN = 200F
        const val ANIMATION_DURATION = 750L

        const val MIDDLE_SCREEN_SQUARE_PART = 5
    }

    // Manga reader activity ViewModel variable
    lateinit var mangaReaderViewModel : MangaReaderViewModel

    // touch down coords
    var xTouch : Float = 0.0F
    var yTouch : Float = 0.0F
    var isHidden = false

    // Function which will set activity title by current opened page
    fun setPageTitle(
        mangaReaderViewModel : MangaReaderViewModel,
        position: Int
    ) {
        val page = position + 1
        val chapter = mangaReaderViewModel.manga.lastViewedChapter + 1
        val totalPages = mangaReaderViewModel.pagesCount

        val navTextView = findViewById<TextView>(R.id.currentPageText)
        navTextView.text = "Chapter $chapter, page $page/$totalPages"
    }

    fun setTitle () {
        var title = if (mangaReaderViewModel.manga.originalName != ""
            && mangaReaderViewModel.manga.originalName != null)
            mangaReaderViewModel.manga.originalName
        else
            mangaReaderViewModel.manga.russianName
        supportActionBar?.title = title
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.manga_reader_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Function which will brute forced reload page
    private fun reloadCurrentPage() {
        val chapter = mangaReaderViewModel.manga
            .chapters[mangaReaderViewModel.manga.lastViewedChapter]
        // at this point we already downloaded whole chapter so no need to worry about exception
        val page = chapter.getPage(chapter.lastViewedPage)

        // this can only fail if we do not have storage permission
        // We have blocking dialog in this case, so it someone still
        // manges to go here, I think we should crash
        page.upload(true)

        val viewPager = findViewById<ViewPager2>(R.id.mangaViewPager)
        var delta = if (mangaReaderViewModel.isOnFirstChapter()) 0 else 1
        var position = chapter.lastViewedPage + delta

        val pagerAdapter = viewPager.adapter
        viewPager.adapter = null
        pagerAdapter?.notifyDataSetChanged()
        viewPager.adapter = pagerAdapter
        viewPager.setCurrentItem(position, false)
    }

    // Function which will call dialog for changing format
    private fun callChangeFormatDialog() {
        val myDialogFragment = ChangeMangaReaderFormatDialog(mangaReaderViewModel)
        myDialogFragment.show(supportFragmentManager, "Change reader format dialog")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reloadPage -> reloadCurrentPage()
            R.id.changeFormat -> callChangeFormatDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manga_reader_activity)

        setSupportActionBar(findViewById<MaterialToolbar>(R.id.headerToolbar))

        mangaReaderViewModel = ViewModelProvider(this).get(MangaReaderViewModel::class.java)

        mangaReaderViewModel.initMangaData()
        setTitle()
        setPageTitle(mangaReaderViewModel,
            mangaReaderViewModel.manga
                .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                .lastViewedPage)

        val viewPager = findViewById<ViewPager2>(R.id.mangaViewPager)
        mangaReaderViewModel.mangaReaderVP2 = viewPager
        val pagerAdapter = MangaReaderVPAdapter(mangaReaderViewModel)
        viewPager.adapter = pagerAdapter

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
                setPageTitle(mangaReaderViewModel,
                    mangaReaderViewModel.manga
                        .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                        .lastViewedPage)
            }
        }

        val nextChapterButton = findViewById<ImageButton>(R.id.nextChapter)
        nextChapterButton.setOnClickListener {
            if (!mangaReaderViewModel.isOnLastChapter()) {
                mangaReaderViewModel.doToNextChapter(viewPager, pagerAdapter)
                setPageTitle(mangaReaderViewModel,
                    mangaReaderViewModel.manga
                        .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                        .lastViewedPage)
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            // Function which will be tried to load prev or next chapter
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
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
                    if (position == pagerAdapter.itemCount - 1
                        && mangaReaderViewModel.currentReaderFormat != MangaReaderViewModel.READER_FORMAT_MANGA)
                        mangaReaderViewModel.doToNextChapter(viewPager, pagerAdapter)
                    else if (position == 0
                        && mangaReaderViewModel.currentReaderFormat == MangaReaderViewModel.READER_FORMAT_MANGA)
                        mangaReaderViewModel.doToNextChapter(viewPager, pagerAdapter)
                }
                // Last chapter
                else if (mangaReaderViewModel.manga.lastViewedChapter == mangaReaderViewModel.manga.chapters.size - 1) {
                    if (position == 0 && mangaReaderViewModel.currentReaderFormat
                        != MangaReaderViewModel.READER_FORMAT_MANGA)
                        mangaReaderViewModel.doToPrevChapter(viewPager, pagerAdapter)
                    else if (position == pagerAdapter.itemCount - 1
                        && mangaReaderViewModel.currentReaderFormat == MangaReaderViewModel.READER_FORMAT_MANGA)
                        mangaReaderViewModel.doToPrevChapter(viewPager, pagerAdapter)
                }
                // Other cases
                else {
                    if (position == 0 && mangaReaderViewModel.currentReaderFormat
                        != MangaReaderViewModel.READER_FORMAT_MANGA)
                        mangaReaderViewModel.doToPrevChapter(viewPager, pagerAdapter)
                    else if (position == pagerAdapter.itemCount - 1
                        && mangaReaderViewModel.currentReaderFormat == MangaReaderViewModel.READER_FORMAT_MANGA)
                        mangaReaderViewModel.doToPrevChapter(viewPager, pagerAdapter)
                    else if (position == pagerAdapter.itemCount - 1
                        && mangaReaderViewModel.currentReaderFormat != MangaReaderViewModel.READER_FORMAT_MANGA)
                        mangaReaderViewModel.doToNextChapter(viewPager, pagerAdapter)
                    else if (position == 0
                        && mangaReaderViewModel.currentReaderFormat == MangaReaderViewModel.READER_FORMAT_MANGA)
                        mangaReaderViewModel.doToNextChapter(viewPager, pagerAdapter)
                }

                setPageTitle(mangaReaderViewModel,
                    mangaReaderViewModel.manga
                    .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                    .lastViewedPage)
            }
        })
    }


    private fun hideMenu() {
        val topMenu = findViewById<MaterialToolbar>(R.id.headerToolbar)
        val bottomMenu = findViewById<MaterialToolbar>(R.id.bottomToolbar)


        ObjectAnimator.ofFloat(topMenu, "translationY", TRANSLATE_MENU_UP).apply {
            duration = ANIMATION_DURATION
            start()
        }

        ObjectAnimator.ofFloat(bottomMenu, "translationY", TRANSLATE_MENU_DOWN).apply {
            duration = ANIMATION_DURATION
            start()
        }
    }

    private fun showMenu() {
        val topMenu = findViewById<MaterialToolbar>(R.id.headerToolbar)
        val bottomMenu = findViewById<MaterialToolbar>(R.id.bottomToolbar)

        ObjectAnimator.ofFloat(topMenu, "translationY", 0F).apply {
            duration = ANIMATION_DURATION
            start()
        }

        ObjectAnimator.ofFloat(bottomMenu, "translationY", 0F).apply {
            duration = ANIMATION_DURATION
            start()
        }
    }

    private fun isInMiddleSquare() : Boolean {
        val width: Int = context!!.resources.displayMetrics.widthPixels
        val height: Int = context!!.resources.displayMetrics.heightPixels

        return (xTouch >= width / MIDDLE_SCREEN_SQUARE_PART
                && xTouch <= width - width / MIDDLE_SCREEN_SQUARE_PART
                && yTouch >= height / MIDDLE_SCREEN_SQUARE_PART
                && yTouch <= height - height / MIDDLE_SCREEN_SQUARE_PART)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                xTouch = event.x
                yTouch = event.y
            }
            MotionEvent.ACTION_UP -> {
                if ((xTouch - event.x) * (xTouch - event.x)
                    + (yTouch - event.y) * (yTouch - event.y) < SINGLE_TOUCH_RAD
                    && isInMiddleSquare()) {
                    if (isHidden)
                        showMenu()
                    else
                        hideMenu()
                    isHidden = !isHidden
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}
