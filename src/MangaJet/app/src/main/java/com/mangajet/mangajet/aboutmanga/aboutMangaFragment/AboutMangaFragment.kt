package com.mangajet.mangajet.aboutmanga.aboutMangaFragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.appbar.MaterialToolbar
import com.mangajet.mangajet.R
import com.mangajet.mangajet.aboutmanga.AboutMangaActivity
import com.mangajet.mangajet.aboutmanga.AboutMangaViewModel
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.MangaPage
import com.mangajet.mangajet.databinding.AboutMangaFragmentBinding
import com.mangajet.mangajet.log.Logger
import com.mangajet.mangajet.mangareader.MangaReaderActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// "About manga" fragment with main information
class AboutMangaFragment : Fragment() {
    private var _binding: AboutMangaFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Async job for loading bitmap
    var job : Job? = null

    // Viewmodel with data
    private lateinit var aboutMangaViewmodel : AboutMangaViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AboutMangaFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    // Function witch will decode bitmap async
    private fun loadBitmap(cover : MangaPage): Bitmap? {
        return try {
            val imageFile = cover.getFile() // Catch ex here
            BitmapFactory.decodeFile(imageFile.absolutePath)
        } catch (ex: MangaJetException) {
            Logger.log("Catch MJE while decoding bitmap of " + cover.url + " : "
                    + ex.message, Logger.Lvl.WARNING)
            null
        }
    }

    private fun createNewTag(tagsLayout : FlexboxLayout, tagName : String) {
        val newTextView = TextView(activity)
        newTextView.text = tagName
        newTextView.setPadding(
            AboutMangaActivity.PADDING_HORZ,
            AboutMangaActivity.PADDING_VERT,
            AboutMangaActivity.PADDING_HORZ,
            AboutMangaActivity.PADDING_VERT
        )

        // make tags clickable
        newTextView.isClickable = true
        newTextView.setTextColor(resources.getColor(R.color.primary))
        newTextView.setBackgroundResource(R.drawable.tag_border)

        // make action on click (return result for tag-search)
        newTextView.setOnClickListener {
            val bundleSignal = Bundle()
            bundleSignal.putCharSequence("tag", newTextView.text)
            setFragmentResult("TAG_TAPPED", bundleSignal)
        }
        tagsLayout.addView(newTextView)
    }

    override fun onStart() {
        super.onStart()

        aboutMangaViewmodel = ViewModelProvider(requireActivity()).get(AboutMangaViewModel::class.java)
        Logger.log("About " + aboutMangaViewmodel.manga.id + " fragment opened")
        val cover = MangaPage(aboutMangaViewmodel.manga.cover,
            aboutMangaViewmodel.manga.library.getHeadersForDownload())
        // this can only fail if we do not have storage permission
        // We have blocking dialog in this case, so it someone still
        // manges to go here, I think we should crash
        cover.upload()

        // set manga title
        binding.titleText.setText( aboutMangaViewmodel.manga.originalName + " (" +
                aboutMangaViewmodel.manga.russianName + ")")
        // set author
        binding.authorText.setText(aboutMangaViewmodel.manga.author)
        // set description
        binding.fullDescriptionText.setText(aboutMangaViewmodel.manga.description)
        // set source URI
        binding.source.isClickable = true
        binding.source.setText(aboutMangaViewmodel.manga.library.getURL())
        binding.source.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(binding.source.text.toString()))
            startActivity(browserIntent)
        }
        // set manga rating
        if (aboutMangaViewmodel.manga.rating != 0.0) {
            binding.ratingNum.setText(aboutMangaViewmodel.manga.rating.toString())
        }
        // load manga cover
        job = GlobalScope.launch(Dispatchers.IO) {
            // POTENTIAL EXCEPTION and ERROR
            // Cover isn't downloaded but we try to draw it => terminate
            val bitmap = loadBitmap(cover)
            withContext(Dispatchers.Main) {
                if (bitmap != null)
                    binding.coverManga.setImageBitmap(bitmap)
            }
        }

        // set button "Read manga"
        val buttonToRead = binding.readMangaButton
        buttonToRead.setOnClickListener{
            if (aboutMangaViewmodel.isInited && aboutMangaViewmodel.manga.chapters.isNotEmpty()) {
                val intent = Intent(activity, MangaReaderActivity::class.java)
                startActivity(intent)
            }
            else
                Toast.makeText(context,
                    "Can't open manga before all chapters are updated...",
                    Toast.LENGTH_SHORT).show()
        }

        // Tags TextView generator
        val tagsLayout = binding.tagsLayout
        tagsLayout.removeAllViews()
        aboutMangaViewmodel.manga.tags.forEach {
            createNewTag(tagsLayout, it)
        }
    }

    override fun onResume() {
        super.onResume()

        val mToolbar = activity?.findViewById<MaterialToolbar>(R.id.aboutMangaToolbar)
        mToolbar?.menu?.clear()
    }
}
