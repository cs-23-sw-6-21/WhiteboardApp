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
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DrawCornerHistoryStage
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
        { pipeline -> DraggablePointsStage(pipeline) },
        { pipeline -> fullCornerDetection(context, storeStage, pipeline) },
        pipeline
    )

    val historyCorners = DrawCornerHistoryStage(
        context,
        pipeline,
        switchablePointPipeline.pointsOutputStage
    )

    /*val drawCorners = DrawCornersStage(
        context,
        pipeline,
        switchablePointPipeline.pointsOutputStage
    )*/

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

    val overlayCorners = OverlayStage(
        context,
        readdedColour.frameBufferInfo,
        historyCorners.frameBufferInfo,
        pipeline
    )

    /*val overlay = OverlayStage(
        context,
        overlayCorners.frameBufferInfo,
        drawCorners.frameBufferInfo,
        pipeline
    )*/


    return Pair(switchablePointPipeline, overlayCorners)
}