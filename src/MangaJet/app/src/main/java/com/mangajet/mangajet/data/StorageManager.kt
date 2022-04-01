package com.mangajet.mangajet.data

import com.mangajet.mangajet.MangaJetApp
import java.io.File

// Singleton which will do everything about memory management
// it also will map files from Android filesystem to ours filesystem
@Suppress("TooManyFunctions")
object StorageManager {

    // Enum class that represents different types of data we are saving
    enum class FileType(val subdirectoryPath: String) {
        Auto(""),                        // decide automatically
        MangaInfo("/manga_info"),        // ones which stored as json for each manga
        LibraryInfo("/library_info"),    // ones which stored as json for each library
        DownloadedPages("/download"),    // ones which user decided to download
        CachedPages("/cached");          // ones which can probably delete if we took too much space

        companion object {
            fun from(findResource: String): FileType = FileType.values().first { it.subdirectoryPath == findResource}
        }
    }

    var storageDirectory =
        MangaJetApp.context!!.getExternalFilesDir("").toString() + "/MangaJet" // will be set from options I believe
    var readPermission = false
    var writePermission = false
    var loadPromises = mutableMapOf<String, WebAccessor.Promise>()

    // Function which will say if path exist in local file
    // MAY THROW MangaJetException
    fun isExist(path: String, type: FileType = FileType.Auto) : Boolean {
        if (!readPermission)
            throw MangaJetException("Read permission not granted")
        if (type == FileType.Auto) {
            for (typeIterator in FileType.values()) {
                val f: File = File(storageDirectory + typeIterator.subdirectoryPath + "/" + path)
                if (f.exists())
                    return true
            }
            return false
        }
        return File(storageDirectory + type.subdirectoryPath + "/" + path).exists()
    }

    // Function which asynchronously start download from Internet to file
    // MAY THROW MangaJetException
    fun download(url: String, path: String, type: FileType = FileType.CachedPages) {
        if (!writePermission)
            throw MangaJetException("Write permission not granted")

        var new_path = type.subdirectoryPath + "/" + path

        // If we already loaded this file - do not do anything
        if (loadPromises.containsKey(new_path))
            return

        // Create file handle
        val file = File(storageDirectory + new_path)

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
                "Cannot create file with path:" + storageDirectory.toString() + new_path.toString())

        // Build a promise and start downloading
        loadPromises.put(new_path, WebAccessor.writeBytesStream(url, file.outputStream()))
    }

    // Function which will wait for specific file to load
    // MAY THROW MangaJetException
    fun await(path: String, type: FileType = FileType.CachedPages){

        var new_path = type.subdirectoryPath + "/" + path

        // If we already loaded this file - do not do anything
        if (!loadPromises.containsKey(new_path))
            return
        loadPromises[new_path]?.join() // Exception may be thrown here
        loadPromises.remove(new_path)
    }

    // Function which will give File handler for specific path
    // MAY THROW MangaJetException
    fun getFile(path: String, type: FileType= FileType.Auto) : File {
        if (!readPermission)
            throw MangaJetException("Read permission not granted")

        if (type == FileType.Auto)
        {
            for (typeIterator in FileType.values()) {
                val f: File = File(storageDirectory + typeIterator.subdirectoryPath + "/" + path)
                if (f.exists())
                    return f
            }
            throw MangaJetException("Cannot find type for " + path)
        }

        return File(storageDirectory + type.subdirectoryPath + "/" + path)
    }

    // Function which will find folder size recursively
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

    // Function which will return folder size
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

    // Function which remove files from directory recursively
    fun removeDirectory(path: String = ""): Boolean {
        val f = File(storageDirectory + "/" + path)
        return f.deleteRecursively()
    }

    // Function which remove files certain type
    fun removeFilesByType(type: FileType): Boolean {
        val f = File(storageDirectory + type.subdirectoryPath)
        return f.deleteRecursively()
    }

    // Function which will safe String to file
    // MAY THROW MangaJetException
    fun saveString(path: String, data: String, type: FileType=FileType.MangaInfo) {
        val new_path = type.subdirectoryPath + "/" + path

        if (type == FileType.Auto)
            throw MangaJetException("Request is too strange")

        // Create file handle
        val file = File(storageDirectory + new_path)

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
                "Cannot create file with path:" + storageDirectory.toString() + new_path.toString())

        file.writeText(data)
        return;
    }

    // Function which will load String to file
    // MAY THROW MangaJetException
    fun loadString(path: String, type: FileType=FileType.Auto) : String {
        val f = getFile(path, type)
        if (!f.exists())
            throw MangaJetException("Cannot find file " + path)
        return f.readText()
    }

    // Function which will return all existed paths for specific file type
    fun getAllPathsForType(type: FileType) : Array<String> {
        if (type == FileType.Auto)
            throw MangaJetException("Request is too strange")
        val f = File(storageDirectory + type.subdirectoryPath)
        var array = ArrayList<String>()

        f.walkTopDown().forEach {
            if (it.path.contains(".json") ||
                it.path.contains(".png") ||
                it.path.contains(".jpg")) {
                array.add(it.path.substring((storageDirectory + type.subdirectoryPath + 1).length))
            }
        }


        return array.toTypedArray()
    }
}
