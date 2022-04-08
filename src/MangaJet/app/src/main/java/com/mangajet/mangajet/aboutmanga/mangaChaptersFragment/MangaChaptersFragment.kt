package com.mangajet.mangajet.aboutmanga.mangaChaptersFragment

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.R
import com.mangajet.mangajet.aboutmanga.AboutMangaViewModel
import com.mangajet.mangajet.data.MangaChapter
import com.mangajet.mangajet.databinding.MangaChaptersFragmentBinding
import com.mangajet.mangajet.mangareader.MangaReaderActivity
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


// "About manga" chapter fragment class
class MangaChaptersFragment : Fragment() {
    // scroll position variable
    lateinit var scrollPosition : Parcelable

    var job : Job? = null
    private lateinit var aboutMangaViewmodel : AboutMangaViewModel

    // List adapter for "chapters" list inner class
    class ChapterListAdapter(context: Context,
                             private val resourceLayout: Int,
                             items: Array<MangaChapter>,
                             public var lastViewedChapter : Int) :
        ArrayAdapter<MangaChapter>(context, resourceLayout, items) {
        // List context
        private val mContext: Context = context

        // Function which will fill every list element
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var v: View? = convertView
            if (v == null) {
                val vi: LayoutInflater
                vi = LayoutInflater.from(mContext)
                v = vi.inflate(resourceLayout, null)
            }

            val p = getItem(position)
            if (p != null) {
                val chapter = v?.findViewById<TextView>(R.id.chapterTitle)
                val icon = v?.findViewById<ImageView>(R.id.viewedIcon)

                if (p.name.isNotEmpty())
                    chapter?.setText(context.getString(R.string.chapter_default_name) + " " +
                            (position + 1).toString() + ": " + p.name)
                else
                    chapter?.setText(context.getString(R.string.chapter_default_name) + " " +
                            (position + 1).toString())

                if (position < lastViewedChapter)
                    icon?.setImageResource(R.drawable.ic_opened_book)
                else
                    icon?.setImageResource(R.drawable.ic_closed_book)
            }
            return v!!
        }
    }

    // Binding tool for "MangaChapterFragment"
    private var _binding: MangaChaptersFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MangaChaptersFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    private suspend fun createChaptersList() {
        aboutMangaViewmodel.job?.join()

        withContext(Dispatchers.Main) {
            aboutMangaViewmodel.adapter?.notifyDataSetChanged()
            binding.chaptersList.visibility = View.VISIBLE
            binding.loadIndicator.hide()
        }
    }

    override fun onStart() {
        super.onStart()
        aboutMangaViewmodel = ViewModelProvider(requireActivity()).get(AboutMangaViewModel::class.java)
        var listView = binding.chaptersList

        binding.loadIndicator.hide()

        // Start async waiting of chapters loading job
        if (aboutMangaViewmodel.job?.isCompleted == false) {
            binding.chaptersList.visibility = View.INVISIBLE
            binding.loadIndicator.show()
            job = GlobalScope.launch(Dispatchers.Default) {
                createChaptersList()
            }
        }

        scrollPosition = listView.onSaveInstanceState()!!
        activity?.let {
            aboutMangaViewmodel.adapter = ChapterListAdapter(
                it,
                R.layout.chapter_list_element,
                aboutMangaViewmodel.manga.chapters,
                aboutMangaViewmodel.manga.lastViewedChapter)

            binding.chaptersList.adapter = aboutMangaViewmodel.adapter

            binding.chaptersList.setOnItemClickListener { parent, view, position, id ->
                val intent = Intent(it, MangaReaderActivity::class.java)
                MangaJetApp.currentManga = aboutMangaViewmodel.manga
                MangaJetApp.currentManga!!.lastViewedChapter = id.toInt()
                MangaJetApp.currentManga!!
                    .chapters[MangaJetApp.currentManga!!.lastViewedChapter]
                    .lastViewedPage = 0
                startActivity(intent)
            }
        }
    }

    // Overridden func which will save scroll position
    override fun onPause() {
        super.onPause()
        var listView = binding.chaptersList
        scrollPosition = listView.onSaveInstanceState()!!
    }

    // Overridden func which will restore scroll position
    override fun onResume() {
        super.onResume()
        val aboutMangaViewmodel = ViewModelProvider(requireActivity()).get(AboutMangaViewModel::class.java)

        binding.chaptersList.invalidateViews()
        binding.chaptersList.onRestoreInstanceState(scrollPosition)
        aboutMangaViewmodel.adapter?.lastViewedChapter = aboutMangaViewmodel.manga.lastViewedChapter
        aboutMangaViewmodel.adapter?.notifyDataSetChanged()
    }
}
