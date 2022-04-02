package com.mangajet.mangajet.mangareader

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga

// Class which represents "Manga Reader" ViewModel
class MangaReaderViewModel : ViewModel() {
    // ViewModel initialization flag
    var isInited = false
    // Manga we are reading right now
    lateinit var manga: Manga

    fun saveMangaState() {
        manga.saveToFile()
    }

    // Function will
    fun initMangaData(intent : Intent) {
        if (!isInited) {
            isInited = true
            // Load manga
            manga = Manga(intent.getStringExtra("Manga").toString())
            manga.lastViewedChapter = intent.getIntExtra("Chapter", manga.lastViewedChapter)

            // And save its state to File
            saveMangaState()
        }
    }
}
