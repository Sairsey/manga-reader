package com.mangajet.mangajet.aboutmanga
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga

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
            manga.updateChapters()
        }
    }

    override fun onCleared() {
        super.onCleared()
        isInited = false
    }
}
