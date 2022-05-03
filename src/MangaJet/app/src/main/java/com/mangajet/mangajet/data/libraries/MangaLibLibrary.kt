package com.mangajet.mangajet.data.libraries

import android.os.Build
import android.text.Html
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

    // Map to transform genres for url
    private val genreMap = mapOf(
        "арт" to "32",
        "безумие" to "91",
        "боевик" to "34",
        "боевые искусства" to "35",
        "вампиры" to "36",
        "военное" to "89",
        "гарем" to "37",
        "гендерная интрига" to "38",
        "героическое фэнтези" to "39",
        "демоны" to "81",
        "детектив" to "40",
        "детское" to "88",
        "дзёсэй" to "41",
        "драма" to "43",
        "игра" to "44",
        "исекай" to "79",
        "история" to "45",
        "киберпанк" to "46",
        "кодомо" to "76",
        "комедия" to "47",
        "космос" to "83",
        "магия" to "85",
        "махо-сёдзё" to "48",
        "машины" to "90",
        "меха" to "49",
        "мистика" to "50",
        "музыка" to "80",
        "научная фантастика" to "51",
        "пародия" to "86",
        "повседневность" to "52",
        "полиция" to "82",
        "постапокалиптика" to "53",
        "приключения" to "54",
        "психология" to "55",
        "романтика" to "56",
        "самурайский боевик" to "57",
        "сверхъестественное" to "58",
        "сёдзё" to "59",
        "сёдзё-ай" to "60",
        "сёнэн" to "61",
        "спорт" to "63",
        "супер сила" to "87",
        "сэйнэн" to "64",
        "трагедия" to "65",
        "триллер" to "66",
        "ужасы" to "67",
        "фантастика" to "68",
        "фэнтези" to "69",
        "школа" to "70",
        "эротика" to "71",
        "этти" to "72",
        "юри" to "73"
    )

    // Map to transform tags for url
    private val tagMap = mapOf(
        "Азартные игры" to "304",
        "Алхимия" to "225",
        "Амнезия / Потеря памяти" to "347",
        "Ангелы" to "226",
        "Антигерои" to "175",
        "Антиутопия" to "227",
        "Апокалипсис" to "228",
        "Армия" to "229",
        "Артефакты" to "230",
        "Боги" to "215",
        "Бои на мечах" to "231",
        "Борьба за власть" to "232",
        "Брат и сестра" to "41",
        "Будущее" to "234",
        "Ведьма" to "338",
        "Вестерн" to "235",
        "Видеоигры" to "185",
        "Виртуальная реальность" to "195",
        "Владыка демонов" to "236",
        "Военные" to "179",
        "Война" to "237",
        "Волшебники / маги" to "281",
        "Волшебные существа" to "239",
        "Воспоминания из другого мира" to "240",
        "Выживание" to "193",
        "ГГ женщина" to "243",
        "ГГ имба" to "291",
        "ГГ мужчина" to "244",
        "Геймеры" to "241",
        "Гильдии" to "242",
        "Глупый ГГ" to "297",
        "Гоблины" to "245",
        "Горничные" to "169",
        "Гяру" to "178",
        "Демоны" to "151",
        "Драконы" to "246",
        "Дружба" to "247",
        "Жестокий мир" to "249",
        "Животные компаньоны" to "250",
        "Завоевание мира" to "251",
        "Зверолюди" to "162",
        "Злые духи" to "252",
        "Зомби" to "149",
        "Игровые элементы" to "253",
        "Империи" to "254",
        "Квесты" to "255",
        "Космос" to "256",
        "Кулинария" to "152",
        "Культивация" to "160",
        "ЛГБТ" to "342",
        "Легендарное оружие" to "257",
        "Лоли" to "187",
        "Магическая академия" to "258",
        "Магия" to "168",
        "Мафия" to "172",
        "Медицина" to "153",
        "Месть" to "259",
        "Монстродевушки" to "188",
        "Монстры" to "189",
        "Музыка" to "357",
        "Навыки / способности" to "260",
        "Насилие / жестокость" to "262",
        "Наёмники" to "261",
        "Нежить" to "263",
        "Ниндзя" to "180",
        "Обмен телами" to "346",
        "Обратный Гарем" to "191",
        "Огнестрельное оружие" to "264",
        "Офисные Работники" to "181",
        "Пародия" to "265",
        "Пираты" to "340",
        "Подземелья" to "266",
        "Политика" to "267",
        "Полиция" to "182",
        "Преступники / Криминал" to "186",
        "Призраки / Духи" to "177",
        "Путешествие во времени" to "194",
        "Рабы" to "354",
        "Разумные расы" to "268",
        "Ранги силы" to "248",
        "Реинкарнация" to "148",
        "Роботы" to "269",
        "Рыцари" to "270",
        "Самураи" to "183",
        "Система" to "271",
        "Скрытие личности" to "273",
        "Спасение мира" to "274",
        "Спортивное тело" to "334",
        "Средневековье" to "173",
        "Стимпанк" to "272",
        "Супергерои" to "275",
        "Традиционные игры" to "184",
        "Умный ГГ" to "302",
        "Учитель / ученик" to "276",
        "Философия" to "277",
        "Хикикомори" to "166",
        "Холодное оружие" to "278",
        "Шантаж" to "279",
        "Эльфы" to "216",
        "Якудза" to "164",
        "Япония" to "280"
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

    // Function to get array of Manga classes by tags, amount of mangas(optional)
    // and offset from start(optional)
    // MAY THROW MangaJetException
    override fun searchMangaByTags(tags: Array<String>, amount: Int, offset: Int) : Array<Manga>{
        var search = ""
        for (i in 0 until tags.size) {
            if(genreMap.containsKey(tags[i]))
                search += "genres[include][]=" + genreMap[tags[i]] + "&"
            else if(tagMap.containsKey(tags[i]))
                search += "tags[include][]=" + tagMap[tags[i]] + "&"
        }
        val url = getURL() + "/manga-list?" + search
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
        var branchID = "null"
        if(query.length() != 0)
            branchID = query.getJSONObject(0).getString("branch_id")
        for (i in 0 until query.length()) {
            if (branchID != query.getJSONObject(i).getString("branch_id"))
                continue
            var id = manga.id + "/v" + query.getJSONObject(i).getString("chapter_volume") + '/'
            id += 'c' + query.getJSONObject(i).getString("chapter_number")
            if(branchID != "null")
                id += "?bid=" + branchID
            var name = query.getJSONObject(i).getString("chapter_name")
            var volume = query.getJSONObject(i).getString("chapter_volume")
            var number = query.getJSONObject(i).getString("chapter_number")
            if(name.equals("null"))
                name = ""
            println(id)
            chapters.add(MangaChapter(manga, id, transformFromHtml(name),
                "Том " + volume + " Глава " + number + " - " + transformFromHtml(name)))
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
        var f = text.indexOf("window.__pg = ")
        if (f == -1)
            return "[]"
        f += "window.__pg = ".length
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
