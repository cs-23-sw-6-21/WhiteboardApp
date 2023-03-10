package dk.scuffed.whiteboardapp.pipeline.stages.points_stages

import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.utils.Vec2Int

/**
 * Simple test stage that offsets the given points on x and y :).
 */
internal class TestDynamicPointsStage(
    pipeline: Pipeline,
    vararg dynamicPoints: Vec2Int
    ) : PointsOutputStage(pipeline, *dynamicPoints)
{
    init {
    }



    override fun update() {
        for (i in 0..points.size-1) {
            points[i] = Vec2Int(points[i].x + 1, points[i].y + 1)
        }
    }
}