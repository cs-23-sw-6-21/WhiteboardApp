package dk.scuffed.whiteboardapp.pipeline.stages.points_stages

import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.utils.Vec2Int
import org.opencv.core.*
import org.opencv.imgproc.Imgproc


/**
 * Stage that outputs the points required so that pointsFrom will be at pointsTo.
 * Requires everything to have 4 points, as this is designed for quads.
 * @param pointsFrom the points forming a quad that we want to be moved to pointsTo.
 * @param pointsTo the points forming a quad that we want pointsFrom to be moved to.
 * @returns the points of the quad that will ensure pointsFrom is distorted so they are at pointsTo.
 */
internal class PerspectiveTransformPointsStage(
    pipeline: IPipeline,
    private val pointsFrom: PointsOutputStage,
    private val pointsTo: PointsOutputStage
) : PointsOutputStage(pipeline) {
    init {
        setInitialPoints()
    }

    override fun update() {
        assert(pointsFrom.points.size == 4)
        assert(pointsTo.points.size == 4)

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

        val perspectiveTransformMatrix = Imgproc.getPerspectiveTransform(src, dst)

        val result = MatOfPoint2f()

        Core.perspectiveTransform(dst, result, perspectiveTransformMatrix)

        val newPoints = result.toArray()

        for (i in points.indices) {
            points[i] = Vec2Int(newPoints[i].x.toInt(), newPoints[i].y.toInt())
        }

        perspectiveTransformMatrix.release()
        result.release()
        src.release()
        dst.release()
    }

    private fun setInitialPoints() {
        points.addAll(
            arrayOf(
                Vec2Int(200, 200),
                Vec2Int(800, 200),
                Vec2Int(800, 800),
                Vec2Int(200, 800),
            )
        )
    }
}