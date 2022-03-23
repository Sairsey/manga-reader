package com.mangajet.mangajet.ui.settings

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.aboutmanga.AboutMangaActivity

// Class which represents "Settings" ViewModel
class SettingViewModel : ViewModel() {
    // only 'Data options' layout settings
    val dataOptionsNames = listOf("Cache", "Backup and recovery")
}
