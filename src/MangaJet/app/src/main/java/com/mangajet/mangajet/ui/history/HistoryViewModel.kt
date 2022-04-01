package com.mangajet.mangajet.ui.history

import android.content.Intent
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.StorageManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.internal.wait

// Class which represents "History" View Model
class HistoryViewModel : ViewModel() {
    var isInited = false                            // is init boolean flag
    val mangasNames = ArrayList<String>()           // mangas names for list
    var mangas : ArrayList<Manga> = arrayListOf()   // mangas for "AboutManga" activity
    var job : Job? = null                           // Async job for searching and uploading
    var adapter : ArrayAdapter<String>? = null      // adapter for list

    // Function which will load info about each manga from "manga names"
    suspend fun addElementsToMangas() {
        val mangasPaths = StorageManager.getAllPathsForType(StorageManager.FileType.MangaInfo)
        for (path in mangasPaths) {
            try {
                mangas.add(
                    Manga(StorageManager.loadString(path))
                )
                mangasNames.add(mangas[mangas.size - 1].originalName)

            } catch (ex: MangaJetException) {
                println(ex.message)
            }
            withContext(Dispatchers.Main) {
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
