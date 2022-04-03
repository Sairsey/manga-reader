package com.mangajet.mangajet.ui.history

import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.StorageManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
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
        var mangasPaths = arrayOf<String>()
        try {
            mangasPaths = StorageManager.getAllPathsForType(StorageManager.FileType.MangaInfo)
        }
        catch (ex: MangaJetException) {
            // we do not have permission to read.
            // not a big problem, just not showing anything
        }
        for (path in mangasPaths) {
            job?.ensureActive()
            try {
                mangas.add(
                    Manga(StorageManager.loadString(path, StorageManager.FileType.MangaInfo))
                )
            }
            catch (ex: MangaJetException) {
                // nothing too tragic, we just haven`t permission to read or file invalid
                // but we should continue
                continue
            }

            job?.ensureActive()
            withContext (Dispatchers.Main) {
                mangasNames.add(mangas[mangas.size - 1].originalName)
                adapter?.notifyDataSetChanged()
            }
        }
    }

    // Function which update mangas info from storage
    fun makeListFromStorage() {
        // cancel job if we need
        job?.cancel()
        mangas.clear()
        mangasNames.clear()

        job = GlobalScope.launch(Dispatchers.Default) {
            addElementsToMangas()
        }
    }

    // Function which set adapter async load mangas info
    fun init(adapterNew: ArrayAdapter<String>) {
        if (!isInited) {
            isInited = true
            adapter = adapterNew
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
        mangas.clear()
        mangasNames.clear()
        isInited = false
    }
}
