package com.mangajet.mangajet.mangareader

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.MangaPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MangaReaderVPAdapter(viewModel: MangaReaderViewModel) :
    RecyclerView.Adapter<MangaReaderVPAdapter.MangaReaderPageHolder>() {
    // viewModel, which contains our interesting data
    var currentViewModelWithData = viewModel

    inner class MangaReaderPageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imagePage = itemView.findViewById<ImageView>(R.id.mangaPage)
        private val pageAndChapterNumberView = itemView.findViewById<TextView>(R.id.pageAndChapterNumber)

        fun bind(mangaPage : MangaPage, position : Int, chapter : Int) {
            pageAndChapterNumberView.text = "$chapter/$position"

            currentViewModelWithData.jobs[position] = GlobalScope.launch(Dispatchers.IO) {
                // POTENTIAL EXCEPTION and ERROR
                // Cover isn't downloaded but we try to draw it => terminate
                val bitmap = currentViewModelWithData.loadBitmap(mangaPage)
                withContext(Dispatchers.Main) {
                    if (bitmap != null)
                        imagePage.setImageBitmap(bitmap)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaReaderPageHolder {
        return MangaReaderPageHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.manga_reader_pager, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MangaReaderPageHolder, position: Int) {
        if (position == 0 || position == itemCount - 1)
            return

        val mangaPage = currentViewModelWithData.manga
            .chapters[currentViewModelWithData.manga.lastViewedChapter]
            .getPage(position - 1)

        holder.bind(mangaPage, position - 1,
            currentViewModelWithData.manga.lastViewedChapter)
    }

    override fun getItemCount(): Int {
        return currentViewModelWithData.manga
            .chapters[currentViewModelWithData.manga.lastViewedChapter].getPagesNum() + 2
    }
}
