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
    var isInited = 0
    val mangasNames = ArrayList<String>()
    var mangas : ArrayList<Manga> = arrayListOf()
    var job : Job? = null
    var adapter : ArrayAdapter<String>? = null

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

    fun initMangas(adapterNew: ArrayAdapter<String>) {
        if (isInited == 0) {
            isInited = 1
            adapter = adapterNew
            job = GlobalScope.launch(Dispatchers.IO) {
                addElementsToMangas()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        isInited = 0
        mangas.clear()
    }
}
