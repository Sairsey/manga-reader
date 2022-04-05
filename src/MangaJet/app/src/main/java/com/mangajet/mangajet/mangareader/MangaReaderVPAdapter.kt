package com.mangajet.mangajet.mangareader

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.MangaPage

class MangaReaderVPAdapter(viewModel: MangaReaderViewModel) :
    RecyclerView.Adapter<MangaReaderVPAdapter.MangaReaderPageHolder>() {
    // viewModel, which contains our interesting data
    var currentViewModelWithData = viewModel

    inner class MangaReaderPageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imagePage = itemView.findViewById<SubsamplingScaleImageView>(R.id.mangaPage)
        private val pageAndChapterNumberView =
            itemView.findViewById<TextView>(R.id.pageAndChapterNumber)

        fun bind(mangaPage : MangaPage, position : Int, chapter : Int) {
            pageAndChapterNumberView.text = "$chapter/$position"

            if (mangaPage.localPath == "") {
                val img = ImageSource.uri(mangaPage.localPath)
                imagePage.setImage(img)
            }
            else {
                val pageFile = mangaPage.getFile()
                val bitmapa = BitmapFactory.decodeFile(pageFile.absolutePath)
                imagePage.setImage(ImageSource.bitmap(bitmapa))
            }
            //imagePage.setImage(ImageSource.uri())
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
        // SPECIAL CASES:
        var pageIndex : Int = 0
        var chapterIndex : Int = currentViewModelWithData.manga.lastViewedChapter

        // only one chapter
        if (currentViewModelWithData.manga.chapters.size == 1)
            pageIndex = position

        // First chapter
        else if (currentViewModelWithData.manga.lastViewedChapter == 0) {
            if (position == itemCount - 1) {
                pageIndex = 0
                chapterIndex++
                currentViewModelWithData.manga.chapters[chapterIndex].updateInfo()
            }
            else
                pageIndex = position
        }
        // Last chapter
        else if (currentViewModelWithData.manga.lastViewedChapter == currentViewModelWithData.manga.chapters.size - 1) {
            if (position == 0) {
                chapterIndex--
                currentViewModelWithData.manga.chapters[chapterIndex].updateInfo()
                pageIndex = currentViewModelWithData.manga.chapters[chapterIndex].getPagesNum() - 1
            }
            else
                pageIndex = position - 1
        }
        // Other cases
        else {
            if (position == 0) {
                chapterIndex--
                currentViewModelWithData.manga.chapters[chapterIndex].updateInfo()
                pageIndex = currentViewModelWithData.manga.chapters[chapterIndex].getPagesNum() - 1
            }
            else if (position == itemCount - 1) {
                pageIndex = 0
                chapterIndex++
                currentViewModelWithData.manga.chapters[chapterIndex].updateInfo()
            }
            else
                pageIndex = position - 1
        }

        if (pageIndex == -1)
            return

        val mangaPage = currentViewModelWithData.manga
            .chapters[chapterIndex]
            .getPage(pageIndex)

        holder.bind(mangaPage, position,
            chapterIndex)
    }

    override fun getItemCount(): Int {
        // Only one chapter
        if (currentViewModelWithData.manga.chapters.size == 1)
            return currentViewModelWithData.manga
                .chapters[currentViewModelWithData.manga.lastViewedChapter].getPagesNum()

        // First or last chapter
        if (currentViewModelWithData.manga.lastViewedChapter == 0
            || currentViewModelWithData.manga.lastViewedChapter == currentViewModelWithData.manga.chapters.size - 1)
            return currentViewModelWithData.manga
                .chapters[currentViewModelWithData.manga.lastViewedChapter].getPagesNum() + 1

        // All chapters
        return currentViewModelWithData.manga
            .chapters[currentViewModelWithData.manga.lastViewedChapter].getPagesNum() + 2
    }
}
