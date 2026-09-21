package com.gostudios.console.vr

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import com.gostudios.console.vr.controller.BluetoothController
import com.gostudios.console.vr.engine.VREngine
import com.gostudios.console.vr.engine.VRScene
import com.gostudios.console.vr.games.GameActivity

class VRActivity : Activity(), BluetoothController.Listener {
    private lateinit var vr: VREngine; private lateinit var ctrl: BluetoothController; private var sel = 0
    private val games = arrayOf("VR Pong","VR Snake","VR Space Shooter","VR Jigsaw","VR Basketball")

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enterImmersive()
        vr = VREngine(this)
        vr.setScene(object : VRScene {
            override fun onDraw(e: VREngine, mvp: FloatArray, d: Float, l: Boolean) { drawMenu(e, mvp, d) }
        })
        setContentView(vr)
        ctrl = BluetoothController(); ctrl.setListener(this)
    }

    private fun enterImmersive() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.let { it.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars()); it.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE }
        } else { @Suppress("DEPRECATION") window.decorView.systemUiVisibility = (android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) }
    }

    private fun drawMenu(e: VREngine, mvp: FloatArray, d: Float) {
        e.drawCube(mvp,0.05f,0.05f,0.1f,30f,0f,0f,-15f) // sky
        e.drawCube(mvp,0.1f,0.12f,0.18f,20f,0f,-2f,-10f) // floor
        e.drawCube(mvp,0f,0.4f,1f,0.5f,0f,2f,-4f) // title bar
        for (i in games.indices) {
            val x=(i-2)*1.5f; val z=-3f; val sel=i==this.sel
            if (sel) e.drawCube(mvp,0f,0.4f,1f,0.9f,x,0f,z-0.05f)
            val r=if (sel)0.15f else 0.1f; val g=if(sel)0.18f else 0.13f; val b=if(sel)0.3f else 0.2f
            e.drawCube(mvp,r,g,b,0.8f,x,0f,z)
        }
    }

    private fun launch(i: Int) { startActivity(Intent(this, GameActivity::class.java).putExtra("game_index", i)) }

    override fun onKeyDown(k: Int, e: KeyEvent): Boolean {
        if (ctrl.onKeyDown(k, e)) return true
        when (k) { KeyEvent.KEYCODE_DPAD_LEFT->sel=(sel-1+games.size)%games.size; KeyEvent.KEYCODE_DPAD_RIGHT->sel=(sel+1)%games.size; KeyEvent.KEYCODE_BUTTON_A,KeyEvent.KEYCODE_ENTER->launch(sel) }
        return super.onKeyDown(k, e)
    }

    override fun onGenericMotionEvent(e: MotionEvent) = ctrl.onMotionEvent(e) || super.onGenericMotionEvent(e)
    override fun onButton(s: BluetoothController.State, btn: Int, p: Boolean) {
        if (!p) return; when(btn) { BluetoothController.BTN_A->launch(sel); 104->sel=(sel+1)%games.size; 103->sel=(sel-1+games.size)%games.size }
    }
    override fun onStick(s: BluetoothController.State, axis: Int, x: Float, y: Float) { if (axis==0&&kotlin.math.abs(x)>0.5f) sel=if(x>0)(sel+1)%games.size else (sel-1+games.size)%games.size }

    override fun onResume() { super.onResume(); vr.onResume();  }
    override fun onPause() { super.onPause(); vr.onPause(); ctrl.setListener(null) }
    override fun onDestroy() { super.onDestroy() }
}


