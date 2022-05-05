package com.mangajet.mangajet.ui.forYou

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.MangaListAdapter
import com.mangajet.mangajet.R
import com.mangajet.mangajet.aboutmanga.AboutMangaActivity
import com.mangajet.mangajet.databinding.ForYouFragmentBinding
import com.mangajet.mangajet.log.Logger
import com.mangajet.mangajet.ui.history.HistoryFragment

// Class which represents "For you" fragment of MainActivity
class ForYouFragment : Fragment() {

    private var _binding: ForYouFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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
                startActivityForResult(intent, MangaJetApp.SEARCH_BY_TAG)}
        }
        return root
    }

    override fun onDestroyView() {
        Logger.log("For you destroyed")
        super.onDestroyView()
        _binding = null
    }
}
