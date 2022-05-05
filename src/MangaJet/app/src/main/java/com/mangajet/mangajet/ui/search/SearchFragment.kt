package com.mangajet.mangajet.ui.search

import com.mangajet.mangajet.R
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.MangaListAdapter
import com.mangajet.mangajet.aboutmanga.AboutMangaActivity
import com.mangajet.mangajet.databinding.SearchFragmentBinding
import com.mangajet.mangajet.log.Logger

// Class which represents "Search" fragment of MainActivity
class SearchFragment : Fragment() {

    private var _binding: SearchFragmentBinding? = null

    private var mToolbar : MaterialToolbar? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var searchViewModel : SearchViewModel

    inner class OnQueryTextListener(fragAct : FragmentActivity) : SearchView.OnQueryTextListener {
        val frag = fragAct

        override fun onQueryTextSubmit(p0: String?): Boolean {
            binding.noResultLayout.visibility = View.INVISIBLE

            val adapter = MangaListAdapter(
                frag,
                R.layout.manga_list_element,
                searchViewModel.mangas
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
                startActivityForResult(intent, MangaJetApp.ABOUT_MANGA_CALLBACK)}

            return true
        }

        override fun onQueryTextChange(p0: String?): Boolean {
            return false
        }

    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.chooseLibs -> searchViewModel.updateLibsSources(fragmentManager)
        }

        return super.onOptionsItemSelected(item)
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        Logger.log("Search opened")
        searchViewModel =
            ViewModelProvider(this).get(SearchViewModel::class.java)

        // init binding
        _binding = SearchFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // init toolbar
        mToolbar = binding.searchToolbar
        mToolbar?.inflateMenu(R.menu.search_menu)
        mToolbar?.setOnMenuItemClickListener{
            onOptionsItemSelected(it)
        }

        // init result layout
        binding.noResultLayout.visibility = View.VISIBLE
        binding.progressBar.hide()

        // init list with mangas
        var searchView = binding.searchView
        activity?.let {
            searchView.setOnQueryTextListener(OnQueryTextListener(it))
        }

        return root
    }


    override fun onStart() {
        super.onStart()

        // init mangas (tag search if we need this)
        activity?.let {
            if (MangaJetApp.OverrideFragmentInMainActivity.FragmentSearch.needToBeOpened) {
                MangaJetApp.OverrideFragmentInMainActivity.FragmentSearch.needToBeOpened = false
                binding.searchView.setQuery(resources.getString(R.string.tag_search)
                        + " " + MangaJetApp.tagSearchInfo!!.first,
                    false)
                searchViewModel.mangas = arrayListOf()
                val adapter = MangaListAdapter(
                    it,
                    R.layout.manga_list_element,
                    searchViewModel.mangas
                )

                searchViewModel.initSearchListView(adapter, binding)

                binding.searchViewList.adapter = adapter
                binding.searchViewList.setOnItemClickListener { parent, view, position, id ->
                    val intent = Intent(it, AboutMangaActivity::class.java)
                    MangaJetApp.currentManga = searchViewModel.mangas[id.toInt()]
                    startActivityForResult(intent, MangaJetApp.ABOUT_MANGA_CALLBACK)
                }
            }
            else {
                // clear query text
                binding.searchView.setQuery("",false)
            }
        }
    }

    override fun onDestroyView() {
        Logger.log("Search destroyed")
        super.onDestroyView()
        binding.searchView.setQuery("", false)
        searchViewModel.job?.cancel()
        _binding = null
    }
}
