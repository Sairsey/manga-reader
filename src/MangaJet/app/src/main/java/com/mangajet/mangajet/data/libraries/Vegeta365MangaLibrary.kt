package com.mangajet.mangajet.data.libraries

import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaChapter
import com.mangajet.mangajet.data.WebAccessor
import org.json.JSONArray
import org.json.JSONObject
import java.util.regex.Matcher
import java.util.regex.Pattern

class Vegeta365MangaLibrary(uniqueID: String, val specialWord : String = "manga") : AbstractLibrary(uniqueID) {

    val headers = mutableMapOf(
        "user-agent" to "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko)" +
                "Chrome/41.0.2228.0 Safari/537.36",
        "accept" to "*/*"
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
        val url = getURL() + "/?s=" + id + "&post_type=wp-manga&post_type=wp-manga"
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here
        val res = ArrayList<Manga>()

        var f = text.indexOf("<div role=\"tabpanel")

        var s = text.indexOf("<footer", f)
        var subtext = text.substring(f, s)

        f = subtext.indexOf("c-tabs-item__content")

        var index = 0
        while (f != -1) {
            f = subtext.indexOf("<a", f)
            var tagEnd = subtext.indexOf("</a", f)
            var imgTag = subtext.indexOf("<img", f)
            // we found valid tag
            if (imgTag != -1 && imgTag < tagEnd) {
                f = subtext.indexOf(specialWord + "/", f) + (specialWord + "/").length
                var s = subtext.indexOf("/", f)
                val mID = subtext.substring(f, s)
                if (index >= offset + amount)
                    break

                if (index >= offset)
                    res.add(Manga(this, mID))
                index++
            }
            f = subtext.indexOf("c-tabs-item__content", f + 1)
        }

        return res.toTypedArray()
    }

    // Map to transform tags for url
    private val tagsMap = mutableMapOf<String, Int>()

    // Helper class for some functions
    private inner class Vegeta365MangaLibraryHelper {

        private val tagPattern: Pattern = Pattern.compile("<.+?>")

        fun removeTags(string: String): String {
            val m: Matcher = tagPattern.matcher(string)
            return m.replaceAll("")
        }

        // Retrieve name of manga
        fun getName(text : String) : String {
            var f = text.indexOf("class=\"post-title")
            f = text.indexOf("<h", f)
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
            var f = text.indexOf("description-summary")
            f = text.indexOf("<div class=\"summary", f)
            f = text.indexOf(">", f) + ">".length
            val s = text.indexOf("<div class=\"c-content-readmore", f)
            if (f == -1 || s == -1)
                return ""
            var descr = text.subSequence(f, s).toString()
            descr = removeTags(descr)

            return descr
        }

        // Retrieve Author name of manga
        fun getAuthor(text : String) : String {
            var f = text.indexOf("Author(s)")
            f = text.indexOf("<div class=\"author-content", f)
            f = text.indexOf(">", f) + ">".length
            val s = text.indexOf("</div", f)
            if (f == -1 || s == -1)
                return ""
            val name = text.subSequence(f, s).toString()

            return removeTags(name)
        }

        // Retrieve title image URL
        fun getTitleImageURL(text : String) : String {
            var f = text.indexOf("summary_image")
            f = text.indexOf("<img", f)
            f = text.indexOf("src=\"", f) + "src=\"".length
            val s = text.indexOf("\"", f)
            if (f == -1 || s == -1)
                return ""
            return text.subSequence(f, s).toString()
        }

        // Retrieve Author name of manga
        fun getTags(text : String) : Array<String>  {
            var f = text.indexOf("<div class=\"genres")
            if (f == -1)
                return arrayOf("")

            var end = text.indexOf("/div", f)

            val res = ArrayList<String>()
            f = text.indexOf("<a", f)

            while (f < end && f != -1) {
                f = text.indexOf(">", f) + ">".length
                var s = text.indexOf("<", f)
                var name = text.substring(f, s)
                res.add(name)
                f = text.indexOf("<a", f)
            }

            // not work on search
            /*
            f = text.indexOf("wp-manga-tags-list")
            if (f != -1) {
                end = text.indexOf("/div", f)
                f = text.indexOf("<a", f)
                while (f < end && f != -1) {
                    f = text.indexOf(">", f) + ">".length
                    var s = text.indexOf("<", f)
                    var name = text.substring(f, s)
                    res.add(name)
                    f = text.indexOf("<a", f)
                }
            }
            */
            return res.toTypedArray()
        }
    }

