package com.adoktl.render

import com.adoktl.math.AdoktlColor
import com.adoktl.math.Vector2

/**
 * Instance data for a single tile
 */
data class TileInstanceData(
    val index: Int,
    val shapeKey: String,
    var position: Vector2,
    var rotation: Double,
    var scaleX: Double,
    var scaleY: Double,
    var color: AdoktlColor,
    var bgColor: AdoktlColor,
    var opacity: Float,
    val texSeed: Int,
    var visible: Boolean = true
)

/**
 * Shape-instanced mesh data
 */
data class ShapeInstancedData(
    val shapeKey: String,
    val tileToInstance: MutableMap<Int, Int> = mutableMapOf(),
    var instanceCount: Int = 0,
    var maxInstances: Int = 0,
    var minTileIndex: Int = Int.MAX_VALUE
)

/**
 * Manager for GPU instanced mesh rendering.
 * Groups tiles by shape and renders them in batch.
 * Port of InstancedMeshManager.ts from Re_ADOJAS.
 */
class InstancedMeshManager(
    private val backend: RenderBackendApi,
    private val onGeometryNeeded: (shapeKey: String) -> Mesh,
    private val useInstancedMesh: Boolean = true
) {
    private val shapeInstances = mutableMapOf<String, ShapeInstancedData>()
    private val tileInstances = mutableMapOf<Int, TileInstanceData>()
    private val geometryCache = mutableMapOf<String, Mesh>()

    private val BATCH_SIZE = 100

    fun setUseInstanced(use: Boolean) {
        // Note: instance rendering support depends on backend capabilities
    }

    fun updateTile(
        tileIndex: Int,
        shapeKey: String,
        position: Vector2,
        rotation: Double,
        scaleX: Double,
        scaleY: Double,
        color: AdoktlColor,
        bgColor: AdoktlColor,
        opacity: Float = 1f,
        visible: Boolean = true,
        texSeed: Int = 0
    ) {
        if (!useInstancedMesh) return

        val existingInstance = tileInstances[tileIndex]
        if (existingInstance != null && existingInstance.shapeKey != shapeKey) {
            val oldShapeData = shapeInstances[existingInstance.shapeKey]
            oldShapeData?.tileToInstance?.remove(tileIndex)
        }

        val instance = TileInstanceData(
            index = tileIndex,
            shapeKey = shapeKey,
            position = position,
            rotation = rotation,
            scaleX = scaleX,
            scaleY = scaleY,
            color = color,
            bgColor = bgColor,
            opacity = opacity,
            texSeed = texSeed,
            visible = visible
        )
        tileInstances[tileIndex] = instance

        val shapeData = shapeInstances.getOrPut(shapeKey) {
            val geometry = onGeometryNeeded(shapeKey)
            geometryCache[shapeKey] = geometry
            ShapeInstancedData(shapeKey = shapeKey, maxInstances = BATCH_SIZE)
        }

        var instanceIndex = shapeData.tileToInstance[tileIndex]
        if (instanceIndex == null) {
            if (shapeData.instanceCount >= shapeData.maxInstances) {
                expandShape(shapeData)
            }
            instanceIndex = shapeData.instanceCount
            shapeData.tileToInstance[tileIndex] = instanceIndex
            shapeData.instanceCount++

            if (tileIndex < shapeData.minTileIndex) {
                shapeData.minTileIndex = tileIndex
            }
        }
    }

    fun updateTileTransform(
        tileIndex: Int,
        position: Vector2,
        rotation: Double,
        scaleX: Double,
        scaleY: Double,
        opacity: Float? = null
    ) {
        val instance = tileInstances[tileIndex] ?: return

        val posChanged = instance.position != position
        val rotChanged = instance.rotation != rotation
        val scaleChanged = instance.scaleX != scaleX || instance.scaleY != scaleY
        val opacityChanged = opacity != null && kotlin.math.abs(instance.opacity - opacity) > 1e-6f

        if (!posChanged && !rotChanged && !scaleChanged && !opacityChanged) return

        if (posChanged) instance.position = position
        if (rotChanged) instance.rotation = rotation
        if (scaleChanged) { instance.scaleX = scaleX; instance.scaleY = scaleY }
        if (opacityChanged) instance.opacity = opacity!!
    }

    fun updateTileColor(tileIndex: Int, color: AdoktlColor, bgColor: AdoktlColor) {
        val instance = tileInstances[tileIndex] ?: return
        val colorChanged = instance.color != color
        val bgColorChanged = instance.bgColor != bgColor
        if (!colorChanged && !bgColorChanged) return
        if (colorChanged) instance.color = color
        if (bgColorChanged) instance.bgColor = bgColor
    }

    fun setTileVisibility(tileIndex: Int, visible: Boolean) {
        val instance = tileInstances[tileIndex] ?: return
        if (instance.visible == visible) return
        instance.visible = visible
    }

    fun removeTile(tileIndex: Int) {
        val instance = tileInstances.remove(tileIndex) ?: return
        shapeInstances[instance.shapeKey]?.tileToInstance?.remove(tileIndex)
    }

    fun renderAll() {
        if (!useInstancedMesh) return

        for ((shapeKey, shapeData) in shapeInstances) {
            if (shapeData.instanceCount == 0) continue
            val geometry = geometryCache[shapeKey] ?: continue

            val positions = mutableListOf<Vector2>()
            val colors = mutableListOf<AdoktlColor>()

            for ((tileIdx, instIdx) in shapeData.tileToInstance) {
                val instance = tileInstances[tileIdx] ?: continue
                if (!instance.visible) continue
                positions.add(instance.position)
                colors.add(instance.color)
            }

            if (positions.isNotEmpty()) {
                backend.drawInstanced(geometry, positions, colors)
            }
        }
    }

    fun clear() {
        shapeInstances.clear()
        tileInstances.clear()
        geometryCache.clear()
    }

    private fun expandShape(shapeData: ShapeInstancedData) {
        val newMax = shapeData.maxInstances * 2
        shapeData.maxInstances = newMax
    }

    fun dispose() {
        clear()
    }
}