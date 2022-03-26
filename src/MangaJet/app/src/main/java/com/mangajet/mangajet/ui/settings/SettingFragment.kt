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
import com.mangajet.mangajet.databinding.SettingsFragmentBinding
import com.mangajet.mangajet.ui.settings.options.AboutAppActivity
import com.mangajet.mangajet.ui.settings.options.CacheSettingActivity
import com.mangajet.mangajet.ui.settings.options.MangaAuthorizationActivity
import com.mangajet.mangajet.ui.settings.options.ThemePickerDialog
import com.mangajet.mangajet.ui.settings.options.StoragePathDialog


// Class which represents "Settings" fragment of MainActivity
class SettingFragment : Fragment() {
    private var _binding: SettingsFragmentBinding? = null
    companion object {
        fun newInstance() = SettingFragment()

        const val AUTHORIZATION_ID = 0
        const val THEME_PICKER_ID = 1
        const val CACHE_ID = 2
        const val BACKUP_ID = 3
        const val STORAGE_PATH_ID = 4
        const val ABOUTAPP_ID = 5
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
                when (id.toInt()) {
                    Companion.AUTHORIZATION_ID -> {
                        var intent : Intent = Intent(it, MangaAuthorizationActivity::class.java)
                        startActivity(intent)
                    }
                    Companion.THEME_PICKER_ID -> {
                        val myDialogFragment = ThemePickerDialog()
                        val manager = fragmentManager
                        if (manager != null) {
                            myDialogFragment.show(manager, "Theme picker dialog")
                        }
                    }
                    Companion.CACHE_ID -> {
                        var intent : Intent = Intent(it, CacheSettingActivity::class.java)
                        startActivity(intent)
                    }
                    Companion.BACKUP_ID -> {
                        var intent : Intent = Intent(it, CacheSettingActivity::class.java)
                        startActivity(intent)
                    }
                    Companion.STORAGE_PATH_ID -> {
                        val myDialogFragment = StoragePathDialog()
                        val manager = fragmentManager
                        if (manager != null) {
                            myDialogFragment.show(manager, "Storage path dialog")
                        }
                    }
                    Companion.ABOUTAPP_ID -> {
                        var intent : Intent = Intent(it, AboutAppActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SettingViewModel::class.java)
        // TODO Use the ViewModel
    }
}
