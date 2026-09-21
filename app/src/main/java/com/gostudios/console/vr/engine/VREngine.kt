package com.gostudios.console.vr.engine

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.gostudios.console.vr.headset.GoStudiosHeadset
import kotlin.math.sin
import kotlin.math.cos
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class VREngine(context: Context) : GLSurfaceView(context), GLSurfaceView.Renderer {

    private val headset = GoStudiosHeadset.getInstance(context)
    private val headTracker = HeadTracker(context)
    private val leftProj = FloatArray(16)
    private val rightProj = FloatArray(16)
    private val viewMat = FloatArray(16)
    private val tempMat = FloatArray(16)
    private val modelMat = FloatArray(16)
    private val mvpMat = FloatArray(16)

    private var w = 1920
    private var h = 1080
    private var programId = 0
    private var posHandle = 0
    private var colorHandle = 0
    private var mvpHandle = 0

    private var scene: VRScene? = null
    private var onRenderCallback: ((Float) -> Unit)? = null

    init {
        setEGLContextClientVersion(2)
        setRenderer(this)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun setScene(s: VRScene) { scene = s }
    fun setOnRenderCallback(cb: (Float) -> Unit) { onRenderCallback = cb }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.05f, 0.05f, 0.08f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        val vs = loadShader(GLES20.GL_VERTEX_SHADER, VERT)
        val fs = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAG)
        programId = GLES20.glCreateProgram()
        GLES20.glAttachShader(programId, vs)
        GLES20.glAttachShader(programId, fs)
        GLES20.glLinkProgram(programId)
        posHandle = GLES20.glGetAttribLocation(programId, "aPos")
        colorHandle = GLES20.glGetUniformLocation(programId, "uColor")
        mvpHandle = GLES20.glGetUniformLocation(programId, "uMVP")
        scene?.onInit()
        headTracker.start()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        w = width; h = height
        val aspect = (w / 2).toFloat() / h.toFloat()
        Matrix.perspectiveM(leftProj, 0, headset.lensFov, aspect, 0.1f, 100f)
        Matrix.perspectiveM(rightProj, 0, headset.lensFov, aspect, 0.1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        val delta = headTracker.update()
        headTracker.getViewMatrix(viewMat)
        val halfIPD = headset.getInterpupillaryDistanceMeters() / 2f

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(programId)

        // Left eye
        GLES20.glViewport(0, 0, w / 2, h)
        Matrix.setIdentityM(modelMat, 0)
        Matrix.translateM(modelMat, 0, halfIPD, 0f, 0f)
        Matrix.multiplyMM(tempMat, 0, viewMat, 0, modelMat, 0)
        Matrix.multiplyMM(mvpMat, 0, leftProj, 0, tempMat, 0)
        scene?.onDraw(this, mvpMat, delta, true)

        // Right eye
        GLES20.glViewport(w / 2, 0, w / 2, h)
        Matrix.setIdentityM(modelMat, 0)
        Matrix.translateM(modelMat, 0, -halfIPD, 0f, 0f)
        Matrix.multiplyMM(tempMat, 0, viewMat, 0, modelMat, 0)
        Matrix.multiplyMM(mvpMat, 0, rightProj, 0, tempMat, 0)
        scene?.onDraw(this, mvpMat, delta, false)

        onRenderCallback?.invoke(delta)
    }

    fun drawCube(mvp: FloatArray, r: Float, g: Float, b: Float, size: Float = 1f, x: Float = 0f, y: Float = 0f, z: Float = -3f) {
        val s = size / 2f
        val v = floatArrayOf(
            x-s,y+s,z+s, x+s,y+s,z+s, x+s,y-s,z+s, x-s,y-s,z+s,
            x-s,y+s,z-s, x-s,y-s,z-s, x+s,y-s,z-s, x+s,y+s,z-s,
            x-s,y+s,z-s, x-s,y+s,z+s, x-s,y-s,z+s, x-s,y-s,z-s,
            x+s,y+s,z+s, x+s,y+s,z-s, x+s,y-s,z-s, x+s,y-s,z+s,
            x-s,y+s,z-s, x+s,y+s,z-s, x+s,y+s,z+s, x-s,y+s,z+s,
            x-s,y-s,z+s, x+s,y-s,z+s, x+s,y-s,z-s, x-s,y-s,z-s
        )
        drawArrays(v, r, g, b, mvp)
    }

    fun drawSphere(mvp: FloatArray, r: Float, g: Float, b: Float, radius: Float = 0.5f, cx: Float = 0f, cy: Float = 0f, cz: Float = -3f, slices: Int = 12, stacks: Int = 8) {
        val verts = mutableListOf<Float>()
        for (i in 0 until stacks) {
            val p1 = Math.PI * i / stacks
            val p2 = Math.PI * (i + 1) / stacks
            for (j in 0 until slices) {
                val t1 = 2 * Math.PI * j / slices
                val t2 = 2 * Math.PI * (j + 1) / slices
                val a = sph(p1, t1, radius, cx, cy, cz)
                val b2 = sph(p1, t2, radius, cx, cy, cz)
                val c = sph(p2, t2, radius, cx, cy, cz)
                val d = sph(p2, t1, radius, cx, cy, cz)
                verts.addAll(listOf(a[0],a[1],a[2], b2[0],b2[1],b2[2], c[0],c[1],c[2]))
                verts.addAll(listOf(a[0],a[1],a[2], c[0],c[1],c[2], d[0],d[1],d[2]))
            }
        }
        drawArrays(verts.toFloatArray(), r, g, b, mvp)
    }

    private fun sph(phi: Double, theta: Double, rad: Float, cx: Float, cy: Float, cz: Float) =
        floatArrayOf(
            (rad * sin(phi) * cos(theta) + cx).toFloat(),
            (rad * cos(phi) + cy).toFloat(),
            (rad * sin(phi) * sin(theta) + cz).toFloat()
        )

    private fun drawArrays(v: FloatArray, r: Float, g: Float, b: Float, mvp: FloatArray) {
        val buf = ByteBuffer.allocateDirect(v.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        buf.put(v).position(0)
        GLES20.glEnableVertexAttribArray(posHandle)
        GLES20.glVertexAttribPointer(posHandle, 3, GLES20.GL_FLOAT, false, 0, buf)
        GLES20.glUniform4f(colorHandle, r, g, b, 1f)
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvp, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, v.size / 3)
    }

    private fun loadShader(type: Int, code: String): Int {
        val s = GLES20.glCreateShader(type)
        GLES20.glShaderSource(s, code)
        GLES20.glCompileShader(s)
        return s
    }

    override fun onPause() { super.onPause(); headTracker.stop() }
    override fun onResume() { super.onResume(); headTracker.start() }

    companion object {
        private const val VERT = "attribute vec4 aPos;uniform mat4 uMVP;void main(){gl_Position=uMVP*aPos;}"
        private const val FRAG = "precision mediump float;uniform vec4 uColor;void main(){gl_FragColor=uColor;}"
    }
}

interface VRScene {
    fun onInit() {}
    fun onDraw(engine: VREngine, mvp: FloatArray, delta: Float, isLeftEye: Boolean)
}

