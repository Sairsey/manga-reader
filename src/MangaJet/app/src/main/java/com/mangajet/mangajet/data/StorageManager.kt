package com.mangajet.mangajet.data

import java.io.File

// Singleton which will do everything about memory management
// it also will map files from Android filesystem to ours filesystem
object StorageManager {
    var storageDirectory = "/MangaJet/data" // will be set from options I believe

    // Function which will say if path exist in local file
    fun isExist(path : String) : Boolean {
        path + ""
        return false
    }

    // Function which asynchronously start download from Internet to file
    fun download(url : String, path : String){
        url + ""
        path + ""
    }

    // Function which will wait for specific file to load
    fun await(path : String){
        path + ""
    }

    // Function which will give File handler for specific path
    fun getFile(path : String) : File? {
        path + ""
        return null
    }
}
