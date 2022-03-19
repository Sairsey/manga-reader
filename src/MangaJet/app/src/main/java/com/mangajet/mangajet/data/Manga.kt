package com.mangajet.mangajet.data

// Class that represents one specific manga, stores info about it(name, author, genre...) and chapters of this manga
class Manga {
    val i = 1// To fool detect
    val id : String

    // Constructor for Libraries
    constructor(library: AbstractLibrary, id: String) {
        library.getURL()
        this.id = id
    }
}
