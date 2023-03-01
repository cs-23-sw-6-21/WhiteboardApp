package dk.scuffed.whiteboardapp.pipeline.stages

import android.content.Context
import android.opengl.GLES20
import android.util.Size
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.openGL.*
import dk.scuffed.whiteboardapp.pipeline.*


internal class MaskingStage(
    context: Context,
    private val inputFrameBufferInfo1: FramebufferInfo,
    private val inputFrameBufferInfo2: FramebufferInfo,
    private val maskFrameBufferInfo: FramebufferInfo,
    pipeline: Pipeline): GLOutputStage(context, R.raw.vertex_shader, R.raw.masking, pipeline) {

    init {
        setup()
    }

    override fun setupFramebufferInfo() {
        val resolution = Size(inputFrameBufferInfo1.textureSize.width, inputFrameBufferInfo1.textureSize.height)
        allocateFramebuffer(GLES20.GL_RGBA, resolution)

    }

    override fun setupUniforms(program: Int) {
        super.setupUniforms(program)

        // Input framebuffer resolution
        val framebufferResolutionHandle = glGetUniformLocation(program, "samplerResolution")
        glUniform2f(framebufferResolutionHandle, inputFrameBufferInfo1.textureSize.width.toFloat(), inputFrameBufferInfo1.textureSize.height.toFloat())

        val maskResolutionHandle = glGetUniformLocation(program, "maskResolution")
        glUniform2f(maskResolutionHandle, maskFrameBufferInfo.textureSize.width.toFloat(), maskFrameBufferInfo.textureSize.height.toFloat())


        // Input framebuffer
        val framebufferTextureHandle1 = glGetUniformLocation(program, "sampler1")
        glUniform1i(framebufferTextureHandle1, inputFrameBufferInfo1.textureUnitPair.textureUnitIndex)
        glActiveTexture(inputFrameBufferInfo1.textureUnitPair.textureUnit)
        glBindTexture(GLES20.GL_TEXTURE_2D, inputFrameBufferInfo1.textureHandle)

        val framebufferTextureHandle2 = glGetUniformLocation(program, "sampler2")
        glUniform1i(framebufferTextureHandle2, inputFrameBufferInfo2.textureUnitPair.textureUnitIndex)
        glActiveTexture(inputFrameBufferInfo2.textureUnitPair.textureUnit)
        glBindTexture(GLES20.GL_TEXTURE_2D, inputFrameBufferInfo2.textureHandle)

        val maskTextureHandle = glGetUniformLocation(program, "mask")
        glUniform1i(maskTextureHandle, maskFrameBufferInfo.textureUnitPair.textureUnitIndex)
        glActiveTexture(maskFrameBufferInfo.textureUnitPair.textureUnit)
        glBindTexture(GLES20.GL_TEXTURE_2D, maskFrameBufferInfo.textureHandle)
    }
}