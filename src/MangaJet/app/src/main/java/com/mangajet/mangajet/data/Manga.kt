package com.mangajet.mangajet.data

// Class that represents one specific manga, stores info about it(name, author, genre...) and chapters of this manga
class Manga {
    val id: String               // This manga unique identifier
    val library: AbstractLibrary // Library which this manga belongs

    // Constructor for Libraries
    constructor(library: AbstractLibrary, id: String) {
        this.library = library
        this.id = id
    }
}
