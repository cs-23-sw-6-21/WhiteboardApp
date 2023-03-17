package dk.scuffed.whiteboardapp.pipeline.stages.points_stages

import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.utils.Vec2Int

/**
 * Stage that always has the points given in the constructor.
 */
internal class ScreenCornerPointsStage(
    pipeline: Pipeline
    ) : PointsOutputStage(pipeline, Vec2Int(0,0), Vec2Int(0,0), Vec2Int(0,0), Vec2Int(0,0))
{
    init {
    }

    override fun update() {
        points[0] = Vec2Int(0, getResolution().height)
        points[1] = Vec2Int(0, 0)
        points[2] = Vec2Int(getResolution().width, 0)
        points[3] = Vec2Int(getResolution().width, getResolution().height)

    }
}