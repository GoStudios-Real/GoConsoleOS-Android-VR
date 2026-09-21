package com.gostudios.console.vr.games

import com.gostudios.console.vr.engine.VREngine

abstract class VRGame {
    abstract val name: String
    abstract val description: String
    var isActive = false; var score = 0; var isPaused = false
    abstract fun init(engine: VREngine)
    abstract fun update(delta: Float, engine: VREngine)
    abstract fun render(engine: VREngine, mvp: FloatArray, isLeftEye: Boolean)
    abstract fun onButton(btn: Int, pressed: Boolean)
    abstract fun onStick(stick: Int, x: Float, y: Float)
    open fun onTrigger(t: Int, v: Float) {}
    open fun cleanup() {}
    open fun reset() { score = 0 }
}
