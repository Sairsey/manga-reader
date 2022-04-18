package com.mangajet.mangajet.mangareader

import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.mangajet.mangajet.MangaJetApp

class MangaLayoutTouchListener : View.OnTouchListener {
    var x = 0.0
    var y = 0.0

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event != null) {
            val localX = event.x
            val localY = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = localX.toDouble()
                    y = localY.toDouble()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if ((x - localX) * (x - localX) + (y - localY) * (y - localY)
                        < MangaReaderActivity.SINGLE_TOUCH_RAD
                    ) {
                        Toast.makeText(MangaJetApp.context, "SINGLE TAP!", Toast.LENGTH_LONG).show()
                    }
                }
            }

        }
        return true
    }
}
