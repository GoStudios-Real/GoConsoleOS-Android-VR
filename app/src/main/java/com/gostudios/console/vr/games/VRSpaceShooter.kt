package com.gostudios.console.vr.games

import com.gostudios.console.vr.engine.VREngine
import kotlin.random.Random

class VRSpaceShooter : VRGame() {
    override val name = "VR Space Shooter"; override val description = "Shoot aliens"
    private var sx=0f; private var sy=0f; private var cd=0f; private var sp=0f
    private val bullets = mutableListOf<Bullet>(); private val enemies = mutableListOf<Enemy>(); private val stars = mutableListOf<Star>()
    data class Bullet(var x:Float,var y:Float,var z:Float,var life:Float=2f)
    data class Enemy(var x:Float,var y:Float,var z:Float,var spd:Float=1f,var hp:Int=1,var sz:Float=0.2f)
    data class Star(var x:Float,var y:Float,var z:Float,var spd:Float)

    override fun init(e: VREngine) { reset(); stars.clear(); for (i in 0..50) stars.add(Star(Random.nextFloat()*4-2,Random.nextFloat()*4-2,-Random.nextFloat()*20,Random.nextFloat()*2+1f)) }
    override fun reset() { super.reset(); sx=0f;sy=0f;bullets.clear();enemies.clear();cd=0f;sp=0f }

    override fun update(d: Float, e: VREngine) {
        if (isPaused) return; cd-=d; sp+=d
        bullets.forEach { it.z-=5f*d; it.life-=d }; bullets.removeAll { it.life<=0||it.z<-30f }
        enemies.forEach { it.z+=it.spd*d }; enemies.removeAll { it.z>-1f }
        if (sp>1.5f) { sp=0f; enemies.add(Enemy(Random.nextFloat()*3-1.5f,Random.nextFloat()*2-1f,-15f,Random.nextFloat()+0.5f)) }
        val hitE=mutableListOf<Enemy>(); val hitB=mutableListOf<Bullet>()
        for (b in bullets) for (en in enemies) if (kotlin.math.abs(b.x-en.x)<en.sz&&kotlin.math.abs(b.y-en.y)<en.sz&&kotlin.math.abs(b.z-en.z)<0.5f) { en.hp--; if (en.hp<=0){hitE.add(en);score+=10}; hitB.add(b) }
        enemies.removeAll(hitE); bullets.removeAll(hitB)
        stars.forEach { it.z+=it.spd*d; if (it.z>0){it.z=-20f;it.x=Random.nextFloat()*4-2;it.y=Random.nextFloat()*4-2} }
    }

    override fun render(e: VREngine, mvp: FloatArray, l: Boolean) {
        e.drawCube(mvp,0f,0.8f,1f,0.3f,sx,sy,-3f)
        for (b in bullets) e.drawCube(mvp,1f,1f,0f,0.05f,b.x,b.y,b.z)
        for (en in enemies) e.drawSphere(mvp,1f,0.2f,0.2f,en.sz,en.x,en.y,en.z)
        for (s in stars) e.drawSphere(mvp,1f,1f,1f,0.02f,s.x,s.y,s.z)
    }

    override fun onButton(b: Int, p: Boolean) { if (p&&cd<=0) { bullets.add(Bullet(sx,sy,-3f)); cd=0.15f } }
    override fun onStick(s: Int, x: Float, y: Float) { if (s==0){sx+=x*0.1f;sy+=y*0.1f} }
}
