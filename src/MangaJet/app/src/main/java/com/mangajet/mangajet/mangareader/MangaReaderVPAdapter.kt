package com.mangajet.mangajet.mangareader

import android.graphics.PointF
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.MangaJetApp.Companion.context
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.MangaPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ensureActive


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
    inner class MangaReaderPageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ImageView on ViewPager2 pager with our page
        private val imagePage = itemView.findViewById<SubsamplingScaleImageView>(R.id.mangaPage)

        // Function which will bind pager with content
        fun bind(mangaPage : MangaPage, position : Int) {

            currentViewModelWithData.jobs[position] = GlobalScope.launch(Dispatchers.IO) {
                val pageFile = currentViewModelWithData.loadBitmap(mangaPage)
                ensureActive()
                withContext(Dispatchers.Main) {
                    if (pageFile != null) {
                        val imageSrc = ImageSource.bitmap(pageFile)
                        imagePage.setImage(imageSrc)
                        if (currentViewModelWithData.currentReaderFormat ==
                                MangaReaderViewModel.READER_FORMAT_MANHWA) {
                            val scaleCoef = (currentViewModelWithData.displayWidth /
                                    imagePage.sWidth.toFloat())
                            imagePage.setScaleAndCenter(scaleCoef, PointF(0F, 0F))
                        }
                    }
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

    // Function which will get page index for not-reverse reader format
    private fun getPageIndexBookFormat(position : Int) : Int {
        var pageIndex = 0

        // SPECIAL CASES:
        // only one chapter
        if (currentViewModelWithData.isSingleChapterManga())
            pageIndex = position

        // First chapter of book
        else if (currentViewModelWithData.isOnFirstChapter()) {
            pageIndex = if (position == itemCount - SKIP_ONE_PAGE_FOR_INDEX)
                0
            else
                position
        }

        // Last chapter
        else if (currentViewModelWithData.isOnLastChapter()) {
            pageIndex = if (position == 0)
                -1
            else
                position - SKIP_LEFT_FAKE_PAGE
        }

        // Other cases
        else {
            if (position == 0)
                pageIndex = -1
            else if (position == itemCount - SKIP_ONE_PAGE_FOR_INDEX)
                pageIndex = 0
            else
                pageIndex = position - SKIP_LEFT_FAKE_PAGE
        }

        return pageIndex
    }

    // Function which will get page index for reverse reader format
    private fun getPageIndexMangaFormat(position : Int) : Int {
        var pageIndex = 0
        var chapterIndex : Int = currentViewModelWithData.manga.lastViewedChapter
        val totalPages = currentViewModelWithData.manga.chapters[chapterIndex].getPagesNum()

        // SPECIAL CASES:
        // only one chapter
        if (currentViewModelWithData.isSingleChapterManga())
            pageIndex = totalPages - position

        // First chapter of book
        else if (currentViewModelWithData.isOnFirstChapter()) {
            if (position == 0) {
                pageIndex = 0
            }
            else
                pageIndex = (totalPages - SKIP_ONE_PAGE_FOR_INDEX) -
                        (position - SKIP_LEFT_FAKE_PAGE)
        }

        // Last chapter
        else if (currentViewModelWithData.isOnLastChapter()) {
            if (position == itemCount - SKIP_ONE_PAGE_FOR_INDEX)
                pageIndex = -1
            else
                pageIndex = (totalPages - SKIP_ONE_PAGE_FOR_INDEX) -
                            (position)
        }

        // Other cases
        else {
            if (position == itemCount - SKIP_ONE_PAGE_FOR_INDEX)
                pageIndex = -1
            else if (position == 0) {
                pageIndex = 0
            }
            else
                pageIndex = (totalPages - SKIP_ONE_PAGE_FOR_INDEX) -
                            (position - SKIP_RIGHT_FAKE_PAGE)
        }

        return pageIndex
    }

    // Function which will get chapter index for not-reverse reader format
    private fun getChapterIndexBookFormat(position : Int) : Int {
        var chapterIndex : Int = currentViewModelWithData.manga.lastViewedChapter

        // SPECIAL CASES:
        // First chapter of book
        if (currentViewModelWithData.isOnFirstChapter()) {
            if (position == itemCount - SKIP_ONE_PAGE_FOR_INDEX)
                chapterIndex++
        }

        // Last chapter
        else if (currentViewModelWithData.isOnLastChapter()) {
            if (position == 0)
                chapterIndex--
        }

        // Other cases
        else {
            if (position == 0)
                chapterIndex--
            else if (position == itemCount - SKIP_ONE_PAGE_FOR_INDEX)
                chapterIndex++
        }

        return chapterIndex
    }

    // Function which will get chapter index for reverse reader format
    private fun getChapterIndexMangaFormat(position : Int) : Int {
        var chapterIndex : Int = currentViewModelWithData.manga.lastViewedChapter

        // SPECIAL CASES:
        // First chapter of book
        if (currentViewModelWithData.isOnFirstChapter()) {
            if (position == 0)
                chapterIndex++
        }

        // Last chapter
        else if (currentViewModelWithData.isOnLastChapter()) {
            if (position == itemCount - SKIP_ONE_PAGE_FOR_INDEX)
                chapterIndex--
        }

        // Other cases
        else {
            if (position == itemCount - SKIP_ONE_PAGE_FOR_INDEX)
                chapterIndex--
            else if (position == 0)
                chapterIndex++
        }

        return chapterIndex
    }

    // Function which will update pages
    private fun updateSomePages(pageIndex : Int, chapterIndex : Int) : Int {
        var newPageIndex = pageIndex
        currentViewModelWithData.manga.chapters[chapterIndex].updateInfo()
        if (pageIndex == -1)
        {
            newPageIndex = currentViewModelWithData.manga.chapters[chapterIndex].getPagesNum() - 1
        }
        return newPageIndex
    }

    override fun onBindViewHolder(holder: MangaReaderPageHolder, position: Int) {
        var pageIndex : Int
        var chapterIndex : Int

        if (currentViewModelWithData.currentReaderFormat ==
            MangaReaderViewModel.READER_FORMAT_MANGA) {
            pageIndex = getPageIndexMangaFormat(position)
            chapterIndex = getChapterIndexMangaFormat(position)
        }
        else {
            pageIndex = getPageIndexBookFormat(position)
            chapterIndex = getChapterIndexBookFormat(position)
        }

        if (chapterIndex != currentViewModelWithData.manga.lastViewedChapter)
        {
            try {
                pageIndex = updateSomePages(pageIndex, chapterIndex)
            }
            catch (ex : MangaJetException) {
                Toast.makeText(MangaJetApp.context, ex.message, Toast.LENGTH_SHORT).show()
                return
            }
        }

        // bind holder
        try {
            val mangaPage = currentViewModelWithData.manga.chapters[chapterIndex].getPage(pageIndex)
            holder.bind(mangaPage, position)
        }
        catch (ex : MangaJetException) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
        }
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
