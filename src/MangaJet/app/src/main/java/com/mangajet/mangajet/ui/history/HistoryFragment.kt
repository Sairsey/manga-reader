package com.mangajet.mangajet.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.aboutmanga.AboutMangaActivity
import com.mangajet.mangajet.databinding.HistoryFragmentBinding

// Class which represents "History" fragment of MainActivity
class HistoryFragment : Fragment() {

    private var _binding: HistoryFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onResume() {
        super.onResume()
        val historyViewModel =
            ViewModelProvider(this).get(HistoryViewModel::class.java)

        historyViewModel.makeListFromStorage()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            val historyViewModel =
                ViewModelProvider(this).get(HistoryViewModel::class.java)

            historyViewModel.makeListFromStorage()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        val historyViewModel =
            ViewModelProvider(this).get(HistoryViewModel::class.java)

        _binding = HistoryFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        var listView = binding.historyListview
        activity?.let {
            val adapter = ArrayAdapter<String>(
                it,
                android.R.layout.simple_list_item_1,
                historyViewModel.mangasNames
            )

            historyViewModel.init(adapter)
            historyViewModel.makeListFromStorage()

            listView.adapter = adapter
            listView.setOnItemClickListener{ parent, view, position, id ->
                val intent = Intent(it, AboutMangaActivity::class.java)
                MangaJetApp.currentManga = historyViewModel.mangas[id.toInt()]
                startActivity(intent)}
        }

        return root
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
