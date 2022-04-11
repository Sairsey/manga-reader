package com.mangajet.mangajet.ui.history

import android.view.View
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.MangaListAdapter
import com.mangajet.mangajet.MangaListElementContainer
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.StorageManager
import com.mangajet.mangajet.databinding.HistoryFragmentBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Class which represents "History" View Model
class HistoryViewModel : ViewModel() {
    var mangas : ArrayList<Manga> = arrayListOf()   // mangas for "AboutManga" activity
    var job : Job? = null                           // Async job for searching and uploading
    var adapter : MangaListAdapter? = null          // adapter for list

    // mangas info for list
    val mangasInfos = ArrayList<MangaListElementContainer>()

    // Function which will load info about each manga from "manga names"
    private suspend fun addElementsToMangas() {
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
                withContext (Dispatchers.Main) {
                    mangasInfos.add(MangaListElementContainer(
                        mangas[mangas.size - 1].originalName,
                        mangas[mangas.size - 1].author,
                        mangas[mangas.size - 1].library.getURL(),
                        mangas[mangas.size - 1].cover,
                        mangas[mangas.size - 1].library.getHeadersForDownload()
                    ))
                    adapter?.notifyDataSetChanged()
                }
            }
            catch (ex: MangaJetException) {
                // nothing too tragic, we just haven`t permission to read or file invalid
                // but we should continue
                continue
            }
        }
    }

    // Function which update mangas info from storage
    fun makeListFromStorage(adapterNew: MangaListAdapter,
        binding : HistoryFragmentBinding) {
        // cancel job if we need
        adapter = adapterNew
        job?.cancel()
        mangas.clear()
        mangasInfos.clear()

        job = GlobalScope.launch(Dispatchers.Default) {
            addElementsToMangas()

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
        mangas.clear()
        mangasInfos.clear()
    }
}
