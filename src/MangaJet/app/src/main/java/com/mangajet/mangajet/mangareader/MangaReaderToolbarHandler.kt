package com.mangajet.mangajet.mangareader

import android.animation.ObjectAnimator
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import com.google.android.material.appbar.MaterialToolbar
import com.mangajet.mangajet.MangaJetApp

// Class which will handle actions with toolbars
class MangaReaderToolbarHandler(
    headerToolbar: MaterialToolbar,
    bottomToolbar: MaterialToolbar,
    prevButton: ImageButton,
    nextButton: ImageButton
) {
    companion object {
        // radius of touch, that we can describe as 'single-tap'
        const val SINGLE_TOUCH_RAD = 100

        // value of translate toolbar up
        const val TRANSLATE_MENU_UP = -200F
        // value of translate toolbar down
        const val TRANSLATE_MENU_DOWN = 200F
        // duration of hide/show animation
        const val ANIMATION_DURATION = 750L

        // what part of screen we choose for 'single-tap' area to hide/show toolbars
        const val MIDDLE_SCREEN_SQUARE_PART = 5
    }

    // reference to header toolbar
    private val topMenu = headerToolbar
    // reference to bottom toolbar
    private val bottomMenu = bottomToolbar

    // previous button reference
    private val prevBut = prevButton
    // next button reference
    private val nextBut = nextButton

    // touch down coordinates
    private var xTouch : Float = 0.0F
    private var yTouch : Float = 0.0F
    // is menu hidden right now
    private var isHidden = false

    // Function which will hide toolbars
    private fun hideMenu() {
        ObjectAnimator.ofFloat(topMenu, "translationY", TRANSLATE_MENU_UP).apply {
            duration = ANIMATION_DURATION
            start()
        }

        ObjectAnimator.ofFloat(bottomMenu, "translationY", TRANSLATE_MENU_DOWN).apply {
            duration = ANIMATION_DURATION
            start()
        }
    }

    // Function which will show toolbars
    private fun showMenu() {
        ObjectAnimator.ofFloat(topMenu, "translationY", 0F).apply {
            duration = ANIMATION_DURATION
            start()
        }

        ObjectAnimator.ofFloat(bottomMenu, "translationY", 0F).apply {
            duration = ANIMATION_DURATION
            start()
        }
    }

    // Function which will return True if we make 'single-tap' in correct area
    private fun isInMiddleSquare() : Boolean {
        val width: Int = MangaJetApp.context!!.resources.displayMetrics.widthPixels
        val height: Int = MangaJetApp.context!!.resources.displayMetrics.heightPixels

        return (xTouch >= width / MIDDLE_SCREEN_SQUARE_PART
                && xTouch <= width - width / MIDDLE_SCREEN_SQUARE_PART
                && yTouch >= height / MIDDLE_SCREEN_SQUARE_PART
                && yTouch <= height - height / MIDDLE_SCREEN_SQUARE_PART)
    }

    // Function which will manage actions
    fun touchEventDispatcher(event: MotionEvent?) {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                xTouch = event.x
                yTouch = event.y
            }
            MotionEvent.ACTION_UP -> {
                if ((xTouch - event.x) * (xTouch - event.x)
                    + (yTouch - event.y) * (yTouch - event.y) < SINGLE_TOUCH_RAD
                    && isInMiddleSquare()) {
                    if (isHidden)
                        showMenu()
                    else
                        hideMenu()
                    isHidden = !isHidden
                }
            }
        }
    }

    // Function which will show or hide buttons
    fun manageButtonsShowStatus(isShowenPrev : Boolean, isShowenNext : Boolean) {
        if (isShowenNext)
            nextBut.visibility = View.VISIBLE
        else
            nextBut.visibility = View.INVISIBLE

        if (isShowenPrev)
            prevBut.visibility = View.VISIBLE
        else
            prevBut.visibility = View.INVISIBLE
    }
}
