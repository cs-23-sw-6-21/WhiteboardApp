package dk.scuffed.whiteboardapp.pipeline.stages

import dk.scuffed.whiteboardapp.pipeline.Pipeline
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
    val points: ArrayList<Vec2Int> = ArrayList()

    init {
        points.clear()
        points.addAll(initialPoints)
    }
}