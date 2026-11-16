
package com.example.brawlstarscoach.dodge

enum class AttackPattern {
    LINEAR,
    SPREAD,
    EXPLOSIVE,
    CURVING,
    AREA_CONTROL,
    UNKNOWN
}

enum class DodgeStrategy {
    ZIGZAG,
    STRAFE,
    FEINT,
    SIDE_STEP,
    MOVE_OUT_OF_AREA,
    VARIABLE_MOVEMENT,
    UNKNOWN
}

data class BrawlerDodgeProfile(
    val name: String,
    val attackPattern: AttackPattern,
    val preferredStrategies: List<DodgeStrategy>,
    val notes: String
)

object BrawlerDodgeDatabase {

    val profiles = listOf(

        BrawlerDodgeProfile(
            "Colt",
            AttackPattern.LINEAR,
            listOf(
                DodgeStrategy.ZIGZAG,
                DodgeStrategy.SIDE_STEP,
                DodgeStrategy.FEINT
            ),
            "Straight projectile streams"
        ),

        BrawlerDodgeProfile(
            "Shelly",
            AttackPattern.SPREAD,
            listOf(
                DodgeStrategy.STRAFE,
                DodgeStrategy.MOVE_OUT_OF_AREA,
                DodgeStrategy.FEINT
            ),
            "Close-range spreading attack"
        ),

        BrawlerDodgeProfile(
            "Brock",
            AttackPattern.EXPLOSIVE,
            listOf(
                DodgeStrategy.SIDE_STEP,
                DodgeStrategy.VARIABLE_MOVEMENT,
                DodgeStrategy.MOVE_OUT_OF_AREA
            ),
            "Rocket impact and explosion"
        ),

        BrawlerDodgeProfile(
            "Piper",
            AttackPattern.LINEAR,
            listOf(
                DodgeStrategy.FEINT,
                DodgeStrategy.ZIGZAG,
                DodgeStrategy.SIDE_STEP
            ),
            "Long-range precision attack"
        ),

        BrawlerDodgeProfile(
            "Dynamike",
            AttackPattern.EXPLOSIVE,
            listOf(
                DodgeStrategy.MOVE_OUT_OF_AREA,
                DodgeStrategy.VARIABLE_MOVEMENT
            ),
            "Delayed explosive attack"
        ),

        BrawlerDodgeProfile(
            "Spike",
            AttackPattern.SPREAD,
            listOf(
                DodgeStrategy.SIDE_STEP,
                DodgeStrategy.VARIABLE_MOVEMENT,
                DodgeStrategy.ZIGZAG
            ),
            "Projectile splitting and spread"
        )
    )

    fun find(name: String): BrawlerDodgeProfile? {
        return profiles.firstOrNull {
            it.name.equals(name, ignoreCase = true)
        }
    }
}
