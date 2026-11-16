
package com.example.brawlstarscoach

import kotlin.math.roundToInt

data class CharacterBehaviorProfile(
    val characterId: String,
    val observations: Int,
    val left: Int,
    val right: Int,
    val up: Int,
    val down: Int
) {
    fun probability(direction: MoveDirection): Int {
        if (observations == 0) return 0

        val count = when (direction) {
            MoveDirection.LEFT -> left
            MoveDirection.RIGHT -> right
            MoveDirection.UP -> up
            MoveDirection.DOWN -> down
            MoveDirection.UNKNOWN -> 0
        }

        return (count * 100f / observations).roundToInt()
    }

    fun mostFrequent(): MoveDirection {
        val counts = mapOf(
            MoveDirection.LEFT to left,
            MoveDirection.RIGHT to right,
            MoveDirection.UP to up,
            MoveDirection.DOWN to down
        )

        return counts.maxByOrNull { it.value }
            ?.takeIf { it.value > 0 }
            ?.key ?: MoveDirection.UNKNOWN
    }
}

class CharacterBehaviorMemory {

    private val histories =
        mutableMapOf<String, MutableList<MoveDirection>>()

    @Synchronized
    fun observe(
        characterId: String,
        direction: MoveDirection
    ) {
        if (characterId.isBlank()) return
        if (direction == MoveDirection.UNKNOWN) return

        val history = histories.getOrPut(characterId) {
            mutableListOf()
        }

        history.add(direction)

        if (history.size > 500) {
            history.removeAt(0)
        }
    }

    @Synchronized
    fun profile(
        characterId: String
    ): CharacterBehaviorProfile {

        val history = histories[characterId]
            ?: emptyList<MoveDirection>()

        return CharacterBehaviorProfile(
            characterId = characterId,
            observations = history.size,
            left = history.count {
                it == MoveDirection.LEFT
            },
            right = history.count {
                it == MoveDirection.RIGHT
            },
            up = history.count {
                it == MoveDirection.UP
            },
            down = history.count {
                it == MoveDirection.DOWN
            }
        )
    }

    @Synchronized
    fun knownCharacters(): Set<String> {
        return histories.keys.toSet()
    }

    @Synchronized
    fun reset(characterId: String) {
        histories.remove(characterId)
    }

    @Synchronized
    fun resetAll() {
        histories.clear()
    }
}
