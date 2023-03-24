package dk.scuffed.whiteboardapp.pipeline.stage_combinations

import android.content.Context
import android.opengl.GLES20
import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.stages.GLOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.*
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.BinarizationStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.MaskingStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.OverlayStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.StoreStage
import dk.scuffed.whiteboardapp.pipeline.stages.pipeline_stages.SwitchablePipelineStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DraggablePointsStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DrawCornersStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.ScreenCornerPointsStage

/**
 * Our canonical full pipeline that does everything except input/output
 */
internal fun fullPipeline(
    context: Context,
    inputStage: GLOutputStage,
    pipeline: IPipeline
): Pair<SwitchablePipelineStage, GLOutputStage> {
    val fullSegmentation = fullSegmentation(context, inputStage.frameBufferInfo, pipeline)

    val storedFramebuffer = pipeline.allocateFramebuffer(
        inputStage,
        GLES20.GL_RGBA,
        inputStage.frameBufferInfo.textureSize
    )

    val maskStage = MaskingStage(
        context,
        inputStage.frameBufferInfo,
        storedFramebuffer,
        fullSegmentation.frameBufferInfo,
        pipeline
    )
    val storeStage = StoreStage(
        context,
        maskStage.frameBufferInfo,
        storedFramebuffer,
        pipeline
    )

    val switchablePipelineStage = SwitchablePipelineStage({pipeline->
        fullCornerDetection(context, storeStage, pipeline)
    }, {pipeline -> DraggablePointsStage(pipeline)}, pipeline)


    val drawCorners = DrawCornersStage(
        context,
        pipeline,
        switchablePipelineStage.pointsOutputStage
    )


    val screenPointsStage = ScreenCornerPointsStage(pipeline)
    val perspectiveCorrection = fullPerspectiveCorrection(
        context,
        storeStage,
        switchablePipelineStage.pointsOutputStage,
        screenPointsStage,
        pipeline
    )

    val grayscaleStage = GrayscaleStage(
        context,
        perspectiveCorrection.frameBufferInfo,
        pipeline
    )

    val binarizationStage = BinarizationStage(
        context,
        grayscaleStage.frameBufferInfo,
        3,
        40f,
        pipeline
    )

    val overlay = OverlayStage(
        context,
        binarizationStage.frameBufferInfo,
        drawCorners.frameBufferInfo,
        pipeline
    )
    return Pair(switchablePipelineStage, overlay)
}