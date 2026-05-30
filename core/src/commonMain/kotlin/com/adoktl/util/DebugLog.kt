package com.adoktl.util

/**
 * Debug logging utility.
 * Port of DebugLog.ts from Re_ADOJAS.
 * Maintains a ring buffer of log entries.
 */
object DebugLog {

    private const val MAX_LOGS = 500
    private val logBuffer = ArrayDeque<String>(MAX_LOGS)
    private var _visible = false

    val isVisible: Boolean get() = _visible
    val logs: List<String> get() = logBuffer.toList()

    fun show() { _visible = true }
    fun hide() { _visible = false }
    fun toggle() { _visible = !_visible }

    fun log(vararg args: Any?) {
        val msg = args.joinToString(" ") { arg ->
            when (arg) {
                null -> "null"
                is Map<*, *> -> arg.entries.joinToString(", ", "{", "}") { "${it.key}=${it.value}" }
                is List<*> -> arg.joinToString(", ", "[", "]")
                is FloatArray -> arg.joinToString(", ", "[", "]")
                is DoubleArray -> arg.joinToString(", ", "[", "]")
                is IntArray -> arg.joinToString(", ", "[", "]")
                else -> arg.toString()
            }
        }
        logBuffer.addLast(msg)
        if (logBuffer.size > MAX_LOGS) {
            logBuffer.removeFirst()
        }
        println("[ADOKTL] $msg")
    }

    fun clear() {
        logBuffer.clear()
    }

    fun getLogText(): String = logBuffer.joinToString("\n")

    fun exportLogs(): String = buildString {
        appendLine("=== ADOKTL Debug Log ===")
        appendLine("Timestamp: ${currentTimeMillis()}")
        appendLine("Entries: ${logBuffer.size}")
        appendLine("---")
        for (entry in logBuffer) {
            appendLine(entry)
        }
    }
}