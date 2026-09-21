package com.gostudios.console.vr.games

import com.gostudios.console.vr.engine.VREngine
import kotlin.random.Random

class VRSnake : VRGame() {
    override val name = "VR Snake"; override val description = "Eat food to grow"
    private data class Seg(var x: Float, var z: Float)
    private val segs = mutableListOf<Seg>()
    private var fx=0f; private var fz=0f; private var dx=1f; private var dz=0f
    private var mt=0f; private val mi=0.2f; private val gs=0.3f; private val gh=5

    override fun init(e: VREngine) { reset() }
    override fun reset() {
        super.reset(); segs.clear()
        for (i in 0..2) segs.add(Seg(-gs*i, -3f))
        dx=1f;dz=0f;mt=0f;spawn()
    }
    private fun spawn() { fx=(Random.nextInt(-gh,gh)*gs); fz=-3f+(Random.nextInt(-gh,gh)*gs) }

    override fun update(d: Float, e: VREngine) {
        if (isPaused) return; mt+=d; if (mt<mi) return; mt=0f
        val h=segs.first(); val nx=h.x+dx*gs; val nz=h.z+dz*gs
        if (nx<-gh*gs||nx>gh*gs||nz<-3f-gh*gs||nz>-3f+gh*gs) { reset(); return }
        for (i in 1 until segs.size) if (kotlin.math.abs(segs[i].x-nx)<0.01f&&kotlin.math.abs(segs[i].z-nz)<0.01f) { reset(); return }
        segs.add(0, Seg(nx, nz))
        if (kotlin.math.abs(nx-fx)<gs&&kotlin.math.abs(nz-fz)<gs) { score+=10; spawn() } else segs.removeAt(segs.lastIndex)
    }

    override fun render(e: VREngine, mvp: FloatArray, l: Boolean) {
        segs.forEachIndexed { i, s -> val br=1f-(i.toFloat()/segs.size)*0.5f; e.drawCube(mvp,0f,0.8f*br,0.4f*br,gs*0.9f,s.x,0f,s.z) }
        e.drawSphere(mvp,1f,0.2f,0.2f,gs*0.6f,fx,0f,fz)
    }

    override fun onButton(b: Int, p: Boolean) {}
    override fun onStick(s: Int, x: Float, y: Float) {
        if (s==0) { if (kotlin.math.abs(x)>kotlin.math.abs(y)) { if (x>0.3f&&dx!=-1f){dx=1f;dz=0f} else if (x<-0.3f&&dx!=1f){dx=-1f;dz=0f} }
        else { if (y>0.3f&&dz!=1f){dx=0f;dz=-1f} else if (y<-0.3f&&dz!=-1f){dx=0f;dz=1f} } }
    }
}
