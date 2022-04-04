package com.mangajet.mangajet.mangareader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.MangaPage
import kotlinx.coroutines.Job

// Class which represents "Manga Reader" ViewModel
class MangaReaderViewModel : ViewModel() {
    // Initialize data to work
    var isInited = false                // ViewModel initialization flag
    lateinit var manga: Manga           // Manga we are reading right now

    // positions in manga
    var maxPos = 0
    var totalPagesInCurrentManga = 0    // amount pages in manga
    var currentChapterIndex = 0         // index of current chapter: in [0; manga.chapters.size)
    var currentPageInChapterIndex = 0   // index of current page in chapter

    // helpful array
    private var pagesInPrevChapters = arrayListOf<Int>()

    // async pages loadings
    lateinit var jobs : Array<Job?>

    fun getPageByPosition(position : Int) : MangaPage {
        var i = 0
        while (!(position >= pagesInPrevChapters[i] &&
            position < pagesInPrevChapters[i] + manga.chapters[i].getPagesNum()))
            i++

        return manga.chapters[i].getPage(position - pagesInPrevChapters[i])
    }

    fun getStartedPosition() : Int {
        return pagesInPrevChapters[currentChapterIndex] + currentPageInChapterIndex
    }


    fun saveLastViewedData() {
        var lastViewedChapter = manga.lastViewedChapter
        var lastViewedPageInChapter = 0
        while (!(maxPos >= pagesInPrevChapters[lastViewedChapter] &&
                    maxPos < pagesInPrevChapters[lastViewedChapter] +
                    manga.chapters[lastViewedChapter].getPagesNum()))
            lastViewedChapter++
        lastViewedPageInChapter = maxPos - pagesInPrevChapters[lastViewedChapter]

        manga.lastViewedChapter = lastViewedChapter
        manga.chapters[lastViewedChapter].lastViewedPage = lastViewedPageInChapter
    }

    // Function will save current manga state to file
    private fun saveMangaState() {
        saveLastViewedData()
        manga.saveToFile()
    }

    // Function will init all data about manga
    fun initMangaData() {
        if (!isInited) {
            isInited = true

            // Load manga and basic info about it
            manga = MangaJetApp.currentManga!!
            pagesInPrevChapters.add(0)
            for (i in 0 until manga.chapters.size) {
                totalPagesInCurrentManga += manga.chapters[i].getPagesNum()
                if (i > 0)
                    pagesInPrevChapters.add(pagesInPrevChapters[i - 1] +
                            manga.chapters[i - 1].getPagesNum())


            }
            currentChapterIndex = manga.lastViewedChapter
            currentPageInChapterIndex = manga.chapters[currentChapterIndex].lastViewedPage
            maxPos = pagesInPrevChapters[currentChapterIndex] + currentPageInChapterIndex

            jobs = arrayOfNulls<Job?>(totalPagesInCurrentManga)

            // And save its state to File
            saveMangaState()
        }
    }

    // Function witch will decode bitmap async
    fun loadBitmap(cover : MangaPage): Bitmap? {
        try {
            val imageFile = cover.getFile() // Catch ex here
            return BitmapFactory.decodeFile(imageFile.absolutePath)
        }
        catch (ex: MangaJetException) {
            return null
        }
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
