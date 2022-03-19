package com.mangajet.mangajet

import android.app.Application
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.WebAccessor

// Insertion point for our app
class MangaJetApp: Application() {
    override fun onCreate() {
        super.onCreate()

        // We need to use WebAccessor and Librarian here,
        // so they will be initialized at known time
        val headers = mapOf("User-Agent" to
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/70.0.3538.77 Safari/537.36")
        val s = WebAccessor.getTextSync("https://mangalib.me/kimetsu-no-yaiba", headers)

        Librarian.getLibrary(Librarian.LibraryName.Mangalib)
    }
}