    override fun searchMangaByTags(tags: Array<String>, amount: Int, offset: Int): Array<Manga> {
        var search = "/?s=&post_type=wp-manga&"
        for (i in 0 until tags.size) {
            if(tagsMap.containsKey(tags[i]))
                search += "genre[]=" + tagsMap[tags[i]].toString().lowercase().replace(" ", "-") + "&"
        }

        val url = getURL() + search
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here
        val res = ArrayList<Manga>()

        var f = text.indexOf("<div role=\"tabpanel")

        var s = text.indexOf("<footer", f)
        var subtext = text.substring(f, s)

        f = subtext.indexOf("c-tabs-item__content")

        var index = 0
        while (f != -1) {
            f = subtext.indexOf("<a", f)
            var tagEnd = subtext.indexOf("</a", f)
            var imgTag = subtext.indexOf("<img", f)
            // we found valid tag
            if (imgTag != -1 && imgTag < tagEnd) {
                f = subtext.indexOf(specialWord + "/", f) + (specialWord + "/").length
                var s = subtext.indexOf("/", f)
                val mID = subtext.substring(f, s)
                if (index >= offset + amount)
                    break

                if (index >= offset)
                    res.add(Manga(this, mID))
                index++
            }
            f = subtext.indexOf("c-tabs-item__content", f + 1)
        }

        return res.toTypedArray()
    }

    override fun getPopularManga(amount: Int, offset: Int): Array<Manga> {
        val url = getURL() + "/manga/"
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here
        val res = ArrayList<Manga>()

        var f = text.indexOf("<div role=\"tabpanel")

        var s = text.indexOf("<nav", f)
        var subtext = text.substring(f, s)

        f = subtext.indexOf("<a")

        var index = 0
        while (f != -1) {
            var tagEnd = subtext.indexOf("</a", f)
            var imgTag = subtext.indexOf("<img", f)
            // we found valid tag
            if (imgTag != -1 && imgTag < tagEnd) {
                f = subtext.indexOf(specialWord + "/", f) + (specialWord + "/").length
                var s = subtext.indexOf("/", f)
                val mID = subtext.substring(f, s)
                if (index >= offset + amount)
                    break

                if (index >= offset)
                    res.add(Manga(this, mID))

                f = subtext.indexOf("<div id=\"manga-item", f)
                index++
            }
            f = subtext.indexOf("<a", f + 1)
        }

        return res.toTypedArray()
    }

    override fun getMangaInfo(id: String): String {
        val url = getURL() + "/" + specialWord + "/" + id
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        val json = JSONObject()
        json.put("name", transformFromHtml(Vegeta365MangaLibraryHelper().getName(text)))
        json.put("cover", transformFromHtml(Vegeta365MangaLibraryHelper().getTitleImageURL(text)))
        json.put("rus_name", transformFromHtml(Vegeta365MangaLibraryHelper().getRusName(text)))
        json.put("author", transformFromHtml(Vegeta365MangaLibraryHelper().getAuthor(text)))
        json.put("description", transformFromHtml(Vegeta365MangaLibraryHelper().getDescr(text)))
        val tagArray = JSONArray(Vegeta365MangaLibraryHelper().getTags(text))
        json.put("tags", tagArray)
        return json.toString()
    }

    override fun getMangaChapters(manga: Manga): Array<MangaChapter> {
        val url = getURL() + "/" + specialWord + "/" + manga.id
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        val chapters = ArrayList<MangaChapter>()

        var f = text.indexOf("<li class=\"wp-manga-chapter")

        while (f != -1) {
            f = text.indexOf("href=\"", f) + "href=\"".length
            var s = text.indexOf("\"", f)
            var link = text.substring(f, s)
            f = text.indexOf(">", f) + ">".length
            s = text.indexOf("<", f)
            var name = text.substring(f, s)
            var id = link.substring(url.length)

            chapters.add(MangaChapter(manga, id, transformFromHtml(name)))

            f = text.indexOf("<li class=\"wp-manga-chapter", f)
        }
        chapters.reverse()

        return chapters.toTypedArray()
    }

    override fun getChapterInfo(mangaID: String, chapterID: String): String {
        val url = getURL() + "/" + specialWord + "/" + mangaID + "/" + chapterID
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        val pages = ArrayList<String>()

        var f = text.indexOf("<img id=\"image-")

        while (f != -1) {
            f = text.indexOf("src=\"", f) + "src=\"".length
            var s = text.indexOf("\"", f)
            pages.add(text.substring(f, s))
            f = text.indexOf("<img id=\"image-", f)
        }

        return JSONArray(pages).toString()
    }
}
