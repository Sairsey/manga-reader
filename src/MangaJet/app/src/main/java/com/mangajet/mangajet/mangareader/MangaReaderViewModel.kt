package com.mangajet.mangajet.mangareader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.viewpager2.widget.ViewPager2
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.MangaPage
import com.mangajet.mangajet.mangareader.formatchangeholder.FormatChangerHandler
import com.mangajet.mangajet.mangareader.formatchangeholder.MangaReaderBaseAdapter
import com.mangajet.mangajet.log.Logger
import com.mangajet.mangajet.mangareader.manhwa.ManhwaReaderVPAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AsyncLoadPage {
    var job: Job
    var page: MangaPage
    var sync = CountDownLatch(1) // 1 - not loaded, 0 - loaded

    constructor(initPage: MangaPage)
    {
        page = initPage
        page.upload()
        job = GlobalScope.launch (Dispatchers.IO) {
            for (i in 0 until Librarian.settings.LOAD_REPEATS) {
                ensureActive()
                try {
                    page.upload(i > 0)
                    ensureActive()
                    page.getFile()
                    ensureActive()
                    break
                } catch (ex: MangaJetException) {
                    Logger.log("Catch MJE exception in loadBitmap: " + ex.message, Logger.Lvl.WARNING)
                    continue
                }
            }
            ensureActive()
            sync.countDown()
        }
    }

}

// Class which represents "Manga Reader" ViewModel
@Suppress("TooManyFunctions")
class MangaReaderViewModel : ViewModel() {
    companion object {
        const val READER_FORMAT_BOOK   = 0  // Book format reader
        const val READER_FORMAT_MANHWA = 1  // Manhwa format reader
        const val READER_FORMAT_MANGA  = 2  // Reverse format reader

        // 'Height/Width' coefficient to select optimize format
        const val HEIGHT_WIDTH_MIN_COEF_TO_MANHWA = 2.5F
    }

    // Initialize data to work
    var isFirstInit = false             // ViewModel is first initialization happened flag
    var isInitializationSuccessed = true
    lateinit var manga: Manga           // Manga we are reading right now
    var pagesCount = 0                  // pages amount in current viewed chapter
    var mutablePagesLoaderMap = mutableMapOf<String, AsyncLoadPage>() // async loader of pages

    var activity : MangaReaderActivity? = null // local variable for activity
    var initilizeJob : Job? = null

    // data for mangaReader format
    var currentReaderFormat = READER_FORMAT_BOOK    // current reader format
    var wasReaderFormat  = READER_FORMAT_BOOK       // previous reader format
    lateinit var mangaReaderVP2 : ViewPager2        // reference on ViewPager2
    var displayWidth : Float = 0F                   // reference on Activity
    // class which will handle all changes in viewpager2 when reader format is changed
    val formatChangerHandler = FormatChangerHandler(this)
    lateinit var navTextView : TextView             // textview with page and chapter number

    var prevAndNextChapterJob : Job? = null
    var prevAndNextChapterSync = CountDownLatch(0)

    /**
     * Case-check functions block
     */
    // Function which check if manga contains only one chapter
    fun isSingleChapterManga() : Boolean {
        return manga.chapters.size == 1
    }

    // Function which check if we reached first chapter
    fun isOnFirstChapter() : Boolean {
        return manga.lastViewedChapter == 0
    }

    // Function which check if we reached last chapter
    fun isOnLastChapter() : Boolean {
        return manga.lastViewedChapter == manga.chapters.size - 1
    }

    // Function which check if we reached last or first chapter
    fun isOnSideChapter() : Boolean {
        return isOnFirstChapter() || isOnLastChapter()
    }

    /**
     * Other functions
     */

    // Function which will set activity title by current opened page
    fun setPageTitle() {
        var chapter = manga.lastViewedChapter
        val page = manga.chapters[chapter].lastViewedPage + 1
        val totalPages = pagesCount

        // increase for correct output
        chapter++
        navTextView.text = "Chapter $chapter, page $page/$totalPages"
    }

    private fun selectOptimizeFormat() : Boolean{
        val middlePage = (pagesCount - 1) / 2
        val mangaPage = manga.chapters[manga.lastViewedChapter].getPage(middlePage)
        mangaPage.upload()
        try {
            val imageFile = mangaPage.getFile() // Catch ex here
            val pageImage = BitmapFactory.decodeFile(imageFile.absolutePath)
            if (pageImage != null && pageImage.height.toFloat() / pageImage.width.toFloat() >
                HEIGHT_WIDTH_MIN_COEF_TO_MANHWA) {
                currentReaderFormat = READER_FORMAT_MANHWA
                wasReaderFormat = READER_FORMAT_MANHWA
            }
            return true
        } catch (ex: MangaJetException) {
            // TODO Show dialog to user with this error
            Logger.log("selectOptimizeFormat exception " + ex.message)
        }
        return false
    }

