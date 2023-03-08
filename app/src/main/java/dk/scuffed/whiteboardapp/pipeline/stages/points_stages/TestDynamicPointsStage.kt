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
        for (i in 0..points.size-1) {
            points[i] = Vec2Int(points[i].x + 1, points[i].y + 1)
        }
    }
}