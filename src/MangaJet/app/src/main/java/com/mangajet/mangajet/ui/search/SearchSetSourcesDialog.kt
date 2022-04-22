package com.mangajet.mangajet.ui.search

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.Settings
import com.mangajet.mangajet.log.Logger
import org.json.JSONException

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
            Logger.log("Set resources dialog opened")
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Choose resources")
                .setMultiChoiceItems(mSourcesNames, mCheckedItems) {
                        dialog, which, isChecked ->
                    Logger.log("Source " + mSourcesNames[which] + " was selected")
                    mCheckedItems[which] = isChecked
                }
                .setPositiveButton("Set sources"
                ) {
                        dialog, id ->
                    wasSelected = true
                    // Save new settings
                    try {
                        Settings.saveState()
                    }
                    catch (ex : MangaJetException){
                        Logger.log("Catch MJE while trying to save setting.json: "
                                + ex.message, Logger.Lvl.WARNING)
                        // Sad, but really doesn't matter
                    }
                    catch (ex : JSONException){
                        Logger.log("Catch JSONException while trying to save setting.json: "
                                + ex.message, Logger.Lvl.WARNING)
                        // Sad, but really doesn't matter
                    }
                }
                .setNegativeButton("Cancel") {
                        dialog, _ ->  dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
