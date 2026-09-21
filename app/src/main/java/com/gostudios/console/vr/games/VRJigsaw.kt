package com.gostudios.console.vr.games

import com.gostudios.console.vr.engine.VREngine
import kotlin.random.Random

class VRJigsaw : VRGame() {
    override val name = "VR Jigsaw"; override val description = "Solve 3D puzzle"
    data class Piece(var x:Float,var y:Float,var z:Float,val tx:Float,val ty:Float,val tz:Float,val r:Float,val g:Float,val b:Float,var placed:Boolean=false)
    private val pieces = mutableListOf<Piece>(); private var sel=0; private val gs=3

    override fun init(e: VREngine) { reset() }
    override fun reset() {
        super.reset(); pieces.clear(); sel=0
        val c=arrayOf(floatArrayOf(1f,0f,0f),floatArrayOf(0f,1f,0f),floatArrayOf(0f,0f,1f),floatArrayOf(1f,1f,0f),floatArrayOf(1f,0f,1f),floatArrayOf(0f,1f,1f),floatArrayOf(1f,0.5f,0f),floatArrayOf(0.5f,0f,1f),floatArrayOf(0f,0.5f,0.5f))
        var i=0; for (r in 0 until gs) for (col in 0 until gs) { val cl=c[i%9]; pieces.add(Piece(Random.nextFloat()*4-2,Random.nextFloat()*3-2,Random.nextFloat()*3-5f,(col-1)*0.4f,(r-1)*0.4f,-3f,cl[0],cl[1],cl[2])); i++ }
    }

    override fun update(d: Float, e: VREngine) {
        if (isPaused) return
        pieces.forEach { p -> if (!p.placed) { val dx=p.tx-p.x; val dy=p.ty-p.y; val dz=p.tz-p.z; if (kotlin.math.sqrt((dx*dx+dy*dy+dz*dz).toDouble())<0.05) { p.placed=true; score++ } } }
    }

    override fun render(e: VREngine, mvp: FloatArray, l: Boolean) {
        pieces.forEachIndexed { i, p ->
            if (i==sel&&!p.placed) e.drawCube(mvp,1f,1f,1f,0.18f,p.x,p.y,p.z) else e.drawCube(mvp,p.r,p.g,p.b,0.18f,p.x,p.y,p.z)
            if (!p.placed) e.drawCube(mvp,p.r*0.3f,p.g*0.3f,p.b*0.3f,0.16f,p.tx,p.ty,p.tz)
        }
    }

    override fun onButton(b: Int, p: Boolean) { if (!p) return; when(b) { 0->{val pc=pieces.getOrNull(sel)?:return; if(!pc.placed){pc.x=pc.tx;pc.y=pc.ty;pc.z=pc.tz}}; 1->{sel=(sel+1)%pieces.size} } }
    override fun onStick(s: Int, x: Float, y: Float) { val p=pieces.getOrNull(sel)?:return; if(!p.placed&&s==0){p.x+=x*0.02f;p.y+=y*0.02f} }
}
