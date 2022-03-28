package com.mangajet.mangajet.mangareader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga

// Class which represents "Manga Reader" ViewModel
class MangaReaderViewModel : ViewModel {
    val manga: Manga

    constructor() {
        // or via search
        manga = Librarian
            .getLibrary(Librarian.LibraryName.Mangachan)!!
            .createMangaById("55817-blade-of-demon-destruction.html")
    }
}
