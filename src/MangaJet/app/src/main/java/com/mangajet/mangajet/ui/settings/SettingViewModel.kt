package com.mangajet.mangajet.ui.settings

import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.BuildConfig
import com.mangajet.mangajet.R

class SettingListElement(name : String, icon : Int) {
    val mName = name
    val mIcon = icon
}

// Class which represents "Settings" ViewModel
class SettingViewModel : ViewModel() {
    val settingsElements =
        if (BuildConfig.VERSION_NAME.endsWith("dev"))
            arrayListOf(
                SettingListElement("Authorizations", R.drawable.ic_authorization),
                SettingListElement("Theme picker", R.drawable.ic_theme_picker),
                SettingListElement("Storage management", R.drawable.ic_storage),
                SettingListElement("Storage path", R.drawable.ic_storage_path),
                SettingListElement("Extra settings", R.drawable.ic_extra_settings),
                SettingListElement("About app",R.drawable.ic_about_app),
                SettingListElement("!TEST!", R.drawable.ic_test))
        // release version
        else
            arrayListOf(
                SettingListElement("Authorizations", R.drawable.ic_authorization),
                SettingListElement("Theme picker", R.drawable.ic_theme_picker),
                SettingListElement("Storage management", R.drawable.ic_storage),
                SettingListElement("Storage path", R.drawable.ic_storage_path),
                SettingListElement("Extra settings", R.drawable.ic_extra_settings),
                SettingListElement("About app",R.drawable.ic_about_app))
}
