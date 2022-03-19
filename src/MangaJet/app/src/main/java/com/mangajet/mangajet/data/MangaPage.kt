package com.mangajet.mangajet.data

import java.io.File

// Class that represents one page of manga's chapter
class MangaPage {
    var url: String // url to online version of page
    var localPath: String // Path to file in local storage

    // Constructor for Libraries
    constructor(link: String) {
        this.url = link
        var f = link.indexOf(".") + 1
        this.localPath = link.subSequence(f, link.length).toString()
        this.localPath = this.localPath.replace(".", "_")
        f = this.localPath.lastIndexOf("_")
        this.localPath = this.localPath.substring(0, f) + '.' + this.localPath.substring(f + 1)
    }

    // Function for checking if page downloaded. If not - it will start to upload to storage
    fun upload() {
        if (!StorageManager.isExist(this.localPath)) {
            StorageManager.download(this.url, this.localPath)
        }
    }

    // Function will return File instance of this image
    fun getFile() : File {
        // safe-check
        upload()

        // wait if not loaded
        StorageManager.await(this.localPath)

        return StorageManager.getFile(this.localPath)!!
    }
}
