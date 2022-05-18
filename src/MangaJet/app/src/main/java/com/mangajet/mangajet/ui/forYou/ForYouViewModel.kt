package com.mangajet.mangajet.ui.forYou

import android.view.View
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.MangaListAdapter
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.databinding.ForYouFragmentBinding
import com.mangajet.mangajet.databinding.MangaChaptersFragmentBinding
import com.mangajet.mangajet.log.Logger
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

    // Function which will load info about each manga from "manga names"
    suspend fun addElementsToMangas() {
        val mangasSearchWords = listOf("Гуль", "Берсерк", "Onepunchman")
        for (name in mangasSearchWords) {
            var manga : Manga
            try {
                manga = Librarian.getLibrary(Librarian.LibraryName.Mangachan)!!.searchManga(name)[2]
            }
            catch (ex: MangaJetException) {
                Logger.log("Catch MJE while trying to load info about manga with " + name +
                        " name: " + ex.message, Logger.Lvl.WARNING)
                // nothing too tragic. If manga not found we can just skip it
                continue
            }

            try {
                manga.updateInfo()
            }
            catch (ex: MangaJetException) {
                Logger.log("Catch MJE while trying to update info in " + manga.id +
                    " : " + ex.message, Logger.Lvl.WARNING)
                // This not so tragic.
                // just continue
                continue
            }

            withContext (Dispatchers.Main) {
                mangas.add(manga)
                adapter?.notifyDataSetChanged()
            }
        }
    }

    // Function which will async load mangas info
    fun initMangas(adapterNew: MangaListAdapter, binding: ForYouFragmentBinding) {
        if (!isInited) {
            binding.loadRecommendationsIndicator.visibility = View.VISIBLE
            binding.noResultLayout.visibility = View.INVISIBLE

            isInited = true
            adapter = adapterNew
            job = GlobalScope.launch(Dispatchers.IO) {
                var recomMangas : ArrayList<Manga>
                try {
                    recomMangas = Librarian.getRecommendedMangas()
                }
                catch (ex: MangaJetException) {
                    Logger.log(ex.message.toString())
                    return@launch
                }
                for (manga in recomMangas) {
                    try {
                        manga.updateInfo()
                        withContext(Dispatchers.Main) {
                            mangas.add(manga)
                        }
                    }
                    catch (ex : MangaJetException) {
                        Logger.log(ex.message.toString())
                    }
                }
                withContext(Dispatchers.Main) {
                    adapter?.notifyDataSetChanged()
                    binding.loadRecommendationsIndicator.visibility = View.INVISIBLE
                    if (adapter?.isEmpty == true)
                        binding.noResultLayout.visibility = View.VISIBLE
                }
            }
        }
        else {
            if (adapter?.isEmpty == true)
                binding.noResultLayout.visibility = View.VISIBLE
        }
    }

    override fun onCleared() {
        super.onCleared()
        isInited = false
        mangas.clear()
    }
}
