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
import com.mangajet.mangajet.data.StorageManager

class StoragePathDialog : DialogFragment() {
    @SuppressLint("CommitPrefEdits")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Choose Storage path")
                .setMessage(StorageManager.storageDirectory)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val DAY = 0
        const val NIGHT = 1
        const val SYSTEM_THEME = 2
    }
}
