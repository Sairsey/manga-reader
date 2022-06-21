package com.mangajet.mangajet.ui.settings.options

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.mangajet.mangajet.R
import com.mangajet.mangajet.data.Settings
import com.mangajet.mangajet.log.Logger

class ExtraSettings : AppCompatActivity() {
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
        val searchResTV = findViewById<SeekBar>(R.id.searchMaxResultsValue)
        val recommendedTagsTV = findViewById<SeekBar>(R.id.forYouMaxTagsValue)
        val recommendedResTV = findViewById<SeekBar>(R.id.forYouMaxResultsValue)
        val searchResTextInfo = findViewById<TextView>(R.id.searchMaxResultsNumber)
        val recommendedTagsTextInfo = findViewById<TextView>(R.id.forYouMaxTagsNumber)
        val recommendedResTextInfo = findViewById<TextView>(R.id.forYouMaxResultsNumber)
        val nsfwSwitch = findViewById<Switch>(R.id.nsfwSwitch)

        // set first values
        searchResTV.progress = Settings.MANGA_SEARCH_AMOUNT
        recommendedTagsTV.progress = Settings.AMOUNT_OF_TAGS_IN_RECOMMENDATIONS
        recommendedResTV.progress = Settings.AMOUNT_OF_MANGAS_IN_RECOMMENDATIONS
        searchResTextInfo.text = Settings.MANGA_SEARCH_AMOUNT.toString()
        recommendedTagsTextInfo.text = Settings.AMOUNT_OF_TAGS_IN_RECOMMENDATIONS.toString()
        recommendedResTextInfo.text = Settings.AMOUNT_OF_MANGAS_IN_RECOMMENDATIONS.toString()
        nsfwSwitch.isChecked = Settings.IS_NSFW_ENABLED

        // set listeners
        searchResTV.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                searchResTextInfo.text = p0!!.progress.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                p0.hashCode()
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                val pos = p0!!.progress
                if (pos != Settings.MANGA_SEARCH_AMOUNT) {
                    Settings.MANGA_SEARCH_AMOUNT = pos
                    Settings.saveState()
                }
            }

        })

        recommendedTagsTV.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                recommendedTagsTextInfo.text = p0!!.progress.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                p0.hashCode()
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                val pos = p0!!.progress
                if (pos != Settings.AMOUNT_OF_TAGS_IN_RECOMMENDATIONS) {
                    Settings.AMOUNT_OF_TAGS_IN_RECOMMENDATIONS = pos
                    Settings.saveState()
                }
            }
        })

        recommendedResTV.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                recommendedResTextInfo.text = p0!!.progress.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                p0.hashCode()
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                val pos = p0!!.progress
                if (pos != Settings.AMOUNT_OF_MANGAS_IN_RECOMMENDATIONS) {
                    Settings.AMOUNT_OF_MANGAS_IN_RECOMMENDATIONS = pos
                    Settings.saveState()
                }
            }
        })

        nsfwSwitch.setOnCheckedChangeListener{ buttonView, isChecked ->
            Settings.IS_NSFW_ENABLED = isChecked
            Settings.saveState()
        }
    }
}
