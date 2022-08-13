package com.mangajet.mangajet.data.libraries

import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaChapter
import com.mangajet.mangajet.data.WebAccessor
import org.json.JSONArray
import org.json.JSONObject

class MangaReaderLibrary(uniqueID: String) : AbstractLibrary(uniqueID) {

    val headers = mutableMapOf(
        "user-agent" to "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko)" +
                "Chrome/41.0.2228.0 Safari/537.36",
        "accept" to "*/*")

    // Function to get Manga class by its id(name)
    override fun createMangaById(id: String) : Manga {
        return Manga(this, id)
    }

    // Function to set cookies after authentication
    override fun setCookies(cookies: String) {
        headers["Cookie"] = cookies
    }

    // Function to get cookies after authentication
    override fun getCookies(): String {
        return headers.getOrDefault("Cookie", "")
    }

    // Function to get headers if we need
    override fun getHeadersForDownload(): Map<String, String> {
        return headers.toMap()
    }

    // Function to get array of Manga classes by its id(name), amount of mangas(optional)
    // and offset from start(optional)
    // MAY THROW MangaJetException
    override fun searchManga(id: String, amount: Int, offset: Int) : Array<Manga> {
        val newId = id.replace(' ', '+')
        val url = getURL() + "/search?s=" + newId
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        var f = text.indexOf("class=\"anipost\"")

        val res = ArrayList<Manga>()

        var index = 0
        while (f != -1) {
            f = text.indexOf("<a", f)
            f = text.indexOf("manga/", f) + "manga/".length
            val s = text.indexOf("\"", f)
            if (index >= offset + amount)
                break

            if (index >= offset)
                res.add(Manga(this, text.subSequence(f, s).toString()))
            f = text.indexOf("class=\"anipost\"", f)
            index++
        }

        return res.toTypedArray()
    }

    // Function to get array of Manga classes by tags, amount of mangas(optional)
    // and offset from start(optional)
    // MAY THROW MangaJetException
    override fun searchMangaByTags(tags: Array<String>, amount: Int, offset: Int) : Array<Manga>{
        val tag = tags[0].lowercase()// Sorry, MangaReader does not support multiple tags
        val url = getURL() + "/genres/" + tag
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here
        var f = text.indexOf("class=\"anipost\"")

        val res = ArrayList<Manga>()

        var index = 0
        while (f != -1) {
            f = text.indexOf("<a", f)
            f = text.indexOf("manga/", f) + "manga/".length
            val s = text.indexOf("\"", f)
            if (index >= offset + amount)
                break

            if (index >= offset)
                res.add(Manga(this, text.subSequence(f, s).toString()))
            f = text.indexOf("class=\"anipost\"", f)
            index++
        }

        return res.toTypedArray()
    }

    // Function to get array of Manga classes by popularity, amount of mangas(optional)
    // and offset from start(optional)
    // MAY THROW MangaJetException
    override fun getPopularManga(amount: Int, offset: Int) : Array<Manga>{
        val url = getURL() + "/popular-manga"
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        var f = text.indexOf("class=\"anipost\"")

        val res = ArrayList<Manga>()

        var index = 0
        while (f != -1) {
            f = text.indexOf("<a", f)
            f = text.indexOf("manga/", f) + "manga/".length
            val s = text.indexOf("\"", f)
            if (index >= offset + amount)
                break

            if (index >= offset)
                res.add(Manga(this, text.subSequence(f, s).toString()))
            f = text.indexOf("class=\"anipost\"", f)
            index++
        }

        return res.toTypedArray()
    }

    // Helper class for some functions
    private class MangaReaderLibraryHelper {
        // Retrieve title image URL
        fun getTitleImageURL(text : String) : String {
            var f = text.indexOf("class=\"imgdesc\"")
            f = text.indexOf("img", f)
            f = text.indexOf("src=\"", f) + "src=\"".length
            val s = text.indexOf("\"", f)
            val url = text.subSequence(f, s).toString()
            return if (url.contains("https:"))
                url
            else
                "https:$url"

        }

