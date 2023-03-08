package dk.scuffed.whiteboardapp.pipeline.stages

import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.Stage
import dk.scuffed.whiteboardapp.utils.Vec2Int

/**
 * Baseclass for stages that output points.
 * @property points allows to read and write these points.
 */
internal abstract class PointsOutputStage(
    pipeline: Pipeline,
    vararg initialPoints: Vec2Int
    ) : Stage(pipeline)
{
    var points: Array<Vec2Int>

    init {
        points = initialPoints as Array<Vec2Int>
    }
}