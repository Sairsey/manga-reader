package com.mangajet.mangajet

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.StorageManager
import com.mangajet.mangajet.data.WebAccessor

// Insertion point for our app
class MangaJetApp : Application() {
    companion object
    {
        var context: Context? = null
    }

    override fun onCreate() {
        val sp = getDefaultSharedPreferences(this)
        AppCompatDelegate.setDefaultNightMode(sp.getInt("THEME",
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        ))

        super.onCreate()
        // We need to use WebAccessor, Librarian and StorageManager here,
        // so they will be initialized at known time
        context = getApplicationContext();
        WebAccessor.hashCode()
        Librarian.hashCode()
        StorageManager.hashCode()

        // on start it is good idea to load all cookies and Authentication from Librarian
        try {
            Librarian.setLibrariesJSON(
                StorageManager.loadString(Librarian.path, StorageManager.FileType.LibraryInfo))
        }
        catch (ex: MangaJetException) {
            // in this case we can just skip, because if file not found it isnt a big deal.
        }
    }
}
