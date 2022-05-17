package com.mangajet.mangajet.ui.forYou

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.Settings
import com.mangajet.mangajet.log.Logger
import org.json.JSONException

class ForYouSetSourcesDialog(
    private val mSourcesNames : Array<String>,      // names of selected sources
    private val mCheckedItems : BooleanArray,       // flags for selected sources
    private val forYouViewModel: ForYouViewModel    // reference of parent view model
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
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
                    forYouViewModel.initMangas(forYouViewModel.adapter!!, true)
                }
                .setNegativeButton("Cancel") {
                        dialog, _ ->  dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
