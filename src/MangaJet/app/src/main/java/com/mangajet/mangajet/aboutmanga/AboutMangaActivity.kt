package com.mangajet.mangajet.aboutmanga

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.MangaPage
import com.mangajet.mangajet.mangareader.MangaReaderActivity
import com.mangajet.mangajet.ui.history.HistoryViewModel

// Class which represents "About Manga" Activity
class AboutMangaActivity : AppCompatActivity() {
    // In methods 'onCreate' we only init data in viewport. All other actions -> in onStart() or onResume() overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_manga_second)

        // Call viewport to manage low speed downloading data in dat class
        val aboutMangaViewmodel = ViewModelProvider(this)[AboutMangaViewModel::class.java]      // Kavo????
        aboutMangaViewmodel.initMangaData(intent)
        setTitle(aboutMangaViewmodel.origTitle)
        findViewById<TextView>(R.id.fullDescriptionText).movementMethod = ScrollingMovementMethod()
    }

    override fun onStart() {
        super.onStart()
        val aboutMangaViewmodel = ViewModelProvider(this)[AboutMangaViewModel::class.java]

        val cover = MangaPage(aboutMangaViewmodel.cover)
        cover.upload()

        findViewById<TextView>(R.id.titleText).setText( aboutMangaViewmodel.origTitle + " (" +
                                                        aboutMangaViewmodel.rusTitle + ")")
        findViewById<TextView>(R.id.authorText).setText(aboutMangaViewmodel.author)
        findViewById<TextView>(R.id.fullDescriptionText).setText(aboutMangaViewmodel.descr)

        val imageFile = cover.getFile()

        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        findViewById<ImageView>(R.id.coverManga).setImageBitmap(bitmap)

        val buttonToRead = findViewById<Button>(R.id.readMangaButton)
        buttonToRead.setOnClickListener{
            val intent = Intent(this, MangaReaderActivity::class.java)
            startActivity(intent)}
    }

    override fun onResume() {
        super.onResume()
    }
}
