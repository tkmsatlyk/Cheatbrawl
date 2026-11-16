
package com.example.brawlstarscoach

import kotlin.math.abs

class CharacterMovementTracker(
    private val memory: CharacterBehaviorMemory,
    private val movementThreshold: Float = 8f
) {

    private val previousPositions =
        mutableMapOf<String, Point2D>()

    @Synchronized
    fun observe(
        characterId: String,
        currentPosition: Point2D
    ): MoveDirection {

        if (characterId.isBlank()) {
            return MoveDirection.UNKNOWN
        }

        val previous = previousPositions[characterId]
        previousPositions[characterId] = currentPosition

        if (previous == null) {
            return MoveDirection.UNKNOWN
        }

        val dx = currentPosition.x - previous.x
        val dy = currentPosition.y - previous.y

        if (
            abs(dx) < movementThreshold &&
            abs(dy) < movementThreshold
        ) {
            return MoveDirection.UNKNOWN
        }

        val direction = if (abs(dx) > abs(dy)) {
            if (dx < 0f) {
                MoveDirection.LEFT
            } else {
                MoveDirection.RIGHT
            }
        } else {
            if (dy < 0f) {
                MoveDirection.UP
            } else {
                MoveDirection.DOWN
            }
        }

        memory.observe(characterId, direction)

        return direction
    }

    @Synchronized
    fun forget(characterId: String) {
        previousPositions.remove(characterId)
    }

    @Synchronized
    fun reset() {
        previousPositions.clear()
    }
}
