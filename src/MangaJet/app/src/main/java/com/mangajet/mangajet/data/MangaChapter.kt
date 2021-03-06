package com.mangajet.mangajet.data

import org.json.JSONArray
import org.json.JSONObject

// Class that represents one chapter of specific, stores pages of this manga
class MangaChapter {
    public var manga: Manga      // Manga from which this chapter created
    public var id: String        // Id of this Chapter
    public var name: String    // Chapter name (can be zero-size string)
    public var fullName: String    // Chapter name (can be zero-size string)
    public var lastViewedPage = 0 // Number of last viewed page
    private var pagesNumber = 0   // Number of pages in this chapter
    private var pagesURLs = arrayListOf<String>()

    // Constructor for Libraries
    constructor(manga: Manga, id: String, name: String = "", fullname: String = name) {
        this.manga = manga
        this.id = id
        this.name = name
        this.fullName = fullname
    }

    // Constructor for JSON
    constructor(manga: Manga, id: String, urls : ArrayList<String>, name: String = "", fullname: String = name) {
        this.manga = manga
        this.id = id
        this.name = name
        this.fullName = fullname
        this.pagesURLs = urls
        this.pagesNumber = urls.size
    }

    // Function for safely retrieving amount of pages
    // MAY THROW MangaJetException
    public fun getPagesNum() : Int {
        if (pagesNumber <= 0)
            updateInfo()
        return pagesNumber
    }

    // Function to fill all manga info except chapters
    // MAY THROW MangaJetException
    fun updateInfo(force : Boolean = false){
        if (!force && pagesURLs.isNotEmpty())
            return

        pagesURLs.clear()
        val chaptersInfoJSON = JSONArray(manga.library.getChapterInfo(manga.id, id)) // Exception may be thrown here
        for (i in 0 until chaptersInfoJSON.length()) {
            pagesURLs.add(chaptersInfoJSON[i].toString())
        }
        pagesNumber = pagesURLs.size
    }

    // Function for safely retrieving amount of pages
    // MAY THROW MangaJetException, ArrayIndexOutOfBoundsException
    public fun getPage(pageNumber : Int) : MangaPage {
        updateInfo()
        if (pagesNumber <= 0)
            getPagesNum()
        if (pageNumber < 0 || pageNumber >= pagesNumber)
            throw ArrayIndexOutOfBoundsException("Bad page index") // Exception may be thrown here
        return MangaPage(pagesURLs[pageNumber], manga.library.getHeadersForDownload())
    }

    // Function for safely retrieving JSON of pages
    public fun getJSON() : String {
        var chapterJSON = JSONObject()
        chapterJSON.put("pages",  JSONArray(pagesURLs))
        chapterJSON.put("name", name)
        chapterJSON.put("fullname", fullName)
        return chapterJSON.toString()
    }

    // Function which will check if chapter downloaded
    fun isLoadedInDownloads() : Boolean {
        if (pagesNumber <= 0)
            return false

        for (i in 0 until pagesURLs.size) {
            var page = MangaPage(pagesURLs[i], manga.library.getHeadersForDownload())
            if (!StorageManager.isExist(page.localPath, StorageManager.FileType.DownloadedPages))
                return false
        }

        return true
    }
    // Delete chapter from localStorage
    // MAY THROW MangaJetException
    fun delete() {
        for (i in 0 until pagesNumber) {
            MangaPage(pagesURLs[i], manga.library.getHeadersForDownload()).removeFileIfExist()
        }
    }
}
