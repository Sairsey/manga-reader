package com.mangajet.mangajet.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HistoryViewModel : ViewModel() {
    class Manga(newName: String, newDescr: String) {
        val name : String = newName
        val descr : String = newDescr
    }

    val mangas = listOf(Manga("manga1", "descr1"),
                        Manga("manga2", "descr2"),
                        Manga("manga3", "descr3"))
    val mangasNames = listOf("manga1", "manga2", "manga3")
}
