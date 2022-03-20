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

    // Utility function to fill data fields of manga class from json file
    private fun fillMangaFromJSON(json: JSONObject){
        this.originalName = json.optString("name")       // Get manga's name
        this.cover = json.optString("cover")             // Get manga's cover
        this.russianName = json.optString("rus_name")    // Get manga's russian name
        this.author = json.optString("author")           // Get manga's author
        this.description = json.optString("description") // Get manga's description
            .replace("\\r", "")
            .replace("\\n", "")
            .replace("\\t", "")
            .trim()
        this.rating = json.optDouble("rating", 0.0) // Get rating
        // Get tags
        val tags = json.optJSONArray("tags")
        val list = ArrayList<String>()
        for(i in 0 until tags.length()){
            list.add(tags.optString(i))
        }
        this.tags = list.toTypedArray()
    }

    // Constructor from JSON string
    // MAY THROW MangaJetException
    constructor(jsonStr: String){
        val json = JSONObject(jsonStr)
        this.id = json.optString("id")
        try {
            this.library = Librarian.getLibrary(
                Librarian.LibraryName.from(json.optString("library"))
            )!!
        }
        catch (expected: NullPointerException) {
            throw MangaJetException("Unknown library")
        }
        this.lastViewedChapter = json.optInt("lastViewedChapter", 0)
        fillMangaFromJSON(json)
    }

    // Function to fill all manga info except chapters
    // MAY THROW MangaJetException
    fun updateInfo(){
        fillMangaFromJSON(JSONObject(library.getMangaInfo(id))) // Exception may be thrown here
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
            this.lastViewedChapter = 0
        json.put("lastViewedChapter", this.lastViewedChapter)
        json.put("lastViewedPage", this.chapters[this.lastViewedChapter].lastViewedPage)
        return json.toString()
    }
}
