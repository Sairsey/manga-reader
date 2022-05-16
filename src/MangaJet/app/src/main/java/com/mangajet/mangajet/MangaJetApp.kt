package com.mangajet.mangajet

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.StorageManager
import com.mangajet.mangajet.data.WebAccessor
import com.mangajet.mangajet.service.UpdateReceiver


// Insertion point for our app
class MangaJetApp : Application() {
    companion object
    {
        var context: Context? = null
        var currentManga : Manga? = null // used for fast sending data between activities without json
        // Fields which provide tags search
        const val ABOUT_MANGA_CALLBACK = 1
        var recv = UpdateReceiver()

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

    // function which will create our NotificationChannel
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(getString(R.string.channel_id), name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate() {
        val sp = getDefaultSharedPreferences(this)
        AppCompatDelegate.setDefaultNightMode(sp.getInt("THEME",
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        ))

        super.onCreate()
        // We need to use WebAccessor, Librarian and StorageManager here,
        // so they will be initialized at known time
        context = getApplicationContext()
        WebAccessor.hashCode()
        StorageManager.hashCode()
        // create notification channel
        createNotificationChannel()
    }
}
