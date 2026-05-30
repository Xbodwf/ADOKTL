package com.adoktl.util

/**
 * Large file parser for ADOFAI level JSON data.
 * Memory-optimized: processes buffer in-place without copies.
 * Handles trailing commas found in ADOFAI exports.
 * Port of LargeFileParser.ts from Re_ADOJAS.
 */
object LargeFileParser {

    /**
     * Parse angle data array from a large JSON buffer.
     * Handles trailing commas and efficiently extracts number arrays.
     */
    fun parseNumberArray(buffer: ByteArray, startPos: Int = 0): List<Double> {
        val values = mutableListOf<Double>()
        var i = skipWhitespace(buffer, startPos)
        if (i >= buffer.size || buffer[i] != '['.code.toByte()) return values
        i++ // skip '['
        i = skipWhitespace(buffer, i)

        val sb = StringBuilder()
        while (i < buffer.size) {
            val b = buffer[i].toInt().toChar()
            when {
                b == ']' -> {
                    if (sb.isNotEmpty()) {
                        sb.toString().toDoubleOrNull()?.let { values.add(it) }
                        sb.clear()
                    }
                    i++
                    return values
                }
                b == ',' -> {
                    if (sb.isNotEmpty()) {
                        sb.toString().toDoubleOrNull()?.let { values.add(it) }
                        sb.clear()
                    }
                    i++
                }
                b == ' ' || b == '\t' || b == '\n' || b == '\r' -> {
                    i++
                }
                else -> {
                    sb.append(b)
                    i++
                }
            }
        }

        if (sb.isNotEmpty()) {
            sb.toString().toDoubleOrNull()?.let { values.add(it) }
        }

        return values
    }

    /**
     * Find a JSON property value position in the buffer.
     * Only searches at root level (depth=0).
     */
    fun findProperty(buffer: ByteArray, propertyName: String): Int? {
        var depth = 0
        var inString = false
        var escapeNext = false
        var currentKey = ""
        var keyStart = -1
        var foundKeyAtDepth = -1

        var i = 0
        while (i < buffer.size) {
            val byte = buffer[i].toInt() and 0xFF

            if (escapeNext) {
                escapeNext = false
                i++
                continue
            }

            if (byte == 0x5C) { // backslash
                escapeNext = true
                i++
                continue
            }

            if (byte == 0x22) { // quote
                if (inString) {
                    inString = false
                    if (depth == 0 && keyStart >= 0) {
                        currentKey = buffer.decodeToString(keyStart, i)
                        foundKeyAtDepth = 0
                    }
                } else {
                    inString = true
                    keyStart = i + 1
                }
                i++
                continue
            }

            if (!inString) {
                when (byte) {
                    0x7B -> depth++ // {
                    0x7D -> depth-- // }
                    0x3A -> { // :
                        if (foundKeyAtDepth == 0 && currentKey == propertyName) {
                            i = skipWhitespace(buffer, i + 1)
                            return i
                        }
                        foundKeyAtDepth = -1
                        currentKey = ""
                    }
                }
            }

            i++
        }

        return null
    }

    /**
     * Extract a string property value (including surrounding quotes).
     */
    fun extractStringProperty(buffer: ByteArray, propertyName: String): String? {
        val start = findProperty(buffer, propertyName) ?: return null
        if (start >= buffer.size || buffer[start].toInt().toChar() != '"') return null

        var end = start + 1
        var escapeNext = false
        while (end < buffer.size) {
            val byte = buffer[end].toInt() and 0xFF
            if (escapeNext) {
                escapeNext = false
                end++
                continue
            }
            if (byte == 0x5C) { escapeNext = true; end++; continue }
            if (byte == 0x22) { end++; break }
            end++
        }

        return buffer.decodeToString(start, end)
    }

    /**
     * Extract a numeric property value.
     */
    fun extractNumberProperty(buffer: ByteArray, propertyName: String): Double? {
        val start = findProperty(buffer, propertyName) ?: return null
        val sb = StringBuilder()
        var i = start
        while (i < buffer.size) {
            val c = buffer[i].toInt().toChar()
            if (c == ',' || c == '}' || c == ']' || c == ' ') break
            sb.append(c)
            i++
        }
        return sb.toString().toDoubleOrNull()
    }

    /**
     * Extract a number array property efficiently.
     */
    fun extractNumberArrayProperty(buffer: ByteArray, propertyName: String): List<Double> {
        val start = findProperty(buffer, propertyName) ?: return emptyList()
        return parseNumberArray(buffer, start)
    }

    /**
     * BOM detection and stripping for UTF-8 files.
     */
    fun stripBOM(buffer: ByteArray): ByteArray {
        return if (buffer.size >= 3 &&
            buffer[0] == 0xEF.toByte() &&
            buffer[1] == 0xBB.toByte() &&
            buffer[2] == 0xBF.toByte()
        ) {
            buffer.copyOfRange(3, buffer.size)
        } else {
            buffer
        }
    }

    private fun skipWhitespace(buffer: ByteArray, pos: Int): Int {
        var i = pos
        while (i < buffer.size) {
            val b = buffer[i].toInt()
            if (b != 0x20 && b != 0x09 && b != 0x0A && b != 0x0D) break
            i++
        }
        return i
    }
}