package com.mangajet.mangajet.mangareader

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.mangajet.mangajet.R
import com.mangajet.mangajet.log.Logger
import com.mangajet.mangajet.mangareader.formatchangeholder.MangaReaderBaseAdapter


// Class which represents "Manga Reader" Activity
@Suppress("TooManyFunctions")
class MangaReaderActivity : AppCompatActivity() {
    // Manga reader activity ViewModel variable
    lateinit var mangaReaderViewModel : MangaReaderViewModel

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
            R.id.reloadPage -> mangaReaderViewModel.menuHandler.reloadCurrentPage()
            R.id.changeFormat -> mangaReaderViewModel.menuHandler.callChangeFormatDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    // initialization all UI manga data
    fun initialize() {
        // get viewpager reference
        val viewPager = findViewById<ViewPager2>(R.id.mangaViewPager)
        // if everything is bad
        if (mangaReaderViewModel.isInitializationSuccessed == false)
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

        // set manga name in activity action bar
        setTitle()
        // set current chapter and page in bottom bar
        mangaReaderViewModel.setPageTitle()

        // init navigation panel handler
        val navigationPanel = findViewById<androidx.appcompat.widget.LinearLayoutCompat>(
            R.id.reader_navigation_panel)
        val prevChapterButton = findViewById<ImageButton>(R.id.prevChapter)
        val nextChapterButton = findViewById<ImageButton>(R.id.nextChapter)
        val seekBar = findViewById<SeekBar>(R.id.reader_page_seek)
        mangaReaderViewModel.navPanelHandler = MangaReaderNavPanelHandler(
            mangaReaderViewModel,
            navigationPanel,
            nextChapterButton,
            prevChapterButton,
            seekBar
        )
        mangaReaderViewModel.navPanelHandler.initialize()

        // set adapter
        mangaReaderViewModel.formatChangerHandler.updateReaderFormat()

        // set valid page position
        var delta = if (mangaReaderViewModel.isOnFirstChapter()) 0 else 1
        viewPager.setCurrentItem(mangaReaderViewModel.manga
            .chapters[mangaReaderViewModel.manga.lastViewedChapter]
            .lastViewedPage + delta, false)

        Logger.log("Chapter " + (mangaReaderViewModel.manga.lastViewedChapter + 1).toString() + " opened")

        // init toolbar handler (which will be shown and hidden by tap)
        val headerToolbar = findViewById<MaterialToolbar>(R.id.headerToolbar)
        val bottomToolbar = findViewById<MaterialToolbar>(R.id.bottomToolbar)
        mangaReaderViewModel.toolbarHandler = MangaReaderToolbarHandler(headerToolbar, bottomToolbar)

        // init menu handler (reload + format buttons)
        mangaReaderViewModel.menuHandler = MangaReaderMenuHandler(mangaReaderViewModel, viewPager,
            supportFragmentManager)

        // hide onLoad circle
        findViewById<CircularProgressIndicator>(R.id.loadIndicator2).hide()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.log("Manga Reader activity started")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manga_reader_activity)
        setSupportActionBar(findViewById<MaterialToolbar>(R.id.headerToolbar))

        // get viewmodel
        mangaReaderViewModel = ViewModelProvider(this)[MangaReaderViewModel::class.java]
        // get screen width for auto-manhwa
        mangaReaderViewModel.displayWidth = getScreenWidth()
        // create viewpager
        val viewPager = findViewById<ViewPager2>(R.id.mangaViewPager)
        mangaReaderViewModel.mangaReaderVP2 = viewPager
        // put 1st page as default because on 0 page we scroll to previous chapter
        viewPager.setCurrentItem(1, false)
        // get reference for bottom text (chapter %i %i/%i)
        mangaReaderViewModel.navTextView = findViewById(R.id.currentPageText)
        // get reference for activity
        mangaReaderViewModel.activity = this
        // show Circle load indicator
        findViewById<CircularProgressIndicator>(R.id.loadIndicator2).show()
        // init all important manga data
        mangaReaderViewModel.initMangaData()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        // for showing and removing menu
        when (event?.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_UP ->
                if (mangaReaderViewModel.initilizeJob == null)
                    mangaReaderViewModel.toolbarHandler.touchEventDispatcher(event)
        }
        // standard Touch event
        return super.dispatchTouchEvent(event)
    }
}
