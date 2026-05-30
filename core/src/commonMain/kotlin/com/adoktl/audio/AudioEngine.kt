package com.adoktl.audio

interface AudioEngine {
    fun loadMusic(path: String)
    fun play()
    fun playScheduled(whenTime: Double, offset: Double = 0.0)
    fun pause()
    fun stop()
    fun resume()
    fun seek(position: Double)
    val position: Double
    val audioTime: Double
    val duration: Double
    var volume: Double
    var pitch: Double
    val isPlaying: Boolean
    val isPaused: Boolean
    val hasAudio: Boolean
    val amplitude: Double
    fun dispose()
}

interface HitsoundEngine {
    suspend fun preSynthesize(groups: List<TimestampGroup>, totalDuration: Double, onProgress: ((Double) -> Unit)? = null)
    fun start(delay: Double = 0.0)
    fun startAtOffset(offset: Double)
    fun stop()
    val isEnabled: Boolean
    fun setEnabled(enabled: Boolean)
    val isSynthesized: Boolean
    val totalDuration: Double
    fun dispose()
}

data class TimestampGroup(
    val type: HitsoundType = HitsoundType.Kick,
    val volume: Int = 100,
    val timestamps: List<Double> = emptyList()
)

data class AudioConfig(
    val useOGGCompression: Boolean = false,
    val bufferSize: Int = 4096,
    val sampleRate: Int = 44100
)

enum class HitsoundType(val key: String) {
    Kick("sndKick"),
    KickHouse("sndKickHouse"),
    KickChroma("sndKickChroma"),
    KickRupture("sndKickRupture"),
    Snare("sndSnareAcoustic2"),
    SnareHouse("sndSnareHouse"),
    SnareVapor("sndSnareVapor"),
    Clap("sndClapHit"),
    ClapHit("sndClapHit"),
    ClapHitEcho("sndClapHitEcho"),
    Hat("sndHat"),
    HatHouse("sndHatHouse"),
    Chuck("sndChuck"),
    Hammer("sndHammer"),
    Shaker("sndShaker"),
    ShakerLoud("sndShakerLoud"),
    Sidestick("sndSidestick"),
    Stick("sndStick"),
    ReverbClack("sndReverbClack"),
    ReverbClap("sndReverbClap"),
    Squareshot("sndSquareshot"),
    FireTile("sndFireTile"),
    IceTile("sndIceTile"),
    PowerUp("sndPowerUp"),
    PowerDown("sndPowerDown"),
    VehiclePositive("sndVehiclePositive"),
    VehicleNegative("sndVehicleNegative"),
    Sizzle("sndSizzle"),
    None("");

    companion object {
        private val map = entries.associateBy { it.name }
        fun fromString(name: String): HitsoundType = map[name] ?: Kick
    }
}

data class HitsoundGroupData(
    val type: HitsoundType,
    val volume: Double,
    val timestamps: DoubleArray
)

data class MixBuffer(
    val channels: Int,
    val sampleRate: Int,
    val length: Int,
    val data: Array<FloatArray>
) {
    fun getDuration(): Double = length.toDouble() / sampleRate
}

fun softClip(x: Double): Double {
    val absX = if (x < 0) -x else x
    return when {
        absX < 0.5 -> x
        absX < 1.5 -> x * (1.0 - x * x / 3.0)
        else -> if (x < 0) -1.0 else 1.0
    }
}