        // Retrieve name of manga
        @Suppress("SwallowedException")
        fun getName(text : String) : String {
            var f = text.indexOf("<h1 itemprop=\"name\"")
            f = text.indexOf(">", f) + ">".length
            val s = text.indexOf("<", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve russian name of manga
        fun getRusName(text : String) : String {
            return getName(text)
        }

        // Retrieve description
        fun getDescr(text : String) : String {
            var f = text.indexOf("<div id=\"noidungm\"")
            if (f == -1)
                return ""
            f = text.indexOf(">", f) + 1
            val s = text.indexOf("<", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve author
        fun getAuthor(text : String) : String {
            var f = text.indexOf("Author</b>")
            f = text.indexOf(":", f) + ":".length
            val s = text.indexOf("<", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve tags
        fun getTags(text : String) : Array<String> {
            val res = ArrayList<String>()
            var f = text.indexOf("Genres</b>")
            if (f == -1)
                return res.toTypedArray()
            f = text.indexOf("<a class=\"green", f)
            while (f != -1) {
                f = text.indexOf(">", f) + "<".length
                val s = text.indexOf("<", f)
                res.add(text.substring(f, s))
                f = text.indexOf("<a class=\"green", s)
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
        json.put("name", transformFromHtml(MangaReaderLibraryHelper().getName(text)))
        json.put("cover", transformFromHtml(MangaReaderLibraryHelper().getTitleImageURL(text)))
        json.put("rus_name", transformFromHtml(MangaReaderLibraryHelper().getRusName(text)))
        json.put("author", transformFromHtml(MangaReaderLibraryHelper().getAuthor(text)))
        json.put("description", transformFromHtml(MangaReaderLibraryHelper().getDescr(text)))
        val tagArray = JSONArray(MangaReaderLibraryHelper().getTags(text))
        json.put("tags", tagArray)
        return json.toString()
    }

    // Function to get array of MangaChapter classes by manga's id(name)
    // MAY THROW MangaJetException
    override fun getMangaChapters(manga: Manga) : Array<MangaChapter> {
        val url = getURL() + "/manga/" + manga.id
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        val chapters = ArrayList<MangaChapter>()

        var f = text.indexOf("<span class=\"rightoff")
        f = text.indexOf("<span class=\"leftoff", f)

        while (f != -1) {
            val oldF = f + "<span class=\"leftoff".length
            var s = text.indexOf("</span>", f)
            val subtext = text.substring(f, s)
            f = subtext.indexOf("chapter/") + "chapter/".length
            s = subtext.indexOf("\"", f)
            val id = subtext.substring(f, s)

            f = subtext.indexOf("chapter-")
            s = subtext.indexOf("\"", f)
            val chapterNumber = subtext.substring(f, s).replace('c', 'C').replace('-', ' ')

            f = subtext.indexOf("</a>", s)
            f = subtext.indexOf(":", f)
            var name = ""
            if(f != -1)
                name = " - " + subtext.substring(f + 1).trim()

            chapters.add(MangaChapter(manga, id, transformFromHtml(name),
                chapterNumber + transformFromHtml(name)))

            f = text.indexOf("<span class=\"leftoff", oldF)
        }

        chapters.reverse()
        return chapters.toTypedArray()
    }

    // Function to get array of pages in specific manga, specific chapter by their ids(names)
    // MAY THROW MangaJetException
    override fun getChapterInfo(mangaID: String, chapterID: String) : String {
        var url = getURL() + "/chapter/" + chapterID
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        var f = text.indexOf("<p id=arraydata style=display:none>") + "<p id=arraydata style=display:none>".length
        val s = text.indexOf("</p>", f)
        val subtext = text.substring(f, s)

        val res = subtext.split(",").toTypedArray()
        return JSONArray(res).toString()
    }
}
