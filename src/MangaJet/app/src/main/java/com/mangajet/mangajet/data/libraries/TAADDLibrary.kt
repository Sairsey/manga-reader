package com.mangajet.mangajet.data.libraries

import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaChapter
import com.mangajet.mangajet.data.WebAccessor
import org.json.JSONArray
import org.json.JSONObject

class TAADDLibrary(uniqueID: String) : AbstractLibrary(uniqueID) {

    val headers = mutableMapOf(
        "user-agent" to "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko)" +
                "Chrome/41.0.2228.0 Safari/537.36",
        "accept" to "*/*",
        "accept-language" to "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7",
        "upgrade-insecure-requests" to "1",
    )

    // Function to get Manga class by its id(name)
    override fun createMangaById(id: String): Manga {
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
    override fun searchManga(id: String, amount: Int, offset: Int): Array<Manga> {
        val url = getURL() + "/search/?wd=" + id
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here
        val res = ArrayList<Manga>()

        var f = text.indexOf("<div class=\"clistChr\"")
        if (f == -1)
            return res.toTypedArray()

        var s = text.indexOf("</ul", f)
        if (s == -1)
            return res.toTypedArray()

        val subtext = text.substring(f, s)

        f = subtext.indexOf("\"intro")

        var index = 0
        while (f != -1) {
            f = subtext.indexOf("book/", f) + "book/".length
            s = subtext.indexOf(".html", f)
            val mID = subtext.substring(f, s)
            if (index >= offset + amount)
                break

            if (index >= offset)
                res.add(Manga(this, mID))
            f = subtext.indexOf("\"intro", f)
            index++
        }

        return res.toTypedArray()
    }

    // Map to transform tags for url
    private val tagsMap = mutableMapOf<String, Int>()

    // Helper class for some functions
    private inner class TAADDLibraryHelper {
        // Function to build tags map for search by tags
        // MAY THROW MangaJetException
        fun buildTagsMap() {
            if (tagsMap.isNotEmpty())
                return
            // at first lets find our tag list
            val url = getURL() + "/search"
            val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here
            var f = text.indexOf("optionbox")
            if (f == -1)
                return

            var s = text.indexOf("category_id", f)

            f = text.indexOf("<ul>", f)
            if (f > s || f == -1)
                return

            s = text.indexOf("</ul>", f)

            val subtext = text.substring(f, s)

            f = subtext.indexOf("li")

            while (f != -1) {
                f = subtext.indexOf("cate_id=\"", f) + "cate_id=\"".length
                s = subtext.indexOf("\"", f)
                val cateId = subtext.substring(f, s).toInt()
                f = subtext.indexOf("<a", f)
                f = subtext.indexOf(">", f) + 1
                s = subtext.indexOf("<", f)
                val cateName = subtext.substring(f, s)
                tagsMap[cateName] = cateId
                f = subtext.indexOf("<li", f)
            }
        }

        // Retrieve URL of page
        fun getPageUrl(chapterID: String, index: Int) : String {
            val url = getURL() + "/chapter/" + chapterID + "-" + index.toString() + ".html"
            val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

            var f = text.indexOf("comicpic")
            f = text.indexOf("src=\"", f) + "src=\"".length
            var s = text.indexOf("\"", f)
            return text.substring(f, s)
        }

        // Retrieve name of manga
        fun getName(text : String) : String {
            var f = text.indexOf("<h1")
            f = text.indexOf(">", f) + ">".length
            val s = text.indexOf("<", f)
            val name = text.subSequence(f, s).toString()

            return name
        }

        // Retrieve russian name of manga
        fun getRusName(text : String) : String {
            return getName(text)
        }

        // Retrieve Description
        fun getDescr(text : String) : String {
            var f = text.indexOf("Summary</b>") + "Summary</b>".length
            f = text.indexOf(">", f) + ">".length
            val s = text.indexOf("</td>", f)
            if (f == -1 || s == -1)
                return ""
            val descr = text.subSequence(f, s).toString()

            return descr
        }

        // Retrieve Author name of manga
        fun getAuthor(text : String) : String {
            var f = text.indexOf("Author")
            f = text.indexOf(">", f) + ">".length
            val s = text.indexOf("<", f)
            if (f == -1 || s == -1)
                return ""
            val name = text.subSequence(f, s).toString()

            return name
        }

        // Retrieve title image URL
        fun getTitleImageURL(text : String) : String {
            var f = text.indexOf("table")
            f = text.indexOf("<img", f)
            f = text.indexOf("src=\"", f) + "src=\"".length
            val s = text.indexOf("\"", f)
            if (f == -1 || s == -1)
                return ""
            return text.subSequence(f, s).toString()
        }

        // Retrieve Author name of manga
        fun getTags(text : String) : Array<String>  {
            var f = text.indexOf("Categories")
            if (f == -1)
                return arrayOf("")

            val end = text.indexOf("/td", f)

            val res = ArrayList<String>()
            f = text.indexOf("<a", f)

            while (f < end && f != -1) {
                f = text.indexOf("<img", f)
                f = text.indexOf(">", f) + ">".length
                var s = text.indexOf("<", f)
                var name = text.substring(f, s)
                res.add(name)
                f = text.indexOf("<a", f)
            }
            return res.toTypedArray()
        }
    }

    override fun searchMangaByTags(tags: Array<String>, amount: Int, offset: Int): Array<Manga> {
        TAADDLibraryHelper().buildTagsMap()
        var search = ""
        for (i in 0 until tags.size) {
            if(tagsMap.containsKey(tags[i]))
                search += tagsMap[tags[i]].toString() + ","
        }

        val url = getURL() + "/search/?category_id=" + search
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here
        val res = ArrayList<Manga>()

        var f = text.indexOf("<div class=\"clistChr\"")
        if (f == -1)
            return res.toTypedArray()

        var s = text.indexOf("</ul", f)
        if (s == -1)
            return res.toTypedArray()

        val subtext = text.substring(f, s)

        f = subtext.indexOf("\"intro")

        var index = 0
        while (f != -1) {
            f = subtext.indexOf("book/", f) + "book/".length
            s = subtext.indexOf(".html", f)
            val mID = subtext.substring(f, s)
            if (index >= offset + amount)
                break

            if (index >= offset)
                res.add(Manga(this, mID))
            f = subtext.indexOf("\"intro", f)
            index++
        }

        return res.toTypedArray()
    }

    override fun getPopularManga(amount: Int, offset: Int): Array<Manga> {
        val url = getURL() + "/list/Hot-Book/"
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here
        val res = ArrayList<Manga>()

        var f = text.indexOf("<div class=\"clistChr\"")
        if (f == -1)
            return res.toTypedArray()

        var s = text.indexOf("</ul", f)
        if (s == -1)
            return res.toTypedArray()

        val subtext = text.substring(f, s)

        f = subtext.indexOf("\"intro")

        var index = 0
        while (f != -1) {
            f = subtext.indexOf("book/", f) + "book/".length
            s = subtext.indexOf(".html", f)
            val mID = subtext.substring(f, s)
            if (index >= offset + amount)
                break

            if (index >= offset)
                res.add(Manga(this, mID))
            f = subtext.indexOf("\"intro", f)
            index++
        }

        return res.toTypedArray()
    }

    override fun getMangaInfo(id: String): String {
        val url = getURL() + "/book/" + id + ".html?waring=1"
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        val json = JSONObject()
        json.put("name", transformFromHtml(TAADDLibraryHelper().getName(text)))
        json.put("cover", transformFromHtml(TAADDLibraryHelper().getTitleImageURL(text)))
        json.put("rus_name", transformFromHtml(TAADDLibraryHelper().getRusName(text)))
        json.put("author", transformFromHtml(TAADDLibraryHelper().getAuthor(text)))
        json.put("description", transformFromHtml(TAADDLibraryHelper().getDescr(text)))
        val tagArray = JSONArray(TAADDLibraryHelper().getTags(text))
        json.put("tags", tagArray)
        return json.toString()
    }

    override fun getMangaChapters(manga: Manga): Array<MangaChapter> {
        val url = getURL() + "/book/" + manga.id + ".html?waring=1"
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        val chapters = ArrayList<MangaChapter>()

        var f = text.indexOf("chapter_list")

        var end = text.indexOf("/table", f)

        f = text.indexOf("<a", f)

        while (f < end && f != -1) {
            f = text.indexOf("href=\"", f) + "href=\"".length
            var s = text.indexOf("\"", f)
            var link = text.substring(f, s)
            f = text.indexOf(">", f) + ">".length
            s = text.indexOf("<", f)
            var name = text.substring(f, s)
            var id = link.substring("/chapter/".length, link.length - 1)

            chapters.add(MangaChapter(manga, id, transformFromHtml(name)))
            f = text.indexOf("<a", f) + 1 // find next (but same) link
            f = text.indexOf("<a", f) // find next link
        }
        chapters.reverse()

        return chapters.toTypedArray()
    }

    override fun getChapterInfo(mangaID: String, chapterID: String): String {
        val url = getURL() + "/chapter/" + chapterID + ".html"
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        val pages = ArrayList<String>()

        var f = text.indexOf("name=\"page\"")
        var s = text.indexOf("/select", f)
        var subtext = text.substring(f, s)

        f = subtext.lastIndexOf("<option")
        f = subtext.indexOf(">", f) + ">".length
        s = subtext.indexOf("<", f)
        var amount = subtext.substring(f, s).toInt()

        for (i in 1..amount)
            pages.add(TAADDLibraryHelper().getPageUrl(chapterID, i))

        return JSONArray(pages).toString()
    }
}
