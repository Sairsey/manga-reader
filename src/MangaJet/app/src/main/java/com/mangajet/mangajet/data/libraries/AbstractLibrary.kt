package com.mangajet.mangajet.data.libraries

import android.os.Build
import android.text.Html
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaChapter

// Abstract class that represents one site from which we parse manga
@Suppress("TooManyFunctions")
abstract class AbstractLibrary(uniqueID: String) {
    private val url: String = uniqueID

    // Function to get Url
    fun getURL() : String{
        return url
    }

    // Helper function to delete weird Html symbols
    internal fun transformFromHtml(text : String) : String{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
        else
            Html.fromHtml(text).toString()
    }

    // Function to get Manga class by its id(name)
    abstract fun createMangaById(id: String) : Manga

    // Function to set cookies after authentication
    abstract fun setCookies(cookies: String)

    // Function to get cookies after authentication
    abstract fun getCookies() : String

    // Function to get headers if we need
    abstract fun getHeadersForDownload() : Map<String, String>

    // Function to get array of Manga classes by its id(name), amount of mangas(optional)
    // and offset from start(optional)
    // MAY THROW MangaJetException
    abstract fun searchManga(id: String, amount: Int = 10, offset: Int = 0) : Array<Manga>

    // Function to get array of Manga classes by tags, amount of mangas(optional)
    // and offset from start(optional)
    // MAY THROW MangaJetException
    abstract fun searchMangaByTags(tags: Array<String>, amount: Int = 10, offset: Int = 0) : Array<Manga>

    // Function to get array of Manga classes by popularity, amount of mangas(optional)
    // and offset from start(optional)
    // MAY THROW MangaJetException
    abstract fun getPopularManga(amount: Int = 10, offset: Int = 0) : Array<Manga>

    // Function to get info(name, author, genre, number of chapters...) about manga as JSON by its id(name)
    // MAY THROW MangaJetException
    abstract fun getMangaInfo(id: String) : String

    // Function to get array of MangaChapter classes by manga's id(name)
    // MAY THROW MangaJetException
    abstract fun getMangaChapters(manga: Manga) : Array<MangaChapter>

    // Function to get array of pages in specific manga, specific chapter by their ids(names)
    // MAY THROW MangaJetException
    abstract fun getChapterInfo(mangaID: String, chapterID: String) : String
}
