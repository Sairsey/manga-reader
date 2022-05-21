package com.mangajet.mangajet.ui.settings.options

import android.app.AlertDialog
import android.app.Dialog
import android.app.UiModeManager.MODE_NIGHT_NO
import android.app.UiModeManager.MODE_NIGHT_YES
import android.os.Bundle
import android.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.fragment.app.DialogFragment
import com.mangajet.mangajet.data.Settings
import com.mangajet.mangajet.log.Logger
// Theme picker dialog class

class ThemePickerDialog : DialogFragment() {
    // Theme names for dialog list
    private val themesNames = arrayOf("Day", "Night", "Match system")
    var themePickedIdFromDialog  = -1
    // Function will set chosen theme in shared preferences
    public fun setAppTheme(theme : Int, dialogListElementId : Int) {
        val sp = getDefaultSharedPreferences(context)
        val editor = sp.edit()
        Logger.log("Theme changed to " + themesNames[dialogListElementId])
        Settings.THEME_PICKED_ID = dialogListElementId
        Settings.saveState()
        editor.putInt("THEME", theme)
        editor.commit()
        AppCompatDelegate.setDefaultNightMode(theme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            Logger.log("Choose theme in Settings opened")
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Choose theme").setPositiveButton("OK"){
                dialog,_ ->
                dialog.dismiss()
            }
                .setSingleChoiceItems(themesNames, Settings.THEME_PICKED_ID) { dialog, which ->
                    themePickedIdFromDialog = which
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDestroy() {
        super.onDestroy()
        when(themePickedIdFromDialog) {
            Settings.DAY -> setAppTheme(MODE_NIGHT_NO, Settings.DAY)
            Settings.NIGHT -> setAppTheme(MODE_NIGHT_YES, Settings.NIGHT)
            Settings.SYSTEM_THEME -> setAppTheme(MODE_NIGHT_FOLLOW_SYSTEM, Settings.SYSTEM_THEME)
        }
    }
}
