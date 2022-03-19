package com.mangajet.mangajet.data

// Abstract class that represents one site from which we parse manga
@Suppress("UnnecessaryAbstractClass")
abstract class AbstractLibrary {

    // Function to get Url
    abstract fun getURL(): String

    // Function to get Manga class by its id(name)
    abstract fun getMangaByName(id: String): Manga

    // Function to get array of Manga classes by its id(name), amount of mangas(optional)
    // and offset from start(optional)
    abstract fun searchManga(id: String, amount: Int = 10, offset: Int = 0): Array<Manga>

    // Function to get info(name, author, genre, number of chapters...) about manga as JSON by its id(name)
    abstract fun getMangaInfo(id: String): String

    // Function to get array of MangaChapter classes by manga's id(name)
    abstract fun getMangasChaptersByName(id: String): Array<MangaChapter>

    // Function to get number of pages in specific manga and specific chapter by their ids(names)
    abstract fun getChaptersNumOfPages(mangaID: String, chapterID: String): Int

    // Function to get Manga Page class by its number, manga id and chapter id
    abstract fun getChaptersNumOfPages(mangaID: String, chapterID: String, pageNumber: Int): MangaPage

}
