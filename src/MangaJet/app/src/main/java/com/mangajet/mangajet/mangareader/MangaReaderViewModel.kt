package com.mangajet.mangajet.mangareader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.MangaJetApp.Companion.context
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.MangaPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

// Class which represents "Manga Reader" ViewModel
class MangaReaderViewModel : ViewModel() {
    companion object {
        const val LOAD_REPEATS = 5      // Load repeat count (if prev load failed -> repeat)
    }

    // Initialize data to work
    var isInited = false                // ViewModel initialization flag
    lateinit var manga: Manga           // Manga we are reading right now

    var pagesCount = 0                  // pages amount in current viewed chapter

    // async pages loadings
    var jobs : Array<Job?> = arrayOf()

    // Function which will save current manga state to file
    private fun saveMangaState() {
        manga.saveToFile()
    }

    // Function which will init all data about manga
    fun initMangaData() {
        if (!isInited) {
            isInited = true

            // Load manga and basic info about it
            manga = MangaJetApp.currentManga!!
            pagesCount = manga.chapters[manga.lastViewedChapter].getPagesNum()

            uploadPages()

            // And save its state to File
            saveMangaState()
        }
    }

    fun uploadPages() {
        for (job in jobs)
            job?.cancel()

        jobs = arrayOfNulls<Job?>(pagesCount)

        for (i in 0 until pagesCount)
            manga.chapters[manga.lastViewedChapter].getPage(i).upload()
    }

    // Function witch will decode bitmap async
    suspend fun loadBitmap(page : MangaPage): Bitmap? {
        for (i in 0 until LOAD_REPEATS) {
            i.hashCode()
            try {
                page.upload(i > 0)
                val imageFile = page.getFile() // Catch ex here
                val res = BitmapFactory.decodeFile(imageFile.absolutePath)
                if (res == null)
                    continue
                return res
            } catch (ex: MangaJetException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                }
                continue
            }
        }
        // TODO Make sneakbar to ask user for reload
        return null
    }

    // Function will save manga instance on destroy 'MangaReaderActivity'
    override fun onCleared() {
        super.onCleared()
        if (isInited) {
            isInited = false
            saveMangaState()
        }
    }
}
