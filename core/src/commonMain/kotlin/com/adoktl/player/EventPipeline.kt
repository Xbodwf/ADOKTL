package com.adoktl.player

import com.adoktl.level.LevelAction
import com.adoktl.level.LevelData

data class EventPipeline(
    val cameraEvents: Map<Int, List<Map<String, Any?>>>,
    val moveTrackEvents: Map<Int, List<Map<String, Any?>>>,
    val positionTrackEvents: Map<Int, List<Map<String, Any?>>>,
    val decorateEvents: Map<Int, List<Map<String, Any?>>>,
    val setSpeedEvents: Map<Int, List<Map<String, Any?>>>,
    val setHitsoundEvents: Map<Int, List<Map<String, Any?>>>,
    val twirlEvents: Map<Int, List<Map<String, Any?>>>,
    val otherEvents: Map<Int, List<Map<String, Any?>>>
) {
    companion object {
        fun fromLevel(data: LevelData): EventPipeline {
            val camera = mutableMapOf<Int, MutableList<Map<String, Any?>>>()
            val moveTrack = mutableMapOf<Int, MutableList<Map<String, Any?>>>()
            val positionTrack = mutableMapOf<Int, MutableList<Map<String, Any?>>>()
            val decorate = mutableMapOf<Int, MutableList<Map<String, Any?>>>()
            val setSpeed = mutableMapOf<Int, MutableList<Map<String, Any?>>>()
            val setHitsound = mutableMapOf<Int, MutableList<Map<String, Any?>>>()
            val twirl = mutableMapOf<Int, MutableList<Map<String, Any?>>>()
            val other = mutableMapOf<Int, MutableList<Map<String, Any?>>>()

            for (action in data.actions) {
                val floor = action.floor
                val map = action.rawData
                when (action.eventType) {
                    "CameraEvent", "CameraExtra" -> {
                        camera.getOrPut(floor) { mutableListOf() }.add(map)
                    }
                    "MoveTrack" -> {
                        moveTrack.getOrPut(floor) { mutableListOf() }.add(map)
                    }
                    "PositionTrack" -> {
                        positionTrack.getOrPut(floor) { mutableListOf() }.add(map)
                    }
                    "AddDecoration", "AddText", "AddObject", "MoveDecorations" -> {
                        decorate.getOrPut(floor) { mutableListOf() }.add(map)
                    }
                    "SetSpeed" -> {
                        setSpeed.getOrPut(floor) { mutableListOf() }.add(map)
                    }
                    "SetHitsound" -> {
                        setHitsound.getOrPut(floor) { mutableListOf() }.add(map)
                    }
                    "Twirl" -> {
                        twirl.getOrPut(floor) { mutableListOf() }.add(map)
                    }
                    else -> {
                        other.getOrPut(floor) { mutableListOf() }.add(map)
                    }
                }
            }

            return EventPipeline(
                cameraEvents = camera,
                moveTrackEvents = moveTrack,
                positionTrackEvents = positionTrack,
                decorateEvents = decorate,
                setSpeedEvents = setSpeed,
                setHitsoundEvents = setHitsound,
                twirlEvents = twirl,
                otherEvents = other
            )
        }
    }
}

enum class ProcessingPhase {
    TILE_GEOMETRY,
    CAMERA,
    POSITION_TRACK,
    MOVE_TRACK,
    PLANET,
    AUDIO,
    DECORATION
}