package dk.scuffed.whiteboardapp.pipeline.stages.points_stages

import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.utils.Vec2Float
import dk.scuffed.whiteboardapp.utils.Vec2Int
import kotlin.math.round

internal class TestTransform(
    private val inputPoints: PointsOutputStage,
    pipeline: IPipeline
) : PointsOutputStage(pipeline, inputPoints.points[0], inputPoints.points[1], inputPoints.points[2], inputPoints.points[3]) {


    override fun update() {

        val a = Vec2Int(0, 0)
        val b = 3
        val c = Vec2Int(500,1000)
        points[0] = (inputPoints.points[0] + a) / b + c
        points[1] = (inputPoints.points[1] + a) / b + c
        points[2] = (inputPoints.points[2] + a) / b + c
        points[3] = (inputPoints.points[3] + a) / b + c
    }
}