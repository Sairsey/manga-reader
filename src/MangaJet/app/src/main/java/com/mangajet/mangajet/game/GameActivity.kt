package com.mangajet.mangajet.game

import android.graphics.Bitmap
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.mangajet.mangajet.R
import com.mangajet.mangajet.log.Logger
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.random.Random

class GameActivity : AppCompatActivity(), View.OnClickListener {

    enum class GameStates{
        AWAIT,
        PLAYING
    }

    private val tilesNum = "8".toInt()
    private var emptyX = 2
    private var emptyY = 2
    private var gameState = GameStates.AWAIT
    private lateinit var group : ViewGroup
    private lateinit var buttons : Array<Array<ImageButton?>>
    private lateinit var tiles : IntArray
    private lateinit var posMatrix : Array<IntArray>
    private lateinit var bitmaps: ArrayList<Bitmap>

    // Function for splitting bitmap
    private fun splitBitmap(bitmap: Bitmap, xCount: Int, yCount: Int): ArrayList<Bitmap> {
        val bitmaps = arrayListOf<Bitmap>()
        val width: Int = bitmap.width / xCount
        val height: Int = bitmap.height / yCount
        for (y in 0 until xCount)
            for (x in 0 until yCount)
                bitmaps.add(Bitmap.createBitmap(bitmap, x * width, y * height, width, height))

        return bitmaps
    }

    // Function to shuffle board
    private fun shuffle(){
        var n = tilesNum
        while(n > 1){
            var randomNum = Random.nextInt(n--)
            var tmp = tiles[randomNum]
            tiles[randomNum] = tiles[n]
            tiles[n] = tmp
        }
        if(!isSolvable())
            shuffle()
    }

    // Function to set board after shuffle
    private fun setBoard(){
        emptyX = 2
        emptyY = 2
        for(i in 0 until group.childCount - 1){
            buttons[i / (emptyX + 1)][i % ((emptyX + 1))]?.setImageBitmap(bitmaps[tiles[i] - 1])
            buttons[i / (emptyX + 1)][i % ((emptyX + 1))]?.scaleType = ImageView.ScaleType.FIT_XY

            posMatrix[i / (emptyX + 1)][i % ((emptyX + 1))] = ((tiles[i] - 1) / (emptyX + 1)) *
                    "10".toInt() +  ((tiles[i] - 1) % (emptyX + 1))
        }

        buttons[emptyX][emptyY]?.alpha = 0F

    }

    // Function to check if can solve this puzzle
    private fun isSolvable(): Boolean {
        var inversions = 0
        for(i in 0 until tilesNum){
            var j = 0
            while(j < i)
                if(tiles[j++] > tiles[i])
                    inversions++
        }
        return inversions%2 == 0
    }

    // Function to check if puzzle is solved
    private fun checkWin(): Boolean {
        if(emptyY == 2 && emptyX == 2) {
            var tmp = 0
            for(i in posMatrix.indices)
                for (j in 0 until posMatrix[i].size)
                    if (posMatrix[i][j] < tmp)
                        return false
                    else
                        tmp = posMatrix[i][j]
        }
        else
            return false
        return true

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.log("Game activity opened")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Init
        group = findViewById<ViewGroup>(R.id.group)
        buttons = Array(emptyX + 1) {
            arrayOfNulls<ImageButton>(
                emptyY + 1
            )
        }
        posMatrix = Array(emptyX + 1) { IntArray(emptyX + 1)}
            for(i in 0 until group.childCount){
            val child = group.getChildAt(i)
            if (child is ImageButton)
                buttons[i / (emptyX + 1)][i % ((emptyX + 1))] = child
        }
        tiles = IntArray(tilesNum + 1)
        for(i in 0 until group.childCount - 1)
            tiles[i] = i + 1

        // Picture to 9 parts and to buttons
        val bitmap = resources.getDrawable(R.drawable.anime_girl).toBitmap()
        bitmaps = splitBitmap(bitmap, emptyX + 1, emptyY + 1)
        var bitmapCount = 0
        for(i in buttons.indices)
            for(j in 0 until buttons[i].size){
                buttons[i][j]?.setImageBitmap(bitmaps[bitmapCount++])
                buttons[i][j]?.scaleType = ImageView.ScaleType.FIT_XY
                buttons[i][j]?.setOnClickListener(this)
                posMatrix[i][j] = buttons[i][j]?.tag.toString().toInt()
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // Game starts after touch
        if(gameState == GameStates.PLAYING)
            return true
        shuffle()
        setBoard()
        gameState = GameStates.PLAYING
        return true
    }

    override fun onClick(view : View) {
        if(gameState == GameStates.AWAIT){
            shuffle()
            setBoard()
            gameState = GameStates.PLAYING
        }
        else if(gameState == GameStates.PLAYING) {
            val button = view as ImageButton
            val x = button.tag.toString()[0] - '0'
            val y = button.tag.toString()[1] - '0'

            @Suppress("ComplexCondition")
            if((abs(emptyX - x) == 1 && emptyY == y) || (abs(emptyY - y) == 1 && emptyX == x)) {
                buttons[emptyX][emptyY]?.setImageBitmap(button.drawable.toBitmap())
                buttons[emptyX][emptyY]?.alpha = "255".toFloat()
                button.alpha = 0F
                val tmp = posMatrix[emptyX][emptyY]
                posMatrix[emptyX][emptyY] = posMatrix[x][y]
                posMatrix[x][y] = tmp
                emptyX = x
                emptyY = y
                if(checkWin()){
                    buttons[emptyX][emptyY]?.setImageBitmap(bitmaps[bitmaps.size - 1])
                    buttons[emptyX][emptyY]?.scaleType = ImageView.ScaleType.FIT_XY
                    buttons[emptyX][emptyY]?.alpha = "255".toFloat()
                    Toast.makeText(this, "Congratulations!", Toast.LENGTH_SHORT).show()
                    gameState = GameStates.AWAIT
                }
            }
        }

    }
}
