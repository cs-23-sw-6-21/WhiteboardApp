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
internal fun whiteBalance(
    context: Context,
    input: GLOutputStage,
    downscaleFactor: Int,
    pipeline: IPipeline
): GLOutputStage {
    var prevStage: GLOutputStage = input
    for (i in 0 until downscaleFactor){
        prevStage = Downscale2xStage(context, prevStage.frameBufferInfo, pipeline)
    }

    val whiteBalanceFastStage = SubtractStage(context, prevStage.frameBufferInfo, input.frameBufferInfo, pipeline)


    val hsvAdjustmentsStage = HSVAdjustmentsStage(context, whiteBalanceFastStage.frameBufferInfo, pipeline)


    return hsvAdjustmentsStage
}