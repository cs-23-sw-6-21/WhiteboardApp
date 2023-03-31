package dk.scuffed.whiteboardapp.pipeline.stage_combinations

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import dk.scuffed.whiteboardapp.pipeline.FramebufferInfo
import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.GLOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.BitmapToFramebufferStage
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.FramebufferToBitmapStage
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.OpenCVDilateStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.SegmentationAccumulationStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.StoreStage
import dk.scuffed.whiteboardapp.pipeline.stages.segmentation_stages.SegmentationPostProcessingStage
import dk.scuffed.whiteboardapp.pipeline.stages.segmentation_stages.SegmentationPreProcessingStage
import dk.scuffed.whiteboardapp.pipeline.stages.segmentation_stages.SegmentationStage
import dk.scuffed.whiteboardapp.segmentation.PPSegmentation

/**
 * Runs the entire perspective correction based on two sets of points and an input framebuffer
 */
internal fun fullSegmentation(
    context: Context,
    inputFramebufferInfo: FramebufferInfo,
    pipeline: IPipeline
): GLOutputStage {
    val segPre = SegmentationPreProcessingStage(
        context,
        inputFramebufferInfo,
        PPSegmentation.Model.PORTRAIT,
        pipeline
    )

    val segBitmap = FramebufferToBitmapStage(
        segPre.frameBufferInfo,
        Bitmap.Config.ARGB_8888,
        pipeline
    )

    val seg = SegmentationStage(
        context,
        PPSegmentation.Model.PORTRAIT,
        segBitmap.outputBitmap,
        pipeline
    )


    val dilation = OpenCVDilateStage(
        seg,
        2.0,
        pipeline
    )


    val segFramebuffer = BitmapToFramebufferStage(
        dilation,
        pipeline
    )

    val lastAccumulator = pipeline.allocateFramebuffer(
        segFramebuffer,
        GLES20.GL_RGBA,
        segFramebuffer.frameBufferInfo.textureSize
    )

    val accumulationStage = SegmentationAccumulationStage(
        context,
        segFramebuffer.frameBufferInfo,
        lastAccumulator,
        pipeline
    )

    val storeAccumulatorStage = StoreStage(
        context,
        accumulationStage.frameBufferInfo,
        lastAccumulator,
        pipeline
    )

    val segPost = SegmentationPostProcessingStage(
        context,
        storeAccumulatorStage.frameBufferInfo,
        pipeline
    )

    return segPost
}