package com.mangajet.mangajet

import android.app.Application
import android.content.Context
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.StorageManager
import com.mangajet.mangajet.data.WebAccessor

// Insertion point for our app
class MangaJetApp : Application() {
    companion object
    {
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        // We need to use WebAccessor, Librarian and StorageManager here,
        // so they will be initialized at known time
        context = getApplicationContext();
        WebAccessor.hashCode()
        Librarian.hashCode()
        StorageManager.hashCode()
    }
}
