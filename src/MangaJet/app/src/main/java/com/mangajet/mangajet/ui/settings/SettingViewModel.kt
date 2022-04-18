package com.mangajet.mangajet.ui.settings

import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.BuildConfig

// Class which represents "Settings" ViewModel
class SettingViewModel : ViewModel() {
    // only name settings
    val dataOptionsNames =
        // developer version
        if (BuildConfig.VERSION_NAME.endsWith("dev"))
            listOf("Authorizations", "Theme picker", "Cache", "Backup and recovery",
                "Storage path", "About app", "!TEST!")
        // release version
        else
            listOf("Authorizations", "Theme picker", "Cache", "Backup and recovery",
                "Storage path", "About app")

}
