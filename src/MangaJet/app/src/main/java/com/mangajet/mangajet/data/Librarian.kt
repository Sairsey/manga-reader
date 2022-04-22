package com.mangajet.mangajet.data

import com.mangajet.mangajet.data.libraries.AbstractLibrary
import com.mangajet.mangajet.data.libraries.MangaLibLibrary
import com.mangajet.mangajet.data.libraries.MangaChanLibrary
import com.mangajet.mangajet.data.libraries.AcomicsLibrary
import com.mangajet.mangajet.data.libraries.ReadMangaLibrary
import org.json.JSONObject

// Singleton class that stores all libraries with manga and provides access to them
object Librarian {

    // Enum class that represents names for parsed sites
    enum class LibraryName(val resource: String){
        Readmanga("https://readmanga.io"),
        Mangalib("https://mangalib.me"),
        Mangachan("https://manga-chan.me"),
        Acomics("https://acomics.ru");

        companion object {
            fun from(findResource: String): LibraryName = LibraryName.values().first { it.resource == findResource}
        }
    }

    // Map for storing libraries names as keys and abstract libraries as value
    private val map = hashMapOf<LibraryName, AbstractLibrary?>()

    // Settings class where we store all important global constants
    val settings = Settings
    // Filename for StorageManager
    public const val path = "libraries.json"

    // Initializer block
    init {
        map[LibraryName.Readmanga] = ReadMangaLibrary(LibraryName.Readmanga.resource)
        map[LibraryName.Mangalib] = MangaLibLibrary(LibraryName.Mangalib.resource)
        map[LibraryName.Mangachan] = MangaChanLibrary(LibraryName.Mangachan.resource)
        map[LibraryName.Acomics] = AcomicsLibrary(LibraryName.Acomics.resource)
    }

    // Function to get abstractLibrary from map by key(enum)
    public fun getLibrary(name: LibraryName) : AbstractLibrary? {
        return map[name]
    }

    // Function to set cookies for each Library from JSON
    fun setLibrariesJSON(jsonDataStr : String) {
        val jsonData = JSONObject(jsonDataStr)

        map.forEach { libraryName, abstractLibrary ->
            abstractLibrary?.setCookies(jsonData[libraryName.resource].toString())
        }
    }

    // Function to get JSON with each abstract library cookies
    fun getLibrariesJSON() : String {
        val jsonData = JSONObject()

        map.forEach { libraryName, abstractLibrary ->
            jsonData.put(libraryName.resource, abstractLibrary?.getCookies() ?: "")
        }

        return jsonData.toString()
    }
}
