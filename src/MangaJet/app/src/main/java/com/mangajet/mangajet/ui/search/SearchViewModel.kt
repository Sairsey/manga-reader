package com.mangajet.mangajet.ui.search

import android.view.View
import android.webkit.WebView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.MangaJetApp.Companion.context
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.mangajet.mangajet.R
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

    // Function which will upload manga into mangas array and catch exceptions
    private suspend fun uploadMangaIntoArray(i : Int) {
        try {
            mangas[i].updateInfo()
            withContext(Dispatchers.Main) {
                mangasNames.add(mangas[i].originalName)
                adapter?.notifyDataSetChanged()
            }
        } catch (ex : MangaJetException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Function which will load info about each manga from "manga names"
    private suspend fun addElementsToMangas(binding : SearchFragmentBinding, queryString : String) {
        try {
            val libsMangas = Librarian.getLibrary(Librarian.LibraryName.Readmanga)!!
                .searchManga(queryString, SEARCH_AMOUNT, 0)

            if (libsMangas.isEmpty()) {
                withContext(Dispatchers.Main) {
                    binding.noResultLayout.visibility = View.VISIBLE
                    binding.progressBar.hide()
                }
                return
            }

            for (i in libsMangas.indices) {
                mangas.add(libsMangas[i])
                uploadMangaIntoArray(i)
            }

            withContext(Dispatchers.Main) {
                binding.progressBar.hide()
                if (mangas.size == 0)
                    binding.noResultLayout.visibility = View.VISIBLE
            }
        } catch (ex : MangaJetException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
                binding.noResultLayout.visibility = View.VISIBLE
                binding.progressBar.hide()
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
        adapter = adapterNew

        destroyAll()
        job = GlobalScope.launch(Dispatchers.IO) {
            addElementsToMangas(binding, queryString)
        }
    }

    override fun onCleared() {
        super.onCleared()
        destroyAll()
    }
}
