package dk.scuffed.whiteboardapp.pipeline.stages

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.*
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.openGL.glBindFramebuffer
import dk.scuffed.whiteboardapp.openGL.loadShader
import dk.scuffed.whiteboardapp.pipeline.FramebufferInfo
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.Stage
import dk.scuffed.whiteboardapp.pipeline.readRawResource
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer


class Vec2(val x: Int, val y: Int)


internal class DrawCorners(
    val context: Context,
    pipeline: Pipeline,
    private vararg val cornerPoints: Vec2
) : Stage(pipeline) {
    private val pointSize: Int = 25
    private var program: Int = 999

    private val cordsPerVertex = 3

    //XYZ Coordinates for the square we are drawing on.
    private val squareCords = floatArrayOf(
        -1.0f, 1.0f, 0.0f,
        -1.0f, -1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        1.0f, 1.0f,0.0f
    )

    // order to draw vertices
    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3)

    // 4 bytes per vertex
    private val vertexStride: Int = cordsPerVertex * 4

    // initialize vertex byte buffer for shape coordinates
    private val vertexBuffer: FloatBuffer =
        // (# of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(squareCords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(squareCords)
                position(0)
            }
        }

    // initialize byte buffer for the draw list
    private val drawListBuffer: ShortBuffer =
        // (# of coordinate values * 2 bytes per short)
        ByteBuffer.allocateDirect(drawOrder.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }

    val frameBufferInfo: FramebufferInfo

    init {
        frameBufferInfo = pipeline.allocateFramebuffer(this, GLES20.GL_RGBA, 1080, 1920)
        setupGlProgram()
    }

    protected fun setupGlProgram() {
        val vertexShaderCode = readRawResource(context, R.raw.vertex_shader)
        val fragmentShaderCode = readRawResource(context, R.raw.corner_shader)

        val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = dk.scuffed.whiteboardapp.openGL.glCreateProgram()
        dk.scuffed.whiteboardapp.openGL.glAttachShader(program, vertexShader)
        dk.scuffed.whiteboardapp.openGL.glAttachShader(program, fragmentShader)
        dk.scuffed.whiteboardapp.openGL.glLinkProgram(program)

    }

    override fun update() {
        for (point in cornerPoints){
            drawPoint(point)
        }
    }


    fun drawPoint(point: Vec2) {
        val bottomLeftCorner = Vec2(point.x - pointSize, point.y - pointSize)
        val topRightCorner = Vec2(point.x + pointSize, point.y + pointSize)

        val size = frameBufferInfo.textureSize
        glViewport(bottomLeftCorner.x, bottomLeftCorner.y, topRightCorner.x-bottomLeftCorner.x, topRightCorner.y-bottomLeftCorner.y)

        // Render to our framebuffer
        glBindFramebuffer(frameBufferInfo.fboHandle)
        glClear(0)

        glUseProgram(program)

        val positionHandle = glGetAttribLocation(program, "position")
        glEnableVertexAttribArray(positionHandle)
        glVertexAttribPointer(
            positionHandle,
            cordsPerVertex,
            GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        // Always provide resolution
        val resolutionHandle = glGetUniformLocation(program, "resolution")
        glUniform2f(
            resolutionHandle,
            pointSize*2.toFloat(),
            pointSize*2.toFloat()
        )
        val offsetHandle = glGetUniformLocation(program, "offset")
        glUniform2f(
            offsetHandle,
            bottomLeftCorner.x.toFloat(),
            bottomLeftCorner.y.toFloat()
        )


        glDrawElements(
            GL_TRIANGLES,
            drawOrder.size,
            GL_UNSIGNED_SHORT,
            drawListBuffer
        )
        glDisableVertexAttribArray(positionHandle)
    }

    private fun cornerCoordinatesForASquare(point: Vec2): FloatArray {
        return floatArrayOf(

        )
    }
}
