package com.mangajet.mangajet.mangareader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.MangaPage
import com.mangajet.mangajet.log.Logger
import kotlinx.coroutines.Job


// Class which represents "Manga Reader" ViewModel
class MangaReaderViewModel : ViewModel() {

    // Initialize data to work
    var isInited = false                // ViewModel initialization flag
    lateinit var manga: Manga           // Manga we are reading right now
    var pagesCount = 0                  // pages amount in current viewed chapter

    var jobs = arrayOf<Job?>()


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
                Logger.log("Catch MJE while trying to get pages count: " + ex.message, Logger.Lvl.WARNING)
                Toast.makeText(MangaJetApp.context, ex.message, Toast.LENGTH_SHORT).show()
            }
            uploadPages()

            // And save its state to File
            try {
                manga.saveToFile()
            }
            catch (ex : MangaJetException) {
                Logger.log("Catch MJE while trying to save manga as json: " + ex.message, Logger.Lvl.WARNING)
                Toast.makeText(MangaJetApp.context, ex.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function which will upload pages
    fun uploadPages() {
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
        for (i in 0 until Librarian.settings.LOAD_REPEATS) {
            i.hashCode()
            try {
                page.upload(i > 0)
                val imageFile = page.getFile()
                return BitmapFactory.decodeFile(imageFile.absolutePath) ?: continue
            } catch (ex: MangaJetException) {
                Logger.log("Catch MJE exception in loadBitmap: " + ex.message, Logger.Lvl.WARNING)
                continue
            }
        }
        // maybe throw exception or reload?
        Logger.log("Return null in loadBitMap", Logger.Lvl.WARNING)
        return null
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
                Logger.log("Catch MJE while trying to save manga " + manga.id +
                        " as json: " + ex.message, Logger.Lvl.WARNING)
                // nothing
            }
        }
    }
}
