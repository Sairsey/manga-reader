package com.mangajet.mangajet.ui.search

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class SearchSetSourcesDialog(sourcesNames : Array<String>, checkedItems : BooleanArray) : DialogFragment() {
    private val mSourcesNames = sourcesNames
    val mCheckedItems = checkedItems

    var wasSelected = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        wasSelected = false

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Choose resources")
                .setMultiChoiceItems(mSourcesNames, mCheckedItems) {
                        dialog, which, isChecked ->
                    mCheckedItems[which] = isChecked
                }
                .setPositiveButton("Set sources"
                ) {
                        dialog, id ->
                    wasSelected = true
                }
                .setNegativeButton("Cancel") {
                        dialog, _ ->  dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
