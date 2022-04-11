package com.mangajet.mangajet.ui.forYou

import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.MangaListAdapter
import com.mangajet.mangajet.MangaListElementContainer
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


// Class which represents "Recommendations" ViewModel
class ForYouViewModel : ViewModel() {
    var isInited = false                            // is init boolean flag
    var mangas : ArrayList<Manga> = arrayListOf()   // mangas for "AboutManga" activity
    var job : Job? = null                           // Async job for searching and uploading
    var adapter : MangaListAdapter? = null      // adapter for list

    // mangas info for list
    val mangasInfos = ArrayList<MangaListElementContainer>()

    // Function which will load info about each manga from "manga names"
    suspend fun addElementsToMangas() {
        val mangasSearchWords = listOf("Гуль", "Берсерк", "Onepunchman")
        for (name in mangasSearchWords) {
            try {
                mangas.add(
                    Librarian.getLibrary(Librarian.LibraryName.Mangachan)!!.searchManga(name)[2]
                )
            }
            catch (ex: MangaJetException) {
                // nothing too tragic. If manga not found we can just skip it
                continue
            }

            try {
                mangas[mangas.size - 1].updateInfo()
            }
            catch (ex: MangaJetException) {
                // This may be tragic.
                // lets remove this manga from mangas
                mangas.removeLast()
            }

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
    }

    // Function which will async load mangas info
    fun initMangas(adapterNew: MangaListAdapter) {
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
