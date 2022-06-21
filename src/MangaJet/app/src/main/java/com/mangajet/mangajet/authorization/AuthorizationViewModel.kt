package com.mangajet.mangajet.mangareader

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.data.libraries.AbstractLibrary
import com.mangajet.mangajet.data.Librarian

class AuthorizationViewModel : ViewModel() {
    lateinit var url : String
    lateinit var key : String
    var library : AbstractLibrary? = null

    fun initAuthScreen(intent : Intent) {
        url = intent.getStringExtra("URL").toString()
        key = url
        library = Librarian.getLibrary(key)
    }
}
