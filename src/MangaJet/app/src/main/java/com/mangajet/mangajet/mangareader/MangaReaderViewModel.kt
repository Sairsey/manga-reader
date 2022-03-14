package com.mangajet.mangajet.mangareader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MangaReaderViewModel : ViewModel() {
    class Manga(newName: String, newDescr: String) {
        val name : String = newName
        val descr : String = newDescr
    }

    val manga = Manga("SomeMangaForNotEmptyClassWarningAbuse", "hehe1")
    val mangasNames = listOf("sample_text", "sample_text2", "sample_text3")
}
