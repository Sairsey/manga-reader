package com.mangajet.mangajet.mangareader

import android.app.Dialog
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment

class ChangeMangaReaderFormatDialog(mMangaReaderViewModel: MangaReaderViewModel) : DialogFragment() {
    // Theme names for dialog list
    private val themesNames = arrayOf("Left-to-right", "Manhwa", "Right-to-left")
    private val mMangaReaderViewModelReference = mMangaReaderViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = android.app.AlertDialog.Builder(it)
            builder.setTitle("Choose theme")
                .setItems(themesNames) { dialog, which ->
                    mMangaReaderViewModelReference.currentReaderFormat = which
                    mMangaReaderViewModelReference.redrawMangaReader()
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
