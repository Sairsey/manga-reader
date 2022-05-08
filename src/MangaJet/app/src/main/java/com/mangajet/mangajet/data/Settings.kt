package com.mangajet.mangajet.data

import org.json.JSONException
import org.json.JSONObject
import java.lang.Boolean
import kotlin.BooleanArray
import kotlin.Int
import kotlin.String

// Class for all important global constants
// No logger here
object Settings {

    // Setting file name
    private const val settingFileName = "settings.json"

    // Amount of searchable mangas
    val MANGA_SEARCH_AMOUNT : Int

    // Chosen resources in search
    val CHOSEN_RESOURCES : BooleanArray

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

    // Initializer block
    init {
        var mangaSearchAmount = "20".toInt()
        var chosenResources = BooleanArray(Librarian.LibraryName.values().size)
        chosenResources[0] = true
        var loadRepeats = "5".toInt()
        var logFileName = "log.txt"
        var stackTraceName = "stackTrace.txt"
        IS_ORIGINAL_NAMES = true
        MAX_SCALE = "5".toFloat()
        AMOUNT_OF_TAGS_IN_RECOMMENDATIONS = "5".toInt()
        AMOUNT_OF_MANGAS_IN_RECOMMENDATIONS = "10".toInt()

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

    }

    // Function to save current state of setting.json
    fun saveState(){
        val json = JSONObject()
        json.put("mangaSearchAmount", MANGA_SEARCH_AMOUNT)
        var res : String = ""
        for (element in CHOSEN_RESOURCES)
            res += Boolean.toString(element) + " "
        json.put("chosenResources", res.trim())
        json.put("loadRepeats", LOAD_REPEATS)
        json.put("logFileName", LOG_FILE_NAME)
        json.put("stackTraceName", STACK_TRACE_FILE_NAME)

        StorageManager.saveString(settingFileName, json.toString(), StorageManager.FileType.LibraryInfo)
    }

}
