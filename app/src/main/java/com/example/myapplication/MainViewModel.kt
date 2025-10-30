package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

class MainViewModel : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState

    init {
        startGame()
    }

    fun startGame() {
        startLevel(1)
    }

    private fun startLevel(level: Int) {
        val rangeMax = (10 * 1.5.pow(level - 1)).roundToInt().coerceAtMost(1_000_000)
        val chances = (3 + log10(rangeMax.toFloat())).roundToInt().coerceIn(3, 9)
        val target = (1..rangeMax).random()

        _gameState.value = GameState(
            level = level,
            target = target,
            rangeMax = rangeMax,
            remainingChances = chances,
            message = "A new round has started!",
            status = GameStatus.RUNNING,
            error = null
        )
    }

    fun onGuess(guessStr: String) {
        val guess = guessStr.toIntOrNull()
        if (guess == null) {
            _gameState.value = _gameState.value.copy(error = ErrorType.INVALID_INPUT)
            return
        }

        if (guess < 1 || guess > _gameState.value.rangeMax) {
            _gameState.value = _gameState.value.copy(error = ErrorType.OUT_OF_RANGE)
            return
        }

        val currentChances = _gameState.value.remainingChances - 1
        when {
            guess == _gameState.value.target -> {
                _gameState.value = _gameState.value.copy(
                    message = "You guessed it! The number was ${_gameState.value.target}.",
                    status = GameStatus.WON,
                    error = null
                )
            }
            currentChances == 0 -> {
                _gameState.value = _gameState.value.copy(
                    remainingChances = 0,
                    message = "You lost! The number was ${_gameState.value.target}.",
                    status = GameStatus.LOST,
                    error = null
                )
            }
            else -> {
                _gameState.value = _gameState.value.copy(
                    remainingChances = currentChances,
                    message = if (guess > _gameState.value.target) "Too high!" else "Too low!",
                    error = null
                )
            }
        }
    }

    fun onHint(currentGuessStr: String?) {
        if (_gameState.value.remainingChances <= 1) {
            _gameState.value = _gameState.value.copy(message = "Not enough chances for a hint.")
            return
        }

        val currentGuess = currentGuessStr?.toIntOrNull()
        val message = when {
            currentGuess == null -> "Enter a number first to get a hint."
            currentGuess > _gameState.value.target -> "The number is lower than $currentGuess."
            else -> "The number is higher than $currentGuess."
        }
        _gameState.value = _gameState.value.copy(
            remainingChances = _gameState.value.remainingChances - 1,
            message = message,
            error = null
        )
    }

    fun goNextLevel() {
        startLevel(_gameState.value.level + 1)
    }

    fun restartGame() {
        startGame()
    }
}

data class GameState(
    val level: Int = 1,
    val target: Int = 0,
    val rangeMax: Int = 10,
    val remainingChances: Int = 3,
    val message: String = "Welcome!",
    val status: GameStatus = GameStatus.IDLE,
    val error: ErrorType? = null
)

enum class GameStatus {
    IDLE,
    RUNNING,
    WON,
    LOST
}

enum class ErrorType {
    INVALID_INPUT,
    OUT_OF_RANGE
}
