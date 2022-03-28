package com.mangajet.mangajet.ui.search

import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Class which represents "Search" ViewModel
class SearchViewModel : ViewModel() {
    var isInited = false                            // is init boolean flag
    val mangasNames = ArrayList<String>()           // mangas names for list
    var mangas : ArrayList<Manga> = arrayListOf()   // mangas for "AboutManga" activity
    var job : Job? = null                           // Async job for searching and uploading
    var adapter : ArrayAdapter<String>? = null      // adapter for list

    // Function which will load info about each manga from "manga names"
    suspend fun addElementsToMangas() {
        val mangasSearchWord = "Учитель"
        val libsMangas = Librarian.getLibrary(Librarian.LibraryName.Mangachan)!!.searchManga(mangasSearchWord)
        for (i in libsMangas.indices) {
            mangas.add(libsMangas[i])
            mangas[i].updateInfo()
            mangasNames.add(mangas[i].originalName)
            withContext (Dispatchers.Main) {
                adapter?.notifyDataSetChanged()
            }
        }
    }

    // Function which will async load mangas info
    fun initMangas(adapterNew: ArrayAdapter<String>) {
        if (!isInited) {
            isInited = true
            adapter = adapterNew
            job = GlobalScope.launch(Dispatchers.IO) {
                addElementsToMangas()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        isInited = false
        mangas.clear()
    }
}
