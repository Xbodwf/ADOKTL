package com.adoktl.engine

import com.adoktl.level.LevelData
import com.adoktl.math.EasingFunctions
import com.adoktl.math.Vector3

class MoveTrackEngine(private val levelData: LevelData) {

    data class MoveTrackTarget(
        val startTime: Double,
        val duration: Double,
        val easingFunc: (Double) -> Double,
        val targets: Map<String, Double>
    )

    data class ActiveAnimation(
        val property: String,
        val startValue: Double,
        val endValue: Double,
        val startTime: Double,
        val duration: Double,
        val easingFunc: (Double) -> Double
    )

    private val eventTimeline = mutableListOf<Pair<Double, Map<String, Any?>>>()
    private val pendingTargets = mutableMapOf<Int, MutableList<MoveTrackTarget>>()
    private val activeAnimations = mutableMapOf<Int, MutableList<ActiveAnimation>>()
    private var lastEventIndex = -1

    private val tileStates = mutableMapOf<Int, TileMoveState>()

    data class TileMoveState(
        var position: Vector3 = Vector3.ZERO,
        var rotation: Double = 0.0,
        var scale: Vector3 = Vector3(1.0, 1.0, 1.0),
        var opacity: Double = 1.0
    )

    fun initEvents(events: Map<Int, List<Map<String, Any?>>>, tileStartTimes: DoubleArray) {
        eventTimeline.clear()
        val entries = mutableListOf<Pair<Double, Map<String, Any?>>>()

        for ((floor, evts) in events) {
            val tileStart = tileStartTimes.getOrElse(floor) { 0.0 }
            for (event in evts) {
                if (!EventUtils.isEventActive(event)) continue
                val angleOffset = (event["angleOffset"] as? Number)?.toDouble() ?: 0.0
                val timeOffset = (angleOffset / 180.0)
                entries.add(Pair(tileStart + timeOffset, event + mapOf("floor" to floor)))
            }
        }

        entries.sortBy { it.first }
        eventTimeline.addAll(entries)
    }

    fun update(elapsedTime: Double) {
        processEvents(elapsedTime)
        updateAnimations(elapsedTime)
    }

    private fun processEvents(elapsedTime: Double) {
        var idx = lastEventIndex
        while (idx + 1 < eventTimeline.size && eventTimeline[idx + 1].first <= elapsedTime) {
            idx++
            val (time, event) = eventTimeline[idx]
            val floor = (event["floor"] as? Number)?.toInt() ?: 0
            val duration = (event["duration"] as? Number)?.toDouble() ?: 0.0
            val easeName = (event["ease"] as? String) ?: "Linear"
            val easingFunc = EasingFunctions.get(easeName)

            val targets = mutableMapOf<String, Double>()

            val positionOffset = event["positionOffset"] as? List<*>
            if (positionOffset != null && positionOffset.size >= 2) {
                val px = (positionOffset[0] as? Number)?.toDouble()
                val py = (positionOffset[1] as? Number)?.toDouble()
                if (px != null) targets["positionX"] = px
                if (py != null) targets["positionY"] = py
            }

            val rotation = event["rotation"] as? Number
            if (rotation != null) targets["rotationZ"] = rotation.toDouble()

            val scale = event["scale"] as? List<*>
            if (scale != null && scale.size >= 2) {
                val sx = (scale[0] as? Number)?.toDouble()
                val sy = (scale[1] as? Number)?.toDouble()
                if (sx != null) targets["scaleX"] = sx
                if (sy != null) targets["scaleY"] = sy
            }

            val opacity = event["opacity"] as? Number
            if (opacity != null) targets["opacity"] = opacity.toDouble() / 100.0

            if (targets.isNotEmpty()) {
                pendingTargets.getOrPut(floor) { mutableListOf() }
                    .add(MoveTrackTarget(time, duration, easingFunc, targets))
            }
        }
        lastEventIndex = idx
    }

    private fun updateAnimations(elapsedTime: Double) {
        for ((floor, pending) in pendingTargets.toList()) {
            val state = tileStates.getOrPut(floor) { TileMoveState() }

            for (target in pending) {
                val progress = ((elapsedTime - target.startTime) / target.duration).coerceIn(0.0, 1.0)

                for ((prop, endValue) in target.targets) {
                    val startValue = getProperty(state, prop)

                    if (progress >= 1.0) {
                        setProperty(state, prop, endValue)
                    } else {
                        val eased = target.easingFunc(progress)
                        val currentValue = startValue + (endValue - startValue) * eased
                        setProperty(state, prop, currentValue)
                    }
                }
            }

            pendingTargets[floor]?.removeAll { target ->
                (elapsedTime - target.startTime) >= target.duration
            }
            if (pendingTargets[floor]?.isEmpty() == true) {
                pendingTargets.remove(floor)
            }
        }
    }

    private fun getProperty(state: TileMoveState, prop: String): Double {
        return when (prop) {
            "positionX" -> state.position.x
            "positionY" -> state.position.y
            "rotationZ" -> state.rotation
            "scaleX" -> state.scale.x
            "scaleY" -> state.scale.y
            "opacity" -> state.opacity
            else -> 0.0
        }
    }

    private fun setProperty(state: TileMoveState, prop: String, value: Double) {
        when (prop) {
            "positionX" -> state.position = Vector3(value, state.position.y, state.position.z)
            "positionY" -> state.position = Vector3(state.position.x, value, state.position.z)
            "rotationZ" -> state.rotation = value
            "scaleX" -> state.scale = Vector3(value, state.scale.y, state.scale.z)
            "scaleY" -> state.scale = Vector3(state.scale.x, value, state.scale.z)
            "opacity" -> state.opacity = value
        }
    }

    fun getTileState(index: Int): TileMoveState? = tileStates[index]
}