package com.mangajet.mangajet.mangareader

import android.app.Dialog
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment

// Class which will call dialog for chose manga reader format
class ChangeMangaReaderFormatDialog(mMangaReaderViewModel: MangaReaderViewModel) : DialogFragment() {
    // Theme names for dialog list
    private val formatNames = arrayOf("Left-to-right", "Manhwa", "Right-to-left")
    // Reference on MangaReaderViewModel with reader data
    private val mMangaReaderViewModelReference = mMangaReaderViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = android.app.AlertDialog.Builder(it)
            builder.setTitle("Choose format")
                .setItems(formatNames) { dialog, which ->
                    mMangaReaderViewModelReference.currentReaderFormat = which
                    mMangaReaderViewModelReference.redrawMangaReader()
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
