package com.davek.laddergame.customview

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.davek.laddergame.AnimationSpeed
import com.davek.laddergame.R
import com.davek.laddergame.customview.Ladder.Companion.DEFAULT_PLAYER_COUNT
import com.davek.laddergame.customview.Ladder.Companion.LADDER_PAINT_STROKE_SIZE
import com.davek.laddergame.customview.Ladder.Companion.MAX_PLAYER_COUNT
import com.davek.laddergame.customview.Ladder.Companion.MIN_PLAYER_COUNT
import com.davek.laddergame.customview.Ladder.Companion.PLAYER_PAINT_STROKE_SIZE
import com.davek.laddergame.customview.Ladder.Companion.ROW_SIZE
import kotlin.math.max

class LadderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Ladder {

    private var playerPaths: MutableSet<Pair<Path, Paint>> = mutableSetOf()
    private var pathPlayer: Path? = null
    private var paintPlayer: Paint? = null
    private var paintPlayer1 = getPaint(R.color.player_1_path, PLAYER_PAINT_STROKE_SIZE)
    private var paintPlayer2 = getPaint(R.color.player_2_path, PLAYER_PAINT_STROKE_SIZE)
    private var paintPlayer3 = getPaint(R.color.player_3_path, PLAYER_PAINT_STROKE_SIZE)
    private var paintPlayer4 = getPaint(R.color.player_4_path, PLAYER_PAINT_STROKE_SIZE)
    private var paintPlayer5 = getPaint(R.color.player_5_path, PLAYER_PAINT_STROKE_SIZE)
    private var paintPlayer6 = getPaint(R.color.player_6_path, PLAYER_PAINT_STROKE_SIZE)
    private var paintLadder = getPaint(R.color.base_ladder, LADDER_PAINT_STROKE_SIZE)
    private var length = 0f
    var isDrawingPlayerPath: Boolean = false
        private set

    override var playerCount: Int = DEFAULT_PLAYER_COUNT
        set(value) {
            if (!isDrawingPlayerPath && value in MIN_PLAYER_COUNT..MAX_PLAYER_COUNT) {
                field = value
                pathPlayer = null
                playerPaths = mutableSetOf()
                getMeasurements()
                invalidate()
            }
        }
    override var ladderDetails: Array<BooleanArray>? = null
        set(value) {
            if (!isDrawingPlayerPath) {
                field = value
                playerPaths = mutableSetOf()
                pathPlayer = null
                invalidate()
            }
        }
    override var animationDuration: Long = AnimationSpeed.MEDIUM.duration
    override var resultDetails: Pair<Int, List<List<Int>>>? = null
        set(value) {
            if (!isDrawingPlayerPath) {
                value?.let { resultDetail ->
                    ladderDetails?.let {
                        paintPlayer = getPlayerPaintById(resultDetail.first)
                        startPlayer(resultDetail.second)
                    }
                    field = null
                }
            }
        }
    override var resultDetailsMap: Map<Int, List<List<Int>>> = mapOf()

    private val playerBoxWidth = context.resources.getDimension(R.dimen.player_box_width)
    private var gapBetweenPlayerBoxes = 0f
    private var gapBetweenLines = 0f
    private var xFirstVerticalLine = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawVerticalLines(canvas)
        ladderDetails?.let {
            drawSteps(canvas, it)
            for (path in playerPaths) {
                canvas.drawPath(path.first, path.second)
            }

            pathPlayer?.let { path ->
                paintPlayer?.let { paint ->
                    canvas.drawPath(path, paint)
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        getMeasurements()
        playerPaths = mutableSetOf()
        for (r in resultDetailsMap) {
            val paint = getPlayerPaintById(r.key)
            val path = getPlayerPath(r.value)
            playerPaths.add(Pair(path, paint))
        }
    }

    private fun drawVerticalLines(canvas: Canvas) {
        val heightInFloat = height.toFloat()
        canvas.drawLine(xFirstVerticalLine, 0f, xFirstVerticalLine, heightInFloat, paintLadder)
        for (i in 1 until playerCount) {
            val xValue = xFirstVerticalLine + i * gapBetweenLines
            canvas.drawLine(xValue, 0f, xValue, heightInFloat, paintLadder)
        }
    }

    private fun drawSteps(canvas: Canvas, stepsDetail: Array<BooleanArray>) {
        for (col in 0 until playerCount - 1) {
            for (row in 0 until ROW_SIZE) {
                if (stepsDetail[row][col]) {
                    /*
                    * ROW_SIZE + 1: +1 to have extra space for top
                    * row + 2: +1 because row starts from 0
                    */
                    val yValue = height / (ROW_SIZE + 1).toFloat() * (row + 1)
                    canvas.drawLine(
                        xFirstVerticalLine + (col) * gapBetweenLines,
                        yValue,
                        xFirstVerticalLine + (col + 1) * gapBetweenLines,
                        yValue,
                        paintLadder
                    )
                }
            }
        }
    }

    private fun startPlayer(playerPath: List<List<Int>>) {
        val newPlayerPath = getPlayerPath(playerPath)
        pathPlayer = newPlayerPath
        isDrawingPlayerPath = true
        paintPlayer?.let {
            animatePath(newPlayerPath, it)
        }
    }

    private fun getPlayerPath(playerPath: List<List<Int>>): Path {
        val gapVertical = height / (ROW_SIZE + 1).toFloat()
        val xStart = xFirstVerticalLine + gapBetweenLines * (playerPath[0][1]).toFloat()

        return Path().apply {
            moveTo(xStart, 0f)
            for (v in playerPath) {
                lineTo(
                    xFirstVerticalLine + v[1] * gapBetweenLines,
                    (v[0] + 1) * gapVertical
                )
            }
        }
    }

    private fun animatePath(path: Path, paint: Paint) {
        val measure = PathMeasure(path, false)
        length = measure.length
        ObjectAnimator.ofFloat(this, "phase", 1.0f, 0.0f).apply {
            duration = animationDuration
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    playerPaths.add(Pair(path, paint))
                    isDrawingPlayerPath = false
                }
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            start()
        }
    }

    // called by animator object
    private fun setPhase(phase: Float) {
        paintPlayer?.pathEffect = createPathEffect(length, phase)
        invalidate()
    }

    private fun createPathEffect(pathLength: Float, phase: Float): PathEffect {
        val offset = 0f
        return DashPathEffect(
            floatArrayOf(pathLength, pathLength),
            max(phase * pathLength, offset)
        )
    }

    private fun getMeasurements() {
        gapBetweenPlayerBoxes = (width - playerBoxWidth * playerCount) / (playerCount + 1)
        gapBetweenLines = gapBetweenPlayerBoxes + playerBoxWidth
        xFirstVerticalLine = gapBetweenPlayerBoxes + (playerBoxWidth / 2)
    }

    private fun getPlayerPaintById(playerNumber: Int): Paint = when(playerNumber) {
        1 -> paintPlayer1
        2 -> paintPlayer2
        3 -> paintPlayer3
        4 -> paintPlayer4
        5 -> paintPlayer5
        6 -> paintPlayer6
        else -> paintPlayer1
    }

    private fun getPaint(@ColorRes colorResId: Int, strokeSize: Float) = Paint().apply {
        color = ContextCompat.getColor(context, colorResId)
        strokeWidth = strokeSize
        style = Paint.Style.STROKE
    }
}