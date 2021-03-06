package com.mangajet.mangajet.ui.forYou

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.MangaListAdapter
import com.mangajet.mangajet.R
import com.mangajet.mangajet.aboutmanga.AboutMangaActivity
import com.mangajet.mangajet.databinding.ForYouFragmentBinding
import com.mangajet.mangajet.log.Logger

// Class which represents "For you" fragment of MainActivity
class ForYouFragment : Fragment() {

    private var _binding: ForYouFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var forYouFragmentViewModel : ForYouViewModel? = null

    // toolbar with filter button
    private var mToolbar : MaterialToolbar? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            MangaJetApp.ABOUT_MANGA_CALLBACK -> {
                if (MangaJetApp.OverrideFragmentInMainActivity.FragmentSearch.needToBeOpened) {
                    val navigationBar = activity?.findViewById<BottomNavigationView>(R.id.nav_view)
                    val view: View = navigationBar!!.findViewById(
                        MangaJetApp.OverrideFragmentInMainActivity.FragmentSearch.mainFragmentId
                    )
                    view.performClick()
                }
            }
            else ->
                super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.chooseSources -> forYouFragmentViewModel!!.updateLibsSources(fragmentManager)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        Logger.log("For you opened")
        forYouFragmentViewModel =
            ViewModelProvider(this)[ForYouViewModel::class.java]

        _binding = ForYouFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        forYouFragmentViewModel!!.binding = binding
        binding.loadRecommendationsIndicator.visibility = View.INVISIBLE
        binding.noResultLayout.visibility = View.INVISIBLE

        // init toolbar
        mToolbar = binding.forYouToolbar
        mToolbar?.inflateMenu(R.menu.for_you_menu)
        mToolbar?.setOnMenuItemClickListener{
            onOptionsItemSelected(it)
        }

        var listView = binding.forYouListView
        activity?.let {
            val adapter = MangaListAdapter(
                requireActivity(),
                R.layout.manga_list_element,
                forYouFragmentViewModel!!.mangas
            )
            forYouFragmentViewModel!!.initMangas(adapter, true)

            listView.adapter = adapter
            listView.setOnItemClickListener{ parent, view, position, id ->
                val intent = Intent(it, AboutMangaActivity::class.java)
                MangaJetApp.currentManga = forYouFragmentViewModel!!.mangas[id.toInt()]
                startActivityForResult(intent, MangaJetApp.ABOUT_MANGA_CALLBACK)}
        }
        return root
    }

    override fun onDestroyView() {
        Logger.log("For you destroyed")
        super.onDestroyView()
        _binding = null
    }
}
