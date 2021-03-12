package com.davek.laddergame.util

import androidx.databinding.BindingAdapter
import com.davek.laddergame.AnimationSpeed
import com.davek.laddergame.customview.LadderView


@BindingAdapter("playerCount")
fun LadderView.setPlayerCount(count: Int?) {
    count?.let {
        playerCount = it
    }
}

@BindingAdapter("showAnimatedResult")
fun LadderView.showAnimatedResult(detail: Pair<Int, List<List<Int>>>?) {
    resultDetails = detail
}

@BindingAdapter("drawnResults")
fun LadderView.setDrawnResults(results: Map<Int, List<List<Int>>>?) {
    results?.let {
        resultDetailsMap = results
    }
}

@BindingAdapter("ladderSteps")
fun LadderView.setLadderSteps(stepDetails: Array<BooleanArray>?) {
    ladderDetails = stepDetails
}

@BindingAdapter("animationDuration")
fun LadderView.setAnimationDuration(speed: AnimationSpeed?) {
    speed?.let {
        animationDuration = it.duration
    }
}