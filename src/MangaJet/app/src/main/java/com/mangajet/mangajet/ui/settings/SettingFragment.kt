package com.mangajet.mangajet.ui.settings

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mangajet.mangajet.authorization.AuthorizationActivity
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.databinding.SettingsFragmentBinding
import com.mangajet.mangajet.mangareader.MangaReaderActivity

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
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        var authorizationButt = binding.authorizationButton
        activity?.let {
            var tmpIt = it;
            authorizationButt.setOnClickListener {
                //Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show()}
                val intent = Intent(tmpIt, AuthorizationActivity::class.java)
                intent.putExtra("URL", Librarian.LibraryName.Mangachan.resource)
                startActivity(intent)
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
