package dk.scuffed.whiteboardapp.pipeline.stages

import android.graphics.Bitmap
import android.util.Size
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.Stage

internal abstract class BitmapOutputStage(
    pipeline: Pipeline,
    resolution: Size,
    config: Bitmap.Config
    ) : Stage(pipeline)
{
    public var outputBitmap: Bitmap

    init {
        outputBitmap = Bitmap.createBitmap(resolution.width, resolution.height, config)
    }
}