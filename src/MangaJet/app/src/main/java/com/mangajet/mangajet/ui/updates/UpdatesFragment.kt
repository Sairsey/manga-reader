package com.mangajet.mangajet.ui.updates

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mangajet.mangajet.R

// Class which represents "Updates" fragment of MainActivity
class UpdatesFragment : Fragment() {

    companion object {
        fun newInstance() = UpdatesFragment()
    }

    private lateinit var viewModel: UpdatesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.updates_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(UpdatesViewModel::class.java)
        // TODO Use the ViewModel
    }

}
