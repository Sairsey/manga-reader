package com.mangajet.mangajet.mangareader

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.mangajet.mangajet.MangaJetApp.Companion.context
import com.mangajet.mangajet.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MangaReaderVPAdapter(viewModel: MangaReaderViewModel) : PagerAdapter() {
    // viewModel, which contains our interesting data
    var currentViewModelWithData = viewModel

    override fun getCount(): Int {
        return currentViewModelWithData.totalPagesInCurrentManga
    }

    override fun instantiateItem(collection: View, position: Int): Any {
        val mangaPage = currentViewModelWithData.getPageByPosition(position)
        val inflater = LayoutInflater.from(context)

        val viewPage = inflater.inflate(R.layout.manga_reader_pager, null)
        val pageView = viewPage.findViewById<View>(R.id.mangaPage) as ImageView
        //textView.text = mangaPage.url

        currentViewModelWithData.jobs[position] = GlobalScope.launch(Dispatchers.IO) {
            // POTENTIAL EXCEPTION and ERROR
            // Cover isn't downloaded but we try to draw it => terminate
            val bitmap = currentViewModelWithData.loadBitmap(mangaPage)
            withContext(Dispatchers.Main) {
                if (bitmap != null)
                    pageView.setImageBitmap(bitmap)
            }
        }

        (collection as ViewPager).addView(viewPage, 0)
        return viewPage
    }

    override fun destroyItem(collection: View, position: Int, `object`: Any) {
        (collection as ViewPager).removeView(`object` as View?)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view.equals(`object`)
    }
}
