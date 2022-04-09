package com.mangajet.mangajet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.MangaPage
import com.mangajet.mangajet.mangareader.MangaReaderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Class for initializing MangaList element
class MangaListElementContainer(
    newTitle : String,
    newAuthor : String,
    newSource : String,
    newCover : String,
) {
    var title : String = newTitle       // manga title
    var author : String = newAuthor     // manga author
    var source : String = newSource     // manga source
    var coverUrl : String = newCover    // manga cover url
}

// List adapter for "manga" list inner class
class MangaListAdapter(
    context: Context,
    private val resourceLayout: Int,
    items: ArrayList<MangaListElementContainer>
) :
    ArrayAdapter<MangaListElementContainer>(context, resourceLayout, items) {
    companion object {
        const val LOAD_REPEATS = 5      // Load repeat count (if prev load failed -> repeat)
    }

    // List context
    private val mContext: Context = context


    // Function which will decode bitmap async
    private fun loadBitmap(page : MangaPage): Bitmap? {
        for (i in 0 until LOAD_REPEATS) {
            i.hashCode()
            try {
                page.upload(i > 0)
                val imageFile = page.getFile() // Catch ex here
                return BitmapFactory.decodeFile(imageFile.absolutePath) ?: continue
            } catch (ex: MangaJetException) {
                continue
            }
        }
        // maybe throw exception or reload?
        return null
    }

    // Function which will fill every list element
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v: View? = convertView
        if (v == null) {
            val vi: LayoutInflater
            vi = LayoutInflater.from(mContext)
            v = vi.inflate(resourceLayout, null)
        }

        val p = getItem(position)
        if (p != null) {
            val cover = v?.findViewById<ImageView>(R.id.coverManga)
            if (p.coverUrl.isNotEmpty()) {
                val coverSrc = MangaPage(p.coverUrl)
                coverSrc.upload()
                GlobalScope.launch(Dispatchers.Default) {
                    // POTENTIAL EXCEPTION and ERROR
                    // Cover isn't downloaded but we try to draw it => terminate
                    val bitmap = loadBitmap(coverSrc)
                    withContext(Dispatchers.Main) {
                        if (bitmap != null)
                            cover?.setImageBitmap(bitmap)
                    }
                }
            }

            val title = v?.findViewById<TextView>(R.id.mangaTitle)
            val author = v?.findViewById<TextView>(R.id.authorTitle)
            val source = v?.findViewById<TextView>(R.id.sourceLib)

            if (p.title.isNotEmpty() && p.title != "")
                title?.text = p.title
            else
                title?.text = "Manga #$position"

            if (p.author.isNotEmpty() && p.author != "")
                author?.text = "Author: " + p.author
            else
                author?.text = "Creative work from Web"

            source?.text = "Source: " + p.source
        }
        return v!!
    }
}
