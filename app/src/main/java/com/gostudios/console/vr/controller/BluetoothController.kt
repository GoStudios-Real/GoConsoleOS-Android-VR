package com.gostudios.console.vr.controller

import android.content.Context
import android.graphics.PointF
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent

class BluetoothController {
    private var listener: Listener? = null
    private val controllers = mutableMapOf<Int, State>()

    interface Listener {
        fun onConnected(s: State) {}
        fun onDisconnected(s: State) {}
        fun onButton(s: State, btn: Int, pressed: Boolean) {}
        fun onStick(s: State, axis: Int, x: Float, y: Float) {}
        fun onTrigger(s: State, axis: Int, value: Float) {}
    }

    data class State(val id: Int, val name: String, val type: Type = Type.GENERIC, var connected: Boolean = true,
        var leftStick: PointF = PointF(0f,0f), var rightStick: PointF = PointF(0f,0f),
        var leftTrigger: Float = 0f, var rightTrigger: Float = 0f, var buttons: MutableMap<Int, Boolean> = mutableMapOf())

    enum class Type { XBOX, PS4, PS5, SWITCH, NOKIA, GENERIC }

    fun setListener(l: Listener?) { listener = l }

    fun onMotionEvent(e: MotionEvent): Boolean {
        val s = getOrCreate(e.deviceId)
        val lx = safe(e, MotionEvent.AXIS_X); val ly = safe(e, MotionEvent.AXIS_Y)
        if (kotlin.math.abs(lx) > 0.15f || kotlin.math.abs(ly) > 0.15f) { s.leftStick = PointF(lx, ly); listener?.onStick(s, 0, lx, ly) }
        val rx = safe(e, MotionEvent.AXIS_Z); val ry = safe(e, MotionEvent.AXIS_RZ)
        if (kotlin.math.abs(rx) > 0.15f || kotlin.math.abs(ry) > 0.15f) { s.rightStick = PointF(rx, ry); listener?.onStick(s, 1, rx, ry) }
        val lt = safe(e, MotionEvent.AXIS_LTRIGGER); val rt = safe(e, MotionEvent.AXIS_RTRIGGER)
        s.leftTrigger = lt; s.rightTrigger = rt
        listener?.onTrigger(s, 0, lt); listener?.onTrigger(s, 1, rt)
        return true
    }

    fun onKeyDown(code: Int, e: KeyEvent): Boolean {
        if (e.deviceId < 0) return false
        val s = getOrCreate(e.deviceId); s.buttons[code] = true; listener?.onButton(s, code, true); return true
    }

    fun onKeyUp(code: Int, e: KeyEvent): Boolean {
        if (e.deviceId < 0) return false
        val s = getOrCreate(e.deviceId); s.buttons[code] = false; listener?.onButton(s, code, false); return true
    }

    fun hasController() = controllers.isNotEmpty()

    private fun getOrCreate(id: Int) = controllers.getOrPut(id) {
        val dev = try { InputDevice.getDevice(id) } catch (e: Exception) { null }
        val name = dev?.name ?: "Unknown"
        val type = detect(name)
        val s = State(id, name, type); listener?.onConnected(s); s
    }

    private fun detect(name: String): Type {
        val n = name.lowercase()
        return when {
            n.contains("xbox") || n.contains("xinput") -> Type.XBOX
            n.contains("dualshock") || n.contains("ps4") -> Type.PS4
            n.contains("dualsense") || n.contains("ps5") -> Type.PS5
            n.contains("switch") || n.contains("nintendo") -> Type.SWITCH
            n.contains("nokia") -> Type.NOKIA
            else -> Type.GENERIC
        }
    }

    private fun safe(e: MotionEvent, axis: Int) = try { e.getAxisValue(axis) } catch (ex: Exception) { 0f }

    companion object {
        const val BTN_A = KeyEvent.KEYCODE_BUTTON_A
        const val BTN_B = KeyEvent.KEYCODE_BUTTON_B
        const val BTN_X = KeyEvent.KEYCODE_BUTTON_X
        const val BTN_Y = KeyEvent.KEYCODE_BUTTON_Y
        const val BTN_START = KeyEvent.KEYCODE_BUTTON_START
        const val BTN_SELECT = KeyEvent.KEYCODE_BUTTON_SELECT
        const val BTN_LB = KeyEvent.KEYCODE_BUTTON_L1
        const val BTN_RB = KeyEvent.KEYCODE_BUTTON_R1
        const val BTN_LS = KeyEvent.KEYCODE_BUTTON_THUMBL
        const val BTN_RS = KeyEvent.KEYCODE_BUTTON_THUMBR
        const val BTN_HOME = KeyEvent.KEYCODE_BUTTON_MODE
    }
}
