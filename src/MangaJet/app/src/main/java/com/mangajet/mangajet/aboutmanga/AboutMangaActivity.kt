package com.mangajet.mangajet.aboutmanga

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.flexbox.FlexboxLayout
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.MangaPage
import com.mangajet.mangajet.mangareader.MangaReaderActivity

// Class which represents "About Manga" Activity
class AboutMangaActivity : AppCompatActivity() {
    companion object {
        const val PADDING_VERT = 5      // Vert padding tag value
        const val PADDING_HORZ = 30      // Horz padding tag value
    }

    // In methods 'onCreate' we only init data in viewport. All other actions -> in onStart() or onResume() overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_manga_second)

        // Call viewport to manage low speed downloading data in dat class
        val aboutMangaViewmodel = ViewModelProvider(this)[AboutMangaViewModel::class.java]
        aboutMangaViewmodel.initMangaData(intent)
        setTitle(aboutMangaViewmodel.manga.originalName)
    }

    override fun onStart() {
        super.onStart()
        val aboutMangaViewmodel = ViewModelProvider(this)[AboutMangaViewModel::class.java]

        val cover = MangaPage(aboutMangaViewmodel.manga.cover)
        cover.upload()

        findViewById<TextView>(R.id.titleText).setText( aboutMangaViewmodel.manga.originalName + " (" +
                                                        aboutMangaViewmodel.manga.russianName + ")")
        findViewById<TextView>(R.id.authorText).setText(aboutMangaViewmodel.manga.author)
        findViewById<TextView>(R.id.fullDescriptionText).setText(aboutMangaViewmodel.manga.description)

        val imageFile = cover.getFile()

        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        findViewById<ImageView>(R.id.coverManga).setImageBitmap(bitmap)

        val buttonToRead = findViewById<Button>(R.id.readMangaButton)
        buttonToRead.setOnClickListener{
            val intent = Intent(this, MangaReaderActivity::class.java)
            startActivity(intent)}

        // Tags TextView generator
        val tagsLayout = findViewById<FlexboxLayout>(R.id.tagsLayout)
        aboutMangaViewmodel.manga.tags.forEach {
            val newTextView = TextView(this)
            newTextView.setText(it)
            newTextView.setPadding(PADDING_HORZ, PADDING_VERT, PADDING_HORZ, PADDING_VERT)
            newTextView.setTextColor(resources.getColor(R.color.primary))
            newTextView.setBackgroundResource(R.drawable.tag_border)
            tagsLayout.addView(newTextView)
        }
    }
}
