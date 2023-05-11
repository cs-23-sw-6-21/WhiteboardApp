package dk.scuffed.whiteboardapp.pipeline.stage_combinations

import android.content.Context
import android.util.Size
import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.GLOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.OverlayStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.PerspectiveCorrectionStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DrawCornersStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.PerspectiveTransformPointsStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.TestTransform
import dk.scuffed.whiteboardapp.utils.Vec2Int

/**
 * Runs the entire perspective correction based on two sets of points and an input framebuffer
 */
internal fun testPerspectiveCorrection(
    context: Context,
    inputStage: GLOutputStage,
    pointsFrom: PointsOutputStage,
    pointsTo: PointsOutputStage,
    pipeline: IPipeline
): GLOutputStage {
    val perspectiveTransformPointsStage = PerspectiveTransformPointsStage(
        pipeline,
        pointsFrom,
        pointsTo
    )

    val aa = TestTransform(perspectiveTransformPointsStage, pipeline)

    val perspectiveCorrectionStage = PerspectiveCorrectionStage(
        context,
        inputStage.frameBufferInfo,
        aa,
        pipeline
    )

    val perspectiveCorrectionStagea = PerspectiveCorrectionStage(
        context,
        inputStage.frameBufferInfo,
        perspectiveTransformPointsStage,
        pipeline
    )


    val drawCorrectionStage = DrawCornersStage(context, pipeline, pointsTo, Size(2048, 2048))

    val overlay = OverlayStage(context, inputStage.frameBufferInfo, drawCorrectionStage.frameBufferInfo, pipeline)

    return overlay
}