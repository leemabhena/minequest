package hu.ait.minesweeper.model

object MineSweeperModel {

    // Field matrix grid to store the mines
    private lateinit var fieldMatrix: Array<Array<Field>>

    // Game settings
    val easy: GameData = GameData(6, 6, 5)
    val medium: GameData = GameData(6, 8, 7)
    val hard: GameData = GameData(8, 12, 9)

    // Mines are represented using (1)
    var EMPTY: Int = 0
    var MINE: Int = 1

    // SET UP THE GAME STATE
    var gameState: GameState = GameState.STOPPED

    // set the game data
    var gameData: GameData = easy

    /**
     * Initialize the game, by adding the mines and computing the number of mines around each field
     * @param size - the size of the mine sweeper grid
     * @param randomize - whether to add the mines at random positions
     * */
    fun initGame(size: Int, randomize: Boolean = false) {
        // initialize the fieldMatrix with all zeros
        fieldMatrix = Array(size){ Array(size) {Field(EMPTY, 0, false, false)} }

        // Update the field matrix to correctly initialize positions with mines
        minePositions(randomize)

        // update the mines around variable for all cells with mines around
        calculateMinesAround()
    }

    /**
     * Get the field details at given row and col positions
     * @param row - the row or x position
     * @param col - the col or y position
     * @return Returns the field matrix
     * */
    public fun getFieldContent(row: Int, col: Int): Field = fieldMatrix[row][col]

    public fun resetFieldMatrix() {
        for (i in fieldMatrix.indices) {
            for (j in fieldMatrix.indices) {
                val field = getFieldContent(i, j)
                field.wasClicked = false
                field.isFlagged = false
                field.minesAround = 0
                field.type = EMPTY
            }
        }

        // Update the field matrix to correctly initialize positions with mines
        minePositions(randomize = true)

        // update the mines around variable for all cells with mines around
        calculateMinesAround()
    }

    /**
     * Adds mines to the field matrix
     * @param randomize -  whether to add the mines at random positions
     * */
    private fun minePositions(randomize: Boolean = false) {
        if (randomize) {
            var minesPlaced = 0
            while (minesPlaced < gameData.numMines) {
                val randomX = (fieldMatrix.indices).random()
                val randomY = (fieldMatrix.indices).random()

                if (fieldMatrix[randomX][randomY].type == EMPTY) {
                    fieldMatrix[randomX][randomY].type = MINE
                    minesPlaced++
                }
            }
        } else {
            // Predefined mine positions (as an example)
            fieldMatrix[0][0].type = MINE
            fieldMatrix[1][1].type = MINE
            fieldMatrix[2][2].type = MINE
        }
    }

    /**
     * Computes the number of mines around each cell and updates the field matrix
     */
    private fun calculateMinesAround() {
        for (x in fieldMatrix.indices) {
            for (y in fieldMatrix.indices) {
                if (fieldMatrix[x][y].type != MINE) {
                    var minesAround = 0
                    for (i in -1..1) {
                        for (j in -1..1) {
                            if (x + i in fieldMatrix.indices && y + j in fieldMatrix.indices && fieldMatrix[x + i][y + j].type == MINE) {
                                minesAround++
                            }
                        }
                    }
                    fieldMatrix[x][y].minesAround = minesAround
                }
            }
        }
    }

    fun didPlayerWin(): Boolean {
        return allCellsRevealed() || allMinesFlagged()
    }

    private fun allCellsRevealed(): Boolean {
        for (i in fieldMatrix.indices) {
            for (j in fieldMatrix.indices) {
                // check if all the cells have been revealed
                val field: Field = getFieldContent(i, j)
                if (field.type == EMPTY && !field.wasClicked) {
                    return false
                }
            }
        }
        return true
    }

    //TODO - clarity on this
    private fun allMinesFlagged(): Boolean {
        for (i in fieldMatrix.indices) {
            for (j in fieldMatrix.indices) {
                // check if all the mines have been flagged
                val field: Field = getFieldContent(i, j)
                if (field.type == MINE && !field.isFlagged) {
                    return false
                }
            }
        }
        return true
    }


    fun revealCelL(row: Int, col: Int) {
        // Update the field clicked attribute
        val field: Field = fieldMatrix[row][col]
        if (field.wasClicked) {
           return
        }
        field.wasClicked = true
        if (field.minesAround == 0 && field.type == EMPTY) {
            // get the neighbors and reveal cell if they don't have bomb and not clicked
            val neighbors: List<Pair<Int, Int>> = getNeighbors(row, col)
            for ((r, c) in neighbors) {
                revealCelL(r, c)
            }
        }
    }

    private fun getNeighbors(row: Int, col: Int): List<Pair<Int, Int>> {
        // Instantiate a mutable list to hold the neighbors as pairs
        val neighbors = mutableListOf<Pair<Int, Int>>()

        // Iterate over the cells surrounding the given cell
        for (i in -1..1) {
            for (j in -1..1) {
                // Exclude the cell itself from its neighbors
                if (i == 0 && j == 0) continue

                // Calculate the potential neighbor's row and column indices
                val neighborRow = row + i
                val neighborCol = col + j

                // Check if the neighbor is within the grid bounds and meets the condition
                if (neighborRow in fieldMatrix.indices && neighborCol in fieldMatrix[0].indices
                    && fieldMatrix[neighborRow][neighborCol].type == EMPTY) {
                    // Add the neighbor's row and column as a pair to the list
                    neighbors.add(Pair(neighborRow, neighborCol))
                }
            }
        }

        // Return the list of neighbors as pairs
        return neighbors
    }
}