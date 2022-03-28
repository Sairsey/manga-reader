package com.mangajet.mangajet.aboutmanga.aboutMangaFragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mangajet.mangajet.R

class AboutMangaFragment : Fragment() {

    companion object {
        fun newInstance() = AboutMangaFragment()
    }

    private lateinit var viewModel: AboutMangaViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.about_manga_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AboutMangaViewModel::class.java)
    }
}
