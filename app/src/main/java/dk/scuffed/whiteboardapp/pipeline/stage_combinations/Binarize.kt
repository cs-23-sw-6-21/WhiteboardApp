package dk.scuffed.whiteboardapp.pipeline.stage_combinations

import android.content.Context
import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.GLOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.*
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.BinarizationStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.GrayscaleStage

/**
 * Binarizes a coloured image using adaptive thresholding.
 */
internal fun binarize(
    context: Context,
    input: GLOutputStage,
    threshold: Float,
    downscaleFactor: Int,
    pipeline: IPipeline
): GLOutputStage {
    val grayscaleStage = GrayscaleStage(
        context,
        input.frameBufferInfo,
        pipeline
    )

    var prevStage: GLOutputStage = grayscaleStage
    for (i in 0 until downscaleFactor){
        prevStage = Downscale2xStage(context, prevStage.frameBufferInfo, pipeline)
    }

    val binarizeFast = BinarizationFastStage(context, grayscaleStage.frameBufferInfo, prevStage.frameBufferInfo, threshold, pipeline)

    return binarizeFast
}