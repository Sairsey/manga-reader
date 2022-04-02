package com.mangajet.mangajet.mangareader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.mangajet.mangajet.R
import com.mangajet.mangajet.aboutmanga.AboutMangaViewModel

// Class which represents "Manga Reader" Activity
class MangaReaderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mangaReaderViewmodel = ViewModelProvider(this).get(MangaReaderViewModel::class.java)

        mangaReaderViewmodel.initMangaData(intent)
        setContentView(R.layout.manga_reader_activity)

        val text = findViewById<TextView>(R.id.sample_texr)

        text.text = "Chapter " + (mangaReaderViewmodel.manga.lastViewedChapter + 1)

    }
}
