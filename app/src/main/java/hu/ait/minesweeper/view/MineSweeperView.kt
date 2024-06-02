package hu.ait.minesweeper.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import hu.ait.minesweeper.MainActivity
import hu.ait.minesweeper.R
import hu.ait.minesweeper.model.Field
import hu.ait.minesweeper.model.GameState
import hu.ait.minesweeper.model.MineSweeperModel
import hu.ait.minesweeper.model.MineSweeperModel.gameData

class MineSweeperView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private lateinit var paintText: Paint
    private lateinit var paintLine: Paint
    private lateinit var paintBackground: Paint
    private val bitmapFlag: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.flag)
    private val bitmapBomb: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.bomb)
    private var flagsUsed: Int = 0


    // Colors for use in writing the numbers to the screen
    private var colors: Array<Int> = arrayOf(
        Color.parseColor("#3DED97"),
        Color.parseColor("#FD8B8B"),
        Color.parseColor("#DF005F"),
        Color.parseColor("#E0002B"),
        Color.parseColor("#C50000")
    )

    init {
        paintText = Paint()
        paintText.textSize = 100f

        paintLine = Paint()
        paintLine.color = Color.parseColor("#725395")
        paintLine.style = Paint.Style.STROKE
        paintLine.strokeWidth = 8f

        paintBackground = Paint()
        paintBackground.color = Color.parseColor("#1b1d3d")
        paintBackground.style = Paint.Style.FILL

        // initialize the minesweeper model
        MineSweeperModel.initGame(gameData.gridSize, false)
    }

    /**
     * Need to update the font size when the view is rendered thus overriding
     * this method.
     * */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        paintText.textSize = height / 18f
    }

    /**
     *  Draw the view to screen, draws the text and game grid
     *  @param canvas - the canvas used for drawing
     * */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawGrid(canvas)
        drawGameText(canvas)

        // Game over, and loss draw the mines
        if (MineSweeperModel.gameState == GameState.LOSS) {
            drawMinesOrFlags(canvas)
        } else if (MineSweeperModel.gameState == GameState.WIN) {
            drawMinesOrFlags(canvas, false)
        }
    }

    /**
     *  Touch screen game logic
    * */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // should not be touchable if game is not going
        if (MineSweeperModel.gameState != GameState.ONGOING) return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            // Get the row and col from the touch coordinates
            val touchX = event.x.toInt() / (width / gameData.gridSize)
            val touchY = event.y.toInt() / (height / gameData.gridSize)

            if (touchX < gameData.gridSize && touchY < gameData.gridSize) {
                // grab the grid item
                val field: Field = MineSweeperModel.getFieldContent(touchX, touchY)

                // Flag mode
                if ((context as MainActivity).isFlagMode) {
                    // Field is not mine end game
                    if (field.type == MineSweeperModel.EMPTY) {
                        // End game
                        MineSweeperModel.gameState = GameState.LOSS
                        (context as MainActivity).showEndGameMessage("You lose - Flagged an empty cell")
                        // stop timer
                        (context as MainActivity).resetChronometer()
                    } else {
                        // Draw the flag
                        field.isFlagged = true
                        field.wasClicked = true
                        flagsUsed += 1
                        (context as MainActivity).updateFlagsRemaining(flagsUsed)
                    }
                    // Draw the flag
                } else {
                    // Field is mine end game
                    if (field.type == MineSweeperModel.MINE) {
                        // End game
                        MineSweeperModel.gameState = GameState.LOSS
                        (context as MainActivity).showEndGameMessage("You dead - Stepped on a mine")
                        // stop timer
                        (context as MainActivity).resetChronometer()
                    } else if (!field.wasClicked) {
                        // Update the clicked attribute
                        MineSweeperModel.revealCelL(touchX, touchY)
                    }
                }
            }
            // Check if the player won
            if (MineSweeperModel.didPlayerWin()) {
                MineSweeperModel.gameState = GameState.WIN
                (context as MainActivity).showEndGameMessage("Congratulations - you won")
                // stop timer
                (context as MainActivity).resetChronometer()
            }
        }
        invalidate()
        return true
    }

    public fun resetGame() {
        // Reset the matrix and compute game positions
        MineSweeperModel.initGame(gameData.gridSize, randomize = true)
        // set the flags used back to zero
        flagsUsed = 0
        invalidate()
    }

    /**
     *  Draws the game grid to the screen
     *  @param canvas - the canvas object used in drawing
     * */
    private fun drawGrid(canvas: Canvas) {
        // Draw the outer rectangle, background is dark purple
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paintBackground)

        // draw cell backgrounds, gray if mines around = 0 green otherwise
        for (i in 0 until gameData.gridSize) {
            for (j in 0 until gameData.gridSize) {
                drawCellBackground(i, j, canvas)
            }
        }

        // Draw horizontal lines
        for (i in 1 until gameData.gridSize) {
            val yPos = i * height.toFloat() / gameData.gridSize
            canvas.drawLine(0f, yPos, width.toFloat(), yPos, paintLine)
        }

        // Draw vertical lines
        for (i in 1 until gameData.gridSize) {
            val xPos = i * width.toFloat() / gameData.gridSize
            canvas.drawLine(xPos, 0f, xPos, height.toFloat(), paintLine)
        }
    }

    /**
     *  Draws the game text and flag to the screen
     *  @param canvas - the canvas object used in drawing
     * */
    private fun drawGameText(canvas: Canvas) {
        for (i in 0 until gameData.gridSize) {
            for (j in 0 until gameData.gridSize) {
                val field: Field = MineSweeperModel.getFieldContent(i, j)

                // Draw the flag if the cell is flagged
                if (field.isFlagged) {
                    val xPos = (i * width.toFloat() / gameData.gridSize).toInt()
                    val yPos = (j * height.toFloat() / gameData.gridSize).toInt()
                    drawImage(canvas, bitmapFlag, xPos, yPos)

                // Draw the number at the center of the cell
                } else if (field.wasClicked && field.type == MineSweeperModel.EMPTY) {
                    val text = field.minesAround.toString()

                    // Only draw numbers if minesAround > 0
                    if (field.minesAround > 0) {
                        drawNumber(i, j, text, canvas)
                    }

                }
            }
        }
    }

    /**
     *  Draws the game number on the given cell
     *  @param canvas - the canvas object used in drawing
     *  @param x - the top left cell x coordinate
     *  @param y - the top left cell y coordinate
     *  @param text - the number to draw to the screen
     * */
    private fun drawNumber(x: Int, y: Int, text: String, canvas: Canvas) {

        // Grab the size of the text, for use in centering it
        val textBounds = Rect()
        paintText.getTextBounds(text, 0, text.length, textBounds)

        // center the text within the cell
        val centerX = (x * width / gameData.gridSize) + (width / gameData.gridSize - textBounds.width()) / 2f
        val centerY = (y * height / gameData.gridSize) + (height / gameData.gridSize + textBounds.height()) / 2f - textBounds.bottom

        // Custom coloring for each number, the darker the shade of red as you have more mines
        paintText.color = colors[text.toInt()]
        canvas.drawText(text, centerX, centerY, paintText)
    }

    /**
     *  Add cell background on the given cell, gray if there is no mines around and not revealed,
     *  purple if its unrevealed and green if revealed with mines around
     *  @param canvas - the canvas object used in drawing
     * */
    private fun drawCellBackground(x: Int, y: Int, canvas: Canvas) {
        val field: Field = MineSweeperModel.getFieldContent(x, y)
        val paint = Paint()

        // Default color (for unrevealed cells)
        paint.color = Color.parseColor("#1b1d3d") // Purple

        if (field.wasClicked) {
            if (field.type == MineSweeperModel.EMPTY && field.minesAround == 0) {
                // Gray for cells without bombs around them and was clicked
                paint.color = Color.parseColor("#B0B1BA")
            } else if (field.minesAround > 0) {
                // Green for revealed cells next to bombs
                paint.color = Color.parseColor("#91A18D")
            }
        }

        // Calculate the position and size for the cell's rectangle
        val xPos = (x * width / gameData.gridSize).toFloat()
        val yPos = (y * height / gameData.gridSize).toFloat()
        val rectSize = width / gameData.gridSize.toFloat()

        // Draw the rectangle with the determined paint color
        canvas.drawRect(xPos, yPos, xPos + rectSize, yPos + rectSize, paint)
    }

    /**
     *  Draws the game number on the given cell
     *  @param canvas - the canvas object used in drawing
     *  @param left - the top left cell x coordinate
     *  @param top - the top left cell y coordinate
     * */
    private fun drawImage(canvas: Canvas, img: Bitmap, left: Int, top: Int) {
        // The width and height of the cells
        val cellWidth = width / gameData.gridSize
        val cellHeight = height / gameData.gridSize

        // Draw image at the given rectangle
        val destRect = Rect(left, top, left + cellWidth, top + cellHeight)
        canvas.drawBitmap(img, null, destRect, null)
    }

    private fun drawMinesOrFlags(canvas: Canvas, mine: Boolean=true) {
        for (i in 0 until gameData.gridSize) {
            for (j in 0 until gameData.gridSize) {
                val field: Field = MineSweeperModel.getFieldContent(i, j)
                if (field.type == MineSweeperModel.MINE) {
                    val xPos = (i * width.toFloat() / gameData.gridSize).toInt()
                    val yPos = (j * height.toFloat() / gameData.gridSize).toInt()
                    val img: Bitmap = if (mine) bitmapBomb else bitmapFlag
                    drawImage(canvas, img, xPos, yPos)
                }
            }
        }
    }



}
