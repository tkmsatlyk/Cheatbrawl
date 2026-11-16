
package com.example.brawlstarscoach.dodge

import com.example.brawlstarscoach.Point2D
import com.example.brawlstarscoach.Threat
import kotlin.math.max
import kotlin.math.sqrt

data class DangerConfig(
    val playerSpeed: Float = 220f,
    val playerRadius: Float = 24f,
    val predictionTime: Float = 1.2f,
    val samples: Int = 12,
    val screenMargin: Float = 20f
)

class DangerMap(
    private val config: DangerConfig = DangerConfig()
) {

    fun evaluate(
        player: Point2D,
        direction: Point2D,
        threats: List<Threat>,
        screenWidth: Float,
        screenHeight: Float
    ): Float {

        var totalDanger = 0f

        for (step in 1..config.samples) {

            val time = config.predictionTime *
                step / config.samples

            val px = player.x +
                direction.x * config.playerSpeed * time

            val py = player.y +
                direction.y * config.playerSpeed * time

            if (
                px < config.screenMargin ||
                py < config.screenMargin ||
                px > screenWidth - config.screenMargin ||
                py > screenHeight - config.screenMargin
            ) {
                return Float.POSITIVE_INFINITY
            }

            for (threat in threats) {

                val tx = threat.position.x +
                    threat.velocity.x * time

                val ty = threat.position.y +
                    threat.velocity.y * time

                val dx = px - tx
                val dy = py - ty

                val distance = sqrt(dx * dx + dy * dy)

                val safeDistance =
                    max(1f, config.playerRadius + threat.radius)

                val risk = (
                    (safeDistance - distance) / safeDistance
                ).coerceIn(0f, 1f)

                totalDanger += risk * risk
            }
        }

        return totalDanger / config.samples
    }
}
