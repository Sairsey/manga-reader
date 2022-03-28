package com.mangajet.mangajet.aboutmanga
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga

// Class which represents "About manga" View Model
class AboutMangaViewModel : ViewModel() {
    // ViewModel initialization flag
    var isInited = 0
    // Manga, which will be initialised on activity start
    lateinit var manga : Manga

    // Function will
    fun initMangaData(intent : Intent) {
        if (isInited == 0) {
            isInited = 1
            manga = Manga(intent.getStringExtra("Manga").toString())
            manga.updateChapters()
        }
    }

    override fun onCleared() {
        super.onCleared()
        isInited = 0
    }
}
