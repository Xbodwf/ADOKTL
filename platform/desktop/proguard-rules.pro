# ADOKTL ProGuard Rules (Desktop)

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Keep Compose runtime
-keep class androidx.compose.** { *; }

# Keep LWJGL
-keep class org.lwjgl.** { *; }
-keep class org.lwjgl.glfw.** { *; }
-keep class org.lwjgl.opengl.** { *; }

# Keep ADOKTL core
-keep class com.adoktl.** { *; }

# Keep serialization
-keep class kotlinx.serialization.** { *; }

# Dontwarn for missing native bindings
-dontwarn org.lwjgl.**
-dontwarn com.adoktl.platform.desktop.DesktopOpenGLBackend