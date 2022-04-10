package com.mangajet.mangajet.ui.settings.options

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.appbar.MaterialToolbar
import com.mangajet.mangajet.R

// Dialog with authors
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
                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Sairsey/manga-reader"))
                    startActivity(browserIntent)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

// About App Activity with buttons 'support' and 'authors'
class AboutAppActivity : AppCompatActivity() {
    companion object {
        const val SUPPORT = 1   // 'Support' button index
        const val AUTHORS = 2   // 'Authors' button index
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTitle(R.string.setting_aboutapp)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_app)

        setSupportActionBar(findViewById<MaterialToolbar>(R.id.aboutAppToolbar))

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
                SUPPORT -> {
                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://i.imgflip.com/17vyv9.jpg"))
                    startActivity(browserIntent)
                }

                AUTHORS -> {
                    val myDialogFragment = AuthorsDialog()
                    val manager = supportFragmentManager
                    myDialogFragment.show(manager, "'Author' dialog")
                }
            }
        }
    }
}
