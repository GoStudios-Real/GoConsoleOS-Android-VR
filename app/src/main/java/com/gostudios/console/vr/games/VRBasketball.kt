package com.gostudios.console.vr.games

import com.gostudios.console.vr.engine.VREngine

class VRBasketball : VRGame() {
    override val name = "VR Basketball"; override val description = "Throw at the hoop"
    data class Ball(var x:Float,var y:Float,var z:Float,var vx:Float,var vy:Float,var vz:Float,var life:Float=3f)
    private val balls=mutableListOf<Ball>(); private var hy=1.5f; private var hx=0f; private var hz=-5f; private var cd=0f; private var ax=0f

    override fun init(e: VREngine) { reset() }
    override fun reset() { super.reset(); balls.clear(); cd=0f }

    override fun update(d: Float, e: VREngine) {
        if (isPaused) return; cd-=d
        balls.forEach { b -> b.vy-=4f*d; b.x+=b.vx*d; b.y+=b.vy*d; b.z+=b.vz*d; b.life-=d
            if (kotlin.math.abs(b.x-hx)<0.2f&&kotlin.math.abs(b.y-hy)<0.3f&&kotlin.math.abs(b.z-hz)<0.3f&&b.vy<0) { score+=10; b.life=0f } }
        balls.removeAll { it.life<=0||it.y<-2f }
    }

    override fun render(e: VREngine, mvp: FloatArray, l: Boolean) {
        e.drawCube(mvp,1f,0.3f,0f,0.05f,hx-0.2f,hy,hz); e.drawCube(mvp,1f,0.3f,0f,0.05f,hx+0.2f,hy,hz)
        e.drawCube(mvp,1f,0.3f,0f,0.05f,hx,hy,hz-0.2f); e.drawCube(mvp,1f,0.3f,0f,0.05f,hx,hy,hz+0.2f)
        e.drawCube(mvp,0.8f,0.8f,0.8f,0.6f,hx,hy+0.5f,hz-0.3f)
        e.drawCube(mvp,0.5f,0.5f,0.5f,0.08f,hx,0f,hz-0.3f)
        for (b in balls) e.drawSphere(mvp,0.8f,0.4f,0f,0.12f,b.x,b.y,b.z)
    }

    override fun onButton(b: Int, p: Boolean) { if (p&&cd<=0) { balls.add(Ball(0f,0.5f,-2f,ax*3f,5f,-6f)); cd=0.3f } }
    override fun onStick(s: Int, x: Float, y: Float) { if (s==0) ax+=x*0.05f }
    override fun onTrigger(t: Int, v: Float) { if (t==1&&cd<=0) { balls.add(Ball(0f,0.5f,-2f,ax*3f,5f+v*2f,-6f)); cd=0.3f } }
}
