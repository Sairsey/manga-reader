package com.mangajet.mangajet.aboutmanga

import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.mangajet.mangajet.MangaJetApp.Companion.context
import com.mangajet.mangajet.aboutmanga.mangaChaptersFragment.MangaChaptersFragment
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// Class which represents "About manga" View Model
class AboutMangaViewModel : ViewModel() {
    // ViewModel initialization flag
    var isInited = false
    // Manga, which will be initialised on activity start
    lateinit var manga : Manga

    // init async job
    var job : Job? = null
    var adapter : MangaChaptersFragment.ChapterListAdapter? = null
    var progressIndicator : LinearProgressIndicator? = null

    // Function will
    fun initMangaData() {
        if (!isInited) {
            job = GlobalScope.launch(Dispatchers.IO) {
                try {
                    manga.updateChapters()
                } catch (ex: MangaJetException) {
                    // chapters from json or no chapters at all.
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context,
                            "Can't upload chapters or manga doesn't contains them",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                withContext(Dispatchers.Main) {
                    progressIndicator?.hide()
                }
                isInited = true
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        isInited = false
    }
}
