package com.mangajet.mangajet.ui.search

import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.MangaJetApp.Companion.context
import com.mangajet.mangajet.MangaListAdapter
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.databinding.SearchFragmentBinding
import com.mangajet.mangajet.log.Logger
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Class which represents "Search" ViewModel
class SearchViewModel : ViewModel() {

    var mangas : ArrayList<Manga> = arrayListOf()   // mangas for "AboutManga" activity
    var job : Job? = null                           // Async job for searching and uploading
    var adapter : MangaListAdapter? = null          // adapter for list

    // Function which will upload manga into mangas array and catch exceptions
    private suspend fun uploadMangaIntoArray(manga : Manga) {
        try {
            job?.ensureActive()
            manga.updateInfo()
            job?.ensureActive()
            withContext(Dispatchers.Main) {
                mangas.add(manga)
                adapter?.notifyDataSetChanged()
            }
        } catch (ex : MangaJetException) {
            // only thing which may fail here is updateInfo
            // which will be deleted if we return false
            Logger.log("Catch MJE in uploadMangaInto Array: " + ex.message, Logger.Lvl.WARNING)
        }
    }

    // Function which will load info about each manga from "manga names"
    private suspend fun addElementsToMangas(
        queryString : String,
        source : Librarian.LibraryName
    ) {
        try {
            val libsMangas = Librarian.getLibrary(source)!!
                .searchManga(queryString, Librarian.settings.MANGA_SEARCH_AMOUNT, 0)

            if (libsMangas.isEmpty()) {
                return
            }
            
            for (i in libsMangas.indices) {
                job?.ensureActive()
                uploadMangaIntoArray(libsMangas[i])
            }

        } catch (ex : MangaJetException) {
            Logger.log("Catch MJE while trying to load info about some manga while searching"
                    + ex.message, Logger.Lvl.WARNING)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Function which will update all sources flags in 'chosenLibraries', where will be searching
    fun updateLibsSources(fragmentManager : FragmentManager?) {
        val librariesNames = Array(Librarian.LibraryName.values().size) { i ->
            Librarian.LibraryName.values()[i].resource}
        val choseResourceDialog = SearchSetSourcesDialog(librariesNames, Librarian.settings.CHOSEN_RESOURCES)
        if (fragmentManager != null) {
            choseResourceDialog.show(fragmentManager, "Choose resource dialog")
            if (choseResourceDialog.wasSelected) {
                for (i in choseResourceDialog.mCheckedItems.indices)
                    Librarian.settings.CHOSEN_RESOURCES[i] = choseResourceDialog.mCheckedItems[i]
            }
        }
    }

    // Function which will destroy and clear all fields and threads
    private fun destroyAll() {
        job?.cancel()
        mangas.clear()
    }

    // Function which will async load mangas info
    fun initMangas(adapterNew: MangaListAdapter, binding : SearchFragmentBinding, queryString : String) {
        binding.progressBar.show()
        binding.noResultLayout.visibility = View.INVISIBLE

        adapter = adapterNew

        //All libraries
        var sources = ""
        for(i in Librarian.LibraryName.values().indices)
            if(Librarian.settings.CHOSEN_RESOURCES[i])
                sources += Librarian.LibraryName.values()[i].resource + " "
        Logger.log("Search \"$queryString\" with these sources: $sources")

        destroyAll()
        job = GlobalScope.launch(Dispatchers.IO) {
            for (i in Librarian.LibraryName.values().indices) {
                if (Librarian.settings.CHOSEN_RESOURCES[i])
                    addElementsToMangas(queryString, Librarian.LibraryName.values()[i])
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
