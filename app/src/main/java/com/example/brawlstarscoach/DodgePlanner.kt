
package com.example.brawlstarscoach

import kotlin.math.sqrt
import kotlin.math.max
import kotlin.math.min

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

        if (threats.isEmpty() || predictionTime <= 0f) {
            return DodgeResult(
                Point2D(0f, 0f),
                0f,
                false
            )
        }

        val moveDistance = 100f
        val playerSpeed = moveDistance / predictionTime

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
                player.x + dx * moveDistance,
                player.y + dy * moveDistance
            )

            if (
                futurePlayer.x < 20f ||
                futurePlayer.y < 20f ||
                futurePlayer.x > screenWidth - 20f ||
                futurePlayer.y > screenHeight - 20f
            ) continue

            val playerVelocity = Point2D(
                dx * playerSpeed,
                dy * playerSpeed
            )

            var danger = 0f

            for (threat in threats) {

                val rx = threat.position.x - player.x
                val ry = threat.position.y - player.y

                val vx =
                    threat.velocity.x - playerVelocity.x

                val vy =
                    threat.velocity.y - playerVelocity.y

                val velocitySquared = vx * vx + vy * vy

                val closestTime = if (velocitySquared > 0.0001f) {
                    (
                        -(rx * vx + ry * vy) /
                        velocitySquared
                    ).coerceIn(0f, predictionTime)
                } else {
                    0f
                }

                val closestX = rx + vx * closestTime
                val closestY = ry + vy * closestTime

                val distance = sqrt(
                    closestX * closestX +
                    closestY * closestY
                )

                val safeDistance = max(
                    1f,
                    threat.radius + 35f
                )

                val risk = (
                    (safeDistance - distance) /
                    safeDistance
                ).coerceIn(0f, 1f)

                danger += risk
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
