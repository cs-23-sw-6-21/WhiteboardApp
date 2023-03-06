package dk.scuffed.whiteboardapp.pipeline.stages

import android.content.Context
import android.opengl.GLES20
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.opengl.*
import dk.scuffed.whiteboardapp.pipeline.FramebufferInfo
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.Stage
import dk.scuffed.whiteboardapp.pipeline.readRawResource
import dk.scuffed.whiteboardapp.utils.Vec2Float
import dk.scuffed.whiteboardapp.utils.Vec2Int
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.*

internal class DrawLinesStage(
    private val context: Context,
    pipeline: Pipeline,
    private vararg val cornerPoints: Vec2Int
) : Stage(pipeline) {
    private var program: Int = 999
    private val lineWidth: Float = 5.0f
    private val cordsPerVertex = 3

    // The width of the smartphone's resolution
    private val widthResolution: Int = 1080
    // The height of the smartphone's resolution
    private val heightResolution: Int = 1920

    //XYZ Coordinates for the square we are drawing on.
    private var squareCoords = FloatArray(cornerPoints.size*3)

    // order to draw vertices
    private val drawOrder = genericDrawOrder(cornerPoints.size)

    // 4 bytes per vertex
    private val vertexStride: Int = cordsPerVertex * 4

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
        frameBufferInfo = pipeline.allocateFramebuffer(this, GLES20.GL_RGBA, widthResolution.toInt(), heightResolution.toInt())
        setupGlProgram()
    }

    /**
     * This function loads our shaders based from the res/raw
     */
    private fun setupGlProgram() {
        val vertexShaderCode = readRawResource(context, R.raw.vertex_shader)
        val fragmentShaderCode = readRawResource(context, R.raw.line_shader)

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = glCreateProgram()
        glAttachShader(program, vertexShader)
        glAttachShader(program, fragmentShader)
        glLinkProgram(program)
    }

    override fun update() {
        drawLines()
    }

    /**
     * Returns the angle between two vectors based on the x axe.
        */
    private fun angleBetweenTwoVec(vec1: Vec2Int, vec2: Vec2Int): Float{
        val vec = vec2.subtact(vec1)
        return (atan2(vec.y.toFloat(), vec.x.toFloat())+(PI/2.0f)).toFloat()
    }

    /**
     * Returns the unit vector based on the angle between two vectors.
     * @param vec1 the line's starts point.
     * @param vec2 the line's end point.
     */
    private fun getUnitVec(vec1:Vec2Int, vec2: Vec2Int):Vec2Float{
        val angle: Float = angleBetweenTwoVec(vec1, vec2)
        return Vec2Float(cos(angle), sin(angle))
    }

    /**
     * Returns the corners for the line in a float array.
     * @param coordinate1 the line's starts point.
     * @param coordinate2 the line's end point.
     */
    private fun arrayOfCorners(coordinate1: Vec2Int, coordinate2: Vec2Int): ArrayList<Float> {
        //The scaled unit vector
        val scaledUnitVector: Vec2Float = getUnitVec(coordinate1, coordinate2).multiply(lineWidth)

        //The vector between the two points
        val vecBetween: Vec2Float = coordinate2.subtact(coordinate1).vec2IntToFloat()

        // Top- and bottom left corners for the square
        val topLeftCorner = scaledUnitVector.additon(coordinate1.vec2IntToFloat())
        val bottomLeftCorner = coordinate1.vec2IntToFloat().subtact(scaledUnitVector)
        // Top- and bottom right corners for the square
        val topRightCorner = topLeftCorner.additon(vecBetween)
        val bottomRightCorner = bottomLeftCorner.additon(vecBetween)

        return arrayListOf(
            map(topLeftCorner.x, widthResolution), map(topLeftCorner.y, heightResolution), 0.0f,
            map(bottomLeftCorner.x, widthResolution), map(bottomLeftCorner.y, heightResolution), 0.0f,
            map(bottomRightCorner.x, widthResolution), map(bottomRightCorner.y, heightResolution), 0.0f,
            map(topRightCorner.x, widthResolution), map(topRightCorner.y, heightResolution), 0.0f
        )
    }

    /**
     * This function maps the coordinates of the vector from the range 0-
     */
    private fun map(value: Float, res: Int): Float {
        val min1 = 0.0f
        val min2 = -1.0f
        val max2 = 1.0f
        return min2 + (value - min1) * (max2 - min2) / (res - min1)
    }

    /**
     * Draws the line between the coordinates
     */
    private fun drawLines() {
        val array: ArrayList<Float> = ArrayList()
        for (i in cornerPoints.indices){
            if(i == cornerPoints.lastIndex){
                array.addAll(arrayOfCorners(cornerPoints[i], cornerPoints[0]))
                break
            }
            array.addAll(arrayOfCorners(cornerPoints[i], cornerPoints[i+1]))
        }
        squareCoords = array.toFloatArray()
        val vertexBuffer: FloatBuffer =
            // (# of coordinate values * 4 bytes per float)
            ByteBuffer.allocateDirect(squareCoords.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(squareCoords)
                    position(0)
                }
            }

        glViewport(
            0,
            0,
            widthResolution,
            heightResolution
        )

        // Render to our framebuffer
        glBindFramebuffer(frameBufferInfo.fboHandle)
        glClear(0)

        glUseProgram(program)

        val positionHandle = glGetAttribLocation(program, "position")
        glEnableVertexAttribArray(positionHandle)
        glVertexAttribPointer(
            positionHandle,
            cordsPerVertex,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )
        glDrawElements(
            GLES20.GL_TRIANGLES,
            drawOrder.size,
            GLES20.GL_UNSIGNED_SHORT,
            drawListBuffer
        )
        glDisableVertexAttribArray(positionHandle)
    }

    /**
     * Returns the draw order for the squares in a ShortArray
     */
    private fun genericDrawOrder(numbersOfSquares: Int): ShortArray {
        var drawOrder: ShortArray = ShortArray(numbersOfSquares*6)
        //The specific draw order is set in the for loop
        for (i: Int in 0 until numbersOfSquares){
            drawOrder[i*6] = (i*4).toShort()
            drawOrder[i*6+1] = (i*4+1).toShort()
            drawOrder[i*6+2] = (i*4+2).toShort()
            drawOrder[i*6+3] = (i*4).toShort()
            drawOrder[i*6+4] = (i*4+2).toShort()
            drawOrder[i*6+5] = (i*4+3).toShort()
        }
        return drawOrder
    }
}