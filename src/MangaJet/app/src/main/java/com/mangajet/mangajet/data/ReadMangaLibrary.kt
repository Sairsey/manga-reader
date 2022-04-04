package com.mangajet.mangajet.data

import android.os.Build
import android.text.Html
import androidx.core.text.isDigitsOnly
import org.json.JSONArray
import org.json.JSONObject

class ReadMangaLibrary(uniqueID: String) : AbstractLibrary(uniqueID) {

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
    override fun searchManga(id: String, amount: Int, offset: Int): Array<Manga> {
        val url = getURL() + "/search/suggestion?query=" + id
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        var f = text.indexOf("\"link\":\"") + "\"link\":\"".length

        val res = ArrayList<Manga>()

        var index = 0
        while (f - "\"link\":\"".length != - 1) {
            if(text.indexOf("list", f) != -1) {
                f = text.indexOf("\"link\":\"", f) + "\"link\":\"".length
                continue
            }

            val s = text.indexOf("\"", f)
            if (index >= offset + amount)
                break

            if (index >= offset)
                res.add(Manga(this, text.subSequence(f, s).toString()))
            f = text.indexOf("\"link\":\"", f) + "\"link\":\"".length
            index++
        }

        return res.toTypedArray()
    }

    // Helper class for some functions
    private class ReadMangaLibraryHelper {
        // Retrieve title image URL
        fun getTitleImageURL(text : String) : String {
            var f = text.indexOf("fotorama")
            f = text.indexOf("src=\"", f) + "src=\"".length
            val s = text.indexOf("\"", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve name of manga
        fun getName(text : String) : String {
            var f = text.indexOf("английское название\">") + "английское название\">".length
            if(f - "английское название\">".length == -1)
                return ""
            val s = text.indexOf("</span>", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve rating of manga
        fun getRating(text : String) : String {
            var f = text.indexOf("data-score=\"") + "data-score=\"".length
            val s = text.indexOf("\"", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve russian name of manga
        fun getRusName(text : String) : String {
            var f = text.indexOf("name'>") + "name'>".length
            val s = text.indexOf("</span>", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve description
        fun getDescr(text : String) : String {
            var f = text.indexOf("itemprop=\"description\"")
            f = text.indexOf("content=", f) + "content=".length + 1
            val s = text.indexOf("\"", f + 1)
            val description = text.subSequence(f, s).toString()
            return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY).toString()
            else
                Html.fromHtml(description).toString()
        }

        // Retrieve author
        fun getAuthor(text : String) : String {
            var f = text.indexOf("class=\"person-link\">") + "class=\"person-link\">".length
            val s = text.indexOf("<", f)
            return text.subSequence(f, s).toString()
        }

        // Retrieve tags
        fun getTags(text : String) : Array<String> {
            val res = ArrayList<String>()
            var f = text.indexOf("elem_genre")
            while (f != -1) {
                f = text.indexOf("<a", f)
                f = text.indexOf(">", f) + 1
                val s = text.indexOf("<", f)
                res.add(text.substring(f, s))
                f = text.indexOf("elem_genre", f)
            }
            return res.toTypedArray()
        }
    }

    // Function to get info(name, author, genre, number of chapters...) about manga as JSON by its id(name)
    // MAY THROW MangaJetException
    override fun getMangaInfo(id: String) : String {
        val url = getURL() + "/" + id
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        val json = JSONObject()
        json.put("name", ReadMangaLibraryHelper().getName(text))
        json.put("cover", ReadMangaLibraryHelper().getTitleImageURL(text))
        json.put("rus_name", ReadMangaLibraryHelper().getRusName(text))
        json.put("rating", ReadMangaLibraryHelper().getRating(text))
        json.put("author", ReadMangaLibraryHelper().getAuthor(text))
        json.put("description", ReadMangaLibraryHelper().getDescr(text))
        val tagArray = JSONArray(ReadMangaLibraryHelper().getTags(text))
        json.put("tags", tagArray)
        return json.toString()
    }

    // Function to get array of MangaChapter classes by manga's id(name)
    // MAY THROW MangaJetException
    override fun getMangaChapters(manga: Manga) : Array<MangaChapter> {
        val url = getURL() + "/" + manga.id
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here

        val chapters = ArrayList<MangaChapter>()

        var f = text.indexOf("item-title")
        while (f != -1) {
            f = text.indexOf("<a href=\"", f) + "<a href=\"".length + 1
            var s = text.indexOf("\"", f)
            val id = text.substring(f, s)
            f = text.indexOf(">", s) + 1
            s = text.indexOf("<", f)
            var name = text.substring(f, s).trim()
            val nameSplit = name.split(' ')
            // Stupid way to get chapter title
            if(nameSplit.size == 2 && nameSplit[0].isDigitsOnly())
                name = nameSplit[1]
            else if(nameSplit[0].isDigitsOnly() && nameSplit[1] != "-") {
                var res = ""
                for(i in 1 until nameSplit.size)
                    res += nameSplit[i] + " "
                name = res
            }
            else{
                var res = ""
                for(i in 1 + 1 + 1 until nameSplit.size)
                    res += nameSplit[i] + " "
                name = res
            }
            name = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                Html.fromHtml(name, Html.FROM_HTML_MODE_LEGACY).toString()
            else
                Html.fromHtml(name).toString()
            chapters.add(MangaChapter(manga, id, name))
            f = text.indexOf("item-title", f)
        }
        chapters.reverse()
        if (chapters.size == 0)
            chapters.add(MangaChapter(manga, manga.id))
        return chapters.toTypedArray()
    }

    // Function to get array of pages in specific manga, specific chapter by their ids(names)
    // MAY THROW MangaJetException
    override fun getChapterInfo(mangaID: String, chapterID: String) : String {
        val url = getURL() + "/" + chapterID
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here
        val res = ArrayList<String>()

        var f = text.indexOf("initReader")
        f = text.indexOf("[[", f) + 1
        var s = text.indexOf("false", f)
        val subtext = text.substring(f, s)

        f = subtext.indexOf('[')
        while (f != -1) {
            f = subtext.indexOf("'", f) + 1
            s = subtext.indexOf("'", f)
            val imgBegin = subtext.substring(f, s) + '/'
            f = subtext.indexOf("\"", s) + 1
            s = subtext.indexOf("\"", f)
            res.add(imgBegin + subtext.substring(f, s))
            f = subtext.indexOf('[', f)
        }
        return JSONArray(res).toString()
    }

}
