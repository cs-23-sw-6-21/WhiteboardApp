package dk.scuffed.whiteboardapp.pipeline.stages

import android.content.Context
import android.opengl.GLES20
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.opengl.*
import dk.scuffed.whiteboardapp.pipeline.FramebufferInfo
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.utils.Vec2Float
import dk.scuffed.whiteboardapp.utils.Vec2Int
import dk.scuffed.whiteboardapp.utils.Vec3Float
import kotlin.math.pow
import kotlin.math.sqrt

internal class PerspectiveCorrectionStage(
    context: Context,
    private val inputFramebufferInfo: FramebufferInfo,
    private val inputVertices: PointsOutputStage,
    pipeline: Pipeline)
    : GLOutputStage(context,
    R.raw.vertex_perspective_correction_shader,
    R.raw.texture_distortable, pipeline) {
    init {
        setup()
    }

    override fun setupFramebufferInfo() {
        allocateFramebuffer(GLES20.GL_RGBA, inputFramebufferInfo.textureSize)
    }

    override fun setupUniforms(program: Int) {
        super.setupUniforms(program)

        // Input framebuffer resolution
        val framebufferResolutionHandle = glGetUniformLocation(program, "resolution")
        glUniform2f(framebufferResolutionHandle, inputFramebufferInfo.textureSize.width.toFloat(), inputFramebufferInfo.textureSize.height.toFloat())


        // Input framebuffer
        val framebufferTextureHandle = glGetUniformLocation(program, "source_texture")
        glUniform1i(framebufferTextureHandle, inputFramebufferInfo.textureUnitPair.textureUnitIndex)
        glActiveTexture(inputFramebufferInfo.textureUnitPair.textureUnit)
        glBindTexture(GLES20.GL_TEXTURE_2D, inputFramebufferInfo.textureHandle)

        val convertedPoints = arrayListOf(
            convert(inputVertices.points[0]),
            convert(inputVertices.points[1]),
            convert(inputVertices.points[2]),
            convert(inputVertices.points[3])
        )

        val projected = calculateProjection(convertedPoints)

        reassignVertices(convertedPoints)
        reassignTexCoord(projected)
    }

    private fun calculateProjection(convertedPoints: ArrayList<Vec2Float>) : ArrayList<Vec3Float>{
        val intersection = lineLineIntersection(convertedPoints[0], convertedPoints[2], convertedPoints[1], convertedPoints[3])

        val distances = arrayListOf(
            distance(convertedPoints[0], intersection),
            distance(convertedPoints[1], intersection),
            distance(convertedPoints[2], intersection),
            distance(convertedPoints[3], intersection),
        )


        val projected = arrayListOf(
            perspectiveCorrect(convertedPoints[0], distances[0], distances[2]),
            perspectiveCorrect(convertedPoints[1], distances[1], distances[3]),
            perspectiveCorrect(convertedPoints[2], distances[2], distances[0]),
            perspectiveCorrect(convertedPoints[3], distances[3], distances[1]),
        )
        /*
            private val textureCoords = floatArrayOf(
        0f, 1f, 0f,
        0f, 0f, 0f,
        1f, 0f, 0f,
        1f, 1f, 0f,
        0f, 1f, 0f,
        1f, 0f, 0f,
    )

         */

        val texCoords = arrayListOf(
            Vec3Float(0f, 1f, 1f).multiply(projected[0].z),
            Vec3Float(0f, 0f, 1f).multiply(projected[1].z),
            Vec3Float(1f, 0f, 1f).multiply(projected[2].z),
            Vec3Float(1f, 1f, 1f).multiply(projected[3].z),
        )

        return texCoords
    }
    // See https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection
    private fun intersection(p1: Vec2Float, p2: Vec2Float, p3: Vec2Float, p4: Vec2Float): Vec2Float{

        val x = ((p1.x*p2.y - p1.y-p2.x)*(p3.x - p4.x) - (p1.x - p2.x)*(p3.x*p4.y - p3.y*p4.x)) /
                ((p1.x - p2.x)*(p3.y - p4.y) - (p1.y - p2.y)*(p3.x - p4.x));

        val y = ((p1.x*p2.y - p1.y-p2.x)*(p3.y - p4.y) - (p1.y - p2.y)*(p3.x*p4.y - p3.y*p4.x)) /
                ((p1.x - p2.x)*(p3.y - p4.y) - (p1.y - p2.y)*(p3.x - p4.x));

        return Vec2Float(x, y)
    }

    fun lineLineIntersection(A: Vec2Float, B: Vec2Float, C: Vec2Float, D: Vec2Float): Vec2Float {
        // Line AB represented as a1x + b1y = c1
        val a1: Float = B.y - A.y
        val b1: Float = A.x - B.x
        val c1: Float = a1 * A.x + b1 * A.y

        // Line CD represented as a2x + b2y = c2
        val a2: Float = D.y - C.y
        val b2: Float = C.x - D.x
        val c2: Float = a2 * C.x + b2 * C.y
        val determinant = a1 * b2 - a2 * b1
        return if (determinant == 0.0f) {
            // The lines are parallel. This is simplified
            // by returning a pair of FLT_MAX
            Vec2Float(Float.MAX_VALUE, Float.MAX_VALUE)
        } else {
            val x = (b2 * c1 - b1 * c2) / determinant
            val y = (a1 * c2 - a2 * c1) / determinant
            Vec2Float(x, y)
        }
    }


    private fun distance(p1: Vec2Float, p2: Vec2Float): Float{
        return sqrt((p2.x - p1.x).pow(2f) + (p2.y - p1.y).pow(2f))
    }
    private fun perspectiveCorrect(p: Vec2Float, d: Float, dOpp: Float): Vec3Float{
        val pOut = Vec3Float(p.x, p.y, 1f)
        val factor = (d + dOpp) / dOpp
        return pOut.multiply(factor)
    }


    /*
    Given
    P1, P2, P3 and P4

    Find lines
    P1->P3
    and
    P2->P4

    Find intersection between these points
    Intersection

    Get distances from each P to Intersection
    D1, D2, D3, D4

    Each point will then be
    float3(Pi.x, Pi.y, 1) * (di + d(i+2)) / d(i+2)
     */

    // See https://www.reedbeta.com/blog/quadrilateral-interpolation-part-1/

    private fun convert(v: Vec2Int): Vec2Float {
        return (Vec2Float(((v.x / 1080f )-0.5f)*2f, ((v.y / 1920f )-0.5f)*2f))
    }
}