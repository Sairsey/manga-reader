package com.mangajet.mangajet.data

import org.json.JSONArray
import org.json.JSONObject

// Class that represents one specific manga, stores info about it(name, author, genre...) and chapters of this manga
class Manga {
    val id: String               // This manga unique identifier
    val library: AbstractLibrary // Library which this manga belongs

    //manga info
    var originalName: String = ""                    // Manga's original name
    var russianName: String = ""                     // Manga's russian name
    var author: String = ""                          // Manga's author name
    var tags: Array<String> = emptyArray()           // Array of manga's tags
    var description: String = ""                     // Manga's description
    var rating: Double = 0.0                         // Manga's rating(some libraries don't support it)
    var cover: String = ""                           // URL to manga's cover
    var chapters: Array<MangaChapter> = emptyArray() // Array of this manga's chapters
    var lastViewedChapter: Int = 0                   // Index of last viewed chapter

    // Constructor for Libraries
    constructor(library: AbstractLibrary, id: String) {
        this.library = library
        this.id = id
    }

    // Utility function to get some data from JSON string by tag nam
    private fun getDataByTag(text: String, tag: String) : String {
        val tagToFind = "$tag\":\""
        var start = text.indexOf(tagToFind) + tagToFind.length
        return text.subSequence(start, text.indexOf("\"", start)).toString()
    }

    // Function to fill all manga info except chapters
    // MAY THROW MangaJetException
    fun updateInfo(){
        val mangaInfo = library.getMangaInfo(id) // Exception may be thrown here
        this.originalName = getDataByTag(mangaInfo, "name")           // Get manga's name
        this.cover = getDataByTag(mangaInfo, "cover")                 // Get manga's cover
        this.russianName = getDataByTag(mangaInfo, "rus_name")        // Get manga's russian name
        this.author = getDataByTag(mangaInfo, "author")               // Get manga's author
        val rating = getDataByTag(mangaInfo, "rating")                // Get manga's rating
        if (rating.isEmpty())// Some libraries doesn't support rating system
            this.rating = 0.0
        else
            this.rating = rating.toDouble()
        this.description = getDataByTag(mangaInfo, "description")     // Get manga's description
            .replace("\\r", "")
            .replace("\\n", "")
            .replace("\\t", "")
            .trim()
        //Get Tags
        val tagToFind = "tags\":["
        var start = mangaInfo.indexOf(tagToFind) + tagToFind.length
        this.tags = mangaInfo.subSequence(start, mangaInfo.indexOf("]", start)).toString()
            .replace("\"", "").split(",").toTypedArray()
    }

    // Function to fill chapters array of manga
    // MAY THROW MangaJetException
    fun updateChapters(){
        chapters = library.getMangaChapters(this) // Exception may be thrown here
    }

    // Function dump information about manga as JSON string
    fun toJSON() : String {
        val json = JSONObject()
        json.put("id", this.id)
        json.put("library", this.library.getURL())
        json.put("name", this.originalName)
        json.put("cover", this.cover)
        json.put("rus_name", this.russianName)
        json.put("author", this.author)
        json.put("description", this.description)
        if(rating != 0.0)
             json.put("rating", this.rating)
        val tagArray = JSONArray(this.tags)
        json.put("tags", tagArray)
        json.put("chaptersAmount", this.chapters.size)
        if (this.lastViewedChapter < 0 || this.lastViewedChapter >= this.chapters.size)
            throw ArrayIndexOutOfBoundsException("Bad last viewed chapter index")
        json.put("lastViewedChapter", this.lastViewedChapter)
        json.put("lastViewedPage", this.chapters[this.lastViewedChapter].lastViewedPage)
        return json.toString()
    }
}
