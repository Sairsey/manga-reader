package com.mangajet.mangajet.ui.search

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga

// Class which represents "Search" ViewModel
class SearchViewModel : ViewModel() {
    var isInited = 0
    val mangasNames = listOf("Гуль", "Гуль", "Гуль")
    var mangas : ArrayList<Manga> = arrayListOf()

    fun initMangas() {
        if (isInited == 0) {
            isInited = 1
            val mangasTmp =
                Librarian.getLibrary(Librarian.LibraryName.Mangachan)!!.searchManga(mangasNames[0])
            mangas.add(mangasTmp[0])
            mangas.add(mangasTmp[1])
            mangas.add(mangasTmp[2])
        }
    }

    fun packageMangaToIntent(id : Int, intent : Intent) {
        mangas[id].updateInfo()
        mangas[id].updateChapters()
        intent.putExtra("Manga original title", mangas[id].originalName)
        intent.putExtra("Manga rus title", mangas[id].russianName)
        intent.putExtra("Manga author", mangas[id].author)
        intent.putExtra("Manga cover", mangas[id].cover)
        intent.putExtra("Manga description", mangas[id].description)
    }

    override fun onCleared() {
        super.onCleared()
        isInited = 0
        mangas.clear()
    }
}
