
package com.example.brawlstarscoach

import kotlin.math.sqrt

data class Point2D(
    val x: Float,
    val y: Float
)

data class Threat(
    val position: Point2D,
    val velocity: Point2D,
    val radius: Float
)

data class DodgeResult(
    val direction: Point2D,
    val dangerScore: Float,
    val available: Boolean
)

class DodgePlanner {

    private val directions = listOf(
        Point2D(-1f, -1f),
        Point2D(0f, -1f),
        Point2D(1f, -1f),
        Point2D(-1f, 0f),
        Point2D(1f, 0f),
        Point2D(-1f, 1f),
        Point2D(0f, 1f),
        Point2D(1f, 1f)
    )

    fun calculate(
        player: Point2D,
        threats: List<Threat>,
        predictionTime: Float,
        screenWidth: Float,
        screenHeight: Float
    ): DodgeResult {

        if (threats.isEmpty()) {
            return DodgeResult(
                Point2D(0f, 0f),
                0f,
                false
            )
        }

        var bestDirection = Point2D(0f, 0f)
        var bestScore = Float.NEGATIVE_INFINITY

        for (direction in directions) {

            val length = sqrt(
                direction.x * direction.x +
                direction.y * direction.y
            )

            val dx = direction.x / length
            val dy = direction.y / length

            val futurePlayer = Point2D(
                player.x + dx * 100f,
                player.y + dy * 100f
            )

            if (
                futurePlayer.x < 20f ||
                futurePlayer.y < 20f ||
                futurePlayer.x > screenWidth - 20f ||
                futurePlayer.y > screenHeight - 20f
            ) continue

            var danger = 0f

            for (threat in threats) {

                val futureX =
                    threat.position.x +
                    threat.velocity.x * predictionTime

                val futureY =
                    threat.position.y +
                    threat.velocity.y * predictionTime

                val distance = sqrt(
                    (futurePlayer.x - futureX) *
                    (futurePlayer.x - futureX) +
                    (futurePlayer.y - futureY) *
                    (futurePlayer.y - futureY)
                )

                val safeDistance = threat.radius + 35f

                if (distance < safeDistance) {
                    danger +=
                        (safeDistance - distance) /
                        safeDistance
                }
            }

            val score = -danger

            if (score > bestScore) {
                bestScore = score
                bestDirection = Point2D(dx, dy)
            }
        }

        if (bestScore == Float.NEGATIVE_INFINITY) {
            return DodgeResult(
                Point2D(0f, 0f),
                1f,
                false
            )
        }

        return DodgeResult(
            direction = bestDirection,
            dangerScore = -bestScore,
            available = true
        )
    }
}
