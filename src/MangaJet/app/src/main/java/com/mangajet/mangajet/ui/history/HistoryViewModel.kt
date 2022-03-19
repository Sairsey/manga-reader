package com.mangajet.mangajet.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// Class which represents "History" View Model
class HistoryViewModel : ViewModel() {
    // dummy class
    class Manga(newName: String, newDescr: String) {
        val name: String = newName
        val descr: String = newDescr
    }

    // dummy data
    val mangas = listOf(Manga("manga1", "descr1"),
                        Manga("manga2", "descr2"),
                        Manga("manga3", "descr3"))
    val mangasNames = listOf("manga1", "manga2", "manga3")
}
