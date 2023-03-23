package dk.scuffed.whiteboardapp.pipeline.stage_combinations

import android.content.Context
import android.opengl.GLES20
import dk.scuffed.whiteboardapp.pipeline.FramebufferInfo
import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.stages.GLOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.update_every_x_frames_stages.UpdateEveryXFramesPointsStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.*
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.BinarizationStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.MaskingStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.OverlayStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.StoreStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DrawCornersStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DrawLinesStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.ScreenCornerPointsStage
import dk.scuffed.whiteboardapp.utils.Vec2Int

/**
 * Our canonical full pipeline that does everything except input/output
 */
internal fun fullPipeline(
    context: Context,
    inputStage: GLOutputStage,
    pipeline: IPipeline
): GLOutputStage {
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


    val cornerDetectionEveryXFrames = UpdateEveryXFramesPointsStage(
        {
            fullCornerDetection(context, storeStage, it)
        },
        10,
        pipeline,
        Vec2Int(0, 0),
        Vec2Int(0, 0),
        Vec2Int(0, 0),
        Vec2Int(0, 0)
    )

    val drawCorners = DrawCornersStage(
        context,
        pipeline,
        cornerDetectionEveryXFrames.getPointsOutputStage()
    )


    val screenPointsStage = ScreenCornerPointsStage(pipeline)
    val perspectiveCorrection = fullPerspectiveCorrection(
        context,
        storeStage,
        cornerDetectionEveryXFrames.getPointsOutputStage(),
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
    return overlay
}