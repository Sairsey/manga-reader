package com.mangajet.mangajet

import android.app.Application
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.StorageManager
import com.mangajet.mangajet.data.WebAccessor

// Insertion point for our app
class MangaJetApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // We need to use WebAccessor and Librarian here,
        // so they will be initialized at known time
        WebAccessor.hashCode()
        Librarian.hashCode()
        StorageManager.hashCode()
    }
}
