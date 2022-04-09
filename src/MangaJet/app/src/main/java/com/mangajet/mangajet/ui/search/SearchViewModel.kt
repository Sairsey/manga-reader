package com.mangajet.mangajet.ui.search

import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.MangaJetApp.Companion.context
import com.mangajet.mangajet.MangaListAdapter
import com.mangajet.mangajet.MangaListElementContainer
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.databinding.SearchFragmentBinding
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Class which represents "Search" ViewModel
class SearchViewModel : ViewModel() {
    companion object {
        const val SEARCH_AMOUNT = 20        // Amount of searchable mangas
        const val WAIT_FOR_BUILD_IN_MS = 75
    }
    var mangas : ArrayList<Manga> = arrayListOf()   // mangas for "AboutManga" activity
    var job : Job? = null                           // Async job for searching and uploading
    var adapter : MangaListAdapter? = null          // adapter for list

    // mangas info for list
    val mangasInfos = ArrayList<MangaListElementContainer>()

    // Selected resources
    var allLibraries = arrayOf(
        Librarian.LibraryName.Readmanga,
        Librarian.LibraryName.Mangachan,
        Librarian.LibraryName.Mangalib,
        Librarian.LibraryName.Acomics
    )
    // flags for each resource
    private var chosenLibraries = BooleanArray(allLibraries.size)

    // init flags
    init {
        chosenLibraries[0] = true
        for (i in 1 until chosenLibraries.size)
            chosenLibraries[i] = false
    }

    // Function which will upload manga into mangas array and catch exceptions
    private suspend fun uploadMangaIntoArray(i : Int) : Boolean {
        try {
            job?.ensureActive()
            mangas[i].updateInfo()
            withContext(Dispatchers.Main) {
                mangasInfos.add(MangaListElementContainer(
                    mangas[i].originalName,
                    mangas[i].author,
                    mangas[i].library.getURL(),
                    mangas[i].cover
                    ))
                adapter?.notifyDataSetChanged()
            }
        } catch (ex : MangaJetException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
            }
            return false
        }
        return true
    }

    // Function which will load info about each manga from "manga names"
    private suspend fun addElementsToMangas(
        queryString : String,
        source : Librarian.LibraryName
    ) {
        var fuckUperDetekt = ""
        try {
            val libsMangas = Librarian.getLibrary(source)!!
                .searchManga(queryString, SEARCH_AMOUNT, 0)

            if (libsMangas.isEmpty()) {
                return
            }
            
            for (i in libsMangas.indices) {
                mangas.add(libsMangas[i])
                if (!uploadMangaIntoArray(mangas.size - 1))
                    mangas.removeAt(mangas.size - 1)
            }

        } catch (ex : MangaJetException) {
            fuckUperDetekt += ""
        }
    }

    // Function which will update all sources flags in 'chosenLibraries', where will be searching
    fun updateLibsSources(fragmentManager : FragmentManager?) {
        val librariesNames = Array(allLibraries.size) { i -> allLibraries[i].resource}
        val choseResourceDialog = SearchSetSourcesDialog(librariesNames, chosenLibraries)
        if (fragmentManager != null) {
            choseResourceDialog.show(fragmentManager, "Choose resource dialog")
            if (choseResourceDialog.wasSelected) {
                for (i in choseResourceDialog.mCheckedItems.indices)
                    chosenLibraries[i] = choseResourceDialog.mCheckedItems[i]
            }
        }
    }

    // Function which will destroy and clear all fields and threads
    private fun destroyAll() {
        job?.cancel()
        adapter?.clear()
        mangas.clear()
        mangasInfos.clear()
    }

    // Function which will async load mangas info
    fun initMangas(adapterNew: MangaListAdapter, binding : SearchFragmentBinding, queryString : String) {
        binding.progressBar.show()
        binding.noResultLayout.visibility = View.INVISIBLE

        adapter = adapterNew

        destroyAll()
        job = GlobalScope.launch(Dispatchers.IO) {
            for (i in allLibraries.indices) {
                if (chosenLibraries[i])
                    addElementsToMangas(queryString, allLibraries[i])
            }

            withContext(Dispatchers.Main) {
                if (mangas.size == 0)
                    binding.noResultLayout.visibility = View.VISIBLE
                binding.progressBar.hide()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        destroyAll()
    }
}
