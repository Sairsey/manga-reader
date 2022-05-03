package com.mangajet.mangajet.data.libraries

import android.os.Build
import android.text.Html
import androidx.core.text.isDigitsOnly
import com.mangajet.mangajet.data.Manga
import com.mangajet.mangajet.data.MangaChapter
import com.mangajet.mangajet.data.WebAccessor
import org.json.JSONArray
import org.json.JSONObject

class ReadMangaLibrary(uniqueID: String) : AbstractLibrary(uniqueID) {

    val headers = mutableMapOf(
        "user-agent" to "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko)" +
                "Chrome/41.0.2228.0 Safari/537.36",
        "accept" to "*/*")

    // Map to transform tags for url
    private val tagsMap = mapOf(
        "боевик" to "action",
        "боевые искусства" to "martial_arts",
        "гарем" to "harem",
        "гендерная интрига" to "gender_intriga",
        "героическое фэнтези" to "heroic_fantasy",
        "детектив" to "detective",
        "дзёсэй" to "josei",
        "драма" to "drama",
        "игра" to "game",
        "история" to "historical",
        "исэкай" to "isekai",
        "киберпанк" to "cyberpunk",
        "кодомо" to "codomo",
        "комедия" to "comedy",
        "махо-сёдзё" to "maho_shoujo",
        "меха" to "mecha",
        "научная фантастика" to "sci_fi",
        "повседневность" to "slice_of_life",
        "постапокалиптика" to "postapocalypse",
        "приключения" to "adventure",
        "психология" to "psychological",
        "романтика" to "romance",
        "самурайский боевик" to "samurai",
        "сверхъестественное" to "supernatural",
        "сёдзё" to "shoujo",
        "сёдзё-ай" to "shoujo_ai",
        "сёнэн" to "shounen",
        "сёнэн-ай" to "shounen_ai",
        "спорт" to "sports",
        "сэйнэн" to "seinen",
        "сянься" to "xianxia",
        "трагедия" to "tragedy",
        "триллер" to "thriller",
        "ужасы" to "horror",
        "уся" to "wuxia",
        "фэнтези" to "fantasy",
        "школа" to "school",
        "этти" to "ecchi",
        "юри" to "yuri"
    )

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

        val json = JSONObject(text)
        val query = json.getJSONArray("suggestions")

        val res = ArrayList<Manga>()

        var index = 0
        for (i in 0 until query.length())
        {
            var link = query.getJSONObject(i).getString("link")

            if (link.contains("/person/") || link.contains("/tag/") || link.contains("/gournal/"))
                continue
            if(link.contains("/year/") || link.contains("/librebook/") || link.contains("/mose.live/"))
                continue
            if (index >= offset + amount)
                break
            if (index >= offset)
                res.add(Manga(this, link))
            index++
        }

        return res.toTypedArray()
    }

    // Function to get array of Manga classes by tags, amount of mangas(optional)
    // and offset from start(optional)
    // MAY THROW MangaJetException
    override fun searchMangaByTags(tags: Array<String>, amount: Int, offset: Int) : Array<Manga>{
        val tag = tags[0]// Sorry, ReadManga does not support multiple tags
        val url = getURL() + "/list/genre/" + tagsMap.getOrDefault(tag,"action")
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here
        var f = text.indexOf("tile col-md-6")

        val res = ArrayList<Manga>()

        var index = 0
        while (f != -1) {
            f = text.indexOf("a href=\"", f) + "a href=\"".length
            val s = text.indexOf("\"", f)
            if (index >= offset + amount)
                break

            if (index >= offset)
                res.add(Manga(this, text.subSequence(f, s).toString()))
            f = text.indexOf("tile col-md-6", f)
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
                return getRusName(text)
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
            val f = text.indexOf("\"description\" content=") + "\"description\" content=".length + 1
            val s = text.indexOf("\"", f + 1)
            return text.subSequence(f, s).toString()
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
        json.put("name", transformFromHtml(ReadMangaLibraryHelper().getName(text)))
        json.put("cover", transformFromHtml(ReadMangaLibraryHelper().getTitleImageURL(text)))
        json.put("rus_name", transformFromHtml(ReadMangaLibraryHelper().getRusName(text)))
        json.put("rating", transformFromHtml(ReadMangaLibraryHelper().getRating(text)))
        json.put("author", transformFromHtml(ReadMangaLibraryHelper().getAuthor(text)))
        json.put("description", transformFromHtml(ReadMangaLibraryHelper().getDescr(text)))
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
            var fullname = name
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
            chapters.add(MangaChapter(manga, id, transformFromHtml(name),  transformFromHtml(fullname)))
            f = text.indexOf("item-title", f)
        }
        chapters.reverse()
        return chapters.toTypedArray()
    }

    // Function to get array of pages in specific manga, specific chapter by their ids(names)
    // MAY THROW MangaJetException
    override fun getChapterInfo(mangaID: String, chapterID: String) : String {
        val url = getURL() + "/" + chapterID + "?mtr=1"
        val text = WebAccessor.getTextSync(url, headers) // Exception may be thrown here
        val res = ArrayList<String>()

        var f = text.indexOf("initReader")
        f = text.indexOf("[[", f)

        var s = text.indexOf("]]", f) + 2
        val subtext = text.substring(f, s)

        val json = JSONArray(subtext)

        for (i in 0 until json.length())
        {
            val subjson = json.getJSONArray(i)
            val link = subjson.getString(0) + subjson.getString(1) + subjson.getString(2)
            res.add(link)
        }
        return JSONArray(res).toString()
    }

}
