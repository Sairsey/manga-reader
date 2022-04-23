package com.mangajet.mangajet.mangareader.straight

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.MangaPage
import com.mangajet.mangajet.mangareader.formatchangeholder.MangaReaderBaseAdapter
import com.mangajet.mangajet.mangareader.MangaReaderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Class which will create adapter for straight (book) format reader
class StraightReaderVPAdapter(viewModel: MangaReaderViewModel) : MangaReaderBaseAdapter(viewModel) {
    // Class for holger reverse format viewpage page
    inner class StraightReaderPageHolder(itemView: View) : MangaReaderPageHolder(itemView) {
        override fun bind(mangaPage: MangaPage, position: Int) {
            currentViewModelWithData.jobs[position] = currentViewModelWithData.viewModelScope
                .launch(Dispatchers.IO) {
                    itemView.findViewById<CircularProgressIndicator>(R.id.loadIndicator).show()
                    val pageFile = currentViewModelWithData.loadBitmap(mangaPage)
                    ensureActive()
                    withContext(Dispatchers.Main) {
                        if (pageFile != null) {
                            val imageSrc = ImageSource.bitmap(pageFile)
                            ensureActive()
                            imagePage.setImage(imageSrc)
                            itemView.findViewById<CircularProgressIndicator>(R.id.loadIndicator).hide()
                        }
                    }
                }
        }
    }

    // Function which will get page index
    override fun getPageIndex(position: Int): Int {
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

    // Function which will get chapter index
    override fun getChapterIndex(position: Int): Int {
        var chapterIndex : Int = currentViewModelWithData.manga.lastViewedChapter

        // SPECIAL CASES:
        if (currentViewModelWithData.isSingleChapterManga())
            return chapterIndex

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaReaderPageHolder {
        return StraightReaderPageHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.manga_reader_pager, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MangaReaderPageHolder, position: Int) {
        var pageIndex : Int = getPageIndex(position)
        var chapterIndex : Int = getChapterIndex(position)

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
            Toast.makeText(MangaJetApp.context, ex.message, Toast.LENGTH_SHORT).show()
        }
    }
}
