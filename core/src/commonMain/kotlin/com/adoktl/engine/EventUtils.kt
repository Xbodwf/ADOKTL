package com.adoktl.engine

object EventUtils {
    fun isEventActive(event: Map<String, Any?>): Boolean {
        if (event["active"] == false || event["active"] == "Disabled") return false
        val disabled = event["disabled"] as? Map<String, Any?>
        if (disabled?.get("active") == true) return false
        if (event["editorOnly"] == true) return false
        return true
    }

    fun isFieldEnabled(event: Map<String, Any?>, fieldName: String): Boolean {
        val disabled = event["disabled"] as? Map<String, Any?>
        return disabled?.get(fieldName) != true
    }

    fun getMovementType(raw: Any?): String {
        return when (raw) {
            is String -> raw
            is Number -> {
                val types = arrayOf("Player", "Tile", "Global", "LastPosition", "LastPositionNoRotation")
                types.getOrElse(raw.toInt()) { "Player" }
            }
            else -> "Player"
        }
    }
}