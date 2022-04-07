package com.mangajet.mangajet.ui.search

import android.preference.PreferenceManager
import android.view.View
import android.webkit.WebView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.MangaJetApp.Companion.context
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.databinding.SearchFragmentBinding

// Class which represents "Search" ViewModel
class SearchViewModel : ViewModel() {
    companion object {
        const val SEARCH_AMOUNT = 20        // Amount of searchable mangas
    }
    val mangasNames = ArrayList<String>()           // mangas names for list
    var mangas : ArrayList<Manga> = arrayListOf()   // mangas for "AboutManga" activity
    var job : Job? = null                           // Async job for searching and uploading
    var adapter : ArrayAdapter<String>? = null      // adapter for list

    // Selected resources
    var allLibraries = arrayOf(
        Librarian.LibraryName.Readmanga,
        Librarian.LibraryName.Mangachan,
        //Librarian.LibraryName.Mangalib,
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
    private suspend fun uploadMangaIntoArray(i : Int) {
        try {
            mangas[i].updateInfo()
            withContext(Dispatchers.Main) {
                mangasNames.add(mangas[i].originalName + "("
                        + mangas[i].library.getURL() + ")"
                )
                adapter?.notifyDataSetChanged()
            }
        } catch (ex : MangaJetException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Function which will load info about each manga from "manga names"
    private suspend fun addElementsToMangas(
        queryString : String,
        source : Librarian.LibraryName
    ) {
        try {
            val libsMangas = Librarian.getLibrary(source)!!
                .searchManga(queryString, SEARCH_AMOUNT, 0)

            if (libsMangas.isEmpty()) {
                return
            }

            for (i in libsMangas.indices) {
                mangas.add(libsMangas[i])
                uploadMangaIntoArray(mangas.size - 1)
            }

        } catch (ex : MangaJetException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
            }
        }
    }

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
        mangasNames.clear()
    }

    // Function which will async load mangas info
    fun initMangas(adapterNew: ArrayAdapter<String>, binding : SearchFragmentBinding, queryString : String) {
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
