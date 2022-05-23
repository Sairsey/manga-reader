package com.mangajet.mangajet.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.MangaListAdapter
import com.mangajet.mangajet.R
import com.mangajet.mangajet.aboutmanga.AboutMangaActivity
import com.mangajet.mangajet.databinding.HistoryFragmentBinding
import com.mangajet.mangajet.log.Logger
import com.hudomju.swipe.SwipeToDismissTouchListener
import com.hudomju.swipe.adapter.ListViewAdapter
import com.mangajet.mangajet.data.StorageManager


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

        val touchListener = SwipeToDismissTouchListener(
            ListViewAdapter(listView),
            object : SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter> {
                override fun canDismiss(position: Int): Boolean {
                    return true
                }

                override fun onDismiss(view: ListViewAdapter, position: Int) {

                    val id = historyViewModel.mangas[position].id
                    var path = id.replace(".", "_") + ".json"
                    var fileToDelete = StorageManager.getFile(path, StorageManager.FileType.MangaInfo)
                    fileToDelete.delete()
                    fileToDelete.delete()
                    fileToDelete.delete()
                    historyViewModel.mangas.removeAt(position)
                    var tmpAdapter = listView.adapter as MangaListAdapter
                    var state = listView.onSaveInstanceState()
                    listView.adapter = null
                    tmpAdapter.notifyDataSetChanged()
                    listView.adapter = tmpAdapter
                    listView.onRestoreInstanceState(state)
                }
            })
        listView!!.setOnTouchListener(touchListener)
        listView.setOnScrollListener(touchListener.makeScrollListener() as AbsListView.OnScrollListener)
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, id ->
            if (touchListener.existPendingDismisses()) {
                touchListener.undoPendingDismiss()
            }
            else {
                val intent = Intent(requireActivity(), AboutMangaActivity::class.java)
                MangaJetApp.currentManga = historyViewModel.mangas[id.toInt()]
                startActivityForResult(intent, MangaJetApp.ABOUT_MANGA_CALLBACK)
            }
        }
        return root
    }

    override fun onDestroyView() {
        Logger.log("History destroyed")
        super.onDestroyView()
        _binding = null
    }
}
