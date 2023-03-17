package dk.scuffed.whiteboardapp.pipeline.StageCombinations

import android.content.Context
import android.graphics.Bitmap
import dk.scuffed.whiteboardapp.pipeline.FramebufferInfo
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.GLOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.BitmapToFramebufferStage
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.FramebufferToBitmapStage
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.OpenCVDilateStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.GaussianBlurStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.PerspectiveCorrectionStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.PerspectiveTransformPointsStage
import dk.scuffed.whiteboardapp.pipeline.stages.segmentation_stages.SegmentationPostProcessingStage
import dk.scuffed.whiteboardapp.pipeline.stages.segmentation_stages.SegmentationPreProcessingStage
import dk.scuffed.whiteboardapp.pipeline.stages.segmentation_stages.SegmentationStage
import dk.scuffed.whiteboardapp.segmentation.PPSegmentation

/**
 * Runs the entire perspective correction based on two sets of points and an input framebuffer
 */
internal fun fullSegmentation(context: Context, inputFramebufferInfo: FramebufferInfo, pipeline: Pipeline): GLOutputStage {
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

    val segFramebufferInfo = BitmapToFramebufferStage(
        dilation,
        pipeline
    )

    val segPost = SegmentationPostProcessingStage(
        context,
        segFramebufferInfo.frameBufferInfo,
        pipeline
    )

    return  segPost
}