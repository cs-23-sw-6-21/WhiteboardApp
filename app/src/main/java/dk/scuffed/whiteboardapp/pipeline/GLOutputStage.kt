package dk.scuffed.whiteboardapp.pipeline

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import android.util.Size
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.openGL.*
import dk.scuffed.whiteboardapp.pipeline.readRawResource
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

internal abstract class GLOutputStage(
    private val context: Context,
    private val vertexShaderResource: Int,
    private val fragmentShaderResource: Int,
    private val pipeline: Pipeline) : Stage(pipeline)
{
    private var program: Int = 999

    private val coordsPerVertex = 3

    //XYZ Coordinates for the square we are drawing on.
    private val squareCoords = floatArrayOf(
        -1.0f, 1.0f, 0.0f,
        -1.0f, -1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        1.0f, 1.0f,0.0f
    )

    // order to draw vertices
    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3)

    // 4 bytes per vertex
    private val vertexStride: Int = coordsPerVertex * 4

    // initialize vertex byte buffer for shape coordinates
    private val vertexBuffer: FloatBuffer =
        // (# of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(squareCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(squareCoords)
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

    lateinit var frameBufferInfo: FramebufferInfo

    protected fun setup() {
        setupGlProgram()
        setupFramebufferInfo()
    }

    protected fun setupGlProgram() {
        val vertexShaderCode = readRawResource(context, vertexShaderResource)
        val fragmentShaderCode = readRawResource(context, fragmentShaderResource)

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = glCreateProgram()
        glAttachShader(program, vertexShader)
        glAttachShader(program, fragmentShader)
        glLinkProgram(program)
    }

    protected abstract fun setupFramebufferInfo()

    protected fun allocateFramebuffer(textureFormat: Int, resolution: Size) {
        frameBufferInfo = pipeline.allocateFramebuffer(this, textureFormat, resolution.width, resolution.height)
    }

    protected open fun setupUniforms(program: Int) {

    }

    final override fun update() {
        val size = frameBufferInfo.textureSize
        glViewport(0, 0, size.width, size.height)

        // Render to our framebuffer
        glBindFramebuffer(frameBufferInfo.fboHandle)
        glClear(0)

        glUseProgram(program)

        val positionHandle = glGetAttribLocation(program, "position")
        glEnableVertexAttribArray(positionHandle)
        glVertexAttribPointer(positionHandle, coordsPerVertex, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)

        // Always provide resolution
        val resolutionHandle = glGetUniformLocation(program, "resolution")
        glUniform2f(resolutionHandle, size.width.toFloat(), size.height.toFloat())

        // Set up user uniforms
        setupUniforms(program)

        glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer)
        glDisableVertexAttribArray(positionHandle)
    }
}