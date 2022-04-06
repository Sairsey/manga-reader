package com.mangajet.mangajet.mangareader

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
    companion object {
        const val SINGLE_CHAPTER_REVERSED_LIST = 0
        const val SIDE_CHAPTER_REVERSED_LIST   = 1
        const val MIDDLE_CHAPTER_REVERSED_LIST = 2
    }

    // viewModel, which contains our interesting data
    var currentViewModelWithData = viewModel

    inner class MangaReaderPageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imagePage = itemView.findViewById<SubsamplingScaleImageView>(R.id.mangaPage)
        private val pageAndChapterNumberView =
            itemView.findViewById<TextView>(R.id.pageAndChapterNumber)

        fun bind(mangaPage : MangaPage, chapter : Int, page : Int) {
            val chapterOutput = chapter + 1
            val pageOutput = page + 1

            //pageAndChapterNumberView.text = "Chap: $chapterOutput / Page: $pageOutput"
            pageAndChapterNumberView.text = "$chapterOutput / $pageOutput"

            val pageFile = currentViewModelWithData.loadBitmap(mangaPage)
            if (pageFile != null)
                imagePage.setImage(ImageSource.bitmap(pageFile))
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
        var pageIndex = 0
        var chapterIndex : Int = currentViewModelWithData.manga.lastViewedChapter

        // SPECIAL CASES:
        // only one chapter
        if (currentViewModelWithData.isSingleChapterManga())
            pageIndex = position

        // First chapter
        else if (currentViewModelWithData.isOnFirstChapter()) {
            if (position == itemCount - 1) {
                pageIndex = 0
                chapterIndex++
                currentViewModelWithData.manga.chapters[chapterIndex].updateInfo()
            }
            else
                pageIndex = position
        }

        // Last chapter
        else if (currentViewModelWithData.isOnLastChapter()) {
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

        // leave if bad page index
        if (pageIndex == -1)
            return

        // bind holder
        val mangaPage = currentViewModelWithData.manga.chapters[chapterIndex].getPage(pageIndex)
        holder.bind(mangaPage, chapterIndex, pageIndex)
    }

    override fun getItemCount(): Int {
        val count = currentViewModelWithData.manga
            .chapters[currentViewModelWithData.manga.lastViewedChapter].getPagesNum()

        // Only one chapter
        if (currentViewModelWithData.isSingleChapterManga())
            return count + SINGLE_CHAPTER_REVERSED_LIST

        // First or last chapter
        else if (currentViewModelWithData.isOnSideChapter())
            return count + SIDE_CHAPTER_REVERSED_LIST

        // Middle chapter
        else
            return count + MIDDLE_CHAPTER_REVERSED_LIST
    }
}
