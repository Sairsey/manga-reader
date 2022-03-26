package com.mangajet.mangajet.data

import org.json.JSONArray
import org.json.JSONObject

// Class that represents Manga-Chan.me library
class MangaChanLibrary(uniqueID: String) : AbstractLibrary(uniqueID) {

    val headers = mutableMapOf(
        "user-agent" to "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko)" +
            "Chrome/41.0.2228.0 Safari/537.36",
        "accept" to "*/*")

    // Function to get Manga class by its id(name)
    override fun createMangaById(id: String) : Manga {
        return Manga(this, id)
    }

    override fun setCookies(cookies: String) {
        headers["Cookie"] = cookies
    }

    // Function to get array of Manga classes by its id(name), amount of mangas(optional)
    // and offset from start(optional)
    // MAY THROW MangaJetException
    override fun searchManga(id: String, amount: Int, offset: Int) : Array<Manga> {
        val newId = id.replace(' ', '+')
        val url = getURL() + "/?do=search&subaction=search&story=" + newId
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        var f = text.indexOf("class=\"content_row\"")

        val res = ArrayList<Manga>()

        while (f != -1) {
            f = text.indexOf("h2", f)
            f = text.indexOf("<a", f)
            f = text.indexOf("manga/", f) + "manga".length + 1
            val s = text.indexOf("\"", f)
            res.add(Manga(this, text.subSequence(f, s).toString()))
            f = text.indexOf("class=\"content_row\"", f)
        }


        return res.toTypedArray()
    }

    // Helper class for some functions
    private class MangaChanLibraryHelper {
        // Retrieve title image URL
        fun getTitleImageURL(text : String) : String {
            var f = text.indexOf("manga_images")
            f = text.indexOf("img", f)
            f = text.indexOf("src=\"", f) + "src=\"".length
            val s = text.indexOf("\"", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve name of manga
        fun getName(text : String) : String {
            var f = text.indexOf("name_row")
            f = text.indexOf("<a", f)
            f = text.indexOf(">", f) + 1
            val s = text.indexOf("(", f) - 1
            return text.subSequence(f, s).toString()
        }

        // Retrieve russian name of manga
        fun getRusName(text : String) : String {
            var f = text.indexOf("name_row")
            f = text.indexOf("(", f) + 1
            val s = text.indexOf(")", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve description
        fun getDescr(text : String) : String {
            var f = text.indexOf("<div id=\"description\"")
            f = text.indexOf(">", f) + 1
            val s = text.indexOf("<", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve author
        fun getAuthor(text : String) : String {
            var f = text.indexOf("mangatitle")
            f = text.indexOf("Автор", f)
            f = text.indexOf("<a", f)
            f = text.indexOf(">", f) + 1
            val s = text.indexOf("<", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve tags
        fun getTags(text : String) : Array<String> {
            var f = text.indexOf("mangatitle")
            f = text.indexOf("Тэги", f)
            var s = text.indexOf("</span", f)
            val subtext = text.subSequence(f, s)

            val res = ArrayList<String>()

            f = subtext.indexOf("<a")
            while (f != -1) {
                f = subtext.indexOf(">", f) + 1
                s = subtext.indexOf("<", f)
                res.add(subtext.substring(f, s))
                f = subtext.indexOf("<a", f)
            }

            return res.toTypedArray()
        }
    }

    // Function to get info(name, author, genre, number of chapters...) about manga as JSON by its id(name)
    // MAY THROW MangaJetException
    override fun getMangaInfo(id: String) : String {
        val url = getURL() + "/manga/" + id
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        val json = JSONObject()
        json.put("name", MangaChanLibraryHelper().getName(text))
        json.put("cover", MangaChanLibraryHelper().getTitleImageURL(text))
        json.put("rus_name", MangaChanLibraryHelper().getRusName(text))
        json.put("author", MangaChanLibraryHelper().getAuthor(text))
        json.put("description", MangaChanLibraryHelper().getDescr(text))
        val tagArray = JSONArray(MangaChanLibraryHelper().getTags(text))
        json.put("tags", tagArray)
        return json.toString()
    }

    // Function to get array of MangaChapter classes by manga's id(name)
    // MAY THROW MangaJetException
    override fun getMangaChapters(manga: Manga) : Array<MangaChapter> {
        val url = getURL() + "/manga/" + manga.id
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        // Find all tables
        val tables = ArrayList<String>()
        var f = text.indexOf("class=\"table_cha\"")

        while (f != -1) {
            f = text.indexOf(">", f) + 1
            var s = text.indexOf("</table", f)
            tables.add(text.substring(f, s))
            f = text.indexOf("table_cha", f)
        }

        val chapters = ArrayList<MangaChapter>()

        for (table in tables) {
            f = table.indexOf("zaliv")
            while (f != -1) {
                f = table.indexOf("href=", f)
                f = table.indexOf("online", f) + "online".length + 1
                var s = table.indexOf("'", f)
                chapters.add(MangaChapter(manga, table.substring(f, s)))
                f = table.indexOf("zaliv", f)
            }
        }
        chapters.reverse()
        return chapters.toTypedArray()
    }

    // Function to get number of pages in specific manga and specific chapter by their ids(names)
    // MAY THROW MangaJetException
    override fun getChaptersNumOfPages(mangaID: String, chapterID: String) : Int {
        val url = getURL() + "/online/" + chapterID
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        var f = text.indexOf("fullimg")
        f = text.indexOf("[", f)
        val s = text.indexOf("]", f) - 1
        var subtext = text.subSequence(f, s).toString() + "]"

        val json = JSONArray(subtext)

        return json.length()
    }

    // Function to get Manga Page class by its number, manga id and chapter id
    // MAY THROW MangaJetException
    override fun getChapterPage(mangaID: String, chapterID: String, pageNumber: Int) : MangaPage {
        val url = getURL() + "/online/" + chapterID
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        var f = text.indexOf("fullimg")
        f = text.indexOf("[", f)
        val s = text.indexOf("]", f) - 1
        var subtext = text.subSequence(f, s).toString() + "]"

        val json = JSONArray(subtext)
        return MangaPage(json[pageNumber].toString())
    }
}
