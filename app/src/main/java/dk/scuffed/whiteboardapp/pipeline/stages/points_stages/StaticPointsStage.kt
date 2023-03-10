package dk.scuffed.whiteboardapp.pipeline.stages.points_stages

import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.utils.Vec2Int

/**
 * Stage that always has the points given in the constructor.
 */
internal class StaticPointsStage(
    pipeline: Pipeline,
    vararg staticpoints: Vec2Int
    ) : PointsOutputStage(pipeline, *staticpoints)
{
    init {
    }

    override fun update() {
    }
}