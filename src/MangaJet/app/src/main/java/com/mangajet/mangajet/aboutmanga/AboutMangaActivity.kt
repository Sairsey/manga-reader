package com.mangajet.mangajet.aboutmanga

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.mangajet.mangajet.R
import com.mangajet.mangajet.mangareader.MangaReaderActivity

// Class which represents "About Manga" Activity
class AboutMangaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_manga_second)

        val buttonToRead = findViewById<Button>(R.id.openMangaReader)
        buttonToRead.setOnClickListener{
            //Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show()}
            val intent = Intent(this, MangaReaderActivity::class.java)
            startActivity(intent)}
    }
}
