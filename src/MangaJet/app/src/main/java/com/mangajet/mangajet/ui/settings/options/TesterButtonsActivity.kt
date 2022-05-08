package com.mangajet.mangajet.ui.settings.options

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.Librarian
import com.mangajet.mangajet.data.MangaJetException
import java.lang.NullPointerException

// Activity with some buttons which will be avaliable only in develop build
class TesterButtonsActivity : AppCompatActivity() {
    companion object {
        const val GEN_SMALL_HISTORY = 0; // Button on generating small history
        const val GEN_BIG_HISTORY = 1;   // Button on generating big history
        const val GEN_HUGE_HISTORY = 2;  // Button on generating very big history
        const val CRASH_APP = 3;         // Button on crash app
    }

    // names of every button
    val buttonNames = listOf(
        "Generate small history",
        "Generate big history",
        "Generate fucking huge history",
        "Crash app")

    // Function for generating history based on search words
    private fun genHistory(words : ArrayList<String>) {
        for (word in words) {
            val res = Librarian.getLibrary(Librarian.LibraryName.Mangalib)!!.searchManga(word)
            for (i in 0 until res.size) {
                try {
                    res[i].updateInfo()
                    res[i].saveToFile()
                } catch (ex: MangaJetException) {
                    println(ex.message)
                }
            }
        }
        Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
    }

    // Function for crashing app
    // Maybe I came up with something more interesting later
    private fun crashApp() {
        val arr = arrayOf("")
        arr[-1]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tester_buttons)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            buttonNames
        )

        val buttonsList = findViewById<ListView>(R.id.testOptionsList)
        buttonsList.adapter = adapter
        buttonsList.setOnItemClickListener { parent, view, position, id ->
            when (id.toInt()) {
                GEN_SMALL_HISTORY -> genHistory(arrayListOf(
                    "Наруто"))
                GEN_BIG_HISTORY -> genHistory(arrayListOf(
                    "Наруто",
                    "Учитель",
                    "Гуль"))
                GEN_HUGE_HISTORY -> genHistory(arrayListOf(
                    "Наруто",
                    "Учитель",
                    "Гуль",
                    "Герой",
                    "Переро",
                    "Блич",
                    "Человек"))
                CRASH_APP -> crashApp()
            }
        }

    }
}
