package dk.scuffed.whiteboardapp.pipeline

import android.content.Context
import android.opengl.GLES20
import android.util.Size
import dk.scuffed.whiteboardapp.opengl.*
import dk.scuffed.whiteboardapp.pipeline.stage_combinations.*
import dk.scuffed.whiteboardapp.pipeline.stages.*
import dk.scuffed.whiteboardapp.pipeline.stages.input_stages.CameraXStage
import dk.scuffed.whiteboardapp.pipeline.stages.output_stages.DrawFramebufferStage

internal class Pipeline(context: Context, private val initialResolution: Size) : IPipeline {

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

        val entirePipeline = fullPipeline(context, cameraXStage, this)

        DrawFramebufferStage(
            context,
            entirePipeline.frameBufferInfo,
            this
        )
    }


    override fun draw() {
        stages.forEach { stage -> stage.performUpdate() }
    }

    override fun onResolutionChanged(resolution: Size) {
        stages.forEach { stage -> stage.performOnResolutionChanged(resolution) }
    }

    override fun getInitialResolution(): Size {
        return initialResolution
    }

    override fun allocateFramebuffer(
        stage: Stage,
        textureFormat: Int,
        size: Size
    ): FramebufferInfo {
        val fboHandle = glGenFramebuffer()

        val textureHandle = glGenTexture()

        val textureUnitPair = allocateTextureUnit(stage)
        glActiveTexture(textureUnitPair.textureUnit)

        glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
        glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, size, GLES20.GL_UNSIGNED_BYTE, null)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        glBindFramebuffer(fboHandle)
        glFramebufferTexture2D(GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureHandle)

        return FramebufferInfo(fboHandle, textureHandle, textureUnitPair, GLES20.GL_RGBA, size)
    }

    override fun addStage(stage: Stage) {
        stages.add(stage)
    }

    override fun allocateTextureUnit(stage: Stage): TextureUnitPair {
        val textureUnitIndex = nextTextureUnit++
        val textureUnit = indexToTextureUnit[textureUnitIndex]
        return TextureUnitPair(textureUnit, textureUnitIndex)
    }
}
