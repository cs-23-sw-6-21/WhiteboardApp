package dk.scuffed.whiteboardapp.pipeline.stages.points_stages

import android.util.Log
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.utils.Vec2Float
import dk.scuffed.whiteboardapp.utils.Vec2Int
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.absoluteValue
import kotlin.math.round


/**
 */
internal class AspectRatioCorrectionStage(
    pipeline: Pipeline,
    private val inputPoints: PointsOutputStage,
    private val screenPoints: PointsOutputStage
) : PointsOutputStage(pipeline)
{
    init {
        setInitialPoints()
    }

    override fun update() {
        assert(inputPoints.points.size == 4)
        assert(screenPoints.points.size == 4)

        // In width/height
        var desiredAspect = approxAspectRatio(inputPoints.points)

        var actualAspect =
            ((screenPoints.points[0].x - screenPoints.points[2].x).absoluteValue).toFloat() /
                    ((screenPoints.points[0].y - screenPoints.points[2].y).absoluteValue).toFloat()


        // switch to landscape
        actualAspect = 1f/actualAspect
        desiredAspect = 1f/desiredAspect

        val correctedPoints = aspectCorrectedPoints(screenPoints.points, actualAspect, desiredAspect)


        for (i in inputPoints.points.indices) {
            points[i] = correctedPoints[i].toVec2Int()
            //points[i] = points[i]
        }
    }

    private fun approxAspectRatio(points: ArrayList<Vec2Int>): Float {
        val xCoords = arrayListOf<Int>(points[0].x, points[1].x, points[2].x, points[3].x).sortedBy { it }
        val yCoords = arrayListOf<Int>(points[0].y, points[1].y, points[2].y, points[3].y).sortedBy { it }



        return (xCoords[0] - xCoords[3]).toFloat() / (yCoords[0] - yCoords[3]).toFloat()
    }

    private fun aspectCorrectedPoints(points: ArrayList<Vec2Int>, actualAspect: Float, desiredAspect: Float): ArrayList<Vec2Float> {
        val correctedPoints = ArrayList<Vec2Float>()
        val aspectCorrection = actualAspect/desiredAspect

        val offset = Vec2Float(getResolution().width.toFloat(), getResolution().height.toFloat()).multiply(0.5f)



        for (p in points) {
            var centered = p.vec2IntToFloat()
                .subtact(offset)

            var corrected = Vec2Float(centered.x * aspectCorrection, centered.y).additon(offset)

            correctedPoints.add(corrected)
        }
        return correctedPoints
    }

    private fun setInitialPoints() {
        points.addAll(arrayOf(
            Vec2Int(200, 200),
            Vec2Int(800, 200),
            Vec2Int(800, 800),
            Vec2Int(200, 800),
        ))
    }
}