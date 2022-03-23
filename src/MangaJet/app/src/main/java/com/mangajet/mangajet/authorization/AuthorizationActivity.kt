package com.mangajet.mangajet.authorization

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.preference.PreferenceManager.getDefaultSharedPreferences
import com.mangajet.mangajet.R

// Class which represents Authorization Activity
class AuthorizationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val sp = getDefaultSharedPreferences(this)
        val theme = sp.getInt("THEME", R.style.Theme_MangaJet)
        setTheme(theme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization_second)
    }
}
