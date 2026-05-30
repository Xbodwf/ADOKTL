package com.adoktl.engine

import com.adoktl.level.LevelData
import com.adoktl.level.DecorationAction
import com.adoktl.math.Vector2
import com.adoktl.math.AdoktlColor

data class DecorationState(
    val tag: String = "",
    val type: String = "Image",
    val decorationImage: String = "",
    val text: String = "",
    val position: Vector2 = Vector2.ZERO,
    val positionOffset: Vector2 = Vector2.ZERO,
    val relativeTo: String = "Tile",
    val rotation: Double = 0.0,
    val rotationOffset: Double = 0.0,
    val scale: Vector2 = Vector2(100.0, 100.0),
    val parallax: Vector2 = Vector2(100.0, 100.0),
    val parallaxOffset: Vector2 = Vector2.ZERO,
    val depth: Double = 0.0,
    val color: AdoktlColor = AdoktlColor.WHITE,
    val opacity: Double = 100.0,
    val visible: Boolean = true
)

class DecorationEngine(private val levelData: LevelData) {

    private val decorations = mutableMapOf<String, DecorationState>()
    private val decorationTimeline = mutableListOf<Pair<Double, DecorationAction>>()
    private val moveDecorationTimeline = mutableListOf<Pair<Double, Map<String, Any?>>>()

    fun init() {
        decorations.clear()
        decorationTimeline.clear()
        moveDecorationTimeline.clear()

        for (deco in levelData.decorations) {
            when (deco.eventType) {
                "AddDecoration" -> {
                    val tag = deco.getString("tag", "deco_${deco.floor}")
                    decorations[tag] = parseDecorationState(deco)
                }
                "AddText" -> {
                    val tag = deco.getString("tag", "text_${deco.floor}")
                    decorations[tag] = parseDecorationState(deco).copy(type = "Text")
                }
                "AddObject" -> {
                    val tag = deco.getString("tag", "obj_${deco.floor}")
                    decorations[tag] = parseDecorationState(deco).copy(type = "Object")
                }
                "MoveDecorations" -> {
                    moveDecorationTimeline.add(Pair(0.0, deco.rawData))
                }
            }
        }

        decorationTimeline.sortBy { it.first }
        moveDecorationTimeline.sortBy { it.first }
    }

    private fun parseDecorationState(deco: DecorationAction): DecorationState {
        return DecorationState(
            tag = deco.getString("tag"),
            decorationImage = deco.getString("decorationImage"),
            text = deco.getString("decText"),
            position = parseVec2(deco.rawData["position"]),
            positionOffset = parseVec2(deco.rawData["positionOffset"]),
            relativeTo = deco.getString("relativeTo", "Tile"),
            rotation = deco.getDouble("rotation"),
            rotationOffset = deco.getDouble("rotationOffset"),
            scale = parseVec2(deco.rawData["scale"], Vector2(100.0, 100.0)),
            parallax = parseVec2(deco.rawData["parallax"], Vector2(100.0, 100.0)),
            parallaxOffset = parseVec2(deco.rawData["parallaxOffset"]),
            depth = deco.getDouble("depth"),
            color = AdoktlColor.fromHex(deco.getString("color", "ffffff")),
            opacity = deco.getDouble("opacity", 100.0),
            visible = deco.getBoolean("visible", true)
        )
    }

    private fun parseVec2(data: Any?, default: Vector2 = Vector2.ZERO): Vector2 {
        val list = data as? List<*> ?: return default
        val x = (list.getOrNull(0) as? Number)?.toDouble() ?: default.x
        val y = (list.getOrNull(1) as? Number)?.toDouble() ?: default.y
        return Vector2(x, y)
    }

    fun getDecorations(): Map<String, DecorationState> = decorations

    fun update(elapsedTime: Double, cameraPosition: Vector2, cameraRotation: Double, cameraZoom: Double) {
    }
}