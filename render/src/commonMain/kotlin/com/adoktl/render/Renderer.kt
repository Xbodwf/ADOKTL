package com.adoktl.render

import com.adoktl.math.Vector2
import com.adoktl.math.AdoktlColor

enum class RenderBackend {
    OPENGL, VULKAN
}

data class RenderConfig(
    val backend: RenderBackend = RenderBackend.OPENGL,
    val width: Int = 1920,
    val height: Int = 1080,
    val backgroundColor: AdoktlColor = AdoktlColor(0f, 0f, 0f, 1f),
    val clearColor: AdoktlColor = AdoktlColor(0f, 0f, 0f, 1f),
    val vsync: Boolean = true,
    val maxFps: Int = 60,
    val multisampling: Int = 4,
    val enableDebug: Boolean = false
)

data class RenderStats(
    val fps: Int = 60,
    val triangleCount: Int = 0,
    val drawCalls: Int = 0,
    val vertexCount: Int = 0,
    val backendInfo: String = ""
)

data class Vertex(
    val x: Float,
    val y: Float,
    val z: Float = 0f,
    val r: Float = 1f,
    val g: Float = 1f,
    val b: Float = 1f,
    val a: Float = 1f,
    val u: Float = 0f,
    val v: Float = 0f
)

data class Mesh(
    val vertices: FloatArray,
    val indices: IntArray,
    val colors: FloatArray? = null,
    val uvs: FloatArray? = null
) {
    fun vertexCount(): Int = vertices.size / 3
    fun indexCount(): Int = indices.size

    fun isEmpty(): Boolean = vertices.isEmpty() || indices.isEmpty()

    companion object {
        val EMPTY = Mesh(floatArrayOf(), intArrayOf())
    }
}

data class CameraData(
    val position: Vector2 = Vector2.ZERO,
    val zoom: Double = 100.0,
    val projectionRatio: Double = 16.0 / 9.0,
    val rotation: Double = 0.0
)

interface RenderBackendApi {
    val name: String
    fun init(config: RenderConfig): Boolean
    fun shutdown()
    fun beginFrame()
    fun endFrame()
    fun clear(color: AdoktlColor)
    fun setViewport(x: Int, y: Int, width: Int, height: Int)
    fun setCamera(camera: CameraData)

    fun drawMesh(mesh: Mesh)
    fun drawInstanced(mesh: Mesh, positions: List<Vector2>, colors: List<AdoktlColor>)

    fun createTexture(width: Int, height: Int, data: ByteArray): Int
    fun updateTexture(id: Int, width: Int, height: Int, data: ByteArray)
    fun deleteTexture(id: Int)

    fun pushScissor(x: Int, y: Int, width: Int, height: Int)
    fun popScissor()

    val stats: RenderStats
}

class Renderer(
    config: RenderConfig = RenderConfig(),
    backend: RenderBackendApi? = null
) {
    private var _backend: RenderBackendApi? = backend
    private var _config: RenderConfig = config
    private var _isInitialized = false

    val isInitialized: Boolean get() = _isInitialized
    val backend: RenderBackendApi? get() = _backend
    val stats: RenderStats get() = _backend?.stats ?: RenderStats()

    fun init(backend: RenderBackendApi, config: RenderConfig = _config): Boolean {
        _backend = backend
        _config = config
        _isInitialized = backend.init(config)
        return _isInitialized
    }

    fun shutdown() {
        _backend?.shutdown()
        _isInitialized = false
    }

    fun beginFrame() = _backend?.beginFrame()
    fun endFrame() = _backend?.endFrame()

    fun clear(color: AdoktlColor) = _backend?.clear(color)

    fun setCamera(position: Vector2, zoom: Double, rotation: Double = 0.0) {
        _backend?.setCamera(CameraData(position, zoom, rotation = rotation))
    }

    fun drawMesh(mesh: Mesh) = _backend?.drawMesh(mesh)

    fun drawTileMeshes(meshes: List<Mesh>) {
        for (mesh in meshes) {
            if (!mesh.isEmpty()) {
                _backend?.drawMesh(mesh)
            }
        }
    }

    fun setViewport(x: Int, y: Int, width: Int, height: Int) {
        _backend?.setViewport(x, y, width, height)
    }

    fun pushScissor(x: Int, y: Int, width: Int, height: Int) {
        _backend?.pushScissor(x, y, width, height)
    }

    fun popScissor() = _backend?.popScissor()
}