package dk.scuffed.whiteboardapp.pipeline.stage_combinations

import android.content.Context
import android.icu.number.Scale
import android.opengl.GLES20
import android.util.Size
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


    // ------------------ SEGMENTATION STUFF START --------------

    val fullSegmentation = fullSegmentation(context, inputStage.frameBufferInfo, pipeline)


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

    // ------------------ SEGMENTATION STUFF END --------------


    // ------------------ LINE DETECTION STUFF START --------------

    val switchablePointPipeline = SwitchablePointPipeline(
        context,
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


    // ------------------ PERSPECTIVE CORRECTION START --------------

    val cameraPointsStage =
        CornersFromResolutionStage(inputStage.frameBufferInfo.textureSize, pipeline)

    val perspectiveCorrected = fullPerspectiveCorrection(
        context,
        maskStage,
        switchablePointPipeline.pointsOutputStage,
        cameraPointsStage,
        pipeline
    )

    GenerateMipmapStage(perspectiveCorrected.frameBufferInfo, false, pipeline)

    // ------------------ PERSPECTIVE CORRECTION END --------------


    // ------------------ POST PROCESSING START --------------

    val downscaled = ScaleToResolution(
        context, perspectiveCorrected.frameBufferInfo,
        Size(perspectiveCorrected.frameBufferInfo.textureSize.width / 32, perspectiveCorrected.frameBufferInfo.textureSize.height / 32),
        pipeline
    )

    val whitebalance = whiteBalance(
        context,
        perspectiveCorrected,
        downscaled,
        pipeline
    )


    val binarized = binarize(
        context,
        perspectiveCorrected,
        downscaled,
        7.5f,
        pipeline)

    val readdedColour = addColour(
        context,
        whitebalance,
        binarized,
        pipeline
    )

    // ------------------ POST PROCESSING END --------------


    val overlay = OverlayStage(
        context,
        readdedColour.frameBufferInfo,
        drawCorners.frameBufferInfo,
        pipeline
    )


    return Pair(switchablePointPipeline, overlay)
}