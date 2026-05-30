package com.adoktl.render

import com.adoktl.math.AdoktlColor

/**
 * Bloom post-processing effect.
 * Extracts bright areas, applies gaussian blur, and composites back.
 */
class BloomEffect {
    private var _enabled: Boolean = false
    private var _threshold: Float = 0.5f
    private var _intensity: Float = 0.7f
    private var _quality: Int = 1
    private var _bloomColor: AdoktlColor = AdoktlColor(1f, 1f, 1f, 1f)
    private var _resolutionWidth: Int = 512
    private var _resolutionHeight: Int = 512

    val isEnabled: Boolean get() = _enabled

    fun setEnabled(enabled: Boolean) { _enabled = enabled }

    fun setThreshold(threshold: Float) {
        _threshold = threshold.coerceIn(0f, 1f)
    }
    fun getThreshold(): Float = _threshold

    fun setIntensity(intensity: Float) { _intensity = intensity }
    fun getIntensity(): Float = _intensity

    fun setQuality(quality: Int) { _quality = if (quality == 0) 0 else 1 }
    fun getQuality(): Int = _quality

    fun setColor(color: AdoktlColor) { _bloomColor = color }
    fun getColor(): AdoktlColor = _bloomColor

    fun setSize(width: Int, height: Int) {
        _resolutionWidth = width
        _resolutionHeight = height
    }

    /**
     * Apply bloom to a rendered frame.
     * @param backend The render backend to use
     * @param sourceTexture ID of the source framebuffer texture
     * @param targetFramebuffer ID of the target framebuffer (null for screen)
     */
    fun render(
        backend: RenderBackendApi,
        sourceTexture: Int,
        targetFramebuffer: Int? = null
    ) {
        if (!_enabled) return

        val div = if (_quality > 0) 2 else 4
        val blurW = _resolutionWidth / div
        val blurH = _resolutionHeight / div

        val brightnessRt = backend.createTexture(_resolutionWidth, _resolutionHeight, ByteArray(_resolutionWidth * _resolutionHeight * 4))
        val blurHRt = backend.createTexture(blurW, blurH, ByteArray(blurW * blurH * 4))
        val blurVRt = backend.createTexture(blurW, blurH, ByteArray(blurW * blurH * 4))

        renderBrightnessPass(backend, sourceTexture, brightnessRt)
        renderBlurPass(backend, brightnessRt, blurHRt, horizontal = true, blurW, blurH)
        renderBlurPass(backend, blurHRt, blurVRt, horizontal = false, blurW, blurH)
        renderCombinePass(backend, sourceTexture, blurVRt, targetFramebuffer)

        backend.deleteTexture(brightnessRt)
        backend.deleteTexture(blurHRt)
        backend.deleteTexture(blurVRt)
    }

    private fun renderBrightnessPass(backend: RenderBackendApi, source: Int, target: Int) {
        val brightnessVerts = buildFullScreenQuad(
            threshold = _threshold,
            mode = "brightness"
        )
        backend.drawMesh(brightnessVerts)
    }

    private fun renderBlurPass(
        backend: RenderBackendApi,
        source: Int,
        target: Int,
        horizontal: Boolean,
        width: Int,
        height: Int
    ) {
        val dirX = if (horizontal) 1f else 0f
        val dirY = if (horizontal) 0f else 1f
        val blurVerts = buildFullScreenQuad(
            blurDirectionX = dirX,
            blurDirectionY = dirY,
            blurResolutionX = width.toFloat(),
            blurResolutionY = height.toFloat(),
            quality = _quality.toFloat(),
            mode = "blur"
        )
        backend.drawMesh(blurVerts)
    }

    private fun renderCombinePass(
        backend: RenderBackendApi,
        source: Int,
        bloom: Int,
        target: Int?
    ) {
        val combineVerts = buildFullScreenQuad(
            intensity = _intensity,
            bloomColor = _bloomColor,
            mode = "combine"
        )
        backend.drawMesh(combineVerts)
    }

    private fun buildFullScreenQuad(
        threshold: Float = 0.5f,
        intensity: Float = 1.0f,
        blurDirectionX: Float = 1f,
        blurDirectionY: Float = 0f,
        blurResolutionX: Float = 256f,
        blurResolutionY: Float = 256f,
        quality: Float = 1f,
        bloomColor: AdoktlColor = AdoktlColor(1f, 1f, 1f, 1f),
        mode: String = "brightness"
    ): Mesh {
        val verts = floatArrayOf(
            -1f, -1f, 0f, 1f, 1f, 1f, 1f, 1f, 1f, 1f,
            1f, -1f, 0f, 1f, 1f, 1f, 1f, 1f, 1f, 1f,
            -1f, 1f, 0f, 1f, 1f, 1f, 1f, 1f, 1f, 1f,
            1f, 1f, 0f, 1f, 1f, 1f, 1f, 1f, 1f, 1f
        )
        val idx = intArrayOf(0, 1, 2, 1, 3, 2)
        return Mesh(verts, idx)
    }

    fun dispose() {
        _enabled = false
    }

    companion object {
        private const val MAX_TEXTURE_SIZE = 4096
    }
}