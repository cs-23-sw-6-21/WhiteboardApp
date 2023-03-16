package dk.scuffed.whiteboardapp.pipeline.StageCombinations

import android.content.Context
import android.graphics.Bitmap
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.GLOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.FramebufferToBitmapStage
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.OpenCVLineDetectionStage
import dk.scuffed.whiteboardapp.pipeline.stages.lines_stages.BiggestSquareStage
import dk.scuffed.whiteboardapp.pipeline.stages.lines_stages.LinesAngleDiscriminatorStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.OverlayStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DrawCornersStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DrawLinesStage
import dk.scuffed.whiteboardapp.utils.Color

/**
 * Does corner detection using hough line transforms and finding the biggest quadrilateral.
 * @return a pair consisting of points found, and a framebuffer containing overlaid debug corners and lines
 */
internal fun fullCornerDetectionWithDebugDrawing(context: Context, inputStage: GLOutputStage, pipeline: Pipeline): Pair<PointsOutputStage, GLOutputStage> {

    val edges = fullCannyEdgeDetection(
        context,
        inputStage,
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
        pipeline
    )

    val verticalLinesAngleDiscriminatorStage = LinesAngleDiscriminatorStage(
        openCVLineDetectionStage,
        -(Math.PI / 4.0f).toFloat(),
        (Math.PI / 4.0f).toFloat(),
        pipeline
    )

    val verticalDrawLinesStage = DrawLinesStage(
        context,
        verticalLinesAngleDiscriminatorStage,
        Color(1.0f, 0.0f, 0.0f, 1.0f),
        pipeline
    )

    val horizontalLinesAngleDiscriminatorStage = LinesAngleDiscriminatorStage(
        openCVLineDetectionStage,
        (Math.PI / 4.0f).toFloat(),
        (Math.PI / 2.0f + Math.PI / 4.0f).toFloat(),
        pipeline
    )

    val horizontalDrawLinesStage = DrawLinesStage(
        context,
        horizontalLinesAngleDiscriminatorStage,
        Color(0.0f, 1.0f, 0.0f, 1.0f),
        pipeline
    )


    val verticalOverlayStage = OverlayStage(
        context,
        inputStage.frameBufferInfo,
        verticalDrawLinesStage.frameBufferInfo,
        pipeline
    )

    val horizontalOverlayStage = OverlayStage(
        context,
        verticalOverlayStage.frameBufferInfo,
        horizontalDrawLinesStage.frameBufferInfo,
        pipeline
    )

    val biggestSquareStage = BiggestSquareStage(
        horizontalLinesAngleDiscriminatorStage,
        verticalLinesAngleDiscriminatorStage,
        pipeline
    )

    val drawCornersStage = DrawCornersStage(
        context,
        pipeline,
        biggestSquareStage
    )

    val debugOverlay = OverlayStage(
        context,
        horizontalOverlayStage.frameBufferInfo,
        drawCornersStage.frameBufferInfo,
        pipeline
    )

    return Pair(biggestSquareStage, debugOverlay)
}