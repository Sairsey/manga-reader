package com.mangajet.mangajet.ui.forYou

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.MangaListAdapter
import com.mangajet.mangajet.aboutmanga.AboutMangaActivity
import com.mangajet.mangajet.databinding.ForYouFragmentBinding
import com.mangajet.mangajet.log.Logger

// Class which represents "For you" fragment of MainActivity
class ForYouFragment : Fragment() {

    private var _binding: ForYouFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        Logger.log("For you opened")
        val forYouFragmentViewModel =
            ViewModelProvider(this).get(ForYouViewModel::class.java)

        _binding = ForYouFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        var listView = binding.forYouListView
        activity?.let {
            val adapter = MangaListAdapter(
                requireActivity(),
                com.mangajet.mangajet.R.layout.manga_list_element,
                forYouFragmentViewModel.mangas
            )
            forYouFragmentViewModel.initMangas(adapter)

            listView.adapter = adapter
            listView.setOnItemClickListener{ parent, view, position, id ->
                val intent = Intent(it, AboutMangaActivity::class.java)
                MangaJetApp.currentManga = forYouFragmentViewModel.mangas[id.toInt()]
                startActivity(intent)}
        }
        return root
    }

    override fun onDestroyView() {
        Logger.log("For you destroyed")
        super.onDestroyView()
        _binding = null
    }
}
