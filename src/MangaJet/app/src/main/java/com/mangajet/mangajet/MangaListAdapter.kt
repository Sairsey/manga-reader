package com.mangajet.mangajet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.MangaPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.mangajet.mangajet.log.Logger

// List adapter for "manga" list inner class
class MangaListAdapter(
    context: Context,
    resourceLayout: Int,
    items: ArrayList<Manga>
) : BaseAdapter() {
    // List context
    private val mContext: Context = context
    private val mItems : ArrayList<Manga> = items
    private val mResourceLayout : Int = resourceLayout

    // Function which will decode bitmap async
    private fun loadBitmap(page : MangaPage): Bitmap? {
        for (i in 0 until Librarian.settings.LOAD_REPEATS) {
            try {
                page.upload(i > 0)
                val imageFile = page.getFile()
                return BitmapFactory.decodeFile(imageFile.absolutePath) ?: continue
            } catch (ex: MangaJetException) {
                // we do not need to catch exceptions here
                Logger.log("Catch MJE exception in loadBitmap: " + ex.message, Logger.Lvl.WARNING)
                continue
            }
        }
        // maybe throw exception or reload?
        Logger.log("Return null in loadBitMap", Logger.Lvl.WARNING)
        return null
    }

    override fun getCount(): Int {
        return mItems.size
    }

    override fun getItem(position: Int): Any {
        return mItems.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private class ViewHolder(view: View) {
        var titleView : TextView
        var autorView : TextView
        var sourceView : TextView
        var coverView : ImageView

        init {
            titleView = view.findViewById<TextView>(R.id.mangaTitle)
            autorView = view.findViewById<TextView>(R.id.authorTitle)
            sourceView = view.findViewById<TextView>(R.id.sourceLib)
            coverView = view.findViewById<ImageView>(R.id.coverManga)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: ViewHolder
        var cv = convertView

        if (cv == null) {
            cv = LayoutInflater.from(mContext).inflate(mResourceLayout, null)
            viewHolder = ViewHolder(cv)
            cv.tag = viewHolder
        }
        else {
            viewHolder = cv.tag as ViewHolder
        }

        val currentItem = getItem(position) as Manga

        if (currentItem.cover.isNotEmpty()) {
            var coverPage = MangaPage(currentItem.cover, currentItem.library.getHeadersForDownload())
            coverPage.upload()

            GlobalScope.launch(Dispatchers.Default) {
                // POTENTIAL EXCEPTION and ERROR
                // Cover isn't downloaded but we try to draw it => terminate
                val bitmap = loadBitmap(coverPage)
                withContext(Dispatchers.Main) {
                    if (bitmap != null)
                        viewHolder.coverView.setImageBitmap(bitmap)
                }
            }

        }

        if (currentItem.originalName.isNotEmpty() && currentItem.originalName != "")
            viewHolder.titleView.text = currentItem.originalName
        else if (currentItem.russianName.isNotEmpty() && currentItem.russianName != "")
            viewHolder.titleView.text = currentItem.russianName
        else
            viewHolder.titleView.text = "Manga #$position"

        if (currentItem.author.isNotEmpty() && currentItem.author != "")
            viewHolder.autorView.text = "Author: " + currentItem.author
        else
            viewHolder.autorView.text = "Creative work from Web"

        viewHolder.sourceView.text = "Source: " + currentItem.library.getURL()

        return cv!!
    }
}
