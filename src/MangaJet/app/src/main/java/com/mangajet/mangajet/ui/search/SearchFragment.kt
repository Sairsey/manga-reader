package com.mangajet.mangajet.ui.search

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mangajet.mangajet.aboutmanga.AboutMangaActivity
import com.mangajet.mangajet.databinding.SearchFragmentBinding

class SearchFragment : Fragment() {

    private var _binding: SearchFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val searchViewModel =
            ViewModelProvider(this).get(SearchViewModel::class.java)

        _binding = SearchFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        var listView = binding.searchViewList
        activity?.let {
            val adapter = ArrayAdapter<String>(
                it,
                R.layout.simple_list_item_1,
                searchViewModel.mangasNames
            )

            listView.adapter = adapter
            listView.setOnItemClickListener{ parent, view, position, id ->
                val intent = Intent(it, AboutMangaActivity::class.java)
                startActivity(intent)}
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}