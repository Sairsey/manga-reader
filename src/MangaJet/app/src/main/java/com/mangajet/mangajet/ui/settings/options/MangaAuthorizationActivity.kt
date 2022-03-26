package com.mangajet.mangajet.ui.settings.options

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import com.mangajet.mangajet.R
import com.mangajet.mangajet.authorization.AuthorizationActivity
import com.mangajet.mangajet.data.Librarian

class MangaAuthorizationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manga_authorization)
        setTitle(R.string.title_authorization)

        val mangasLibraries = arrayListOf<String>()
        Librarian.LibraryName.values().forEach { mangasLibraries.add(it.toString()) }

        val mangasLibrariesURL = arrayListOf<String>()
        Librarian.LibraryName.values().forEach { mangasLibrariesURL.add(it.resource) }

        val mangaAuthorizationList = findViewById<ListView>(R.id.mangaAuthorizationList)
        val adapter = ArrayAdapter<String> (
            this,
            android.R.layout.simple_list_item_1,
            mangasLibraries
        )

        mangaAuthorizationList.adapter = adapter
        mangaAuthorizationList.setOnItemClickListener{ parent, view, position, id ->
            var intent : Intent = Intent(this, AuthorizationActivity::class.java)
            intent.putExtra("URL", mangasLibrariesURL[id.toInt()])
            startActivity(intent)
        }
    }
}
