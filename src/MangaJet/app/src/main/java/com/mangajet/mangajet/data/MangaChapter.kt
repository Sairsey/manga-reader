package com.mangajet.mangajet.data

import org.json.JSONObject
import javax.net.ssl.ManagerFactoryParameters

// Class that represents one chapter of specific, stores pages of this manga
class MangaChapter {
    public var manga: Manga      // Manga from which this chapter created
    public var id: String        // Id of this Chapter
    public var lastViewedPage = 0 // Number of last viewed page
    private var pagesNumber = -1  // Number of pages in this chapter

    // Constructor for Libraries
    constructor(manga: Manga, id: String) {
        this.manga = manga
        this.id = id
    }

    // Function for safely retrieving amount of pages
    // MAY THROW MangaJetException
    public fun getPagesNum() : Int {
        if (pagesNumber == -1)
            pagesNumber = manga.library.getChaptersNumOfPages(manga.id, id) // Exception may be thrown here
        return pagesNumber
    }

    // Function for safely retrieving amount of pages
    // MAY THROW MangaJetException, ArrayIndexOutOfBoundsException
    public fun getPage(pageNumber : Int) : MangaPage {
        if (pagesNumber == -1)
            getPagesNum()
        if (pageNumber < 0 || pageNumber >= pagesNumber)
            throw ArrayIndexOutOfBoundsException("Bad page index") // Exception may be thrown here
        return manga.library.getChapterPage(manga.id, id, pageNumber) // Exception may be thrown here
    }
}
