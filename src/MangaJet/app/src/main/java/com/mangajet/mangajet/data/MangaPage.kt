package com.mangajet.mangajet.data

import java.io.File

// Class that represents one page of manga's chapter
class MangaPage {
    var url: String // url to online version of page
    var localPath: String // Path to file in local storage
    var mangaHeaders: Map<String, String>

    // Constructor for Libraries
    constructor(link: String, headers: Map<String, String> = mapOf()) {
        this.url = link
        this.mangaHeaders = headers
        var f = link.indexOf(".") + 1
        this.localPath = link.subSequence(f, link.length).toString()
        this.localPath = this.localPath.replace(".", "_")
        f = this.localPath.lastIndexOf("_")
        this.localPath = this.localPath.substring(0, f) + '.' + this.localPath.substring(f + 1)
    }

    // Function for checking if page downloaded. If not - it will start to upload to storage
    // MAY THROW MangaJetException
    fun upload(force: Boolean = false) {
        if (force || !StorageManager.isExist(this.localPath)) { // Exception may be thrown here
            StorageManager.download(
                url = this.url,
                path = this.localPath,
                headers = mangaHeaders,
                type = StorageManager.FileType.CachedPages)  // Exception may be thrown here
        }
    }

    // Function will return File instance of this image
    // MAY THROW MangaJetException
    fun getFile() : File {
        // safe-check
        upload() // Exception may be thrown here

        // wait if not loaded
        StorageManager.await(this.localPath) // Exception may be thrown here

        return StorageManager.getFile(this.localPath) // Exception may be thrown here
    }
}
