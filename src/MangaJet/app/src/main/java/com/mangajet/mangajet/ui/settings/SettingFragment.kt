package com.mangajet.mangajet.ui.settings

import android.R
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.mangajet.mangajet.aboutmanga.AboutMangaActivity
import com.mangajet.mangajet.authorization.AuthorizationActivity
import com.mangajet.mangajet.databinding.SettingsFragmentBinding
import com.mangajet.mangajet.mangareader.MangaReaderActivity
import com.mangajet.mangajet.ui.search.SearchViewModel
import com.mangajet.mangajet.ui.settings.options.CacheSettingActivity

// Class which represents "Settings" fragment of MainActivity
class SettingFragment : Fragment() {
    private var _binding: SettingsFragmentBinding? = null
    companion object {
        fun newInstance() = SettingFragment()
    }

    private lateinit var viewModel: SettingViewModel
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View? {
        val settingsViewModel =
            ViewModelProvider(this).get(SettingViewModel::class.java)

        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val dataSettingsList = binding.settingsOptionsList
        activity?.let {
            val adapter = ArrayAdapter<String>(
                it,
                R.layout.simple_list_item_1,
                settingsViewModel.dataOptionsNames
            )

            dataSettingsList.adapter = adapter
            dataSettingsList.setOnItemClickListener{ parent, view, position, id ->
                var intent : Intent = Intent(it, CacheSettingActivity::class.java)
                when (id.toInt()) {
                    0 -> intent = Intent(it, CacheSettingActivity::class.java)
                    1 -> intent = Intent(it, CacheSettingActivity::class.java)
                }
                startActivity(intent)}
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SettingViewModel::class.java)
        // TODO Use the ViewModel
    }

}
