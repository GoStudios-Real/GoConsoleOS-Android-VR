package com.gostudios.console.vr.games

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import com.gostudios.console.vr.controller.BluetoothController
import com.gostudios.console.vr.engine.VREngine
import com.gostudios.console.vr.engine.VRScene

class GameActivity : Activity(), BluetoothController.Listener {
    private lateinit var vr: VREngine; private lateinit var ctrl: BluetoothController; private var game: VRGame? = null

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enterImmersive()
        val idx = intent.getIntExtra("game_index", 0)
        game = when (idx) { 0->VRPong(); 1->VRSnake(); 2->VRSpaceShooter(); 3->VRJigsaw(); 4->VRBasketball(); else->VRPong() }
        vr = VREngine(this)
        vr.setScene(object : VRScene {
            override fun onDraw(e: VREngine, mvp: FloatArray, d: Float, l: Boolean) {
                game?.update(d, e); game?.render(e, mvp, l)
            }
        })
        setContentView(vr)
        ctrl = BluetoothController(); ctrl.setListener(this)
    }

    private fun enterImmersive() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.let { it.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars()); it.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE }
        } else { @Suppress("DEPRECATION") window.decorView.systemUiVisibility = (android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) }
    }

    override fun onKeyDown(k: Int, e: KeyEvent) = ctrl.onKeyDown(k, e) || super.onKeyDown(k, e)
    override fun onGenericMotionEvent(e: MotionEvent) = ctrl.onMotionEvent(e) || super.onGenericMotionEvent(e)

    override fun onButton(s: BluetoothController.State, btn: Int, pressed: Boolean) {
        if (!pressed) return
        when (btn) {
            BluetoothController.BTN_A -> game?.onButton(0, true)
            BluetoothController.BTN_B -> game?.onButton(1, true)
            BluetoothController.BTN_X -> game?.onButton(2, true)
            BluetoothController.BTN_Y -> game?.onButton(3, true)
            BluetoothController.BTN_START -> game?.let { it.isPaused = !it.isPaused }
            BluetoothController.BTN_SELECT -> game?.reset()
        }
    }

    override fun onStick(s: BluetoothController.State, axis: Int, x: Float, y: Float) { game?.onStick(axis, x, y) }
    override fun onTrigger(s: BluetoothController.State, axis: Int, value: Float) { game?.onTrigger(axis, value) }
    override fun onResume() { super.onResume(); vr.onResume();  }
    override fun onPause() { super.onPause(); vr.onPause(); ctrl.setListener(null) }
    override fun onDestroy() { super.onDestroy(); game?.cleanup() }
}


