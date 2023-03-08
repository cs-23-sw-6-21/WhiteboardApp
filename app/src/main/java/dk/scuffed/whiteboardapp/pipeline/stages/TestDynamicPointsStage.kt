package dk.scuffed.whiteboardapp.pipeline.stages

import android.graphics.Bitmap
import android.util.Size
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.Stage
import dk.scuffed.whiteboardapp.utils.Vec2Int

internal class TestDynamicPointsStage(
    pipeline: Pipeline,
    vararg dynamicPoints: Vec2Int
    ) : PointsOutputStage(pipeline, *dynamicPoints)
{
    init {
    }



    override fun update() {
        for (v in points) {
            v.x++
            v.y++
        }
    }
}