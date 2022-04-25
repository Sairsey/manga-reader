package com.mangajet.mangajet.ui.settings

import android.R
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mangajet.mangajet.BuildConfig
import com.mangajet.mangajet.databinding.SettingsFragmentBinding
import com.mangajet.mangajet.log.Logger
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
        const val STORAGE_ID = 2        // Storage submenu id
        const val STORAGE_PATH_ID = 3   // Storage path dialog id
        const val ABOUTAPP_ID = 4       // About app submenu id
        const val TESTBUTTONS_ID = 5    // Test button submenu id
    }

    // Binding tool to get layout elements
    private val binding get() = _binding!!

    // List adapter for "settings" list inner class
    inner class SettingsListAdapter(context: Context,
                                   private val resourceLayout: Int,
                                   items: ArrayList<SettingListElement>) :
        ArrayAdapter<SettingListElement>(context, resourceLayout, items) {
        // List context
        private val mContext: Context = context

        // Function which will fill every list element
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var v: View? = convertView
            if (v == null) {
                val vi: LayoutInflater = LayoutInflater.from(mContext)
                v = vi.inflate(resourceLayout, null)
            }

            val p = getItem(position)
            if (p != null) {
                val menuIcon = v?.findViewById<ImageView>(R.id.menuIcon)
                val menuTitle = v?.findViewById<TextView>(R.id.menuName)

                menuIcon?.setImageResource(p.mIcon)
                menuTitle?.text = p.mName
            }
            return v!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View? {
        Logger.log("Settings opened")
        val settingsViewModel =
            ViewModelProvider(this).get(SettingViewModel::class.java)

        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val dataSettingsList = binding.settingsOptionsList
        activity?.let {
            val adapter = SettingsListAdapter(it,
                R.layout.setting_list_element,
                settingsViewModel.settingsElements)

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
                    STORAGE_ID -> {
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
