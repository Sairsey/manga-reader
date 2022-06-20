package com.mangajet.mangajet.data

import com.mangajet.mangajet.data.libraries.AbstractLibrary
import com.mangajet.mangajet.data.libraries.ReadMangaLibrary
import com.mangajet.mangajet.data.libraries.MangaLibLibrary
import com.mangajet.mangajet.data.libraries.MangaChanLibrary
import com.mangajet.mangajet.data.libraries.AcomicsLibrary
import com.mangajet.mangajet.data.libraries.NineMangaLibrary

import com.mangajet.mangajet.log.Logger
import org.json.JSONObject

// Singleton class that stores all libraries with manga and provides access to them
object Librarian {

    // Enum class that represents names for parsed sites
    enum class LibraryName(val resource: String){
        Readmanga("https://readmanga.io"),
        Mangalib("https://mangalib.me"),
        Mangachan("https://manga-chan.me"),
        Acomics("https://acomics.ru"),
        NineMangaEN("https://en.ninemanga.com"),
        NineMangaDE("https://de.ninemanga.com"),
        NineMangaRU("https://ru.ninemanga.com"),
        NineMangaIT("https://it.ninemanga.com"),
        NineMangaBR("https://br.ninemanga.com"),
        NineMangaFR("https://fr.ninemanga.com");

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
        map[LibraryName.NineMangaEN] = NineMangaLibrary(LibraryName.NineMangaEN.resource)
        map[LibraryName.NineMangaDE] = NineMangaLibrary(LibraryName.NineMangaDE.resource)
        map[LibraryName.NineMangaRU] = NineMangaLibrary(LibraryName.NineMangaRU.resource)
        map[LibraryName.NineMangaIT] = NineMangaLibrary(LibraryName.NineMangaIT.resource)
        map[LibraryName.NineMangaBR] = NineMangaLibrary(LibraryName.NineMangaBR.resource)
        map[LibraryName.NineMangaFR] = NineMangaLibrary(LibraryName.NineMangaFR.resource)
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

    // Function to get Frequency of tags in History
    fun getTagFrequency() : MutableMap<String, Int> {
        var result : MutableMap<String, Int> = mutableMapOf()
        // load all manga infos
        var paths = StorageManager.getAllPathsForType(StorageManager.FileType.MangaInfo)
        // for every manga
        for (path in paths) {
            try {
                // load json from history
                var json = StorageManager.loadString(path, StorageManager.FileType.MangaInfo)
                // load manga from history
                var manga = JSONObject(json)
                var tags = manga.getJSONArray("tags")
                // for every manga tag
                for (i in 0 until tags.length()) {
                    val tag = tags.getString(i)
                    // increment
                    result[tag] = result.getOrDefault(tag, 0) + 1
                }
            }
            catch (ex : MangaJetException) {
                Logger.log("getTagFrequency: Cannot load " + path)
            }
        }
        return result
    }

    // function to get recommended tags
    fun getRecommendedTags(freq : MutableMap<String, Int>) : ArrayList<String> {
        // transform to list of pairs
        var freqList = freq.toList()
        // sort it by Int
        freqList = freqList.sortedByDescending { it.second }
        // create result array of tags
        var tagArray = arrayListOf<String>()
        for (i in 0 until freqList.size) {
            // too many tags
            if (i >= Settings.AMOUNT_OF_TAGS_IN_RECOMMENDATIONS)
                break
            // add tag
            tagArray.add(freqList[i].first)
        }
        return tagArray
    }

    // function to get mangas we already seen
    private fun getWatchedMangaIDs() : ArrayList<String> {
        var paths = StorageManager.getAllPathsForType(StorageManager.FileType.MangaInfo)
        var mangasIDs = arrayListOf<String>()
        // for every manga
        for (path in paths) {
            try {
                // load json from history
                var json = StorageManager.loadString(path, StorageManager.FileType.MangaInfo)
                // load manga from history
                var manga = JSONObject(json)
                mangasIDs.add(manga.getString("id"))
            }
            catch (ex : MangaJetException) {
                Logger.log("getRecommendedMangas: Cannot load " + path)
            }
        }
        return mangasIDs
    }

    // function to get mangas from sources by tags
    private fun getMangasByTags(
        names : ArrayList<LibraryName>,
        tags : Array<String>,
        forbiddenManga: ArrayList<String>,
        offset : Int
    ) : ArrayList<Manga> {
        var result : ArrayList<Manga> = arrayListOf()

        for (name in names) {
            var searchedMangas = map[name]!!.searchMangaByTags(tags, offset = offset)
            for (manga in searchedMangas) {
                if (!forbiddenManga.contains(manga.id))
                    result.add(manga)
            }
        }
        return result
    }

    // function to get Recommended mangas
    fun getRecommendedMangas(
        searchLibraries : ArrayList<LibraryName> =
            arrayListOf(LibraryName.Readmanga, LibraryName.Mangalib))
    : ArrayList<Manga> {
        var result = arrayListOf<Manga>()

        // PART 1 (Phantom dick): Get frequency
        val freq = getTagFrequency()

        if (freq.isEmpty())
            return result

        // PART 2 (Battle Sucktion): Get good tags
        var tagArray = getRecommendedTags(freq)

        // PART 3 (Stardust Dungeon-masters): Get history mangas
        var mangasIDs = getWatchedMangaIDs()

        // PART 4 (Ass is (Un)breakable): Search mangas
        var offset = 0
        while (result.size < settings.AMOUNT_OF_MANGAS_IN_RECOMMENDATIONS) {
            var newSearch = getMangasByTags(
                searchLibraries,
                tagArray.toTypedArray(),
                mangasIDs,
                offset)
            result.addAll(newSearch)
            offset += settings.AMOUNT_OF_MANGAS_IN_RECOMMENDATIONS
        }

        // PART 5 (Golden Rain): return data
        return result
    }
}
