package com.mangajet.mangajet.mangareader.reverse

import androidx.viewpager2.widget.ViewPager2
import com.mangajet.mangajet.mangareader.MangaReaderViewModel
import com.mangajet.mangajet.mangareader.formatchangeholder.MangaReaderBaseAdapter

// Class which will listen page changes in 'Manga (Reverse)' format reader
class ReversePageChangeListener(
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

        if (mangaReaderViewModel.isInited == false)
            return

        mangaReaderViewModel.manga
            .chapters[mangaReaderViewModel.manga.lastViewedChapter]
            .lastViewedPage = mangaReaderViewModel.manga
            .chapters[mangaReaderViewModel.manga.lastViewedChapter]
            .getPagesNum() - position
        if (mangaReaderViewModel.isOnLastChapter())
            mangaReaderViewModel.manga
                .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                .lastViewedPage -= 1


        // SPECIAL CASES
        // only one chapter
        if (mangaReaderViewModel.isSingleChapterManga()) {
            // do nothing
        }
        // First chapter
        else if (mangaReaderViewModel.isOnFirstChapter()) {
            if (position == 0)
                mangaReaderViewModel.doToNextChapter(viewPager, pagerAdapter)
        }
        // Last chapter
        else if (mangaReaderViewModel.manga.lastViewedChapter == mangaReaderViewModel.manga.chapters.size - 1) {
            if (position == pagerAdapter.itemCount - 1)
                mangaReaderViewModel.doToPrevChapter(viewPager, pagerAdapter)
        }
        // Other cases
        else {
            if (position == pagerAdapter.itemCount - 1)
                mangaReaderViewModel.doToPrevChapter(viewPager, pagerAdapter)
            else if (position == 0)
                mangaReaderViewModel.doToNextChapter(viewPager, pagerAdapter)
        }

        mangaReaderViewModel.setPageTitle()
    }
}
