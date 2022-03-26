package com.mangajet.mangajet.ui.settings

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.aboutmanga.AboutMangaActivity

// Class which represents "Settings" ViewModel
class SettingViewModel : ViewModel() {
    // only name settings
    val dataOptionsNames = listOf("Authorizations", "Theme picker", "Cache", "Backup and recovery",
        "Storage path", "About app")
}
