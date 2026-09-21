package com.gostudios.console.vr.games

import com.gostudios.console.vr.engine.VREngine
import kotlin.math.abs

class VRPong : VRGame() {
    override val name = "VR Pong"; override val description = "Classic paddle vs AI"
    private var bx=0f; private var by=0f; private var bvx=2f; private var bvy=1.5f
    private var py=0f; private var ay=0f; private val bs=0.1f; private val pw=0.6f; private val ph=0.15f; private val aw=2f; private val ah=1.5f

    override fun init(e: VREngine) { reset() }
    override fun reset() { super.reset(); bx=0f;by=0f;bvx=2f;bvy=1.5f;py=0f;ay=0f }

    override fun update(d: Float, e: VREngine) {
        if (isPaused) return
        bx+=bvx*d; by+=bvy*d
        if (by>ah||by<-ah) bvy=-bvy; by=by.coerceIn(-ah,ah)
        if (bx<0.3f&&bx>-0.3f&&by<py+ph&&by>py-ph) { bvx=abs(bvx); bvy+=py*0.5f }
        val tgt=by; ay+=(tgt-ay)*d*2f; ay=ay.coerceIn(-ah,ah)
        if (bx>1.5f) { if (by<ay+ph&&by>ay-ph) bvx=-abs(bvx) else { score++; reset() } }
        if (bx<-2f) reset()
    }

    override fun render(e: VREngine, mvp: FloatArray, l: Boolean) {
        e.drawSphere(mvp,1f,1f,1f,bs,bx,by,-3f)
        e.drawCube(mvp,0f,0.4f,1f,0.15f,0f,py,-3f)
        e.drawCube(mvp,1f,0.2f,0.2f,0.15f,1.5f,ay,-3f)
        e.drawCube(mvp,0.3f,0.3f,0.5f,0.05f,0f,ah,-3f)
        e.drawCube(mvp,0.3f,0.3f,0.5f,0.05f,0f,-ah,-3f)
    }

    override fun onButton(b: Int, p: Boolean) {}
    override fun onStick(s: Int, x: Float, y: Float) { if (s==0) py+=y*0.1f }
}
