package com.mangajet.mangajet.aboutmanga

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.MangaPage
import com.mangajet.mangajet.mangareader.MangaReaderActivity

// Class which represents "About Manga" Activity
class AboutMangaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_manga_second)

        val manga = Librarian.getLibrary(Librarian.LibraryName.Mangachan)!!.searchManga("Клинок")[0]

        manga.updateInfo()

        manga.updateChapters()

        val page = manga.chapters[manga.lastViewedChapter].getPage(1+1+1+1+1)

        page.upload()

        val cover = MangaPage(manga.cover)
        cover.upload(true)

        findViewById<TextView>(R.id.titleText).setText(manga.originalName + " (" + manga.russianName + ")")
        findViewById<TextView>(R.id.authorText).setText(manga.author)
        findViewById<TextView>(R.id.fullDescriptionText).setText(manga.description)

        val imageFile = page.getFile()

        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        findViewById<ImageView>(R.id.coverManga).setImageBitmap(bitmap)

        val buttonToRead = findViewById<Button>(R.id.readMangaButton)
        buttonToRead.setOnClickListener{
            val intent = Intent(this, MangaReaderActivity::class.java)
            startActivity(intent)}
    }
}
