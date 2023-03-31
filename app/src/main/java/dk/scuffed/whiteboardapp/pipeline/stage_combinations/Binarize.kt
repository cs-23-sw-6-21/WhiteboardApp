package dk.scuffed.whiteboardapp.pipeline.stage_combinations

import android.content.Context
import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.GLOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.*
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.BinarizationStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.GrayscaleStage

/**
 * Adds colour from camera back in the binarization
 */
internal fun binarize(
    context: Context,
    input: GLOutputStage,
    threshold: Float,
    pipeline: IPipeline
): GLOutputStage {
    val grayscaleStage = GrayscaleStage(
        context,
        input.frameBufferInfo,
        pipeline
    )

    val downscale = Downscale2xStage(context, grayscaleStage.frameBufferInfo, pipeline)
    val downscale1 = Downscale2xStage(context, downscale.frameBufferInfo, pipeline)
    val downscale2 = Downscale2xStage(context, downscale1.frameBufferInfo, pipeline)
    val downscale3 = Downscale2xStage(context, downscale2.frameBufferInfo, pipeline)
    val downscale4 = Downscale2xStage(context, downscale3.frameBufferInfo, pipeline)
    val downscale5 = Downscale2xStage(context, downscale4.frameBufferInfo, pipeline)

    val binarizeFast = BinarizationFastStage(context, grayscaleStage.frameBufferInfo, downscale5.frameBufferInfo, threshold, pipeline)

    return binarizeFast
}