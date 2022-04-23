package com.mangajet.mangajet.mangareader

import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2

// Class which will handle actions with menu
class MangaReaderMenuHandler(mangaReaderVM : MangaReaderViewModel,
                             mangaReaderVP : ViewPager2,
                             mSupportFragmentManager : FragmentManager) {
    // reference to manga reader view model with data
    private val mangaReaderViewModel = mangaReaderVM
    // reference to viewPager2
    private val viewPager = mangaReaderVP
    // reference to fragment manager
    private val supportFragmentManager = mSupportFragmentManager

    // Function which will brute forced reload page
    fun reloadCurrentPage() {
        val chapter = mangaReaderViewModel.manga
            .chapters[mangaReaderViewModel.manga.lastViewedChapter]
        // at this point we already downloaded whole chapter so no need to worry about exception
        val page = chapter.getPage(chapter.lastViewedPage)

        // this can only fail if we do not have storage permission
        // We have blocking dialog in this case, so it someone still
        // manges to go here, I think we should crash
        page.upload(true)

        var delta = if (mangaReaderViewModel.isOnFirstChapter()) 0 else 1
        var position = chapter.lastViewedPage + delta

        val pagerAdapter = viewPager.adapter
        viewPager.adapter = null
        pagerAdapter?.notifyDataSetChanged()
        viewPager.adapter = pagerAdapter
        viewPager.setCurrentItem(position, false)
    }

    // Function which will call dialog for changing format
    fun callChangeFormatDialog() {
        val myDialogFragment = ChangeMangaReaderFormatDialog(mangaReaderViewModel)
        myDialogFragment.show(supportFragmentManager, "Change reader format dialog")
    }
}
