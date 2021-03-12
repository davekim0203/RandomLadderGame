package com.davek.laddergame

enum class AnimationSpeed(val value: Int, val duration: Long) {
    OFF(0, 0L),
    LOW(1, 4000L),
    MEDIUM(2, 2000L),
    HIGH(3, 1000L)
}