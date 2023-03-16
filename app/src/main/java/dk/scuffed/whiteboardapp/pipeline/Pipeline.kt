package dk.scuffed.whiteboardapp.pipeline

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Camera
import android.opengl.GLES20
import android.util.Size
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.opengl.*
import dk.scuffed.whiteboardapp.pipeline.StageCombinations.*
import dk.scuffed.whiteboardapp.pipeline.StageCombinations.fullSegmentation
import dk.scuffed.whiteboardapp.pipeline.StageCombinations.perspectiveCorrectionTestPipeline
import dk.scuffed.whiteboardapp.pipeline.stages.*
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.BitmapToFramebufferStage
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.FramebufferToBitmapStage
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.OpenCVLineDetectionStage
import dk.scuffed.whiteboardapp.pipeline.stages.input_stages.CameraXStage
import dk.scuffed.whiteboardapp.pipeline.stages.lines_stages.BiggestSquareStage
import dk.scuffed.whiteboardapp.pipeline.stages.lines_stages.LinesAngleDiscriminatorStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.*
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.CannyStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.GaussianBlurStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.GrayscaleStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.PerspectiveCorrectionStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.SobelStage
import dk.scuffed.whiteboardapp.pipeline.stages.output_stages.DrawFramebufferStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.*
import dk.scuffed.whiteboardapp.pipeline.stages.segmentation_stages.SegmentationPostProcessingStage
import dk.scuffed.whiteboardapp.pipeline.stages.segmentation_stages.SegmentationPreProcessingStage
import dk.scuffed.whiteboardapp.pipeline.stages.segmentation_stages.SegmentationStage
import dk.scuffed.whiteboardapp.segmentation.PPSegmentation
import dk.scuffed.whiteboardapp.utils.Color
import dk.scuffed.whiteboardapp.utils.Vec2Int

class Pipeline(context: Context, internal val initialResolution: Size) {

    private var stages = mutableListOf<Stage>()
    private var nextTextureUnit: Int = 0

    private val indexToTextureUnit = intArrayOf(
        GLES20.GL_TEXTURE0,
        GLES20.GL_TEXTURE1,
        GLES20.GL_TEXTURE2,
        GLES20.GL_TEXTURE3,
        GLES20.GL_TEXTURE4,
        GLES20.GL_TEXTURE5,
        GLES20.GL_TEXTURE6,
        GLES20.GL_TEXTURE7,
        GLES20.GL_TEXTURE8,
        GLES20.GL_TEXTURE9,
        GLES20.GL_TEXTURE10,
        GLES20.GL_TEXTURE11,
        GLES20.GL_TEXTURE12,
        GLES20.GL_TEXTURE13,
        GLES20.GL_TEXTURE14,
        GLES20.GL_TEXTURE15,
        GLES20.GL_TEXTURE16,
        GLES20.GL_TEXTURE17,
        GLES20.GL_TEXTURE18,
        GLES20.GL_TEXTURE19,
    )

    init {
        glDisable(GLES20.GL_BLEND)
        glDisable(GLES20.GL_CULL_FACE)
        glDisable(GLES20.GL_DEPTH_TEST)
        glClearColorError()


        val opt = BitmapFactory.Options()
        opt.inScaled = false


        val cameraXStage = CameraXStage(
            context,
            this
        )
        /*
        val fullSegmentation = fullSegmentation(context, cameraXStage.frameBufferInfo, this)

        val storedFramebuffer: FramebufferInfo = allocateFramebuffer(cameraXStage, GLES20.GL_RGBA, cameraXStage.frameBufferInfo.textureSize.width, cameraXStage.frameBufferInfo.textureSize.height)
        val maskStage = MaskingStage(
            context,
            cameraXStage.frameBufferInfo,
            storedFramebuffer,
            fullSegmentation.frameBufferInfo,
            this
        )
        val storeStage = StoreStage(
            context,
            maskStage.frameBufferInfo,
            storedFramebuffer,
            this
        )


        val cornerDetection = fullCornerDetection(context, storeStage, this)
        //val draggablePointsStage = DraggablePointsStage(this)
        val drawCorners = DrawCornersStage(
            context,
            this,
            cornerDetection
        )


        val resolutionStage = ResolutionPointsStage(this)
        val perspectiveCorrection = fullPerspectiveCorrection(
            context,
            storeStage,
            cornerDetection,
            resolutionStage,
            this
        )

        val binarizationStage = BinarizationStage(
            context,
            perspectiveCorrection.frameBufferInfo,
            10,
            20f,
            this
        )

        val overlay = OverlayStage(
            context,
            binarizationStage.frameBufferInfo,
            drawCorners.frameBufferInfo,
            this
        )
*/

        val (a, b) = fullCornerDetectionWithDebugDrawing(context, cameraXStage, this)

        DrawFramebufferStage(
            context,
            b.frameBufferInfo,
            this
        )
    }


    fun draw() {
        stages.forEach { stage -> stage.performUpdate() }
    }

    fun onResolutionChanged(resolution: Size) {
        stages.forEach { stage -> stage.performOnResolutionChanged(resolution) }
    }

    internal fun allocateFramebuffer(stage: Stage, textureFormat: Int, width: Int, height: Int): FramebufferInfo {
        val fboHandle = glGenFramebuffer()

        val textureHandle = glGenTexture()

        val textureUnitPair = allocateTextureUnit(stage)
        glActiveTexture(textureUnitPair.textureUnit)

        glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
        glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, GLES20.GL_UNSIGNED_BYTE, null)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        glBindFramebuffer(fboHandle)
        glFramebufferTexture2D(GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureHandle)

        return FramebufferInfo(fboHandle, textureHandle, textureUnitPair, GLES20.GL_RGBA, Size(width, height))
    }

    internal fun addStage(stage: Stage){
        stages.add(stage)
    }

    internal fun allocateTextureUnit(stage: Stage): TextureUnitPair {
        val textureUnitIndex = nextTextureUnit++;
        val textureUnit = indexToTextureUnit[textureUnitIndex]
        return TextureUnitPair(textureUnit, textureUnitIndex)
    }
}
