package com.mangajet.mangajet.ui.settings.options

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.mangajet.mangajet.data.StorageManager

class ThemePickerDialog : DialogFragment() {
    private val themesNames = arrayOf("Blue", "Orange", "Pink", "Green")

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Choose theme")
                .setItems(themesNames) { dialog, which ->

                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
