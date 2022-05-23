package com.mangajet.mangajet.data

import com.mangajet.mangajet.MangaJetApp
import java.io.File
import java.io.FileOutputStream
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.Arrays
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


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

    // Path to directory there we are saving all our data
    var storageDirectory =
        MangaJetApp.context!!.getExternalFilesDir("").toString() + "/MangaJet" // will be set from options I believe
    // boolean flags for cheching our read-write Permissions
    var readPermission = false
    var writePermission = false
    // Map which contains promises - special variables which created
    // with threads of loading data and has the interface to "join"
    // this threads
    // Also this variable used for mutex-es
    var loadPromises = mutableMapOf<String, WebAccessor.Promise?>()


    // Function which will say if path exist in local file
    // MAY THROW MangaJetException
    fun isExist(path: String, type: FileType = FileType.Auto) : Boolean {
        if (!readPermission)
            throw MangaJetException("Read permission not granted")
        //synchronized(loadPromises) {
            if (type == FileType.Auto) {
                for (typeIterator in FileType.values()) {
                    val f: File = File(storageDirectory + typeIterator.subdirectoryPath + "/" + path)
                    if (f.exists())
                        return true
                }
                return false
            }
            return File(storageDirectory + type.subdirectoryPath + "/" + path).exists()
        //}
    }

    // Function which asynchronously start download from Internet to file
    // MAY THROW MangaJetException
    fun download(
        url: String,
        path: String,
        type: FileType = FileType.CachedPages,
        headers: Map<String, String> = mapOf()) {
        if (!writePermission)
            throw MangaJetException("Write permission not granted")

        if (type == FileType.Auto)
            throw MangaJetException("Request is too strange")

        var new_path = type.subdirectoryPath + "/" + path

        // lock this scope with java-style mutex
        // this means that only 1 thread can access to this block
        // at a time
        //synchronized(loadPromises) {
            // If we already loading this file - do not do anything
            if (loadPromises.containsKey(new_path)) {
                println("loading of " + new_path + " in progress")
                return
            }
       // }

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

            println("start loading of" + new_path)

        //synchronized(loadPromises) {
            // If we already loading this file - do not do anything
            if (loadPromises.containsKey(new_path)) {
                println("loading of " + new_path + " in progress")
                return
            }

            // Build a promise and start downloading
            loadPromises.put(
                new_path,
                WebAccessor.writeBytesStream(url, file.outputStream(), headers)
            )
        //}
    }

    // Function which will wait for specific file to load
    // MAY THROW MangaJetException
    fun await(path: String, type: FileType = FileType.CachedPages){

        var new_path = type.subdirectoryPath + "/" + path

        if (type == FileType.Auto)
            throw MangaJetException("Request is too strange")

        // lock this scope with java-style mutex
        // this means that only 1 thread can access to this block
        // at a time
        //synchronized(loadPromises) {
            // If we are not loading this file - do not do anything
            if (!loadPromises.containsKey(new_path))
                return
            loadPromises[new_path]?.join() // Exception may be thrown here
            loadPromises.remove(new_path)
        //}
    }

    // Function which will give File handler for specific path
    // MAY THROW MangaJetException
    fun getFile(path: String, type: FileType= FileType.Auto) : File {
        if (!readPermission)
            throw MangaJetException("Read permission not granted")
        //synchronized(loadPromises) {
            if (type == FileType.Auto) {
                for (typeIterator in FileType.values()) {
                    val f: File =
                        File(storageDirectory + typeIterator.subdirectoryPath + "/" + path)
                    if (f.exists())
                        return f
                }
                throw MangaJetException("Cannot find type for " + path)
            }

            return File(storageDirectory + type.subdirectoryPath + "/" + path)
        //}
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
    fun usedStorageSizeByType(type : FileType): Long {
        val dir = File(storageDirectory + type.subdirectoryPath)
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
        if (!writePermission)
            throw MangaJetException("Write permission not granted")

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
        if (!readPermission)
            throw MangaJetException("Read permission not granted")

        //synchronized(loadPromises) {
            val f = getFile(path, type)
            if (!f.exists())
                throw MangaJetException("Cannot find file " + path)
            return f.readText()
        //}
    }

    // Function which will create ZIP archive of specific file types
    fun createZipArchive(
        outputStream: OutputStream,
        fileTypes: ArrayList<FileType> =
                             arrayListOf(
                                 FileType.LibraryInfo,
                                 FileType.MangaInfo)) {
        val zos = ZipOutputStream(BufferedOutputStream(outputStream))
        for (type in fileTypes) {
            val inputDirectory = File(storageDirectory + type.subdirectoryPath)
            inputDirectory.walkTopDown().forEach { file ->
                val zipFileName = file.absolutePath.removePrefix(storageDirectory).removePrefix("/")
                val entry = ZipEntry( "$zipFileName${(if (file.isDirectory) "/" else "" )}")
                zos.putNextEntry(entry)
                if (file.isFile) {
                    file.inputStream().copyTo(zos)
                }
                zos.closeEntry()
            }
        }
        zos.close()
        return
    }

    // Function which will create ZIP archive of specific file types
    fun unpackZipArchive(inputStream: InputStream) {
        val zip = ZipInputStream(BufferedInputStream(inputStream))

        var entry = zip.getNextEntry()

        while (entry != null) {
            val filePath = storageDirectory + "/" + entry.name

            // if directory - create it
            if (entry.isDirectory) {
                val unzipFile = File(filePath)
                if (!unzipFile.isDirectory) unzipFile.mkdirs()
            }
            // if file - fill it with data
            else {
                BufferedOutputStream(FileOutputStream(filePath)).use { fileOutputStream ->
                    zip.copyTo(fileOutputStream)
                }
            }
            entry = zip.getNextEntry()
        }

        return
    }

    // Function which will copy file from one File type to another
    // MAY THROW MangaJetException
    fun copyToType(path: String, inType : FileType, outType : FileType) {
        val fileIn = File(storageDirectory + inType.subdirectoryPath + "/" + path)

        if (!writePermission)
            throw MangaJetException("Write permission not granted")

        if (outType == FileType.Auto)
            throw MangaJetException("Request is too strange")

        var new_path = outType.subdirectoryPath + "/" + path

        // lock this scope with java-style mutex
        // this means that only 1 thread can access to this block
        // at a time
        //synchronized(loadPromises) {
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

            file.writeBytes(fileIn.readBytes())
        //}
    }

    // Function which will return paths for all elements of specific file type in order of modification date
    fun getAllPathsForType(type: FileType) : Array<String> {
        if (!readPermission)
            throw MangaJetException("Read permission not granted")

        if (type == FileType.Auto)
            throw MangaJetException("Request is too strange")
        val f = File(storageDirectory + type.subdirectoryPath)

        var files = ArrayList<File>()

        // get all files
        f.walkTopDown().forEach {
            if (it.path.contains(".json") ||
                it.path.contains(".png") ||
                it.path.contains(".jpg")) {
                    files.add(it)
            }
        }
        val filesArray = files.toTypedArray()

        // sort them by modification date
        Arrays.sort(filesArray, Comparator.comparingLong(File::lastModified).reversed());

        // make paths
        val pathArray = ArrayList<String>()
        filesArray.forEach {
            pathArray.add(it.path.substring((storageDirectory + type.subdirectoryPath + 1).length))
        }

        return pathArray.toTypedArray()
    }
}
