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
    val mangasNames = listOf("One piece", "Boku no hero academia", "Attack on titan")
    var mangas : ArrayList<Manga> = arrayListOf()

    fun initMangas() {
        if (isInited == 0) {
            isInited = 1
            for (name in mangasNames) {
                mangas.add(
                    Librarian.getLibrary(Librarian.LibraryName.Mangachan)!!.searchManga(name)[0]
                )
            }
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
