package dk.scuffed.whiteboardapp.pipeline

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.util.Size
import dk.scuffed.whiteboardapp.opengl.*
import dk.scuffed.whiteboardapp.pipeline.stages.*
import dk.scuffed.whiteboardapp.pipeline.stages.openCVPlaceholders.OpenCVDilateStage
import dk.scuffed.whiteboardapp.segmentation.PPSegmentation

class Pipeline(context: Context) {

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
        glClearColor(1.0f, 0.0f, 1.0f, 1.0f)


        val cameraXStage = CameraXStage(
            context,
            this
        )

        val segPreProcess = SegmentationPreProcessingStage(
            context,
            cameraXStage.frameBufferInfo,
            Size(256, 144),
            this
        )

        val convertBitmap = FramebufferToBitmapStage(
            segPreProcess.frameBufferInfo,
            Bitmap.Config.ARGB_8888,
            this,
        )

        val segStage = SegmentationStage(
            context,
            PPSegmentation.Model.PORTRAIT,
            convertBitmap.outputBitmap,
            this,
        )

        val dilate = OpenCVDilateStage(
            segStage,
            1.0,
            this
        )

        val convertFramebuffer = BitmapToFramebufferStage(
            dilate,
            this,
        )

        val segPostProcess = SegmentationPostProcessingStage(
            context,
            convertFramebuffer.frameBufferInfo,
            cameraXStage.frameBufferInfo.textureSize,
            this
        )



        val grayscale = GrayscaleStage(
            context,
            cameraXStage.frameBufferInfo,
            this,
        )

        val gaussianx = GaussianBlurStage(
            context,
            grayscale.frameBufferInfo,
            true,
            this,
        )

        val gaussiany = GaussianBlurStage(
            context,
            gaussianx.frameBufferInfo,
            false,
            this,
        )

        val sobelStage = SobelStage(
            context,
            gaussiany.frameBufferInfo,
            this,
        )
        /*
        val convertBitmap = FramebufferToBitmapStage(
            cameraXStage.frameBufferInfo,
            Bitmap.Config.ARGB_8888,
            this,
        )
        
        var convertBitmap = FramebufferToBitmapStage(
            segPreProcess.frameBufferInfo,
            Bitmap.Config.ARGB_8888,
            this,
        )

        
       val segStage = SegmentationStage(
            context,
            PPSegmentation.Model.PORTRAIT,

        val grayscaleCVStage = OpenCVGrayScaleStage(
            convertBitmap.outputBitmap,
            this,
        )

        val gaussianBlurCVStage = OpenCVGaussianBlurStage(
            grayscaleCVStage.outputBitmap,
            this,
        )

        val sobelCVStage =  OpenCVSobelStage(
            gaussianBlurCVStage.outputBitmap,
            this,
        )

        val cannyCVStage = OpenCVCannyStage(
            sobelCVStage.outputBitmap,
            this,
        )



        DrawFramebufferStage(
            context,
            segPostProcess.frameBufferInfo,
            this
        )

        val convertFramebuffer = BitmapToFramebufferStage(
            cannyCVStage,
            this,
        )

        val storeStageFramebufferInfo : FramebufferInfo =
            allocateFramebuffer(cameraXStage, GLES20.GL_RGBA,
                cameraXStage.frameBufferInfo.textureSize.width,
                cameraXStage.frameBufferInfo.textureSize.height
            )

        val maskStage = MaskingStage(
            context,
            sobelStage.frameBufferInfo,
            storeStageFramebufferInfo,
            segPostProcess.frameBufferInfo,
            this
        )

        val storeStage = StoreStage(
            context,
            maskStage.frameBufferInfo,
            storeStageFramebufferInfo,
            this
        )

        val overlay = OverlayStage(
            context,
            maskStage.frameBufferInfo,
            segPostProcess.frameBufferInfo,
            this
        )
        */

        DrawFramebufferStage(
            context,
            binarize.frameBufferInfo,
            this
        )
    }

    fun draw() {
        stages.forEach { stage -> stage.performUpdate() }
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
