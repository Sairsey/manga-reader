package com.mangajet.mangajet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
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
import com.mangajet.mangajet.log.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// List adapter for "manga" list inner class
class MangaListAdapter(
    context: Context,
    resourceLayout: Int,
    items: ArrayList<Manga>
) : BaseAdapter() {
    companion object {
        const val MIN_ALPHA_COVER_VALUE =   0
        const val MAX_ALPHA_COVER_VALUE = 255
        const val KILO = 1024
        const val PART_FROM_MAX_MEMORY = 8

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        private val maxMemory = (Runtime.getRuntime().maxMemory() / KILO).toInt()
        // Use 1/8th of the available memory for this memory cache.
        private val cacheSize = maxMemory / PART_FROM_MAX_MEMORY

        // Cache in which we will store all bitmaps
        // One for all adapters because
        private var memoryCache: LruCache<String, Bitmap>

        init {
            memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
                override fun sizeOf(key: String, bitmap: Bitmap): Int {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.byteCount / KILO
                }
            }
        }
    }

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
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                if (bitmap == null)
                    continue
                else {
                    synchronized(memoryCache) {
                        memoryCache.put(page.url, bitmap)
                    }
                    return bitmap
                }
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
        var deleteTextView: TextView
        init {
            titleView = view.findViewById<TextView>(R.id.mangaTitle)
            autorView = view.findViewById<TextView>(R.id.authorTitle)
            sourceView = view.findViewById<TextView>(R.id.sourceLib)
            coverView = view.findViewById<ImageView>(R.id.coverManga)
            deleteTextView = view.findViewById<TextView>(R.id.txt_delete)
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

            var cacheBitmap : Bitmap? = null
            // at first - try to load bitmap from cache
            synchronized(memoryCache) {
                if (memoryCache.get(coverPage.url) != null)
                    cacheBitmap = memoryCache.get(coverPage.url)
            }

            if (cacheBitmap != null) {
                viewHolder.coverView.setImageBitmap(cacheBitmap)
                viewHolder.coverView.imageAlpha = MAX_ALPHA_COVER_VALUE
            }
            else {
                // if not found - load from Storage Manager
                viewHolder.coverView.imageAlpha = MIN_ALPHA_COVER_VALUE

                GlobalScope.launch(Dispatchers.Default) {
                    // POTENTIAL EXCEPTION and ERROR
                    // Cover isn't downloaded but we try to draw it => terminate
                    loadBitmap(coverPage)
                    withContext(Dispatchers.Main) {
                        // lets redraw everything if we loaded bitmap to cache
                        // this way we put image during getView,
                        // not in coroutine and everything is great
                        notifyDataSetChanged()
                    }
                }
            }

        }

        if (currentItem.originalName.isNotEmpty() && currentItem.originalName != "") {
            viewHolder.titleView.text = currentItem.originalName
            viewHolder.deleteTextView.text = "Manga `" + currentItem.originalName + "` was deleted from history"
        }
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
