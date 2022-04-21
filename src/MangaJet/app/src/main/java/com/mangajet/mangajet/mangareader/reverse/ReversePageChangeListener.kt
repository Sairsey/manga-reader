package com.mangajet.mangajet.mangareader.reverse

import androidx.viewpager2.widget.ViewPager2
import com.mangajet.mangajet.mangareader.MangaReaderViewModel
import com.mangajet.mangajet.mangareader.formatchangeholder.MangaReaderBaseAdapter

class ReversePageChangeListener(
    mangaReaderVM : MangaReaderViewModel,
    mangaReaderViewPager : ViewPager2
) :
    ViewPager2.OnPageChangeCallback() {
    val mangaReaderViewModel = mangaReaderVM
    val viewPager = mangaReaderViewPager
    lateinit var pagerAdapter : MangaReaderBaseAdapter

    // Function which will be tried to load prev or next chapter
    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)

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
