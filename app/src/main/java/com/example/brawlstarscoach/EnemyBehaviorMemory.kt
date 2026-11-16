
package com.example.brawlstarscoach

import kotlin.math.roundToInt

enum class MoveDirection {
    LEFT,
    RIGHT,
    UP,
    DOWN,
    UNKNOWN
}

data class EnemyProfile(
    val observations: Int,
    val leftCount: Int,
    val rightCount: Int,
    val upCount: Int,
    val downCount: Int
) {
    fun mostFrequent(): MoveDirection {
        val values = mapOf(
            MoveDirection.LEFT to leftCount,
            MoveDirection.RIGHT to rightCount,
            MoveDirection.UP to upCount,
            MoveDirection.DOWN to downCount
        )

        return values.maxByOrNull { it.value }
            ?.takeIf { it.value > 0 }
            ?.key ?: MoveDirection.UNKNOWN
    }

    fun probability(direction: MoveDirection): Int {
        if (observations == 0) return 0

        val count = when (direction) {
            MoveDirection.LEFT -> leftCount
            MoveDirection.RIGHT -> rightCount
            MoveDirection.UP -> upCount
            MoveDirection.DOWN -> downCount
            MoveDirection.UNKNOWN -> 0
        }

        return (count * 100f / observations).roundToInt()
    }
}

class EnemyBehaviorMemory {

    private val history = mutableListOf<MoveDirection>()

    @Synchronized
    fun observe(direction: MoveDirection) {
        if (direction == MoveDirection.UNKNOWN) return

        history.add(direction)

        if (history.size > 200) {
            history.removeAt(0)
        }
    }

    @Synchronized
    fun profile(): EnemyProfile {

        return EnemyProfile(
            observations = history.size,
            leftCount = history.count {
                it == MoveDirection.LEFT
            },
            rightCount = history.count {
                it == MoveDirection.RIGHT
            },
            upCount = history.count {
                it == MoveDirection.UP
            },
            downCount = history.count {
                it == MoveDirection.DOWN
            }
        )
    }

    @Synchronized
    fun reset() {
        history.clear()
    }
}
