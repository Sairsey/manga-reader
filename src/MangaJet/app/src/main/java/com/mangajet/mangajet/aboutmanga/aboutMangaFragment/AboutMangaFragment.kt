package com.mangajet.mangajet.aboutmanga.aboutMangaFragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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
    private var _binding: AboutMangaFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // Async job for loading bitmap
    var job : Job? = null

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
            null
        }
    }

    override fun onStart() {
        super.onStart()
        val aboutMangaViewmodel = ViewModelProvider(requireActivity()).get(AboutMangaViewModel::class.java)

        val cover = MangaPage(aboutMangaViewmodel.manga.cover,
            aboutMangaViewmodel.manga.library.getHeadersForDownload())
        // this can only fail if we do not have storage permission
        // We have blocking dialog in this case, so it someone still
        // manges to go here, I think we should crash
        cover.upload()

        binding.titleText.setText( aboutMangaViewmodel.manga.originalName + " (" +
                aboutMangaViewmodel.manga.russianName + ")")
        binding.authorText.setText(aboutMangaViewmodel.manga.author)
        binding.fullDescriptionText.setText(aboutMangaViewmodel.manga.description)

        job = GlobalScope.launch(Dispatchers.IO) {
            // POTENTIAL EXCEPTION and ERROR
            // Cover isn't downloaded but we try to draw it => terminate
            val bitmap = loadBitmap(cover)
            withContext(Dispatchers.Main) {
                if (bitmap != null)
                    binding.coverManga.setImageBitmap(bitmap)
            }
        }

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
            val newTextView = TextView(activity)
            newTextView.text = it
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
