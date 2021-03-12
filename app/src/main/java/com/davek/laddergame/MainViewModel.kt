package com.davek.laddergame

import androidx.lifecycle.*
import com.davek.laddergame.customview.Ladder.Companion.DEFAULT_PLAYER_COUNT
import com.davek.laddergame.customview.Ladder.Companion.ROW_SIZE
import com.davek.laddergame.util.SingleLiveEvent
import kotlin.random.Random

class MainViewModel : ViewModel() {

    private val _playerCount = MutableLiveData<Int>()
    val playerCount: LiveData<Int>
        get() = _playerCount

    private val areStepsDrawn = MutableLiveData<Boolean>()
    val ladderStepsDetail = MediatorLiveData<Array<BooleanArray>?>()

    private val _playerResultDetails = SingleLiveEvent<Pair<Int, List<List<Int>>>>()
    val playerResultDetails: LiveData<Pair<Int, List<List<Int>>>>
        get() = _playerResultDetails

    private val _playerResultDetailsMap = MutableLiveData<MutableMap<Int, List<List<Int>>>>()
    val playerResultDetailsMap: LiveData<MutableMap<Int, List<List<Int>>>>
        get() = _playerResultDetailsMap

    val isGameRunning = MediatorLiveData<Boolean>()

    private val _animationSpeed = MutableLiveData<AnimationSpeed>()
    val animationSpeed: LiveData<AnimationSpeed>
        get() = _animationSpeed

    private val _showStartNewGameDialog = SingleLiveEvent<Any>()
    val showStartNewGameDialog: LiveData<Any>
        get() = _showStartNewGameDialog

    private val _showAnimationSpeedDialog = SingleLiveEvent<Any>()
    val showAnimationSpeedDialog: LiveData<Any>
        get() = _showAnimationSpeedDialog

    init {
        ladderStepsDetail.addSource(playerCount) {
            ladderStepsDetail.value = setLadderStepsDetail(areStepsDrawn, playerCount)
        }
        ladderStepsDetail.addSource(areStepsDrawn) {
            ladderStepsDetail.value = setLadderStepsDetail(areStepsDrawn, playerCount)
        }
        isGameRunning.addSource(playerResultDetails) {
            isGameRunning.value = setIsGameRunning(playerResultDetailsMap)
        }
        isGameRunning.addSource(playerResultDetailsMap) {
            isGameRunning.value = setIsGameRunning(playerResultDetailsMap)
        }

        _playerCount.value = DEFAULT_PLAYER_COUNT
        areStepsDrawn.value = false
        _animationSpeed.value = AnimationSpeed.MEDIUM
    }

    private fun setLadderStepsDetail(isReady: LiveData<Boolean>, numOfPlayers: LiveData<Int>): Array<BooleanArray>? {
        val ready = isReady.value
        val count = numOfPlayers.value

        return if(ready != null && ready && count != null) {
            _playerResultDetailsMap.value = mutableMapOf()
            createLadder(count)
        } else {
            null
        }
    }

    private fun setIsGameRunning(isRunning: LiveData<MutableMap<Int, List<List<Int>>>>): Boolean? {
        return isRunning.value?.isNotEmpty()
    }

    fun onPlayerCountIncreaseButtonClick() {
        _playerCount.value?.let {
            if(it < 6) {
                val newPlayerCount = it + 1
                _playerCount.value = newPlayerCount
            }
        }
    }

    fun onPlayerCountDecreaseButtonClick() {
        _playerCount.value?.let {
            if(it > 2) {
                val newPlayerCount = it - 1
                _playerCount.value = newPlayerCount
            }
        }
    }

    fun onCreateStepsButtonClick() {
        _playerCount.value?.let {
            areStepsDrawn.value = true
        }
    }

    fun onResetStepsButtonClick() {
        val isRunning = isGameRunning.value
        val count = playerCount.value
        val drawnPlayerResults = playerResultDetailsMap.value

        if(isRunning != null && count != null && drawnPlayerResults != null) {
            if (isRunning && count != drawnPlayerResults.size) {
                _showStartNewGameDialog.call()
            } else {
                resetGame()
            }
        }
    }

    fun resetGame() {
        _playerResultDetailsMap.value = mutableMapOf()
        areStepsDrawn.value = false
    }

    fun onPlayerClick(playerNumber: Int) {
        ladderStepsDetail.value?.let {
            val details = Pair(playerNumber, getPlayerResultPathDetail(playerNumber - 1, it))
            _playerResultDetailsMap.value?.put(details.first, details.second)
            _playerResultDetails.value = details
        }
    }

    fun onAnimationSpeedButtonClick() {
        _showAnimationSpeedDialog.call()
    }

    fun setSpeed(speed: Int) {
        _animationSpeed.value = when(speed) {
            AnimationSpeed.OFF.value -> AnimationSpeed.OFF
            AnimationSpeed.LOW.value -> AnimationSpeed.LOW
            AnimationSpeed.MEDIUM.value -> AnimationSpeed.MEDIUM
            AnimationSpeed.HIGH.value -> AnimationSpeed.HIGH
            else -> AnimationSpeed.MEDIUM
        }
    }

    private fun createLadder(colSize: Int): Array<BooleanArray> {
        val board: Array<BooleanArray> = Array(ROW_SIZE) { BooleanArray(colSize) }
        for(row in 0 until ROW_SIZE) {
            //if first col, can be either true or false
            board[row][0] = Random.nextBoolean()

            for(col in 1 until colSize-1) {
                // Can't be true if left vertex is true
                board[row][col] = if(board[row][col - 1]) {
                    false
                } else {
                    Random.nextBoolean()
                }
            }
            // Last column is all false because it can't go right
            board[row][colSize - 1] = false
        }

        return board
    }

    private fun getPlayerResultPathDetail(user: Int, ladder: Array<BooleanArray>): List<List<Int>> {
        val playerPath: MutableList<List<Int>> = mutableListOf()
        val previousPos = Vertex<Int?, Int?>(null, null)
        val currentPos = Vertex(0, user)
        var reachedEnd = false
        playerPath.add(listOf(currentPos.row, currentPos.col))

        while (!reachedEnd) {
            val currentPosCopy = currentPos.copy()
            when {
                currentPos.row == previousPos.row -> {
                    currentPos.row = currentPos.row + 1
                }
                ladder[currentPos.row][currentPos.col] -> {
                    currentPos.col = currentPos.col + 1
                }
                currentPos.col > 0 && ladder[currentPos.row][currentPos.col - 1] -> {
                    currentPos.col = currentPos.col - 1
                }
                else -> {
                    currentPos.row = currentPos.row + 1
                }
            }
            previousPos.row = currentPosCopy.row
            previousPos.col = currentPosCopy.col

            playerPath.add(listOf(currentPos.row, currentPos.col))

            if (currentPos.row == ROW_SIZE) {
                reachedEnd = true
            }
        }

        return playerPath
    }
}

data class Vertex<A, B>(
    var row: A,
    var col: B
)