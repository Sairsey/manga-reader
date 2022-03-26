package com.mangajet.mangajet.aboutmanga
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga

// Class which represents "About manga" View Model
class AboutMangaViewModel : ViewModel() {
    // T ODO: IN NEXT SPRINTS WE NEED TO PUT IN 'val' INFO ABOUT MANGA
    var isInited = 0

    lateinit var manga : Manga

    fun initMangaData(intent : Intent) {
        if (isInited == 0) {
            isInited = 1
            manga = Manga(intent.getStringExtra("Manga").toString())
        }
    }

    override fun onCleared() {
        super.onCleared()
        isInited = 0
    }
}
