package dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages

import android.content.Context
import android.opengl.GLES20
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.opengl.*
import dk.scuffed.whiteboardapp.pipeline.FramebufferInfo
import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.stages.GLOutputStage

/**
 * Performs whitebalancing on input1 based on input2, which should represent the background color of the whiteboard.
 */
internal class WhitebalanceStage(
    context: Context,
    private val inputFramebufferInfo1: FramebufferInfo,
    private val inputFramebufferInfo2: FramebufferInfo,
    pipeline: IPipeline
) : GLOutputStage(context, R.raw.vertex_shader, R.raw.whitebalance_shader, pipeline) {
    init {
        setup()
    }

    override fun setupFramebufferInfo() {
        allocateFramebuffer(GLES20.GL_RGBA, inputFramebufferInfo2.textureSize)
    }

    override fun setupUniforms(program: Int) {
        super.setupUniforms(program)

        // We don't need the framebuffer resolution as it is the same as resolution :^)

        // Input framebuffer
        val framebufferTextureHandle1 = glGetUniformLocation(program, "framebuffer1")
        glUniform1i(framebufferTextureHandle1, inputFramebufferInfo1.textureUnitPair.textureUnitIndex)
        glActiveTexture(inputFramebufferInfo1.textureUnitPair.textureUnit)
        glBindTexture(GLES20.GL_TEXTURE_2D, inputFramebufferInfo1.textureHandle)

        // Input framebuffer
        val framebufferTextureHandle2 = glGetUniformLocation(program, "framebuffer2")
        glUniform1i(framebufferTextureHandle2, inputFramebufferInfo2.textureUnitPair.textureUnitIndex)
        glActiveTexture(inputFramebufferInfo2.textureUnitPair.textureUnit)
        glBindTexture(GLES20.GL_TEXTURE_2D, inputFramebufferInfo2.textureHandle)

    }
}