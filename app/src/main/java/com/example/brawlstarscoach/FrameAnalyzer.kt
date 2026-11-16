
package com.example.brawlstarscoach

import android.media.Image
import kotlin.math.abs

data class FrameAnalysis(
    val motionScore: Float,
    val sampleCount: Int
)

class FrameAnalyzer {

    private var previousFrame: ByteArray? = null

    @Synchronized
    fun analyze(image: Image): FrameAnalysis {

        val plane = image.planes[0]
        val buffer = plane.buffer
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride

        val width = image.width
        val height = image.height

        val stepX = (width / 64).coerceAtLeast(1)
        val stepY = (height / 64).coerceAtLeast(1)

        val samples = ArrayList<Byte>()

        var y = 0

        while (y < height) {
            var x = 0

            while (x < width) {
                val index =
                    y * rowStride + x * pixelStride

                if (index < buffer.limit()) {
                    samples.add(buffer.get(index))
                }

                x += stepX
            }

            y += stepY
        }

        val current = ByteArray(samples.size) {
            samples[it]
        }

        val previous = previousFrame
        previousFrame = current

        if (previous == null ||
            previous.size != current.size
        ) {
            return FrameAnalysis(0f, current.size)
        }

        var difference = 0L

        for (i in current.indices) {
            difference += abs(
                (current[i].toInt() and 0xFF) -
                (previous[i].toInt() and 0xFF)
            )
        }

        val score = if (current.isNotEmpty()) {
            difference.toFloat() /
                current.size.toFloat() / 255f
        } else {
            0f
        }

        return FrameAnalysis(
            motionScore = score.coerceIn(0f, 1f),
            sampleCount = current.size
        )
    }

    @Synchronized
    fun reset() {
        previousFrame = null
    }
}
