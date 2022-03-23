package com.mangajet.mangajet.ui.settings.options

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.DialogFragment
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.StorageManager

class AuthorsDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Authors")
                .setMessage("Kozhevnikova Diana\n" +
                            "Kungurov Fedor\n" +
                            "Parusov Vladimir\n" +
                            "Popov Ivan\n" +
                            "Sachuk Aleksander\n")
                .setCancelable(true)
                .setPositiveButton("Sources") { dialog, id ->
                    val intent = Intent(it, AboutAppWebView::class.java)
                    intent.putExtra("URL", "https://github.com/Sairsey/manga-reader")
                    startActivity(intent)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

class AboutAppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTitle(R.string.setting_aboutapp)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_app)

        val aboutAppList = findViewById<ListView>(R.id.aboutAppList)
        val adapter = ArrayAdapter<String> (
            this,
            android.R.layout.simple_list_item_1,
            listOf( "Program version: v0.1.0",
                    "Support",
                    "Authors")
        )

        aboutAppList.adapter = adapter
        aboutAppList.setOnItemClickListener{ parent, view, position, id ->
            when (id.toInt()) {
                Companion.SUPPORT -> {
                    val intent = Intent(this, AboutAppWebView::class.java)
                    intent.putExtra("URL", "https://i.imgflip.com/17vyv9.jpg")
                    startActivity(intent)
                }

                Companion.AUTHORS -> {
                    val myDialogFragment = AuthorsDialog()
                    val manager = supportFragmentManager
                    myDialogFragment.show(manager, "'Author' dialog")
                }
            }
        }
    }

    companion object {
        const val SUPPORT = 1
        const val AUTHORS = 2
    }
}
