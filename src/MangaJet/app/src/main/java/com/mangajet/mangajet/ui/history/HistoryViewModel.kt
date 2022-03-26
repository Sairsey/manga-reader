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
    // dummy data
    var isInited = 0
    val mangasNames = ArrayList<String>()
    var mangas : ArrayList<Manga> = arrayListOf()
    var job : Job? = null
    var adapter : ArrayAdapter<String>? = null

    suspend fun addElementsToMangas() {
        val mangasSearchWords = listOf("Клинок", "Dorohedoro", "chainsaw")
        for (name in mangasSearchWords) {
            mangas.add(
                Librarian.getLibrary(Librarian.LibraryName.Mangachan)!!.searchManga(name)[0]
            )
            mangas[mangas.size - 1].updateInfo()
            mangas[mangas.size - 1].updateChapters()
            mangasNames.add(mangas[mangas.size - 1].originalName)
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
