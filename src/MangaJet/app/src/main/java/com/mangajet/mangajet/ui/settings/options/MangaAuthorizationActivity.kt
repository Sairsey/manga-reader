package com.mangajet.mangajet.ui.settings.options

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.android.material.appbar.MaterialToolbar
import com.mangajet.mangajet.R
import com.mangajet.mangajet.authorization.AuthorizationActivity
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.log.Logger

// Authorization in different manga libraries Activity class
class MangaAuthorizationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.log("Authorization in Settings opened")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manga_authorization)
        setSupportActionBar(findViewById<MaterialToolbar>(R.id.authorizationToolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return true
    }
}
