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

    // ------------------ LINE DETECTION STUFF START --------------
    val storedFramebuffer = pipeline.allocateFramebuffer(
        inputStage,
        GLES20.GL_RGBA,
        inputStage.frameBufferInfo.textureSize
    )

    val maskStage = MaskingStage(
        context,
        storedFramebuffer,
        inputStage.frameBufferInfo,
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

    // --------------- LINE DETECTION STUFF END


    val cameraPointsStage =
        CornersFromResolutionStage(inputStage.frameBufferInfo.textureSize, pipeline)
    val cameraCorrected = fullPerspectiveCorrection(
        context,
        inputStage,
        switchablePointPipeline.pointsOutputStage,
        cameraPointsStage,
        pipeline
    )
    
    val maskCorrected = fullPerspectiveCorrection(
        context,
        fullSegmentation,
        switchablePointPipeline.pointsOutputStage,
        cameraPointsStage,
        pipeline
    )

    val whitebalance = whiteBalance(
        context,
        cameraCorrected,
        5,
        pipeline
    )

    val binarized = binarize(
        context,
        cameraCorrected,
        perspectiveCorrection,
        7.5f,
        5,
        pipeline)

    val readdedColour = addColour(
        context,
        whitebalance,
        binarized,
        pipeline
    )

    val oldAccumulator = pipeline.allocateFramebuffer(binarized, binarized.frameBufferInfo.textureFormat, binarized.frameBufferInfo.textureSize)

    val maskedAccumulation = MaskedAccumulationStage(context, readdedColour.frameBufferInfo, oldAccumulator, maskCorrected.frameBufferInfo, 0.2f, pipeline)

    val storeAccumulator = StoreStage(context, maskedAccumulation.frameBufferInfo, oldAccumulator, pipeline)

    // INSERT MASKED ACCUMULATOR HERE

    val thresholded = StepStage(context, maskedAccumulation.frameBufferInfo, 0.5f, pipeline)



    val overlay = OverlayStage(
        context,
        thresholded.frameBufferInfo,
        drawCorners.frameBufferInfo,
        pipeline
    )


    return Pair(switchablePointPipeline, thresholded)
}