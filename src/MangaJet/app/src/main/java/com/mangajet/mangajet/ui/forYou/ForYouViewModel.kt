package com.mangajet.mangajet.ui.forYou

import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.MangaListAdapter
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.databinding.ForYouFragmentBinding
import com.mangajet.mangajet.log.Logger
import com.mangajet.mangajet.ui.search.SearchSetSourcesDialog
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
    var adapter : MangaListAdapter? = null          // adapter for list
    var binding : ForYouFragmentBinding? = null     // binding with all UI elements

    // Function which will update all sources flags in 'chosenLibraries', where will be searching
    fun updateLibsSources(fragmentManager : FragmentManager?) {
        val librariesNames = Array(Librarian.LibraryName.values().size) { i ->
            Librarian.LibraryName.values()[i].resource}
        val choseResourceDialog = ForYouSetSourcesDialog(
            librariesNames,
            Librarian.settings.CHOSEN_FOR_YOU_RESOURCES,
            this)
        if (fragmentManager != null) {
            choseResourceDialog.show(fragmentManager, "Choose resource dialog")
        }
    }

    // Function which will async load mangas info
    fun initMangas(adapterNew: MangaListAdapter, force : Boolean = false) {
        if (!isInited || force) {
            binding!!.loadRecommendationsIndicator.visibility = View.VISIBLE
            binding!!.noResultLayout.visibility = View.INVISIBLE
            binding!!.forYouListView.visibility = View.INVISIBLE

            if (force)
                mangas.clear()

            isInited = true
            adapter = adapterNew
            job = GlobalScope.launch(Dispatchers.IO) {
                val resourcesArrayList = arrayListOf<Librarian.LibraryName>()
                for (i in Librarian.LibraryName.values().indices)
                    if (Librarian.settings.CHOSEN_FOR_YOU_RESOURCES[i])
                        resourcesArrayList.add(Librarian.LibraryName.values()[i])
                var recomMangas = Librarian.getRecommendedMangas(resourcesArrayList)
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
                    binding!!.loadRecommendationsIndicator.visibility = View.INVISIBLE
                    binding!!.forYouListView.visibility = View.VISIBLE
                    if (adapter?.isEmpty == true)
                        binding!!.noResultLayout.visibility = View.VISIBLE
                }
            }
        }
        else {
            if (adapter?.isEmpty == true)
                binding!!.noResultLayout.visibility = View.VISIBLE
        }
    }

    override fun onCleared() {
        super.onCleared()
        isInited = false
        mangas.clear()
    }
}
