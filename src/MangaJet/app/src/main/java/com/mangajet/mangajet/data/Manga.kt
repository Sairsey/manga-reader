package com.mangajet.mangajet.data

import com.mangajet.mangajet.data.libraries.AbstractLibrary
import com.mangajet.mangajet.log.Logger
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

// Class that represents one specific manga, stores info about it(name, author, genre...) and chapters of this manga
class Manga {
    lateinit var id: String               // This manga unique identifier
    lateinit var library: AbstractLibrary // Library which this manga belongs

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

        // check if we already have this manga in local storage
        val path = id.replace(".", "_") + ".json"
        var isExist = false
        try {
            isExist = StorageManager.isExist(path, StorageManager.FileType.MangaInfo)
        }
        catch (ex : MangaJetException) {
            Logger.log("Can't check if manga exist, because no permission granted:" + ex.message, Logger.Lvl.WARNING)
            // user didn`t gave permission to us. Very bad.
        }

        // if file Exist, better to load from json
        if (isExist){
            try {
                val json = JSONObject(StorageManager.loadString(path, StorageManager.FileType.MangaInfo))
                fromJSON(json)
            }
            catch (ex : MangaJetException){
                Logger.log("Could not create manga " + id + " from JSON" + ex.message, Logger.Lvl.WARNING)
                // nothing too tragic. Just forget about it
            }
            catch(ex : JSONException){
                Logger.log("Could not create manga " + id + " from JSON" + ex.message, Logger.Lvl.WARNING)
                // nothing too tragic. Just forget about it
            }
        }
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

    // Fill data from JSONObject
    // MAY THROW MangaJetException
    private fun fromJSON(json: JSONObject) {
        id = json.optString("id")
        try {
            library = Librarian.getLibrary(
                Librarian.LibraryName.from(json.optString("library"))
            )!!
        }
        catch (expected: NullPointerException) {
            Logger.log("Unknown library: " + expected.message, Logger.Lvl.WARNING)
            throw MangaJetException("Unknown library")
        }
        this.lastViewedChapter = json.optInt("lastViewedChapter", 0)
        fillMangaFromJSON(json)
        val chaptersJson = json.getJSONObject("chapters")
        val listTmp = arrayListOf<MangaChapter>()
        listTmp.ensureCapacity(chaptersJson.length())
        for (i in 0 until chaptersJson.length()) {
            val chapterId = chaptersJson.names().getString(i)
            try {
                val chapterJSON = chaptersJson.getJSONObject(chapterId)
                val chapterName = chapterJSON.getString("name")
                val chapterFullName = chapterJSON.getString("fullname")
                val pagesJSON = chapterJSON.getJSONArray("pages")
                val pagesArray = arrayListOf<String>()
                pagesArray.ensureCapacity(pagesJSON.length())
                for (j in 0 until pagesJSON.length())
                    pagesArray.add(pagesJSON[j].toString())
                listTmp.add(MangaChapter(this, chapterId, pagesArray, chapterName, chapterFullName))
            }
            catch (ex: JSONObject) { // old jsons must die
                Logger.log("Invalid old json " + id)
                this.lastViewedChapter = 0
                continue
            }

        }
        this.chapters = listTmp.toTypedArray()
        if (this.chapters.isNotEmpty() && this.chapters.size >= lastViewedChapter)
            this.chapters[lastViewedChapter].lastViewedPage = json.optInt("lastViewedPage", 0)
    }

    // Constructor from JSON string
    // MAY THROW MangaJetException
    constructor(jsonStr: String){
        try {
            val json = JSONObject(jsonStr)
            fromJSON(json)
        }
        catch (es :JSONException){
            throw MangaJetException("Bad json " + jsonStr)
        }
    }

    // Function to fill all manga info except chapters
    // MAY THROW MangaJetException
    fun updateInfo(){
        fillMangaFromJSON(JSONObject(library.getMangaInfo(id))) // Exception may be thrown here
    }

    // Function to fill chapters array of manga
    // MAY THROW MangaJetException
    fun updateChapters(){
        var tmp = chapters
        chapters = library.getMangaChapters(this)
        for (i in tmp.indices) {
            chapters[i] = tmp[i]
        }
    }

    // Function dump information about manga as JSON string
    // MAY THROW MangaJetException
    fun toJSON(full : Boolean = false) : String {
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
        if (this.chapters.isNotEmpty())
            json.put("lastViewedPage", this.chapters[this.lastViewedChapter].lastViewedPage)
        else
            json.put("lastViewedPage", 0)
        val jsonChapters = JSONObject()

        if (full)
            chapters.forEach{it.updateInfo()}

        chapters.forEach { jsonChapters.put(it.id, JSONArray(it.getJSON())) }
        json.put("chapters", jsonChapters)

        return json.toString()
    }

    // function for saving Manga to file
    // MAY THROW MangaJetException
    fun saveToFile(full: Boolean = false) {
        var string = toJSON(full)
        var path = id.replace(".", "_") + ".json"
        StorageManager.saveString(path, string, StorageManager.FileType.MangaInfo)
    }
}
