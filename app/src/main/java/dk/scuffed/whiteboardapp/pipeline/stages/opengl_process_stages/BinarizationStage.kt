package dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages

import android.content.Context
import android.opengl.GLES20
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.opengl.*
import dk.scuffed.whiteboardapp.pipeline.FramebufferInfo
import dk.scuffed.whiteboardapp.pipeline.stages.GLOutputStage
import dk.scuffed.whiteboardapp.pipeline.Pipeline

/**
 * Does simple binarization using a global threshold with a shader.
 */
internal class BinarizationStage(context: Context, private val inputFramebufferInfo: FramebufferInfo, pipeline: Pipeline) : GLOutputStage(context, R.raw.vertex_shader, R.raw.binarization_shader, pipeline) {

    init {
        setup()
    }

    override fun setupFramebufferInfo() {
        allocateFramebuffer(GLES20.GL_RGBA, inputFramebufferInfo.textureSize)
    }

    override fun setupUniforms(program: Int) {
        super.setupUniforms(program)

        // Input framebuffer
        val framebufferTextureHandle = glGetUniformLocation(program, "framebuffer")
        glUniform1i(framebufferTextureHandle, inputFramebufferInfo.textureUnitPair.textureUnitIndex)
        glActiveTexture(inputFramebufferInfo.textureUnitPair.textureUnit)
        glBindTexture(GLES20.GL_TEXTURE_2D, inputFramebufferInfo.textureHandle)

        val windowSize = glGetUniformLocation(program, "windowSize")
        glUniform1i(windowSize, 10)

        val threshold = glGetUniformLocation(program, "threshold")
        glUniform1f(threshold, 20f)
    }
}