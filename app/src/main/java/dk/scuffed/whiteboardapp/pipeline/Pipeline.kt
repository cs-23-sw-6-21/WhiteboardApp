package dk.scuffed.whiteboardapp.pipeline

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Camera
import android.opengl.GLES20
import android.util.Size
import dk.scuffed.whiteboardapp.R
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
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.*
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DraggablePointsStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.PerspectiveTransformPointsStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.StaticPointsStage
import dk.scuffed.whiteboardapp.utils.Color
import dk.scuffed.whiteboardapp.utils.Vec2Int

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


        val opt = BitmapFactory.Options()
        opt.inScaled = false

        val textureStage = CameraXStage(
            context,
            this
        )

        val draggablePointsStage = DraggablePointsStage(
            this
        )

        val screenPoints = StaticPointsStage(
            this,
            Vec2Int(0, 0),
            Vec2Int(0, 1920),
            Vec2Int(1080, 1920),
            Vec2Int(1080, 0),
        )


        val perspectivePoints = PerspectiveTransformPointsStage(
            this,
            draggablePointsStage,
            screenPoints
        )

        val drawLinesStage = DrawCornersStage(
            context,
            this,
            draggablePointsStage
            )



        val corrected = PerspectiveCorrectionStage(
            context,
            textureStage.frameBufferInfo,
            perspectivePoints,
            this,
        )
        val overlayStage = OverlayStage(
            context,
            corrected.frameBufferInfo,
            drawLinesStage.frameBufferInfo,
            this
        )


        DrawFramebufferStage(
            context,
            overlayStage.frameBufferInfo,
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
