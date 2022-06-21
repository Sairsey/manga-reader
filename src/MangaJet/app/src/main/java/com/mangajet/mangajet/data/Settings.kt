package com.mangajet.mangajet.data

import com.mangajet.mangajet.data.libraries.AcomicsLibrary
import com.mangajet.mangajet.data.libraries.MangaChanLibrary
import com.mangajet.mangajet.data.libraries.MangaLibLibrary
import com.mangajet.mangajet.data.libraries.ReadMangaLibrary
import org.json.JSONException
import org.json.JSONObject
import java.lang.Boolean
import kotlin.BooleanArray
import kotlin.Int
import kotlin.String

// Class for all important global constants
// No logger here
object Settings {
    // Theme picked
    const val DAY = 0           // Day theme
    const val NIGHT = 1         // Night theme
    const val SYSTEM_THEME = 2  // System match theme
    var THEME_PICKED_ID : Int

    // Setting file name
    private const val settingFileName = "settings.json"

    // Amount of searchable mangas
    var MANGA_SEARCH_AMOUNT : Int

    // Chosen resources in search
    val CHOSEN_RESOURCES : BooleanArray

    // Filtered chosen resources in search
    var FILTERED_CHOSEN_RESOURCES : BooleanArray

    // Chosen resources in 'For you'
    val CHOSEN_FOR_YOU_RESOURCES : BooleanArray

    // Filtered chosen resources in 'For you'
    var FILTERED_CHOSEN_FOR_YOU_RESOURCES : BooleanArray

    // Load repeat count (if prev load failed -> repeat)
    val LOAD_REPEATS : Int

    // Log file name
    val LOG_FILE_NAME : String

    // Stack trace file name
    val STACK_TRACE_FILE_NAME : String

    // Original names
    var IS_ORIGINAL_NAMES : kotlin.Boolean

    // Maximum scale of image
    var MAX_SCALE : kotlin.Float

    // Amount of tags used for recommendations
    var AMOUNT_OF_TAGS_IN_RECOMMENDATIONS : kotlin.Int

    // Amount of mangas for recommendations
    var AMOUNT_OF_MANGAS_IN_RECOMMENDATIONS : kotlin.Int

    // Is NSFW enabled
    var IS_NSFW_ENABLED : kotlin.Boolean

    // List of all installed libraries
    var INSTALLED_LIBRARIES : ArrayList<Librarian.LibraryEntry>

    // Function to filter out some libraries
    fun filter(lib: Librarian.LibraryEntry) : kotlin.Boolean {
        if (lib.isNSFW && !Settings.IS_NSFW_ENABLED)
            return false
        return true
    }

    // function to build filtered arrays
    fun buildFiltered() {
        var tmpFilteredChosen = ArrayList<kotlin.Boolean>()
        var tmpFilteredChosenForYou = ArrayList<kotlin.Boolean>()
        for (i in INSTALLED_LIBRARIES.indices) {
            if (!filter(INSTALLED_LIBRARIES[i]))
                continue
            tmpFilteredChosen.add(CHOSEN_RESOURCES[i])
            tmpFilteredChosenForYou.add(CHOSEN_FOR_YOU_RESOURCES[i])
        }
        FILTERED_CHOSEN_RESOURCES = tmpFilteredChosen.toBooleanArray()
        FILTERED_CHOSEN_FOR_YOU_RESOURCES = tmpFilteredChosenForYou.toBooleanArray()
    }

    // function to parse filtered arrays back to original
    fun parseFiltered() {
        var index = 0
        for (i in INSTALLED_LIBRARIES.indices) {
            if (!filter(INSTALLED_LIBRARIES[i]))
                continue
            if (index >= FILTERED_CHOSEN_RESOURCES.size)
                break
            CHOSEN_RESOURCES[i] = FILTERED_CHOSEN_RESOURCES[index]
            CHOSEN_FOR_YOU_RESOURCES[i] = FILTERED_CHOSEN_FOR_YOU_RESOURCES[index]
            index++
        }
    }

