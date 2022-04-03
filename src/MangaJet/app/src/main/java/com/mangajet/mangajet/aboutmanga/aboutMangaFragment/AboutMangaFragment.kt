package com.mangajet.mangajet.aboutmanga.aboutMangaFragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mangajet.mangajet.R
import com.mangajet.mangajet.aboutmanga.AboutMangaActivity
import com.mangajet.mangajet.aboutmanga.AboutMangaViewModel
import com.mangajet.mangajet.data.MangaJetException
import com.mangajet.mangajet.data.MangaPage
import com.mangajet.mangajet.databinding.AboutMangaFragmentBinding
import com.mangajet.mangajet.mangareader.MangaReaderActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


// "About manga" fragment with main information
class AboutMangaFragment : Fragment() {
    // Async job for loading bitmap
    var job : Job? = null
    private var _binding: AboutMangaFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AboutMangaFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    // Function witch will decode bitmap async
    fun loadBitmap(cover : MangaPage): Bitmap? {
        try {
            val imageFile = cover.getFile() // Catch ex here
            return BitmapFactory.decodeFile(imageFile.absolutePath)
        }
        catch (ex: MangaJetException) {
            return null
        }
    }

    override fun onStart() {
        super.onStart()
        val aboutMangaViewmodel = ViewModelProvider(requireActivity()).get(AboutMangaViewModel::class.java)

        val cover = MangaPage(aboutMangaViewmodel.manga.cover)
        cover.upload()

        binding.titleText.setText( aboutMangaViewmodel.manga.originalName + " (" +
                aboutMangaViewmodel.manga.russianName + ")")
        binding.authorText.setText(aboutMangaViewmodel.manga.author)
        binding.fullDescriptionText.setText(aboutMangaViewmodel.manga.description)

        job = GlobalScope.launch(Dispatchers.IO) {
            val bitmap = loadBitmap(cover)
            withContext(Dispatchers.Main) {
                if (bitmap != null)
                    binding.coverManga.setImageBitmap(bitmap)
            }
        }

        val buttonToRead = binding.readMangaButton
        buttonToRead.setOnClickListener{
            val intent = Intent(activity, MangaReaderActivity::class.java)
            startActivity(intent)}

        // Tags TextView generator
        val tagsLayout = binding.tagsLayout
        tagsLayout.removeAllViews()
        aboutMangaViewmodel.manga.tags.forEach {
            val newTextView = TextView(activity)
            newTextView.setText(it)
            newTextView.setPadding(
                AboutMangaActivity.PADDING_HORZ,
                AboutMangaActivity.PADDING_VERT,
                AboutMangaActivity.PADDING_HORZ,
                AboutMangaActivity.PADDING_VERT
            )
            newTextView.setTextColor(resources.getColor(R.color.primary))
            newTextView.setBackgroundResource(R.drawable.tag_border)
            tagsLayout.addView(newTextView)
        }
    }
}
