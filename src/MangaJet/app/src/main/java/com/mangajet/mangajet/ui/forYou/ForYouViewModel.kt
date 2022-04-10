package com.mangajet.mangajet.ui.forYou

import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.MangaListAdapter
import com.mangajet.mangajet.MangaListElementContainer
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
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
        val mangasSearchWords = listOf("гуль", "Берсерк", "Onepunchman")
        for (name in mangasSearchWords) {
            mangas.add(
                Librarian.getLibrary(Librarian.LibraryName.Mangachan)!!.searchManga(name)[2]
            )
            mangas[mangas.size - 1].updateInfo()
            withContext (Dispatchers.Main) {
                mangasInfos.add(MangaListElementContainer(
                    mangas[mangas.size - 1].originalName,
                    mangas[mangas.size - 1].author,
                    mangas[mangas.size - 1].library.getURL(),
                    mangas[mangas.size - 1].cover
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