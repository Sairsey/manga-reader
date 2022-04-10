package com.mangajet.mangajet.data.libraries

import android.os.Build
import android.text.Html
import androidx.core.text.isDigitsOnly
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaChapter
import com.mangajet.mangajet.data.WebAccessor
import org.json.JSONArray
import org.json.JSONObject

class MangaLibLibrary(uniqueID: String) : AbstractLibrary(uniqueID) {

    val headers = mutableMapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36",
        "Accept" to "*/*",
        "Referer" to getURL() + "/")

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
    override fun searchManga(id: String, amount: Int, offset: Int): Array<Manga> {
        val url = getURL() + "/manga-list?name=" + id
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        var f = text.indexOf("media-card-wrap")

        val res = ArrayList<Manga>()

        var index = 0
        while (f != -1) {
            f = text.indexOf("slug=\"", f) + "slug=\"".length
            val s = text.indexOf("\"", f)
            if (index >= offset + amount)
                break

            if (index >= offset)
                res.add(Manga(this, text.subSequence(f, s).toString()))
            f = text.indexOf("media-card-wrap", f)
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
    private class MangaLibLibraryHelper {
        // Retrieve title image URL
        fun getTitleImageURL(text : String) : String {
            var f = text.indexOf("cover paper\">")
            f = text.indexOf("img src=\"", f) + "img src=\"".length
            val s = text.indexOf("\"", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve name of manga
        fun getName(text : String) : String {
            val f = text.indexOf("media-name__alt\">") + "media-name__alt\">".length
            if(f == - 1 + "media-name__alt\">".length)
                return getRusName(text)
            val s = text.indexOf("<", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve rating of manga
        fun getRating(text : String) : String {
            var f = text.indexOf("bestRating\"")
            f = text.indexOf("content=\"", f) + "content=\"".length
            val s = text.indexOf("\"", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve russian name of manga
        fun getRusName(text : String) : String {
            val f = text.indexOf("media-name__main\">") + "media-name__main\">".length
            val s = text.indexOf("<", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve description
        fun getDescr(text : String) : String {
            var f = text.indexOf("itemprop=\"description\"")
            if(f == -1)
                return ""
            f = text.indexOf("content=\"", f) + "content=\"".length
            val s = text.indexOf("\"", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve author
        fun getAuthor(text : String) : String {
            var f = text.indexOf("Автор")
            f = text.indexOf("<a href", f)
            f = text.indexOf("\">", f) + "\">".length
            val s = text.indexOf("<", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve tags
        fun getTags(text : String) : Array<String> {
            val res = ArrayList<String>()
            var f = text.indexOf("media-tags\"")
            var s = text.indexOf("<div", f)

            val subtext = text.substring(f, s)
            f = subtext.indexOf("item")
            f = subtext.indexOf(">", f) + 1
            while (f != 0) {
                s = subtext.indexOf("<", f)
                res.add(subtext.substring(f, s).trim())
                f = subtext.indexOf("item", s)
                if(f == -1)
                    break
                f = subtext.indexOf(">", f) + 1
            }
            return res.toTypedArray()
        }
    }

    // Function to get info(name, author, genre, number of chapters...) about manga as JSON by its id(name)
    // MAY THROW MangaJetException
    override fun getMangaInfo(id: String) : String {
        val url = getURL() + "/" + id + "?section=info"
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        val json = JSONObject()
        json.put("name", transformFromHtml(MangaLibLibraryHelper().getName(text)))
        json.put("cover", transformFromHtml(MangaLibLibraryHelper().getTitleImageURL(text)))
        json.put("rus_name", transformFromHtml(MangaLibLibraryHelper().getRusName(text)))
        json.put("rating", transformFromHtml(MangaLibLibraryHelper().getRating(text)))
        json.put("author", transformFromHtml(MangaLibLibraryHelper().getAuthor(text)))
        json.put("description", transformFromHtml(MangaLibLibraryHelper().getDescr(text)))
        val tagArray = JSONArray(MangaLibLibraryHelper().getTags(text))
        json.put("tags", tagArray)
        return json.toString()
    }

    // Function to get array of MangaChapter classes by manga's id(name)
    // MAY THROW MangaJetException
    override fun getMangaChapters(manga: Manga) : Array<MangaChapter> {
        val url = getURL() + "/" + manga.id + "?section=chapters"
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        val chapters = ArrayList<MangaChapter>()

        val f = text.indexOf("window.__DATA__ = ") + "window.__DATA__ = ".length
        var s = text.indexOf("window._SITE_COLOR_ ", f)
        var subtext = text.substring(f, s)
        s = subtext.lastIndexOf(';')
        subtext = subtext.substring(0, s)

        val json = JSONObject(JSONObject(subtext)["chapters"].toString())
        val query = json.getJSONArray("list")
        for (i in 0 until query.length()) {
            var id = manga.id + "/v" + query.getJSONObject(i).getString("chapter_volume") + '/'
            id += 'c' + query.getJSONObject(i).getString("chapter_number")
            var name = query.getJSONObject(i).getString("chapter_name")
            if(name.equals("null"))
                name = ""
            chapters.add(MangaChapter(manga, id, transformFromHtml(name)))
        }
        chapters.reverse()

        return chapters.toTypedArray()
    }

    // Function to get array of pages in specific manga, specific chapter by their ids(names)
    // MAY THROW MangaJetException
    override fun getChapterInfo(mangaID: String, chapterID: String) : String {
        val url = getURL() + "/" + chapterID
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here
        val res = ArrayList<String>()

        // Get amount of pages
        var f = text.indexOf("window.__pg = ") + "window.__pg = ".length
        var s = text.indexOf(";", f)
        var subtext = text.substring(f, s)
        val pageData = JSONArray(subtext)
        // Get url
        f = text.indexOf("window.__info = ") + "window.__info = ".length
        s = text.lastIndexOf(';')
        subtext = text.substring(f, s)

        val json = JSONObject(subtext)
        json.optJSONObject("")
        val urlEnd = JSONObject(json["img"].toString())["url"].toString()
        val urlBegin = JSONObject(json["servers"].toString())["main"].toString().
        replace('2', '3') // Ohhh, dunno, but this is for hentai

        // Combine
        for(i in 0 until pageData.length())
                res.add(urlBegin + '/' + urlEnd + pageData.getJSONObject(i).getString("u"))

        return JSONArray(res).toString()
    }

}
