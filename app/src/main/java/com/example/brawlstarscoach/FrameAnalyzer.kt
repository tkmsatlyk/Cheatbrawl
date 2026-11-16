
package com.example.brawlstarscoach

import android.media.Image
import kotlin.math.abs

data class FrameAnalysis(
    val motionScore: Float,
    val sampleCount: Int
)

class FrameAnalyzer {

    private var previousFrame: IntArray? = null

    @Synchronized
    fun analyze(image: Image): FrameAnalysis {

        val plane = image.planes[0]
        val buffer = plane.buffer

        val width = image.width
        val height = image.height

        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride

        val stepX = (width / 96).coerceAtLeast(1)
        val stepY = (height / 96).coerceAtLeast(1)

        val columns = (width + stepX - 1) / stepX
        val rows = (height + stepY - 1) / stepY

        val current = IntArray(columns * rows)

        var sampleIndex = 0
        var y = 0

        while (y < height) {

            var x = 0

            while (x < width) {

                val index =
                    y * rowStride + x * pixelStride

                if (index + 2 < buffer.limit()) {

                    val r = buffer.get(index).toInt() and 0xFF
                    val g = buffer.get(index + 1).toInt() and 0xFF
                    val b = buffer.get(index + 2).toInt() and 0xFF

                    val gray =
                        (r * 299 + g * 587 + b * 114) / 1000

                    current[sampleIndex++] = gray
                }

                x += stepX
            }

            y += stepY
        }

        val previous = previousFrame
        previousFrame = current.copyOf(sampleIndex)

        if (previous == null ||
            previous.size != sampleIndex ||
            sampleIndex == 0
        ) {
            return FrameAnalysis(0f, sampleIndex)
        }

        var difference = 0L

        for (i in 0 until sampleIndex) {

            difference += abs(
                current[i] - previous[i]
            )
        }

        val score =
            difference.toFloat() /
            sampleIndex.toFloat() /
            255f

        return FrameAnalysis(
            motionScore = score.coerceIn(0f, 1f),
            sampleCount = sampleIndex
        )
    }

    @Synchronized
    fun reset() {
        previousFrame = null
    }
}
