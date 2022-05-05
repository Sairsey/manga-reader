package com.mangajet.mangajet

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.StorageManager
import com.mangajet.mangajet.data.WebAccessor

// Insertion point for our app
class MangaJetApp : Application() {
    companion object
    {
        var context: Context? = null
        var currentManga : Manga? = null // used for fast sending data between activities without json

        // Fields which provide tags search
        const val ABOUT_MANGA_CALLBACK = 1

        var tagSearchInfo : Pair<String, String>? = null
    }


    enum class OverrideFragmentInMainActivity(val mainFragmentId : Int) {
        FragmentSearch(R.id.navigation_search),
        FragmentHistory(R.id.navigation_history),
        FragmentForYou(R.id.navigation_for_you),
        FragmentFavorite(R.id.navigation_favourite),
        FragmentSettings(R.id.navigation_settings);

        var needToBeOpened = false
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
        StorageManager.hashCode()
    }
}
