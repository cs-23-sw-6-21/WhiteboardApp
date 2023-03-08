package dk.scuffed.whiteboardapp.pipeline.stages

import android.graphics.Bitmap
import android.util.Size
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.Stage
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