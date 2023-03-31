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
import dk.scuffed.whiteboardapp.pipeline.stages.pipeline_stages.SwitchablePointPipeline
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.CornersFromResolutionStage
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
): Pair<SwitchablePointPipeline, GLOutputStage> {
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

    val switchablePointPipeline = SwitchablePointPipeline(
        { pipeline -> DraggablePointsStage(pipeline) },
        { pipeline -> fullCornerDetection(context, storeStage, pipeline) },
        pipeline
    )


    val drawCorners = DrawCornersStage(
        context,
        pipeline,
        switchablePointPipeline.pointsOutputStage
    )


    val cameraPointsStage =
        CornersFromResolutionStage(inputStage.frameBufferInfo.textureSize, pipeline)
    val perspectiveCorrection = fullPerspectiveCorrection(
        context,
        storeStage,
        switchablePointPipeline.pointsOutputStage,
        cameraPointsStage,
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
        5,
        25f,
        pipeline
    )

    val readdedColour = addColour(
        context,
        perspectiveCorrection,
        binarizationStage,
        pipeline
    )

    val overlay = OverlayStage(
        context,
        readdedColour.frameBufferInfo,
        drawCorners.frameBufferInfo,
        pipeline
    )


    return Pair(switchablePointPipeline, overlay)
}