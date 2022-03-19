package com.mangajet.mangajet.ui.history

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mangajet.mangajet.R
import com.mangajet.mangajet.aboutmanga.AboutMangaActivity
import com.mangajet.mangajet.databinding.HistoryFragmentBinding

class HistoryFragment : Fragment() {

    private var _binding: HistoryFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

            listView.adapter = adapter
            listView.setOnItemClickListener{ parent, view, position, id ->
                //val element = parent.getItemAtPosition(position) // The item that was clicked
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