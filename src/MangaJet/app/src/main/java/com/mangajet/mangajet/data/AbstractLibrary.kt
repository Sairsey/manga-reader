package com.mangajet.mangajet.data

// Abstract class that represents one site from which we parse manga
abstract class AbstractLibrary(uniqueID: String) {
    private val url: String = uniqueID

    // Function to get Url
    fun getURL() : String{
        return url
    }

    // Function to get Manga class by its id(name)
    abstract fun createMangaById(id: String) : Manga

    // Function to set cookies after authentication
    abstract fun setCookies(cookies: String)

    // Function to get array of Manga classes by its id(name), amount of mangas(optional)
    // and offset from start(optional)
    // MAY THROW MangaJetException
    abstract fun searchManga(id: String, amount: Int = 10, offset: Int = 0) : Array<Manga>

    // Function to get info(name, author, genre, number of chapters...) about manga as JSON by its id(name)
    // MAY THROW MangaJetException
    abstract fun getMangaInfo(id: String) : String

    // Function to get array of MangaChapter classes by manga's id(name)
    // MAY THROW MangaJetException
    abstract fun getMangaChapters(manga: Manga) : Array<MangaChapter>

    // Function to get number of pages in specific manga and specific chapter by their ids(names)
    // MAY THROW MangaJetException
    abstract fun getChaptersNumOfPages(mangaID: String, chapterID: String) : Int

    // Function to get Manga Page class by its number, manga id and chapter id
    // MAY THROW MangaJetException
    abstract fun getChapterPage(mangaID: String, chapterID: String, pageNumber: Int) : MangaPage
}
