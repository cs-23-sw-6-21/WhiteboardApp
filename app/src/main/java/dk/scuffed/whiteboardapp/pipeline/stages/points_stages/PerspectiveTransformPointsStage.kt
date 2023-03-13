package dk.scuffed.whiteboardapp.pipeline.stages.points_stages

import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.utils.Vec2Int
import org.opencv.core.Core

/**
 * Stage that always has the points given in the constructor.
 */
internal class PerspectiveTransformPointsStage(
    pipeline: Pipeline,
    pointsFrom: PointsOutputStage,
    pointsTo: PointsOutputStage
    ) : PointsOutputStage(pipeline)
{
    init {
    }

    override fun update() {
        val transformMatrix = Core.perspectiveTransform()
    }
}