    // Function which will init all data about manga
    fun initMangaData() {
        // only on first time
        if (!isFirstInit) {
            isFirstInit = true
            // get manga from our shared memory
            manga = MangaJetApp.currentManga!!
            // create async job
            initilizeJob = viewModelScope.launch(Dispatchers.IO) {
                // get amount of pages in our chapter
                try {
                    pagesCount = manga.chapters[manga.lastViewedChapter].getPagesNum()
                } catch (ex: MangaJetException) {
                    Logger.log("Pages exception " + ex.message)
                    pagesCount = 0
                }

                // zero pages is very bad
                if (pagesCount == 0) {
                    isInitializationSuccessed = false
                }

                // load previous and next chapter if they exist
                if (isInitializationSuccessed) {
                    try {
                        if (manga.lastViewedChapter > 0)
                            manga.chapters[manga.lastViewedChapter - 1].getPagesNum()
                        if (manga.lastViewedChapter != (manga.chapters.size - 1))
                            manga.chapters[manga.lastViewedChapter + 1].getPagesNum()
                    } catch (ex: MangaJetException) {
                        Logger.log("Pages exception " + ex.message)
                        isInitializationSuccessed = false
                    }
                }

                // load 1 page to determine manga type
                if (isInitializationSuccessed) {
                    if (!selectOptimizeFormat())
                        isInitializationSuccessed = false
                }

                // delegate all other initialization to activity
                withContext(Dispatchers.Main) {
                    activity?.initialize()
                }

                if (isInitializationSuccessed) {
                    // start pages loading
                    uploadPages()
                }

                // mark job as done
                initilizeJob = null
            }
        }
        else {
            if (initilizeJob == null) {
                // delegate all other initialization to activity
                activity?.initialize()
                if (isInitializationSuccessed) {
                    // start pages loading
                    uploadPages()
                }
            }
        }
    }

    // Function which will upload pages
    private fun uploadPages() {
        prevAndNextChapterSync.await()

        val it: MutableIterator<Map.Entry<String, AsyncLoadPage>> = mutablePagesLoaderMap.entries.iterator()
        while (it.hasNext()) {
            var next = it.next()
            if (!next.value.sync.await(1, TimeUnit.MILLISECONDS)) {
                next.value.job.cancel()
                it.remove()
            }
        }

        // load all our pages
        for (i in 0 until pagesCount) {
            // get page
            var page = manga.chapters[manga.lastViewedChapter].getPage(i)

            // if not already cached
            if (!mutablePagesLoaderMap.containsKey(page.url)) {
                // start loading
                mutablePagesLoaderMap[page.url] = AsyncLoadPage(page)
            }
        }

        prevAndNextChapterSync = CountDownLatch(1)
        prevAndNextChapterJob = viewModelScope.launch (Dispatchers.IO){
            // if we have previous chapter
            if (manga.lastViewedChapter > 0) {
                var prevChapter = manga.chapters[manga.lastViewedChapter - 1]
                var firstPage = prevChapter.getPage(0)
                var lastPage = prevChapter.getPage(prevChapter.getPagesNum() - 1)
                // load first Page
                if (!mutablePagesLoaderMap.containsKey(firstPage.url)) {
                    // start loading
                    mutablePagesLoaderMap[firstPage.url] = AsyncLoadPage(firstPage)
                }
                // load last Page
                if (!mutablePagesLoaderMap.containsKey(lastPage.url)) {
                    // start loading
                    mutablePagesLoaderMap[lastPage.url] = AsyncLoadPage(lastPage)
                }
            }

            // if we have next chapter
            if (manga.lastViewedChapter < manga.chapters.size - 1) {
                var nextChapter = manga.chapters[manga.lastViewedChapter + 1]
                var firstPage = nextChapter.getPage(0)
                var lastPage = nextChapter.getPage(nextChapter.getPagesNum() - 1)
                // load first Page
                if (!mutablePagesLoaderMap.containsKey(firstPage.url)) {
                    // start loading
                    mutablePagesLoaderMap[firstPage.url] = AsyncLoadPage(firstPage)
                }
                // load last Page
                if (!mutablePagesLoaderMap.containsKey(lastPage.url)) {
                    // start loading
                    mutablePagesLoaderMap[lastPage.url] = AsyncLoadPage(lastPage)
                }
            }
            prevAndNextChapterSync.countDown()
        }
    }

    // Function which will return True if format changes to 'reverse' format
    private fun isChangedToManga() : Boolean {
        return currentReaderFormat == READER_FORMAT_MANGA && wasReaderFormat != READER_FORMAT_MANGA
    }

    // Function which will return True if format changes from 'reverse' format to some else
    private fun isChangedToBook() : Boolean {
        return currentReaderFormat != READER_FORMAT_MANGA && wasReaderFormat == READER_FORMAT_MANGA
    }

    // Function which will return delta to set item in pager when reader format changes to book
    private fun getDeltaWhenChangerToBook() : Int {
        return when {
            isSingleChapterManga() -> 0
            isOnFirstChapter() -> 1
            isOnLastChapter() -> 0
            else -> 1
        }
    }

