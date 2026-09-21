package com.gostudios.console.vr.engine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.Matrix

class HeadTracker(context: Context) : SensorEventListener {
    private val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotVec = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val rotMat = FloatArray(16)
    private val adjRotMat = FloatArray(16)
    private val orient = FloatArray(3)
    private var yaw = 0f; private var pitch = 0f
    private var gyroX = 0f; private var gyroY = 0f
    private var useRotVec = true
    private var lastTs = 0L
    var sensitivity = 1.0f

    fun start() {
        if (rotVec != null) { sm.registerListener(this, rotVec, SensorManager.SENSOR_DELAY_GAME); useRotVec = true }
        else {
            accel?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
            gyro?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
            useRotVec = false
        }
    }
    fun stop() { sm.unregisterListener(this) }

    fun update(): Float {
        val now = System.nanoTime()
        val dt = if (lastTs > 0) (now - lastTs) / 1_000_000_000f else 0.016f
        lastTs = now
        if (!useRotVec) {
            yaw += gyroX * dt * sensitivity
            pitch = (pitch + gyroY * dt * sensitivity).coerceIn(-89f, 89f)
        }
        return dt
    }

    fun getViewMatrix(out: FloatArray) {
        if (useRotVec) {
            SensorManager.getRotationMatrixFromVector(rotMat, orient)
            SensorManager.remapCoordinateSystem(rotMat, SensorManager.AXIS_X, SensorManager.AXIS_Z, adjRotMat)
            Matrix.setLookAtM(out, 0, 0f,0f,0f, -adjRotMat[2],-adjRotMat[6],-adjRotMat[10], adjRotMat[1],adjRotMat[5],adjRotMat[9])
        } else {
            Matrix.setRotateM(out, 0, yaw, 0f, 1f, 0f)
            val t = FloatArray(16); Matrix.setRotateM(t, 0, pitch, 1f, 0f, 0f)
            Matrix.multiplyMM(out, 0, out, 0, t, 0)
        }
    }

    override fun onSensorChanged(e: SensorEvent) {
        when (e.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> System.arraycopy(e.values, 0, orient, 0, e.values.size.coerceAtMost(3))
            Sensor.TYPE_GYROSCOPE -> { gyroX = e.values[0]; gyroY = e.values[1] }
            Sensor.TYPE_ACCELEROMETER -> {
                val a = 0.98f
                val ax = e.values[0]; val ay = e.values[1]
                val accP = Math.toDegrees(kotlin.math.atan2(ay.toDouble(), kotlin.math.sqrt((ax*ax).toDouble()))).toFloat()
                pitch = a * pitch + (1-a) * accP
            }
        }
    }
    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
}
