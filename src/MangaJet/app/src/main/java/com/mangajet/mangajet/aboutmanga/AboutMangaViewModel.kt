package com.mangajet.mangajet.aboutmanga
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException

// Class which represents "About manga" View Model
class AboutMangaViewModel : ViewModel() {
    // ViewModel initialization flag
    var isInited = false
    // Manga, which will be initialised on activity start
    lateinit var manga : Manga

    // Function will
    fun initMangaData() {
        if (!isInited) {
            isInited = true
            manga = MangaJetApp.currentManga!!
            try {
                manga.updateChapters()
            }
            catch (ex: MangaJetException) {
                // chapters from json or no chapters at all.
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        isInited = false
    }
}
