package com.mangajet.mangajet.data

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import javax.net.ssl.ManagerFactoryParameters

// Class that represents one chapter of specific, stores pages of this manga
class MangaChapter {
    public var manga: Manga      // Manga from which this chapter created
    public var id: String        // Id of this Chapter
    public var lastViewedPage = 0 // Number of last viewed page
    private var pagesNumber = -1  // Number of pages in this chapter
    private var pagesURLs = arrayListOf<String>()

    // Constructor for Libraries
    constructor(manga: Manga, id: String) {
        this.manga = manga
        this.id = id
        try {
            updateInfo()
        } catch (ex : MangaJetException) {
            Log.v(Log.DEBUG.toString(), ex.toString())
        }
    }

    // Constructor for Libraries
    constructor(manga: Manga, id: String, urls : ArrayList<String>) {
        this.manga = manga
        this.id = id
        this.pagesURLs = urls
        this.pagesNumber = urls.size
    }

    // Function for safely retrieving amount of pages
    // MAY THROW MangaJetException
    public fun getPagesNum() : Int {
        if (pagesNumber == -1)
            updateInfo()
        return pagesNumber
    }

    // Function to fill all manga info except chapters
    // MAY THROW MangaJetException
    fun updateInfo(){
        val chaptersInfoJSON = JSONArray(manga.library.getChapterInfo(manga.id, id)) // Exception may be thrown here
        for (i in 0 until chaptersInfoJSON.length()) {
            pagesURLs.add(chaptersInfoJSON[i].toString())
        }
        pagesNumber = pagesURLs.size
    }

    // Function for safely retrieving amount of pages
    // MAY THROW MangaJetException, ArrayIndexOutOfBoundsException
    public fun getPage(pageNumber : Int) : MangaPage {
        if (pagesNumber == -1)
            getPagesNum()
        if (pageNumber < 0 || pageNumber >= pagesNumber)
            throw ArrayIndexOutOfBoundsException("Bad page index") // Exception may be thrown here
        return MangaPage(pagesURLs[pageNumber])
    }

    // Function for safely retrieving JSON of pages
    public fun getJSON() : String {
        return JSONArray(pagesURLs).toString()
    }
}
