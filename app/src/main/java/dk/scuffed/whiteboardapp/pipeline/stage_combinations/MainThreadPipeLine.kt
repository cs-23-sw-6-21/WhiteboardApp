package dk.scuffed.whiteboardapp.pipeline.stage_combinations

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.stages.GLOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.FramebufferToBitmapStage
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.OpenCVLineDetectionStage
import dk.scuffed.whiteboardapp.pipeline.stages.lines_stages.LinesAngleDiscriminatorStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.*
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.BinarizationStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.MaskingStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.OverlayStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.StoreStage
import dk.scuffed.whiteboardapp.pipeline.stages.pipeline_stages.SwitchablePointPipeline
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.BiggestQuadStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.CornersFromResolutionStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DraggablePointsStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DrawCornersStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.ScreenCornerPointsStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.WeightedPointsStage

/**
 * Our canonical full pipeline that does everything except input/output
 */
internal fun mainThreadPipeline(
    context: Context,
    inputStage: GLOutputStage,
    pipeline: IPipeline
): GLOutputStage {

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

    val edges = fullCannyEdgeDetection(
        context,
        storeStage,
        pipeline
    )

    val edgesBitmapStage = FramebufferToBitmapStage(
        edges.frameBufferInfo,
        Bitmap.Config.ARGB_8888,
        pipeline
    )

    val openCVLineDetectionStage = OpenCVLineDetectionStage(
        edgesBitmapStage,
        150,
        50,
        pipeline,
        1.0,
        Math.PI / 180.0,
        15.0,
        Math.PI / 75.0
    )

    val verticalLinesAngleDiscriminatorStage = LinesAngleDiscriminatorStage(
        openCVLineDetectionStage,
        -(Math.PI / 4.0f).toFloat(),
        (Math.PI / 4.0f).toFloat(),
        pipeline
    )

    val horizontalLinesAngleDiscriminatorStage = LinesAngleDiscriminatorStage(
        openCVLineDetectionStage,
        (Math.PI / 4.0f).toFloat(),
        (Math.PI / 2.0f + Math.PI / 4.0f).toFloat(),
        pipeline
    )

    val biggestQuadStage = BiggestQuadStage(
        horizontalLinesAngleDiscriminatorStage,
        verticalLinesAngleDiscriminatorStage,
        pipeline
    )

    val weightedCornerStage = WeightedPointsStage(
        biggestQuadStage,
        20,
        5.0f,
        pipeline
    )

    val drawCorners = DrawCornersStage(
        context,
        pipeline,
        weightedCornerStage
    )

    // --------------- LINE DETECTION STUFF END


    // ------------------ PERSPECTIVE CORRECTION START --------------

    val cameraPointsStage =
        CornersFromResolutionStage(inputStage.frameBufferInfo.textureSize, pipeline)

    val perspectiveCorrected = fullPerspectiveCorrection(
        context,
        maskStage,
        weightedCornerStage,
        cameraPointsStage,
        pipeline
    )

    // ------------------ PERSPECTIVE CORRECTION END --------------


    // ------------------ POST PROCESSING START --------------

    val whitebalance = whiteBalance(
        context,
        perspectiveCorrected,
        5,
        pipeline
    )

    val binarized = binarize(
        context,
        perspectiveCorrected,
        7.5f,
        3,
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

    return overlay
}