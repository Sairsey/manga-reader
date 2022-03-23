package com.mangajet.mangajet.ui.settings.options

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.mangajet.mangajet.MainActivity
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.StorageManager
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow


class ClearCacheDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Delete cache")
                .setMessage("Are you sure about deleting cache?")
                .setCancelable(true)
                .setPositiveButton("Delete") { dialog, id ->
                    StorageManager.removeDirectory()
                    (activity as CacheSettingActivity?)?.fillCacheListAdapter()
                }
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

class CacheSettingActivity : AppCompatActivity() {
    private fun getStringSize(size: Long): String {
        if (size <= 0)
            return "0MB"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / log10(Companion.sizeDescr)).toInt()
        return DecimalFormat("#,##0.#").format(size / Companion.sizeDescr.pow(digitGroups.toDouble())) +
                             " " + units[digitGroups]
    }

    fun fillCacheListAdapter() {
        val cacheSettingsList = findViewById<ListView>(R.id.cacheOptionsList)
        val adapter = ArrayAdapter<String> (
            this,
            android.R.layout.simple_list_item_1,
            listOf("Storage size: " + getStringSize(StorageManager.usedStorageSizeInBytes()),
                "Delete cache")
        )

        cacheSettingsList.adapter = adapter
        cacheSettingsList.setOnItemClickListener{ parent, view, position, id ->
            when (id.toInt()) {
                1 -> {
                    val myDialogFragment = ClearCacheDialog()
                    val manager = supportFragmentManager
                    myDialogFragment.show(manager, "'Delete cache' dialog")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cache_setting)
        fillCacheListAdapter()
    }

    companion object {
        const val sizeDescr = 1024.0
    }
}
