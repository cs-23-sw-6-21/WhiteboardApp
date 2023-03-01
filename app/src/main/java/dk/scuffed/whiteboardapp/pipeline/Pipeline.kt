package dk.scuffed.whiteboardapp.pipeline

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLES20
import android.util.Size
import dk.scuffed.whiteboardapp.openGL.*
import dk.scuffed.whiteboardapp.pipeline.stages.*
import dk.scuffed.whiteboardapp.pipeline.stages.BitmapToFramebufferStage
import dk.scuffed.whiteboardapp.pipeline.stages.CameraXStage
import dk.scuffed.whiteboardapp.pipeline.stages.DrawFramebufferStage
import dk.scuffed.whiteboardapp.pipeline.stages.FramebufferToBitmapStage
import dk.scuffed.whiteboardapp.pipeline.stages.openCVPlaceholders.OpenCVGaussianBlurStage
import dk.scuffed.whiteboardapp.pipeline.stages.openCVPlaceholders.OpenCVGrayScaleStage
import dk.scuffed.whiteboardapp.pipeline.stages.openCVPlaceholders.OpenCVSobelStage
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

        var grayscale = GrayscaleStage(
            context,
            cameraXStage.frameBufferInfo,
            this,
        )

        var gaussianx = GaussianBlurStage(
            context,
            grayscale.frameBufferInfo,
            true,
            this,
        )

        var gaussiany = GaussianBlurStage(
            context,
            gaussianx.frameBufferInfo,
            false,
            this,
        )

        var sobelStage = SobelStage(
            context,
            gaussiany.frameBufferInfo,
            this,
        )

        /*var convertBitmap = FramebufferToBitmapStage(
            cameraXStage.frameBufferInfo,
            Bitmap.Config.ARGB_8888,
            this,
        )

        var convertFramebuffer = BitmapToFramebufferStage(
            sobelCVStage,
            this,
        )
        */

        DrawFramebufferStage(
            context,
            sobelStage.frameBufferInfo,
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