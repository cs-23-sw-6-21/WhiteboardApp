package dk.scuffed.whiteboardapp.pipeline.StageCombinations

import android.content.Context
import android.opengl.GLES20
import dk.scuffed.whiteboardapp.pipeline.FramebufferInfo
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.GLOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.*
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.BinarizationStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.MaskingStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.OverlayStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.StoreStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DraggablePointsStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DrawCornersStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.ScreenCornerPointsStage

/**
 * Our canonical full pipeline that does everything except input/output
 */
internal fun fullPipeline(context: Context, inputStage: GLOutputStage, pipeline: Pipeline): GLOutputStage {
    val fullSegmentation = fullSegmentation(context, inputStage.frameBufferInfo, pipeline)

    val storedFramebuffer: FramebufferInfo = pipeline.allocateFramebuffer(inputStage, GLES20.GL_RGBA, inputStage.frameBufferInfo.textureSize.width, inputStage.frameBufferInfo.textureSize.height)
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


    //val cornerDetection = fullCornerDetection(context, storeStage, this)
    val draggablePointsStage = DraggablePointsStage(pipeline)
    val drawCorners = DrawCornersStage(
        context,
        pipeline,
        draggablePointsStage
    )


    val screenPointsStage = ScreenCornerPointsStage(pipeline)
    val perspectiveCorrection = fullPerspectiveCorrection(
        context,
        storeStage,
        draggablePointsStage,
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
        10,
        20f,
        pipeline
    )

    val overlay = OverlayStage(
        context,
        binarizationStage.frameBufferInfo,
        drawCorners.frameBufferInfo,
        pipeline
    )
    return  overlay
}