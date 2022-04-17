package com.mangajet.mangajet.mangareader

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.MangaJetApp.Companion.context
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.MangaJetException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Class which represents "Manga Reader" Activity
class MangaReaderActivity : AppCompatActivity() {
    // Manga reader activity ViewModel variable
    lateinit var mangaReaderViewModel : MangaReaderViewModel

    // Function which will set activity title by current opened page
    fun setPageTitle(
        mangaReaderViewModel : MangaReaderViewModel,
        position: Int
    ) {
        val page = position + 1
        val chapter = mangaReaderViewModel.manga.lastViewedChapter + 1
        val totalPages = mangaReaderViewModel.pagesCount
        supportActionBar?.title = "Chapter $chapter, page $page/$totalPages"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.manga_reader_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Function which will brute forced reload page
    private fun reloadCurrentPage() {
        val chapter = mangaReaderViewModel.manga
            .chapters[mangaReaderViewModel.manga.lastViewedChapter]
        // at this point we already downloaded whole chapter so no need to worry about exception
        val page = chapter.getPage(chapter.lastViewedPage)

        // this can only fail if we do not have storage permission
        // We have blocking dialog in this case, so it someone still
        // manges to go here, I think we should crash
        page.upload(true)

        val viewPager = findViewById<ViewPager2>(R.id.mangaViewPager)
        var delta = if (mangaReaderViewModel.isOnFirstChapter()) 0 else 1
        var position = chapter.lastViewedPage + delta

        val pagerAdapter = viewPager.adapter
        viewPager.adapter = null
        pagerAdapter?.notifyDataSetChanged()
        viewPager.adapter = pagerAdapter
        viewPager.setCurrentItem(position, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reloadPage -> reloadCurrentPage()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manga_reader_activity)

        setSupportActionBar(findViewById<MaterialToolbar>(R.id.mangaReaderToolbar))

        mangaReaderViewModel = ViewModelProvider(this).get(MangaReaderViewModel::class.java)

        mangaReaderViewModel.initMangaData()
        setPageTitle(mangaReaderViewModel,
            mangaReaderViewModel.manga
                .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                .lastViewedPage)

        val viewPager = findViewById<ViewPager2>(R.id.mangaViewPager)
        val pagerAdapter = MangaReaderVPAdapter(mangaReaderViewModel)
        viewPager.adapter = pagerAdapter

        var delta = if (mangaReaderViewModel.isOnFirstChapter()) 0 else 1
        viewPager.setCurrentItem(mangaReaderViewModel.manga
            .chapters[mangaReaderViewModel.manga.lastViewedChapter]
            .lastViewedPage + delta, false)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            // Function which will load previous chapter after scroll
            private fun doToPrevChapter() {
                // update chapter
                mangaReaderViewModel.manga.lastViewedChapter--

                // update pages count (and load chapter)
                try {
                    mangaReaderViewModel.pagesCount = mangaReaderViewModel.manga
                        .chapters[mangaReaderViewModel.manga.lastViewedChapter].getPagesNum()
                }
                catch (ex:MangaJetException) {
                    Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                    viewPager.setCurrentItem(1, false)
                    mangaReaderViewModel.manga.lastViewedChapter++
                    return
                }

                // start loading all pages
                val job = GlobalScope.launch(Dispatchers.IO) {
                    mangaReaderViewModel.uploadPages()
                }

                // set correct page
                mangaReaderViewModel.manga
                    .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                    .lastViewedPage = mangaReaderViewModel.pagesCount - 1

                // save manga state
                try {
                    mangaReaderViewModel.manga.saveToFile()
                }
                catch (ex : MangaJetException) {
                    Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                }

                // update adapter
                while (job.isActive)
                    println("Im busy man too")

                viewPager.adapter = null
                pagerAdapter.notifyDataSetChanged()
                viewPager.adapter = pagerAdapter

                // determine delta
                var delta = 0
                if (mangaReaderViewModel.manga.lastViewedChapter == 0)
                    delta = -1

                viewPager.setCurrentItem(mangaReaderViewModel.pagesCount + delta, false)

                val chapter = mangaReaderViewModel.manga.lastViewedChapter + 1
                Toast.makeText(context, "Chapter $chapter",
                    Toast.LENGTH_SHORT).show()
            }

            // Function which will load next chapter after scroll
            private fun doToNextChapter() {
                // update chapter
                mangaReaderViewModel.manga.lastViewedChapter++;

                // update pages count (and load chapter)
                try {
                    mangaReaderViewModel.pagesCount = mangaReaderViewModel.manga
                        .chapters[mangaReaderViewModel.manga.lastViewedChapter].getPagesNum()
                }
                catch (ex:MangaJetException) {
                    Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                    var delta = 0
                    if (mangaReaderViewModel.manga.lastViewedChapter == 0)
                        delta = -1
                    viewPager.setCurrentItem(mangaReaderViewModel.pagesCount + delta, false)
                    mangaReaderViewModel.manga.lastViewedChapter++
                    return
                }

                // start loading all pages
                val job = GlobalScope.launch(Dispatchers.IO) {
                    mangaReaderViewModel.uploadPages()
                }

                // set correct page
                mangaReaderViewModel.manga
                    .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                    .lastViewedPage = 0

                // save manga state
                try {
                    mangaReaderViewModel.manga.saveToFile()
                }
                catch (ex : MangaJetException) {
                    Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                }

                // update adapter
                while (job.isActive)
                    println("Im busy man")

                viewPager.adapter = null
                pagerAdapter.notifyDataSetChanged()
                viewPager.adapter = pagerAdapter
                viewPager.setCurrentItem(1, false)

                val chapter = mangaReaderViewModel.manga.lastViewedChapter + 1
                Toast.makeText(context, "Chapter $chapter",
                    Toast.LENGTH_SHORT).show()
            }

            // Function which will be tried to load prev or next chapter
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mangaReaderViewModel.manga
                    .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                    .lastViewedPage = if (mangaReaderViewModel.isOnFirstChapter()) position
                                      else position - 1

                // SPECIAL CASES
                // only one chapter
                if (mangaReaderViewModel.isSingleChapterManga()) {
                    // do nothing
                }
                // First chapter
                else if (mangaReaderViewModel.isOnFirstChapter()) {
                    if (position == pagerAdapter.itemCount - 1) {
                        doToNextChapter()
                    }
                }
                // Last chapter
                else if (mangaReaderViewModel.manga.lastViewedChapter == mangaReaderViewModel.manga.chapters.size - 1) {
                    if (position == 0)
                        doToPrevChapter()
                }
                // Other cases
                else {
                    if (position == 0)
                        doToPrevChapter()
                    else if (position == pagerAdapter.itemCount - 1)
                        doToNextChapter()
                }

                setPageTitle(mangaReaderViewModel,
                    mangaReaderViewModel.manga
                    .chapters[mangaReaderViewModel.manga.lastViewedChapter]
                    .lastViewedPage)
            }
        })
    }
}
