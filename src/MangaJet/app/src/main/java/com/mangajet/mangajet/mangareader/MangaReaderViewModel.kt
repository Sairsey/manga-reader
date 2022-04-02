package com.mangajet.mangajet.mangareader

import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.MangaJetApp
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
    fun initMangaData() {
        if (!isInited) {
            isInited = true

            // Load manga
            manga = MangaJetApp.currentManga!!

            // And save its state to File
            saveMangaState()
        }
    }
}
