package com.mangajet.mangajet.ui.settings.options

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.google.android.material.appbar.MaterialToolbar
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.Settings
import com.mangajet.mangajet.log.Logger

class ExtraSettings : AppCompatActivity() {
    inner class KeyPressFilter(
        private val editViewId : Int
    ) : View.OnKeyListener {
        override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
            // if the event is a key down event on the enter button
            if (event.action == KeyEvent.ACTION_DOWN &&
                keyCode == KeyEvent.KEYCODE_ENTER)
            {
                var textVal : Int = (v as EditText).text.toString().split(".")
                    .toTypedArray()[0].toInt()
                when (editViewId) {
                    R.id.searchMaxResults -> {
                        if (textVal < Settings.MIN_MANGA_SEARCH_AMOUNT)
                            textVal = Settings.MIN_MANGA_SEARCH_AMOUNT
                        else if (textVal > Settings.MAX_MANGA_SEARCH_AMOUNT)
                            textVal = Settings.MAX_MANGA_SEARCH_AMOUNT

                        Settings.MANGA_SEARCH_AMOUNT = textVal
                        Settings.saveState()
                        (v as EditText).setText(textVal.toString())
                    }
                    R.id.forYouMaxTags -> {
                        if (textVal < Settings.MIN_AMOUNT_OF_TAGS_IN_RECOMMENDATIONS)
                            textVal = Settings.MIN_AMOUNT_OF_TAGS_IN_RECOMMENDATIONS
                        else if (textVal > Settings.MAX_AMOUNT_OF_TAGS_IN_RECOMMENDATIONS)
                            textVal = Settings.MAX_AMOUNT_OF_TAGS_IN_RECOMMENDATIONS

                        Settings.AMOUNT_OF_TAGS_IN_RECOMMENDATIONS = textVal
                        Settings.saveState()
                        (v as EditText).setText(textVal.toString())
                    }
                    R.id.forYouMaxResults -> {
                        if (textVal < Settings.MIN_AMOUNT_OF_MANGAS_IN_RECOMMENDATIONS)
                            textVal = Settings.MIN_MANGA_SEARCH_AMOUNT
                        else if (textVal > Settings.MAX_AMOUNT_OF_MANGAS_IN_RECOMMENDATIONS)
                            textVal = Settings.MAX_AMOUNT_OF_MANGAS_IN_RECOMMENDATIONS

                        Settings.AMOUNT_OF_MANGAS_IN_RECOMMENDATIONS = textVal
                        Settings.saveState()
                        (v as EditText).setText(textVal.toString())
                    }
                }
                return true
            }
            return false
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.log("Extra in Settings opened")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extra_settings)
        setSupportActionBar(findViewById<MaterialToolbar>(R.id.extraToolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get fields
        val searchResTV = findViewById<EditText>(R.id.searchMaxResultsValue)
        val recommendedTagsTV = findViewById<EditText>(R.id.forYouMaxTagsValue)
        val recommendedResTV = findViewById<EditText>(R.id.forYouMaxResultsValue)

        // set first values
        searchResTV.setText(Settings.MANGA_SEARCH_AMOUNT.toString())
        recommendedTagsTV.setText(Settings.AMOUNT_OF_TAGS_IN_RECOMMENDATIONS.toString())
        recommendedResTV.setText(Settings.AMOUNT_OF_MANGAS_IN_RECOMMENDATIONS.toString())

        // set listeners
        searchResTV.setOnKeyListener(KeyPressFilter(R.id.searchMaxResults))
        recommendedTagsTV.setOnKeyListener(KeyPressFilter(R.id.forYouMaxTags))
        recommendedResTV.setOnKeyListener(KeyPressFilter(R.id.forYouMaxResults))
    }
}
