package com.mangajet.mangajet.mangareader

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.data.libraries.AbstractLibrary
import com.mangajet.mangajet.data.Librarian

class AuthorizationViewModel : ViewModel() {
    lateinit var url : String
    lateinit var key : Librarian.LibraryName
    var library : AbstractLibrary? = null

    fun initAuthScreen(intent : Intent) {
        url = intent.getStringExtra("URL").toString()
        key = Librarian.LibraryName.from(url)
        library = Librarian.getLibrary(key)
    }
}
