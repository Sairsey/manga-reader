package com.mangajet.mangajet.ui.recommendations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecommendationsViewModel : ViewModel() {
    class Manga(newName: String, newDescr: String) {
        val name : String = newName
        val descr : String = newDescr
    }

    val mangas = listOf(Manga("RecommendedManga1", "descr1"),
        Manga("RecommendedManga2", "descr2"),
        Manga("RecommendedManga3", "descr3"))
    val mangasNames = listOf("RecommendedManga1", "RecommendedManga2", "RecommendedManga3")
}
