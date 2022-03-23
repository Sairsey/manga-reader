package com.mangajet.mangajet.ui.settings.options

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.fragment.app.DialogFragment

class ThemePickerDialog : DialogFragment() {
    private val themesNames = arrayOf("Day", "Night", "Automaticly")

    private fun setAppTheme(theme : Int) {
        val sp = getDefaultSharedPreferences(context)
        val editor = sp.edit()
        editor.putInt("THEME", theme)
        editor.commit()
        AppCompatDelegate.setDefaultNightMode(theme)
    }

    @SuppressLint("CommitPrefEdits")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Choose theme")
                .setItems(themesNames) { dialog, which ->
                    when(which) {
                        Companion.DAY -> setAppTheme(MODE_NIGHT_NO)
                        Companion.NIGHT -> setAppTheme(MODE_NIGHT_YES)
                        Companion.SYSTEM_THEME -> setAppTheme(MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val DAY = 0
        const val NIGHT = 1
        const val SYSTEM_THEME = 2
    }
}
