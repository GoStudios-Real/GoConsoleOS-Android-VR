package com.gostudios.console.vr.headset

import android.content.Context
import android.content.SharedPreferences
import android.opengl.Matrix
import android.util.SizeF
import kotlin.math.*

/**
 * GoStudios Cardboard VR Headset configuration.
 * Works with ANY cardboard/plastic VR headset holder.
 * No Google Cardboard SDK dependency.
 */
class GoStudiosHeadset(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("gostudios_headset", Context.MODE_PRIVATE)

    // Interpupillary distance (typical: 60-68mm)
    var ipd: Float = prefs.getFloat("ipd", 63f)
        set(value) { field = value.coerceIn(40f, 80f); save() }

    // Screen-to-lens distance (depends on headset)
    var screenToLens: Float = prefs.getFloat("screen_to_lens", 40f)
        set(value) { field = value.coerceIn(20f, 80f); save() }

    // Lens field of view (depends on headset lens)
    var lensFov: Float = prefs.getFloat("lens_fov", 90f)
        set(value) { field = field.coerceIn(60f, 120f); save() }

    // Barrel distortion coefficients (for lens correction)
    var distortionK1: Float = prefs.getFloat("dist_k1", 0.441f)
    var distortionK2: Float = prefs.getFloat("dist_k2", 0.156f)

    // Device-specific calibration
    var screenWidth: Float = prefs.getFloat("screen_w", 1080f)
    var screenHeight: Float = prefs.getFloat("screen_h", 1920f)
    var screenDensity: Float = prefs.getFloat("density", 2.75f)

    // Headset model name
    var headsetModel: String = prefs.getString("model", "GoStudios Cardboard") ?: "GoStudios Cardboard"

    // Predefined headset profiles
    enum class HeadsetProfile(val displayName: String, val ipd: Float, val screenToLens: Float, val fov: Float) {
        GOSTUDIOS_CARDBOARD("GoStudios Cardboard", 63f, 40f, 90f),
        GOSTUDIOS_PRO("GoStudios Pro VR", 63f, 35f, 100f),
        GOSTUDIOS_LITE("GoStudios Lite", 65f, 45f, 80f),
        UNIVERSAL_CARDBOARD("Universal Cardboard", 64f, 42f, 85f),
        CUSTOM("Custom", 63f, 40f, 90f);

        fun applyTo(headset: GoStudiosHeadset) {
            headset.ipd = ipd
            headset.screenToLens = screenToLens
            headset.lensFov = fov
            headset.headsetModel = displayName
        }
    }

    private fun save() {
        prefs.edit()
            .putFloat("ipd", ipd)
            .putFloat("screen_to_lens", screenToLens)
            .putFloat("lens_fov", lensFov)
            .putFloat("dist_k1", distortionK1)
            .putFloat("dist_k2", distortionK2)
            .putFloat("screen_w", screenWidth)
            .putFloat("screen_h", screenHeight)
            .putFloat("density", screenDensity)
            .putString("model", headsetModel)
            .apply()
    }

    fun applyProfile(profile: HeadsetProfile) {
        profile.applyTo(this)
    }

    fun getLeftProjectionMatrix(aspectRatio: Float): FloatArray {
        val proj = FloatArray(16)
        val fovRad = Math.toRadians(lensFov.toDouble() / 2.0).toFloat()
        val near = 0.1f
        val far = 100f
        Matrix.perspectiveM(proj, 0, lensFov, aspectRatio, near, far)
        return proj
    }

    fun getRightProjectionMatrix(aspectRatio: Float): FloatArray {
        return getLeftProjectionMatrix(aspectRatio)
    }

    fun getInterpupillaryDistanceMeters(): Float = ipd / 1000f

    fun getBarrelDistortionScale(): Float {
        val r = 1.0f
        val r2 = r * r
        return 1f + distortionK1 * r2 + distortionK2 * r2 * r2
    }

    fun detectDevice(): DeviceInfo {
        val dm = context.resources.displayMetrics
        screenWidth = dm.widthPixels.toFloat()
        screenHeight = dm.heightPixels.toFloat()
        screenDensity = dm.density
        return DeviceInfo(
            manufacturer = android.os.Build.MANUFACTURER,
            model = android.os.Build.MODEL,
            sdk = android.os.Build.VERSION.SDK_INT,
            screenW = screenWidth,
            screenH = screenHeight,
            density = screenDensity
        )
    }

    fun isNokiaDevice(): Boolean {
        val model = android.os.Build.MODEL.lowercase()
        return model.contains("nokia") || model.contains("g22") || model.contains("g21") ||
               model.contains("g20") || model.contains("g10") || model.contains("g50")
    }

    data class DeviceInfo(
        val manufacturer: String,
        val model: String,
        val sdk: Int,
        val screenW: Float,
        val screenH: Float,
        val density: Float
    )

    companion object {
        private var instance: GoStudiosHeadset? = null

        fun getInstance(context: Context): GoStudiosHeadset {
            if (instance == null) instance = GoStudiosHeadset(context.applicationContext)
            return instance!!
        }
    }
}
