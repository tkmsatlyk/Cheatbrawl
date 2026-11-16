
package com.example.brawlstarscoach.dodge

import com.example.brawlstarscoach.Point2D
import com.example.brawlstarscoach.Threat
import com.example.brawlstarscoach.DodgeResult
import kotlin.math.sqrt

class AutoDodgeEngine(
    private val dangerMap: DangerMap = DangerMap()
) {

    private val directions = listOf(
        Point2D(0f, 0f),
        Point2D(-1f, 0f),
        Point2D(1f, 0f),
        Point2D(0f, -1f),
        Point2D(0f, 1f),
        Point2D(-0.707f, -0.707f),
        Point2D(0.707f, -0.707f),
        Point2D(-0.707f, 0.707f),
        Point2D(0.707f, 0.707f)
    )

    fun calculate(
        player: Point2D,
        threats: List<Threat>,
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
        var bestDanger = Float.POSITIVE_INFINITY
        var found = false

        for (direction in directions) {

            val length = sqrt(
                direction.x * direction.x +
                direction.y * direction.y
            )

            val normalized = if (length > 0f) {
                Point2D(
                    direction.x / length,
                    direction.y / length
                )
            } else {
                direction
            }

            val danger = dangerMap.evaluate(
                player = player,
                direction = normalized,
                threats = threats,
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )

            if (danger < bestDanger) {
                bestDanger = danger
                bestDirection = normalized
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
