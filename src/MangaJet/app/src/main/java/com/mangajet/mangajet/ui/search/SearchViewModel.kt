package com.mangajet.mangajet.ui.search

import android.database.Cursor
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.MangaJetApp.Companion.context
import com.mangajet.mangajet.MangaListAdapter
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.databinding.SearchFragmentBinding
import com.mangajet.mangajet.log.Logger
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Class which represents "Search" ViewModel
@Suppress("TooManyFunctions")
class SearchViewModel : ViewModel() {
    companion object {
        const val  RANDOM_POPULAR_MANGA_COUNT = 5
    }

    var mangas : ArrayList<Manga> = arrayListOf()           // mangas for "AboutManga" activity
    var job : Job? = null                                   // Async job for searching and uploading
    var adapter : MangaListAdapter? = null                  // adapter for list
    var hintJob : Job? = null                               // job for hint loading

    // Mutex data sync protection
    private var searchListMutex = true

    // Data for suggestions list in search view
    var searchSuggestionsCursor : Cursor? = null
    var suggestionsStrings : Array<String>? = null

    // Function which will upload manga into mangas array and catch exceptions
    private suspend fun uploadMangaIntoArray(manga : Manga) {
        try {
            job?.ensureActive()
            manga.updateInfo()
            job?.ensureActive()
            withContext(Dispatchers.Main) {
                synchronized(searchListMutex) {
                    mangas.add(manga)
                }
                adapter?.notifyDataSetChanged()
            }
        } catch (ex : MangaJetException) {
            // only thing which may fail here is updateInfo
            // which will be deleted if we return false
            Logger.log("Catch MJE in uploadMangaInto Array: " + ex.message, Logger.Lvl.WARNING)
        }
    }

    // Function which will find mangas by query
    private suspend fun searchMangasByQuery(
        queryString : String,
        source : Librarian.LibraryName
    ) : Array<Manga> {
        try {
            val libsMangas = Librarian.getLibrary(source)!!
                .searchManga(queryString, Librarian.settings.MANGA_SEARCH_AMOUNT, 0)

            return libsMangas
        } catch (ex : MangaJetException) {
            Logger.log("Catch MJE while trying to load info about some manga while searching"
                    + ex.message, Logger.Lvl.WARNING)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
            }
        }

        return arrayOf()
    }

    // Function which will find mangas by Tags
    private suspend fun searchMangasByTags(
        tags : Array<String>,
        source : Librarian.LibraryName
    ) : Array<Manga> {
        try {
            val libsMangas = Librarian.getLibrary(source)!!
                .searchMangaByTags(tags, Librarian.settings.MANGA_SEARCH_AMOUNT, 0)

            return libsMangas
        } catch (ex : MangaJetException) {
            Logger.log("Catch MJE while trying to load info about some manga while searching"
                    + ex.message, Logger.Lvl.WARNING)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
            }
        }

        return arrayOf()
    }

    // Function which will load info about each manga from "manga names"
    private suspend fun addElementsToMangas(
        libsMangas : Array<Manga>
    ) {
        try {
            if (libsMangas.isEmpty()) {
                return
            }
            
            for (i in libsMangas.indices) {
                job?.ensureActive()
                uploadMangaIntoArray(libsMangas[i])
            }

        } catch (ex : MangaJetException) {
            Logger.log("Catch MJE while trying to load info about some manga while adding to adapter"
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
        synchronized(searchListMutex) {
            mangas.clear()
        }
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

        job?.cancel()

        job = viewModelScope.launch(Dispatchers.IO) {
            for (i in Librarian.LibraryName.values().indices) {
                if (Librarian.settings.CHOSEN_RESOURCES[i]) {
                    val mangas = searchMangasByQuery(queryString, Librarian.LibraryName.values()[i])
                    addElementsToMangas(mangas)
                }
            }

            withContext(Dispatchers.Main) {
                synchronized(searchListMutex) {
                    if (mangas.size == 0)
                        binding.noResultLayout.visibility = View.VISIBLE
                    binding.progressBar.hide()
                }
            }
        }
    }

    fun setSomeRandomPopularHint(binding: SearchFragmentBinding)  {
        hintJob?.cancel()
        hintJob = viewModelScope.launch(Dispatchers.IO) {
            // choose random resource
            var arrayOfResources = arrayListOf<Int>()
            for (i in Librarian.LibraryName.values().indices)
                if (Librarian.settings.CHOSEN_RESOURCES[i])
                    arrayOfResources.add(i)
            val randomResource = (arrayOfResources.indices).random()

            // choose random manga
            val popularMangas =
                Librarian.getLibrary(Librarian.LibraryName.values()[randomResource])!!.
                getPopularManga(RANDOM_POPULAR_MANGA_COUNT)
            val randomManga = popularMangas[(popularMangas.indices).random()]
            randomManga.updateInfo()
            val hint = if (popularMangas[(popularMangas.indices).random()].originalName != "")
                randomManga.originalName
            else
                randomManga.russianName

            withContext(Dispatchers.Main) {
                binding.searchView.queryHint = context!!.resources!!.
                    getString(R.string.for_example) + " " + hint
            }
        }
    }

    fun initSuggestionData() {
        // tmp data filler
        suggestionsStrings = arrayOf()
    }

    fun initSearchListView(adapterNew: MangaListAdapter, binding : SearchFragmentBinding) {
        // show load process
        binding.progressBar.show()
        binding.noResultLayout.visibility = View.INVISIBLE

        // init adapter
        adapter =  adapterNew

        // init info for search
        val source = Librarian.LibraryName.from(MangaJetApp.tagSearchInfo!!.second)
        val tag = arrayOf<String>(MangaJetApp.tagSearchInfo!!.first)

        job?.cancel()

        job = viewModelScope.launch(Dispatchers.IO) {
            val mangas = searchMangasByTags(tag, source)
            addElementsToMangas(mangas)

            withContext(Dispatchers.Main) {
                synchronized(searchListMutex) {
                    if (mangas.isEmpty())
                        binding.noResultLayout.visibility = View.VISIBLE
                    binding.progressBar.hide()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        destroyAll()
    }
}
