package com.mangajet.mangajet.ui.history

import android.content.Intent
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Class which represents "History" View Model
class HistoryViewModel : ViewModel() {
    var isInited = false                            // is init boolean flag
    val mangasNames = ArrayList<String>()           // mangas names for list
    var mangas : ArrayList<Manga> = arrayListOf()   // mangas for "AboutManga" activity
    var job : Job? = null                           // Async job for searching and uploading
    var adapter : ArrayAdapter<String>? = null      // adapter for list

    // Function which will load info about each manga from "manga names"
    suspend fun addElementsToMangas() {
        val mangasSearchWords = listOf("Клинок", "Dorohedoro", "chainsaw")
        for (name in mangasSearchWords) {
            mangas.add(
                Librarian.getLibrary(Librarian.LibraryName.Mangachan)!!.searchManga(name)[0]
            )
            mangas[mangas.size - 1].updateInfo()
            withContext (Dispatchers.Main) {
                mangasNames.add(mangas[mangas.size - 1].originalName)
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
