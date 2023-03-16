package dk.scuffed.whiteboardapp.pipeline.StageCombinations

import android.content.Context
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.GLOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.GaussianBlurStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.OverlayStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.PerspectiveCorrectionStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DraggablePointsStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DrawCornersStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.PerspectiveTransformPointsStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.ResolutionPointsStage

/**
 * Runs the entire perspective correction based on draggable points and screen points and an input framebuffer
 */
internal fun perspectiveCorrectionTestPipeline(context: Context, inputStage: GLOutputStage, pipeline: Pipeline): GLOutputStage {
    val draggablePoints = DraggablePointsStage(
        pipeline
    )

    val resolutionPoints = ResolutionPointsStage(
        pipeline
    )


    val perspectiveTransformPointsStage = fullPerspectiveCorrection(
        context,
        inputStage,
        draggablePoints,
        resolutionPoints,
        pipeline
    )

    val drawCorners = DrawCornersStage(
        context,
        pipeline,
        draggablePoints
    )

    val overlay = OverlayStage(
        context,
        perspectiveTransformPointsStage.frameBufferInfo,
        drawCorners.frameBufferInfo,
        pipeline
    )

    return  overlay
}