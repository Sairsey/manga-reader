package com.mangajet.mangajet.ui.settings

import androidx.lifecycle.ViewModel

// Class which represents "Settings" ViewModel
class SettingViewModel : ViewModel() {
    // only name settings
    val dataOptionsNames = listOf("Authorizations", "Theme picker", "Cache", "Backup and recovery",
        "Storage path", "About app")
}
