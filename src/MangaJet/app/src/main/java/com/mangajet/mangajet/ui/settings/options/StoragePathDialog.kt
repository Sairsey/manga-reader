package com.mangajet.mangajet.ui.settings.options

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.mangajet.mangajet.data.StorageManager
import com.mangajet.mangajet.log.Logger

// Dialog with storage directory
class StoragePathDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Logger.log("Storage path dialog in Settings opened")
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Choose Storage path")
                .setMessage(StorageManager.storageDirectory)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
