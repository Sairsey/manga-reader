package com.mangajet.mangajet.data.libraries

import android.os.Build
import android.text.Html
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaChapter
import com.mangajet.mangajet.data.WebAccessor
import org.json.JSONArray
import org.json.JSONObject

// Class that represents Manga-Chan.me library
class AcomicsLibrary(uniqueID: String) : AbstractLibrary(uniqueID) {

    val headers = mutableMapOf(
        "user-agent" to "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko)" +
                "Chrome/41.0.2228.0 Safari/537.36",
        "Cookie" to "ageRestrict=18",
        "accept" to "*/*")

    // Global constant to know how many pages in chapter in this version
    companion object {
        private const val PAGESPERCHAPTER = 20
    }

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
        val basicURL = getURL()
        val url = basicURL + "/search?keyword=" + id
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here
        var f = text.indexOf("list-loadable")

        val res = ArrayList<Manga>()

        var index = 0
        while (f != -1) {
            f = text.indexOf(basicURL, f) + basicURL.length + 1
            val s = text.indexOf("\"", f)
            if (index >= offset + amount)
                break

            if (index >= offset)
                res.add(Manga(this, text.subSequence(f, s).toString()))
            f = text.indexOf("list-loadable", f)
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
    private class AcomicsLibraryHelper {
        // Retrieve title image URL
        fun getTitleImageURL(text : String) : String {
            val sizes = arrayOf("55x55", "88x31", "200x40", "160x90", "468x60", "470x150")
            var cover = "/design/main/pic/catalog-stub.png?18-07-2014"
            for(size in sizes) {
                var f = text.indexOf(size)
                if (f == -1)
                    continue
                f = text.indexOf("img src=\"", f) + "img src=\"".length
                val s = text.indexOf("\"", f)
                cover = text.subSequence(f, s).toString()
            }
            return cover
        }

        // Retrieve name of manga
        fun getName(text : String) : String {
            var f = text.indexOf("contentMargin")
            f = text.indexOf("<h2>", f) + "<h2>".length
            var s = text.indexOf("<", f)
            var subtext = text.subSequence(f, s).toString()
            f = subtext.indexOf("[") + 1
            if(f == 0)
                return getRusName(text)
            s = subtext.indexOf("]", f)
            return subtext.subSequence(f, s).toString()
        }

        // Retrieve russian name of manga
        fun getRusName(text : String) : String {
            var f = text.indexOf("contentMargin")
            f = text.indexOf("<h2>", f) + "<h2>".length
            var s = text.indexOf("<", f)
            var subtext = text.subSequence(f, s).toString()

            f = subtext.indexOf("[")
            if(f != -1)
                subtext = subtext.subSequence(0, f).toString()
            return subtext.trim()
        }

        // Retrieve description
        fun getDescr(text : String) : String {
            var f = text.indexOf("about-summary")
            f = text.indexOf("</div>", f) + "</div>".length
            val s = text.indexOf("<b>Автор", f)
            if(s == -1)
                return ""
            val subtext = text.subSequence(f, s + 1).toString()

            // Remove tags
            var isTag = false
            var str = ""
            for(s in subtext){
                 if (s == '<')
                     isTag = true
                else if(s == '>')
                    isTag = false
                else if(!isTag)
                    str += s
            }
            val strArray = str.split("\\s".toRegex()).toTypedArray()
            var descr = ""
            for(str in strArray)
                descr += "$str "

            return descr
        }

        // Retrieve author
        fun getAuthor(text : String) : String {
            var f = text.indexOf("Автор:")
            if(f == -1)
                f = text.indexOf("Автор ")

            f = text.indexOf(">", f) + 1
            var s = text.indexOf("<", f)
            if(s - 1 == f){
                f = text.indexOf(">", s) + 1
                s = text.indexOf("<", f)
            }
            return text.subSequence(f, s).toString().trim()
        }

        // Retrieve tags
        fun getTags(text : String) : Array<String> {
            val res = ArrayList<String>()

            var f = text.indexOf("about-summary")
            f = text.indexOf("div", f) + "div".length
            var s = text.indexOf("div", f)
            val subtext = text.subSequence(f, s).toString()
            f = subtext.indexOf("</span>") + "</span>".length

            while(f != -1 + "</span>".length) {
                s = subtext.indexOf("<", f)
                res.add(subtext.substring(f, s))
                f = subtext.indexOf("</span>", s) + "</span>".length
            }

            return res.toTypedArray()
        }
    }

    // Function to get info(name, author, genre, number of chapters...) about manga as JSON by its id(name)
    // MAY THROW MangaJetException
    override fun getMangaInfo(id: String) : String {
        val url = getURL() + "/" + id
        val about = WebAccessor.getTextSync("$url/about", headers) // Exception may be thrown here
        val banner = WebAccessor.getTextSync("$url/banner", headers) // Exception may be thrown here

        val json = JSONObject()
        json.put("name", transformFromHtml(AcomicsLibraryHelper().getName(about)))
        json.put("cover", getURL() + transformFromHtml(AcomicsLibraryHelper().getTitleImageURL(banner)))
        json.put("rus_name", transformFromHtml(AcomicsLibraryHelper().getRusName(about)))
        json.put("author", transformFromHtml(AcomicsLibraryHelper().getAuthor(about)))
        json.put("description", transformFromHtml(AcomicsLibraryHelper().getDescr(about)))
        val tagArray = JSONArray(AcomicsLibraryHelper().getTags(about))
        json.put("tags", tagArray)
        return json.toString()
    }

    // Function to get array of MangaChapter classes by manga's id(name)
    // MAY THROW MangaJetException
    override fun getMangaChapters(manga: Manga) : Array<MangaChapter> {
        val url = getURL() + "/" + manga.id + "/content"
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        val chapters = ArrayList<MangaChapter>()

        var f = text.indexOf("pageNav")
        if(f != -1){
            chapters.add(MangaChapter(manga, manga.id + "/content"))
            // Get Number of chapters
            var s = text.indexOf("<table>", f)
            val subtext = text.subSequence(f, s).toString()
            f = subtext.indexOf("button") + "button".length
            var z = f
            while(true) {
                if(z == -1 + "button".length)
                    break
                f = z
                z = subtext.indexOf("button", f) + "button".length
            }
            f = subtext.indexOf("<", f)
            f = subtext.indexOf(">", f) + 1
            s = subtext.indexOf("<", f)
            val chaptersAmount = subtext.substring(f, s).toInt()
            // Fill chapters
            for(i in 1 until chaptersAmount)
                chapters.add(MangaChapter(manga, manga.id + "/content?skip=" + (i * PAGESPERCHAPTER).toString()))
        }

        if (chapters.size == 0)
            chapters.add(MangaChapter(manga, manga.id + "/content"))
        return chapters.toTypedArray()
    }

    // Function to get array of pages in specific manga, specific chapter by their ids(names)
    // MAY THROW MangaJetException
    override fun getChapterInfo(mangaID: String, chapterID: String) : String {
        val url = getURL() + "/" + chapterID
        var text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here
        val res = ArrayList<String>()

        var f = text.indexOf("contentMargin")
        if(f == -1)
            return JSONArray(res).toString()
        f = text.indexOf("<table>", f)
        var s = text.indexOf("</table>", f)
        val subtext = text.substring(f, s)

        f = subtext.indexOf("href=\"") + "href=\"".length
        while (f != -1 + "href=\"".length) {
            s = subtext.indexOf("\"", f)
            val pageUrl = subtext.substring(f, s)
            // New request
            text = WebAccessor.getTextSync(pageUrl, headers)// Exception may be thrown here
            var v = text.indexOf("id=\"mainImage\"")
            v = text.indexOf("src=\"", v) + "src=\"".length
            val w = text.indexOf("\"", v)
            res.add(getURL() + text.substring(v, w))
            f = subtext.indexOf("href=\"", f) + "href=\"".length
        }
        return JSONArray(res).toString()
    }

}
