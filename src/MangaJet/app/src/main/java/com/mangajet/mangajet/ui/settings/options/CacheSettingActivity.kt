package com.mangajet.mangajet.ui.settings.options
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.appbar.MaterialToolbar
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.StorageManager
import com.mangajet.mangajet.log.Logger
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.pow
// Clear cache dialog class
class ClearCacheDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            Logger.log("Clear cache dialog opened")
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Clear cache")
                .setMessage("Are you sure you want to clear your cache?")
                .setCancelable(true)
                .setPositiveButton("Delete") { dialog, id ->
                    StorageManager.removeFilesByType(StorageManager.FileType.CachedPages)
                    StorageManager.removeFilesByType(StorageManager.FileType.DownloadedPages)
                    StorageManager.removeFilesByType(StorageManager.FileType.MangaInfo)
                    Logger.log("Delete clicked")
                }
                .setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                        Logger.log("Cancel clicked")
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

// Cache management Activity
class CacheSettingActivity : AppCompatActivity() {
    companion object {
        const val KILO = 1024.0     // Size of one Kilo
        const val CREATE_FILE = 1   // Specific number for Action in android
        const val OPEN_FILE = 2   // Specific number for Action in android
    }

    // Function will return readable string with storage size
    private fun getStringSize(size: Long): String {
        if (size <= 0)
            return "0MB"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / log10(KILO)).toInt()
        return DecimalFormat("#,##0.#").format(size / KILO.pow(digitGroups.toDouble())) +
                             " " + units[digitGroups]
    }

/*    fun fillCacheSizeView(){
        val cacheSizeView = findViewById<TextView>(R.id.cacheSize)
        var stringToFillWith = getStringSize(StorageManager.usedStorageSizeInBytes())
        cacheSizeView.setText(stringToFillWith)
    }*/

    fun buttonPressed() {
        val myDialogFragment = ClearCacheDialog()
        val manager = supportFragmentManager
        myDialogFragment.show(manager, "'Delete cache' dialog")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_FILE
            && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            data?.data?.also { uri ->
                // Perform operations on the document using its URI.
                val outputStream = contentResolver.openOutputStream(uri)
                StorageManager.createZipArchive(outputStream!!)
                Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
            }
        }
        else if (requestCode == OPEN_FILE
            && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            data?.data?.also { uri ->
                // Perform operations on the document using its URI.
                val inputStream = contentResolver.openInputStream(uri)
                StorageManager.unpackZipArchive(inputStream!!)
                Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
            }
        }
    }
    @Suppress("LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.log("Cache options in Settings opened")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cache_setting)
        //fillCacheSizeView()
        val clearCacheButton = findViewById<Button>(R.id.clearCacheButton)
        clearCacheButton.setOnClickListener { buttonPressed() }
        setSupportActionBar(findViewById<MaterialToolbar>(R.id.cacheToolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val cacheSettingsList = findViewById<ListView>(R.id.cacheSettings)
        val adapter = ArrayAdapter<String> (
            this,
            android.R.layout.simple_list_item_1,
            listOf( "Cache size: " + getStringSize(StorageManager.usedStorageSizeInBytes()),
                "Size of Manga Infos: " + getStringSize(StorageManager.
                usedStorageSizeByType(StorageManager.FileType.MangaInfo)),
                "Size of Cached Pages: " + getStringSize(StorageManager.
                usedStorageSizeByType(StorageManager.FileType.CachedPages)),
                "Size of Downloaded Pages: " + getStringSize(StorageManager.
                usedStorageSizeByType(StorageManager.FileType.DownloadedPages)))
        )
        cacheSettingsList.adapter = adapter
        val backupButton = findViewById<Button>(R.id.backupButton)
        backupButton.setOnClickListener {
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val backupPath = "MangaJetBackup_" + sdf.format(Date()) + ".zip"

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/zip"
                putExtra(Intent.EXTRA_TITLE, backupPath)
            }
            startActivityForResult(intent, CREATE_FILE)
        }
        val clearMangaInfosButton = findViewById<Button>(R.id.clearMangaInfos)
        clearMangaInfosButton.setOnClickListener {
            StorageManager.removeFilesByType(StorageManager.FileType.MangaInfo)
            val adapter = ArrayAdapter<String> (
                this,
                android.R.layout.simple_list_item_1,
                listOf( "Cache size: " + getStringSize(StorageManager.usedStorageSizeInBytes()),
                    "Size of Manga Infos: " + getStringSize(StorageManager.
                    usedStorageSizeByType(StorageManager.FileType.MangaInfo)),
                    "Size of Cached Pages: " + getStringSize(StorageManager.
                    usedStorageSizeByType(StorageManager.FileType.CachedPages)),
                    "Size of Downloaded Pages: " + getStringSize(StorageManager.
                    usedStorageSizeByType(StorageManager.FileType.DownloadedPages)))
            )
            cacheSettingsList.adapter = adapter
        }
        val clearDownloadedPages = findViewById<Button>(R.id.clearDownloadedPages)
        clearDownloadedPages.setOnClickListener {
            StorageManager.removeFilesByType(StorageManager.FileType.DownloadedPages)
            val adapter = ArrayAdapter<String> (
                this,
                android.R.layout.simple_list_item_1,
                listOf( "Cache size: " + getStringSize(StorageManager.usedStorageSizeInBytes()),
                    "Size of Manga Infos: " + getStringSize(StorageManager.
                    usedStorageSizeByType(StorageManager.FileType.MangaInfo)),
                    "Size of Cached Pages: " + getStringSize(StorageManager.
                    usedStorageSizeByType(StorageManager.FileType.CachedPages)),
                    "Size of Downloaded Pages: " + getStringSize(StorageManager.
                    usedStorageSizeByType(StorageManager.FileType.DownloadedPages)))
            )
            cacheSettingsList.adapter = adapter
        }
        val clearCachedPages = findViewById<Button>(R.id.clearCachedPages)
        clearCachedPages.setOnClickListener {
            StorageManager.removeFilesByType(StorageManager.FileType.CachedPages)
            val adapter = ArrayAdapter<String> (
                this,
                android.R.layout.simple_list_item_1,
                listOf( "Cache size: " + getStringSize(StorageManager.usedStorageSizeInBytes()),
                    "Size of Manga Infos: " + getStringSize(StorageManager.
                    usedStorageSizeByType(StorageManager.FileType.MangaInfo)),
                    "Size of Cached Pages: " + getStringSize(StorageManager.
                    usedStorageSizeByType(StorageManager.FileType.CachedPages)),
                    "Size of Downloaded Pages: " + getStringSize(StorageManager.
                    usedStorageSizeByType(StorageManager.FileType.DownloadedPages)))
            )
            cacheSettingsList.adapter = adapter
        }
        val restoreButton = findViewById<Button>(R.id.restoreButton)
        restoreButton.setOnClickListener {

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/zip"
            }
            startActivityForResult(intent, OPEN_FILE)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return true
    }
}

