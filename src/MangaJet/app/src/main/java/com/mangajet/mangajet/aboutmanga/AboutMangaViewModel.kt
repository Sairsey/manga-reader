package com.mangajet.mangajet.aboutmanga
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga

// Class which represents "About manga" View Model
class AboutMangaViewModel : ViewModel() {
    // T ODO: IN NEXT SPRINTS WE NEED TO PUT IN 'val' INFO ABOUT MANGA
    var isInited = 0

    lateinit var origTitle : String
    lateinit var rusTitle : String
    lateinit var author : String
    lateinit var cover : String
    lateinit var descr : String

    fun initMangaData(intent : Intent) {
        if (isInited == 0) {
            isInited = 1
            origTitle = intent.getStringExtra("Manga original title").toString()
            rusTitle = intent.getStringExtra("Manga rus title").toString()
            author = intent.getStringExtra("Manga author").toString()
            cover = intent.getStringExtra("Manga cover").toString()
            descr = intent.getStringExtra("Manga description").toString()
        }
    }

    override fun onCleared() {
        super.onCleared()
        isInited = 0
    }
}
