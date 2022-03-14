package com.mangajet.mangajet.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchViewModel : ViewModel() {
    class Manga(newName: String, newDescr: String) {
        val name : String = newName
        val descr : String = newDescr
    }

    val mangas = listOf(Manga("FoundedManga1", "descr1"),
        Manga("FoundedManga2", "descr2"),
        Manga("FoundedManga3", "descr3"))
    val mangasNames = listOf("FoundedManga1", "FoundedManga2", "FoundedManga3")
}
