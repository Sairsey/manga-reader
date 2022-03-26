package com.mangajet.mangajet.data

import com.mangajet.mangajet.MangaJetApp
import java.io.File

// Singleton which will do everything about memory management
// it also will map files from Android filesystem to ours filesystem
object StorageManager {
    var storageDirectory =
        MangaJetApp.context!!.getExternalFilesDir("").toString() + "/MangaJet" // will be set from options I believe
    var readPermission = false
    var writePermission = false
    var loadPromises = mutableMapOf<String, WebAccessor.Promise>()

    // Function which will say if path exist in local file
    // MAY THROW MangaJetException
    fun isExist(path: String) : Boolean {
        if (!readPermission)
            throw MangaJetException("Read permission not granted")
        val f: File = File(storageDirectory + "/" + path)
        return f.exists()
    }

    // Function which asynchronously start download from Internet to file
    // MAY THROW MangaJetException
    fun download(url: String, path: String) {
        if (!writePermission)
            throw MangaJetException("Write permission not granted")

        // If we already loaded this file - do not do anything
        if (loadPromises.containsKey(path))
            return

        // Create file handle
        val file = File(storageDirectory + "/" + path)

        // If file already already exist - delete it
        if (file.exists()) {
            file.delete()
        }

        // Create all Directories
        file.mkdirs()

        // Delete our file, which was created as directory by file.mkdirs()
        file.delete()

        // Create file
        if (!file.createNewFile())
            throw MangaJetException(
                "Cannot create file with path:" + storageDirectory.toString() + "/" + path.toString())

        // Build a promise and start downloading
        loadPromises.put(path, WebAccessor.writeBytesStream(url, file.outputStream()))
    }

    // Function which will wait for specific file to load
    // MAY THROW MangaJetException
    fun await(path: String){
        // If we already loaded this file - do not do anything
        if (!loadPromises.containsKey(path))
            return
        loadPromises[path]?.join() // Exception may be thrown here
        loadPromises.remove(path)
    }

    // Function which will give File handler for specific path
    // MAY THROW MangaJetException
    fun getFile(path: String) : File {
        if (!readPermission)
            throw MangaJetException("Read permission not granted")
        return File(storageDirectory + "/" + path)
    }

    // Function which will give File handler for specific path
    private fun dirSize(dir: File): Long {
        if (dir.exists()) {
            var result: Long = 0
            val fileList = dir.listFiles()
            for (i in fileList!!.indices) {
                if (fileList[i].isDirectory) {
                    result += dirSize(fileList[i])
                } else {
                    result += fileList[i].length()
                }
            }
            return result
        }
        return 0
    }

    // Function which will give File handler for specific path
    fun usedStorageSizeInBytes(): Long {
        val dir = File(storageDirectory)
        if (dir.exists()) {
            var result: Long = 0
            val fileList = dir.listFiles()
            for (i in fileList!!.indices) {
                if (fileList[i].isDirectory) {
                    result += dirSize(fileList[i])
                } else {
                    result += fileList[i].length()
                }
            }
            return result
        }
        return 0
    }

    // Function which will give File handler for specific path
    fun removeDirectory(path: String = ""): Boolean {
        val f = File(storageDirectory + "/" + path)
        return f.deleteRecursively()
    }
}
