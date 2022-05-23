package com.mangajet.mangajet.data

import com.mangajet.mangajet.log.Logger
import java.io.File

// Class that represents one page of manga's chapter
class MangaPage {
    var url: String // url to online version of page
    var localPath: String // Path to file in local storage
    var mangaHeaders: Map<String, String> // Headers for downloading
    var shouldBeInDownloaded : Boolean // flag for downloaded support

    // Constructor for Libraries
    constructor(link: String, headers: Map<String, String> = mapOf()) {
        this.url = link
        this.mangaHeaders = headers
        this.shouldBeInDownloaded = false
        var f = link.indexOf(".") + 1
        var s = link.indexOf("?", f)
        if (s == -1)
            s = link.length
        this.localPath = link.subSequence(f, s).toString()
        this.localPath = this.localPath.replace(".", "_")
        f = this.localPath.lastIndexOf("_")

        this.localPath = this.localPath.substring(0, f) + '.' + this.localPath.substring(f + 1)
    }

    // Function for checking if page downloaded. If not - it will start to upload to storage
    // MAY THROW MangaJetException
    fun upload(force: Boolean = false, isToDownload: Boolean = false) {
        // if we do not load some file - load it to cache at first
        if (force || !StorageManager.isExist(this.localPath)) { // Exception may be thrown here
            StorageManager.download(
                url = this.url,
                path = this.localPath,
                headers = mangaHeaders,
                type = StorageManager.FileType.CachedPages)  // Exception may be thrown here
        }
        else{
            // Check that load successfully
            val file = StorageManager.getFile(this.localPath)
            val correctSize = WebAccessor.getLength(this.url, this.mangaHeaders.toMap())
            if(correctSize != -1L && file.length() != correctSize){
                // Try again
                Logger.log("Trying to reload the page")
                upload(force = true, isToDownload) // Exception may be thrown here
            }
        }

        shouldBeInDownloaded = isToDownload
    }

    // Function will return File instance of this image
    // MAY THROW MangaJetException
    fun getFile() : File {
        // Safe-check
        upload(isToDownload = shouldBeInDownloaded) // Exception may be thrown here
        // Wait if not loaded
        StorageManager.await(this.localPath) // Exception may be thrown here

        // If we wanted to load it to downloaded pages, we should just move it from cached
        // Exception may be thrown here
        if (shouldBeInDownloaded &&
            StorageManager.isExist(this.localPath, StorageManager.FileType.CachedPages) &&
            !StorageManager.isExist(this.localPath, StorageManager.FileType.DownloadedPages)) {
            StorageManager.copyToType(this.localPath,
                StorageManager.FileType.CachedPages,
                StorageManager.FileType.DownloadedPages)
        }

        return StorageManager.getFile(this.localPath) // Exception may be thrown here
    }

     // Function will delete File from storage if it exist there
     // returns True if something was deleted, False otherwise
     // MAY THROW MangaJetException
     fun removeFileIfExist() : Boolean {
         var success = false

         // wait while all operation will be completed
         try {
             StorageManager.await(this.localPath) // Exception may be thrown here
         }
         catch (ex: MangaJetException) {
             return success
         }

         // Exception may be thrown here
         if (StorageManager.isExist(this.localPath, StorageManager.FileType.CachedPages))
            success = success ||
                    StorageManager.getFile(this.localPath, StorageManager.FileType.CachedPages).delete()

         // Exception may be thrown here
         if (StorageManager.isExist(this.localPath, StorageManager.FileType.DownloadedPages))
             success = success ||
                     StorageManager.getFile(this.localPath, StorageManager.FileType.DownloadedPages).delete()

         return success
     }
}
