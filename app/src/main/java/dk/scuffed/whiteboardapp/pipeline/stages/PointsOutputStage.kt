package dk.scuffed.whiteboardapp.pipeline.stages

import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.Stage
import dk.scuffed.whiteboardapp.utils.Vec2Int

internal abstract class PointsOutputStage(
    pipeline: Pipeline,
    vararg initialPoints: Vec2Int
    ) : Stage(pipeline)
{
    var points: Array<out Vec2Int>

    init {
        points = initialPoints
    }
}