    // Initializer block
    init {
        INSTALLED_LIBRARIES = arrayListOf(
            Librarian.LibraryEntry(
                "ReadManga",
                "https://readmanga.io",
                ReadMangaLibrary::class.qualifiedName!!,
                false),
            Librarian.LibraryEntry(
                "Manga-chan",
                "https://manga-chan.me",
                MangaChanLibrary::class.qualifiedName!!,
                false),
            Librarian.LibraryEntry(
                "Mangalib",
                "https://mangalib.me",
                MangaLibLibrary::class.qualifiedName!!,
                false),
            Librarian.LibraryEntry(
                "Acomics",
                "https://acomics.ru",
                AcomicsLibrary::class.qualifiedName!!,
                false),
            Librarian.LibraryEntry(
                "MintManga",
                "https://mintmanga.live",
                ReadMangaLibrary::class.qualifiedName!!,
                true),
            Librarian.LibraryEntry(
                "Hentai-chan",
                "https://hentaichan.live",
                MangaChanLibrary::class.qualifiedName!!,
                true),
            Librarian.LibraryEntry(
                "Hentailib",
                "https://hentailib.me",
                MangaLibLibrary::class.qualifiedName!!,
                true),
        )
        var mangaSearchAmount = "20".toInt()
        var chosenResources = BooleanArray(INSTALLED_LIBRARIES.size)
        chosenResources[0] = true
        var chosenForYouResources = BooleanArray(INSTALLED_LIBRARIES.size)
        chosenForYouResources[0] = true
        FILTERED_CHOSEN_RESOURCES = BooleanArray(0)
        FILTERED_CHOSEN_FOR_YOU_RESOURCES = BooleanArray(0)
        var loadRepeats = "5".toInt()
        var logFileName = "log.txt"
        var stackTraceName = "stackTrace.txt"
        IS_NSFW_ENABLED = false
        IS_ORIGINAL_NAMES = true
        MAX_SCALE = "5".toFloat()
        AMOUNT_OF_TAGS_IN_RECOMMENDATIONS = "5".toInt()
        AMOUNT_OF_MANGAS_IN_RECOMMENDATIONS = "10".toInt()
        THEME_PICKED_ID = SYSTEM_THEME
        // Try to get settings from file
        try{
            if(StorageManager.isExist(settingFileName, StorageManager.FileType.LibraryInfo)){
                val json = JSONObject(StorageManager.loadString(settingFileName,
                    StorageManager.FileType.LibraryInfo))
                if(json.has("mangaSearchAmount"))
                    mangaSearchAmount = json.getInt("mangaSearchAmount")
                var strArray = arrayOf("")
                if(json.has("chosenResources"))
                    strArray = json.getString("chosenResources").split(" ").toTypedArray()
                if(chosenResources.size == strArray.size)
                    for(i in chosenResources.indices)
                        chosenResources[i] = strArray[i].toBoolean()
                if(json.has("chosenForYouResources"))
                    strArray = json.getString("chosenForYouResources").split(" ").toTypedArray()
                if(chosenForYouResources.size == strArray.size)
                    for(i in chosenForYouResources.indices)
                        chosenForYouResources[i] = strArray[i].toBoolean()
                if(json.has("loadRepeats"))
                    loadRepeats = json.getInt("loadRepeats")
                if(json.has("logFileName"))
                    logFileName = json.getString("logFileName")
                if(json.has("stackTraceName"))
                    stackTraceName = json.getString("stackTraceName")
                if(json.has("originalNames"))
                    IS_ORIGINAL_NAMES = json.getBoolean("originalNames")
                if(json.has("max_scale"))
                    MAX_SCALE = json.getDouble("max_scale").toFloat()
                if(json.has("amount_of_tags_in_recommend"))
                    AMOUNT_OF_TAGS_IN_RECOMMENDATIONS = json.getInt("amount_of_tags_in_recommend")
                if(json.has("amount_of_manga_in_recommend"))
                    AMOUNT_OF_MANGAS_IN_RECOMMENDATIONS = json.getInt("amount_of_mangas_in_recommend")
                if(json.has("theme_picked_id"))
                    THEME_PICKED_ID = json.getInt("theme_picked_id")
                if(json.has("is_nsfw_enabled"))
                    IS_NSFW_ENABLED = json.getBoolean("is_nsfw_enabled")
            }
        }
        catch (ex : MangaJetException){
            // Could not load setting? Gonna cry?
        }
        catch (ex : JSONException){
            ex.hashCode()
            // Could not load setting? Gonna cry?
        }

        MANGA_SEARCH_AMOUNT = mangaSearchAmount
        CHOSEN_RESOURCES = chosenResources
        CHOSEN_FOR_YOU_RESOURCES = chosenForYouResources
        LOAD_REPEATS = loadRepeats
        LOG_FILE_NAME = logFileName
        STACK_TRACE_FILE_NAME = stackTraceName
        try {
            saveState()
        }
        catch (ex : MangaJetException){
            // Sad, but really doesn't matter
        }
        catch (ex : JSONException){
            ex.hashCode()
            // Sad, but really doesn't matter
        }

        buildFiltered()
    }

    // Function to save current state of setting.json
    fun saveState(){
        val json = JSONObject()
        json.put("mangaSearchAmount", MANGA_SEARCH_AMOUNT)
        var res : String = ""
        for (element in CHOSEN_RESOURCES)
            res += Boolean.toString(element) + " "
        json.put("chosenResources", res.trim())
        res = ""
        for (element in CHOSEN_FOR_YOU_RESOURCES)
            res += Boolean.toString(element) + " "
        json.put("chosenForYouResources", res.trim())
        json.put("loadRepeats", LOAD_REPEATS)
        json.put("logFileName", LOG_FILE_NAME)
        json.put("stackTraceName", STACK_TRACE_FILE_NAME)
        json.put("theme_picked_id", THEME_PICKED_ID)
        json.put("is_nsfw_enabled", IS_NSFW_ENABLED)
        parseFiltered()
        buildFiltered()
        StorageManager.saveString(settingFileName, json.toString(), StorageManager.FileType.LibraryInfo)
    }

}
