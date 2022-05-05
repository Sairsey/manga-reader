package com.mangajet.mangajet.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.MangaListAdapter
import com.mangajet.mangajet.R
import com.mangajet.mangajet.aboutmanga.AboutMangaActivity
import com.mangajet.mangajet.databinding.HistoryFragmentBinding
import com.mangajet.mangajet.log.Logger


// Class which represents "History" fragment of MainActivity
class HistoryFragment : Fragment() {
    private var _binding: HistoryFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Adapter for ListView
    private lateinit var adapter: MangaListAdapter

    override fun onResume() {
        super.onResume()
        val historyViewModel =
            ViewModelProvider(this).get(HistoryViewModel::class.java)

        binding.progressBar.show()
        binding.noResultLayout.visibility = View.INVISIBLE
        historyViewModel.makeListFromStorage(adapter, binding)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            val historyViewModel =
                ViewModelProvider(this).get(HistoryViewModel::class.java)

            binding.progressBar.show()
            binding.noResultLayout.visibility = View.INVISIBLE
            historyViewModel.makeListFromStorage(adapter, binding)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            MangaJetApp.SEARCH_BY_TAG -> {
                if (data == null) {
                    super.onActivityResult(requestCode, resultCode, data)
                    return
                }
                // send info for search

                // collect info for search
                val tag = data!!.getCharSequenceExtra("tag").toString()
                val src = data!!.getCharSequenceExtra("src").toString()

                MangaJetApp.isNeedToTagSearch = true
                MangaJetApp.tagSearchInfo = Pair(tag, src)
                val navigationBar = activity?.findViewById<BottomNavigationView>(R.id.nav_view)
                val view: View = navigationBar!!.findViewById(R.id.navigation_search)
                view.performClick()
            }
            else ->
                super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        Logger.log("History opened")
        val historyViewModel =
            ViewModelProvider(this).get(HistoryViewModel::class.java)

        _binding = HistoryFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        var listView = binding.historyListview
        adapter = MangaListAdapter(
            requireActivity(),
            com.mangajet.mangajet.R.layout.manga_list_element,
            historyViewModel.mangas
        )

        binding.progressBar.show()
        binding.noResultLayout.visibility = View.INVISIBLE
        historyViewModel.makeListFromStorage(adapter, binding)

        listView.adapter = adapter
        listView.setOnItemClickListener{ parent, view, position, id ->
            val intent = Intent(requireActivity(), AboutMangaActivity::class.java)
            MangaJetApp.currentManga = historyViewModel.mangas[id.toInt()]
            startActivityForResult(intent, MangaJetApp.SEARCH_BY_TAG)}

        return root
    }

    override fun onDestroyView() {
        Logger.log("History destroyed")
        super.onDestroyView()
        _binding = null
    }
}
