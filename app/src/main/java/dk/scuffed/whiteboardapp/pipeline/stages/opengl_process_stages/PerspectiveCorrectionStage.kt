package dk.scuffed.whiteboardapp.pipeline.stages

import android.content.Context
import android.opengl.GLES20
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.opengl.*
import dk.scuffed.whiteboardapp.pipeline.FramebufferInfo
import dk.scuffed.whiteboardapp.pipeline.GLOutputStage
import dk.scuffed.whiteboardapp.pipeline.Pipeline

internal class PerspectiveCorrectionStage(context: Context, private val inputFramebufferInfo: FramebufferInfo, pipeline: Pipeline)
    : GLOutputStage(context,
    R.raw.vertex_perspective_correction_shader,
    R.raw.texture_distortable, pipeline) {
    init {
        setup()
    }

    override fun setupFramebufferInfo() {
        allocateFramebuffer(GLES20.GL_RGBA, inputFramebufferInfo.textureSize)
    }

    override fun setupUniforms(program: Int) {
        super.setupUniforms(program)

        // We don't need the framebuffer resolution as it is the same as resolution :^)

        // Input framebuffer
        val framebufferTextureHandle = glGetUniformLocation(program, "source_texture")
        glUniform1i(framebufferTextureHandle, inputFramebufferInfo.textureUnitPair.textureUnitIndex)
        glActiveTexture(inputFramebufferInfo.textureUnitPair.textureUnit)
        glBindTexture(GLES20.GL_TEXTURE_2D, inputFramebufferInfo.textureHandle)
    }
}