    private fun getDeltaWhenChangerToManga() : Int {
        return when {
            isSingleChapterManga() -> 0
            isOnFirstChapter() -> 0
            isOnLastChapter() -> 1
            else -> 1
        }
    }

    // Function which will redraw viewPager2 with pages with correct chosen format
    fun redrawMangaReader() {
        if (wasReaderFormat != currentReaderFormat) {
            // change viewpager params
            formatChangerHandler.updateReaderFormat()

            // change viewpager position
            if (isChangedToManga() || isChangedToBook()) {
                var pageIndex = manga.chapters[manga.lastViewedChapter].lastViewedPage
                if (isChangedToBook())
                    pageIndex = (pagesCount - 1) - pageIndex

                val delta = if (isChangedToBook()) getDeltaWhenChangerToBook()
                    else getDeltaWhenChangerToManga()

                mangaReaderVP2.setCurrentItem(
                    (mangaReaderVP2.adapter!!.itemCount - 1) - pageIndex - delta,
                    false
                )
            }

            wasReaderFormat = currentReaderFormat
        }
    }

    // Function which will load previous chapter after scroll
    fun doToPrevChapter(viewPager : ViewPager2, pagerAdapter : MangaReaderBaseAdapter) {
        // update chapter
        manga.lastViewedChapter--

        // update pages count (and load chapter)
        try {
            pagesCount = manga
                .chapters[manga.lastViewedChapter].getPagesNum()
        } catch (ex: MangaJetException) {
            Toast.makeText(MangaJetApp.context, ex.message, Toast.LENGTH_SHORT).show()
            viewPager.setCurrentItem(1, false)
            manga.lastViewedChapter++
            return
        }

        // start loading all pages
        uploadPages()

        // set correct page
        manga.chapters[manga.lastViewedChapter].lastViewedPage = pagesCount - 1

        // save manga state
        try {
            manga.saveToFile()
        } catch (ex: MangaJetException) {
            Toast.makeText(MangaJetApp.context, ex.message, Toast.LENGTH_SHORT).show()
        }

        viewPager.adapter = null
        pagerAdapter.notifyDataSetChanged()
        viewPager.adapter = pagerAdapter

        if (currentReaderFormat == READER_FORMAT_MANHWA)
            (pagerAdapter as ManhwaReaderVPAdapter).wasPrevReload = true

        // determine delta
        var delta = 0
        if (manga.lastViewedChapter == 0)
            delta = -1

        if (currentReaderFormat != READER_FORMAT_MANGA)
            viewPager.setCurrentItem(pagesCount + delta, false)
        else
            viewPager.setCurrentItem(1, false)

        val chapter = manga.lastViewedChapter + 1
        Toast.makeText(
            MangaJetApp.context, "Chapter $chapter",
            Toast.LENGTH_SHORT
        ).show()
        Logger.log("Going to chapter $chapter", Logger.Lvl.INFO)
    }

    // Function which will load next chapter after scroll
    fun doToNextChapter(viewPager : ViewPager2, pagerAdapter : MangaReaderBaseAdapter) {
        // update chapter
        manga.lastViewedChapter++

        // update pages count (and load chapter)
        try {
            pagesCount = manga
                .chapters[manga.lastViewedChapter].getPagesNum()
        } catch (ex: MangaJetException) {
            Toast.makeText(MangaJetApp.context, ex.message, Toast.LENGTH_SHORT).show()
            var delta = 0
            if (manga.lastViewedChapter == 0)
                delta = -1
            viewPager.setCurrentItem(pagesCount + delta, false)
            manga.lastViewedChapter++
            return
        }

        // start loading all pages
        uploadPages()

        // set correct page
        manga.chapters[manga.lastViewedChapter]
            .lastViewedPage = 0

        // save manga state
        try {
            manga.saveToFile()
        } catch (ex: MangaJetException) {
            Toast.makeText(MangaJetApp.context, ex.message, Toast.LENGTH_SHORT).show()
        }

        viewPager.adapter = null
        pagerAdapter.notifyDataSetChanged()
        viewPager.adapter = pagerAdapter
        if (currentReaderFormat != READER_FORMAT_MANGA)
            viewPager.setCurrentItem(1, false)
        else {
            // determine delta
            var delta = 0
            if (manga.lastViewedChapter == manga.chapters.size - 1)
                delta = -1

            viewPager.setCurrentItem(pagesCount + delta, false)
        }

        val chapter = manga.lastViewedChapter + 1
        Toast.makeText(
            MangaJetApp.context, "Chapter $chapter",
            Toast.LENGTH_SHORT
        ).show()
        Logger.log("Going to chapter $chapter", Logger.Lvl.INFO)
    }

    // Function will save manga instance on destroy 'MangaReaderActivity'
    override fun onCleared() {
        super.onCleared()
        try {
            manga.saveToFile()
        }
        catch (ex : MangaJetException) {
            Logger.log("Catch MJE while trying to save manga " + manga.id +
                    " as json: " + ex.message, Logger.Lvl.WARNING)
        // nothing
        }
    }
}
