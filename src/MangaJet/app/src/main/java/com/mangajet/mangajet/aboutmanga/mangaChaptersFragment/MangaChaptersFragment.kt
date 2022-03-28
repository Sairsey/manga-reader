package com.mangajet.mangajet.aboutmanga.mangaChaptersFragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mangajet.mangajet.R

class MangaChaptersFragment : Fragment() {

    companion object {
        fun newInstance() = MangaChaptersFragment()
    }

    private lateinit var viewModel: MangaChaptersViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.manga_chapters_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MangaChaptersViewModel::class.java)
    }

}
