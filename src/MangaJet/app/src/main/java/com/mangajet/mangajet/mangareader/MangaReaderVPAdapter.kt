package com.mangajet.mangajet.mangareader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.MangaPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MangaReaderVPAdapter(viewModel: MangaReaderViewModel) :
    RecyclerView.Adapter<MangaReaderVPAdapter.MangaReaderPageHolder>() {
    // We add some amount of pages in ViewPager2 for scrolling between chapters
    companion object {
        // reserved pages count for manga with single-chapter
        const val REVERSED_PAGES_AMOUNT_SINGLE_CHAPTER = 0
        // reserved pages count for first or last chapters
        const val REVERSED_PAGES_AMOUNT_SIDE_CHAPTER   = 1
        // reserved pages count for middle chapters
        const val REVERSED_PAGES_AMOUNT_MIDDLE_CHAPTER = 2
    }

    // viewModel, which contains our interesting data
    var currentViewModelWithData = viewModel

    // Class which will provide access between ViewPager2 and ImageView
    inner class MangaReaderPageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ImageView on ViewPager2 pager with our page
        private val imagePage = itemView.findViewById<SubsamplingScaleImageView>(R.id.mangaPage)

        // Function which will bind pager with content
        fun bind(mangaPage : MangaPage, position : Int) {
            currentViewModelWithData.jobs[position] = GlobalScope.launch(Dispatchers.IO) {
                val pageFile = currentViewModelWithData.loadBitmap(mangaPage)
                withContext(Dispatchers.Main) {
                    if (pageFile != null)
                        imagePage.setImage(ImageSource.bitmap(pageFile))
                    itemView.findViewById<CircularProgressIndicator>(R.id.loadIndicator).hide()
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

        // bind holder
        val mangaPage = currentViewModelWithData.manga.chapters[chapterIndex].getPage(pageIndex)
        holder.bind(mangaPage, position)
    }

    override fun getItemCount(): Int {
        val count = currentViewModelWithData.manga
            .chapters[currentViewModelWithData.manga.lastViewedChapter].getPagesNum()

        // Only one chapter
        if (currentViewModelWithData.isSingleChapterManga())
            return count + REVERSED_PAGES_AMOUNT_SINGLE_CHAPTER

        // First or last chapter
        else if (currentViewModelWithData.isOnSideChapter())
            return count + REVERSED_PAGES_AMOUNT_SIDE_CHAPTER

        // Middle chapter
        else
            return count + REVERSED_PAGES_AMOUNT_MIDDLE_CHAPTER
    }
}
