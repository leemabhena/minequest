package hu.ait.minesweeper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Chronometer
import android.widget.Spinner
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import hu.ait.minesweeper.databinding.ActivityMainBinding
import hu.ait.minesweeper.model.GameData
import hu.ait.minesweeper.model.GameState
import hu.ait.minesweeper.model.MineSweeperModel
import hu.ait.minesweeper.model.MineSweeperModel.gameData
import hu.ait.minesweeper.view.MineSweeperView
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var minesweeper: MineSweeperView
    var isFlagMode: Boolean = false

    // Chronometer settings
    private var isChronometerRunning = false
    private var pauseOffset: Long = 0
    private lateinit var chronometer: Chronometer

    // Buttons
    private lateinit var pauseGame: Button
    private lateinit var startGame: Button

    // difficulty
    private lateinit var difficulty: Spinner

    // flags remaining
    lateinit var flagsRemaining: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        minesweeper = binding.grid
        chronometer = binding.chronometer
        pauseGame = binding.pause
        startGame = binding.restart
        flagsRemaining = binding.flagsRemaining
        flagsRemaining.text = gameData.numFlags.toString()

        binding.flagSwitch.setOnCheckedChangeListener{ _, isChecked ->
            isFlagMode = isChecked
        }

        pauseGame.setOnClickListener{
            togglePauseGame()
        }

        startGame.setOnClickListener{
            toggleStartGame()
        }

        // difficulty
        difficulty = binding.spinner
        // Access the string array from resources
        val difficultyArray = resources.getStringArray(R.array.difficulty_array)

        // Create an ArrayAdapter using a simple spinner layout and the string array
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, difficultyArray)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        difficulty.adapter = adapter

        // Spinner item selection listener for changing game difficulty
        difficulty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when (parent.getItemAtPosition(position).toString().lowercase()) {
                    "easy" -> changeDifficulty(MineSweeperModel.easy)
                    "medium" -> changeDifficulty(MineSweeperModel.medium)
                    "hard" -> changeDifficulty(MineSweeperModel.hard)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
    }

    private fun togglePauseGame() {
        // stop the chronometer && make the grid un clickable
        if (MineSweeperModel.gameState == GameState.PAUSED) {
            // the game is paused, restart the game
            pauseGame.text = resources.getString(R.string.pause)
            MineSweeperModel.gameState = GameState.ONGOING
            // start the chronometer
            startChronometer()
        } else if (MineSweeperModel.gameState == GameState.ONGOING){
            // Want to pause the game
            pauseGame.text = resources.getString(R.string.resume)
            MineSweeperModel.gameState = GameState.PAUSED
            // Pause the chronometer
            pauseChronometer()
        }
    }

    private fun toggleStartGame() {
        // Start the game
        MineSweeperModel.gameState = GameState.ONGOING
        // start the chronometer from 0
        resetChronometer()
        // Start the chronometer
        startChronometer()
        // reset the game
        minesweeper.resetGame()
        // update the number of flags remaining
        flagsRemaining.text = gameData.numFlags.toString()
    }

    fun showEndGameMessage(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
    }

    // Chronometer functions

    private fun startChronometer() {
        if (!isChronometerRunning) {
            chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
            chronometer.start()
            isChronometerRunning = true
        }
    }

    private fun pauseChronometer() {
        if (isChronometerRunning) {
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
            chronometer.stop()
            isChronometerRunning = false
        }
    }

    fun resetChronometer() {
        chronometer.base = SystemClock.elapsedRealtime()
        pauseOffset = 0
        chronometer.stop()
        isChronometerRunning = false
    }

    fun updateFlagsRemaining(flagsUsed: Int) {
        if ((gameData.numFlags - flagsUsed) >= 0)
            flagsRemaining.text = (gameData.numFlags - flagsUsed).toString()
    }

    fun changeDifficulty(newGameData: GameData) {
        // update the game data
        gameData = newGameData
        // Update the number of flags
        flagsRemaining.text = gameData.numFlags.toString()
        // Reset the game with the new settings
        minesweeper.resetGame()
        // reset chronometer as well
        resetChronometer()
        // stop the game
        MineSweeperModel.gameState = GameState.STOPPED
    }
}

