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
import com.mangajet.mangajet.BuildConfig
import com.mangajet.mangajet.databinding.SettingsFragmentBinding
import com.mangajet.mangajet.ui.settings.options.AboutAppActivity
import com.mangajet.mangajet.ui.settings.options.CacheSettingActivity
import com.mangajet.mangajet.ui.settings.options.MangaAuthorizationActivity
import com.mangajet.mangajet.ui.settings.options.StoragePathDialog
import com.mangajet.mangajet.ui.settings.options.TesterButtonsActivity
import com.mangajet.mangajet.ui.settings.options.ThemePickerDialog

// Class which represents "Settings" fragment of MainActivity
class SettingFragment : Fragment() {
    private var _binding: SettingsFragmentBinding? = null
    companion object {
        const val AUTHORIZATION_ID = 0  // Authorization submenu id
        const val THEME_PICKER_ID = 1   // Theme picker dialog id
        const val CACHE_ID = 2          // Cache submenu id
        const val BACKUP_ID = 3         // Backup submenu id
        const val STORAGE_PATH_ID = 4   // Storage path dialog id
        const val ABOUTAPP_ID = 5       // About app submenu id
        const val TESTBUTTONS_ID = 6    // Test button submenu id
    }

    // Binding tool to get layout elements
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
                    AUTHORIZATION_ID -> {
                        var intent = Intent(it, MangaAuthorizationActivity::class.java)
                        startActivity(intent)
                    }
                    THEME_PICKER_ID -> {
                        val myDialogFragment = ThemePickerDialog()
                        val manager = fragmentManager
                        if (manager != null) {
                            myDialogFragment.show(manager, "Theme picker dialog")
                        }
                    }
                    CACHE_ID -> {
                        var intent = Intent(it, CacheSettingActivity::class.java)
                        startActivity(intent)
                    }
                    BACKUP_ID -> {
                        var intent = Intent(it, CacheSettingActivity::class.java)
                        startActivity(intent)
                    }
                    STORAGE_PATH_ID -> {
                        val myDialogFragment = StoragePathDialog()
                        val manager = fragmentManager
                        if (manager != null) {
                            myDialogFragment.show(manager, "Storage path dialog")
                        }
                    }
                    ABOUTAPP_ID -> {
                        var intent = Intent(it, AboutAppActivity::class.java)
                        startActivity(intent)
                    }
                    TESTBUTTONS_ID -> {
                        if (BuildConfig.VERSION_NAME.endsWith("dev")) {
                            var intent = Intent(it, TesterButtonsActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
            }
        }

        return root
    }
}
