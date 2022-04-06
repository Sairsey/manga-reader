package com.mangajet.mangajet.ui.search

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.aboutmanga.AboutMangaActivity
import com.mangajet.mangajet.databinding.SearchFragmentBinding

// Class which represents "Search" fragment of MainActivity
class SearchFragment : Fragment() {

    private var _binding: SearchFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var searchViewModel : SearchViewModel

    inner class OnQueryTextListener(fragAct : FragmentActivity) : SearchView.OnQueryTextListener {
        val frag = fragAct

        override fun onQueryTextSubmit(p0: String?): Boolean {
            binding.noResultLayout.visibility = View.INVISIBLE

            val adapter = ArrayAdapter<String>(
                frag,
                R.layout.simple_list_item_1,
                searchViewModel.mangasNames
            )

            if (p0 != null) {
                searchViewModel.initMangas(adapter, binding, p0)
            }
            else
                return false

            binding.searchViewList.adapter = adapter
            binding.searchViewList.setOnItemClickListener{ parent, view, position, id ->
                val intent = Intent(frag, AboutMangaActivity::class.java)
                MangaJetApp.currentManga = searchViewModel.mangas[id.toInt()]
                startActivity(intent)}

            return true
        }

        override fun onQueryTextChange(p0: String?): Boolean {
            return false
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        searchViewModel =
            ViewModelProvider(this).get(SearchViewModel::class.java)

        _binding = SearchFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.noResultLayout.visibility = View.VISIBLE
        binding.progressBar.hide()

        var searchView = binding.searchView

        activity?.let {
            searchView.setOnQueryTextListener(OnQueryTextListener(it))
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.searchView.setQuery("", false)
        searchViewModel.job?.cancel()
        _binding = null
    }
}
