package com.adoktl.render

import com.adoktl.math.AdoktlColor

/**
 * Icon sprite for event markers (Twirl, Speed+, Speed-, End).
 * Port of IconLoader.ts from Re_ADOJAS.
 */
object IconLoader {

    private val iconCache = mutableMapOf<IconType, IconData>()

    data class IconData(
        val type: IconType,
        val width: Int,
        val height: Int,
        val pixels: ByteArray
    )

    fun loadIcon(type: IconType): IconData {
        return iconCache.getOrPut(type) {
            generateIcon(type)
        }
    }

    fun getIconSprite(type: IconType, opacity: Float = 1f, targetHeight: Float = 0.22f): IconSpriteData {
        val icon = loadIcon(type)
        val aspect = icon.width.toFloat() / icon.height.toFloat()
        return IconSpriteData(
            iconData = icon,
            scaleX = targetHeight * aspect,
            scaleY = targetHeight,
            opacity = opacity
        )
    }

    fun getTwirlIcon(angle: Double, dir: Int): IconType {
        val red = angle < 180.0
        return if (red) {
            if (dir >= 0) IconType.TwirlR1 else IconType.TwirlRNeg1
        } else {
            if (dir >= 0) IconType.TwirlB1 else IconType.TwirlBNeg1
        }
    }

    fun getSetSpeedIcon(ratio: Double): IconType {
        return if (ratio > 1.05) IconType.SpeedPlus else IconType.SpeedMinus
    }

    private fun generateIcon(type: IconType): IconData {
        val size = 32
        val pixels = ByteArray(size * size * 4)

        when (type) {
            IconType.End -> generateEndIcon(pixels, size)
            IconType.SpeedPlus -> generateSpeedPlusIcon(pixels, size)
            IconType.SpeedMinus -> generateSpeedMinusIcon(pixels, size)
            IconType.TwirlB1, IconType.TwirlBNeg1 -> generateTwirlIcon(pixels, size, blue = true)
            IconType.TwirlR1, IconType.TwirlRNeg1 -> generateTwirlIcon(pixels, size, blue = false)
        }

        return IconData(type, size, size, pixels)
    }

    private fun generateEndIcon(pixels: ByteArray, size: Int) {
        val cx = size / 2
        val cy = size / 2
        for (y in 0 until size) {
            for (x in 0 until size) {
                val dx = x - cx
                val dy = y - cy
                val dist = kotlin.math.sqrt((dx * dx + dy * dy).toDouble())
                val idx = (y * size + x) * 4
                if (dist < size / 2 - 1) {
                    val angle = kotlin.math.atan2(dy.toDouble(), dx.toDouble())
                    val barWidth = 3
                    val bar = ((dx + cx) % (barWidth * 2) < barWidth)
                    val normDist = dist / (size / 2)
                    if (bar && normDist > 0.3) {
                        pixels[idx] = 255; pixels[idx + 1] = 255
                        pixels[idx + 2] = 255; pixels[idx + 3] = (255 * (1.0 - normDist * 0.3)).toInt().toByte()
                    } else {
                        pixels[idx] = 100; pixels[idx + 1] = 100
                        pixels[idx + 2] = 100; pixels[idx + 3] = (200 * (1.0 - normDist * 0.5)).toInt().toByte()
                    }
                }
            }
        }
    }

    private fun generateSpeedPlusIcon(pixels: ByteArray, size: Int) {
        val cx = size / 2
        val cy = size / 2
        for (y in 0 until size) {
            for (x in 0 until size) {
                val idx = (y * size + x) * 4
                val isVertical = kotlin.math.abs(x - cx) <= 2 && y in 4 until size - 4
                val isHorizontal = kotlin.math.abs(y - cy) <= 2 && x in 4 until size - 4
                if (isVertical || isHorizontal) {
                    pixels[idx] = 255; pixels[idx + 1] = 200
                    pixels[idx + 2] = 0; pixels[idx + 3] = 255
                }
            }
        }
    }

    private fun generateSpeedMinusIcon(pixels: ByteArray, size: Int) {
        val cx = size / 2
        for (y in 0 until size) {
            for (x in 0 until size) {
                val idx = (y * size + x) * 4
                val isHorizontal = kotlin.math.abs(y - cx) <= 2 && x in 4 until size - 4
                if (isHorizontal) {
                    pixels[idx] = 255; pixels[idx + 1] = 100
                    pixels[idx + 2] = 0; pixels[idx + 3] = 255
                }
            }
        }
    }

    private fun generateTwirlIcon(pixels: ByteArray, size: Int, blue: Boolean) {
        val cx = size / 2
        val cy = size / 2
        val r = if (blue) 50 else 200
        val g = 50
        val b = if (blue) 200 else 50
        for (y in 0 until size) {
            for (x in 0 until size) {
                val dx = x - cx
                val dy = y - cy
                val dist = kotlin.math.sqrt((dx * dx + dy * dy).toDouble())
                val idx = (y * size + x) * 4
                if (dist < size / 2 - 2 && dist > size / 4) {
                    val angle = kotlin.math.atan2(dy.toDouble(), dx.toDouble()) % (kotlin.math.PI / 2)
                    if (kotlin.math.abs(angle) < 0.3) {
                        pixels[idx] = r.toByte(); pixels[idx + 1] = g.toByte()
                        pixels[idx + 2] = b.toByte(); pixels[idx + 3] = 255
                    }
                }
            }
        }
    }
}

data class IconSpriteData(
    val iconData: IconLoader.IconData,
    val scaleX: Float,
    val scaleY: Float,
    val opacity: Float = 1f
)

enum class IconType {
    End, SpeedPlus, SpeedMinus, TwirlB1, TwirlBNeg1, TwirlR1, TwirlRNeg1
}