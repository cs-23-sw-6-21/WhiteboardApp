package dk.scuffed.whiteboardapp.pipeline

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.util.Size
import dk.scuffed.whiteboardapp.opengl.*
import dk.scuffed.whiteboardapp.pipeline.stages.*
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.FramebufferToBitmapStage
import dk.scuffed.whiteboardapp.pipeline.stages.input_stages.CameraXStage
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.OpenCVLineDetectionStage
import dk.scuffed.whiteboardapp.pipeline.stages.lines_stages.LinesAngleDiscriminatorStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.*
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.GaussianBlurStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.GrayscaleStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.OverlayStage
import dk.scuffed.whiteboardapp.pipeline.stages.output_stages.DrawFramebufferStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DrawLinesStage
import dk.scuffed.whiteboardapp.utils.Color

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
        glClearColorError()


        val cameraXStage = CameraXStage(
            context,
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
            this
        )

        val cannyStage = CannyStage(
            context,
            sobelStage.frameBufferInfo,
            0.1f,
            0.2f,
            this
        )

        val cannyBitmapStage = FramebufferToBitmapStage(
            cannyStage.frameBufferInfo,
            Bitmap.Config.ARGB_8888,
            this
        )

        val openCVLineDetectionStage = OpenCVLineDetectionStage(
            cannyBitmapStage,
            150,
            this
        )

        val verticalLinesAngleDiscriminatorStage = LinesAngleDiscriminatorStage(
            openCVLineDetectionStage,
            -(Math.PI / 4.0f).toFloat(),
            (Math.PI / 4.0f).toFloat(),
            this
        )

        val verticalDrawLinesStage = DrawLinesStage(
            context,
            verticalLinesAngleDiscriminatorStage,
            Color(1.0f, 0.0f, 0.0f, 1.0f),
            this
        )

        val horizontalLinesAngleDiscriminatorStage = LinesAngleDiscriminatorStage(
            openCVLineDetectionStage,
            (Math.PI / 4.0f).toFloat(),
            (Math.PI / 2.0f + Math.PI / 4.0f).toFloat(),
            this
        )

        val horizontalDrawLinesStage = DrawLinesStage(
            context,
            horizontalLinesAngleDiscriminatorStage,
            Color(0.0f, 1.0f, 0.0f, 1.0f),
        this
        )


        val verticalOverlayStage = OverlayStage(
            context,
            cannyStage.frameBufferInfo,
            verticalDrawLinesStage.frameBufferInfo,
            this
        )

        val horizontalOverlayStage = OverlayStage(
            context,
            verticalOverlayStage.frameBufferInfo,
            horizontalDrawLinesStage.frameBufferInfo,
            this
        )

        DrawFramebufferStage(
            context,
            horizontalOverlayStage.frameBufferInfo,
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
