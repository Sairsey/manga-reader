package com.mangajet.mangajet.ui.settings.options

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.fragment.app.DialogFragment
import com.mangajet.mangajet.log.Logger


// Theme picker dialog class
class ThemePickerDialog : DialogFragment() {
    companion object {
        const val DAY = 0           // Day theme
        const val NIGHT = 1         // Night theme
        const val SYSTEM_THEME = 2  // System match theme
    }

    // Theme names for dialog list
    private val themesNames = arrayOf("Day", "Night", "Automaticly")

    // Function will set chosen theme in shared preferences
    private fun setAppTheme(theme : Int) {
        val sp = getDefaultSharedPreferences(context)
        val editor = sp.edit()
        Logger.log("Theme changed to " + themesNames[theme])
        editor.putInt("THEME", theme)
        editor.commit()
        AppCompatDelegate.setDefaultNightMode(theme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            Logger.log("Choose theme in Settings opened")
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Choose theme")
                .setItems(themesNames) { dialog, which ->
                    when(which) {
                        DAY -> setAppTheme(MODE_NIGHT_NO)
                        NIGHT -> setAppTheme(MODE_NIGHT_YES)
                        SYSTEM_THEME -> setAppTheme(MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
