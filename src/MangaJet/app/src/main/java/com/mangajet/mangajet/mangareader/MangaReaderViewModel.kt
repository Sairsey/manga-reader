package com.mangajet.mangajet.mangareader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.MangaPage


// Class which represents "Manga Reader" ViewModel
class MangaReaderViewModel : ViewModel() {
    companion object {
        const val LOAD_REPEATS = 5      // Load repeat count (if prev load failed -> repeat)
    }

    // Initialize data to work
    var isInited = false                // ViewModel initialization flag
    lateinit var manga: Manga           // Manga we are reading right now
    var pagesCount = 0                  // pages amount in current viewed chapter


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

    // Function which check if we are in middle chapter
    fun isOnMiddleChapter() : Boolean {
        return !(manga.lastViewedChapter == manga.chapters.size - 1
                || manga.lastViewedChapter == 0)
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
            pagesCount = manga.chapters[manga.lastViewedChapter].getPagesNum()

            uploadPages()

            // And save its state to File
            manga.saveToFile()
        }
    }

    // Function which will upload pages
    fun uploadPages() {
        for (i in 0 until pagesCount)
            manga.chapters[manga.lastViewedChapter].getPage(i).upload()
    }

    // Function which will brutforced reload page
    fun uploadCurrentPage(page : MangaPage) {
        page.upload(true)
    }

    // Function which will decode bitmap async
    fun loadBitmap(page : MangaPage): Bitmap? {
        for (i in 0 until LOAD_REPEATS) {
            i.hashCode()
            try {
                page.upload(i > 0)
                val imageFile = page.getFile() // Catch ex here
                return BitmapFactory.decodeFile(imageFile.absolutePath) ?: continue
            } catch (ex: MangaJetException) {
                continue
            }
        }
        // maybe throw exception or reload?
        return null
    }

    // Function will save manga instance on destroy 'MangaReaderActivity'
    override fun onCleared() {
        super.onCleared()
        if (isInited) {
            isInited = false
            manga.saveToFile()
        }
    }
}
