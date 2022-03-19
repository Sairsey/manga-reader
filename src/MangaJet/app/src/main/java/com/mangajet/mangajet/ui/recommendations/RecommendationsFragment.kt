package com.mangajet.mangajet.ui.recommendations

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
import com.mangajet.mangajet.databinding.RecommendationsFragmentBinding

// Class which represents "Recommendations" fragment of MainActivity
class RecommendationsFragment : Fragment() {

    private var _binding: RecommendationsFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        val recommendedViewModel =
            ViewModelProvider(this).get(RecommendationsViewModel::class.java)

        _binding = RecommendationsFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        var listView = binding.recommendedListView
        activity?.let {
            val adapter = ArrayAdapter<String>(
                it,
                R.layout.simple_list_item_1,
                recommendedViewModel.mangasNames
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
