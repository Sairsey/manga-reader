package com.mangajet.mangajet.ui.history

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangajet.mangajet.MangaListAdapter
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.StorageManager
import com.mangajet.mangajet.databinding.HistoryFragmentBinding
import com.mangajet.mangajet.log.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

// Class which represents "History" View Model
class HistoryViewModel : ViewModel() {
    var mangas : ArrayList<Manga> = arrayListOf()   // mangas for "AboutManga" activity
    var job : Job? = null                           // Async job for searching and uploading
    var adapter : MangaListAdapter? = null          // adapter for list
    var historyMutex : Boolean = true               // Mutex for data sync  protection

    // Function which will load info about each manga from "manga names"
    private suspend fun addElementsToMangas(context : CoroutineContext) {
        var mangasPaths = arrayOf<String>()
        try {
            mangasPaths = StorageManager.getAllPathsForType(StorageManager.FileType.MangaInfo)
        }
        catch (ex: MangaJetException) {
            Logger.log("Catch MJE while trying to load info about some manga: " + ex.message, Logger.Lvl.WARNING)
            // we do not have permission to read.
            // not a big problem, just not showing anything
        }
        for (path in mangasPaths) {
            context.ensureActive()
            try {
                var manga = Manga(StorageManager.loadString(path, StorageManager.FileType.MangaInfo))
                withContext (Dispatchers.Main) {
                    //synchronized(historyMutex) {
                        mangas.add(manga)
                    //}
                }
            }
            catch (ex: MangaJetException) {
                Logger.log("Catch MJE while trying to get info about some manga: " + ex.message, Logger.Lvl.WARNING)
                // nothing too tragic, we just haven`t permission to read or file invalid
                // but we should continue
                continue
            }
        }
        withContext (Dispatchers.Main) {
            adapter?.notifyDataSetChanged()
        }
    }

    // Function which update mangas info from storage
    fun makeListFromStorage(adapterNew: MangaListAdapter,
        binding : HistoryFragmentBinding) {
        // cancel job if we need
        adapter = adapterNew
        job?.cancel()
        //synchronized(historyMutex) {
            mangas.clear()
        //}

        job = viewModelScope.launch(Dispatchers.Default) {
            addElementsToMangas(coroutineContext)

            withContext(Dispatchers.Main) {
                binding.progressBar.hide()
                if (mangas.isEmpty())
                    binding.noResultLayout.visibility = View.VISIBLE
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        job?.cancel()
        //synchronized(historyMutex) {
            mangas.clear()
        //}
    }
}
