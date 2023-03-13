package dk.scuffed.whiteboardapp.pipeline.stages.points_stages

import android.util.Log
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.utils.Vec2Int
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.round


/**
 * Stage that always has the points given in the constructor.
 */
internal class PerspectiveTransformPointsStage(
    pipeline: Pipeline,
    val pointsFrom: PointsOutputStage,
    val pointsTo: PointsOutputStage
    ) : PointsOutputStage(pipeline)
{
    init {
        setInitialPoints()
    }

    override fun update() {
        val src = MatOfPoint2f(
            Point(pointsFrom.points[0].x.toDouble(), pointsFrom.points[0].y.toDouble()),
            Point(pointsFrom.points[1].x.toDouble(), pointsFrom.points[1].y.toDouble()),
            Point(pointsFrom.points[2].x.toDouble(), pointsFrom.points[2].y.toDouble()),
            Point(pointsFrom.points[3].x.toDouble(), pointsFrom.points[3].y.toDouble()),
            )
        val dst = MatOfPoint2f(
            Point(pointsTo.points[0].x.toDouble(), pointsTo.points[0].y.toDouble()),
            Point(pointsTo.points[1].x.toDouble(), pointsTo.points[1].y.toDouble()),
            Point(pointsTo.points[2].x.toDouble(), pointsTo.points[2].y.toDouble()),
            Point(pointsTo.points[3].x.toDouble(), pointsTo.points[3].y.toDouble()),
        )

        val warpmat = Imgproc.getPerspectiveTransform(src, dst)

        val result = MatOfPoint2f()


        //Imgproc.warpPerspective(dst, result, warpmat, src.size())
        Core.perspectiveTransform(dst, result, warpmat)

        //Log.d("sdklfs", "Size: " + result)



        val newpoints = result.toArray()


        for (i in points.indices) {
            points[i] = Vec2Int(newpoints[i].x.toInt(), newpoints[i].y.toInt())
        }

        /*
        val src = MatOfPoint2f(
            pointsFrom.points,
            sortedPoints.get(1),
            sortedPoints.get(2),
            sortedPoints.get(3)
        )

        var dst: MatOfPoint2f? = MatOfPoint2f(
            Point(0, 0),
            Point(450 - 1, 0),
            Point(0, 450 - 1),
            Point(450 - 1, 450 - 1)
        )        Imgproc.getPerspectiveTransform()
        val transformMatrix = Core.p
        */

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