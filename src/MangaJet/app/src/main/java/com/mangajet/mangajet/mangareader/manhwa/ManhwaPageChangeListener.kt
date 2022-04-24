package com.mangajet.mangajet.mangareader.manhwa

import androidx.viewpager2.widget.ViewPager2
import com.mangajet.mangajet.log.Logger
import com.mangajet.mangajet.mangareader.MangaReaderViewModel
import com.mangajet.mangajet.mangareader.formatchangeholder.MangaReaderBaseAdapter

// Class which will listen page changes in 'Manhwa' format reader
class ManhwaPageChangeListener(
    mangaReaderVM : MangaReaderViewModel,
    mangaReaderViewPager : ViewPager2
    ) :
    ViewPager2.OnPageChangeCallback() {
    // manga reader viewModel reference with data
    val mangaReaderViewModel = mangaReaderVM
    // viewpager2 reference to listen for
    val viewPager = mangaReaderViewPager
    // viewpager2 page adapter
    lateinit var pagerAdapter : MangaReaderBaseAdapter

    // Function which will be tried to load prev or next chapter
    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)
        mangaReaderViewModel.manga
            .chapters[mangaReaderViewModel.manga.lastViewedChapter]
            .lastViewedPage = if (mangaReaderViewModel.isOnFirstChapter()) position
        else position - 1

        var page = mangaReaderViewModel.manga
            .chapters[mangaReaderViewModel.manga.lastViewedChapter]
            .lastViewedPage

        Logger.log("Selected page $page", Logger.Lvl.INFO)

        // SPECIAL CASES
        // only one chapter
        if (mangaReaderViewModel.isSingleChapterManga()) {
            // do nothing
        }
        // First chapter
        else if (mangaReaderViewModel.isOnFirstChapter()) {
            if (position == pagerAdapter.itemCount - 1)
                mangaReaderViewModel.doToNextChapter(viewPager, pagerAdapter)
        }
        // Last chapter
        else if (mangaReaderViewModel.manga.lastViewedChapter == mangaReaderViewModel.manga.chapters.size - 1) {
            if (position == 0)
                mangaReaderViewModel.doToPrevChapter(viewPager, pagerAdapter)
        }
        // Other cases
        else {
            if (position == 0)
                mangaReaderViewModel.doToPrevChapter(viewPager, pagerAdapter)
            else if (position == pagerAdapter.itemCount - 1)
                mangaReaderViewModel.doToNextChapter(viewPager, pagerAdapter)
        }

        mangaReaderViewModel.setPageTitle()
    }
}
