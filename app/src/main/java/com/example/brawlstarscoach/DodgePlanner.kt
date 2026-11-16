
package com.example.brawlstarscoach

import kotlin.math.sqrt
import kotlin.math.max

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
                Point2D(0f, 0f), 0f, false
            )
        }

        var bestDirection = Point2D(0f, 0f)
        var bestDanger = Float.POSITIVE_INFINITY
        var found = false

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

                val safeDistance = max(
                    1f, threat.radius + 35f
                )

                danger += max(
                    0f,
                    (safeDistance - distance) / safeDistance
                )
            }

            if (danger < bestDanger) {
                bestDanger = danger
                bestDirection = Point2D(dx, dy)
                found = true
            }
        }

        return DodgeResult(
            direction = bestDirection,
            dangerScore = if (found) bestDanger else 1f,
            available = found
        )
    }
}
