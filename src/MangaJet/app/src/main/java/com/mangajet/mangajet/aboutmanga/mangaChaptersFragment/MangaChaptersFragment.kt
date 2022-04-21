package com.mangajet.mangajet.aboutmanga.mangaChaptersFragment

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Parcelable
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.ImageView
import android.widget.Button
import android.widget.Toast
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.mangajet.mangajet.MangaJetApp
import com.mangajet.mangajet.R
import com.mangajet.mangajet.aboutmanga.AboutMangaViewModel
import com.mangajet.mangajet.data.MangaChapter
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.databinding.MangaChaptersFragmentBinding
import com.mangajet.mangajet.log.Logger
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
    // job for async waiting of 'initManga()' function
    var job : Job? = null
    // about manga viewmodel for management UI elements
    lateinit var aboutMangaViewmodel : AboutMangaViewModel

    // List adapter for "chapters" list inner class
    inner class ChapterListAdapter(context: Context,
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
                val vi: LayoutInflater = LayoutInflater.from(mContext)
                v = vi.inflate(resourceLayout, null)
            }

            val pos = if (!aboutMangaViewmodel.isChaptersListReversed)
                position
            else
                (aboutMangaViewmodel.manga.chapters.size - 1) - position

            val p = getItem(pos)
            if (p != null) {
                val chapter = v?.findViewById<TextView>(R.id.chapterTitle)
                val icon = v?.findViewById<ImageView>(R.id.viewedIcon)
                val button =  v?.findViewById<Button>(R.id.downloadChapter)
                if (p.name.isNotEmpty())
                    chapter?.setText(context.getString(R.string.chapter_default_name) + " " +
                            (pos + 1).toString() + ": " + p.name)
                else
                    chapter?.setText(context.getString(R.string.chapter_default_name) + " " +
                            (pos + 1).toString())
                button?.setOnClickListener {
                    try {
                        for(i in 0 until p.getPagesNum()){
                            p.getPage(i).getFile()
                        }
                        p.manga.saveToFile()
                        Toast.makeText(mContext, "Done", Toast.LENGTH_SHORT).show()
                    }
                    catch (ex: MangaJetException){
                        Logger.log("Catch MJE while filling mangaChapterFragment: " + ex.message)
                        Toast.makeText(mContext, ex.message, Toast.LENGTH_SHORT).show()
                    }
                }
                if (!aboutMangaViewmodel.isChaptersListReversed) {
                    if (pos < lastViewedChapter)
                        icon?.setImageResource(R.drawable.ic_opened_book)
                    else
                        icon?.setImageResource(R.drawable.ic_closed_book)
                }
                else {
                    if (pos < lastViewedChapter)
                        icon?.setImageResource(R.drawable.ic_opened_book)
                    else
                        icon?.setImageResource(R.drawable.ic_closed_book)
                }
            }
            return v!!
        }
    }

    // Binding tool for "MangaChapterFragment"
    private var _binding: MangaChaptersFragmentBinding? = null

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    // Function which will reverse all list with chapters in chaptersList
    private fun reverseChaptersList() {
        aboutMangaViewmodel.isChaptersListReversed = !aboutMangaViewmodel.isChaptersListReversed
        aboutMangaViewmodel.adapter?.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reverseList -> reverseChaptersList()
        }

        return super.onOptionsItemSelected(item)
    }

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

    // Function which will init list witch chapters adapter
    private fun initChaptersListAdapter() {
        activity?.let {
            aboutMangaViewmodel.adapter = ChapterListAdapter(
                it,
                R.layout.chapter_list_element,
                aboutMangaViewmodel.manga.chapters,
                aboutMangaViewmodel.manga.lastViewedChapter)

            binding.chaptersList.adapter = aboutMangaViewmodel.adapter

            binding.chaptersList.setOnItemClickListener { parent, view, position, id ->
                val pos = if (!aboutMangaViewmodel.isChaptersListReversed)
                    id
                else
                    (aboutMangaViewmodel.manga.chapters.size - 1) - id

                if (aboutMangaViewmodel.isInited) {
                    val intent = Intent(it, MangaReaderActivity::class.java)
                    MangaJetApp.currentManga = aboutMangaViewmodel.manga
                    MangaJetApp.currentManga!!.lastViewedChapter = pos.toInt()
                    MangaJetApp.currentManga!!
                        .chapters[MangaJetApp.currentManga!!.lastViewedChapter]
                        .lastViewedPage = 0

                    // check if we need authorization
                    val pagesCnt = MangaJetApp.currentManga!!
                        .chapters[MangaJetApp.currentManga!!.lastViewedChapter].getPagesNum()
                    if (pagesCnt > 0)
                        startActivity(intent)
                    else
                        Toast.makeText(it,
                        "Can't open chapter: maybe its empty or you need to authorizes on site",
                            Toast.LENGTH_SHORT).show()
                }
                else
                    Toast.makeText(it,
                        "Can't open manga before all chapters are updated...",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function which will async wait for 'initManga()' function finish
    private suspend fun createChaptersList() {
        aboutMangaViewmodel.job?.join()

        withContext(Dispatchers.Main) {
            initChaptersListAdapter()
            aboutMangaViewmodel.adapter?.notifyDataSetChanged()
            binding.chaptersList.visibility = View.VISIBLE
            binding.loadIndicator.hide()
        }
    }


    override fun onStart() {
        super.onStart()
        aboutMangaViewmodel = ViewModelProvider(requireActivity()).get(AboutMangaViewModel::class.java)
        var listView = binding.chaptersList

        binding.loadIndicator.show()
        binding.chaptersList.visibility = View.INVISIBLE

        // Start async waiting of chapters loading job if its not loaded yet
        if (aboutMangaViewmodel.job?.isCompleted == false) {
            job = GlobalScope.launch(Dispatchers.Default) {
                createChaptersList()
            }
        }
        // if loaded -> show it
        else {
            binding.loadIndicator.hide()
            binding.chaptersList.visibility = View.VISIBLE
            aboutMangaViewmodel.adapter?.notifyDataSetChanged()
        }

        scrollPosition = listView.onSaveInstanceState()!!
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

        val mToolbar = activity?.findViewById<MaterialToolbar>(R.id.aboutMangaToolbar)
        mToolbar?.inflateMenu(R.menu.chapters_fragment_menu)
        mToolbar?.setOnMenuItemClickListener{
            onOptionsItemSelected(it)
        }

        if (aboutMangaViewmodel.isInited) {
            binding.chaptersList.visibility = View.VISIBLE
            binding.loadIndicator.hide()

            if (aboutMangaViewmodel.adapter == null)
                initChaptersListAdapter()
        }
        binding.chaptersList.onRestoreInstanceState(scrollPosition)
        aboutMangaViewmodel.adapter?.lastViewedChapter = aboutMangaViewmodel.manga.lastViewedChapter
        aboutMangaViewmodel.adapter?.notifyDataSetChanged()
    }
}
