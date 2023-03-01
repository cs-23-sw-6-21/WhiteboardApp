package dk.scuffed.whiteboardapp.pipeline.stages

import android.content.Context
import android.opengl.GLES20
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.opengl.glActiveTexture
import dk.scuffed.whiteboardapp.opengl.glBindTexture
import dk.scuffed.whiteboardapp.opengl.glGetUniformLocation
import dk.scuffed.whiteboardapp.opengl.glUniform1i
import dk.scuffed.whiteboardapp.pipeline.FramebufferInfo
import dk.scuffed.whiteboardapp.pipeline.GLOutputStage
import dk.scuffed.whiteboardapp.pipeline.Pipeline

internal class OverlayStage(
    context: Context,
    private val backgroundFramebufferInfo: FramebufferInfo,
    private val foregroundFramebufferInfo: FramebufferInfo,
    pipeline: Pipeline
) : GLOutputStage(context, R.raw.vertex_shader, R.raw.overlay_shader, pipeline) {

    init {
        // FIXME: For now we only support framebuffers with the same size
        assert(backgroundFramebufferInfo.textureSize == foregroundFramebufferInfo.textureSize)

        setup()
    }

    override fun setupFramebufferInfo() {
        allocateFramebuffer(GLES20.GL_RGBA, backgroundFramebufferInfo.textureSize)
    }

    override fun setupUniforms(program: Int) {
        super.setupUniforms(program)

        val backgroundFramebufferHandle = glGetUniformLocation(program, "background_framebuffer")
        glUniform1i(backgroundFramebufferHandle, backgroundFramebufferInfo.textureUnitPair.textureUnitIndex)
        glActiveTexture(backgroundFramebufferInfo.textureUnitPair.textureUnit)
        glBindTexture(GLES20.GL_TEXTURE_2D, backgroundFramebufferInfo.textureHandle)


        val foregroundFramebufferHandle = glGetUniformLocation(program, "foreground_framebuffer")
        glUniform1i(foregroundFramebufferHandle, foregroundFramebufferInfo.textureUnitPair.textureUnitIndex)
        glActiveTexture(foregroundFramebufferInfo.textureUnitPair.textureUnit)
        glBindTexture(GLES20.GL_TEXTURE_2D, foregroundFramebufferInfo.textureHandle)
    }
}