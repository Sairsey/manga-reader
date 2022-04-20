package com.mangajet.mangajet.mangareader

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.viewpager2.widget.ViewPager2
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.MangaPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.FieldPosition


// Class which represents "Manga Reader" ViewModel
@Suppress("TooManyFunctions")
class MangaReaderViewModel : ViewModel() {
    companion object {
        const val LOAD_REPEATS = 5      // Load repeat count (if prev load failed -> repeat)

        const val READER_FORMAT_BOOK = 0
        const val READER_FORMAT_MANHWA = 1
        const val READER_FORMAT_MANGA = 2
    }

    // Initialize data to work
    var isInited = false                // ViewModel initialization flag
    lateinit var manga: Manga           // Manga we are reading right now
    var pagesCount = 0                  // pages amount in current viewed chapter
    var jobs = arrayOf<Job?>()

    // data for mangaReader format
    var currentReaderFormat = READER_FORMAT_BOOK
    var wasReaderFormat  = READER_FORMAT_BOOK
    lateinit var mangaReaderVP2 : ViewPager2
    lateinit var currentActivityRef : Activity

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
    // Function which will init all data about manga
    fun initMangaData() {
        if (!isInited) {
            isInited = true

            // Load manga and basic info about it
            manga = MangaJetApp.currentManga!!
            try {
                pagesCount = manga.chapters[manga.lastViewedChapter].getPagesNum()
            }
            catch (ex : MangaJetException) {
                Toast.makeText(MangaJetApp.context, ex.message, Toast.LENGTH_SHORT).show()
            }
            uploadPages()

            // And save its state to File
            try {
                manga.saveToFile()
            }
            catch (ex : MangaJetException) {
                Toast.makeText(MangaJetApp.context, ex.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function which will upload pages
    private fun uploadPages() {
        for (job in jobs)
            if (job != null && job.isActive)
                job.cancel()

        jobs = arrayOfNulls(pagesCount + 2)

        // at this point chapter already loaded so no need to worry about getPage exception
        // upload() can only fail if we do not have storage permission
        // We have blocking dialog in this case, so it someone still
        // manges to go here, I think we should crash
        for (i in 0 until pagesCount)
            manga.chapters[manga.lastViewedChapter].getPage(i).upload()
    }

    // Function which will decode bitmap async
    fun loadBitmap(page : MangaPage): Bitmap? {
        for (i in 0 until LOAD_REPEATS) {
            i.hashCode()
            try {
                page.upload(i > 0)
                val imageFile = page.getFile()
                return BitmapFactory.decodeFile(imageFile.absolutePath) ?: continue
            } catch (ex: MangaJetException) {
                continue
            }
        }
        // maybe throw exception or reload?
        return null
    }

    private fun isChangedToManga() : Boolean {
        return currentReaderFormat == READER_FORMAT_MANGA && wasReaderFormat != READER_FORMAT_MANGA
    }

    private fun isChangedToBook() : Boolean {
        return currentReaderFormat != READER_FORMAT_MANGA && wasReaderFormat == READER_FORMAT_MANGA
    }

    fun redrawMangaReader() {
        // change orientation
        if (currentReaderFormat == READER_FORMAT_MANHWA) {
            mangaReaderVP2.orientation = ViewPager2.ORIENTATION_VERTICAL
        }
        else
            mangaReaderVP2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // change viewpager position
        if (isChangedToManga() || isChangedToBook()) {
            val pageIndex = manga.chapters[manga.lastViewedChapter].lastViewedPage
            val delta = when {
                isSingleChapterManga() -> 0
                isOnFirstChapter() -> 0
                isOnLastChapter() -> 1
                else -> 1
            }

            mangaReaderVP2.setCurrentItem(
                (mangaReaderVP2.adapter!!.itemCount - 1) - pageIndex - delta,
                false)
        }

        val pagerAdapter = mangaReaderVP2.adapter
        pagerAdapter?.notifyDataSetChanged()
    }

    // Function which will load previous chapter after scroll
    fun doToPrevChapter(viewPager : ViewPager2, pagerAdapter : MangaReaderVPAdapter) {
        // update chapter
        manga.lastViewedChapter--

        // update pages count (and load chapter)
        try {
            pagesCount = manga
                .chapters[manga.lastViewedChapter].getPagesNum()
        }
        catch (ex:MangaJetException) {
            Toast.makeText(MangaJetApp.context, ex.message, Toast.LENGTH_SHORT).show()
            viewPager.setCurrentItem(1, false)
            manga.lastViewedChapter++
            return
        }

        // start loading all pages
        val job = GlobalScope.launch(Dispatchers.IO) {
            uploadPages()
        }

        // set correct page
        manga.chapters[manga.lastViewedChapter].lastViewedPage = pagesCount - 1

        // save manga state
        try {
            manga.saveToFile()
        }
        catch (ex : MangaJetException) {
            Toast.makeText(MangaJetApp.context, ex.message, Toast.LENGTH_SHORT).show()
        }

        // update adapter
        while (job.isActive)
            println("Im busy man too")

        viewPager.adapter = null
        pagerAdapter.notifyDataSetChanged()
        viewPager.adapter = pagerAdapter

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
            Toast.LENGTH_SHORT).show()
    }

    // Function which will load next chapter after scroll
    fun doToNextChapter(viewPager : ViewPager2, pagerAdapter : MangaReaderVPAdapter) {
        // update chapter
        manga.lastViewedChapter++;

        // update pages count (and load chapter)
        try {
            pagesCount = manga
                .chapters[manga.lastViewedChapter].getPagesNum()
        }
        catch (ex:MangaJetException) {
            Toast.makeText(MangaJetApp.context, ex.message, Toast.LENGTH_SHORT).show()
            var delta = 0
            if (manga.lastViewedChapter == 0)
                delta = -1
            viewPager.setCurrentItem(pagesCount + delta, false)
            manga.lastViewedChapter++
            return
        }

        // start loading all pages
        val job = GlobalScope.launch(Dispatchers.IO) {
            uploadPages()
        }

        // set correct page
        manga.chapters[manga.lastViewedChapter]
            .lastViewedPage = 0

        // save manga state
        try {
            manga.saveToFile()
        }
        catch (ex : MangaJetException) {
            Toast.makeText(MangaJetApp.context, ex.message, Toast.LENGTH_SHORT).show()
        }

        // update adapter
        while (job.isActive)
            println("Im busy man")

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
            Toast.LENGTH_SHORT).show()
    }

    // Function will save manga instance on destroy 'MangaReaderActivity'
    override fun onCleared() {
        super.onCleared()
        if (isInited) {
            isInited = false
            try {
                manga.saveToFile()
            }
            catch (ex : MangaJetException) {
                // nothing
            }
        }
    }
}
