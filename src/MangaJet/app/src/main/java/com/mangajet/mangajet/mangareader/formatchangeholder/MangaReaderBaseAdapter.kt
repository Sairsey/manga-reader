package com.mangajet.mangajet.mangareader.formatchangeholder

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.MangaPage
import com.mangajet.mangajet.mangareader.MangaReaderViewModel

//
abstract class MangaReaderBaseAdapter(viewModel: MangaReaderViewModel) :
    RecyclerView.Adapter<MangaReaderBaseAdapter.MangaReaderPageHolder>() {
    // We add some amount of pages in ViewPager2 for scrolling between chapters
    companion object {
        // reserved pages count for manga with single-chapter
        const val REVERSED_PAGES_AMOUNT_SINGLE_CHAPTER = 0
        // reserved pages count for first or last chapters
        const val REVERSED_PAGES_AMOUNT_SIDE_CHAPTER   = 1
        // reserved pages count for middle chapters
        const val REVERSED_PAGES_AMOUNT_MIDDLE_CHAPTER = 2

        // Fake left page count
        const val SKIP_LEFT_FAKE_PAGE     = 1
        // Fake right page count
        const val SKIP_RIGHT_FAKE_PAGE    = 1
        // Value to go form 'Count' values to 'Index' values
        const val SKIP_ONE_PAGE_FOR_INDEX = 1
    }

    // viewModel, which contains our interesting data
    var currentViewModelWithData = viewModel

    // Class which will provide access between ViewPager2 and ImageView
    abstract inner class MangaReaderPageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ImageView on ViewPager2 pager with our page
        val imagePage = itemView.findViewById<SubsamplingScaleImageView>(R.id.mangaPage)

        // Function which will bind pager with content
        abstract fun bind(mangaPage : MangaPage, position : Int)
    }

    // Function which will get page index for current reader format
    abstract fun getPageIndex(position : Int) : Int

    // Function which will get chapter index for current reader format
    abstract fun getChapterIndex(position : Int) : Int

    // Function which will update pages
    fun updateSomePages(pageIndex : Int, chapterIndex : Int) : Int {
        var newPageIndex = pageIndex
        currentViewModelWithData.manga.chapters[chapterIndex].updateInfo()
        if (pageIndex == -1)
        {
            newPageIndex = currentViewModelWithData.manga.chapters[chapterIndex].getPagesNum() - 1
        }
        return newPageIndex
    }

    override fun getItemCount(): Int {
        val count = try {
            currentViewModelWithData.manga
                .chapters[currentViewModelWithData.manga.lastViewedChapter].getPagesNum()
        } catch (ex : MangaJetException) {
            Toast.makeText(MangaJetApp.context, ex.message, Toast.LENGTH_SHORT).show()
            0
        }

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
