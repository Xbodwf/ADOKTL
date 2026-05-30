package com.adoktl.render

import com.adoktl.math.AdoktlColor
import com.adoktl.math.EasingFunctions

/**
 * Flash effect for screen-wide color transitions.
 * Manages FG and BG flash layers with easing.
 */
class FlashEffect {
    private var _enabled: Boolean = true

    private var fgFlash = FlashLayer()
    private var bgFlash = FlashLayer()

    val isEnabled: Boolean get() = _enabled
    fun setEnabled(enabled: Boolean) { _enabled = enabled }

    val isActive: Boolean get() = fgFlash.active || bgFlash.active
    val isFGActive: Boolean get() = fgFlash.active
    val isBGActive: Boolean get() = bgFlash.active

    fun getFGOpacity(): Float = fgFlash.currentOpacity
    fun getBGOpacity(): Float = bgFlash.currentOpacity

    fun startFlash(
        currentTime: Double,
        duration: Double,
        startColor: AdoktlColor,
        endColor: AdoktlColor,
        startOpacity: Float,
        endOpacity: Float,
        ease: String,
        plane: FlashPlane
    ) {
        val layer = when (plane) {
            FlashPlane.FG -> fgFlash
            FlashPlane.BG -> bgFlash
        }
        layer.start(currentTime, duration, startColor, endColor, startOpacity, endOpacity, ease)
    }

    fun update(currentTime: Double) {
        fgFlash.update(currentTime)
        bgFlash.update(currentTime)
    }

    fun stop() {
        fgFlash.reset()
        bgFlash.reset()
    }

    fun reset() {
        fgFlash = FlashLayer()
        bgFlash = FlashLayer()
    }

    fun dispose() {
        _enabled = false
        stop()
    }
}

enum class FlashPlane { FG, BG }

class FlashLayer {
    var active: Boolean = false
        private set
    var currentOpacity: Float = 0f
        private set
    var currentColor: AdoktlColor = AdoktlColor(1f, 1f, 1f, 1f)
        private set

    private var startTime: Double = 0.0
    private var duration: Double = 0.0
    private var startColor: AdoktlColor = AdoktlColor(1f, 1f, 1f, 1f)
    private var endColor: AdoktlColor = AdoktlColor(0f, 0f, 0f, 1f)
    private var startOpacity: Float = 0f
    private var endOpacity: Float = 0f
    private var ease: String = "Linear"

    fun start(
        startTime: Double,
        duration: Double,
        startColor: AdoktlColor,
        endColor: AdoktlColor,
        startOpacity: Float,
        endOpacity: Float,
        ease: String
    ) {
        this.active = true
        this.startTime = startTime
        this.duration = duration
        this.startColor = startColor
        this.endColor = endColor
        this.startOpacity = startOpacity
        this.endOpacity = endOpacity
        this.ease = ease

        this.currentColor = startColor
        this.currentOpacity = startOpacity
    }

    fun update(currentTime: Double) {
        if (!active) return

        val elapsed = currentTime - startTime
        val t = if (duration > 0.0) (elapsed / duration).coerceIn(0.0, 1.0) else 1.0

        val easeFunc = EasingFunctions.get(ease)
        val progress = easeFunc(t).toFloat()

        currentColor = currentColor.lerpTo(endColor, progress)
        currentOpacity = startOpacity + (endOpacity - startOpacity) * progress

        if (t >= 1.0) {
            active = false
            currentOpacity = 0f
        }
    }

    fun reset() {
        active = false
        currentOpacity = 0f
        currentColor = AdoktlColor(1f, 1f, 1f, 1f)
        startColor = AdoktlColor(1f, 1f, 1f, 1f)
        endColor = AdoktlColor(0f, 0f, 0f, 1f)
        startOpacity = 0f
        endOpacity = 0f
        ease = "Linear"
    }
}