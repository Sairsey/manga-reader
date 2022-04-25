package com.mangajet.mangajet.data.libraries

import android.os.Build
import android.text.Html
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaChapter
import com.mangajet.mangajet.data.WebAccessor
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
        val url = getURL() + "/?do=search&subaction=search&story=" + newId
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        var f = text.indexOf("class=\"content_row\"")

        val res = ArrayList<Manga>()

        var index = 0
        while (f != -1) {
            f = text.indexOf("h2", f)
            f = text.indexOf("<a", f)
            f = text.indexOf("manga/", f) + "manga".length + 1
            val s = text.indexOf("\"", f)
            if (index >= offset + amount)
                break

            if (index >= offset)
                res.add(Manga(this, text.subSequence(f, s).toString()))
            f = text.indexOf("class=\"content_row\"", f)
            index++
        }


        return res.toTypedArray()
    }

    // Helper function to delete weird Html symbols
    private fun transformFromHtml(text : String) : String{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
        else
            Html.fromHtml(text).toString()
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
        @Suppress("SwallowedException")
        fun getName(text : String) : String {
            var f = text.indexOf("name_row")
            f = text.indexOf("<a", f)
            f = text.indexOf(">", f) + 1
            val sLast = text.indexOf("</a>", f) - 1
            val s = text.indexOf("(", f) - 1
            try {
                if (s == -1 || s > sLast)
                    return text.subSequence(f, sLast).toString()
                return text.subSequence(f, s).toString()
            }
            catch (ex: StringIndexOutOfBoundsException) {
                return ""
            }
        }

        // Retrieve russian name of manga
        fun getRusName(text : String) : String {
            var f = text.indexOf("name_row")
            f = text.indexOf("<a", f)
            f = text.indexOf(">", f) + 1
            val sLast = text.indexOf("</a>", f) - 1;
            f = text.indexOf("(", f) + 1
            if (f == -1 || f > sLast)
                return ""
            val s = text.indexOf(")", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve description
        fun getDescr(text : String) : String {
            var f = text.indexOf("<div id=\"description\"")
            if (f == -1)
                return ""
            f = text.indexOf(">", f) + 1
            val s = text.indexOf("<", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve author
        fun getAuthor(text : String) : String {
            var f = text.indexOf(">Автор")
            f = text.indexOf("<a", f)
            f = text.indexOf(">", f) + 1
            val s = text.indexOf("<", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve tags
        fun getTags(text : String) : Array<String> {
            val res = ArrayList<String>()
            var f = text.indexOf("mangatitle")
            if (f == -1)
                return res.toTypedArray()
            f = text.indexOf("Тэги", f)
            var s = text.indexOf("</span", f)
            val subtext = text.subSequence(f, s)


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
        json.put("name", transformFromHtml(MangaChanLibraryHelper().getName(text)))
        json.put("cover", transformFromHtml(MangaChanLibraryHelper().getTitleImageURL(text)))
        json.put("rus_name", transformFromHtml(MangaChanLibraryHelper().getRusName(text)))
        json.put("author", transformFromHtml(MangaChanLibraryHelper().getAuthor(text)))
        json.put("description", transformFromHtml(MangaChanLibraryHelper().getDescr(text)))
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
                var fullnameStart = table.indexOf(">", s) + 1
                var nameStart = table.indexOf("&nbsp;&nbsp;", s) + "&nbsp;&nbsp;".length + 1
                nameStart = table.indexOf("&nbsp;&nbsp;", nameStart) + "&nbsp;&nbsp;".length
                if(nameStart == "&nbsp;&nbsp;".length - 1)
                    chapters.add(MangaChapter(manga, table.substring(f, s)))
                else {
                    var nameFinish = table.indexOf("<", nameStart)
                    chapters.add(MangaChapter(manga, table.substring(f, s),
                        transformFromHtml(table.substring(nameStart, nameFinish)),
                        transformFromHtml(table.substring(fullnameStart, nameFinish))))
                    f = table.indexOf("zaliv", f)
                }
            }
        }
        chapters.reverse()
        if (chapters.size == 0)
            chapters.add(MangaChapter(manga, manga.id))
        return chapters.toTypedArray()
    }

    // on some sites we might go to another URL
    val secretURL = "http://exhentai-dono.me"

    // Function to get array of pages in specific manga, specific chapter by their ids(names)
    // MAY THROW MangaJetException
    override fun getChapterInfo(mangaID: String, chapterID: String) : String {
        var url = "/online/" + chapterID
        if (getURL()[getURL().length - 2] == 'v')
            url = secretURL + url + "?development_access=true"
        else
            url = getURL() + url
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        var f = text.indexOf("fullimg")
        f = text.indexOf("[", f)
        val s = text.indexOf("]", f)
        var subtext = text.subSequence(f, s).toString() + "]"
        if (subtext[subtext.length - 2] == ',')
            subtext = subtext.dropLast(2) + "]"

        val json = JSONArray(subtext)

        return json.toString()
    }
}
