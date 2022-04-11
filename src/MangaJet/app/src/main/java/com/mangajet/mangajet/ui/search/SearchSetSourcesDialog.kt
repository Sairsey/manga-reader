package com.mangajet.mangajet.ui.search

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

// Class for creating dialog for select search sources
class SearchSetSourcesDialog(sourcesNames : Array<String>, checkedItems : BooleanArray) : DialogFragment() {
    // all manga sources names
    private val mSourcesNames = sourcesNames
    // flags for selected sources
    val mCheckedItems = checkedItems
    // flag of positive button selected in dialog
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
