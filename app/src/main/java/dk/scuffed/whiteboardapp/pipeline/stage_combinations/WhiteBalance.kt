package dk.scuffed.whiteboardapp.pipeline.stage_combinations

import android.content.Context
import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.stages.GLOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.*

/**
 * Applies the fast white balancing, by downscaling the image and using it as the background color.
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

    val whiteBalanceFastStage = WhitebalanceStage(context, prevStage.frameBufferInfo, input.frameBufferInfo, pipeline)


    val hsvAdjustmentsStage = HSVAdjustmentsStage(context, whiteBalanceFastStage.frameBufferInfo, pipeline)


    return hsvAdjustmentsStage
}