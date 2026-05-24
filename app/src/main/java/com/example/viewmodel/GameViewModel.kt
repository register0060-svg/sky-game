package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PreferencesManager
import com.example.models.GameDifficulty
import com.example.models.GameMode
import com.example.models.ScoreEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class GameEvent {
    object Correct : GameEvent()
    object Wrong : GameEvent()
    object Win : GameEvent()
    object Fail : GameEvent()
}

data class CellProps(
    val sizeScale: Float,
    val opacity: Float,
    val rotation: Float,
    val translateX: Float,
    val translateY: Float
)

data class GameState(
    val mode: GameMode = GameMode.MODE_1,
    val diff: GameDifficulty = GameDifficulty.EASY,
    val numbers: List<Int> = emptyList(),
    val cellProps: List<CellProps> = emptyList(),
    val chaosPositions: List<Pair<Float, Float>> = emptyList(),
    val target: Int = 1,
    val timeLeft: Int = 0,
    val totalTime: Int = 0,
    val found: Set<Int> = emptySet(),
    val mistakes: Int = 0,
    val combo: Int = 0,
    val elapsed: Float = 0f,
    val isPlaying: Boolean = false,
    val isFinished: Boolean = false,
    val isWin: Boolean = false,
    val finalScore: Int = 0,
    val isNewBest: Boolean = false,
    val errorCell: Int? = null,
    val okCell: Int? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val scoreDao = db.scoreDao()
    val prefs = PreferencesManager(application)

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.soundOn)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()
    
    private val _userName = MutableStateFlow(prefs.userName)
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private var timerJob: Job? = null
    private var startTimeMs: Long = 0

    fun toggleSound() {
        val newSound = !prefs.soundOn
        prefs.soundOn = newSound
        _soundEnabled.value = newSound
    }
    
    fun setUserName(name: String) {
        val cleanName = name.take(10).uppercase()
        prefs.userName = cleanName
        _userName.value = cleanName
    }

    fun getTopScoresByDiff(diffId: String): Flow<List<ScoreEntry>> {
        return if (diffId == "all") scoreDao.getTopScores() else scoreDao.getTopScoresByDiff(diffId)
    }

    fun startGame(mode: GameMode, diff: GameDifficulty) {
        timerJob?.cancel()
        
        val sequence = (1..mode.numbersCount).toList().shuffled()
        val totalTime = mode.baseTimeSeconds + diff.timeMod
        
        val cellProps = if (diff == GameDifficulty.HARD) {
            sequence.map {
                if (mode.isChaos) {
                    CellProps(
                        sizeScale = Random.nextFloat() * 1.2f + 0.9f,
                        opacity = Random.nextFloat() * 0.6f + 0.28f,
                        rotation = Random.nextInt(-24, 24).toFloat(),
                        translateX = 0f,
                        translateY = 0f
                    )
                } else {
                    CellProps(
                        sizeScale = Random.nextFloat() * 0.9f + 0.55f,
                        opacity = Random.nextFloat() * 0.4f + 0.55f,
                        rotation = Random.nextInt(-24, 24).toFloat(),
                        translateX = Random.nextInt(-7, 7).toFloat(),
                        translateY = Random.nextInt(-6, 6).toFloat()
                    )
                }
            }
        } else {
            emptyList()
        }
        
        val chaosPositions = if (mode.isChaos) {
            sequence.map {
                Pair(Random.nextFloat(), Random.nextFloat())
            }
        } else {
            emptyList()
        }

        _state.value = GameState(
            mode = mode,
            diff = diff,
            numbers = sequence,
            cellProps = cellProps,
            chaosPositions = chaosPositions,
            target = 1,
            timeLeft = totalTime,
            totalTime = totalTime,
            found = emptySet(),
            mistakes = 0,
            combo = 0,
            elapsed = 0f,
            isPlaying = true,
            isFinished = false,
            isWin = false
        )
        
        startTimeMs = System.currentTimeMillis()
        startTimer()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_state.value.timeLeft > 0 && _state.value.isPlaying) {
                delay(1000)
                _state.update {
                    val newTimeLeft = it.timeLeft - 1
                    if (newTimeLeft <= 0) {
                        _events.tryEmit(GameEvent.Fail)
                        endGame(false)
                        it.copy(timeLeft = 0)
                    } else {
                        it.copy(timeLeft = newTimeLeft)
                    }
                }
            }
        }
    }

    fun tapNumber(number: Int, index: Int) {
        val currentState = _state.value
        if (!currentState.isPlaying || currentState.found.contains(number)) return

        if (number == currentState.target) {
            // Correct
            val newFound = currentState.found + number
            val newCombo = currentState.combo + 1
            
            _state.update {
                it.copy(
                    found = newFound,
                    combo = newCombo,
                    target = currentState.target + 1,
                    okCell = index,
                    errorCell = null
                )
            }
            
            if (newFound.size == currentState.mode.numbersCount) {
                _events.tryEmit(GameEvent.Win)
                endGame(true)
            } else {
                _events.tryEmit(GameEvent.Correct)
            }
            
            // Clear ok cell highlight after a brief moment
            viewModelScope.launch {
                delay(300)
                _state.update { if (it.okCell == index) it.copy(okCell = null) else it }
            }
        } else {
            // Wrong
            _events.tryEmit(GameEvent.Wrong)
            val newMistakes = currentState.mistakes + 1
            val newTimeLeft = Math.max(2, currentState.timeLeft - currentState.diff.penalty)
            
            _state.update {
                it.copy(
                    combo = 0,
                    mistakes = newMistakes,
                    timeLeft = newTimeLeft,
                    errorCell = index,
                    okCell = null
                )
            }
            
            // Clear error cell highlight
            viewModelScope.launch {
                delay(400)
                _state.update { if (it.errorCell == index) it.copy(errorCell = null) else it }
            }
        }
    }

    private fun endGame(isWin: Boolean) {
        timerJob?.cancel()
        val elapsed = (System.currentTimeMillis() - startTimeMs) / 1000f
        
        val currentState = _state.value
        if (isWin) {
            val isNewBest = prefs.saveBestTime(currentState.mode.id, currentState.diff.id, elapsed)
            val scoreParams = currentState.mode.numbersCount.toFloat() / elapsed * 1000f * currentState.diff.scoreMultiplier
            val rawScore = (scoreParams - (currentState.mistakes * 45)).toInt()
            val score = Math.max(0, rawScore)
            
            _state.update {
                it.copy(
                    isPlaying = false,
                    isFinished = true,
                    isWin = true,
                    elapsed = elapsed,
                    isNewBest = isNewBest,
                    finalScore = score
                )
            }
        } else {
            _state.update {
                it.copy(
                    isPlaying = false,
                    isFinished = true,
                    isWin = false,
                    elapsed = currentState.totalTime.toFloat(),
                    finalScore = 0
                )
            }
        }
    }
    
    fun submitScore() {
        val currentState = _state.value
        if (!currentState.isWin || prefs.userName.isBlank()) return
        
        viewModelScope.launch {
            val entry = ScoreEntry(
                name = prefs.userName,
                score = currentState.finalScore,
                modeId = currentState.mode.id,
                modeName = currentState.mode.title,
                diffId = currentState.diff.id,
                diffName = currentState.diff.title,
                time = currentState.elapsed,
                mistakes = currentState.mistakes
            )
            
            // Clean up lower scores of same user for same mode/diff
            scoreDao.deleteLowerScores(entry.name, entry.modeId, entry.diffId, entry.score)
            // Insert new score
            scoreDao.insertScore(entry)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
