package com.davek.laddergame.customview

interface Ladder {

    companion object {
        const val ROW_SIZE: Int = 15
        const val MIN_PLAYER_COUNT: Int = 2
        const val MAX_PLAYER_COUNT: Int = 6
        const val DEFAULT_PLAYER_COUNT: Int = MIN_PLAYER_COUNT
        const val LADDER_PAINT_STROKE_SIZE: Float = 10f
        const val PLAYER_PAINT_STROKE_SIZE: Float = 20f
    }

    /*
    * number of players
    */
    var playerCount : Int

    /*
    *  first value: player number
    *  second value: result detail
    */
    var resultDetails : Pair<Int, List<List<Int>>>?

    /*
    *  map of player result details
    */
    var resultDetailsMap : Map<Int, List<List<Int>>>

    /*
    * ladder step details to draw
    */
    var ladderDetails : Array<BooleanArray>?

    /*
    * duration of player path drawing animation
    */
    var animationDuration : Long
}